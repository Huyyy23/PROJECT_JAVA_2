package GUI;

import BUS.ChiTietPhieuNhapBUS;
import BUS.NhaCungCapBUS;
import BUS.PhieuNhapBUS;
import BUS.SanPhamBUS;
import DTO.ChiTietPhieuNhapDTO;
import DTO.NhaCungCapDTO;
import DTO.PhieuNhapDTO;
import DTO.SanPhamDTO;
import DTO.SharedData;
import UTIL.PhieuNhapExcelUtils;
import UTIL.PhieuNhapPDFUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NhapHangPanelGUI extends JPanel {

    // ===== COLORS =====
    private static final Color PRIMARY      = new Color(21, 101, 192);
    private static final Color PRIMARY_DARK = new Color(10, 60, 130);
    private static final Color ACCENT       = new Color(0, 188, 212);
    private static final Color CONTENT_BG   = new Color(236, 242, 250);
    private static final Color WHITE        = Color.WHITE;
    private static final Color ROW_ALT      = new Color(245, 250, 255);
    private static final Color TABLE_HEADER = new Color(21, 101, 192);
    private static final Color SUCCESS      = new Color(46, 125, 50);
    private static final Color DANGER       = new Color(198, 40, 40);
    private static final Color WARNING_COL  = new Color(230, 120, 0);
    private static final Color CARD_BORDER  = new Color(187, 222, 251);

    // ===== FONTS =====
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_TOTAL  = new Font("Segoe UI", Font.BOLD,  16);

    // ===== COMPONENT REFS =====
    private JTextField        txtSearch;
    private JComboBox<String> cbTrangThaiFilter;
    private JComboBox<String> cbSort;
    private JTable            tablePhieu;
    private DefaultTableModel modelPhieu;

    // ===== BUS =====
    private final PhieuNhapBUS        phieuNhapBUS  = new PhieuNhapBUS();
    private final NhaCungCapBUS       nhaCungCapBUS = new NhaCungCapBUS();
    private final SanPhamBUS          sanPhamBUS    = new SanPhamBUS();
    private final ChiTietPhieuNhapBUS chiTietBUS    = new ChiTietPhieuNhapBUS();

    // ===== DATA =====
    private List<Object[]>   dsPhieuNhap    = new ArrayList<>();
    private List<PhieuNhapDTO> dsPhieuNhapDTO = new ArrayList<>();

    // ================================================================
    public NhapHangPanelGUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createBodyPanel(),   BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { loadData(); }
        });
    }

    // ================================================================
    // DATA
    // ================================================================
    private void loadData() {
        try {
            dsPhieuNhapDTO = phieuNhapBUS.getAll();
            ArrayList<NhaCungCapDTO> listNCC = nhaCungCapBUS.getDanhSachTatCa();

            dsPhieuNhap.clear();
            for (PhieuNhapDTO pn : dsPhieuNhapDTO) {
                String tenNCC = "---";
                for (NhaCungCapDTO ncc : listNCC) {
                    if (ncc.getMaNCC() == pn.getMaNhaCungCap()) { tenNCC = ncc.getTenNCC(); break; }
                }
                String tenNV = (SharedData.currentMaNV == pn.getMaNV() && SharedData.currentTenNV != null)
                               ? SharedData.currentTenNV : "NV #" + pn.getMaNV();
                dsPhieuNhap.add(new Object[]{
                    pn.getMaPN(), pn.getNgayNhap() != null ? pn.getNgayNhap().toString() : "",
                    tenNCC, tenNV, pn.getTongTien(), pn.getTrangThai()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            warn("Không thể tải dữ liệu phiếu nhập: " + e.getMessage());
        }
        refreshTable(txtSearch != null ? txtSearch.getText().trim() : "");
    }

    private void refreshTable(String keyword) {
        modelPhieu.setRowCount(0);
        String kw       = keyword.toLowerCase();
        int filterIdx   = cbTrangThaiFilter != null ? cbTrangThaiFilter.getSelectedIndex() : 0;

        List<Object[]> filtered = new ArrayList<>();
        for (Object[] row : dsPhieuNhap) {
            String tenNCC    = row[2].toString().toLowerCase();
            String tenNV     = row[3].toString().toLowerCase();
            String maPN      = String.valueOf(row[0]);
            String trangThai = String.valueOf(row[5]);
            if (!kw.isEmpty() && !tenNCC.contains(kw) && !tenNV.contains(kw) && !maPN.contains(kw)) continue;
            switch (filterIdx) {
                case 0: if ("Huy".equals(trangThai))        continue; break;
                case 1: if (!"HoanThanh".equals(trangThai)) continue; break;
                case 2: if (!"ChoXuLy".equals(trangThai))   continue; break;
                case 3: if (!"Huy".equals(trangThai))       continue; break;
            }
            filtered.add(row);
        }

        if (cbSort != null) {
            int sortIdx = cbSort.getSelectedIndex();
            filtered.sort((r1, r2) -> {
                switch (sortIdx) {
                    case 1: return r2[1].toString().compareTo(r1[1].toString());
                    case 2: return r1[1].toString().compareTo(r2[1].toString());
                    case 3: return ((BigDecimal) r2[4]).compareTo((BigDecimal) r1[4]);
                    case 4: return ((BigDecimal) r1[4]).compareTo((BigDecimal) r2[4]);
                    default: return ((Integer) r2[0]).compareTo((Integer) r1[0]);
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
    }

    // ================================================================
    // HEADER — gradient + icon + title + nút outline bên phải
    // ================================================================
    private JPanel createHeaderPanel() {
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
        panel.setPreferredSize(new Dimension(0, 62));

        // ── TRÁI: icon truck + tiêu đề ──
        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));

        JComponent truckIcon = new JComponent() {
            { setPreferredSize(new Dimension(36, 62)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cy = getHeight() / 2 - 2;
                g2.drawRect(2, cy - 7, 17, 11);
                g2.drawRect(19, cy - 4, 11, 8);
                g2.drawLine(21, cy - 4, 21, cy + 1);
                g2.drawLine(21, cy + 1, 30, cy + 1);
                g2.fillOval(3,  cy + 4, 6, 6);
                g2.fillOval(19, cy + 4, 6, 6);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(8,  cy - 7, 8,  cy + 4);
                g2.drawLine(13, cy - 7, 13, cy + 4);
                g2.dispose();
            }
        };

        JLabel title = new JLabel("  QUẢN LÝ NHẬP HÀNG");
        title.setFont(FONT_TITLE);
        title.setForeground(WHITE);

        GridBagConstraints lgc = new GridBagConstraints();
        lgc.anchor = GridBagConstraints.CENTER; lgc.insets = new Insets(0, 0, 0, 4);
        lgc.gridx = 0; left.add(truckIcon, lgc);
        lgc.gridx = 1; lgc.insets = new Insets(0, 0, 0, 0);
        left.add(title, lgc);
        panel.add(left, BorderLayout.WEST);

        // ── PHẢI: Export Excel + Xuất PDF (đã bỏ nút Lập phiếu nhập) ──
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

        JButton btnExcelExport = buildOutlineBtn("Export Excel", "excel");
        JButton btnPdf        = buildOutlineBtn("Xuất PDF", "pdf");

        btnExcelExport.addActionListener(e -> exportExcel());
        btnPdf.addActionListener(e -> exportPdf());

        GridBagConstraints rgc = new GridBagConstraints();
        rgc.anchor = GridBagConstraints.CENTER; rgc.insets = new Insets(0, 8, 0, 0);
        rgc.gridx = 0; right.add(btnExcelExport, rgc);
        rgc.gridx = 1; right.add(btnPdf, rgc);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    // ================================================================
    // BODY — toolbar 1 dòng + bảng (không có stat cards)
    // ================================================================
    private JPanel createBodyPanel() {
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(CONTENT_BG);
        body.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel tableCard = new JPanel(new BorderLayout(0, 8));
        tableCard.setBackground(WHITE);
        tableCard.setBorder(new CompoundBorder(
            new LineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        tableCard.add(buildToolbar(),    BorderLayout.NORTH);
        tableCard.add(buildPhieuTable(), BorderLayout.CENTER);

        body.add(tableCard, BorderLayout.CENTER);
        return body;
    }

    // ================================================================
    // TOOLBAR — 1 dòng: search | filter | sort  ←→  nút hành động
    // ================================================================
    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(WHITE);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // ── TRÁI: search + combobox lọc + sort ──
        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftGroup.setBackground(WHITE);

        // Search bar
        JPanel searchBar = new JPanel(new BorderLayout());
        searchBar.setBackground(WHITE);
        searchBar.setPreferredSize(new Dimension(260, 36));
        searchBar.setBorder(new CompoundBorder(
            new LineBorder(new Color(180, 210, 240), 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JComponent searchIcon = new JComponent() {
            { setPreferredSize(new Dimension(34, 36)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = 12, cy = getHeight() / 2 - 1, r = 6;
                g2.setColor(new Color(160, 185, 220));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                g2.drawLine(cx + r - 2, cy + r - 2, cx + r + 4, cy + r + 4);
                g2.dispose();
            }
        };

        txtSearch = new JTextField();
        txtSearch.setFont(FONT_NORMAL);
        txtSearch.setToolTipText("Tìm kiếm phiếu nhập...");
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        txtSearch.setOpaque(false);
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { refreshTable(txtSearch.getText().trim()); }
        });

        // Placeholder
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { if (txtSearch.getText().equals("Tìm kiếm phiếu nhập...")) txtSearch.setText(""); }
            @Override public void focusLost(FocusEvent e)   { if (txtSearch.getText().isEmpty()) txtSearch.setText("Tìm kiếm phiếu nhập..."); }
        });
        txtSearch.setText("Tìm kiếm phiếu nhập...");
        txtSearch.setForeground(new Color(150, 160, 180));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                String t = txtSearch.getText().trim();
                if (!t.equals("Tìm kiếm phiếu nhập...")) refreshTable(t);
            }
        });

        searchBar.add(searchIcon, BorderLayout.WEST);
        searchBar.add(txtSearch,  BorderLayout.CENTER);

        // ComboBox lọc trạng thái
        cbTrangThaiFilter = new JComboBox<>(new String[]{ "Tất cả trạng thái", "Hoàn thành", "Chờ xử lý", "Đã hủy" });
        cbTrangThaiFilter.setFont(FONT_NORMAL);
        cbTrangThaiFilter.setPreferredSize(new Dimension(160, 36));
        cbTrangThaiFilter.addActionListener(e -> refreshTable(getSearchText()));

        // ComboBox sắp xếp
        cbSort = new JComboBox<>(new String[]{
            "Sắp xếp mặc định", "Mới nhất", "Cũ nhất", "Tiền cao → thấp", "Tiền thấp → cao"
        });
        cbSort.setFont(FONT_NORMAL);
        cbSort.setPreferredSize(new Dimension(175, 36));
        cbSort.addActionListener(e -> refreshTable(getSearchText()));

        leftGroup.add(searchBar);
        leftGroup.add(cbTrangThaiFilter);
        leftGroup.add(cbSort);

        // ── PHẢI: nút hành động ──
        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightGroup.setBackground(WHITE);

        JButton btnXemChiTiet = buildSolidBtn("Xem chi tiết", SUCCESS);
        JButton btnThanhToan  = buildSolidBtn("Thanh toán",   PRIMARY);
        JButton btnHuy        = buildSolidBtn("Hủy phiếu",    DANGER);
        JButton btnLapMoi     = buildSolidBtn("+ Lập phiếu",  new Color(255, 185, 0));
        btnLapMoi.setForeground(PRIMARY_DARK);

        JButton btnReset = buildSolidBtn("Làm mới", new Color(90, 100, 115));

        btnXemChiTiet.addActionListener(e -> {
            int row = tablePhieu.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn phiếu nhập cần xem!"); return; }
            openDialog((Integer) modelPhieu.getValueAt(row, 0));
        });
        btnThanhToan.addActionListener(e -> thanhToanPhieu());
        btnHuy.addActionListener(e -> huyPhieu());
        btnLapMoi.addActionListener(e -> openDialog(null));
        btnReset.addActionListener(e -> {
            txtSearch.setText("Tìm kiếm phiếu nhập...");
            txtSearch.setForeground(new Color(150, 160, 180));
            cbTrangThaiFilter.setSelectedIndex(0);
            cbSort.setSelectedIndex(0);
            loadData();
        });

        rightGroup.add(btnXemChiTiet);
        rightGroup.add(btnThanhToan);
        rightGroup.add(btnHuy);
        rightGroup.add(btnLapMoi);
        rightGroup.add(btnReset);

        toolbar.add(leftGroup,  BorderLayout.WEST);
        toolbar.add(rightGroup, BorderLayout.EAST);
        return toolbar;
    }

    private String getSearchText() {
        String t = txtSearch.getText().trim();
        return t.equals("Tìm kiếm phiếu nhập...") ? "" : t;
    }

    // ================================================================
    // TABLE
    // ================================================================
    private JScrollPane buildPhieuTable() {
        String[] cols = { "Mã phiếu", "Ngày nhập", "Nhà cung cấp", "Nhân viên", "Tổng tiền (đ)", "Trạng thái" };
        modelPhieu = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tablePhieu = new JTable(modelPhieu) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!(c instanceof JLabel)) return c;
                JLabel lbl = (JLabel) c;
                lbl.setFont(FONT_NORMAL);
                lbl.setHorizontalAlignment(col == 0 || col == 4 ? SwingConstants.CENTER : SwingConstants.LEFT);
                if (!isRowSelected(row)) lbl.setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                else                     lbl.setBackground(new Color(187, 222, 251));

                if (col == 5) {
                    String val = modelPhieu.getValueAt(row, col).toString();
                    if ("HoanThanh".equals(val)) {
                        lbl.setForeground(WHITE);
                        lbl.setBackground(isRowSelected(row) ? new Color(187, 222, 251) : (row % 2 == 0 ? WHITE : ROW_ALT));
                        lbl.setText("  Hoàn thành  ");
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if ("Huy".equals(val)) {
                        lbl.setForeground(DANGER);
                        lbl.setText("  Đã hủy");
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else {
                        lbl.setForeground(WARNING_COL);
                        lbl.setText("  Chờ xử lý");
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                } else {
                    lbl.setForeground(new Color(30, 40, 60));
                }
                return lbl;
            }
        };

        // Renderer cột trạng thái — vẽ badge pill
        tablePhieu.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setOpaque(true);
                String val = value != null ? value.toString() : "";
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) lbl.setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                else             lbl.setBackground(new Color(187, 222, 251));
                switch (val) {
                    case "HoanThanh":
                        lbl.setForeground(WHITE);
                        lbl.setBackground(SUCCESS);
                        lbl.setText("  Hoàn thành  ");
                        break;
                    case "Huy":
                        lbl.setForeground(WHITE);
                        lbl.setBackground(DANGER);
                        lbl.setText("  Đã hủy  ");
                        break;
                    default:
                        lbl.setForeground(WHITE);
                        lbl.setBackground(WARNING_COL);
                        lbl.setText("  Chờ xử lý  ");
                }
                return lbl;
            }
        });

        styleTable(tablePhieu);
        tablePhieu.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablePhieu.getColumnModel().getColumn(1).setPreferredWidth(110);
        tablePhieu.getColumnModel().getColumn(2).setPreferredWidth(230);
        tablePhieu.getColumnModel().getColumn(3).setPreferredWidth(150);
        tablePhieu.getColumnModel().getColumn(4).setPreferredWidth(140);
        tablePhieu.getColumnModel().getColumn(5).setPreferredWidth(130);

        tablePhieu.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tablePhieu.getSelectedRow() >= 0)
                    openDialog((Integer) modelPhieu.getValueAt(tablePhieu.getSelectedRow(), 0));
            }
        });

        JScrollPane sc = new JScrollPane(tablePhieu);
        sc.setBorder(new LineBorder(new Color(180, 210, 240), 1));
        sc.getVerticalScrollBar().setUnitIncrement(16);
        return sc;
    }

    // ================================================================
    // EXPORT
    // ================================================================
    private void exportExcel() {
        int row = tablePhieu.getSelectedRow();
        if (row >= 0) {
            int maPN = (Integer) modelPhieu.getValueAt(row, 0);
            PhieuNhapDTO pn = getPhieuNhapDTO(maPN);
            if (pn != null) {
                ArrayList<ChiTietPhieuNhapDTO> ct = chiTietBUS.getByMaPN(maPN);
                PhieuNhapExcelUtils.exportChiTiet(this, pn, ct);
                return;
            }
        }
        PhieuNhapExcelUtils.exportDanhSach(this, dsPhieuNhapDTO);
    }

    private void exportPdf() {
        int row = tablePhieu.getSelectedRow();
        if (row >= 0) {
            int maPN = (Integer) modelPhieu.getValueAt(row, 0);
            PhieuNhapDTO pn = getPhieuNhapDTO(maPN);
            if (pn != null) {
                ArrayList<ChiTietPhieuNhapDTO> ct = chiTietBUS.getByMaPN(maPN);
                PhieuNhapPDFUtils.exportChiTiet(this, pn, ct);
                return;
            }
        }
        PhieuNhapPDFUtils.exportDanhSach(this, dsPhieuNhapDTO);
    }

    private PhieuNhapDTO getPhieuNhapDTO(int maPN) {
        for (PhieuNhapDTO p : dsPhieuNhapDTO) if (p.getMaPN() == maPN) return p;
        return null;
    }

    // ================================================================
    // HỦY PHIẾU
    // ================================================================
    private void huyPhieu() {
        int row = tablePhieu.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn phiếu nhập cần hủy!"); return; }
        String trangThai = modelPhieu.getValueAt(row, 5).toString();
        if (!"ChoXuLy".equals(trangThai)) {
            warn("Chỉ những phiếu đang 'Chờ xử lý' mới được phép hủy!\nPhiếu đã hoàn thành không thể thay đổi."); return;
        }
        int maPN = (Integer) modelPhieu.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this,
            "<html>Xác nhận hủy phiếu nhập tạm <b>#" + maPN + "</b>?<br>"
            + "<font color='red'>Hành động này không thể hoàn tác!</font></html>",
            "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                if (phieuNhapBUS.updateTrangThai(maPN, "Huy")) {
                    loadData(); showToast("Đã hủy phiếu nhập #" + maPN);
                } else warn("Hủy phiếu thất bại, vui lòng thử lại!");
            } catch (Exception ex) { ex.printStackTrace(); warn("Lỗi khi hủy phiếu: " + ex.getMessage()); }
        }
    }

    // ================================================================
    // THANH TOÁN
    // ================================================================
    private void thanhToanPhieu() {
        int row = tablePhieu.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn phiếu nhập cần thanh toán!"); return; }
        String trangThai = modelPhieu.getValueAt(row, 5).toString();
        if (!"ChoXuLy".equals(trangThai)) { warn("Chỉ những phiếu đang 'Chờ xử lý' mới cần thanh toán!"); return; }
        int maPN = (Integer) modelPhieu.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this,
            "<html>Xác nhận thanh toán và nhập kho cho phiếu <b>#" + maPN + "</b>?<br>"
            + "Hệ thống sẽ tự động cộng số lượng vào tồn kho.<br>"
            + "<font color='green'>Thao tác này là an toàn.</font></html>",
            "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                if (phieuNhapBUS.updateTrangThai(maPN, "HoanThanh")) {
                    loadData(); showToast("Đã thanh toán & nhập kho thành công!");
                } else warn("Có lỗi xảy ra khi thanh toán!");
            } catch (Exception ex) { ex.printStackTrace(); warn("Lỗi hệ thống: " + ex.getMessage()); }
        }
    }

    // ================================================================
    // MỞ DIALOG
    // ================================================================
    private void openDialog(Integer maPN) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        String ttl = (maPN == null) ? "Lập phiếu nhập hàng" : "Chi tiết phiếu nhập  #" + maPN;
        JDialog dlg = (owner instanceof Frame)
            ? new JDialog((Frame) owner, ttl, true)
            : new JDialog((Dialog) owner, ttl, true);
        dlg.setSize(1040, 720);
        dlg.setLocationRelativeTo(owner);
        dlg.setResizable(true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setContentPane(new NhapHangDialogPanel(maPN, dlg));
        dlg.setVisible(true);
        loadData();
    }

    // ================================================================
    // INNER CLASS: DIALOG
    // ================================================================
    class NhapHangDialogPanel extends JPanel {

        private final Integer maPN;
        private final JDialog parentDlg;
        private final boolean viewOnly;

        private JComboBox<NhaCungCapDTO> cbNCC;
        private JTextField txtNgayNhap, txtGhiChu;
        private JTextField txtMaSP, txtTenSP, txtDonGia, txtSoLuong;

        private JTable            tblChiTiet;
        private DefaultTableModel modelChiTiet;
        private JLabel            lblTongTien, lblSoDong;

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
            if (viewOnly) loadViewData();
        }

        private JPanel buildDlgHeader() {
            JPanel p = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(0, 52));
            p.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            JLabel lbl = new JLabel(viewOnly ? "CHI TIẾT PHIẾU NHẬP  #" + maPN : "LẬP PHIẾU NHẬP HÀNG MỚI");
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 17)); lbl.setForeground(WHITE);
            JLabel dateLbl = new JLabel("Ngày: " + LocalDate.now());
            dateLbl.setFont(FONT_SMALL); dateLbl.setForeground(new Color(200, 230, 255));
            p.add(lbl, BorderLayout.WEST); p.add(dateLbl, BorderLayout.EAST);
            return p;
        }

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
            p.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER, 1), BorderFactory.createEmptyBorder(12, 16, 12, 16)));

            ArrayList<NhaCungCapDTO> listNCC = nhaCungCapBUS.getDanhSachHoatDong();
            cbNCC = new JComboBox<>();
            cbNCC.addItem(new NhaCungCapDTO() {{ setMaNCC(0); setTenNCC("-- Chọn nhà cung cấp --"); }});
            for (NhaCungCapDTO ncc : listNCC) cbNCC.addItem(ncc);
            cbNCC.setFont(FONT_NORMAL);
            cbNCC.setPreferredSize(new Dimension(0, 32));
            cbNCC.setEnabled(!viewOnly);

            txtNgayNhap = new JTextField(LocalDate.now().toString());
            txtNgayNhap.setEditable(false); txtNgayNhap.setFocusable(false);
            txtNgayNhap.setBackground(new Color(235, 240, 248));
            txtNgayNhap.setForeground(new Color(60, 80, 120));
            styleField(txtNgayNhap);

            txtGhiChu = new JTextField();
            txtGhiChu.setEnabled(!viewOnly);
            styleField(txtGhiChu);

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(0, 8, 0, 8); gc.fill = GridBagConstraints.HORIZONTAL; gc.anchor = GridBagConstraints.WEST;
            gc.gridx = 0; gc.weightx = 0;   p.add(makeLabel("Nhà cung cấp (*):"), gc);
            gc.gridx = 1; gc.weightx = 1.6; p.add(cbNCC, gc);
            gc.gridx = 2; gc.weightx = 0;   p.add(makeLabel("Ngày nhập:"), gc);
            gc.gridx = 3; gc.weightx = 0.5; p.add(txtNgayNhap, gc);
            gc.gridx = 4; gc.weightx = 0;   p.add(makeLabel("Ghi chú:"), gc);
            gc.gridx = 5; gc.weightx = 1.4; p.add(txtGhiChu, gc);
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
            p.setBackground(new Color(240, 248, 255));
            p.setBorder(new CompoundBorder(new LineBorder(new Color(180, 220, 240), 1), BorderFactory.createEmptyBorder(10, 16, 10, 16)));

            txtMaSP    = new JTextField();    styleField(txtMaSP);
            txtTenSP   = new JTextField();    styleField(txtTenSP); txtTenSP.setEditable(false);
            txtTenSP.setBackground(new Color(245, 248, 252));
            txtDonGia  = new JTextField();    styleField(txtDonGia);
            txtSoLuong = new JTextField("1"); styleField(txtSoLuong);

            txtMaSP.addFocusListener(new FocusAdapter() { @Override public void focusLost(FocusEvent e) { lookupSP(); } });
            txtMaSP.addKeyListener(new KeyAdapter() {
                @Override public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) { lookupSP(); txtDonGia.requestFocus(); }
                }
            });

            JButton btnChonSP = buildActionButton(" Chọn SP", ACCENT, WHITE);
            btnChonSP.setPreferredSize(new Dimension(100, 32));
            btnChonSP.addActionListener(e -> openChonSPDialog());

            JButton btnThem = buildActionButton("+ Thêm", SUCCESS, WHITE);
            btnThem.setPreferredSize(new Dimension(90, 32));
            btnThem.addActionListener(e -> themDong());

            boolean hasNCC = cbNCC.getSelectedIndex() > 0;
            txtMaSP.setEnabled(hasNCC); txtDonGia.setEnabled(hasNCC);
            txtSoLuong.setEnabled(hasNCC); btnChonSP.setEnabled(hasNCC); btnThem.setEnabled(hasNCC);

            cbNCC.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    NhaCungCapDTO sel = (NhaCungCapDTO) cbNCC.getSelectedItem();
                    boolean ok = sel != null && sel.getMaNCC() > 0;
                    txtMaSP.setEnabled(ok); txtDonGia.setEnabled(ok);
                    txtSoLuong.setEnabled(ok); btnChonSP.setEnabled(ok); btnThem.setEnabled(ok);
                    if (ok) txtMaSP.requestFocus();
                    else { txtMaSP.setText(""); txtTenSP.setText(""); txtDonGia.setText(""); txtSoLuong.setText("1"); }
                }
            });

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(0, 6, 0, 6); gc.fill = GridBagConstraints.HORIZONTAL; gc.anchor = GridBagConstraints.CENTER;
            gc.gridx = 0; gc.weightx = 0;    p.add(makeLabel("Mã SP:"), gc);
            gc.gridx = 1; gc.weightx = 0.35; p.add(txtMaSP, gc);
            gc.gridx = 2; gc.weightx = 0;    p.add(btnChonSP, gc);
            gc.gridx = 3; gc.weightx = 1.2;  p.add(txtTenSP, gc);
            gc.gridx = 4; gc.weightx = 0;    p.add(makeLabel("Đơn giá nhập (đ):"), gc);
            gc.gridx = 5; gc.weightx = 0.65; p.add(txtDonGia, gc);
            gc.gridx = 6; gc.weightx = 0;    p.add(makeLabel("SL:"), gc);
            gc.gridx = 7; gc.weightx = 0.25; p.add(txtSoLuong, gc);
            gc.gridx = 8; gc.weightx = 0;    p.add(btnThem, gc);
            return p;
        }

        private JPanel buildChiTietTable() {
            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(WHITE);
            p.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER, 1), BorderFactory.createEmptyBorder(6, 6, 6, 6)));

            String[] cols = { "STT", "Mã SP", "Tên sản phẩm", "Đơn giá nhập (đ)", "Số lượng", "Thành tiền (đ)" };
            modelChiTiet = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return !viewOnly && (c == 3 || c == 4); }
            };

            tblChiTiet = new JTable(modelChiTiet) {
                @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                    JLabel c = (JLabel) super.prepareRenderer(r, row, col);
                    c.setFont(FONT_NORMAL);
                    c.setHorizontalAlignment(col == 0 || col == 1 || col == 4 ? SwingConstants.CENTER : SwingConstants.LEFT);
                    if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                    else c.setBackground(new Color(187, 222, 251));
                    c.setForeground(new Color(30, 40, 60));
                    return c;
                }
            };
            styleTable(tblChiTiet);
            tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(45);
            tblChiTiet.getColumnModel().getColumn(1).setPreferredWidth(65);
            tblChiTiet.getColumnModel().getColumn(2).setPreferredWidth(310);
            tblChiTiet.getColumnModel().getColumn(3).setPreferredWidth(160);
            tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(80);
            tblChiTiet.getColumnModel().getColumn(5).setPreferredWidth(160);

            modelChiTiet.addTableModelListener(e -> {
                int col = e.getColumn();
                if ((col == 3 || col == 4) && e.getFirstRow() >= 0) recalcRow(e.getFirstRow());
            });

            JScrollPane sc = new JScrollPane(tblChiTiet);
            sc.setBorder(null); sc.getVerticalScrollBar().setUnitIncrement(16);
            p.add(sc, BorderLayout.CENTER);
            return p;
        }

        private JPanel buildDlgFooter() {
            JPanel outer = new JPanel(new BorderLayout());
            outer.setBackground(CONTENT_BG);

            JPanel bar = new JPanel(new GridLayout(1, 2, 1, 0));
            bar.setBackground(new Color(8, 50, 110));
            bar.setPreferredSize(new Dimension(0, 54));
            lblSoDong   = new JLabel("0 mặt hàng", SwingConstants.CENTER);
            lblTongTien = new JLabel("0 đ",         SwingConstants.CENTER);
            lblSoDong.setFont(FONT_TOTAL);   lblSoDong.setForeground(new Color(100, 220, 180));
            lblTongTien.setFont(FONT_TOTAL); lblTongTien.setForeground(new Color(255, 220, 100));
            bar.add(makeSummaryBlock("Số dòng:",        lblSoDong));
            bar.add(makeSummaryBlock("Tổng tiền nhập:", lblTongTien));
            outer.add(bar, BorderLayout.NORTH);

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
            btnRow.setBackground(CONTENT_BG);
            if (!viewOnly) {
                JButton btnXoa    = buildActionButton("Xóa dòng",  DANGER,           WHITE);
                JButton btnReset  = buildActionButton("Làm mới",   new Color(90, 100, 115), WHITE);
                JButton btnLuuTam = buildActionButton("LƯU TẠM",  WARNING_COL,      WHITE);
                JButton btnLuu    = buildBigButton("THANH TOÁN",   SUCCESS);
                btnXoa.addActionListener(e   -> xoaDong());
                btnReset.addActionListener(e -> resetDlg());
                btnLuuTam.addActionListener(e -> luuPhieu("ChoXuLy"));
                btnLuu.addActionListener(e   -> luuPhieu("HoanThanh"));
                btnRow.add(btnXoa); btnRow.add(btnReset); btnRow.add(btnLuuTam); btnRow.add(btnLuu);
            } else {
                JButton btnExcel = buildActionButton("↓ Excel", SUCCESS, WHITE);
                JButton btnPdf   = buildActionButton("↓ PDF",   DANGER,  WHITE);
                btnExcel.addActionListener(e -> {
                    try {
                        PhieuNhapDTO pn = phieuNhapBUS.getById(maPN);
                        if (pn != null) PhieuNhapExcelUtils.exportChiTiet(this, pn, chiTietBUS.getByMaPN(maPN));
                    } catch (Exception ex) { warn("Lỗi xuất Excel: " + ex.getMessage()); }
                });
                btnPdf.addActionListener(e -> {
                    try {
                        PhieuNhapDTO pn = phieuNhapBUS.getById(maPN);
                        if (pn != null) PhieuNhapPDFUtils.exportChiTiet(this, pn, chiTietBUS.getByMaPN(maPN));
                    } catch (Exception ex) { warn("Lỗi xuất PDF: " + ex.getMessage()); }
                });
                JButton btnClose = buildBigButton("Đóng", PRIMARY);
                btnClose.addActionListener(e -> parentDlg.dispose());
                btnRow.add(btnExcel); btnRow.add(btnPdf); btnRow.add(btnClose);
            }
            outer.add(btnRow, BorderLayout.SOUTH);
            return outer;
        }

        // ------ LOGIC ------
        private int getSelectedMaNCC() {
            NhaCungCapDTO sel = (NhaCungCapDTO) cbNCC.getSelectedItem();
            return sel == null ? 0 : sel.getMaNCC();
        }

        private void lookupSP() {
            String txt = txtMaSP.getText().trim();
            if (txt.isEmpty()) return;
            int maNCC = getSelectedMaNCC();
            if (maNCC <= 0) { txtTenSP.setText("⚠ Vui lòng chọn NCC trước!"); txtDonGia.setText(""); return; }
            try {
                int maSP = Integer.parseInt(txt);
                SanPhamDTO sp = sanPhamBUS.timTheoMa(maSP);
                if (sp == null) { txtTenSP.setText("⚠ Mã SP không tồn tại"); txtDonGia.setText(""); return; }
                NhaCungCapDTO selNCC = (NhaCungCapDTO) cbNCC.getSelectedItem();
                try {
                    sanPhamBUS.kiemTraSPThuocNCC(maSP, maNCC, sp.getTenSP(), selNCC != null ? selNCC.getTenNCC() : "NCC #" + maNCC);
                } catch (IllegalArgumentException e) {
                    txtTenSP.setText("⚠ SP không thuộc NCC này"); txtDonGia.setText("");
                    JOptionPane.showMessageDialog(this, "<html>" + e.getMessage().replace("\n", "<br>") + "</html>",
                            "Sản phẩm không hợp lệ", JOptionPane.WARNING_MESSAGE); return;
                }
                txtTenSP.setText(sp.getTenSP());
                BigDecimal giaGoc = sp.getGiaGoc();
                txtDonGia.setText(giaGoc != null ? giaGoc.toPlainString() : sp.getGia().toPlainString());
            } catch (NumberFormatException ex) { txtTenSP.setText("⚠ Mã không hợp lệ"); }
        }

        private void openChonSPDialog() {
            int maNCC = getSelectedMaNCC();
            if (maNCC <= 0) { warn("Vui lòng chọn nhà cung cấp trước!"); return; }

            ArrayList<SanPhamDTO> dsSpTheoNCC = sanPhamBUS.getSanPhamByNhaCungCap(maNCC);
            Window owner = SwingUtilities.getWindowAncestor(this);
            JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Chọn sản phẩm", true)
                : new JDialog((Dialog) owner, "Chọn sản phẩm", true);
            dlg.setSize(740, 500); dlg.setLocationRelativeTo(owner);

            JPanel content = new JPanel(new BorderLayout(6, 6));
            content.setBackground(CONTENT_BG); content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JTextField txtKw = new JTextField();
            txtKw.setFont(FONT_NORMAL);
            txtKw.setBorder(new CompoundBorder(new LineBorder(new Color(180, 210, 240), 1), BorderFactory.createEmptyBorder(5, 10, 5, 10)));

            String[] cols = { "Mã SP", "Tên sản phẩm", "Thương hiệu", "Giá gốc (đ)", "Tồn kho" };
            DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (SanPhamDTO sp : dsSpTheoNCC) {
                BigDecimal goc = sp.getGiaGoc() != null ? sp.getGiaGoc() : sp.getGia();
                mdl.addRow(new Object[]{ sp.getMaSP(), sp.getTenSP(), sp.getThuongHieu(), formatMoney(goc), sp.getSoLuongTon() });
            }

            JTable tbl = new JTable(mdl);
            tbl.setRowHeight(32); tbl.setFont(FONT_NORMAL); tbl.setGridColor(new Color(220, 230, 245));
            tbl.setShowVerticalLines(true); tbl.setSelectionBackground(new Color(187, 222, 251));
            tbl.setSelectionForeground(PRIMARY_DARK); tbl.setIntercellSpacing(new Dimension(0, 1));
            tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            styleTable(tbl);

            txtKw.addKeyListener(new KeyAdapter() {
                @Override public void keyReleased(KeyEvent e) {
                    String kw = txtKw.getText().trim().toLowerCase(); mdl.setRowCount(0);
                    for (SanPhamDTO sp : dsSpTheoNCC) {
                        if (sp.getTenSP().toLowerCase().contains(kw) || sp.getThuongHieu().toLowerCase().contains(kw)
                                || String.valueOf(sp.getMaSP()).contains(kw)) {
                            BigDecimal goc = sp.getGiaGoc() != null ? sp.getGiaGoc() : sp.getGia();
                            mdl.addRow(new Object[]{ sp.getMaSP(), sp.getTenSP(), sp.getThuongHieu(), formatMoney(goc), sp.getSoLuongTon() });
                        }
                    }
                }
            });

            JButton btnChon = buildActionButton("✔ Chọn", PRIMARY, WHITE);
            JButton btnHuy2 = buildActionButton("Hủy", DANGER, WHITE);

            Runnable doChon = () -> {
                int r = tbl.getSelectedRow(); if (r < 0) return;
                int maSP = (int) mdl.getValueAt(r, 0);
                for (SanPhamDTO sp : dsSpTheoNCC) {
                    if (sp.getMaSP() == maSP) {
                        txtMaSP.setText(String.valueOf(sp.getMaSP())); txtTenSP.setText(sp.getTenSP());
                        BigDecimal goc = sp.getGiaGoc() != null ? sp.getGiaGoc() : sp.getGia();
                        txtDonGia.setText(goc.toPlainString()); txtSoLuong.setText("1");
                        dlg.dispose(); break;
                    }
                }
            };
            btnChon.addActionListener(e -> doChon.run()); btnHuy2.addActionListener(e -> dlg.dispose());
            tbl.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) doChon.run(); } });

            JPanel searchRow = new JPanel(new BorderLayout(12, 0)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); g2.dispose();
                }
            };
            searchRow.setOpaque(false); searchRow.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            JLabel lblKw = new JLabel("Tìm sản phẩm:"); lblKw.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblKw.setForeground(Color.WHITE);
            searchRow.add(lblKw, BorderLayout.WEST); searchRow.add(txtKw, BorderLayout.CENTER);

            JPanel btnRow2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); btnRow2.setBackground(CONTENT_BG);
            btnRow2.add(btnHuy2); btnRow2.add(btnChon);

            content.add(searchRow, BorderLayout.NORTH);
            content.add(new JScrollPane(tbl) {{ setBorder(new LineBorder(CARD_BORDER, 1)); }}, BorderLayout.CENTER);
            content.add(btnRow2, BorderLayout.SOUTH);
            dlg.setContentPane(content); dlg.setVisible(true);
        }

        private void themDong() {
            String maSPStr   = txtMaSP.getText().trim();
            String tenSP     = txtTenSP.getText().trim();
            String donGiaStr = txtDonGia.getText().trim().replace(",", "");
            String slStr     = txtSoLuong.getText().trim();
            if (maSPStr.isEmpty() || tenSP.isEmpty() || tenSP.startsWith("⚠")) { warn("Vui lòng chọn sản phẩm hợp lệ!"); return; }
            if (donGiaStr.isEmpty()) { warn("Vui lòng nhập đơn giá nhập!"); return; }

            int maSP; BigDecimal donGia; int sl;
            try { maSP = Integer.parseInt(maSPStr); } catch (Exception ex) { warn("Mã SP không hợp lệ!"); return; }
            try { donGia = new BigDecimal(donGiaStr); if (donGia.compareTo(BigDecimal.ZERO) <= 0) throw new Exception(); }
                catch (Exception ex) { warn("Đơn giá phải lớn hơn 0!"); return; }
            try { sl = Integer.parseInt(slStr); if (sl <= 0) throw new Exception(); }
                catch (Exception ex) { warn("Số lượng phải > 0!"); return; }

            int maNCC = getSelectedMaNCC();
            NhaCungCapDTO selNCC = (NhaCungCapDTO) cbNCC.getSelectedItem();
            try {
                sanPhamBUS.kiemTraSPThuocNCC(maSP, maNCC, tenSP, selNCC != null ? selNCC.getTenNCC() : "NCC #" + maNCC);
            } catch (IllegalArgumentException e) {
                txtMaSP.setText(""); txtTenSP.setText(""); txtDonGia.setText("");
                JOptionPane.showMessageDialog(this, "<html>" + e.getMessage().replace("\n", "<br>") + "</html>",
                        "Không thể thêm sản phẩm", JOptionPane.ERROR_MESSAGE);
                txtMaSP.requestFocus(); return;
            }

            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(sl));
            for (int i = 0; i < chiTietList.size(); i++) {
                if ((int) chiTietList.get(i)[0] == maSP) {
                    int slMoi = (int) chiTietList.get(i)[3] + sl;
                    BigDecimal dg = (BigDecimal) chiTietList.get(i)[2];
                    BigDecimal tt = dg.multiply(BigDecimal.valueOf(slMoi));
                    chiTietList.get(i)[3] = slMoi; chiTietList.get(i)[4] = tt;
                    modelChiTiet.setValueAt(slMoi, i, 4); modelChiTiet.setValueAt(formatMoney(tt), i, 5);
                    showToast("Cộng thêm SL: " + tenSP + " → " + slMoi);
                    clearAddRow(); recalcTongTien(); cbNCC.setEnabled(false); return;
                }
            }
            chiTietList.add(new Object[]{ maSP, tenSP, donGia, sl, thanhTien });
            modelChiTiet.addRow(new Object[]{ modelChiTiet.getRowCount() + 1, maSP, tenSP, formatMoney(donGia), sl, formatMoney(thanhTien) });
            lblSoDong.setText(modelChiTiet.getRowCount() + " mặt hàng");
            showToast("Đã thêm: " + tenSP); clearAddRow(); recalcTongTien(); cbNCC.setEnabled(false);
        }

        private void clearAddRow() {
            txtMaSP.setText(""); txtTenSP.setText(""); txtDonGia.setText(""); txtSoLuong.setText("1"); txtMaSP.requestFocus();
        }

        private void recalcRow(int row) {
            if (row < 0 || row >= chiTietList.size()) return;
            try {
                BigDecimal dg = new BigDecimal(modelChiTiet.getValueAt(row, 3).toString().replace(",", ""));
                int sl = Integer.parseInt(modelChiTiet.getValueAt(row, 4).toString());
                if (dg.compareTo(BigDecimal.ZERO) <= 0 || sl <= 0) return;
                BigDecimal tt = dg.multiply(BigDecimal.valueOf(sl));
                chiTietList.get(row)[2] = dg; chiTietList.get(row)[3] = sl; chiTietList.get(row)[4] = tt;
                modelChiTiet.setValueAt(formatMoney(tt), row, 5); recalcTongTien();
            } catch (Exception ignored) {}
        }

        private void recalcTongTien() {
            BigDecimal tong = BigDecimal.ZERO;
            for (Object[] r : chiTietList) tong = tong.add((BigDecimal) r[4]);
            lblTongTien.setText(formatMoney(tong) + " đ");
        }

        private void xoaDong() {
            int row = tblChiTiet.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn dòng cần xóa!"); return; }
            chiTietList.remove(row); modelChiTiet.removeRow(row);
            for (int i = 0; i < modelChiTiet.getRowCount(); i++) modelChiTiet.setValueAt(i + 1, i, 0);
            lblSoDong.setText(modelChiTiet.getRowCount() + " mặt hàng"); recalcTongTien();
            if (modelChiTiet.getRowCount() == 0) cbNCC.setEnabled(true);
        }

        private void resetDlg() {
            chiTietList.clear(); modelChiTiet.setRowCount(0); cbNCC.setSelectedIndex(0);
            txtNgayNhap.setText(LocalDate.now().toString()); txtGhiChu.setText(""); clearAddRow();
            lblTongTien.setText("0 đ"); lblSoDong.setText("0 mặt hàng"); cbNCC.setEnabled(true);
        }

        private void luuPhieu(String trangThaiTarget) {
            NhaCungCapDTO selNCC = (NhaCungCapDTO) cbNCC.getSelectedItem();
            if (selNCC == null || selNCC.getMaNCC() <= 0) { warn("Vui lòng chọn nhà cung cấp!"); return; }
            if (chiTietList.isEmpty()) { warn("Chưa có sản phẩm nào trong phiếu!"); return; }
            if (SharedData.currentMaNV <= 0) { warn("Không xác định được nhân viên đang đăng nhập!"); return; }

            BigDecimal tong = BigDecimal.ZERO;
            for (Object[] r : chiTietList) tong = tong.add((BigDecimal) r[4]);

            String msg = trangThaiTarget.equals("ChoXuLy")
                ? "Lưu tạm phiếu nhập? (Chưa cộng vào tồn kho)"
                : "Xác nhận thanh toán và nhập kho ngay lập tức?";
            int ok = JOptionPane.showConfirmDialog(this,
                "<html>" + msg + "<br>Nhà cung cấp : <b>" + selNCC.getTenNCC() + "</b><br>"
                + "Tổng tiền : <b>" + formatMoney(tong) + " đ</b></html>",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

            if (ok == JOptionPane.YES_OPTION) {
                try {
                    PhieuNhapDTO pnDTO = new PhieuNhapDTO(selNCC.getMaNCC(), SharedData.currentMaNV,
                            LocalDate.now(), null, txtGhiChu.getText().trim(), trangThaiTarget);
                    int newMaPN = phieuNhapBUS.addPhieuNhap(pnDTO);
                    if (newMaPN <= 0) { warn("Lưu phiếu nhập thất bại!"); return; }
                    for (Object[] ct : chiTietList) {
                        chiTietBUS.them(new ChiTietPhieuNhapDTO(newMaPN, (int) ct[0], (int) ct[3], (BigDecimal) ct[2], null));
                    }
                    phieuNhapBUS.updateTongTien(newMaPN);
                    JOptionPane.showMessageDialog(this,
                        (trangThaiTarget.equals("ChoXuLy") ? "Lưu tạm" : "Lập phiếu") + " #" + newMaPN + " thành công!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    parentDlg.dispose();
                } catch (Exception ex) { ex.printStackTrace(); warn("Lỗi khi lưu phiếu: " + ex.getMessage()); }
            }
        }

        private void loadViewData() {
            try {
                PhieuNhapDTO pn = phieuNhapBUS.getById(maPN);
                if (pn == null) return;
                for (int i = 0; i < cbNCC.getItemCount(); i++) {
                    if (cbNCC.getItemAt(i).getMaNCC() == pn.getMaNhaCungCap()) { cbNCC.setSelectedIndex(i); break; }
                }
                txtNgayNhap.setText(pn.getNgayNhap() != null ? pn.getNgayNhap().toString() : "");
                txtGhiChu.setText(pn.getGhiChu() != null ? pn.getGhiChu() : "");

                ArrayList<ChiTietPhieuNhapDTO> dsChiTiet = chiTietBUS.getByMaPN(maPN);
                modelChiTiet.setRowCount(0); chiTietList.clear();
                BigDecimal tongTien = BigDecimal.ZERO;
                for (ChiTietPhieuNhapDTO ct : dsChiTiet) {
                    BigDecimal tt = ct.getThanhTien() != null ? ct.getThanhTien()
                            : ct.getDonGiaNhap().multiply(BigDecimal.valueOf(ct.getSoLuong()));
                    SanPhamDTO sp = sanPhamBUS.timTheoMa(ct.getMaSP());
                    String tenSP = sp != null ? sp.getTenSP() : "SP #" + ct.getMaSP();
                    chiTietList.add(new Object[]{ ct.getMaSP(), tenSP, ct.getDonGiaNhap(), ct.getSoLuong(), tt });
                    modelChiTiet.addRow(new Object[]{ modelChiTiet.getRowCount() + 1,
                        ct.getMaSP(), tenSP, formatMoney(ct.getDonGiaNhap()), ct.getSoLuong(), formatMoney(tt) });
                    tongTien = tongTien.add(tt);
                }
                lblSoDong.setText(dsChiTiet.size() + " mặt hàng");
                lblTongTien.setText(formatMoney(tongTien) + " đ");
            } catch (Exception e) { e.printStackTrace(); lblTongTien.setText("-- đ"); lblSoDong.setText("Lỗi tải dữ liệu"); }
        }

        private JPanel makeSummaryBlock(String labelText, JLabel valueLabel) {
            JPanel block = new JPanel(new GridBagLayout()); block.setBackground(PRIMARY_DARK);
            JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11)); lbl.setForeground(new Color(170, 205, 255));
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0; gc.gridy = 0; gc.insets = new Insets(4, 8, 1, 8); gc.anchor = GridBagConstraints.CENTER;
            block.add(lbl, gc); gc.gridy = 1; gc.insets = new Insets(1, 8, 4, 8); block.add(valueLabel, gc);
            return block;
        }

        private void styleField(JTextField f) {
            f.setFont(FONT_NORMAL); f.setPreferredSize(new Dimension(0, 32));
            f.setBorder(new CompoundBorder(new LineBorder(new Color(180, 210, 240), 1), BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        }
    } // end NhapHangDialogPanel

    // ================================================================
    // TOAST
    // ================================================================
    private void showToast(String msg) {
        try {
            Window owner = SwingUtilities.getWindowAncestor(this);
            JWindow toast = new JWindow(owner);
            JLabel lbl = new JLabel("  " + msg + "  ");
            lbl.setFont(FONT_LABEL); lbl.setForeground(WHITE); lbl.setOpaque(true); lbl.setBackground(SUCCESS);
            lbl.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(30, 100, 40), 1, true), BorderFactory.createEmptyBorder(7, 12, 7, 12)));
            toast.add(lbl); toast.pack();
            Point loc = getLocationOnScreen();
            toast.setLocation(loc.x + getWidth() - toast.getWidth() - 20, loc.y + getHeight() - toast.getHeight() - 20);
            toast.setVisible(true);
            new Timer(1600, e -> toast.dispose()) {{ setRepeats(false); start(); }};
        } catch (Exception ignored) {}
    }

    // ================================================================
    // SHARED HELPERS
    // ================================================================
    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void styleTable(JTable t) {
        t.setRowHeight(34); t.setFont(FONT_NORMAL); t.setGridColor(new Color(220, 230, 245));
        t.setShowVerticalLines(true); t.setSelectionBackground(new Color(187, 222, 251));
        t.setSelectionForeground(PRIMARY_DARK); t.setIntercellSpacing(new Dimension(0, 1));
        JTableHeader h = t.getTableHeader(); h.setPreferredSize(new Dimension(0, 38)); h.setReorderingAllowed(false);
        DefaultTableCellRenderer hr = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
                setBackground(TABLE_HEADER); setForeground(Color.WHITE); setOpaque(true);
                setFont(new Font("Segoe UI", Font.BOLD, 13)); setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(180, 210, 240)));
                return this;
            }
        };
        for (int i = 0; i < t.getColumnModel().getColumnCount(); i++) t.getColumnModel().getColumn(i).setHeaderRenderer(hr);
    }

    private JButton buildOutlineBtn(String text, String iconType) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 50));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                int iconX = 10, iconY = getHeight() / 2;
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                switch (iconType) {
                    case "add":
                        g2.setColor(WHITE);
                        g2.drawLine(iconX + 5, iconY - 5, iconX + 5, iconY + 5);
                        g2.drawLine(iconX,     iconY,     iconX + 10, iconY);
                        break;
                    case "excel":
                        g2.setColor(new Color(100, 220, 130));
                        g2.fillRoundRect(iconX, iconY - 7, 14, 14, 3, 3);
                        g2.setColor(WHITE);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawLine(iconX + 7, iconY - 7, iconX + 7, iconY + 7);
                        g2.drawLine(iconX, iconY, iconX + 14, iconY);
                        break;
                    case "pdf":
                        g2.setColor(new Color(240, 80, 80));
                        g2.fillRoundRect(iconX, iconY - 7, 12, 15, 3, 3);
                        g2.setColor(WHITE);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawLine(iconX + 2, iconY - 2, iconX + 10, iconY - 2);
                        g2.drawLine(iconX + 2, iconY + 1, iconX + 10, iconY + 1);
                        g2.drawLine(iconX + 2, iconY + 4, iconX + 7,  iconY + 4);
                        break;
                }
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, iconX + 16, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Canvas cv = new Canvas();
        FontMetrics fm = cv.getFontMetrics(new Font("Segoe UI", Font.BOLD, 12));
        int w = fm.stringWidth(text) + 42;
        btn.setPreferredSize(new Dimension(w, 36));
        return btn;
    }

    private JButton buildSolidBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(WHITE); btn.setFont(FONT_LABEL);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
        return btn;
    }

    private JButton buildActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(fg); btn.setFont(FONT_LABEL);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
        return btn;
    }

    private JButton buildBigButton(String text, Color bg) {
        final Font f = new Font("Segoe UI", Font.BOLD, 14);
        Canvas cv = new Canvas(); FontMetrics fm = cv.getFontMetrics(f);
        final int W = fm.stringWidth(text) + 44, H = 42;
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setFont(f); g2.setColor(WHITE);
                FontMetrics tfm = g2.getFontMetrics();
                g2.drawString(text, (getWidth() - tfm.stringWidth(text)) / 2, (getHeight() - tfm.getHeight()) / 2 + tfm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(W, H); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
        };
        btn.setText(""); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(FONT_LABEL); l.setForeground(PRIMARY); return l;
    }

    private String formatMoney(BigDecimal val) {
        if (val == null) return "0";
        return new DecimalFormat("#,###").format(val);
    }
}