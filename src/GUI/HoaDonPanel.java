package GUI;

import BUS.HoaDonBUS;
import UTIL.DBConnection;
import UTIL.HoaDonExcelUtils;
import UTIL.HoaDonPDFUtils;
import DTO.HoaDonDTO;
import DTO.SharedData;
import DTO.ChiTietHoaDonDTO;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class HoaDonPanel extends JPanel {

    private static final Color PRIMARY            = new Color(21, 101, 192);
    private static final Color PRIMARY_DARK       = new Color(10, 60, 130);
    private static final Color CONTENT_BG         = new Color(236, 242, 250);
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
    private static final Color ORANGE_BTN         = new Color(230, 140, 0);
    private static final Color ORANGE_DARK        = new Color(180, 100, 0);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TABLE  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 13);

    private static final DateTimeFormatter DTF     = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DTF_SEC = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DecimalFormat     CurrencyFmt;

    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("vi", "VN"));
        sym.setGroupingSeparator('.');
        sym.setDecimalSeparator(',');
        CurrencyFmt = new DecimalFormat("#,###", sym);
    }

    private final HoaDonBUS bus = new HoaDonBUS();

    private ArrayList<HoaDonDTO> hoaDonList = new ArrayList<>();
    private DefaultTableModel    tableModel;
    private JTable               table;
    private JTextField           txtSearch;
    private JComboBox<String>    cbFilterTrangThai;

    private boolean dataLoaded = false;

    private final java.util.HashMap<Integer, String> cacheNV = new java.util.HashMap<>();
    private final java.util.HashMap<Integer, String> cacheKH = new java.util.HashMap<>();

    public HoaDonPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(CONTENT_BG);
        buildUI();
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing() && !dataLoaded) {
                dataLoaded = true;
                loadData();
            }
        });
    }

    private void loadData() {
        cacheNV.clear(); cacheKH.clear();
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{ "...", "\u0110ang t\u1ea3i d\u1eef li\u1ec7u...", "", "", "", "", "", "" });
        SwingWorker<ArrayList<HoaDonDTO>, Void> worker = new SwingWorker<ArrayList<HoaDonDTO>, Void>() {
            @Override protected ArrayList<HoaDonDTO> doInBackground() {
                ArrayList<HoaDonDTO> data = bus.getDanhSachHoaDon();
                if (data == null) data = new ArrayList<>();
                for (HoaDonDTO hd : data) {
                    int maNV = hd.getMaNV();
                    if (!cacheNV.containsKey(maNV)) cacheNV.put(maNV, fetchTenNhanVien(maNV));
                    Integer maKH = hd.getMaKhachHang();
                    if (maKH != null && !cacheKH.containsKey(maKH)) cacheKH.put(maKH, fetchTenKhachHang(maKH));
                }
                return data;
            }
            @Override protected void done() {
                try {
                    ArrayList<HoaDonDTO> data = get();
                    hoaDonList.clear(); hoaDonList.addAll(data); refreshTable();
                } catch (Exception ex) {
                    tableModel.setRowCount(0);
                    tableModel.addRow(new Object[]{ "", "L\u1ed7i t\u1ea3i d\u1eef li\u1ec7u: " + ex.getMessage(), "", "", "", "", "", "" });
                }
            }
        };
        worker.execute();
    }

    private void buildUI() {
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

        JComponent invoiceIcon = new JComponent() {
            { setPreferredSize(new Dimension(30, 58)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                int cx = getWidth() / 2, cy = getHeight() / 2 - 2;
                g2.fillRoundRect(cx - 9, cy - 11, 18, 22, 3, 3);
                g2.setColor(PRIMARY);
                g2.fillRect(cx - 6, cy - 7, 12, 2);
                g2.fillRect(cx - 6, cy - 2, 12, 2);
                g2.fillRect(cx - 6, cy + 3, 8,  2);
                g2.dispose();
            }
        };

        JLabel lblTitle = new JLabel("  QU\u1ea2N L\u00dd H\u00d3A \u0110\u01a0N");
        lblTitle.setFont(FONT_TITLE); lblTitle.setForeground(WHITE);

        JPanel leftPanel = new JPanel(new GridBagLayout()); leftPanel.setOpaque(false);
        GridBagConstraints lgc = new GridBagConstraints(); lgc.anchor = GridBagConstraints.CENTER;
        lgc.gridx = 0; leftPanel.add(invoiceIcon, lgc);
        lgc.gridx = 1; leftPanel.add(lblTitle, lgc);
        topHeader.add(leftPanel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new GridBagLayout()); actionPanel.setOpaque(false);
        GridBagConstraints agc = new GridBagConstraints();
        agc.anchor = GridBagConstraints.CENTER; agc.insets = new Insets(0, 8, 0, 0);

        JButton btnRefresh = buildOutlineBtn("L\u00e0m m\u1edbi", true);
        btnRefresh.addActionListener(e -> loadData());
        JButton btnHeaderExcel = buildOutlineBtnWithIcon("Export Excel", "excel");
        btnHeaderExcel.addActionListener(e -> HoaDonExcelUtils.exportDanhSach(HoaDonPanel.this, hoaDonList));
        JButton btnHeaderPdf = buildOutlineBtnWithIcon("Xu\u1ea5t PDF", "pdf");
        btnHeaderPdf.addActionListener(e -> HoaDonPDFUtils.exportDanhSach(HoaDonPanel.this, hoaDonList));

        agc.gridx = 0; actionPanel.add(btnRefresh, agc);
        agc.gridx = 1; actionPanel.add(btnHeaderExcel, agc);
        agc.gridx = 2; actionPanel.add(btnHeaderPdf, agc);
        topHeader.add(actionPanel, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); g2.dispose();
            }
        };
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true) {
                @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(BORDER_COLOR); g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12); g2.dispose();
                }
            }, BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JPanel toolBar = new JPanel(new BorderLayout()); toolBar.setOpaque(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); searchPanel.setOpaque(false);
        JPanel searchBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8); g2.dispose();
            }
        };
        searchBar.setOpaque(false); searchBar.setPreferredSize(new Dimension(260, 36));

        JComponent searchIcon = new JComponent() {
            { setPreferredSize(new Dimension(34, 36)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = 13, cy = getHeight()/2, r = 6;
                g2.setColor(new Color(160, 185, 220));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(cx-r, cy-r, r*2, r*2); g2.drawLine(cx+r-2, cy+r-2, cx+r+4, cy+r+4);
                g2.dispose();
            }
        };

        txtSearch = new JTextField(); txtSearch.setFont(FONT_NORMAL);
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 4)); txtSearch.setOpaque(false);
        String ph = "T\u00ecm ki\u1ebfm h\u00f3a \u0111\u01a1n...";
        txtSearch.setText(ph); txtSearch.setForeground(Color.GRAY);
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
        searchBar.add(searchIcon, BorderLayout.WEST); searchBar.add(txtSearch, BorderLayout.CENTER);

        cbFilterTrangThai = makeStyledCombo(new String[]{
            "T\u1ea5t c\u1ea3 tr\u1ea1ng th\u00e1i", "Ho\u00e0n th\u00e0nh", "\u0110\u00e3 h\u1ee7y", "Ch\u1edd x\u1eed l\u00fd"
        });
        cbFilterTrangThai.setPreferredSize(new Dimension(165, 36));
        cbFilterTrangThai.addActionListener(e -> doSearch());
        searchPanel.add(searchBar); searchPanel.add(cbFilterTrangThai);

        // Toolbar: CHỈ Xem chi tiết + Xóa (đã bỏ Sửa)
        JPanel rowActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); rowActions.setOpaque(false);
        JButton btnXem = buildActionBtn("Xem chi ti\u1ebft", SUCCESS, SUCCESS_DARK, WHITE);
        JButton btnXoa = buildActionBtn("X\u00f3a", RED_BTN, RED_DARK, WHITE);
        btnXem.addActionListener(e -> actionView());
        btnXoa.addActionListener(e -> actionDelete());
        rowActions.add(btnXem); rowActions.add(btnXoa);

        toolBar.add(searchPanel, BorderLayout.WEST); toolBar.add(rowActions, BorderLayout.EAST);

        String[] cols = {"M\u00e3 HD", "Ng\u00e0y l\u1eadp", "Nh\u00e2n vi\u00ean", "Kh\u00e1ch h\u00e0ng",
                "T\u1ed5ng ti\u1ec1n h\u00e0ng", "% Gi\u1ea3m", "T\u1ed5ng thanh to\u00e1n", "Tr\u1ea1ng th\u00e1i"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(FONT_TABLE); table.setRowHeight(44);
        table.setShowVerticalLines(false); table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR); table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT_PRIMARY); table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setIntercellSpacing(new Dimension(0, 1));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                                                                     boolean isSel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, foc, row, col);
                if (!isSel) c.setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                setFont(FONT_TABLE); setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14)); return c;
            }
        });
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                                                                     boolean sel, boolean foc, int row, int col) {
                String status = val == null ? "" : val.toString();
                Color bgColor;
                switch (status) {
                    case "Ho\u00e0n th\u00e0nh": bgColor = SUCCESS; break;
                    case "\u0110\u00e3 h\u1ee7y": bgColor = RED_BTN; break;
                    default: bgColor = new Color(230, 140, 0); break;
                }
                final Color finalBg = bgColor;
                JLabel badge = new JLabel(status, SwingConstants.CENTER) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(finalBg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                        g2.dispose(); super.paintComponent(g);
                    }
                };
                badge.setForeground(WHITE); badge.setFont(new Font("Segoe UI", Font.BOLD, 12)); badge.setOpaque(false);
                JPanel wrap = new JPanel(new GridBagLayout());
                wrap.setBackground(sel ? ROW_SELECTED : (row % 2 == 0 ? WHITE : ROW_ALT));
                badge.setPreferredSize(new Dimension(110, 26)); wrap.add(badge); return wrap;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER); header.setBackground(TABLE_HEADER_BG); header.setForeground(WHITE);
        header.setPreferredSize(new Dimension(0, 44));
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR)); header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                                                                     boolean isSel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, foc, row, col);
                lbl.setBackground(TABLE_HEADER_BG); lbl.setForeground(WHITE); lbl.setFont(FONT_HEADER); lbl.setOpaque(true);
                if (col == 7) { lbl.setHorizontalAlignment(SwingConstants.CENTER); lbl.setBorder(BorderFactory.createEmptyBorder()); }
                else { lbl.setHorizontalAlignment(SwingConstants.LEFT); lbl.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14)); }
                return lbl;
            }
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(145);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(65);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) actionView();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(WHITE);
        centerPanel.add(toolBar, BorderLayout.NORTH); centerPanel.add(scroll, BorderLayout.CENTER);

        JPanel outerWrap = new JPanel(new BorderLayout(0, 8)); outerWrap.setBackground(CONTENT_BG);
        outerWrap.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        outerWrap.add(topHeader, BorderLayout.NORTH); outerWrap.add(centerPanel, BorderLayout.CENTER);
        add(outerWrap, BorderLayout.CENTER);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (HoaDonDTO hd : hoaDonList) {
            String ngayLap   = hd.getNgayLap() != null ? hd.getNgayLap().format(DTF) : "\u2014";
            String tenNV     = cacheNV.getOrDefault(hd.getMaNV(), layTenNhanVien(hd.getMaNV()));
            String tenKH     = hd.getMaKhachHang() != null ? cacheKH.getOrDefault(hd.getMaKhachHang(), layTenKhachHang(hd.getMaKhachHang())) : "Kh\u00e1ch l\u1ebb";
            String tongTien  = hd.getTongTienHang()     != null ? CurrencyFmt.format(hd.getTongTienHang())    + " \u20ab" : "0 \u20ab";
            String phanTram  = hd.getPhanTramGiamHang() != null ? hd.getPhanTramGiamHang().stripTrailingZeros().toPlainString() + "%" : "0%";
            String tongTT    = hd.getTongThanhToan()    != null ? CurrencyFmt.format(hd.getTongThanhToan())   + " \u20ab" : "0 \u20ab";
            String trangThai = formatTrangThai(hd.getTrangThai());
            tableModel.addRow(new Object[]{ hd.getMaHoaDon(), ngayLap, tenNV, tenKH, tongTien, phanTram, tongTT, trangThai });
        }
    }

    private void doSearch() {
        if (cbFilterTrangThai == null) return;
        String kw = txtSearch.getText().trim();
        if (kw.equalsIgnoreCase("t\u00ecm ki\u1ebfm h\u00f3a \u0111\u01a1n...")) kw = "";
        final String kwFinal = kw.toLowerCase();
        final String filterStatus = cbFilterTrangThai.getSelectedItem().toString();
        if (!dataLoaded) return;
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{ "...", "\u0110ang l\u1ecdc...", "", "", "", "", "", "" });
        SwingWorker<ArrayList<HoaDonDTO>, Void> worker = new SwingWorker<ArrayList<HoaDonDTO>, Void>() {
            @Override protected ArrayList<HoaDonDTO> doInBackground() {
                ArrayList<HoaDonDTO> all = bus.getDanhSachHoaDon();
                if (all == null) return new ArrayList<>();
                for (HoaDonDTO hd : all) {
                    int maNV = hd.getMaNV();
                    if (!cacheNV.containsKey(maNV)) cacheNV.put(maNV, fetchTenNhanVien(maNV));
                    Integer maKH = hd.getMaKhachHang();
                    if (maKH != null && !cacheKH.containsKey(maKH)) cacheKH.put(maKH, fetchTenKhachHang(maKH));
                }
                ArrayList<HoaDonDTO> result = new ArrayList<>();
                for (HoaDonDTO hd : all) {
                    if (!filterStatus.equals("T\u1ea5t c\u1ea3 tr\u1ea1ng th\u00e1i")) {
                        if (!formatTrangThai(hd.getTrangThai()).equals(filterStatus)) continue;
                    }
                    if (!kwFinal.isEmpty()) {
                        String tenNV = cacheNV.getOrDefault(hd.getMaNV(), "");
                        String tenKH = hd.getMaKhachHang() != null ? cacheKH.getOrDefault(hd.getMaKhachHang(), "") : "";
                        boolean match = String.valueOf(hd.getMaHoaDon()).contains(kwFinal) ||
                            (hd.getNgayLap() != null && hd.getNgayLap().format(DTF).toLowerCase().contains(kwFinal)) ||
                            tenNV.toLowerCase().contains(kwFinal) || tenKH.toLowerCase().contains(kwFinal) ||
                            (hd.getGhiChu() != null && hd.getGhiChu().toLowerCase().contains(kwFinal));
                        if (!match) continue;
                    }
                    result.add(hd);
                }
                return result;
            }
            @Override protected void done() {
                try { hoaDonList.clear(); hoaDonList.addAll(get()); refreshTable(); }
                catch (Exception ex) { tableModel.setRowCount(0); }
            }
        };
        worker.execute();
    }

    private HoaDonDTO getSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui l\u00f2ng ch\u1ecdn m\u1ed9t h\u00f3a \u0111\u01a1n tr\u00ean b\u1ea3ng!", "Th\u00f4ng b\u00e1o", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return hoaDonList.get(row);
    }

    // Kiểm tra quyền quản lý — dùng ở nhiều chỗ
    private boolean isQuanLy() {
        return "QuanLy".equals(SharedData.currentRole);
    }

    private void actionView() { HoaDonDTO hd = getSelected(); if (hd != null) showXemDialog(hd); }

    private void actionDelete() {
        if (!isQuanLy()) {
            JOptionPane.showMessageDialog(this,
                "B\u1ea1n kh\u00f4ng c\u00f3 quy\u1ec1n th\u1ef1c hi\u1ec7n ch\u1ee9c n\u0103ng n\u00e0y!\nCh\u1ec9 Qu\u1ea3n l\u00fd m\u1edbi \u0111\u01b0\u1ee3c ph\u00e9p h\u1ee7y h\u00f3a \u0111\u01a1n.",
                "Kh\u00f4ng c\u00f3 quy\u1ec1n", JOptionPane.WARNING_MESSAGE);
            return;
        }
        HoaDonDTO hd = getSelected(); if (hd == null) return;
        String tenKH = hd.getMaKhachHang() != null ? layTenKhachHang(hd.getMaKhachHang()) : "Kh\u00e1ch l\u1ebb";
        int ok = JOptionPane.showConfirmDialog(this,
            "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n H\u1ee6Y h\u00f3a \u0111\u01a1n #" + hd.getMaHoaDon() + "?\n"
            + "Kh\u00e1ch h\u00e0ng: " + tenKH + "\nT\u1ed5ng thanh to\u00e1n: "
            + (hd.getTongThanhToan() != null ? CurrencyFmt.format(hd.getTongThanhToan()) + " \u20ab" : "0 \u20ab")
            + "\n\nH\u00e0nh \u0111\u1ed9ng n\u00e0y s\u1ebd \u0111\u1ed5i tr\u1ea1ng th\u00e1i sang '\u0110\u00e3 h\u1ee7y'. Kh\u00f4ng th\u1ec3 ho\u00e0n t\u00e1c!",
            "X\u00e1c nh\u1eadn h\u1ee7y h\u00f3a \u0111\u01a1n", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            boolean result = bus.huyHoaDon(hd.getMaHoaDon());
            if (result) { loadData(); JOptionPane.showMessageDialog(this, "H\u00f3a \u0111\u01a1n #" + hd.getMaHoaDon() + " \u0111\u00e3 \u0111\u01b0\u1ee3c h\u1ee7y th\u00e0nh c\u00f4ng!", "Th\u00e0nh c\u00f4ng", JOptionPane.INFORMATION_MESSAGE); }
            else JOptionPane.showMessageDialog(this, "Kh\u00f4ng th\u1ec3 h\u1ee7y h\u00f3a \u0111\u01a1n! Vui l\u00f2ng ki\u1ec3m tra l\u1ea1i.", "L\u1ed7i", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // XEM CHI TIẾT + Inline Edit
    // =========================================================================
    private void showXemDialog(HoaDonDTO hd) {
        JDialog dlg = new JDialog(getParentFrame(), true);
        dlg.setUndecorated(true); dlg.setSize(860, 610); dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WHITE); root.setBorder(new LineBorder(BORDER_COLOR, 1));

        String tenNV = layTenNhanVien(hd.getMaNV());
        String tenKH = hd.getMaKhachHang() != null ? layTenKhachHang(hd.getMaKhachHang()) : "Kh\u00e1ch l\u1ebb";
        JPanel header = createDialogHeader(dlg, "Chi ti\u1ebft h\u00f3a \u0111\u01a1n #" + hd.getMaHoaDon());

        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(12, 30, 4, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(6, 10, 6, 10);

        String ngayLap    = hd.getNgayLap() != null ? hd.getNgayLap().format(DTF) : "\u2014";
        String tongTienHg = hd.getTongTienHang()     != null ? CurrencyFmt.format(hd.getTongTienHang())   + " \u20ab" : "0 \u20ab";
        String tienGiam   = hd.getTienGiamHang()     != null ? CurrencyFmt.format(hd.getTienGiamHang())   + " \u20ab" : "0 \u20ab";
        String tienVAT    = hd.getTienVAT()           != null ? CurrencyFmt.format(hd.getTienVAT())        + " \u20ab" : "0 \u20ab";
        String tongTT     = hd.getTongThanhToan()    != null ? CurrencyFmt.format(hd.getTongThanhToan())  + " \u20ab" : "0 \u20ab";
        String phanTram   = hd.getPhanTramGiamHang() != null ? hd.getPhanTramGiamHang().stripTrailingZeros().toPlainString() + "%" : "0%";
        String trangThai  = formatTrangThai(hd.getTrangThai());
        String ghiChu     = hd.getGhiChu() != null && !hd.getGhiChu().isEmpty() ? hd.getGhiChu() : "\u2014";

        // Label động cho "Sửa hóa đơn"
        boolean quanLy = isQuanLy();

        JLabel lblSuaValue = new JLabel("KH\u00d4NG");
        lblSuaValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSuaValue.setForeground(new Color(150, 80, 0));

        addRowLabel(form, gc, 0, "M\u00e3 h\u00f3a \u0111\u01a1n:",    "#" + hd.getMaHoaDon(), "Ng\u00e0y l\u1eadp:",         ngayLap);
        addRowLabel(form, gc, 1, "Nh\u00e2n vi\u00ean:",      tenNV,                  "Kh\u00e1ch h\u00e0ng:",       tenKH);
        addRowLabel(form, gc, 2, "T\u1ed5ng ti\u1ec1n h\u00e0ng:", tongTienHg,        "% Gi\u1ea3m gi\u00e1:",      phanTram);
        addRowLabel(form, gc, 3, "Ti\u1ec1n gi\u1ea3m:",      tienGiam,               "Ti\u1ec1n VAT (10%):",  tienVAT);
        addRowLabel(form, gc, 4, "T\u1ed5ng thanh to\u00e1n:", tongTT,               "Tr\u1ea1ng th\u00e1i:",       trangThai);
        addRowLabel(form, gc, 5, "Ghi ch\u00fa:",        ghiChu,                 null, null);

        // Dòng 6: Sửa hóa đơn — chỉ hiển thị với Quản lý
        if (quanLy) {
            gc.gridy = 6;
            gc.gridx = 0; gc.weightx = 0.12;
            JLabel lbSua = new JLabel("S\u1eeda h\u00f3a \u0111\u01a1n:");
            lbSua.setFont(FONT_LABEL); lbSua.setForeground(new Color(100, 130, 170));
            form.add(lbSua, gc);
            gc.gridx = 1; gc.weightx = 0.88; gc.gridwidth = 3;
            form.add(lblSuaValue, gc);
            gc.gridwidth = 1;
        }

        // Bảng chi tiết
        JLabel lblCTTitle = new JLabel(quanLy
            ? "  Chi ti\u1ebft s\u1ea3n ph\u1ea9m  (\u2192 ch\u1ecdn d\u00f2ng \u0111\u1ec3 s\u1eeda M\u00e3 SP / S\u1ed1 l\u01b0\u1ee3ng)"
            : "  Chi ti\u1ebft s\u1ea3n ph\u1ea9m");
        lblCTTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCTTitle.setForeground(PRIMARY);
        lblCTTitle.setBorder(BorderFactory.createEmptyBorder(4, 30, 4, 30));

        String[] ctCols = {"M\u00e3 SP", "T\u00ean s\u1ea3n ph\u1ea9m", "S\u1ed1 l\u01b0\u1ee3ng", "\u0110\u01a1n gi\u00e1", "Th\u00e0nh ti\u1ec1n"};
        DefaultTableModel ctModel = new DefaultTableModel(ctCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        loadChiTietHoaDon(hd.getMaHoaDon(), ctModel);

        JTable ctTable = new JTable(ctModel);
        ctTable.setFont(FONT_TABLE); ctTable.setRowHeight(36);
        ctTable.setShowVerticalLines(false); ctTable.setGridColor(BORDER_COLOR);
        ctTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ctTable.setSelectionBackground(ROW_SELECTED);
        ctTable.getTableHeader().setFont(FONT_HEADER);
        ctTable.getTableHeader().setBackground(TABLE_HEADER_BG);
        ctTable.getTableHeader().setForeground(WHITE);
        ctTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
        ctTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                                                                     boolean isSel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, isSel, foc, row, col);
                setBackground(TABLE_HEADER_BG);
                setForeground(WHITE);
                setFont(FONT_HEADER);
                setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return this;
            }
        });
        ctTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                                                                     boolean isSel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, isSel, foc, row, col);
                setFont(FONT_TABLE);
                setForeground(isSel ? TEXT_PRIMARY : new Color(30, 30, 30));
                setBackground(isSel ? ROW_SELECTED : (row % 2 == 0 ? WHITE : ROW_ALT));
                setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return this;
            }
        });
        JScrollPane ctScroll = new JScrollPane(ctTable);
        ctScroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));
        ctScroll.setPreferredSize(new Dimension(0, 150));
        ctScroll.getViewport().setBackground(WHITE);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(WHITE); footer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnSuaChiTiet = buildActionBtn("S\u1eeda d\u00f2ng n\u00e0y", ORANGE_BTN, ORANGE_DARK, WHITE);
        btnSuaChiTiet.setVisible(false);

        JButton btnExportExcel = buildSolidBtnWithIcon("Export Excel", "excel", new Color(33, 115, 70), new Color(20, 80, 45));
        btnExportExcel.addActionListener(ev -> {
            java.util.List<ChiTietHoaDonDTO> ctList = loadChiTietHoaDonList(hd.getMaHoaDon());
            HoaDonExcelUtils.exportChiTiet(dlg, hd, ctList);
        });

        JButton btnExportPdf = buildSolidBtnWithIcon("Xu\u1ea5t PDF", "pdf", new Color(183, 28, 28), new Color(130, 15, 15));
        btnExportPdf.addActionListener(ev -> {
            java.util.List<ChiTietHoaDonDTO> ctList = loadChiTietHoaDonList(hd.getMaHoaDon());
            HoaDonPDFUtils.exportChiTiet(dlg, hd, ctList);
        });

        JButton btnClose = buildActionBtn("\u0110\u00f3ng", GRAY_BTN, GRAY_DARK, WHITE);
        btnClose.addActionListener(e -> dlg.dispose());

        footer.add(btnSuaChiTiet);
        footer.add(btnExportExcel);
        footer.add(btnExportPdf);
        footer.add(btnClose);

        // Chọn dòng → hiện nút Sửa (chỉ với Quản lý)
        ctTable.getSelectionModel().addListSelectionListener(e2 -> {
            if (!e2.getValueIsAdjusting()) {
                btnSuaChiTiet.setVisible(quanLy && ctTable.getSelectedRow() != -1);
                footer.revalidate(); footer.repaint();
            }
        });

        // Hành động Sửa dòng
        btnSuaChiTiet.addActionListener(ev -> {
            int selRow = ctTable.getSelectedRow(); if (selRow == -1) return;
            int maSPCu = 0;
            try { maSPCu = Integer.parseInt(ctModel.getValueAt(selRow, 0).toString()); } catch (Exception ignore) {}
            String tenSPCu = ctModel.getValueAt(selRow, 1).toString();
            int soLuongCu = 0;
            try { soLuongCu = Integer.parseInt(ctModel.getValueAt(selRow, 2).toString()); } catch (Exception ignore) {}
            showSuaChiTietDialog(dlg, hd.getMaHoaDon(), maSPCu, tenSPCu, soLuongCu, ctModel, ctTable, lblSuaValue);
        });

        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setBackground(WHITE);
        JPanel bottomSection = new JPanel(new BorderLayout()); bottomSection.setBackground(WHITE);
        bottomSection.add(lblCTTitle, BorderLayout.NORTH);
        bottomSection.add(ctScroll,   BorderLayout.CENTER);
        mainContent.add(form,          BorderLayout.NORTH);
        mainContent.add(bottomSection, BorderLayout.CENTER);

        root.add(header,      BorderLayout.NORTH);
        root.add(mainContent, BorderLayout.CENTER);
        root.add(footer,      BorderLayout.SOUTH);
        dlg.setContentPane(root); dlg.setVisible(true);
    }

    // =========================================================================
    // DIALOG SỬA CHI TIẾT
    // =========================================================================
    private void showSuaChiTietDialog(JDialog parentDlg, int maHoaDon,
                                       int maSPCu, String tenSPCu, int soLuongCu,
                                       DefaultTableModel ctModel, JTable ctTable, JLabel lblSuaValue) {
        JDialog editDlg = new JDialog(parentDlg, true);
        editDlg.setUndecorated(true); editDlg.setSize(520, 380); editDlg.setLocationRelativeTo(parentDlg);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WHITE); root.setBorder(new LineBorder(BORDER_COLOR, 1));

        JPanel hdr = createDialogHeader(editDlg, "S\u1eeda chi ti\u1ebft s\u1ea3n ph\u1ea9m \u2014 H\u00f3a \u0111\u01a1n #" + maHoaDon);

        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(18, 30, 6, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(8, 8, 8, 8);

        // Row 0: Mã SP hiện tại (readonly)
        gc.gridy = 0; gc.gridx = 0; gc.weightx = 0.3;
        JLabel l1 = new JLabel("M\u00e3 SP hi\u1ec7n t\u1ea1i:"); l1.setFont(FONT_LABEL); l1.setForeground(new Color(100,130,170)); form.add(l1, gc);
        gc.gridx = 1; gc.weightx = 0.7;
        form.add(makeReadonlyField(maSPCu + "  (" + tenSPCu + ")"), gc);

        // Row 1: Mã SP mới (có live lookup)
        gc.gridy = 1; gc.gridx = 0; gc.weightx = 0.3;
        JLabel l2 = new JLabel("M\u00e3 SP m\u1edbi:"); l2.setFont(FONT_LABEL); l2.setForeground(PRIMARY); form.add(l2, gc);
        gc.gridx = 1; gc.weightx = 0.7;
        JTextField tfMaSPMoi = makeField(String.valueOf(maSPCu));
        tfMaSPMoi.setToolTipText("Nh\u1eadp M\u00e3 SP m\u1edbi c\u00f3 trong CSDL"); form.add(tfMaSPMoi, gc);

        // Row 2: Live info — Tên SP tra được
        gc.gridy = 2; gc.gridx = 0; gc.weightx = 0.3; gc.insets = new Insets(0, 8, 4, 8);
        JLabel lTenSP = new JLabel("T\u00ean s\u1ea3n ph\u1ea9m:"); lTenSP.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lTenSP.setForeground(new Color(130,150,180)); form.add(lTenSP, gc);
        gc.gridx = 1; gc.weightx = 0.7;
        JLabel lblTenSPLive = new JLabel(tenSPCu);
        lblTenSPLive.setFont(new Font("Segoe UI", Font.BOLD, 12)); lblTenSPLive.setForeground(new Color(21, 101, 192));
        lblTenSPLive.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4)); form.add(lblTenSPLive, gc);

        // Row 3: Live info — Tồn kho
        gc.gridy = 3; gc.gridx = 0; gc.weightx = 0.3;
        JLabel lTon = new JLabel("T\u1ed3n kho hi\u1ec7n t\u1ea1i:"); lTon.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lTon.setForeground(new Color(130,150,180)); form.add(lTon, gc);
        gc.gridx = 1; gc.weightx = 0.7;
        // tồn kho hiệu dụng ban đầu = tonKho + soLuongCu (đang chiếm)
        int[] cachedTonKho = { -1 }; // -1 = chưa load
        JLabel lblTonKhoLive = new JLabel("\u2014");
        lblTonKhoLive.setFont(new Font("Segoe UI", Font.BOLD, 12)); lblTonKhoLive.setForeground(SUCCESS);
        lblTonKhoLive.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4)); form.add(lblTonKhoLive, gc);
        gc.insets = new Insets(8, 8, 8, 8);

        // Load thông tin SP ban đầu (mã cũ)
        try (Connection cnInit = DBConnection.getConnection();
             PreparedStatement psInit = cnInit.prepareStatement(
                     "SELECT SoLuongTon FROM SANPHAM WHERE MaSP = ?")) {
            psInit.setInt(1, maSPCu);
            try (ResultSet rsInit = psInit.executeQuery()) {
                if (rsInit.next()) {
                    int ton = rsInit.getInt("SoLuongTon");
                    cachedTonKho[0] = ton;
                    // Tồn kho hiệu dụng: cộng lại SL đang dùng trong HD này
                    int tonHD = ton + soLuongCu;
                    lblTonKhoLive.setText(tonHD + " s\u1ea3n ph\u1ea9m");
                    lblTonKhoLive.setForeground(tonHD > 0 ? SUCCESS : RED_BTN);
                }
            }
        } catch (Exception ignored) {}

        // Row 4: Số lượng mới
        gc.gridy = 4; gc.gridx = 0; gc.weightx = 0.3;
        JLabel l3 = new JLabel("S\u1ed1 l\u01b0\u1ee3ng m\u1edbi:"); l3.setFont(FONT_LABEL); l3.setForeground(PRIMARY); form.add(l3, gc);
        gc.gridx = 1; gc.weightx = 0.7;
        JTextField tfSoLuong = makeField(String.valueOf(soLuongCu));
        tfSoLuong.setToolTipText("Nh\u1eadp s\u1ed1 l\u01b0\u1ee3ng m\u1edbi (> 0)"); form.add(tfSoLuong, gc);

        // DocumentListener: tra DB ngay khi nhập Mã SP mới
        final int[] liveTonKho = { cachedTonKho[0] + soLuongCu }; // hiệu dụng
        final boolean[] isSameSP = { true };
        javax.swing.Timer[] debounce = { null };
        tfMaSPMoi.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void onChanged() {
                String raw = tfMaSPMoi.getText().trim();
                int maTest;
                try { maTest = Integer.parseInt(raw); if (maTest <= 0) throw new NumberFormatException(); }
                catch (NumberFormatException ex) {
                    lblTenSPLive.setText("Nh\u1eadp m\u00e3 SP h\u1ee3p l\u1ec7...");
                    lblTenSPLive.setForeground(new Color(150,150,150));
                    lblTonKhoLive.setText("\u2014"); lblTonKhoLive.setForeground(GRAY_BTN); return;
                }
                lblTenSPLive.setText("\u0110ang t\u00ecm...");
                lblTenSPLive.setForeground(new Color(150,150,150));
                lblTonKhoLive.setText("..."); lblTonKhoLive.setForeground(GRAY_BTN);
                if (debounce[0] != null) debounce[0].stop();
                final int mLookup = maTest;
                debounce[0] = new javax.swing.Timer(250, ev -> {
                    new SwingWorker<String[], Void>() {
                        @Override protected String[] doInBackground() {
                            try (Connection cn2 = DBConnection.getConnection();
                                 PreparedStatement ps2 = cn2.prepareStatement(
                                         "SELECT TenSP, SoLuongTon FROM SANPHAM WHERE MaSP = ?")) {
                                ps2.setInt(1, mLookup);
                                try (ResultSet rs2 = ps2.executeQuery()) {
                                    if (rs2.next()) {
                                        String ten = rs2.getString("TenSP");
                                        int ton    = rs2.getInt("SoLuongTon");
                                        // Tồn kho hiệu dụng: nếu cùng SP thì cộng lại SL đang chiếm
                                        int tonHD  = ton + (mLookup == maSPCu ? soLuongCu : 0);
                                        return new String[]{ ten, String.valueOf(tonHD), "ok" };
                                    }
                                }
                            } catch (Exception ex) { return new String[]{ null, null, "err:" + ex.getMessage() }; }
                            return new String[]{ null, null, "notfound" };
                        }
                        @Override protected void done() {
                            try {
                                String[] res = get();
                                if ("ok".equals(res[2])) {
                                    lblTenSPLive.setText(res[0]);
                                    lblTenSPLive.setForeground(PRIMARY);
                                    int ton = Integer.parseInt(res[1]);
                                    liveTonKho[0] = ton;
                                    isSameSP[0]   = (mLookup == maSPCu);
                                    lblTonKhoLive.setText(ton + " s\u1ea3n ph\u1ea9m");
                                    lblTonKhoLive.setForeground(ton > 0 ? SUCCESS : RED_BTN);
                                } else if ("notfound".equals(res[2])) {
                                    lblTenSPLive.setText("Kh\u00f4ng t\u00ecm th\u1ea5y m\u00e3 SP " + mLookup);
                                    lblTenSPLive.setForeground(RED_BTN);
                                    lblTonKhoLive.setText("\u2014"); lblTonKhoLive.setForeground(GRAY_BTN);
                                    liveTonKho[0] = -1;
                                } else {
                                    lblTenSPLive.setText("L\u1ed7i CSDL");
                                    lblTenSPLive.setForeground(RED_BTN);
                                    lblTonKhoLive.setText("\u2014"); lblTonKhoLive.setForeground(GRAY_BTN);
                                    liveTonKho[0] = -1;
                                }
                            } catch (Exception ignored) {}
                        }
                    }.execute();
                });
                debounce[0].setRepeats(false); debounce[0].start();
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { onChanged(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { onChanged(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onChanged(); }
        });

        JLabel lblMsg = new JLabel(" ");
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMsg.setBorder(BorderFactory.createEmptyBorder(2, 30, 6, 30));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); footer.setBackground(WHITE);
        JButton btnLuu = buildActionBtn("L\u01b0u", SUCCESS, SUCCESS_DARK, WHITE);
        JButton btnHuy = buildActionBtn("H\u1ee7y", GRAY_BTN, GRAY_DARK, WHITE);
        btnHuy.addActionListener(e -> editDlg.dispose());

        btnLuu.addActionListener(e -> {
            // Validate Mã SP
            int maSPMoi;
            try {
                maSPMoi = Integer.parseInt(tfMaSPMoi.getText().trim());
                if (maSPMoi <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                lblMsg.setText("L\u1ed7i: M\u00e3 SP ph\u1ea3i l\u00e0 s\u1ed1 nguy\u00ean d\u01b0\u01a1ng.");
                lblMsg.setForeground(RED_BTN); return;
            }
            // Validate Số lượng
            int soLuongMoi;
            try {
                soLuongMoi = Integer.parseInt(tfSoLuong.getText().trim());
                if (soLuongMoi <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                lblMsg.setText("L\u1ed7i: S\u1ed1 l\u01b0\u1ee3ng ph\u1ea3i l\u00e0 s\u1ed1 nguy\u00ean d\u01b0\u01a1ng.");
                lblMsg.setForeground(RED_BTN); return;
            }

            // Kiểm tra SP tồn tại và tồn kho
            String[] tenSPArr  = { null };
            BigDecimal[] donGia = { null };
            int[] tonKho        = { -1 };
            try (Connection cn = DBConnection.getConnection();
                 PreparedStatement ps = cn.prepareStatement(
                         "SELECT TenSP, Gia, SoLuongTon FROM SANPHAM WHERE MaSP = ?")) {
                ps.setInt(1, maSPMoi);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tenSPArr[0] = rs.getString("TenSP");
                        donGia[0]   = rs.getBigDecimal("Gia");
                        tonKho[0]   = rs.getInt("SoLuongTon");
                    }
                }
            } catch (Exception ex) {
                lblMsg.setText("S\u1eeda th\u1ea5t b\u1ea1i: L\u1ed7i k\u1ebft n\u1ed1i CSDL \u2014 " + ex.getMessage());
                lblMsg.setForeground(RED_BTN); return;
            }

            if (tenSPArr[0] == null) {
                lblMsg.setText("L\u1ed7i: M\u00e3 SP " + maSPMoi + " kh\u00f4ng t\u1ed3n t\u1ea1i trong CSDL.");
                lblMsg.setForeground(RED_BTN); return;
            }

            // Tồn kho hiệu dụng (nếu cùng SP thì hoàn trả sl cũ)
            int tonKhoHD = tonKho[0] + (maSPMoi == maSPCu ? soLuongCu : 0);
            if (soLuongMoi > tonKhoHD) {
                lblMsg.setText("L\u1ed7i: T\u1ed3n kho ch\u1ec9 c\u00f2n " + tonKhoHD + " (y\u00eau c\u1ea7u: " + soLuongMoi + ").");
                lblMsg.setForeground(RED_BTN); return;
            }

            final int fMaSPMoi    = maSPMoi;
            final int fSL         = soLuongMoi;
            final String fTenSP   = tenSPArr[0];
            final BigDecimal fGia  = donGia[0] != null ? donGia[0] : BigDecimal.ZERO;

            try (Connection cn = DBConnection.getConnection()) {
                cn.setAutoCommit(false);
                try {
                    // 1. Cập nhật CHITIETHOADON
                    try (PreparedStatement ps = cn.prepareStatement(
                            "UPDATE CHITIETHOADON SET MaSP=?, SoLuong=?, DonGia=? WHERE MaHoaDon=? AND MaSP=?")) {
                        ps.setInt(1, fMaSPMoi); ps.setInt(2, fSL); ps.setBigDecimal(3, fGia);
                        ps.setInt(4, maHoaDon); ps.setInt(5, maSPCu);
                        int updated = ps.executeUpdate();
                        if (updated == 0) throw new Exception("Kh\u00f4ng t\u00ecm th\u1ea5y d\u00f2ng c\u1ea7n s\u1eeda.");
                    }
                    // 2. Điều chỉnh tồn kho
                    if (fMaSPMoi != maSPCu) {
                        // Hoàn trả SP cũ
                        try (PreparedStatement ps = cn.prepareStatement(
                                "UPDATE SANPHAM SET SoLuongTon=SoLuongTon+? WHERE MaSP=?")) {
                            ps.setInt(1, soLuongCu); ps.setInt(2, maSPCu); ps.executeUpdate();
                        }
                        // Trừ SP mới
                        try (PreparedStatement ps = cn.prepareStatement(
                                "UPDATE SANPHAM SET SoLuongTon=SoLuongTon-? WHERE MaSP=?")) {
                            ps.setInt(1, fSL); ps.setInt(2, fMaSPMoi); ps.executeUpdate();
                        }
                    } else {
                        // Cùng SP — chỉ điều chỉnh delta số lượng
                        int delta = fSL - soLuongCu; // dương = cần thêm, âm = hoàn trả
                        if (delta > 0) {
                            // Dùng thêm → trừ tồn kho
                            try (PreparedStatement ps = cn.prepareStatement(
                                    "UPDATE SANPHAM SET SoLuongTon=SoLuongTon-? WHERE MaSP=?")) {
                                ps.setInt(1, delta); ps.setInt(2, fMaSPMoi); ps.executeUpdate();
                            }
                        } else if (delta < 0) {
                            // Dùng ít hơn → hoàn trả phần dư
                            try (PreparedStatement ps = cn.prepareStatement(
                                    "UPDATE SANPHAM SET SoLuongTon=SoLuongTon+? WHERE MaSP=?")) {
                                ps.setInt(1, -delta); ps.setInt(2, fMaSPMoi); ps.executeUpdate();
                            }
                        }
                        // delta == 0: số lượng không đổi, không cần update tồn kho
                    }
                    cn.commit();

                    // 3. Cập nhật bảng hiển thị
                    int selRow = ctTable.getSelectedRow();
                    BigDecimal thanhTien = fGia.multiply(new BigDecimal(fSL));
                    ctModel.setValueAt(fMaSPMoi, selRow, 0);
                    ctModel.setValueAt(fTenSP,   selRow, 1);
                    ctModel.setValueAt(fSL,       selRow, 2);
                    ctModel.setValueAt(CurrencyFmt.format(fGia)       + " \u20ab", selRow, 3);
                    ctModel.setValueAt(CurrencyFmt.format(thanhTien)  + " \u20ab", selRow, 4);

                    // 4. Cập nhật label "Sửa hóa đơn"
                    lblSuaValue.setText(LocalDateTime.now().format(DTF_SEC));
                    lblSuaValue.setForeground(SUCCESS);

                    // 5. Làm mới bảng chính
                    loadData();

                    editDlg.dispose();
                    JOptionPane.showMessageDialog(parentDlg,
                        "S\u1eeda th\u00e0nh c\u00f4ng!\nS\u1ea3n ph\u1ea9m: " + fTenSP + "\nS\u1ed1 l\u01b0\u1ee3ng: " + fSL,
                        "Th\u00e0nh c\u00f4ng", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    cn.rollback();
                    lblMsg.setText("S\u1eeda th\u1ea5t b\u1ea1i: " + ex.getMessage());
                    lblMsg.setForeground(RED_BTN);
                }
            } catch (Exception ex) {
                lblMsg.setText("S\u1eeda th\u1ea5t b\u1ea1i: L\u1ed7i k\u1ebft n\u1ed1i CSDL \u2014 " + ex.getMessage());
                lblMsg.setForeground(RED_BTN);
            }
        });

        footer.add(btnHuy); footer.add(btnLuu);

        JPanel center = new JPanel(new BorderLayout()); center.setBackground(WHITE);
        center.add(form, BorderLayout.CENTER); center.add(lblMsg, BorderLayout.SOUTH);

        root.add(hdr, BorderLayout.NORTH); root.add(center, BorderLayout.CENTER); root.add(footer, BorderLayout.SOUTH);
        editDlg.setContentPane(root); editDlg.setVisible(true);
    }

    // =========================================================================
    // DB HELPERS
    // =========================================================================
    private String fetchTenNhanVien(int maNV) {
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT tenNV FROM NhanVien WHERE maNV = ?")) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { String ten = rs.getString("tenNV"); return ten != null ? ten : "NV#" + maNV; }
            }
        } catch (Exception e) { /* ignore */ }
        return "NV#" + maNV;
    }

    private String fetchTenKhachHang(int maKH) {
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT TenKhachHang FROM KHACHHANG WHERE MaKhachHang = ?")) {
            ps.setInt(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { String ten = rs.getString("TenKhachHang"); return ten != null && !ten.isEmpty() ? ten : "KH#" + maKH; }
            }
        } catch (Exception e) { /* ignore */ }
        return "KH#" + maKH;
    }

    private String layTenNhanVien(int maNV) { return cacheNV.computeIfAbsent(maNV, this::fetchTenNhanVien); }
    private String layTenKhachHang(int maKH) { return cacheKH.computeIfAbsent(maKH, this::fetchTenKhachHang); }

    private void loadChiTietHoaDon(int maHoaDon, DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT ct.MaSP, sp.TenSP, ct.SoLuong, ct.DonGia, ct.SoLuong * ct.DonGia AS ThanhTien " +
                "FROM CHITIETHOADON ct JOIN SANPHAM sp ON ct.MaSP = sp.MaSP WHERE ct.MaHoaDon = ?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("MaSP"), rs.getString("TenSP"), rs.getInt("SoLuong"),
                        CurrencyFmt.format(rs.getBigDecimal("DonGia"))    + " \u20ab",
                        CurrencyFmt.format(rs.getBigDecimal("ThanhTien")) + " \u20ab"
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private java.util.List<ChiTietHoaDonDTO> loadChiTietHoaDonList(int maHoaDon) {
        java.util.List<ChiTietHoaDonDTO> list = new java.util.ArrayList<>();
        String sql = "SELECT ct.MaSP, sp.TenSP, ct.SoLuong, ct.DonGia, ct.SoLuong * ct.DonGia AS ThanhTien " +
                "FROM CHITIETHOADON ct JOIN SANPHAM sp ON ct.MaSP = sp.MaSP WHERE ct.MaHoaDon = ?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietHoaDonDTO ct = new ChiTietHoaDonDTO();
                    ct.setMaHoaDon(maHoaDon); ct.setMaSP(rs.getInt("MaSP")); ct.setTenSP(rs.getString("TenSP"));
                    ct.setSoLuong(rs.getInt("SoLuong")); ct.setDonGia(rs.getBigDecimal("DonGia"));
                    ct.setThanhTien(rs.getBigDecimal("ThanhTien")); list.add(ct);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private String formatTrangThai(String tt) {
        if (tt == null) return "\u2014";
        switch (tt) {
            case "HoanThanh": return "Ho\u00e0n th\u00e0nh";
            case "Huy":       return "\u0110\u00e3 h\u1ee7y";
            case "ChoXuLy":   return "Ch\u1edd x\u1eed l\u00fd";
            default:          return tt;
        }
    }

    // =========================================================================
    // UI HELPERS
    // =========================================================================
    private JTextField makeField(String text) {
        if (text == null) text = "";
        JTextField tf = new JTextField(text); tf.setFont(FONT_NORMAL); tf.setForeground(TEXT_PRIMARY);
        tf.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    private JTextField makeReadonlyField(String text) {
        JTextField tf = makeField(text); tf.setEditable(false);
        tf.setBackground(new Color(245, 248, 255)); tf.setForeground(new Color(100, 120, 150)); return tf;
    }

    private JComboBox<String> makeStyledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items); cb.setFont(FONT_NORMAL); cb.setBackground(WHITE);
        cb.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return cb;
    }

    private void addRowLabel(JPanel p, GridBagConstraints gc, int row,
                             String l1, String v1, String l2, String v2) {
        gc.gridy = row;
        gc.gridx = 0; gc.weightx = 0.12;
        JLabel lb1 = new JLabel(l1); lb1.setFont(FONT_LABEL); lb1.setForeground(new Color(100, 130, 170)); p.add(lb1, gc);
        gc.gridx = 1; gc.weightx = 0.38;
        JLabel vl1 = new JLabel(v1 != null && !v1.isEmpty() ? v1 : "\u2014");
        vl1.setFont(FONT_NORMAL); vl1.setForeground(TEXT_PRIMARY); p.add(vl1, gc);
        if (l2 != null) {
            gc.gridx = 2; gc.weightx = 0.12;
            JLabel lb2 = new JLabel(l2); lb2.setFont(FONT_LABEL); lb2.setForeground(new Color(100, 130, 170)); p.add(lb2, gc);
            gc.gridx = 3; gc.weightx = 0.38;
            JLabel vl2 = new JLabel(v2 != null && !v2.isEmpty() ? v2 : "\u2014");
            vl2.setFont(FONT_NORMAL); vl2.setForeground(TEXT_PRIMARY); p.add(vl2, gc);
        }
    }

    private JPanel createDialogHeader(JDialog dlg, String title) {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY));
                g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose();
            }
        };
        header.setOpaque(false); header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15)); lblTitle.setForeground(WHITE);
        JLabel lblClose = new JLabel("  X  ");
        lblClose.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblClose.setForeground(new Color(200, 230, 255));
        lblClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClose.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dlg.dispose(); }
            @Override public void mouseEntered(MouseEvent e) { lblClose.setForeground(WHITE); }
            @Override public void mouseExited(MouseEvent e)  { lblClose.setForeground(new Color(200, 230, 255)); }
        });
        header.add(lblTitle, BorderLayout.WEST); header.add(lblClose, BorderLayout.EAST);
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
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18)); return btn;
    }

    private JButton buildOutlineBtn(String text, boolean withRefreshIcon) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) { g2.setColor(new Color(255,255,255,40)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                g2.setColor(WHITE); g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,8,8);
                if (withRefreshIcon) {
                    int ix=10, iy=getHeight()/2-6, ir=5; g2.setColor(WHITE);
                    g2.setStroke(new BasicStroke(1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                    g2.drawArc(ix-ir,iy,ir*2,ir*2,60,-300);
                    int ax=ix+ir-1,ay=iy+2; int[] px={ax-3,ax+1,ax+1}; int[] py={ay,ay-4,ay};
                    g2.fillPolygon(px,py,3);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(WHITE); btn.setFont(FONT_LABEL);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(withRefreshIcon ? BorderFactory.createEmptyBorder(8,28,8,18) : BorderFactory.createEmptyBorder(8,18,8,18));
        return btn;
    }

    private JButton buildOutlineBtnWithIcon(String text, String iconType) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) { g2.setColor(new Color(255,255,255,40)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                g2.setColor(WHITE); g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,8,8);
                drawBtnIcon(g2, iconType, 10, getHeight()/2); g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(WHITE); btn.setFont(FONT_LABEL);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.setBorder(BorderFactory.createEmptyBorder(8,34,8,16)); return btn;
    }

    private JButton buildSolidBtnWithIcon(String text, String iconType, Color bg, Color hover) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                drawBtnIcon(g2, iconType, 12, getHeight()/2); g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(WHITE); btn.setFont(FONT_LABEL);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.setBorder(BorderFactory.createEmptyBorder(8,36,8,16)); return btn;
    }

    private void drawBtnIcon(Graphics2D g2, String iconType, int x, int cy) {
        if ("excel".equals(iconType)) {
            int y=cy-8, w=15, h=16;
            g2.setColor(new Color(255,255,255,200)); g2.fillRoundRect(x,y,w,h,3,3);
            g2.setColor(WHITE); g2.setStroke(new BasicStroke(1.3f)); g2.drawRoundRect(x,y,w,h,3,3);
            g2.setColor(new Color(80,200,120)); g2.fillRoundRect(x+1,y+1,w-2,4,2,2);
            g2.setColor(new Color(21,101,192)); g2.setStroke(new BasicStroke(0.9f));
            int mx=x+w/2; g2.drawLine(mx,y+5,mx,y+h-1);
            g2.drawLine(x+1,y+h/3+2,x+w-1,y+h/3+2); g2.drawLine(x+1,y+h*2/3+1,x+w-1,y+h*2/3+1);
        } else if ("pdf".equals(iconType)) {
            int y=cy-9, w=13, h=17, fold=5;
            int[] px={x,x+w-fold,x+w,x+w,x}; int[] py={y,y,y+fold,y+h,y+h};
            g2.setColor(new Color(255,255,255,200)); g2.fillPolygon(px,py,5);
            g2.setColor(new Color(160,210,255,180)); g2.fillPolygon(new int[]{x+w-fold,x+w-fold,x+w},new int[]{y,y+fold,y+fold},3);
            g2.setColor(WHITE); g2.setStroke(new BasicStroke(1.3f)); g2.drawPolygon(px,py,5);
            g2.setStroke(new BasicStroke(0.9f)); g2.drawLine(x+w-fold,y,x+w-fold,y+fold); g2.drawLine(x+w-fold,y+fold,x+w,y+fold);
            g2.setColor(new Color(255,255,255,160)); g2.setStroke(new BasicStroke(1.0f));
            g2.drawLine(x+2,y+7,x+w-2,y+7); g2.drawLine(x+2,y+10,x+w-2,y+10); g2.drawLine(x+2,y+13,x+w-4,y+13);
            g2.setColor(new Color(255,80,80)); g2.setStroke(new BasicStroke(2.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g2.drawLine(x+1,y+2,x+1,y+h-2);
        }
    }
}
