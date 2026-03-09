package GUI;

import BUS.NhanVienBUS;
import DTO.NhanVienDTO;
import UTIL.NVExcelUtils;
import UTIL.NVPDFUtils;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class NhanVienPanel extends JPanel {

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
    private static final Color RED_BTN            = new Color(198, 40, 40);
    private static final Color RED_DARK           = new Color(160, 20, 20);
    private static final Color SUCCESS            = new Color(46, 125, 50);
    private static final Color SUCCESS_DARK       = new Color(30, 90, 35);
    private static final Color TABLE_HEADER_BG    = new Color(21, 101, 192);
    private static final Color BORDER_COLOR       = new Color(180, 210, 240);
    private static final Color TEXT_PRIMARY       = new Color(10, 60, 130);
    private static final Color ROW_SELECTED       = new Color(187, 222, 251);
    private static final Color ROW_ALT            = new Color(245, 250, 255);
    private static final Color WHITE              = Color.WHITE;
    private static final Color ORANGE_BTN         = new Color(230, 100, 20);
    private static final Color ORANGE_DARK        = new Color(180, 70, 10);
    private static final Color TEAL_BTN           = new Color(0, 137, 123);
    private static final Color TEAL_DARK          = new Color(0, 105, 92);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TABLE  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 13);

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    // =========================================================================
    // DATA & UI
    // =========================================================================
    private final NhanVienBUS bus = new NhanVienBUS();

    private List<NhanVienDTO> employeeList = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private JComboBox<String> cbFilterVaiTro;
    private JComboBox<String> cbFilterTrangThai;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public NhanVienPanel() {
        SDF.setLenient(false);
        setLayout(new BorderLayout(0, 0));
        setBackground(CONTENT_BG);
        buildUI();
        loadData();
    }

    // =========================================================================
    // LOAD DATA
    // =========================================================================
    private void loadData() {
        employeeList.clear();
        List<NhanVienDTO> data = bus.getAll();
        if (data != null) employeeList.addAll(data);
        refreshTable();
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

        JComponent staffIcon = new JComponent() {
            { setPreferredSize(new Dimension(30, 58)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                int cy = getHeight() / 2 - 4;
                g2.fillOval(9, cy - 9, 12, 12);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(3, cy + 4, 24, 14, 0, 180);
                g2.dispose();
            }
        };

        JLabel lblTitle = new JLabel("  QUẢN LÝ NHÂN VIÊN");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(WHITE);

        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);
        GridBagConstraints lgc = new GridBagConstraints();
        lgc.anchor = GridBagConstraints.CENTER;
        lgc.gridx = 0; leftPanel.add(staffIcon, lgc);
        lgc.gridx = 1; leftPanel.add(lblTitle, lgc);
        topHeader.add(leftPanel, BorderLayout.WEST);

        // ── ACTION BUTTONS TRÊN HEADER ────────────────────────────────────────
        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setOpaque(false);
        GridBagConstraints agc = new GridBagConstraints();
        agc.anchor = GridBagConstraints.CENTER;
        agc.insets = new Insets(0, 8, 0, 0);

        // Nút Phiếu NV — lên header, icon tài liệu G2D, màu teal
        JButton btnPhieuNV = buildIconOutlineBtn("Phiếu NV", g2 -> {
            g2.drawRect(0, 0, 10, 14);
            g2.drawLine(7, 0, 10, 3); g2.drawLine(7, 0, 7, 3); g2.drawLine(7, 3, 10, 3);
            g2.drawLine(2, 6, 8, 6);
            g2.drawLine(2, 9, 8, 9);
            g2.drawLine(2, 12, 6, 12);
        });
        btnPhieuNV.setToolTipText("Xuất phiếu nhân sự PDF của nhân viên đang chọn");
        btnPhieuNV.addActionListener(e -> {
            NhanVienDTO nv = getSelected();
            if (nv != null) NVPDFUtils.exportChiTiet(this, nv);
        });

        // Nút Import Excel — icon mũi tên xuống + bảng
        JButton btnImportExcel = buildIconOutlineBtn("Import Excel", g2 -> {
            g2.drawLine(9, 0, 9, 10);
            g2.drawLine(5, 7, 9, 11); g2.drawLine(13, 7, 9, 11);
            g2.drawLine(1, 14, 17, 14); g2.drawLine(1, 14, 1, 17); g2.drawLine(17, 14, 17, 17);
            g2.drawLine(1, 17, 17, 17);
        });
        btnImportExcel.setToolTipText("Nhập danh sách nhân viên từ file Excel");
        btnImportExcel.addActionListener(e -> doImportExcel());

        // Nút Export Excel — icon bảng + mũi tên lên
        JButton btnExportExcel = buildIconOutlineBtn("Export Excel", g2 -> {
            g2.drawRect(1, 0, 16, 9);
            g2.drawLine(1, 4, 17, 4);
            g2.drawLine(6, 0, 6, 9); g2.drawLine(11, 0, 11, 9);
            g2.drawLine(9, 11, 9, 17);
            g2.drawLine(5, 14, 9, 10); g2.drawLine(13, 14, 9, 10);
        });
        btnExportExcel.setToolTipText("Xuất danh sách nhân viên ra file Excel");
        btnExportExcel.addActionListener(e -> NVExcelUtils.exportExcel(this, employeeList));

        // Nút Xuất PDF — icon tài liệu + mũi tên xuống
        JButton btnExportPDF = buildIconOutlineBtn("Xuất PDF", g2 -> {
            g2.drawRect(1, 0, 12, 16);
            g2.drawLine(9, 0, 13, 4); g2.drawLine(9, 0, 9, 4); g2.drawLine(9, 4, 13, 4);
            g2.drawLine(4, 7,  11, 7);
            g2.drawLine(4, 10, 11, 10);
            g2.drawLine(4, 13, 9,  13);
            g2.drawLine(15, 10, 15, 16);
            g2.drawLine(12, 13, 15, 17); g2.drawLine(18, 13, 15, 17);
        });
        btnExportPDF.setToolTipText("Xuất danh sách nhân viên ra file PDF");
        btnExportPDF.addActionListener(e -> NVPDFUtils.exportDanhSach(this, employeeList));

        agc.gridx = 0; actionPanel.add(btnPhieuNV,     agc);
        agc.gridx = 1; actionPanel.add(btnImportExcel, agc);
        agc.gridx = 2; actionPanel.add(btnExportExcel, agc);
        agc.gridx = 3; actionPanel.add(btnExportPDF,   agc);
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
        };
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true) {
                    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(BORDER_COLOR);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
                        g2.dispose();
                    }
                },
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // ── TOOLBAR ───────────────────────────────────────────────────────────
        JPanel toolBar = new JPanel(new BorderLayout());
        toolBar.setOpaque(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

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
        searchBar.setPreferredSize(new Dimension(230, 36));

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
        String ph = "Tìm kiếm nhân viên...";
        txtSearch.setText(ph);
        txtSearch.setForeground(Color.GRAY);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(ph)) { txtSearch.setText(""); txtSearch.setForeground(TEXT_PRIMARY); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) { txtSearch.setText(ph); txtSearch.setForeground(Color.GRAY); }
            }
        });
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { doSearch(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { doSearch(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
        });
        searchBar.add(searchIcon, BorderLayout.WEST);
        searchBar.add(txtSearch,  BorderLayout.CENTER);

        cbFilterVaiTro = makeStyledCombo(new String[]{"Tất cả vai trò", "Nhân viên bán hàng", "Quản lý"});
        cbFilterVaiTro.setPreferredSize(new Dimension(165, 36));
        cbFilterVaiTro.addActionListener(e -> doSearch());

        cbFilterTrangThai = makeStyledCombo(new String[]{"Tất cả trạng thái", "Đang làm việc", "Đã nghỉ việc"});
        cbFilterTrangThai.setPreferredSize(new Dimension(165, 36));
        cbFilterTrangThai.addActionListener(e -> doSearch());

        searchPanel.add(searchBar);
        searchPanel.add(cbFilterVaiTro);
        searchPanel.add(cbFilterTrangThai);

        // ── BUTTONS HÀNH ĐỘNG TRÊN TỪNG DÒNG ─────────────────────────────────
        JPanel rowActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rowActions.setOpaque(false);
        JButton btnXem        = buildActionBtn("Xem chi tiết", SUCCESS,   SUCCESS_DARK, WHITE);
        JButton btnSua        = buildActionBtn("Sửa",          PRIMARY,   PRIMARY_DARK, WHITE);
        JButton btnXoa        = buildActionBtn("Xóa",          RED_BTN,   RED_DARK,     WHITE);
        // "+ Nhân viên" xuống footer, màu vàng nổi bật
        JButton btnThem       = buildActionBtn("+ Nhân viên",  ACCENT_YELLOW, ACCENT_YELLOW_DARK, PRIMARY_DARK);

        btnXem.addActionListener(e -> actionView());
        btnSua.addActionListener(e -> actionEdit());
        btnXoa.addActionListener(e -> actionDelete());
        btnThem.addActionListener(e -> showThemDialog());

        rowActions.add(btnXem);
        rowActions.add(btnSua);
        rowActions.add(btnXoa);
        rowActions.add(btnThem);

        toolBar.add(searchPanel, BorderLayout.WEST);
        toolBar.add(rowActions,  BorderLayout.EAST);

        // ── TABLE ──────────────────────────────────────────────────────────────
        String[] cols = {"Mã NV", "Tên nhân viên", "Giới tính", "SĐT", "Vai trò", "Tình trạng"};
        tableModel = new DefaultTableModel(cols, 0) {
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

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, foc, row, col);
                if (!isSel) c.setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                setFont(FONT_TABLE);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                return c;
            }
        });

        // Badge renderer cột Tình trạng
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                String status = val == null ? "" : val.toString();
                boolean active = status.equals("Đang làm việc");
                JLabel badge = new JLabel(status, SwingConstants.CENTER) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(active ? new Color(46, 125, 50) : RED_BTN);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                        g2.dispose(); super.paintComponent(g);
                    }
                };
                badge.setForeground(WHITE);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
                badge.setOpaque(false);
                JPanel wrap = new JPanel(new GridBagLayout());
                wrap.setBackground(sel ? ROW_SELECTED : (row % 2 == 0 ? WHITE : ROW_ALT));
                badge.setPreferredSize(new Dimension(120, 26));
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
                lbl.setBackground(TABLE_HEADER_BG);
                lbl.setForeground(WHITE);
                lbl.setFont(FONT_HEADER);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(230);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);

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
    // EXCEL IMPORT
    // =========================================================================
    private void doImportExcel() {
        List<NhanVienDTO> ds = NVExcelUtils.importExcel(this);
        if (ds == null || ds.isEmpty()) return;

        int success = 0, fail = 0;
        StringBuilder errMsg = new StringBuilder();

        for (NhanVienDTO nv : ds) {
            try {
                bus.validate(nv);
                if (bus.insert(nv)) success++;
                else { fail++; errMsg.append("\n- ").append(nv.getTenNV()).append(": lỗi DB"); }
            } catch (IllegalArgumentException ex) {
                fail++;
                errMsg.append("\n- ").append(nv.getTenNV()).append(": ").append(ex.getMessage());
            }
        }

        loadData();

        String msg = "✅ Nhập thành công: " + success + " nhân viên.";
        if (fail > 0) msg += "\n❌ Thất bại: " + fail + " nhân viên:" + errMsg;
        JOptionPane.showMessageDialog(this, msg, "Kết quả Import",
                fail > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // ACTIONS
    // =========================================================================
    private NhanVienDTO getSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return employeeList.get(row);
    }

    private void actionView()   { NhanVienDTO nv = getSelected(); if (nv != null) showXemDialog(nv); }
    private void actionEdit()   { NhanVienDTO nv = getSelected(); if (nv != null) showSuaDialog(nv); }
    private void actionDelete() {
        NhanVienDTO nv = getSelected();
        if (nv == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa nhân viên: " + nv.getTenNV() + "?\nDữ liệu sẽ không thể khôi phục.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                if (bus.delete(nv.getMaNV())) loadData();
                else JOptionPane.showMessageDialog(this, "Xóa thất bại do lỗi dữ liệu ràng buộc!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (NhanVienDTO nv : employeeList) {
            tableModel.addRow(new Object[]{
                nv.getMaNV(), nv.getTenNV(), nv.getGioiTinh(),
                nv.getSoDienThoai(),
                bus.formatVaiTro(nv.getVaiTro()),
                bus.formatTrangThai(nv.getTrangThai())
            });
        }
    }

    private void doSearch() {
        if (cbFilterVaiTro == null || cbFilterTrangThai == null) return;
        String kw = txtSearch.getText().trim();
        if (kw.equalsIgnoreCase("tìm kiếm nhân viên...")) kw = "";
        String role   = cbFilterVaiTro.getSelectedItem().toString();
        String status = cbFilterTrangThai.getSelectedItem().toString();

        employeeList.clear();
        employeeList.addAll(bus.search(kw, role, status));
        refreshTable();
    }

    // =========================================================================
    // DIALOGS
    // =========================================================================
    private void showXemDialog(NhanVienDTO nv) {
        JDialog dlg = new JDialog(getParentFrame(), true);
        dlg.setUndecorated(true);
        dlg.setSize(820, 430);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WHITE);
        root.setBorder(new LineBorder(BORDER_COLOR, 1));

        JPanel header = createDialogHeader(dlg, "Thông tin nhân viên: " + nv.getTenNV());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(12, 10, 12, 10);

        String nSinh = nv.getNgaySinh()   != null ? SDF.format(nv.getNgaySinh())   : "—";
        String nVao  = nv.getNgayVaoLam() != null ? SDF.format(nv.getNgayVaoLam()) : "—";

        addRowLabel(form, gc, 0, "Mã NV:",     String.valueOf(nv.getMaNV()), "Vai trò:",      bus.formatVaiTro(nv.getVaiTro()));
        addRowLabel(form, gc, 1, "Họ và Tên:", nv.getTenNV(),               "Tình trạng:",   bus.formatTrangThai(nv.getTrangThai()));
        addRowLabel(form, gc, 2, "Giới tính:", nv.getGioiTinh(),            "SĐT:",          nv.getSoDienThoai());
        addRowLabel(form, gc, 3, "Ngày sinh:", nSinh,                       "Email:",        nv.getEmail());
        addRowLabel(form, gc, 4, "CCCD:",      nv.getCccd(),                "Ngày vào làm:", nVao);
        addRowLabel(form, gc, 5, "Địa chỉ:",  nv.getDiaChi(),              null, null);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(WHITE);

        // ── NÚT XUẤT PHIẾU PDF ───────────────────────────────────────────────
        JButton btnPDF   = buildIconActionBtn("Xuất Phiếu NV", TEAL_BTN, TEAL_DARK, WHITE, g2 -> {
            g2.drawRect(0, 0, 10, 14);
            g2.drawLine(7, 0, 10, 3); g2.drawLine(7, 0, 7, 3); g2.drawLine(7, 3, 10, 3);
            g2.drawLine(2, 6, 8, 6); g2.drawLine(2, 9, 8, 9); g2.drawLine(2, 12, 6, 12);
        });
        JButton btnClose = buildActionBtn("Đóng", GRAY_BTN, GRAY_DARK, WHITE);

        btnPDF.addActionListener(e -> {
            dlg.dispose();
            NVPDFUtils.exportChiTiet(this, nv);
        });
        btnClose.addActionListener(e -> dlg.dispose());

        footer.add(btnPDF);
        footer.add(btnClose);

        root.add(header, BorderLayout.NORTH);
        root.add(form,   BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private void showThemDialog() {
        JDialog dlg = new JDialog(getParentFrame(), true);
        dlg.setUndecorated(true);
        dlg.setSize(860, 490);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WHITE);
        root.setBorder(new LineBorder(BORDER_COLOR, 1));

        JPanel header = createDialogHeader(dlg, "Thêm Nhân Viên Mới");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(13, 10, 13, 10);

        JTextField        tfTen    = makeField("");
        JComboBox<String> cbGT     = makeCombo(new String[]{"Nam", "Nữ"});
        JTextField        tfCCCD   = makeField(""); setNumericOnly(tfCCCD);
        JTextField        tfSDT    = makeField(""); setNumericOnly(tfSDT);
        JTextField        tfEmail  = makeField("");
        JTextField        tfDiaChi = makeField("");
        JTextField        tfNgSinh = makeField("dd/MM/yyyy"); styleDateField(tfNgSinh);
        JTextField        tfNgVao  = makeField("dd/MM/yyyy"); styleDateField(tfNgVao);
        JComboBox<String> cbVaiTro = makeCombo(new String[]{"Nhân viên bán hàng", "Quản lý"});
        JComboBox<String> cbTThai  = makeCombo(new String[]{"Đang làm việc", "Đã nghỉ việc"});

        addFormRow(form, gc, 0, "Họ và Tên *",   tfTen,    "Giới Tính",     cbGT);
        addFormRow(form, gc, 1, "Số CCCD",        tfCCCD,   "Số Điện Thoại", tfSDT);
        addFormRow(form, gc, 2, "Ngày Sinh",      tfNgSinh, "Email",         tfEmail);
        addFormRow(form, gc, 3, "Ngày Vào Làm",   tfNgVao,  "Địa Chỉ",      tfDiaChi);
        addFormRow(form, gc, 4, "Vai Trò",        cbVaiTro, "Tình Trạng",    cbTThai);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(WHITE);
        JButton btnLuu = buildActionBtn("Lưu thông tin", PRIMARY, PRIMARY_DARK, WHITE);
        JButton btnBQ  = buildActionBtn("Hủy bỏ", GRAY_BTN, GRAY_DARK, WHITE);
        btnBQ.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            Date ngaySinh   = parseDate(tfNgSinh.getText());
            Date ngayVaoLam = parseDate(tfNgVao.getText());
            NhanVienDTO newNV = new NhanVienDTO(0,
                    tfTen.getText().trim(),
                    cbGT.getSelectedItem().toString(),
                    tfSDT.getText().trim(),
                    tfEmail.getText().trim(),
                    tfDiaChi.getText().trim(),
                    ngaySinh, ngayVaoLam,
                    bus.parseVaiTro(cbVaiTro.getSelectedItem().toString()),
                    bus.parseTrangThai(cbTThai.getSelectedItem().toString()),
                    tfCCCD.getText().trim());
            try {
                bus.validate(newNV);
                if (!tfNgSinh.getText().equals("dd/MM/yyyy") && ngaySinh == null) {
                    JOptionPane.showMessageDialog(dlg, "Ngày sinh không đúng định dạng dd/MM/yyyy!"); return;
                }
                if (!tfNgVao.getText().equals("dd/MM/yyyy") && ngayVaoLam == null) {
                    JOptionPane.showMessageDialog(dlg, "Ngày vào làm không đúng định dạng dd/MM/yyyy!"); return;
                }
                if (bus.insert(newNV)) {
                    loadData(); dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
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

    private void showSuaDialog(NhanVienDTO nv) {
        JDialog dlg = new JDialog(getParentFrame(), true);
        dlg.setUndecorated(true);
        dlg.setSize(860, 490);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WHITE);
        root.setBorder(new LineBorder(BORDER_COLOR, 1));

        JPanel header = createDialogHeader(dlg, "Cập nhật nhân viên — Mã: " + nv.getMaNV());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(13, 10, 13, 10);

        JTextField        tfTen    = makeField(nv.getTenNV());
        JComboBox<String> cbGT     = makeCombo(new String[]{"Nam", "Nữ"});
        if (nv.getGioiTinh() != null) cbGT.setSelectedItem(nv.getGioiTinh());
        JTextField        tfCCCD   = makeField(nv.getCccd());   setNumericOnly(tfCCCD);
        JTextField        tfSDT    = makeField(nv.getSoDienThoai()); setNumericOnly(tfSDT);
        JTextField        tfEmail  = makeField(nv.getEmail());
        JTextField        tfDiaChi = makeField(nv.getDiaChi());
        JTextField        tfNgSinh = makeField(nv.getNgaySinh()   != null ? SDF.format(nv.getNgaySinh())   : "dd/MM/yyyy"); styleDateField(tfNgSinh);
        JTextField        tfNgVao  = makeField(nv.getNgayVaoLam() != null ? SDF.format(nv.getNgayVaoLam()) : "dd/MM/yyyy"); styleDateField(tfNgVao);
        JComboBox<String> cbVaiTro = makeCombo(new String[]{"Nhân viên bán hàng", "Quản lý"});
        if (nv.getVaiTro() != null) cbVaiTro.setSelectedItem(bus.formatVaiTro(nv.getVaiTro()));
        JComboBox<String> cbTThai  = makeCombo(new String[]{"Đang làm việc", "Đã nghỉ việc"});
        if (nv.getTrangThai() != null) cbTThai.setSelectedItem(bus.formatTrangThai(nv.getTrangThai()));

        addFormRow(form, gc, 0, "Họ và Tên *",   tfTen,    "Giới Tính",     cbGT);
        addFormRow(form, gc, 1, "Số CCCD",        tfCCCD,   "Số Điện Thoại", tfSDT);
        addFormRow(form, gc, 2, "Ngày Sinh",      tfNgSinh, "Email",         tfEmail);
        addFormRow(form, gc, 3, "Ngày Vào Làm",   tfNgVao,  "Địa Chỉ",      tfDiaChi);
        addFormRow(form, gc, 4, "Vai Trò",        cbVaiTro, "Tình Trạng",    cbTThai);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(WHITE);
        JButton btnLuu = buildActionBtn("Cập nhật", PRIMARY, PRIMARY_DARK, WHITE);
        JButton btnBQ  = buildActionBtn("Hủy bỏ", GRAY_BTN, GRAY_DARK, WHITE);
        btnBQ.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            Date ngaySinh   = parseDate(tfNgSinh.getText());
            Date ngayVaoLam = parseDate(tfNgVao.getText());

            nv.setTenNV(tfTen.getText().trim());
            nv.setGioiTinh(cbGT.getSelectedItem().toString());
            nv.setCccd(tfCCCD.getText().trim());
            nv.setSoDienThoai(tfSDT.getText().trim());
            nv.setEmail(tfEmail.getText().trim());
            nv.setDiaChi(tfDiaChi.getText().trim());
            nv.setNgaySinh(ngaySinh);
            nv.setNgayVaoLam(ngayVaoLam);
            nv.setVaiTro(bus.parseVaiTro(cbVaiTro.getSelectedItem().toString()));
            nv.setTrangThai(bus.parseTrangThai(cbTThai.getSelectedItem().toString()));

            try {
                bus.validate(nv);
                if (!tfNgSinh.getText().equals("dd/MM/yyyy") && ngaySinh == null) {
                    JOptionPane.showMessageDialog(dlg, "Ngày sinh không đúng định dạng dd/MM/yyyy!"); return;
                }
                if (!tfNgVao.getText().equals("dd/MM/yyyy") && ngayVaoLam == null) {
                    JOptionPane.showMessageDialog(dlg, "Ngày vào làm không đúng định dạng dd/MM/yyyy!"); return;
                }
                if (bus.update(nv)) {
                    loadData(); dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                } else {
                    JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cập nhật CSDL.");
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

    // =========================================================================
    // UI HELPERS
    // =========================================================================
    private Date parseDate(String s) {
        if (s == null || s.trim().isEmpty() || s.equals("dd/MM/yyyy")) return null;
        try { return new Date(SDF.parse(s.trim()).getTime()); } catch (ParseException e) { return null; }
    }

    private void setNumericOnly(JTextField tf) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void replace(FilterBypass fb, int off, int len, String text, AttributeSet attr) throws BadLocationException {
                if (text.matches("[0-9]*")) super.replace(fb, off, len, text, attr);
            }
            @Override public void insertString(FilterBypass fb, int off, String str, AttributeSet attr) throws BadLocationException {
                if (str.matches("[0-9]*")) super.insertString(fb, off, str, attr);
            }
        });
    }

    private JTextField makeField(String text) {
        if (text == null) text = "";
        JTextField tf = new JTextField(text);
        tf.setFont(FONT_NORMAL);
        tf.setForeground(TEXT_PRIMARY);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    private void styleDateField(JTextField tf) {
        if (tf.getText().equals("dd/MM/yyyy")) tf.setForeground(Color.GRAY);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { if (tf.getText().equals("dd/MM/yyyy")) { tf.setText(""); tf.setForeground(TEXT_PRIMARY); } }
            @Override public void focusLost(FocusEvent e)   { if (tf.getText().isEmpty()) { tf.setText("dd/MM/yyyy"); tf.setForeground(Color.GRAY); } }
        });
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_NORMAL); cb.setBackground(WHITE);
        cb.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        return cb;
    }

    private JComboBox<String> makeStyledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_NORMAL); cb.setBackground(WHITE);
        cb.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return cb;
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

    private void addRowLabel(JPanel p, GridBagConstraints gc, int row,
                              String l1, String v1, String l2, String v2) {
        gc.gridy = row;
        gc.gridx = 0; gc.weightx = 0.1; JLabel lb1 = new JLabel(l1); lb1.setFont(FONT_LABEL); lb1.setForeground(new Color(100,130,170)); p.add(lb1, gc);
        gc.gridx = 1; gc.weightx = 0.4; JLabel vl1 = new JLabel(v1 != null && !v1.isEmpty() ? v1 : "—"); vl1.setFont(FONT_NORMAL); vl1.setForeground(TEXT_PRIMARY); p.add(vl1, gc);
        if (l2 != null) {
            gc.gridx = 2; gc.weightx = 0.1; JLabel lb2 = new JLabel(l2); lb2.setFont(FONT_LABEL); lb2.setForeground(new Color(100,130,170)); p.add(lb2, gc);
            gc.gridx = 3; gc.weightx = 0.4; JLabel vl2 = new JLabel(v2 != null && !v2.isEmpty() ? v2 : "—"); vl2.setFont(FONT_NORMAL); vl2.setForeground(TEXT_PRIMARY); p.add(vl2, gc);
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
        final int prefW = fm.stringWidth(label) + 36;
        final int prefH = 36;
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_YELLOW_DARK : ACCENT_YELLOW);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setFont(btnFont); g2.setColor(PRIMARY_DARK);
                FontMetrics tfm = g2.getFontMetrics();
                g2.drawString(label, (getWidth() - tfm.stringWidth(label)) / 2,
                        (getHeight() - tfm.getHeight()) / 2 + tfm.getAscent());
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

    /** Nút outline trên header có icon Graphics2D vẽ tay, không dùng emoji */
    private JButton buildIconOutlineBtn(String label, java.util.function.Consumer<Graphics2D> iconPainter) {
        final int ICON_W = 18;
        Font btnFont = new Font("Segoe UI", Font.BOLD, 13);
        Canvas cv = new Canvas();
        FontMetrics fm0 = cv.getFontMetrics(btnFont);
        final int prefW = ICON_W + 6 + fm0.stringWidth(label) + 36;
        final int prefH = 36;
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                // nền hover
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,40));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                }
                // viền outline
                g2.setColor(WHITE); g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,8,8);
                // vẽ icon
                int iconX = 12, iconY = (prefH - ICON_W) / 2;
                Graphics2D gi = (Graphics2D) g2.create();
                gi.translate(iconX, iconY);
                gi.setColor(WHITE);
                gi.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                iconPainter.accept(gi);
                gi.dispose();
                // vẽ text
                g2.setFont(btnFont); g2.setColor(WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int textX = iconX + ICON_W + 6;
                int textY = (prefH - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(label, textX, textY);
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

    /** Nút action (table footer) có icon Graphics2D */
    private JButton buildIconActionBtn(String label, Color bg, Color hover, Color fg,
                                       java.util.function.Consumer<Graphics2D> iconPainter) {
        final int ICON_W = 16;
        Font btnFont = new Font("Segoe UI", Font.BOLD, 13);
        Canvas cv = new Canvas();
        FontMetrics fm0 = cv.getFontMetrics(btnFont);
        final int prefW = ICON_W + 6 + fm0.stringWidth(label) + 36;
        final int prefH = 36;
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                // vẽ icon
                int iconX = 12, iconY = (prefH - ICON_W) / 2;
                Graphics2D gi = (Graphics2D) g2.create();
                gi.translate(iconX, iconY);
                gi.setColor(fg);
                gi.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                iconPainter.accept(gi);
                gi.dispose();
                // vẽ text
                g2.setFont(btnFont); g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                int textX = iconX + ICON_W + 6;
                int textY = (prefH - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(label, textX, textY);
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
}