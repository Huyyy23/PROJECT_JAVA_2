package GUI;

import BUS.NhaCungCapBUS;
import BUS.SanPhamBUS;
import DTO.NhaCungCapDTO;
import DTO.SanPhamDTO;
import DTO.SharedData;
import UTIL.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * NhapHangPanelGUI — Quản lý phiếu nhập hàng
 *
 * LUỒNG NGHIỆP VỤ:
 *   1. Lập phiếu → chọn NCC + thêm SP → nhấn "Lưu tạm"
 *      → INSERT PHIEUNHAP(TrangThai='ChoXuLy')
 *      → INSERT CHITIETPHIEUNHAP
 *      → KHÔNG sinh serial, KHÔNG cộng SoLuongTon
 *
 *   2. Khi đã thanh toán → mở phiếu ChoXuLy → nhấn "Xác nhận hoàn thành"
 *      → INSERT SERIAL (batch)
 *      → UPDATE SANPHAM.SoLuongTon += SoLuong (cho từng SP)
 *      → UPDATE PHIEUNHAP.TrangThai = 'HoanThanh'
 *
 *   3. Hủy phiếu ChoXuLy → chỉ đổi TrangThai='Huy', không cần rollback kho
 *      Hủy phiếu HoanThanh → trigger trg_PhieuNhap_Huy tự trừ kho + serial→Loi
 *
 * SCHEMA (SQL mới):
 *   PHIEUNHAP.TrangThai: HoanThanh | Huy | ChoXuLy
 *   PHIEUNHAP.MaNhaCungCap NOT NULL
 *   CHITIETPHIEUNHAP.ThanhTien: computed persisted (SoLuong * DonGiaNhap)
 */
public class NhapHangPanelGUI extends JPanel {

    // ── COLORS ────────────────────────────────────────────────────────
    private static final Color PRIMARY      = new Color(21, 101, 192);
    private static final Color PRIMARY_DARK = new Color(10,  60, 130);
    private static final Color ACCENT       = new Color(0,  188, 212);
    private static final Color CONTENT_BG   = new Color(236, 242, 250);
    private static final Color WHITE        = Color.WHITE;
    private static final Color ROW_ALT      = new Color(245, 250, 255);
    private static final Color TABLE_HEADER = new Color(21, 101, 192);
    private static final Color SUCCESS      = new Color(46, 125,  50);
    private static final Color DANGER       = new Color(198, 40,  40);
    private static final Color WARNING      = new Color(230, 120,   0);
    private static final Color CARD_BORDER  = new Color(187, 222, 251);

    // ── FONTS ─────────────────────────────────────────────────────────
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_STAT   = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_TOTAL  = new Font("Segoe UI", Font.BOLD,  16);

    // ── COMPONENTS ────────────────────────────────────────────────────
    private JTextField        txtSearch;
    private JComboBox<String> cbTrangThaiFilter;
    private JComboBox<String> cbSort;
    private JTable            tablePhieu;
    private DefaultTableModel modelPhieu;
    private JLabel            lblTongPN, lblHoanThanh, lblChoXuLy, lblDaHuy, lblRecordCount;

    // ── BUS ───────────────────────────────────────────────────────────
    private final NhaCungCapBUS nhaCungCapBUS = new NhaCungCapBUS();
    private final SanPhamBUS    sanPhamBUS    = new SanPhamBUS();

    // ── DATA ──────────────────────────────────────────────────────────
    /** [MaPN, NgayNhap, TenNCC, TenNV, TongTien(BigDecimal), TrangThai] */
    private final List<Object[]> dsPhieuNhap = new ArrayList<>();

