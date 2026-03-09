package GUI;

import BUS.NhaCungCapBUS;
import DTO.NhaCungCapDTO;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class NhaCungCapPanel extends JPanel {

    // =========================================================================
    // MÃ MÀU
    // =========================================================================
    private static final Color PRIMARY            = new Color(21, 101, 192);
    private static final Color PRIMARY_DARK       = new Color(10, 60, 130);
    private static final Color CONTENT_BG         = new Color(236, 242, 250);
    private static final Color ACCENT_YELLOW      = new Color(255, 215, 40);
    private static final Color ACCENT_YELLOW_DARK = new Color(240, 180, 0);
    private static final Color GRAY_BTN           = new Color(134, 142, 150);
    private static final Color GRAY_DARK          = new Color(108, 117, 125);
    private static final Color ORANGE_BTN         = new Color(243, 156, 18);
    private static final Color ORANGE_DARK        = new Color(211, 84, 0);
    private static final Color RED_BTN            = new Color(198, 40, 40);
    private static final Color RED_DARK           = new Color(160, 20, 20);
    private static final Color SUCCESS            = new Color(46, 125, 50);
    private static final Color TABLE_HEADER_BG    = new Color(21, 101, 192);
    private static final Color BORDER_COLOR       = new Color(180, 210, 240);
    private static final Color TEXT_PRIMARY       = new Color(10, 60, 130);
    private static final Color ROW_SELECTED       = new Color(187, 222, 251);
    private static final Color ROW_ALT            = new Color(245, 250, 255);
    private static final Color WHITE              = Color.WHITE;

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TABLE  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 13);

    // =========================================================================
    // DATA – dùng BUS thay vì DAO
    // =========================================================================
    private final NhaCungCapBUS bus = new NhaCungCapBUS();

    private final List<NhaCungCapDTO> supplierList  = new ArrayList<>();
    private final List<NhaCungCapDTO> filteredList  = new ArrayList<>();

    // =========================================================================
    // UI COMPONENTS
    // =========================================================================
    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        txtSearch;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public NhaCungCapPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(CONTENT_BG);
        buildUI();
        // loadData();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadData(); 
            }
        });
    }

    // =========================================================================
    // LOAD DATA – gọi qua BUS
    // =========================================================================
    private void loadData() {
        supplierList.clear();
        List<NhaCungCapDTO> data = bus.getAll();
        if (data != null) supplierList.addAll(data);
        filteredList.clear();
        filteredList.addAll(supplierList);
        if (tableModel != null) refreshTable();
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

        JComponent supplierIcon = new JComponent() {
            { setPreferredSize(new Dimension(34, 58)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int ox = 3, oy = getHeight() / 2 - 8;
                g2.drawRect(ox, oy + 7, 26, 13);
                int[] rx = {ox, ox+10, ox+10};
                int[] ry = {oy+7, oy, oy+7};
                g2.drawPolygon(rx, ry, 3);
                g2.drawRect(ox+17, oy-2, 5, 9);
                g2.drawRect(ox+11, oy+12, 5, 8);
                g2.drawRect(ox+3,  oy+10, 4, 4);
                g2.dispose();
            }
        };

        JLabel lblTitle = new JLabel("  QUẢN LÝ NHÀ CUNG CẤP");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(WHITE);

        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);
        GridBagConstraints lgc = new GridBagConstraints();
        lgc.anchor = GridBagConstraints.CENTER;
        lgc.gridx = 0; leftPanel.add(supplierIcon, lgc);
        lgc.gridx = 1; leftPanel.add(lblTitle, lgc);
        topHeader.add(leftPanel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setOpaque(false);
        GridBagConstraints agc = new GridBagConstraints();
        agc.anchor = GridBagConstraints.CENTER;
        agc.insets = new Insets(0, 8, 0, 0);

        JButton btnImport = buildOutlineBtn("Import");
        JButton btnExport = buildOutlineBtn("Xuất file");
        JButton btnThem   = buildYellowBtn("+ Nhà cung cấp");
        btnThem.addActionListener(e -> showThemDialog());

        agc.gridx = 0; actionPanel.add(btnImport, agc);
        agc.gridx = 1; actionPanel.add(btnExport, agc);
        agc.gridx = 2; actionPanel.add(btnThem, agc);
        topHeader.add(actionPanel, BorderLayout.EAST);

        // ── CONTENT PANEL ─────────────────────────────────────────────────────
        JPanel centerPanel = new JPanel(new BorderLayout(0, 0)) {
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
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        centerPanel.setOpaque(false);

        // ── TOOLBAR ───────────────────────────────────────────────────────────
        JPanel toolBar = new JPanel(new BorderLayout());
        toolBar.setOpaque(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JPanel searchBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        searchBar.setOpaque(false);
        searchBar.setPreferredSize(new Dimension(380, 36));

        JComponent searchIcon = new JComponent() {
            { setPreferredSize(new Dimension(34, 36)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = 13, cy = getHeight()/2, r = 6;
                g2.setColor(new Color(160, 185, 220));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(cx-r, cy-r, r*2, r*2);
                g2.drawLine(cx+r-2, cy+r-2, cx+r+4, cy+r+4);
                g2.dispose();
            }
        };

        txtSearch = new JTextField();
        txtSearch.setFont(FONT_NORMAL);
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 4));
        txtSearch.setOpaque(false);
        String ph = "Tìm kiếm theo mã, tên, điện thoại nhà cung cấp...";
        txtSearch.setText(ph); txtSearch.setForeground(Color.GRAY);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { if (txtSearch.getText().equals(ph)) { txtSearch.setText(""); txtSearch.setForeground(TEXT_PRIMARY); } }
            @Override public void focusLost(FocusEvent e)   { if (txtSearch.getText().isEmpty()) { txtSearch.setText(ph); txtSearch.setForeground(Color.GRAY); } }
        });
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { doSearch(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { doSearch(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
        });
        searchBar.add(searchIcon, BorderLayout.WEST);
        searchBar.add(txtSearch,  BorderLayout.CENTER);

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchWrap.setOpaque(false);
        searchWrap.add(searchBar);
        toolBar.add(searchWrap, BorderLayout.WEST);

        // ── TABLE ──────────────────────────────────────────────────────────────
        String[] cols = {"Mã nhà cung cấp", "Tên nhà cung cấp", "Điện thoại", "Email", "Địa chỉ", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public Class<?> getColumnClass(int c) { return String.class; }
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(FONT_TABLE);
        table.setRowHeight(44);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setIntercellSpacing(new Dimension(0, 1));

        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, foc, row, col);
                if (!isSel) c.setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                setFont(FONT_TABLE);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                return c;
            }
        });

        // Badge renderer cột Trạng thái
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                String status = val == null ? "" : val.toString();
                boolean active = status.equals("Đang hợp tác");
                JLabel badge = new JLabel(status, SwingConstants.CENTER) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(active ? SUCCESS : RED_BTN);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                        g2.dispose(); super.paintComponent(g);
                    }
                };
                badge.setForeground(WHITE);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
                badge.setOpaque(false);
                JPanel wrap = new JPanel(new GridBagLayout());
                wrap.setBackground(sel ? ROW_SELECTED : (row % 2 == 0 ? WHITE : ROW_ALT));
                badge.setPreferredSize(new Dimension(130, 26));
                wrap.add(badge);
                return wrap;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(WHITE);
        header.setPreferredSize(new Dimension(0, 44));
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, foc, row, col);
                lbl.setBackground(TABLE_HEADER_BG); lbl.setForeground(WHITE);
                lbl.setFont(FONT_HEADER);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(230);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(240);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);

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

        JPanel outerWrap = new JPanel(new BorderLayout(0, 8));
        outerWrap.setBackground(CONTENT_BG);
        outerWrap.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        outerWrap.add(topHeader,   BorderLayout.NORTH);
        outerWrap.add(centerPanel, BorderLayout.CENTER);
        add(outerWrap, BorderLayout.CENTER);
    }

    // =========================================================================
    // ACTIONS
    // =========================================================================
    private NhaCungCapDTO getSelected() {
        int row = table.getSelectedRow();
        return (row == -1) ? null : filteredList.get(row);
    }

    private void actionEdit() {
        NhaCungCapDTO ncc = getSelected();
        if (ncc != null) showCapNhatDialog(ncc);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (NhaCungCapDTO ncc : filteredList) {
            tableModel.addRow(new Object[]{
                bus.formatMa(ncc.getMaNCC()),
                ncc.getTenNCC(),
                ncc.getSDT(),
                ncc.getEmail(),
                ncc.getDiaChi(),
                bus.formatTrangThai(ncc.getTrangThai())   // ← dùng BUS
            });
        }
    }

    /** Tìm kiếm ủy thác hoàn toàn cho BUS */
    private void doSearch() {
        String kw = txtSearch.getText().trim();
        String ph = "tìm kiếm theo mã, tên, điện thoại nhà cung cấp...";
        if (kw.equalsIgnoreCase(ph)) kw = "";

        filteredList.clear();
        filteredList.addAll(bus.search(kw));
        refreshTable();
    }

    // =========================================================================
    // DIALOGS
    // =========================================================================
    private void showThemDialog() {
        JDialog dlg = new JDialog(getParentFrame(), true);
        dlg.setUndecorated(true);
        dlg.setSize(820, 300);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WHITE);
        root.setBorder(new LineBorder(BORDER_COLOR, 1));

        JPanel header = createDialogHeader(dlg, "Thêm Nhà Cung Cấp Mới");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(15, 30, 10, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(14, 10, 14, 10);

        // Sinh mã mới qua BUS
        JTextField tfMa     = makeField("Tự động"); tfMa.setEditable(false);
        JTextField tfTen    = makeField("");
        JTextField tfSDT    = makeField("");
        JTextField tfEmail  = makeField("");
        JTextField tfDiaChi = makeField("");

        addFormRow(form, gc, 0, "Mã nhà cung cấp", tfMa,     "Email",      tfEmail);
        addFormRow(form, gc, 1, "Tên nhà cung cấp", tfTen,    "Điện thoại", tfSDT);
        addFormRow(form, gc, 2, "Địa chỉ",          tfDiaChi, null,         null);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(WHITE);
        JButton btnLuu = buildActionBtn("Lưu thông tin", PRIMARY, PRIMARY_DARK, WHITE);
        JButton btnBQ  = buildActionBtn("Hủy bỏ", GRAY_BTN, GRAY_DARK, WHITE);
        btnBQ.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            // MaNhaCungCap là IDENTITY — SQL Server tự sinh, truyền 0 làm placeholder
            NhaCungCapDTO ncc = new NhaCungCapDTO(
                    0,
                    tfTen.getText().trim(),
                    tfSDT.getText().trim(),
                    tfEmail.getText().trim(),
                    tfDiaChi.getText().trim(), 1);
            try {
                // Validation tập trung tại BUS
                if (bus.insert(ncc)) {
                    loadData(); dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Thêm thành công!");
                } else {
                    JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu vào CSDL.");
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            }
        });
        footer.add(btnBQ); footer.add(btnLuu);

        root.add(header, BorderLayout.NORTH);
        root.add(form,   BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private void showCapNhatDialog(NhaCungCapDTO ncc) {
        JDialog dlg = new JDialog(getParentFrame(), true);
        dlg.setUndecorated(true);
        dlg.setSize(820, 300);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WHITE);
        root.setBorder(new LineBorder(BORDER_COLOR, 1));

        JPanel header = createDialogHeader(dlg, "Chi tiết nhà cung cấp — Mã: " + bus.formatMa(ncc.getMaNCC()));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(15, 30, 10, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(14, 10, 14, 10);

        JTextField tfMa     = makeField(bus.formatMa(ncc.getMaNCC())); tfMa.setEditable(false);
        JTextField tfTen    = makeField(ncc.getTenNCC());
        JTextField tfSDT    = makeField(ncc.getSDT());
        JTextField tfEmail  = makeField(ncc.getEmail());
        JTextField tfDiaChi = makeField(ncc.getDiaChi());

        addFormRow(form, gc, 0, "Mã nhà cung cấp", tfMa,     "Email",      tfEmail);
        addFormRow(form, gc, 1, "Tên nhà cung cấp", tfTen,    "Điện thoại", tfSDT);
        addFormRow(form, gc, 2, "Địa chỉ",          tfDiaChi, null,         null);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(WHITE);
        JButton btnSua = buildActionBtn("Cập nhật", PRIMARY, PRIMARY_DARK, WHITE);
        
        // 1. TẠO NÚT TRẠNG THÁI ĐỘNG (BỎ NÚT XÓA)
        boolean isActive = (ncc.getTrangThai() == 1);
        String lblTrangThai = isActive ? "Ngừng hợp tác" : "Tiếp tục hợp tác";
        Color bgTrangThai   = isActive ? RED_BTN : SUCCESS;
        Color hoverTrangThai= isActive ? RED_DARK : new Color(30, 100, 40);
        
        JButton btnTrangThai = buildActionBtn(lblTrangThai, bgTrangThai, hoverTrangThai, WHITE);
        JButton btnBQ        = buildActionBtn("Đóng", GRAY_BTN, GRAY_DARK, WHITE);

        btnSua.addActionListener(e -> {
            ncc.setTenNCC(tfTen.getText().trim());
            ncc.setSDT(tfSDT.getText().trim());
            ncc.setEmail(tfEmail.getText().trim());
            ncc.setDiaChi(tfDiaChi.getText().trim());
            try {
                if (bus.update(ncc)) {
                    loadData(); doSearch(); dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                } else {
                    JOptionPane.showMessageDialog(this, "Có lỗi khi cập nhật CSDL!");
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            }
        });

        // 2. SỰ KIỆN CẬP NHẬT TRẠNG THÁI (BẮT LỖI RÕ RÀNG)
        btnTrangThai.addActionListener(e -> {
            String msg = bus.getToggleConfirmMessage(ncc);
            if (JOptionPane.showConfirmDialog(dlg, msg, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    if (bus.toggleTrangThai(ncc)) { 
                        JOptionPane.showMessageDialog(dlg, "Đã cập nhật trạng thái thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        loadData(); 
                        doSearch(); // Ép UI vẽ lại chính xác dữ liệu sau khi đổi
                        dlg.dispose(); 
                    } else {
                        // NẾU CODE RƠI VÀO ĐÂY: Có nghĩa là lệnh UPDATE dưới SQL bị lỗi
                        JOptionPane.showMessageDialog(dlg, "Cập nhật thất bại!\nCó thể do ràng buộc (Check Constraint) dưới CSDL.", "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnBQ.addActionListener(e -> dlg.dispose());
        
        // 3. ADD CÁC NÚT VÀO FOOTER (ĐÃ LOẠI BỎ btnXoa)
        footer.add(btnSua); 
        footer.add(btnTrangThai); 
        footer.add(btnBQ);

        root.add(header, BorderLayout.NORTH);
        root.add(form,   BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }


    // =========================================================================
    // UI HELPERS
    // =========================================================================
    private JTextField makeField(String text) {
        if (text == null) text = "";
        JTextField tf = new JTextField(text);
        tf.setFont(FONT_NORMAL); tf.setForeground(TEXT_PRIMARY);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    private void addFormRow(JPanel p, GridBagConstraints gc, int row,
                             String l1, JComponent f1, String l2, JComponent f2) {
        gc.gridy = row;
        gc.gridx = 0; gc.weightx = 0.05; JLabel lb1 = new JLabel(l1); lb1.setFont(FONT_LABEL); lb1.setForeground(PRIMARY); p.add(lb1, gc);
        gc.gridx = 1; gc.weightx = 0.40; p.add(f1, gc);
        gc.gridx = 2; gc.weightx = 0.05; p.add(Box.createHorizontalStrut(20), gc);
        if (l2 != null && f2 != null) {
            gc.gridx = 3; gc.weightx = 0.05; JLabel lb2 = new JLabel(l2); lb2.setFont(FONT_LABEL); lb2.setForeground(PRIMARY); p.add(lb2, gc);
            gc.gridx = 4; gc.weightx = 0.40; p.add(f2, gc);
        }
    }

    private JPanel createDialogHeader(JDialog dlg, String title) {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(WHITE);

        JLabel lblClose = new JLabel("  ✕  ");
        lblClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblClose.setForeground(new Color(200, 230, 255));
        lblClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClose.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dlg.dispose(); }
            @Override public void mouseEntered(MouseEvent e) { lblClose.setForeground(WHITE); }
            @Override public void mouseExited(MouseEvent e)  { lblClose.setForeground(new Color(200, 230, 255)); }
        });

        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblClose, BorderLayout.EAST);

        Point[] initClick = new Point[1];
        header.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { initClick[0] = e.getPoint(); }
        });
        header.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                dlg.setLocation(dlg.getLocation().x + e.getX() - initClick[0].x,
                                dlg.getLocation().y + e.getY() - initClick[0].y);
            }
        });
        return header;
    }

    private JFrame getParentFrame() {
        Window w = SwingUtilities.getWindowAncestor(this);
        return (w instanceof JFrame) ? (JFrame) w : null;
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
        btn.setForeground(fg); btn.setFont(FONT_LABEL);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    private JButton buildYellowBtn(String label) {
        Font btnFont = new Font("Segoe UI", Font.BOLD, 13);
        Canvas cv = new Canvas();
        FontMetrics fm = cv.getFontMetrics(btnFont);
        final int prefW = fm.stringWidth(label) + 36, prefH = 36;
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_YELLOW_DARK : ACCENT_YELLOW);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setFont(btnFont); g2.setColor(PRIMARY_DARK);
                FontMetrics tfm = g2.getFontMetrics();
                g2.drawString(label, (getWidth()-tfm.stringWidth(label))/2, (getHeight()-tfm.getHeight())/2+tfm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(prefW, prefH); }
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
        btn.setForeground(WHITE); btn.setFont(FONT_LABEL);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }
}