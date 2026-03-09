package GUI;

import BUS.KhachHangBUS;
import DTO.KhachHangDTO;

import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class KhachHangPanel extends JPanel {

    // =========================================================================
    // MÀU SẮC
    // =========================================================================
    private static final Color PRIMARY        = new Color(21, 101, 192);
    private static final Color PRIMARY_DARK   = new Color(10, 60, 130);
    private static final Color CONTENT_BG     = new Color(236, 242, 250);
    private static final Color BORDER_COLOR   = new Color(180, 210, 240);
    private static final Color TABLE_HDR_BG   = new Color(21, 101, 192);
    private static final Color ROW_SELECTED   = new Color(187, 222, 251);
    private static final Color ROW_ALT        = new Color(245, 250, 255);
    private static final Color TEXT_DARK      = new Color(10, 60, 130);
    private static final Color WHITE          = Color.WHITE;

    private static final Color ACCENT_YELLOW      = new Color(255, 215, 40);
    private static final Color ACCENT_YELLOW_DARK = new Color(220, 175, 0);
    private static final Color GRAY_BTN           = new Color(134, 142, 150);
    private static final Color GRAY_DARK          = new Color(90, 100, 110);
    private static final Color GREEN_BTN          = new Color(46, 125, 50);
    private static final Color GREEN_DARK         = new Color(27, 94, 32);
    private static final Color RED_BTN            = new Color(198, 40, 40);
    private static final Color RED_DARK           = new Color(150, 20, 20);

    // Badge hạng
    private static final Color C_VO_HANG   = new Color(150, 150, 150);
    private static final Color C_DONG      = new Color(176, 117, 68);
    private static final Color C_BAC       = new Color(130, 148, 165);
    private static final Color C_VANG      = new Color(195, 155, 0);
    private static final Color C_KIM_CUONG = new Color(80, 170, 215);

    private static final Font FNT_TITLE  = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FNT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FNT_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FNT_TABLE  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FNT_HDR    = new Font("Segoe UI", Font.BOLD, 13);

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    // =========================================================================
    // DATA
    // =========================================================================
    private final KhachHangBUS bus = new KhachHangBUS();
    private List<KhachHangDTO> fullList;
    private List<KhachHangDTO> showList;

    // =========================================================================
    // UI COMPONENTS
    // =========================================================================
    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        txtSearch;
    private JComboBox<String> cbTacVu;
    private JComboBox<String> cbHang;
    private JLabel            lblCount;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public KhachHangPanel() {
        SDF.setLenient(false);
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        buildUI();
        loadFromDB();
    }

    // =========================================================================
    // DATA
    // =========================================================================
    private void loadFromDB() {
        fullList = bus.getAll();
        applyFilter();
    }

    private void applyFilter() {
        String kw   = searchKw();
        String hang = cbHang == null ? null : (String) cbHang.getSelectedItem();
        showList = fullList.stream()
            .filter(kh -> kw.isEmpty()
                || contains(kh.getTenKhachHang(), kw)
                || contains(String.valueOf(kh.getMaKhachHang()), kw)
                || contains(kh.getSoDienThoai(), kw)
                || contains(kh.getEmail(), kw))
            .filter(kh -> {
                if (hang == null || "Tất cả hạng".equals(hang)) return true;
                return hang.equalsIgnoreCase(normalizeHang(kh.getHangKhachHang()));
            })
            .collect(Collectors.toList());
        refreshTable();
    }

    private String searchKw() {
        if (txtSearch == null) return "";
        String t = txtSearch.getText().trim();
        return "Tìm kiếm khách hàng...".equals(t) ? "" : t.toLowerCase();
    }

    private boolean contains(String s, String kw) {
        return s != null && s.toLowerCase().contains(kw);
    }

    private String nvl(String s, String def) { return (s != null && !s.isEmpty()) ? s : def; }

    private String normalizeHang(String raw) {
        if (raw == null || raw.isEmpty()) return "Vô hạng";
        return switch (raw.trim()) {
            case "KimCuong", "Kim Cuong", "Kim Cương" -> "Kim Cương";
            case "Vang",  "Vàng"                      -> "Vàng";
            case "Bac",   "Bạc"                       -> "Bạc";
            case "Dong",  "Đồng"                      -> "Đồng";
            case "Thuong","Thường"                    -> "Vô hạng";
            case "VoHang","Vo Hang","Vô hạng"         -> "Vô hạng";
            default -> raw;
        };
    }

    private String hienThiGioiTinh(String gt) {
        if (gt == null || gt.isEmpty()) return "—";
        if (gt.trim().equalsIgnoreCase("Nu")) return "Nữ";
        return gt;
    }

    private String dbGioiTinh(String gt) {
        if (gt == null || gt.isEmpty()) return "Nam";
        if (gt.trim().equalsIgnoreCase("Nữ")) return "Nu";
        return gt;
    }

    // =========================================================================
    // BUILD UI
    // =========================================================================
    private void buildUI() {

        // ── HEADER ────────────────────────────────────────────────────────────
        JPanel topHeader = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        topHeader.setOpaque(false);
        topHeader.setPreferredSize(new Dimension(0, 58));
        topHeader.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JComponent iconCust = new JComponent() {
            { setPreferredSize(new Dimension(36, 58)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cy = getHeight() / 2 - 2;
                g2.drawOval(9, cy - 10, 13, 13);
                g2.drawArc(2, cy + 4, 27, 14, 0, 180);
                g2.dispose();
            }
        };

        JLabel lblTitle = new JLabel("  QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(FNT_TITLE); lblTitle.setForeground(WHITE);

        lblCount = new JLabel("(0 khách hàng)");
        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCount.setForeground(new Color(180, 220, 255));
        lblCount.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JPanel leftH = new JPanel(new GridBagLayout());
        leftH.setOpaque(false);
        GridBagConstraints lg = new GridBagConstraints(); lg.anchor = GridBagConstraints.CENTER;
        lg.gridx = 0; leftH.add(iconCust, lg);
        lg.gridx = 1; leftH.add(lblTitle, lg);
        lg.gridx = 2; leftH.add(lblCount, lg);
        topHeader.add(leftH, BorderLayout.WEST);

        JPanel rightH = new JPanel(new GridBagLayout());
        rightH.setOpaque(false);
        GridBagConstraints rg = new GridBagConstraints();
        rg.anchor = GridBagConstraints.CENTER; rg.insets = new Insets(0, 8, 0, 0);

        JButton btnExport = buildOutlineBtn("↓ Xuất file");
        JButton btnThem   = buildYellowBtn("+ Thêm khách hàng");
        btnThem.addActionListener(e -> showFormDialog(null));

        rg.gridx = 0; rightH.add(btnExport, rg);
        rg.gridx = 1; rightH.add(btnThem,   rg);
        topHeader.add(rightH, BorderLayout.EAST);

        // ── CONTENT BOX ───────────────────────────────────────────────────────
        JPanel centerPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        centerPanel.setOpaque(false);

        // ── TOOLBAR ───────────────────────────────────────────────────────────
        JPanel toolBar = new JPanel(new BorderLayout(8, 0));
        toolBar.setOpaque(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(12, 14, 10, 14));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);

        cbTacVu = makeStyledCombo(new String[]{
            "Tất cả tác vụ",
            "Đã mua hàng thành công",
            "Đang chờ đổi trả / bảo hành"
        });
        cbTacVu.setPreferredSize(new Dimension(240, 36));
        cbTacVu.addActionListener(e -> applyFilter());

        cbHang = makeStyledCombo(new String[]{
            "Tất cả hạng",
            "Vô hạng", "Đồng", "Bạc", "Vàng", "Kim Cương"
        });
        cbHang.setPreferredSize(new Dimension(160, 36));
        cbHang.addActionListener(e -> applyFilter());

        filterRow.add(cbTacVu);
        filterRow.add(cbHang);
        filterRow.add(buildSearchBar());

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);
        JButton btnView = buildActionBtn("Xem",   GREEN_BTN, GREEN_DARK, WHITE);
        JButton btnEdit = buildActionBtn("Sửa",   PRIMARY,   PRIMARY_DARK, WHITE);
        JButton btnDel  = buildActionBtn("Xóa",   RED_BTN,   RED_DARK,    WHITE);
        btnView.addActionListener(e -> actionView());
        btnEdit.addActionListener(e -> actionEdit());
        btnDel .addActionListener(e -> actionDelete());
        actionRow.add(btnView); actionRow.add(btnEdit); actionRow.add(btnDel);

        toolBar.add(filterRow, BorderLayout.WEST);
        toolBar.add(actionRow, BorderLayout.EAST);

        // ── BẢNG ─────────────────────────────────────────────────────────────
        String[] cols = {"Mã KH", "Họ và tên", "Giới tính", "SĐT", "Email", "Điểm", "Hạng", "Giảm giá"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(FNT_TABLE);
        table.setRowHeight(44);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(220, 232, 245));
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT_DARK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Renderer mặc định — căn giữa tất cả cột
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                setFont(FNT_TABLE); setForeground(TEXT_DARK);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                return this;
            }
        });

        // Renderer cột Hạng — chỉ màu nền + chữ căn giữa, không icon
        table.getColumnModel().getColumn(6).setCellRenderer(new TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                String hang    = val == null ? "Vô hạng" : val.toString();
                Color  bgBadge = hangColor(hang);

                JLabel badge = new JLabel(hang, SwingConstants.CENTER) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(bgBadge);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                badge.setForeground(Color.WHITE);
                badge.setOpaque(false);
                badge.setPreferredSize(new Dimension(100, 26));

                JPanel wrap = new JPanel(new GridBagLayout());
                wrap.setBackground(sel ? ROW_SELECTED : (row % 2 == 0 ? WHITE : ROW_ALT));
                wrap.add(badge);
                return wrap;
            }
        });

        // Renderer cột Giảm giá — căn giữa
        table.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel(val == null ? "0%" : val.toString(), SwingConstants.CENTER);
                lbl.setFont(FNT_BOLD);
                boolean has = val != null && !"0%".equals(val.toString());
                lbl.setForeground(has ? new Color(198, 40, 40) : new Color(150, 160, 175));
                lbl.setBackground(sel ? ROW_SELECTED : (row % 2 == 0 ? WHITE : ROW_ALT));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                return lbl;
            }
        });

        // Header — căn giữa
        JTableHeader header = table.getTableHeader();
        header.setFont(FNT_HDR); header.setBackground(TABLE_HDR_BG);
        header.setForeground(WHITE); header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                lbl.setBackground(TABLE_HDR_BG); lbl.setForeground(WHITE); lbl.setFont(FNT_HDR);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });

        int[] widths = {65, 195, 80, 120, 195, 70, 118, 75};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) actionEdit();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(WHITE);

        centerPanel.add(toolBar, BorderLayout.NORTH);
        centerPanel.add(scroll,  BorderLayout.CENTER);

        // ── OUTER WRAP ────────────────────────────────────────────────────────
        JPanel outerWrap = new JPanel(new BorderLayout(0, 8));
        outerWrap.setBackground(CONTENT_BG);
        outerWrap.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        outerWrap.add(topHeader,   BorderLayout.NORTH);
        outerWrap.add(centerPanel, BorderLayout.CENTER);
        add(outerWrap, BorderLayout.CENTER);
    }

    // =========================================================================
    // SEARCH BAR
    // =========================================================================
    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        bar.setOpaque(false); bar.setPreferredSize(new Dimension(220, 36));

        JComponent searchIcon = new JComponent() {
            { setPreferredSize(new Dimension(34, 36)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(160, 185, 220));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = 13, cy = getHeight()/2, r = 6;
                g2.drawOval(cx-r, cy-r, r*2, r*2);
                g2.drawLine(cx+r-2, cy+r-2, cx+r+4, cy+r+4);
                g2.dispose();
            }
        };

        txtSearch = new JTextField();
        txtSearch.setFont(FNT_NORMAL); txtSearch.setForeground(Color.GRAY);
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 6));
        txtSearch.setOpaque(false);
        final String PH = "Tìm kiếm khách hàng...";
        txtSearch.setText(PH);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (PH.equals(txtSearch.getText())) { txtSearch.setText(""); txtSearch.setForeground(TEXT_DARK); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) { txtSearch.setText(PH); txtSearch.setForeground(Color.GRAY); }
            }
        });
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        bar.add(searchIcon, BorderLayout.WEST);
        bar.add(txtSearch,  BorderLayout.CENTER);
        return bar;
    }

    // =========================================================================
    // REFRESH TABLE
    // =========================================================================
    private void refreshTable() {
        tableModel.setRowCount(0);
        if (showList == null) return;
        for (KhachHangDTO kh : showList) {
            String hang = normalizeHang(kh.getHangKhachHang());
            String giam = kh.getPhanTramGiam() > 0 ? (int) kh.getPhanTramGiam() + "%" : "0%";
            tableModel.addRow(new Object[]{
                kh.getMaKhachHang(), kh.getTenKhachHang(),
                hienThiGioiTinh(kh.getGioiTinh()), nvl(kh.getSoDienThoai(),"—"),
                nvl(kh.getEmail(),"—"), kh.getDiemTichLuy(), hang, giam
            });
        }
        if (lblCount != null) lblCount.setText("(" + showList.size() + " khách hàng)");
    }

    // =========================================================================
    // HANG HELPERS
    // =========================================================================
    private Color hangColor(String h) {
        return switch (h) {
            case "Kim Cương" -> C_KIM_CUONG;
            case "Vàng"      -> C_VANG;
            case "Bạc"       -> C_BAC;
            case "Đồng"      -> C_DONG;
            default          -> C_VO_HANG;
        };
    }

    private String hangText(String h) { return h == null ? "Vô hạng" : h; }

    // =========================================================================
    // ACTIONS
    // =========================================================================
    private KhachHangDTO getSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return showList.get(row);
    }

    private void actionView()   { KhachHangDTO kh = getSelected(); if (kh != null) showViewDialog(kh); }
    private void actionEdit()   { KhachHangDTO kh = getSelected(); if (kh != null) showFormDialog(kh); }
    private void actionDelete() {
        KhachHangDTO kh = getSelected();
        if (kh == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "Xóa khách hàng \"" + kh.getTenKhachHang() + "\"?\nHành động không thể hoàn tác.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                if (bus.delete(kh.getMaKhachHang())) loadFromDB();
                else JOptionPane.showMessageDialog(this, "Xóa thất bại! Khách hàng có thể đang liên kết với hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // DIALOG: XEM CHI TIẾT
    // =========================================================================
    private void showViewDialog(KhachHangDTO kh) {
        JDialog dlg = makeDialog(780, 440, "Chi tiết – " + kh.getTenKhachHang());
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(22, 30, 10, 30));
        GridBagConstraints gc = stdGc();

        String nSinh = kh.getNgaySinh()   != null ? SDF.format(kh.getNgaySinh())   : "—";
        String nDK   = kh.getNgayDangKy() != null ? SDF.format(kh.getNgayDangKy()) : "—";
        String hang  = normalizeHang(kh.getHangKhachHang());
        String giam  = kh.getPhanTramGiam() > 0 ? (int) kh.getPhanTramGiam() + "%" : "0%";

        addViewRow(form, gc, 0, "Mã KH:",       String.valueOf(kh.getMaKhachHang()),   "Hạng KH:",       hang);
        addViewRow(form, gc, 1, "Họ và tên:",   kh.getTenKhachHang(),                  "Điểm tích lũy:", String.valueOf(kh.getDiemTichLuy()));
        addViewRow(form, gc, 2, "Giới tính:",   hienThiGioiTinh(kh.getGioiTinh()),     "Giảm giá:",      giam);
        addViewRow(form, gc, 3, "SĐT:",         nvl(kh.getSoDienThoai(),"—"),           "Email:",         nvl(kh.getEmail(),"—"));
        addViewRow(form, gc, 4, "Ngày sinh:",   nSinh,                                 "Ngày đăng ký:",  nDK);
        addViewRow(form, gc, 5, "Địa chỉ:",     nvl(kh.getDiaChi(),"—"),               null, null);

        JButton btnClose = buildActionBtn("Đóng", GRAY_BTN, GRAY_DARK, WHITE);
        btnClose.addActionListener(e -> dlg.dispose());
        setupDialog(dlg, form, btnClose, null);
    }

    // =========================================================================
    // DIALOG: THÊM / SỬA
    // =========================================================================
    private void showFormDialog(KhachHangDTO kh) {
        boolean isEdit = (kh != null);
        String  title  = isEdit ? "Cập nhật – Mã: " + kh.getMaKhachHang() : "Thêm Khách Hàng Mới";
        JDialog dlg    = makeDialog(880, 510, title);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 6, 30));
        GridBagConstraints gc = stdGc();

        JTextField tfTen    = makeField(isEdit ? nvl(kh.getTenKhachHang(),"") : "");
        JComboBox<String> cbGT = makeCombo(new String[]{"Nam","Nữ","Khác"});
        if (isEdit && kh.getGioiTinh() != null) cbGT.setSelectedItem(hienThiGioiTinh(kh.getGioiTinh()));
        JTextField tfSDT    = makeField(isEdit ? nvl(kh.getSoDienThoai(),"") : "");
        JTextField tfEmail  = makeField(isEdit ? nvl(kh.getEmail(),"")       : "");
        JTextField tfDiaChi = makeField(isEdit ? nvl(kh.getDiaChi(),"")      : "");
        JTextField tfNgSinh = makeDateF(isEdit && kh.getNgaySinh()   != null ? SDF.format(kh.getNgaySinh())   : "");
        JTextField tfNgDK   = makeDateF(isEdit && kh.getNgayDangKy() != null ? SDF.format(kh.getNgayDangKy()) : "");
        JTextField tfDiem   = makeField(isEdit ? String.valueOf(kh.getDiemTichLuy()) : "0");
        if (!isEdit) {
            tfDiem.setEditable(false);
            tfDiem.setBackground(new Color(240, 244, 250));
            tfDiem.setForeground(new Color(140, 160, 190));
            tfDiem.setToolTipText("Điểm tích lũy được cộng tự động sau mỗi đơn hàng");
        }

        JLabel lblHangAuto = new JLabel();
        updateHangLabel(lblHangAuto, isEdit ? kh.getDiemTichLuy() : 0);
        tfDiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void up() { try { updateHangLabel(lblHangAuto, Integer.parseInt(tfDiem.getText().trim())); } catch (NumberFormatException ignored) {} }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { up(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { up(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { up(); }
        });

        addFormRow(form, gc, 0, "Họ và tên: *",   tfTen,    "Giới tính:",      cbGT);
        addFormRow(form, gc, 1, "Số điện thoại:", tfSDT,    "Email:",           tfEmail);
        addFormRow(form, gc, 2, "Ngày sinh:",      tfNgSinh, "Địa chỉ:",         tfDiaChi);
        addFormRow(form, gc, 3, "Ngày đăng ký:",  tfNgDK,   "Điểm tích lũy:",   tfDiem);

        // Hạng tự động – 1 hàng span
        JPanel hangRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hangRow.setOpaque(false);
        JLabel lbHang = new JLabel("Hạng (tự động tính):  ");
        lbHang.setFont(FNT_BOLD); lbHang.setForeground(PRIMARY);
        hangRow.add(lbHang); hangRow.add(lblHangAuto);
        gc.gridy = 4; gc.gridx = 0; gc.gridwidth = 5; gc.weightx = 1.0;
        form.add(hangRow, gc);
        gc.gridwidth = 1;

        JButton btnLuu = buildActionBtn(isEdit ? "Cập nhật" : "Lưu", PRIMARY, PRIMARY_DARK, WHITE);
        JButton btnBQ  = buildActionBtn("Hủy", GRAY_BTN, GRAY_DARK, WHITE);
        btnBQ.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            try {
                String ten = tfTen.getText().trim();
                if (ten.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Vui lòng nhập họ và tên!");
                    return;
                }
                KhachHangDTO dto = isEdit ? kh : new KhachHangDTO();
                dto.setTenKhachHang(ten);
                String gt = cbGT.getSelectedItem() != null ? cbGT.getSelectedItem().toString() : "Nam";
                dto.setGioiTinh(dbGioiTinh(gt));
                String sdt = tfSDT.getText().trim();
                dto.setSoDienThoai(sdt.isEmpty() ? null : sdt);
                String email = tfEmail.getText().trim();
                dto.setEmail(email.isEmpty() ? null : email);
                String diaChi = tfDiaChi.getText().trim();
                dto.setDiaChi(diaChi.isEmpty() ? null : diaChi);
                dto.setNgaySinh(parseDate(tfNgSinh.getText()));
                dto.setNgayDangKy(parseDate(tfNgDK.getText()));
                int diem = 0;
                try { diem = Integer.parseInt(tfDiem.getText().trim()); } catch (NumberFormatException ignored) {}
                dto.setDiemTichLuy(Math.max(0, diem));
                if (sdt != null && !sdt.isEmpty() && !sdt.matches("^0\\d{9}$")) {
                    JOptionPane.showMessageDialog(dlg, "Số điện thoại phải có 10 chữ số bắt đầu bằng 0!", "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (email != null && !email.isEmpty() && !(email.contains("@") && email.contains("."))) {
                    JOptionPane.showMessageDialog(dlg, "Email không đúng định dạng!", "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                bus.applyHangVaGiam(dto);
                boolean ok = isEdit ? bus.update(dto) : bus.insert(dto);
                if (ok) {
                    loadFromDB();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(KhachHangPanel.this,
                        isEdit ? "Cập nhật thành công!" : "Thêm thành công!");
                } else {
                    JOptionPane.showMessageDialog(dlg, "Lưu thất bại! Kiểm tra lại dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg,
                    "Lỗi: " + ex.getClass().getSimpleName() + "\n" + ex.getMessage(),
                    "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        });
        setupDialog(dlg, form, btnBQ, btnLuu);
    }

    // ── Cập nhật label hạng trong form — chỉ text + màu, không icon ──────────
    private void updateHangLabel(JLabel lbl, int diem) {
        String tenHang; Color c; String pct;
        if      (diem >= 2000) { tenHang = "Kim Cương"; c = C_KIM_CUONG; pct = "giảm 12%"; }
        else if (diem >= 1000) { tenHang = "Vàng";      c = C_VANG;      pct = "giảm 8%";  }
        else if (diem >=  400) { tenHang = "Bạc";       c = C_BAC;       pct = "giảm 5%";  }
        else if (diem >=  150) { tenHang = "Đồng";      c = C_DONG;      pct = "giảm 2%";  }
        else                   { tenHang = "Vô hạng";   c = C_VO_HANG;   pct = "0%";        }
        lbl.setText("  " + tenHang + "  –  " + pct);
        lbl.setForeground(c);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setIcon(null);
    }

    // =========================================================================
    // DIALOG FACTORY
    // =========================================================================
    private JDialog makeDialog(int w, int h, String title) {
        JDialog dlg = new JDialog(parentFrame(), true);
        dlg.setUndecorated(true);
        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(this);
        // Bắt đầu trong suốt để fade-in
        dlg.setOpacity(0.0f);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WHITE); root.setBorder(new LineBorder(BORDER_COLOR, 1));
        root.add(buildDlgHeader(dlg, title), BorderLayout.NORTH);
        dlg.setContentPane(root);
        return dlg;
    }

    /** Chạy animation fade-in + scale-up khi dialog hiển thị */
    private void animateDialogOpen(JDialog dlg) {
        final int W = dlg.getWidth(), H = dlg.getHeight();
        final Point center = dlg.getLocation();
        final int STEPS = 18;
        final int[] step = {0};
        Timer anim = new Timer(14, null);
        anim.addActionListener(e -> {
            step[0]++;
            float progress = (float) step[0] / STEPS;
            // Ease-out cubic
            float ease = 1f - (1f - progress) * (1f - progress) * (1f - progress);
            // Opacity: 0 → 1
            float opacity = Math.min(1f, ease);
            try { dlg.setOpacity(opacity); } catch (Exception ignored) {}
            // Scale: 92% → 100%
            float scale = 0.92f + 0.08f * ease;
            int nw = (int)(W * scale), nh = (int)(H * scale);
            int nx = center.x + (W - nw) / 2;
            int ny = center.y + (H - nh) / 2;
            dlg.setSize(nw, nh);
            dlg.setLocation(nx, ny);
            if (step[0] >= STEPS) {
                anim.stop();
                dlg.setOpacity(1f);
                dlg.setSize(W, H);
                dlg.setLocation(center);
            }
        });
        anim.start();
    }

    private void setupDialog(JDialog dlg, JPanel form, JButton btn1, JButton btn2) {
        JPanel root = (JPanel) dlg.getContentPane();
        root.add(form, BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        footer.setBackground(WHITE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));
        footer.add(btn1);
        if (btn2 != null) footer.add(btn2);
        root.add(footer, BorderLayout.SOUTH);
        // Hiện dialog trước, rồi chạy animation
        SwingUtilities.invokeLater(() -> animateDialogOpen(dlg));
        dlg.setVisible(true);
    }

    private JPanel buildDlgHeader(JDialog dlg, String title) {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY));
                g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose();
            }
        };
        hdr.setOpaque(false); hdr.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 15)); lblT.setForeground(WHITE);
        JLabel lblX = new JLabel("  X  ");
        lblX.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblX.setForeground(new Color(180, 220, 255));
        lblX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblX.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dlg.dispose(); }
            @Override public void mouseEntered(MouseEvent e) { lblX.setForeground(WHITE); }
            @Override public void mouseExited (MouseEvent e) { lblX.setForeground(new Color(180, 220, 255)); }
        });
        hdr.add(lblT, BorderLayout.WEST); hdr.add(lblX, BorderLayout.EAST);
        Point[] initPt = {null};
        hdr.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { initPt[0] = e.getPoint(); }
        });
        hdr.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (initPt[0] != null)
                    dlg.setLocation(dlg.getX()+e.getX()-initPt[0].x, dlg.getY()+e.getY()-initPt[0].y);
            }
        });
        return hdr;
    }

    // =========================================================================
    // FORM / VIEW ROW HELPERS
    // =========================================================================
    private GridBagConstraints stdGc() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(10, 10, 10, 10);
        return gc;
    }

    private void addFormRow(JPanel p, GridBagConstraints gc, int row,
                            String l1, JComponent f1, String l2, JComponent f2) {
        gc.gridy = row;
        gc.gridx = 0; gc.weightx = 0.0; p.add(lbl(l1, PRIMARY), gc);
        gc.gridx = 1; gc.weightx = 0.4; p.add(f1, gc);
        gc.gridx = 2; gc.weightx = 0.05; p.add(Box.createHorizontalStrut(10), gc);
        if (l2 != null && f2 != null) {
            gc.gridx = 3; gc.weightx = 0.0; p.add(lbl(l2, PRIMARY), gc);
            gc.gridx = 4; gc.weightx = 0.4; p.add(f2, gc);
        }
    }

    private void addViewRow(JPanel p, GridBagConstraints gc, int row,
                             String l1, String v1, String l2, String v2) {
        gc.gridy = row;
        gc.gridx = 0; gc.weightx = 0.0; p.add(lbl(l1, new Color(100,130,170)), gc);
        gc.gridx = 1; gc.weightx = 0.4; p.add(val(v1), gc);
        if (l2 != null) {
            gc.gridx = 2; gc.weightx = 0.05; p.add(Box.createHorizontalStrut(10), gc);
            gc.gridx = 3; gc.weightx = 0.0;  p.add(lbl(l2, new Color(100,130,170)), gc);
            gc.gridx = 4; gc.weightx = 0.4;  p.add(val(v2), gc);
        }
    }

    private JLabel lbl(String t, Color c) { JLabel l = new JLabel(t); l.setFont(FNT_BOLD); l.setForeground(c); return l; }
    private JLabel val(String t)          { JLabel l = new JLabel(t == null || t.isEmpty() ? "—" : t); l.setFont(FNT_NORMAL); l.setForeground(TEXT_DARK); return l; }

    // =========================================================================
    // FIELD / COMBO
    // =========================================================================
    private JTextField makeField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(FNT_NORMAL); tf.setForeground(TEXT_DARK);
        tf.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return tf;
    }

    private JTextField makeDateF(String text) {
        JTextField tf = makeField(text.isEmpty() ? "dd/MM/yyyy" : text);
        if (text.isEmpty()) tf.setForeground(Color.GRAY);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if ("dd/MM/yyyy".equals(tf.getText())) { tf.setText(""); tf.setForeground(TEXT_DARK); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText("dd/MM/yyyy"); tf.setForeground(Color.GRAY); }
            }
        });
        return tf;
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FNT_NORMAL); cb.setBackground(WHITE);
        cb.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        return cb;
    }

    private JComboBox<String> makeStyledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FNT_NORMAL); cb.setBackground(WHITE);
        cb.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return cb;
    }

    private java.sql.Date parseDate(String s) {
        if (s == null || s.isEmpty() || "dd/MM/yyyy".equals(s)) return null;
        try { return new java.sql.Date(SDF.parse(s.trim()).getTime()); }
        catch (ParseException e) { return null; }
    }

    // =========================================================================
    // BUTTON BUILDERS
    // =========================================================================
    private JButton buildYellowBtn(String label) {
        Font f = new Font("Segoe UI", Font.BOLD, 13);
        Canvas cv = new Canvas(); FontMetrics fm = cv.getFontMetrics(f);
        int pw = fm.stringWidth(label) + 36, ph = 36;
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_YELLOW_DARK : ACCENT_YELLOW);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setFont(f); g2.setColor(PRIMARY_DARK);
                FontMetrics m = g2.getFontMetrics();
                g2.drawString(label, (getWidth()-m.stringWidth(label))/2,
                        (getHeight()-m.getHeight())/2 + m.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(pw, ph); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
        };
        btn.setText(""); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildOutlineBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) { g2.setColor(new Color(255,255,255,40)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                g2.setColor(WHITE); g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(WHITE); btn.setFont(FNT_BOLD);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    private JButton buildActionBtn(String text, Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(fg); btn.setFont(FNT_BOLD);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    // =========================================================================
    // MISC
    // =========================================================================
    private JFrame parentFrame() {
        Window w = SwingUtilities.getWindowAncestor(this);
        return (w instanceof JFrame jf) ? jf : null;
    }
}