    // =================================================================
    public NhapHangPanelGUI() {
        setLayout(new BorderLayout(8, 8));
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(createTitlePanel(),  BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { loadData(); }
        });
    }

    // =================================================================
    // DATA
    // =================================================================
    private void loadData() {
        dsPhieuNhap.clear();
        String sql =
            "SELECT pn.MaPN, CONVERT(VARCHAR,pn.NgayNhap,23) AS NgayNhap, " +
            "       ncc.TenNhaCungCap, nv.TenNV, pn.TongTien, pn.TrangThai " +
            "FROM PHIEUNHAP pn " +
            "JOIN NHACUNGCAP ncc ON pn.MaNhaCungCap = ncc.MaNhaCungCap " +
            "JOIN NHANVIEN   nv  ON pn.MaNV = nv.MaNV " +
            "ORDER BY pn.MaPN DESC";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                dsPhieuNhap.add(new Object[]{
                    rs.getInt("MaPN"),
                    rs.getString("NgayNhap") != null ? rs.getString("NgayNhap") : "",
                    rs.getString("TenNhaCungCap"),
                    rs.getString("TenNV"),
                    rs.getBigDecimal("TongTien"),
                    rs.getString("TrangThai")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            warn("Không thể tải dữ liệu phiếu nhập:\n" + e.getMessage());
        }
        refreshTable(txtSearch != null ? txtSearch.getText().trim() : "");
        updateStatCards();
    }

    private void refreshTable(String keyword) {
        modelPhieu.setRowCount(0);
        String kw        = keyword.toLowerCase();
        int    filterIdx = cbTrangThaiFilter != null ? cbTrangThaiFilter.getSelectedIndex() : 0;

        List<Object[]> filtered = new ArrayList<>();
        for (Object[] row : dsPhieuNhap) {
            String tenNCC    = row[2].toString().toLowerCase();
            String tenNV     = row[3].toString().toLowerCase();
            String maPN      = String.valueOf(row[0]);
            String ngay      = row[1].toString();
            String trangThai = String.valueOf(row[5]);

            if (!kw.isEmpty()
                    && !tenNCC.contains(kw)
                    && !tenNV.contains(kw)
                    && !maPN.contains(kw)
                    && !ngay.contains(kw)) continue;

            // 0=Tất cả, 1=Hoàn thành, 2=Chờ xử lý, 3=Đã hủy
            switch (filterIdx) {
                case 1: if (!"HoanThanh".equals(trangThai)) continue; break;
                case 2: if (!"ChoXuLy".equals(trangThai))   continue; break;
                case 3: if (!"Huy".equals(trangThai))        continue; break;
            }
            filtered.add(row);
        }

        if (cbSort != null) {
            int sortIdx = cbSort.getSelectedIndex();
            filtered.sort((a, b) -> {
                switch (sortIdx) {
                    case 1: return b[1].toString().compareTo(a[1].toString());
                    case 2: return a[1].toString().compareTo(b[1].toString());
                    case 3: {
                        BigDecimal ba = a[4] != null ? (BigDecimal) a[4] : BigDecimal.ZERO;
                        BigDecimal bb = b[4] != null ? (BigDecimal) b[4] : BigDecimal.ZERO;
                        return bb.compareTo(ba);
                    }
                    case 4: {
                        BigDecimal ba = a[4] != null ? (BigDecimal) a[4] : BigDecimal.ZERO;
                        BigDecimal bb = b[4] != null ? (BigDecimal) b[4] : BigDecimal.ZERO;
                        return ba.compareTo(bb);
                    }
                    default: return ((Integer) b[0]).compareTo((Integer) a[0]);
                }
            });
        }

        for (Object[] row : filtered) {
            BigDecimal tien = (BigDecimal) row[4];
            modelPhieu.addRow(new Object[]{
                row[0], row[1], row[2], row[3],
                tien != null ? formatMoney(tien) : "0",
                row[5]
            });
        }
        if (lblRecordCount != null)
            lblRecordCount.setText("Tổng: " + modelPhieu.getRowCount() + " phiếu");
    }

    private void updateStatCards() {
        long tong      = dsPhieuNhap.size();
        long hoanThanh = dsPhieuNhap.stream().filter(r -> "HoanThanh".equals(r[5])).count();
        long choXuLy   = dsPhieuNhap.stream().filter(r -> "ChoXuLy".equals(r[5])).count();
        long daHuy     = dsPhieuNhap.stream().filter(r -> "Huy".equals(r[5])).count();

        lblTongPN.setText(String.valueOf(tong));
        lblHoanThanh.setText(String.valueOf(hoanThanh));
        lblChoXuLy.setText(String.valueOf(choXuLy));
        lblDaHuy.setText(String.valueOf(daHuy));
    }

    // =================================================================
    // TITLE PANEL
    // =================================================================
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 58));

        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        JLabel title = new JLabel("  QUẢN LÝ NHẬP HÀNG");
        title.setFont(FONT_TITLE); title.setForeground(WHITE);
        left.add(title, new GridBagConstraints());
        panel.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
        JTextField txtNgay = createSmallField(LocalDate.now().toString(), 110);
        txtNgay.setEditable(false);
        JButton btnLap = buildYellowButton("+ Lập phiếu nhập");
        btnLap.addActionListener(e -> openDialog(null));
        GridBagConstraints rc = new GridBagConstraints();
        rc.anchor = GridBagConstraints.CENTER; rc.insets = new Insets(0, 6, 0, 0);
        rc.gridx = 0; right.add(makeInlineLabel("Ngày:"), rc);
        rc.gridx = 1; right.add(txtNgay, rc);
        rc.gridx = 2; rc.insets = new Insets(0, 14, 0, 0); right.add(btnLap, rc);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // =================================================================
    // CENTER PANEL
    // =================================================================
    private JPanel createCenterPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(CONTENT_BG);
        p.add(createStatPanel(),  BorderLayout.NORTH);
        p.add(createTablePanel(), BorderLayout.CENTER);
        return p;
    }

    // =================================================================
    // STAT CARDS — đổi "Nhập tháng này" → "Chờ xử lý"
    // =================================================================
    private JPanel createStatPanel() {
        JPanel p = new JPanel(new GridLayout(1, 4, 12, 0));
        p.setBackground(CONTENT_BG);
        p.setPreferredSize(new Dimension(0, 90));

        lblTongPN    = new JLabel("0", SwingConstants.CENTER);
        lblHoanThanh = new JLabel("0", SwingConstants.CENTER);
        lblChoXuLy   = new JLabel("0", SwingConstants.CENTER);
        lblDaHuy     = new JLabel("0", SwingConstants.CENTER);

        p.add(buildStatCard("Tổng phiếu nhập", lblTongPN,    PRIMARY,                 "📦"));
        p.add(buildStatCard("Hoàn thành",       lblHoanThanh, SUCCESS,                 "✅"));
        p.add(buildStatCard("Chờ xử lý",        lblChoXuLy,   WARNING,                 "⏳"));
        p.add(buildStatCard("Đã hủy",           lblDaHuy,     DANGER,                  "❌"));
        return p;
    }

    private JPanel buildStatCard(String title, JLabel valueLbl, Color color, String emoji) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 14));
                g2.fillRoundRect(4, 4, getWidth()-4, getHeight()-4, 14, 14);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth()-4, getHeight()-4, 14, 14);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth()-4, 6, 6, 6);
                g2.fillRect(0, 3, getWidth()-4, 3);
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 12));

        JLabel lblEmoji = new JLabel(emoji);
        lblEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        valueLbl.setFont(FONT_STAT); valueLbl.setForeground(color);
        valueLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(100, 120, 150));

        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        top.add(lblEmoji, BorderLayout.WEST); top.add(valueLbl, BorderLayout.EAST);
        card.add(top, BorderLayout.CENTER); card.add(lblTitle, BorderLayout.SOUTH);
        return card;
    }

    // =================================================================
    // TABLE PANEL
    // =================================================================
    private JPanel createTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER,1), BorderFactory.createEmptyBorder(10,10,10,10)));
        p.add(buildToolbar(),    BorderLayout.NORTH);
        p.add(buildPhieuTable(), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(0, 6));
        toolbar.setBackground(WHITE);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // Dòng 1
        JPanel row1 = new JPanel(new BorderLayout(8, 0)); row1.setBackground(WHITE);

        JPanel searchBar = new JPanel(new BorderLayout());
        searchBar.setBackground(WHITE); searchBar.setPreferredSize(new Dimension(340, 36));
        searchBar.setBorder(new CompoundBorder(new LineBorder(new Color(180,210,240),1,true), BorderFactory.createEmptyBorder()));

        JComponent searchIcon = new JComponent() {
            { setPreferredSize(new Dimension(36,36)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=13, cy=getHeight()/2-1, r=7;
                g2.setColor(new Color(160,185,220)); g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g2.drawOval(cx-r,cy-r,r*2,r*2); g2.drawLine(cx+r-2,cy+r-2,cx+r+4,cy+r+4); g2.dispose();
            }
        };
        txtSearch = new JTextField(); txtSearch.setFont(FONT_NORMAL);
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0,4,0,0)); txtSearch.setOpaque(false);
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { refreshTable(txtSearch.getText().trim()); }
        });
        JButton btnSearch = buildActionButton("Tìm kiếm", PRIMARY, WHITE);
        btnSearch.setPreferredSize(new Dimension(100,36));
        btnSearch.addActionListener(e -> refreshTable(txtSearch.getText().trim()));

        searchBar.add(searchIcon, BorderLayout.WEST);
        searchBar.add(txtSearch,  BorderLayout.CENTER);
        searchBar.add(btnSearch,  BorderLayout.EAST);

        JButton btnXem      = buildActionButton("Xem chi tiết",  PRIMARY,                WHITE);
        JButton btnHoanThanh= buildActionButton("Hoàn thành",    SUCCESS,                WHITE);
        JButton btnHuy      = buildActionButton("Hủy phiếu",     DANGER,                 WHITE);
        JButton btnReset    = buildActionButton("Làm mới",        new Color(90,100,115), WHITE);

        btnXem.setToolTipText("Xem chi tiết phiếu nhập");
        btnHoanThanh.setToolTipText("Xác nhận hoàn thành phiếu ChoXuLy: sinh serial + cộng kho");
        btnHuy.setToolTipText("Hủy phiếu nhập đang chọn");

        btnXem.addActionListener(e -> {
            int row = tablePhieu.getSelectedRow();
            if (row<0){ warn("Vui lòng chọn phiếu nhập cần xem!"); return; }
            openDialog((Integer) modelPhieu.getValueAt(row,0));
        });
        btnHoanThanh.addActionListener(e -> xacNhanHoanThanh());
        btnHuy.addActionListener(e -> huyPhieu());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbTrangThaiFilter.setSelectedIndex(0);
            cbSort.setSelectedIndex(0);
            loadData();
        });

        JPanel ag = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); ag.setBackground(WHITE);
        ag.add(btnXem); ag.add(btnHoanThanh); ag.add(btnHuy); ag.add(btnReset);
        row1.add(searchBar, BorderLayout.WEST); row1.add(ag, BorderLayout.EAST);

        // Dòng 2
        JPanel row2 = new JPanel(new BorderLayout(8,0));
        row2.setBackground(new Color(248,251,255));
        row2.setBorder(new CompoundBorder(new LineBorder(new Color(220,235,250),1), BorderFactory.createEmptyBorder(5,10,5,10)));

        // Đổi filter: thêm "Chờ xử lý"
        cbTrangThaiFilter = new JComboBox<>(new String[]{"Tất cả","Hoàn thành","Chờ xử lý","Đã hủy"});
        cbTrangThaiFilter.setFont(FONT_NORMAL); cbTrangThaiFilter.setPreferredSize(new Dimension(170,30));
        cbTrangThaiFilter.addActionListener(e -> refreshTable(txtSearch.getText().trim()));

        cbSort = new JComboBox<>(new String[]{"Sắp xếp: Mặc định","Ngày nhập (Mới nhất)","Ngày nhập (Cũ nhất)","Tổng tiền (Cao → Thấp)","Tổng tiền (Thấp → Cao)"});
        cbSort.setFont(FONT_NORMAL); cbSort.setPreferredSize(new Dimension(210,30));
        cbSort.addActionListener(e -> refreshTable(txtSearch.getText().trim()));

        lblRecordCount = new JLabel("Tổng: 0 phiếu");
        lblRecordCount.setFont(new Font("Segoe UI",Font.ITALIC,12)); lblRecordCount.setForeground(new Color(120,140,170));

        JPanel fl = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); fl.setOpaque(false);
        JLabel lf=new JLabel("Lọc:"); lf.setFont(FONT_LABEL); lf.setForeground(PRIMARY);
        JLabel ls=new JLabel("Sắp xếp:"); ls.setFont(FONT_LABEL); ls.setForeground(PRIMARY);
        fl.add(lf); fl.add(cbTrangThaiFilter); fl.add(Box.createHorizontalStrut(12)); fl.add(ls); fl.add(cbSort);
        JPanel fr = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); fr.setOpaque(false); fr.add(lblRecordCount);
        row2.add(fl, BorderLayout.WEST); row2.add(fr, BorderLayout.EAST);

        JPanel rows = new JPanel(new BorderLayout(0,6)); rows.setBackground(WHITE);
        rows.add(row1, BorderLayout.NORTH); rows.add(row2, BorderLayout.SOUTH);
        toolbar.add(rows, BorderLayout.CENTER);
        return toolbar;
    }

    private JScrollPane buildPhieuTable() {
        String[] cols = {"Mã phiếu","Ngày nhập","Nhà cung cấp","Nhân viên","Tổng tiền (đ)","Trạng thái"};
        modelPhieu = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablePhieu = new JTable(modelPhieu) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                JLabel c = (JLabel) super.prepareRenderer(r, row, col);
                c.setFont(FONT_NORMAL);
                c.setHorizontalAlignment(col==0||col==4||col==5 ? SwingConstants.CENTER : SwingConstants.LEFT);
                if (!isRowSelected(row)) c.setBackground(row%2==0 ? WHITE : ROW_ALT);
                else                     c.setBackground(new Color(187,222,251));
                if (col == 5) {
                    String val = modelPhieu.getValueAt(row,col).toString();
                    switch (val) {
                        case "HoanThanh":
                            c.setText("  ✅ Hoàn thành"); c.setForeground(SUCCESS); break;
                        case "ChoXuLy":
                            c.setText("  ⏳ Chờ xử lý"); c.setForeground(WARNING); break;
                        default:
                            c.setText("  ❌ Đã hủy");    c.setForeground(DANGER);  break;
                    }
                    c.setFont(new Font("Segoe UI",Font.BOLD,12));
                } else { c.setForeground(new Color(30,40,60)); }
                return c;
            }
        };
        styleTable(tablePhieu);
        tablePhieu.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablePhieu.getColumnModel().getColumn(1).setPreferredWidth(110);
        tablePhieu.getColumnModel().getColumn(2).setPreferredWidth(240);
        tablePhieu.getColumnModel().getColumn(3).setPreferredWidth(160);
        tablePhieu.getColumnModel().getColumn(4).setPreferredWidth(150);
        tablePhieu.getColumnModel().getColumn(5).setPreferredWidth(140);
        tablePhieu.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2 && tablePhieu.getSelectedRow()>=0)
                    openDialog((Integer) modelPhieu.getValueAt(tablePhieu.getSelectedRow(),0));
            }
        });
        JScrollPane sc = new JScrollPane(tablePhieu);
        sc.setBorder(new LineBorder(new Color(180,210,240),1));
        sc.getVerticalScrollBar().setUnitIncrement(16);
        return sc;
    }

    // =================================================================
    // XÁC NHẬN HOÀN THÀNH (từ danh sách — phiếu ChoXuLy)
    // Sinh serial + cộng kho + đổi TrangThai='HoanThanh'
    // =================================================================
    private void xacNhanHoanThanh() {
        int row = tablePhieu.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn phiếu nhập cần xác nhận hoàn thành!"); return; }
        String trangThai = modelPhieu.getValueAt(row, 5).toString();
        if (!"ChoXuLy".equals(trangThai)) {
            warn("Chỉ có thể xác nhận hoàn thành phiếu đang ở trạng thái 'Chờ xử lý'!\n"
               + "Phiếu đang chọn: " + formatTrangThai(trangThai));
            return;
        }
        int maPN = (Integer) modelPhieu.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this,
            "<html>Xác nhận hoàn thành phiếu nhập <b>#" + maPN + "</b>?<br><br>"
            + "• Serial sẽ được sinh tự động<br>"
            + "• Tồn kho sẽ được cộng ngay<br>"
            + "• Không thể hoàn tác sau khi xác nhận</html>",
            "Xác nhận hoàn thành", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        thucHienHoanThanh(maPN);
    }

    /**
     * Thực hiện hoàn thành phiếu ChoXuLy:
     * 1. Đọc chi tiết từ CHITIETPHIEUNHAP
     * 2. Sinh SERIAL (batch)
     * 3. Cộng SoLuongTon vào SANPHAM
     * 4. Đổi TrangThai='HoanThanh'
     * Tất cả trong 1 transaction.
     */
    private void thucHienHoanThanh(int maPN) {
        Connection cn = null;
        try {
            cn = DBConnection.getConnection();
            cn.setAutoCommit(false);

            // Đọc chi tiết phiếu
            List<int[]> chiTietRows = new ArrayList<>(); // [maSP, maChiTietPN, soLuong]
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT MaSP, MaChiTietPN, SoLuong FROM CHITIETPHIEUNHAP WHERE MaPN=?")) {
                ps.setInt(1, maPN);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    chiTietRows.add(new int[]{
                        rs.getInt("MaSP"),
                        rs.getInt("MaChiTietPN"),
                        rs.getInt("SoLuong")
                    });
                }
            }
            if (chiTietRows.isEmpty()) throw new SQLException("Phiếu #" + maPN + " không có chi tiết nào!");

            int totalSerial = 0;
            Date ngayNhap = Date.valueOf(LocalDate.now());

            // Sinh serial + cộng kho
            String sqlSerial  = "INSERT INTO SERIAL (SerialCode,MaSP,MaChiTietPN,TrangThai,NgayNhap) VALUES (?,?,?,N'TrongKho',?)";
            String sqlMaxStt  = "SELECT ISNULL(MAX(CAST(RIGHT(SerialCode,3) AS INT)),0) " +
                                "FROM SERIAL WHERE MaSP=? AND SerialCode LIKE ?";
            String sqlCongKho = "UPDATE SANPHAM SET SoLuongTon = SoLuongTon + ? WHERE MaSP = ?";

            for (int[] ct : chiTietRows) {
                int maSP = ct[0], maChiTiet = ct[1], sl = ct[2];

                // Đọc STT lớn nhất hiện có của SP này (trong transaction → lock)
                int maxStt = 0;
                try (PreparedStatement psMax = cn.prepareStatement(sqlMaxStt)) {
                    psMax.setInt(1, maSP);
                    psMax.setString(2, String.format("SP%02d-", maSP) + "%");
                    ResultSet rsMax = psMax.executeQuery();
                    if (rsMax.next()) maxStt = rsMax.getInt(1);
                }

                // Sinh serial tiếp từ maxStt+1 — format SP{02d}-{03d}
                try (PreparedStatement psS = cn.prepareStatement(sqlSerial)) {
                    for (int i = 1; i <= sl; i++) {
                        psS.setString(1, String.format("SP%02d-%03d", maSP, maxStt + i));
                        psS.setInt(2, maSP);
                        psS.setInt(3, maChiTiet);
                        psS.setDate(4, ngayNhap);
                        psS.addBatch();
                    }
                    psS.executeBatch();
                    totalSerial += sl;
                }

                // Cộng kho
                try (PreparedStatement psCK = cn.prepareStatement(sqlCongKho)) {
                    psCK.setInt(1, sl);
                    psCK.setInt(2, maSP);
                    psCK.executeUpdate();
                }
            }

            // Đổi trạng thái
            try (PreparedStatement ps = cn.prepareStatement(
                    "UPDATE PHIEUNHAP SET TrangThai=N'HoanThanh' WHERE MaPN=?")) {
                ps.setInt(1, maPN);
                ps.executeUpdate();
            }

            cn.commit();
            loadData();
            showToast("✅ Hoàn thành phiếu #" + maPN + " — " + totalSerial + " serial đã sinh");

        } catch (Exception ex) {
            if (cn != null) try { cn.rollback(); } catch (Exception ignored) {}
            ex.printStackTrace();
            warn("Lỗi khi xác nhận hoàn thành:\n" + ex.getMessage());
        } finally {
            if (cn != null) try { cn.setAutoCommit(true); cn.close(); } catch (Exception ignored) {}
        }
    }

    // =================================================================
    // HỦY PHIẾU
    // ChoXuLy → chỉ đổi TrangThai, không cần rollback kho
    // HoanThanh → trigger trg_PhieuNhap_Huy tự trừ kho + serial→Loi
    // =================================================================
    private void huyPhieu() {
        int row = tablePhieu.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn phiếu nhập cần hủy!"); return; }
        String tt = modelPhieu.getValueAt(row, 5).toString();
        if ("Huy".equals(tt)) { warn("Phiếu này đã bị hủy trước đó!"); return; }

        int maPN    = (Integer) modelPhieu.getValueAt(row, 0);
        String tenNCC = modelPhieu.getValueAt(row, 2).toString();

        String extraWarning = "HoanThanh".equals(tt)
            ? "<br><font color='red'>⚠ Phiếu đã hoàn thành — tồn kho sẽ bị trừ lại, serial → Lỗi!</font>"
            : "<br><font color='gray'>Phiếu chưa hoàn thành — tồn kho không bị ảnh hưởng.</font>";

        int ok = JOptionPane.showConfirmDialog(this,
            "<html>Xác nhận hủy phiếu nhập <b>#" + maPN + "</b>?<br>"
            + "Nhà cung cấp: <b>" + tenNCC + "</b>"
            + extraWarning
            + "<br><br><b>Hành động này không thể hoàn tác!</b></html>",
            "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                 "UPDATE PHIEUNHAP SET TrangThai = N'Huy' WHERE MaPN = ?")) {
            ps.setInt(1, maPN);
            ps.executeUpdate();
            loadData();
            showToast("Đã hủy phiếu nhập #" + maPN);
        } catch (Exception ex) {
            ex.printStackTrace();
            warn("Lỗi khi hủy phiếu:\n" + ex.getMessage());
        }
    }

    // =================================================================
    // MỞ DIALOG
    // =================================================================
    private void openDialog(Integer maPN) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        String ttl   = (maPN == null) ? "Lập phiếu nhập hàng mới" : "Chi tiết phiếu nhập  #" + maPN;
        JDialog dlg  = (owner instanceof Frame)
            ? new JDialog((Frame) owner, ttl, true)
            : new JDialog((Dialog) owner, ttl, true);
        dlg.setSize(1060, 730);
        dlg.setLocationRelativeTo(owner);
        dlg.setResizable(true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setContentPane(new NhapHangDialogPanel(maPN, dlg));
        dlg.setVisible(true);
        loadData();
    }

    // =================================================================
    // INNER CLASS: DIALOG LẬP / XEM PHIẾU NHẬP
    // =================================================================
    class NhapHangDialogPanel extends JPanel {

        private final Integer maPN;
        private final JDialog parentDlg;
        private final boolean viewOnly;
        private String trangThaiHienTai = ""; // chỉ dùng khi viewOnly=true

        private JComboBox<NhaCungCapDTO> cbNCC;
        private JTextField txtNgayNhap, txtGhiChu;
        private JTextField txtMaSP, txtTenSP, txtDonGia, txtSoLuong;
        private JTable            tblChiTiet;
        private DefaultTableModel modelChiTiet;
        private JLabel            lblTongTien, lblSoDong;

        /** [maSP, tenSP, donGia(BigDecimal), soLuong, thanhTien(BigDecimal)] */
        private final ArrayList<Object[]> chiTietList = new ArrayList<>();

        NhapHangDialogPanel(Integer maPN, JDialog dlg) {
            this.maPN      = maPN;
            this.parentDlg = dlg;
            this.viewOnly  = (maPN != null);
            setLayout(new BorderLayout());
            setBackground(CONTENT_BG);
            add(buildDlgHeader(), BorderLayout.NORTH);
            add(buildDlgCenter(), BorderLayout.CENTER);
            add(buildDlgFooter(), BorderLayout.SOUTH);
            if (viewOnly) SwingUtilities.invokeLater(this::loadViewData);
        }

        // ── HEADER ────────────────────────────────────────────────────
        private JPanel buildDlgHeader() {
            JPanel p = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY));
                    g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose();
                }
            };
            p.setOpaque(false); p.setPreferredSize(new Dimension(0, 52));
            p.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            JLabel lbl = new JLabel(viewOnly ? "CHI TIẾT PHIẾU NHẬP  #" + maPN : "LẬP PHIẾU NHẬP HÀNG MỚI");
            lbl.setFont(new Font("Segoe UI",Font.BOLD,17)); lbl.setForeground(WHITE);
            JLabel dateLbl = new JLabel("Ngày: " + LocalDate.now());
            dateLbl.setFont(FONT_SMALL); dateLbl.setForeground(new Color(200,230,255));
            p.add(lbl, BorderLayout.WEST); p.add(dateLbl, BorderLayout.EAST);
            return p;
        }

        // ── CENTER ────────────────────────────────────────────────────
        private JPanel buildDlgCenter() {
            JPanel p = new JPanel(new BorderLayout(0, 8));
            p.setBackground(CONTENT_BG);
            p.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
            p.add(buildInfoRow(),    BorderLayout.NORTH);
            p.add(buildMidSection(), BorderLayout.CENTER);
            return p;
        }

        private JPanel buildInfoRow() {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(WHITE);
            p.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER,1), BorderFactory.createEmptyBorder(12,16,12,16)));

            ArrayList<NhaCungCapDTO> listNCC = nhaCungCapBUS.getDanhSachHoatDong();
            cbNCC = new JComboBox<>();
            cbNCC.addItem(new NhaCungCapDTO() {{ setMaNCC(0); setTenNCC("-- Chọn nhà cung cấp --"); }});
            for (NhaCungCapDTO ncc : listNCC) cbNCC.addItem(ncc);
            cbNCC.setFont(FONT_NORMAL); cbNCC.setPreferredSize(new Dimension(0, 32));
            cbNCC.setEnabled(!viewOnly);

            txtNgayNhap = new JTextField(LocalDate.now().toString());
            txtNgayNhap.setEditable(false); txtNgayNhap.setFocusable(false);
            txtNgayNhap.setBackground(new Color(235,240,248)); txtNgayNhap.setForeground(new Color(60,80,120));
            styleField(txtNgayNhap);

            txtGhiChu = new JTextField(); txtGhiChu.setEnabled(!viewOnly); styleField(txtGhiChu);

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(0,8,0,8); gc.fill = GridBagConstraints.HORIZONTAL; gc.anchor = GridBagConstraints.WEST;
            gc.gridx=0; gc.weightx=0;   p.add(makeLabel("Nhà cung cấp (*):"), gc);
            gc.gridx=1; gc.weightx=1.8; p.add(cbNCC, gc);
            gc.gridx=2; gc.weightx=0;   p.add(makeLabel("Ngày nhập:"), gc);
            gc.gridx=3; gc.weightx=0.5; p.add(txtNgayNhap, gc);
            gc.gridx=4; gc.weightx=0;   p.add(makeLabel("Ghi chú:"), gc);
            gc.gridx=5; gc.weightx=1.4; p.add(txtGhiChu, gc);
            return p;
        }

        private JPanel buildMidSection() {
            JPanel p = new JPanel(new BorderLayout(0, 6));
            p.setBackground(CONTENT_BG);
            if (!viewOnly) p.add(buildAddRow(), BorderLayout.NORTH);
            p.add(buildChiTietTable(), BorderLayout.CENTER);
            return p;
        }

        private JPanel buildAddRow() {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(new Color(240,248,255));
            p.setBorder(new CompoundBorder(new LineBorder(new Color(180,220,240),1), BorderFactory.createEmptyBorder(10,16,10,16)));

            txtMaSP    = new JTextField();    styleField(txtMaSP);
            txtTenSP   = new JTextField();    styleField(txtTenSP); txtTenSP.setEditable(false); txtTenSP.setBackground(new Color(245,248,252));
            txtDonGia  = new JTextField();    styleField(txtDonGia);
            txtSoLuong = new JTextField("1"); styleField(txtSoLuong);

            txtMaSP.addFocusListener(new FocusAdapter() { @Override public void focusLost(FocusEvent e) { lookupSP(); } });
            txtMaSP.addKeyListener(new KeyAdapter() {
                @Override public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) { lookupSP(); txtDonGia.requestFocus(); }
                }
            });

            JButton btnChonSP = buildActionButton("🔍 Chọn SP", ACCENT, WHITE);
            btnChonSP.setPreferredSize(new Dimension(110, 32));
            btnChonSP.addActionListener(e -> openChonSPDialog());

            JButton btnThem = buildActionButton("+ Thêm", SUCCESS, WHITE);
            btnThem.setPreferredSize(new Dimension(90, 32));
            btnThem.addActionListener(e -> themDong());

            boolean hasNCC = cbNCC.getSelectedIndex() > 0;
            setInputEnabled(hasNCC, txtMaSP, txtDonGia, txtSoLuong, btnChonSP, btnThem);

            cbNCC.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    NhaCungCapDTO sel = (NhaCungCapDTO) cbNCC.getSelectedItem();
                    boolean on = sel != null && sel.getMaNCC() > 0;
                    setInputEnabled(on, txtMaSP, txtDonGia, txtSoLuong, btnChonSP, btnThem);
                    if (!on) clearAddRow(); else txtMaSP.requestFocus();
                }
            });

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets=new Insets(0,6,0,6); gc.fill=GridBagConstraints.HORIZONTAL; gc.anchor=GridBagConstraints.CENTER;
            gc.gridx=0; gc.weightx=0;   p.add(makeLabel("Mã SP:"), gc);
            gc.gridx=1; gc.weightx=0.3; p.add(txtMaSP, gc);
            gc.gridx=2; gc.weightx=0;   p.add(btnChonSP, gc);
            gc.gridx=3; gc.weightx=1.2; p.add(txtTenSP, gc);
            gc.gridx=4; gc.weightx=0;   p.add(makeLabel("Đơn giá nhập (đ):"), gc);
            gc.gridx=5; gc.weightx=0.6; p.add(txtDonGia, gc);
            gc.gridx=6; gc.weightx=0;   p.add(makeLabel("SL:"), gc);
            gc.gridx=7; gc.weightx=0.2; p.add(txtSoLuong, gc);
            gc.gridx=8; gc.weightx=0;   p.add(btnThem, gc);
            return p;
        }

        private void setInputEnabled(boolean en, JComponent... cs) {
            for (JComponent c : cs) c.setEnabled(en);
        }

        private JPanel buildChiTietTable() {
            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(WHITE);
            p.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER,1), BorderFactory.createEmptyBorder(6,6,6,6)));

            String[] cols = {"STT","Mã SP","Tên sản phẩm","Đơn giá nhập (đ)","Số lượng","Thành tiền (đ)"};
            modelChiTiet = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return !viewOnly && (c==3||c==4); }
            };
            tblChiTiet = new JTable(modelChiTiet) {
                @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                    JLabel c = (JLabel) super.prepareRenderer(r, row, col);
                    c.setFont(FONT_NORMAL);
                    c.setHorizontalAlignment(col==0||col==1||col==4 ? SwingConstants.CENTER : SwingConstants.LEFT);
                    if (!isRowSelected(row)) c.setBackground(row%2==0 ? WHITE : ROW_ALT);
                    else                     c.setBackground(new Color(187,222,251));
                    c.setForeground(new Color(30,40,60)); return c;
                }
            };
            styleTable(tblChiTiet);
            tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(45);
            tblChiTiet.getColumnModel().getColumn(1).setPreferredWidth(65);
            tblChiTiet.getColumnModel().getColumn(2).setPreferredWidth(340);
            tblChiTiet.getColumnModel().getColumn(3).setPreferredWidth(170);
            tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(80);
            tblChiTiet.getColumnModel().getColumn(5).setPreferredWidth(170);

            modelChiTiet.addTableModelListener(e -> {
                int col = e.getColumn();
                if ((col==3||col==4) && e.getFirstRow()>=0) recalcRow(e.getFirstRow());
            });
            JScrollPane sc = new JScrollPane(tblChiTiet); sc.setBorder(null);
            sc.getVerticalScrollBar().setUnitIncrement(16);
            p.add(sc, BorderLayout.CENTER);
            return p;
        }

        // ── FOOTER ────────────────────────────────────────────────────
        private JPanel buildDlgFooter() {
            JPanel outer = new JPanel(new BorderLayout()); outer.setBackground(CONTENT_BG);
            JPanel bar = new JPanel(new GridLayout(1,2,1,0));
            bar.setBackground(new Color(8,50,110)); bar.setPreferredSize(new Dimension(0,54));
            lblSoDong   = new JLabel("0 mặt hàng", SwingConstants.CENTER);
            lblTongTien = new JLabel("0 đ",         SwingConstants.CENTER);
            lblSoDong.setFont(FONT_TOTAL);   lblSoDong.setForeground(new Color(100,220,180));
            lblTongTien.setFont(FONT_TOTAL); lblTongTien.setForeground(new Color(255,220,100));
            bar.add(makeSummaryBlock("Số dòng:", lblSoDong));
            bar.add(makeSummaryBlock("Tổng tiền nhập:", lblTongTien));
            outer.add(bar, BorderLayout.NORTH);

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8)); btnRow.setBackground(CONTENT_BG);
            if (!viewOnly) {
                // Lập phiếu mới: 3 nút
                JButton btnXoa       = buildActionButton("Xóa dòng",         DANGER,                  WHITE);
                JButton btnReset     = buildActionButton("Làm mới",           new Color(90,100,115),  WHITE);
                JButton btnLuuTam    = buildBigButton("LƯU TẠM (Chờ xử lý)", WARNING);
                JButton btnHoanThanh = buildBigButton("LẬP PHIẾU (Hoàn thành)", SUCCESS);

                btnXoa.addActionListener(e -> xoaDong());
                btnReset.addActionListener(e -> resetDlg());
                btnLuuTam.addActionListener(e -> luuPhieu("ChoXuLy"));
                btnHoanThanh.addActionListener(e -> luuPhieu("HoanThanh"));

                // Tooltip giải thích
                btnLuuTam.setToolTipText("Lưu phiếu ở trạng thái chờ — tồn kho chưa thay đổi, chưa sinh serial");
                btnHoanThanh.setToolTipText("Lập phiếu hoàn thành ngay — sinh serial + cộng tồn kho");

                btnRow.add(btnXoa); btnRow.add(btnReset); btnRow.add(btnLuuTam); btnRow.add(btnHoanThanh);
            } else {
                // Xem phiếu: hiện thêm nút "Xác nhận hoàn thành" nếu phiếu đang ChoXuLy
                JButton btnClose = buildBigButton("Đóng", PRIMARY);
                btnClose.addActionListener(e -> parentDlg.dispose());

                // Nút hoàn thành động — sẽ ẩn/hiện sau khi loadViewData chạy
                JButton btnConfirm = buildBigButton("✔ Xác nhận hoàn thành", SUCCESS);
                btnConfirm.setToolTipText("Sinh serial + cộng kho → chuyển sang HoanThanh");
                btnConfirm.setVisible(false); // ẩn mặc định, loadViewData sẽ bật lên nếu ChoXuLy
                btnConfirm.addActionListener(e -> {
                    int ok = JOptionPane.showConfirmDialog(this,
                        "<html>Xác nhận hoàn thành phiếu <b>#" + maPN + "</b>?<br>"
                        + "• Serial sẽ được sinh<br>• Tồn kho sẽ được cộng<br>• Không thể hoàn tác</html>",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (ok != JOptionPane.YES_OPTION) return;
                    thucHienHoanThanh(maPN);
                    parentDlg.dispose();
                });

                // Lưu ref để loadViewData bật lên sau
                this.putClientProperty("btnConfirm", btnConfirm);
                btnRow.add(btnConfirm); btnRow.add(btnClose);
            }
            outer.add(btnRow, BorderLayout.SOUTH);
            return outer;
        }

        // ── LOGIC ─────────────────────────────────────────────────────

        private int getSelectedMaNCC() {
            NhaCungCapDTO sel = (NhaCungCapDTO) cbNCC.getSelectedItem();
            return (sel != null) ? sel.getMaNCC() : 0;
        }

        /**
         * Tra cứu SP theo mã — kiểm tra SP phải thuộc NCC đang chọn
         * (qua bảng NHACUNGCAP_SANPHAM).
         */
        private void lookupSP() {
            String txt = txtMaSP.getText().trim();
            if (txt.isEmpty()) return;
            int maNCC = getSelectedMaNCC();
            if (maNCC <= 0) {
                txtTenSP.setText("⚠ Vui lòng chọn NCC trước!"); txtDonGia.setText(""); return;
            }
            try {
                int maSP = Integer.parseInt(txt);
                // Kiểm tra SP tồn tại + thuộc NCC này
                String sql =
                    "SELECT sp.TenSP, sp.GiaGoc, sp.Gia, sp.TrangThai " +
                    "FROM SANPHAM sp " +
                    "JOIN NHACUNGCAP_SANPHAM ns ON sp.MaSP = ns.MaSP " +
                    "WHERE sp.MaSP = ? AND ns.MaNhaCungCap = ?";
                try (Connection cn = DBConnection.getConnection();
                     PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setInt(1, maSP); ps.setInt(2, maNCC);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        // SP có tồn tại không?
                        SanPhamDTO sp = sanPhamBUS.timTheoMa(maSP);
                        if (sp == null)
                            txtTenSP.setText("⚠ Mã SP không tồn tại");
                        else
                            txtTenSP.setText("⚠ SP này không thuộc NCC đã chọn");
                        txtDonGia.setText(""); return;
                    }
                    String trangThai = rs.getString("TrangThai");
                    if ("NgungBan".equals(trangThai)) {
                        txtTenSP.setText("⚠ Sản phẩm đã ngừng bán"); txtDonGia.setText(""); return;
                    }
                    txtTenSP.setText(rs.getString("TenSP"));
                    BigDecimal giaGoc = rs.getBigDecimal("GiaGoc");
                    BigDecimal gia    = rs.getBigDecimal("Gia");
                    BigDecimal show   = giaGoc != null ? giaGoc : gia;
                    txtDonGia.setText(show != null ? show.toPlainString() : "");
                }
            } catch (NumberFormatException ex) {
                txtTenSP.setText("⚠ Mã không hợp lệ");
            } catch (Exception ex) {
                ex.printStackTrace();
                txtTenSP.setText("⚠ Lỗi tra cứu SP");
            }
        }

        /**
         * Dialog chọn SP — chỉ hiển thị SP thuộc NCC đang chọn
         * thông qua JOIN NHACUNGCAP_SANPHAM.
         */
        private void openChonSPDialog() {
            if (getSelectedMaNCC() <= 0) { warn("Vui lòng chọn nhà cung cấp trước!"); return; }
            int maNCC = getSelectedMaNCC();

            // Lấy tên NCC để hiện trên tiêu đề dialog
            NhaCungCapDTO selNCC = (NhaCungCapDTO) cbNCC.getSelectedItem();
            String tenNCC = selNCC != null ? selNCC.getTenNCC() : "";

            List<SanPhamDTO> dsSP = new ArrayList<>();
            try (Connection cn = DBConnection.getConnection();
                 PreparedStatement ps = cn.prepareStatement(
                     // JOIN NHACUNGCAP_SANPHAM để lọc SP theo NCC
                     "SELECT sp.MaSP, sp.TenSP, sp.ThuongHieu, " +
                     "       sp.GiaGoc, sp.Gia, sp.SoLuongTon, sp.TrangThai " +
                     "FROM SANPHAM sp " +
                     "JOIN NHACUNGCAP_SANPHAM ns ON sp.MaSP = ns.MaSP " +
                     "WHERE ns.MaNhaCungCap = ? " +
                     "  AND sp.TrangThai <> N'NgungBan' " +
                     "ORDER BY sp.MaLoai, sp.TenSP")) {
                ps.setInt(1, maNCC);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        SanPhamDTO sp = new SanPhamDTO();
                        sp.setMaSP(rs.getInt("MaSP")); sp.setTenSP(rs.getString("TenSP"));
                        sp.setThuongHieu(rs.getString("ThuongHieu"));
                        sp.setGiaGoc(rs.getBigDecimal("GiaGoc")); sp.setGia(rs.getBigDecimal("Gia"));
                        sp.setSoLuongTon(rs.getInt("SoLuongTon")); sp.setTrangThai(rs.getString("TrangThai"));
                        dsSP.add(sp);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace(); warn("Lỗi tải danh sách sản phẩm:\n" + ex.getMessage()); return;
            }

            if (dsSP.isEmpty()) {
                warn("Nhà cung cấp \"" + tenNCC + "\" chưa có sản phẩm nào được liên kết.\n"
                   + "Vui lòng cập nhật liên kết NCC-SP trong phần Nhà cung cấp.");
                return;
            }

            Window owner = SwingUtilities.getWindowAncestor(this);
            JDialog dlg  = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Chọn sản phẩm — " + tenNCC, true)
                : new JDialog((Dialog) owner, "Chọn sản phẩm — " + tenNCC, true);
            dlg.setSize(820, 540); dlg.setLocationRelativeTo(owner);

            JPanel content = new JPanel(new BorderLayout(6, 6));
            content.setBackground(CONTENT_BG); content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            JTextField txtKw = new JTextField(); txtKw.setFont(FONT_NORMAL);
            txtKw.setBorder(new CompoundBorder(new LineBorder(new Color(180,210,240),1), BorderFactory.createEmptyBorder(5,10,5,10)));

            String[] cols = {"Mã SP","Tên sản phẩm","Thương hiệu","Giá gốc (đ)","Tồn kho","Tình trạng"};
            DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            final List<SanPhamDTO> allSP = dsSP;
            java.util.function.Consumer<String> reloadTbl = kw -> {
                mdl.setRowCount(0);
                String kLow = kw.toLowerCase();
                for (SanPhamDTO sp : allSP) {
                    if (!kLow.isEmpty()
                            && !sp.getTenSP().toLowerCase().contains(kLow)
                            && !String.valueOf(sp.getMaSP()).contains(kLow)
                            && !(sp.getThuongHieu()!=null && sp.getThuongHieu().toLowerCase().contains(kLow)))
                        continue;
                    BigDecimal goc = sp.getGiaGoc()!=null ? sp.getGiaGoc() : sp.getGia();
                    mdl.addRow(new Object[]{
                        sp.getMaSP(), sp.getTenSP(), sp.getThuongHieu(),
                        formatMoney(goc), sp.getSoLuongTon(),
                        "HetHang".equals(sp.getTrangThai()) ? "Hết hàng" : "Còn hàng"
                    });
                }
            };
            reloadTbl.accept("");

            JTable tbl = new JTable(mdl);
            tbl.setRowHeight(32); tbl.setFont(FONT_NORMAL);
            tbl.setGridColor(new Color(220,230,245)); tbl.setShowVerticalLines(true);
            tbl.setSelectionBackground(new Color(187,222,251)); tbl.setSelectionForeground(PRIMARY_DARK);
            tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tbl.setIntercellSpacing(new Dimension(0,1));

            DefaultTableCellRenderer hRdr = new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c) {
                    super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                    setBackground(TABLE_HEADER); setForeground(WHITE); setFont(new Font("Segoe UI",Font.BOLD,13));
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setBorder(BorderFactory.createMatteBorder(0,0,1,1,new Color(180,210,240)));
                    setOpaque(true); return this;
                }
            };
            JTableHeader hdr = tbl.getTableHeader(); hdr.setPreferredSize(new Dimension(0,36)); hdr.setReorderingAllowed(false);
            for (int i=0;i<tbl.getColumnCount();i++) tbl.getColumnModel().getColumn(i).setHeaderRenderer(hRdr);

            tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c) {
                    super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                    setFont(FONT_NORMAL);
                    boolean hetHang = "Hết hàng".equals(t.getValueAt(r,5));
                    setHorizontalAlignment(c==0||c==4||c==5 ? SwingConstants.CENTER : SwingConstants.LEFT);
                    if (sel)          { setBackground(new Color(187,222,251)); setForeground(PRIMARY_DARK); }
                    else if (hetHang) { setBackground(new Color(255,245,245)); setForeground(new Color(180,40,40)); }
                    else              { setBackground(r%2==0 ? WHITE : ROW_ALT); setForeground(new Color(30,40,60)); }
                    return this;
                }
            });
            tbl.getColumnModel().getColumn(0).setPreferredWidth(60);
            tbl.getColumnModel().getColumn(1).setPreferredWidth(280);
            tbl.getColumnModel().getColumn(2).setPreferredWidth(120);
            tbl.getColumnModel().getColumn(3).setPreferredWidth(140);
            tbl.getColumnModel().getColumn(4).setPreferredWidth(80);
            tbl.getColumnModel().getColumn(5).setPreferredWidth(90);

            txtKw.addKeyListener(new KeyAdapter() {
                @Override public void keyReleased(KeyEvent e) { reloadTbl.accept(txtKw.getText().trim()); }
            });

            Runnable doChon = () -> {
                int row = tbl.getSelectedRow(); if (row<0){ warn("Vui lòng chọn một sản phẩm!"); return; }
                int maSP = (int) mdl.getValueAt(row, 0);
                allSP.stream().filter(sp -> sp.getMaSP()==maSP).findFirst().ifPresent(sp -> {
                    txtMaSP.setText(String.valueOf(sp.getMaSP()));
                    txtTenSP.setText(sp.getTenSP());
                    BigDecimal goc = sp.getGiaGoc()!=null ? sp.getGiaGoc() : sp.getGia();
                    txtDonGia.setText(goc!=null ? goc.toPlainString() : "");
                    txtSoLuong.setText("1"); dlg.dispose();
                });
            };

            JButton btnChon = buildActionButton("✔ Chọn", PRIMARY, WHITE);
            JButton btnHuy2 = buildActionButton("Hủy",    DANGER,  WHITE);
            btnChon.addActionListener(e -> doChon.run());
            btnHuy2.addActionListener(e -> dlg.dispose());
            tbl.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount()==2) doChon.run(); }
            });

            JPanel searchRow = new JPanel(new BorderLayout(12,0)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(new GradientPaint(0,0,PRIMARY_DARK,getWidth(),0,PRIMARY));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10); g2.dispose();
                }
            };
            searchRow.setOpaque(false); searchRow.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));

            // Panel trái: tên NCC + số lượng SP
            JPanel leftInfo = new JPanel(new BorderLayout(0, 2)); leftInfo.setOpaque(false);
            JLabel lblNccTitle = new JLabel("NCC: " + tenNCC);
            lblNccTitle.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblNccTitle.setForeground(new Color(255,230,100));
            JLabel lblSpCount = new JLabel(dsSP.size() + " sản phẩm");
            lblSpCount.setFont(new Font("Segoe UI", Font.PLAIN, 11)); lblSpCount.setForeground(new Color(180,220,255));
            leftInfo.add(lblNccTitle, BorderLayout.NORTH); leftInfo.add(lblSpCount, BorderLayout.SOUTH);

            // Panel giữa: ô tìm kiếm
            JPanel searchBox = new JPanel(new BorderLayout(8, 0)); searchBox.setOpaque(false);
            JLabel lblKw = new JLabel("  🔍 Tìm:"); lblKw.setFont(new Font("Segoe UI",Font.BOLD,13)); lblKw.setForeground(WHITE);
            searchBox.add(lblKw, BorderLayout.WEST); searchBox.add(txtKw, BorderLayout.CENTER);

            searchRow.add(leftInfo,  BorderLayout.WEST);
            searchRow.add(searchBox, BorderLayout.CENTER);

            JPanel btnRow2 = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btnRow2.setBackground(CONTENT_BG);
            btnRow2.add(btnHuy2); btnRow2.add(btnChon);

            JScrollPane sc = new JScrollPane(tbl); sc.setBorder(new LineBorder(CARD_BORDER,1));
            content.add(searchRow, BorderLayout.NORTH);
            content.add(sc,        BorderLayout.CENTER);
            content.add(btnRow2,   BorderLayout.SOUTH);
            dlg.setContentPane(content); dlg.setVisible(true);
        }

        private void themDong() {
            String maSPStr   = txtMaSP.getText().trim();
            String tenSP     = txtTenSP.getText().trim();
            String donGiaStr = txtDonGia.getText().trim().replace(",","");
            String slStr     = txtSoLuong.getText().trim();

            if (maSPStr.isEmpty()||tenSP.isEmpty()||tenSP.startsWith("⚠")) { warn("Vui lòng chọn sản phẩm hợp lệ!"); return; }
            if (donGiaStr.isEmpty()) { warn("Vui lòng nhập đơn giá nhập!"); return; }

            int maSP; BigDecimal donGia; int sl;
            try { maSP = Integer.parseInt(maSPStr); } catch (Exception ex) { warn("Mã SP không hợp lệ!"); return; }
            try { donGia=new BigDecimal(donGiaStr); if(donGia.compareTo(BigDecimal.ZERO)<=0) throw new Exception(); }
                catch (Exception ex) { warn("Đơn giá phải lớn hơn 0!"); return; }
            try { sl=Integer.parseInt(slStr); if(sl<=0) throw new Exception(); }
                catch (Exception ex) { warn("Số lượng phải > 0!"); return; }

            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(sl));

            for (int i=0; i<chiTietList.size(); i++) {
                if ((int)chiTietList.get(i)[0] == maSP) {
                    int slMoi = (int)chiTietList.get(i)[3] + sl;
                    BigDecimal dg = (BigDecimal)chiTietList.get(i)[2];
                    BigDecimal tt = dg.multiply(BigDecimal.valueOf(slMoi));
                    chiTietList.get(i)[3]=slMoi; chiTietList.get(i)[4]=tt;
                    modelChiTiet.setValueAt(slMoi,          i,4);
                    modelChiTiet.setValueAt(formatMoney(tt),i,5);
                    showToast("Cộng thêm SL: " + tenSP + " → " + slMoi);
                    clearAddRow(); recalcTongTien(); cbNCC.setEnabled(false); return;
                }
            }
            chiTietList.add(new Object[]{ maSP, tenSP, donGia, sl, thanhTien });
            modelChiTiet.addRow(new Object[]{ modelChiTiet.getRowCount()+1, maSP, tenSP, formatMoney(donGia), sl, formatMoney(thanhTien) });
            lblSoDong.setText(modelChiTiet.getRowCount() + " mặt hàng");
            showToast("Đã thêm: " + tenSP);
            clearAddRow(); recalcTongTien(); cbNCC.setEnabled(false);
        }

        private void clearAddRow() {
            txtMaSP.setText(""); txtTenSP.setText(""); txtDonGia.setText(""); txtSoLuong.setText("1");
            txtMaSP.requestFocus();
        }

        private void recalcRow(int row) {
            if (row<0||row>=chiTietList.size()) return;
            try {
                String dgStr = modelChiTiet.getValueAt(row,3).toString().replace(",","").replace(".","");
                BigDecimal dg = new BigDecimal(dgStr);
                int sl = Integer.parseInt(modelChiTiet.getValueAt(row,4).toString());
                if (dg.compareTo(BigDecimal.ZERO)<=0||sl<=0) return;
                BigDecimal tt = dg.multiply(BigDecimal.valueOf(sl));
                chiTietList.get(row)[2]=dg; chiTietList.get(row)[3]=sl; chiTietList.get(row)[4]=tt;
                modelChiTiet.setValueAt(formatMoney(tt),row,5); recalcTongTien();
            } catch (Exception ignored) {}
        }

        private void recalcTongTien() {
            BigDecimal tong = BigDecimal.ZERO;
            for (Object[] r : chiTietList) tong = tong.add((BigDecimal)r[4]);
            lblTongTien.setText(formatMoney(tong) + " đ");
        }

        private void xoaDong() {
            int row = tblChiTiet.getSelectedRow(); if (row<0){ warn("Vui lòng chọn dòng cần xóa!"); return; }
            chiTietList.remove(row); modelChiTiet.removeRow(row);
            for (int i=0;i<modelChiTiet.getRowCount();i++) modelChiTiet.setValueAt(i+1,i,0);
            lblSoDong.setText(modelChiTiet.getRowCount() + " mặt hàng"); recalcTongTien();
            if (modelChiTiet.getRowCount()==0) cbNCC.setEnabled(true);
        }

        private void resetDlg() {
            chiTietList.clear(); modelChiTiet.setRowCount(0);
            cbNCC.setSelectedIndex(0); cbNCC.setEnabled(true);
            txtNgayNhap.setText(LocalDate.now().toString()); txtGhiChu.setText("");
            clearAddRow(); lblTongTien.setText("0 đ"); lblSoDong.setText("0 mặt hàng");
        }

        /**
         * Lưu phiếu nhập với trạng thái chỉ định.
         *
         * trangThaiTarget = "ChoXuLy":
         *   → INSERT PHIEUNHAP(ChoXuLy) + CHITIETPHIEUNHAP
         *   → KHÔNG sinh serial, KHÔNG cộng kho
         *
         * trangThaiTarget = "HoanThanh":
         *   → INSERT PHIEUNHAP(HoanThanh) + CHITIETPHIEUNHAP
         *   → INSERT SERIAL (batch) + UPDATE SoLuongTon
         */
        private void luuPhieu(String trangThaiTarget) {
            NhaCungCapDTO selNCC = (NhaCungCapDTO) cbNCC.getSelectedItem();
            if (selNCC==null||selNCC.getMaNCC()<=0) { warn("Vui lòng chọn nhà cung cấp!"); return; }
            if (chiTietList.isEmpty()) { warn("Chưa có sản phẩm nào trong phiếu!"); return; }
            if (SharedData.currentMaNV<=0) { warn("Không xác định được nhân viên đang đăng nhập!"); return; }

            BigDecimal tong = BigDecimal.ZERO;
            for (Object[] r : chiTietList) tong = tong.add((BigDecimal)r[4]);
            int totalSerial = chiTietList.stream().mapToInt(r -> (int)r[3]).sum();

            boolean isHoanThanh = "HoanThanh".equals(trangThaiTarget);
            String confirmMsg = isHoanThanh
                ? "<html>Xác nhận lập phiếu nhập (Hoàn thành)?<br><br>"
                  + "Nhà cung cấp : <b>" + selNCC.getTenNCC() + "</b><br>"
                  + "Số dòng SP   : <b>" + chiTietList.size() + " sản phẩm</b><br>"
                  + "Tổng serial  : <b>" + totalSerial + " máy</b><br>"
                  + "Tổng tiền    : <b style='color:red'>" + formatMoney(tong) + " đ</b><br><br>"
                  + "<font color='gray'>✅ Serial được sinh + tồn kho cộng ngay.</font></html>"
                : "<html>Lưu tạm phiếu nhập (Chờ xử lý)?<br><br>"
                  + "Nhà cung cấp : <b>" + selNCC.getTenNCC() + "</b><br>"
                  + "Số dòng SP   : <b>" + chiTietList.size() + " sản phẩm</b><br>"
                  + "Tổng tiền    : <b>" + formatMoney(tong) + " đ</b><br><br>"
                  + "<font color='orange'>⏳ Tồn kho chưa thay đổi — serial chưa được sinh.<br>"
                  + "Bạn sẽ xác nhận hoàn thành sau.</font></html>";

            int ok = JOptionPane.showConfirmDialog(this, confirmMsg,
                isHoanThanh ? "Xác nhận lập phiếu" : "Xác nhận lưu tạm",
                JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;

            Connection cn = null;
            try {
                cn = DBConnection.getConnection(); cn.setAutoCommit(false);

                // Bước 1: INSERT PHIEUNHAP
                int newMaPN;
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO PHIEUNHAP (MaNhaCungCap,MaNV,NgayNhap,TongTien,GhiChu,TrangThai) " +
                        "VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, selNCC.getMaNCC()); ps.setInt(2, SharedData.currentMaNV);
                    ps.setDate(3, Date.valueOf(LocalDate.now())); ps.setBigDecimal(4, tong);
                    ps.setString(5, txtGhiChu.getText().trim());
                    ps.setString(6, trangThaiTarget);
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    if (!keys.next()) throw new SQLException("Không lấy được MaPN!");
                    newMaPN = keys.getInt(1);
                }

                // Bước 2: INSERT CHITIETPHIEUNHAP
                String sqlCT = "INSERT INTO CHITIETPHIEUNHAP (MaPN,MaSP,SoLuong,DonGiaNhap) VALUES (?,?,?,?)";
                int[] maChiTietArr = new int[chiTietList.size()];
                for (int i = 0; i < chiTietList.size(); i++) {
                    Object[] ct = chiTietList.get(i);
                    int maSP = (int)ct[0]; int sl=(int)ct[3]; BigDecimal dg=(BigDecimal)ct[2];
                    try (PreparedStatement psCT = cn.prepareStatement(sqlCT, Statement.RETURN_GENERATED_KEYS)) {
                        psCT.setInt(1,newMaPN); psCT.setInt(2,maSP); psCT.setInt(3,sl); psCT.setBigDecimal(4,dg);
                        psCT.executeUpdate();
                        ResultSet keys = psCT.getGeneratedKeys();
                        if (!keys.next()) throw new SQLException("Không lấy được MaChiTietPN!");
                        maChiTietArr[i] = keys.getInt(1);
                    }
                }

                // Bước 3 (chỉ khi HoanThanh): Sinh serial + cộng kho
                int serialCount = 0;
                if (isHoanThanh) {
                    String sqlSerial  = "INSERT INTO SERIAL (SerialCode,MaSP,MaChiTietPN,TrangThai,NgayNhap) VALUES (?,?,?,N'TrongKho',?)";
                    String sqlMaxStt  = "SELECT ISNULL(MAX(CAST(RIGHT(SerialCode,3) AS INT)),0) " +
                                       "FROM SERIAL WHERE MaSP=? AND SerialCode LIKE ?";
                    String sqlCongKho = "UPDATE SANPHAM SET SoLuongTon = SoLuongTon + ? WHERE MaSP = ?";
                    Date ngayNhap = Date.valueOf(LocalDate.now());

                    for (int i = 0; i < chiTietList.size(); i++) {
                        int maSP      = (int) chiTietList.get(i)[0];
                        int sl        = (int) chiTietList.get(i)[3];
                        int maChiTiet = maChiTietArr[i];

                        // Đọc STT lớn nhất hiện có của SP này
                        int maxStt = 0;
                        try (PreparedStatement psMax = cn.prepareStatement(sqlMaxStt)) {
                            psMax.setInt(1, maSP);
                            psMax.setString(2, String.format("SP%02d-", maSP) + "%");
                            ResultSet rsMax = psMax.executeQuery();
                            if (rsMax.next()) maxStt = rsMax.getInt(1);
                        }

                        // Sinh serial tiếp từ maxStt+1 — format SP{02d}-{03d}
                        try (PreparedStatement psS = cn.prepareStatement(sqlSerial)) {
                            for (int j = 1; j <= sl; j++) {
                                psS.setString(1, String.format("SP%02d-%03d", maSP, maxStt + j));
                                psS.setInt(2, maSP); psS.setInt(3, maChiTiet); psS.setDate(4, ngayNhap);
                                psS.addBatch();
                            }
                            psS.executeBatch(); serialCount += sl;
                        }

                        try (PreparedStatement psCK = cn.prepareStatement(sqlCongKho)) {
                            psCK.setInt(1, sl); psCK.setInt(2, maSP);
                            psCK.executeUpdate();
                        }
                    }
                }

                cn.commit();

                String successMsg = isHoanThanh
                    ? "✅ Lập phiếu nhập #" + newMaPN + " thành công!\nĐã sinh " + serialCount + " serial và cập nhật tồn kho."
                    : "⏳ Đã lưu tạm phiếu nhập #" + newMaPN + " (Chờ xử lý).\nTồn kho chưa thay đổi — xác nhận hoàn thành sau.";
                JOptionPane.showMessageDialog(this, successMsg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                parentDlg.dispose();

            } catch (Exception ex) {
                if (cn!=null) try { cn.rollback(); } catch (Exception ignored){}
                ex.printStackTrace(); warn("Lỗi khi lưu phiếu:\n" + ex.getMessage());
            } finally {
                if (cn!=null) try { cn.setAutoCommit(true); cn.close(); } catch (Exception ignored){}
            }
        }

        /** Xem chi tiết phiếu nhập cũ (viewOnly=true) */
        private void loadViewData() {
            try (Connection cn = DBConnection.getConnection();
                 PreparedStatement ps = cn.prepareStatement(
                     "SELECT MaNhaCungCap, CONVERT(VARCHAR,NgayNhap,23) AS NgayNhap, GhiChu, TrangThai " +
                     "FROM PHIEUNHAP WHERE MaPN=?")) {
                ps.setInt(1, maPN); ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int maNCC = rs.getInt("MaNhaCungCap");
                    for (int i=0; i<cbNCC.getItemCount(); i++)
                        if (cbNCC.getItemAt(i).getMaNCC()==maNCC){ cbNCC.setSelectedIndex(i); break; }
                    txtNgayNhap.setText(rs.getString("NgayNhap")!=null ? rs.getString("NgayNhap") : "");
                    txtGhiChu.setText(rs.getString("GhiChu")!=null ? rs.getString("GhiChu") : "");
                    trangThaiHienTai = rs.getString("TrangThai");

                    // Bật nút "Xác nhận hoàn thành" nếu phiếu đang ChoXuLy
                    if ("ChoXuLy".equals(trangThaiHienTai)) {
                        JButton btnConfirm = (JButton) getClientProperty("btnConfirm");
                        if (btnConfirm != null) btnConfirm.setVisible(true);
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }

            modelChiTiet.setRowCount(0); chiTietList.clear();
            BigDecimal tongTien = BigDecimal.ZERO;
            try (Connection cn = DBConnection.getConnection();
                 PreparedStatement ps = cn.prepareStatement(
                     "SELECT ct.MaSP, sp.TenSP, ct.DonGiaNhap, ct.SoLuong, ct.ThanhTien " +
                     "FROM CHITIETPHIEUNHAP ct JOIN SANPHAM sp ON ct.MaSP=sp.MaSP " +
                     "WHERE ct.MaPN=? ORDER BY ct.MaChiTietPN")) {
                ps.setInt(1, maPN); ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int maSP=rs.getInt("MaSP"); String tenSP=rs.getString("TenSP");
                    BigDecimal dg=rs.getBigDecimal("DonGiaNhap"); int sl=rs.getInt("SoLuong");
                    BigDecimal tt=rs.getBigDecimal("ThanhTien");
                    if (tt==null) tt=dg.multiply(BigDecimal.valueOf(sl));
                    chiTietList.add(new Object[]{ maSP, tenSP, dg, sl, tt });
                    modelChiTiet.addRow(new Object[]{ modelChiTiet.getRowCount()+1, maSP, tenSP, formatMoney(dg), sl, formatMoney(tt) });
                    tongTien=tongTien.add(tt);
                }
                lblSoDong.setText(chiTietList.size() + " mặt hàng");
                lblTongTien.setText(formatMoney(tongTien) + " đ");
            } catch (Exception e) {
                e.printStackTrace(); lblTongTien.setText("-- đ"); lblSoDong.setText("Lỗi tải dữ liệu");
            }
        }

        // ── Helpers dialog ────────────────────────────────────────────
        private JPanel makeSummaryBlock(String lbl, JLabel valLbl) {
            JPanel block = new JPanel(new GridBagLayout()); block.setBackground(PRIMARY_DARK);
            JLabel l = new JLabel(lbl, SwingConstants.CENTER);
            l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setForeground(new Color(170,205,255));
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx=0; gc.gridy=0; gc.insets=new Insets(4,8,1,8); gc.anchor=GridBagConstraints.CENTER; block.add(l,gc);
            gc.gridy=1; gc.insets=new Insets(1,8,4,8); block.add(valLbl,gc);
            return block;
        }

        private void styleField(JTextField f) {
            f.setFont(FONT_NORMAL); f.setPreferredSize(new Dimension(0,32));
            f.setBorder(new CompoundBorder(new LineBorder(new Color(180,210,240),1), BorderFactory.createEmptyBorder(3,8,3,8)));
        }
    } // end NhapHangDialogPanel

    // =================================================================
    // TOAST
    // =================================================================
    private void showToast(String msg) {
        try {
            Window owner = SwingUtilities.getWindowAncestor(this);
            JWindow toast = new JWindow(owner);
            JLabel lbl = new JLabel("  " + msg + "  ");
            lbl.setFont(FONT_LABEL); lbl.setForeground(WHITE); lbl.setOpaque(true); lbl.setBackground(SUCCESS);
            lbl.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(30,100,40),1,true), BorderFactory.createEmptyBorder(7,12,7,12)));
            toast.add(lbl); toast.pack();
            Point loc = getLocationOnScreen();
            toast.setLocation(loc.x+getWidth()-toast.getWidth()-20, loc.y+getHeight()-toast.getHeight()-20);
            toast.setVisible(true);
            new Timer(1800, e -> toast.dispose()) {{ setRepeats(false); start(); }};
        } catch (Exception ignored) {}
    }

    // =================================================================
    // SHARED HELPERS
    // =================================================================
    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private String formatTrangThai(String tt) {
        switch (tt) {
            case "HoanThanh": return "Hoàn thành";
            case "ChoXuLy":   return "Chờ xử lý";
            case "Huy":       return "Đã hủy";
            default:          return tt;
        }
    }

    private void styleTable(JTable t) {
        t.setRowHeight(34); t.setFont(FONT_NORMAL);
        t.setGridColor(new Color(220,230,245)); t.setShowVerticalLines(true);
        t.setSelectionBackground(new Color(187,222,251)); t.setSelectionForeground(PRIMARY_DARK);
        t.setIntercellSpacing(new Dimension(0,1));
        JTableHeader h = t.getTableHeader(); h.setPreferredSize(new Dimension(0,38)); h.setReorderingAllowed(false);
        DefaultTableCellRenderer hRdr = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl,Object v,boolean sel,boolean foc,int r,int c) {
                super.getTableCellRendererComponent(tbl,v,sel,foc,r,c);
                setBackground(TABLE_HEADER); setForeground(WHITE); setFont(new Font("Segoe UI",Font.BOLD,13));
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createMatteBorder(0,0,1,1,new Color(180,210,240)));
                return this;
            }
        };
        for (int i=0;i<t.getColumnModel().getColumnCount();i++) t.getColumnModel().getColumn(i).setHeaderRenderer(hRdr);
    }

    private JButton buildActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(fg); btn.setFont(FONT_LABEL); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120,36)); return btn;
    }

    private JButton buildBigButton(String text, Color bg) {
        final Font f = new Font("Segoe UI",Font.BOLD,14);
        Canvas cv = new Canvas(); FontMetrics fm = cv.getFontMetrics(f);
        final int W=fm.stringWidth(text)+44, H=42;
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setFont(f); g2.setColor(WHITE); FontMetrics tfm=g2.getFontMetrics();
                g2.drawString(text,(getWidth()-tfm.stringWidth(text))/2,(getHeight()-tfm.getHeight())/2+tfm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(W,H); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
        };
        btn.setText(""); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildYellowButton(String text) {
        final Font f = new Font("Segoe UI",Font.BOLD,13);
        Canvas cv = new Canvas(); FontMetrics fm = cv.getFontMetrics(f);
        final int W=fm.stringWidth(text)+40, H=36;
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(240,180,0) : new Color(255,215,40));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setFont(f); g2.setColor(PRIMARY_DARK); FontMetrics tfm=g2.getFontMetrics();
                g2.drawString(text,(getWidth()-tfm.stringWidth(text))/2,(getHeight()-tfm.getHeight())/2+tfm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(W,H); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
        };
        btn.setText(""); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextField createSmallField(String text, int w) {
        JTextField f = new JTextField(text); f.setFont(FONT_SMALL); f.setForeground(WHITE);
        f.setBackground(new Color(255,255,255,40));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(255,255,255,80),1), BorderFactory.createEmptyBorder(2,6,2,6)));
        f.setPreferredSize(new Dimension(w,26)); f.setCaretColor(WHITE); return f;
    }

    private JLabel makeLabel(String text) {
        JLabel l=new JLabel(text); l.setFont(FONT_LABEL); l.setForeground(PRIMARY); return l;
    }

    private JLabel makeInlineLabel(String text) {
        JLabel l=new JLabel(text); l.setFont(FONT_SMALL); l.setForeground(new Color(200,230,255)); return l;
    }

    private String formatMoney(BigDecimal val) {
        if (val==null) return "0";
        return new DecimalFormat("#,###").format(val);
    }
}
