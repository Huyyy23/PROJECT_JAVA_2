package GUI;

import BUS.LoaiSanPhamBUS;
import BUS.NhaCungCapBUS;
import BUS.SanPhamBUS;
import BUS.ThongSoKyThuatBUS;
import DTO.LoaiSanPhamDTO;
import DTO.NhaCungCapDTO;
import DTO.SanPhamDTO;
import DTO.ThongSoKyThuatDTO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SanPhamPanel extends JPanel {

    private static final Color PRIMARY       = new Color(21, 101, 192);
    private static final Color PRIMARY_DARK  = new Color(10, 60, 130);
    private static final Color ACCENT        = new Color(0, 188, 212);
    private static final Color CONTENT_BG    = new Color(236, 242, 250);
    private static final Color WHITE         = Color.WHITE;
    private static final Color ROW_ALT       = new Color(245, 250, 255);
    private static final Color TABLE_HEADER  = new Color(21, 101, 192);
    private static final Color SUCCESS       = new Color(46, 125, 50);
    private static final Color DANGER        = new Color(198, 40, 40);
    private static final Color WARNING_COLOR = new Color(230, 120, 0);
    private static final Color LAPTOP_SPEC_BG = new Color(232, 245, 233);
    private static final Color LAPTOP_SPEC_BORDER = new Color(46, 125, 50);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_STAT   = new Font("Segoe UI", Font.BOLD, 22);

    private final SanPhamBUS         sanPhamBUS = new SanPhamBUS();
    private final LoaiSanPhamBUS     loaiBUS    = new LoaiSanPhamBUS();
    private final NhaCungCapBUS      nccBUS     = new NhaCungCapBUS();
    private final ThongSoKyThuatBUS  tsktBUS    = new ThongSoKyThuatBUS();

    private ArrayList<SanPhamDTO>     allProducts  = new ArrayList<>();
    private ArrayList<SanPhamDTO>     showProducts = new ArrayList<>();
    private ArrayList<LoaiSanPhamDTO> dsLoai       = new ArrayList<>();

    private JTextField         txtSearch;
    private JTable             tableProduct;
    private DefaultTableModel  modelProduct;
    private JLabel             lblTongSP, lblConHang, lblSapHet, lblHetHang, lblRecordCount;
    private JComboBox<String>  cbStatus;
    private JComboBox<String>  cbSubLoai;
    private JComboBox<String>  cbLaptopBrand;
    private ArrayList<JButton> groupBtns = new ArrayList<>();
    private java.util.Map<Integer, String> loaiMap = new java.util.HashMap<>();

    // Thương hiệu theo loại: key=MaLoai, value=Set<ThuongHieu>
    private java.util.Map<Integer, java.util.Set<String>> brandByLoai = new java.util.HashMap<>();
    // Thương hiệu theo NCC: key=MaNCC, value=Set<ThuongHieu> — build sẵn trong background thread
    private java.util.Map<Integer, java.util.Set<String>> nccBrandsCache = new java.util.LinkedHashMap<>();

    private String filterLaptopBrand = "Tat_ca";
    private String filterStatus      = "Tat_ca";
    private String filterGroup       = "Tat_ca";
    private int    filterSubLoai     = -1;
    private String activeGiaKey      = "Tat_ca";

    private static final int   MALOAI_LAPTOP  = 1;
    private static final int[] MALOAI_PHUKIEN = {2, 3, 4, 5};

    private static final String[][] GIA_RANGES = {
        {"Tat_ca",  "Tất cả",        "0",        "999999999"},
        {"duoi5",   "Dưới 5 triệu",  "0",        "5000000"},
        {"5den15",  "5 - 15 triệu",  "5000000",  "15000000"},
        {"15den30", "15 - 30 triệu", "15000000", "30000000"},
        {"tren30",  "Trên 30 triệu", "30000000", "999999999"},
    };

    private JPanel            subLoaiRow = null;
    private java.util.List<JButton> allGiaBtns = new ArrayList<>();
    
    private static final String[] STATUS_KEYS    = {"Tat_ca","Còn hàng","Sắp hết hàng","Hết hàng"};
    private static final String[] STATUS_DISPLAY = {"Tất cả","Còn hàng","Sắp hết hàng","Hết hàng"};

    public SanPhamPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(createTitlePanel(),  BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        this.addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { loadData(); }
        });
    }

    private boolean isRebuildingSubLoai = false;
    private void rebuildSubLoaiCombo() {
        if (cbSubLoai == null) return;
        isRebuildingSubLoai = true;
        try {
            String[] subItems = new String[MALOAI_PHUKIEN.length + 1];
            subItems[0] = "Tất cả phụ kiện";
            for (int i = 0; i < MALOAI_PHUKIEN.length; i++)
                subItems[i + 1] = loaiMap.getOrDefault(MALOAI_PHUKIEN[i], "Loại " + MALOAI_PHUKIEN[i]);
            cbSubLoai.removeAllItems();
            for (String s : subItems) cbSubLoai.addItem(s);
            cbSubLoai.setSelectedIndex(0);
        } finally { isRebuildingSubLoai = false; }
    }

    private void loadData() {
        new javax.swing.SwingWorker<Void, Void>() {
            private ArrayList<SanPhamDTO>     tmpProducts;
            private ArrayList<LoaiSanPhamDTO> tmpLoai;
            private java.util.Map<Integer, java.util.Set<String>> tmpNccBrands;
            @Override protected Void doInBackground() {
                tmpLoai     = loaiBUS.getAll();
                tmpProducts = sanPhamBUS.getDanhSachSanPham();

                // Build nccBrandsCache trên background thread — tránh block EDT khi mở form
                // getDanhSachNccCuaSP mỗi SP 1 query → chạy ở đây để form mở tức thì
                ArrayList<NhaCungCapDTO> dsNccAll = nccBUS.getDanhSachHoatDong();
                tmpNccBrands = new java.util.LinkedHashMap<>();
                for (NhaCungCapDTO nccItem : dsNccAll)
                    tmpNccBrands.put(nccItem.getMaNCC(),
                        new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER));
                for (SanPhamDTO spItem : tmpProducts) {
                    if (spItem.getThuongHieu() == null || spItem.getThuongHieu().trim().isEmpty()) continue;
                    ArrayList<Integer> nccIds = nccBUS.getDanhSachNccCuaSP(spItem.getMaSP());
                    for (int nId : nccIds) {
                        if (tmpNccBrands.containsKey(nId))
                            tmpNccBrands.get(nId).add(spItem.getThuongHieu().trim());
                    }
                }
                return null;
            }
            @Override protected void done() {
                try { get(); } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SanPhamPanel.this,
                        "Lỗi tải dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                allProducts   = tmpProducts;
                dsLoai        = tmpLoai;
                nccBrandsCache = tmpNccBrands;  // gán cache đã build sẵn
                loaiMap.clear();
                brandByLoai.clear();
                for (LoaiSanPhamDTO l : dsLoai) loaiMap.put(l.getMaLoai(), l.getTenLoai());
                for (SanPhamDTO sp : allProducts) {
                    if (sp.getThuongHieu() == null || sp.getThuongHieu().trim().isEmpty()) continue;
                    brandByLoai
                        .computeIfAbsent(sp.getMaLoai(), k -> new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER))
                        .add(sp.getThuongHieu().trim());
                }
                rebuildSubLoaiCombo();
                loadDynamicBrands();
                applyFilter();
            }
        }.execute();
    }

    private boolean isLoadingBrands = false;
    private void loadDynamicBrands() {
        if (cbLaptopBrand == null) return;
        isLoadingBrands = true;
        try {
            java.util.Set<String> uniqueBrands = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            for (SanPhamDTO sp : allProducts) {
                if (sp.getThuongHieu() != null && !sp.getThuongHieu().trim().isEmpty())
                    uniqueBrands.add(sp.getThuongHieu().trim());
            }
            String currentSelection = cbLaptopBrand.getSelectedItem() != null
                ? cbLaptopBrand.getSelectedItem().toString() : "Tất cả hãng";
            cbLaptopBrand.removeAllItems();
            cbLaptopBrand.addItem("Tất cả hãng");
            for (String brand : uniqueBrands) cbLaptopBrand.addItem(brand);
            if (!currentSelection.equals("Tất cả hãng") && uniqueBrands.contains(currentSelection))
                cbLaptopBrand.setSelectedItem(currentSelection);
            else
                cbLaptopBrand.setSelectedIndex(0);
        } finally { isLoadingBrands = false; }
    }

    private void applyFilter() {
        String kw = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        showProducts.clear();
        for (SanPhamDTO sp : allProducts) {
            if (!filterStatus.equals("Tat_ca") && !getStatusKey(sp).equals(filterStatus)) continue;
            if (!filterLaptopBrand.equals("Tat_ca") && !sp.getThuongHieu().equalsIgnoreCase(filterLaptopBrand)) continue;
            if (filterGroup.equals("Laptop")) {
                if (sp.getMaLoai() != MALOAI_LAPTOP) continue;
            } else if (filterGroup.equals("PhuKien")) {
                if (filterSubLoai != -1) {
                    if (sp.getMaLoai() != filterSubLoai) continue;
                } else {
                    boolean isPhuKien = false;
                    for (int ma : MALOAI_PHUKIEN) if (sp.getMaLoai() == ma) { isPhuKien = true; break; }
                    if (!isPhuKien) continue;
                }
            }
            if (!kw.isEmpty()) {
                boolean match = sp.getTenSP().toLowerCase().contains(kw)
                    || sp.getThuongHieu().toLowerCase().contains(kw)
                    || String.valueOf(sp.getMaSP()).contains(kw)
                    || (sp.getMauSac() != null && sp.getMauSac().toLowerCase().contains(kw));
                if (!match) continue;
            }
            if (!activeGiaKey.equals("Tat_ca")) {
                double minGia = 0, maxGia = 999_999_999;
                for (String[] g : GIA_RANGES) {
                    if (g[0].equals(activeGiaKey)) { minGia = Double.parseDouble(g[2]); maxGia = Double.parseDouble(g[3]); break; }
                }
                double gia = sp.getGia() != null ? sp.getGia().doubleValue() : 0;
                if (gia < minGia || gia > maxGia) continue;
            }
            showProducts.add(sp);
        }
        renderTable();
        updateStats();
    }

    private String getStatusKey(SanPhamDTO sp) {
        int sl = sp.getSoLuongTon(), min = sp.getSoLuongToiThieu() > 0 ? sp.getSoLuongToiThieu() : 5;
        if (sl <= 0)   return "Hết hàng";
        if (sl <= min) return "Sắp hết hàng";
        return "Còn hàng";
    }
    private String getStatusDisplay(SanPhamDTO sp) { return getStatusKey(sp); }
    private Color  getStatusColor(SanPhamDTO sp) {
        String k = getStatusKey(sp);
        if (k.equals("Hết hàng"))     return DANGER;
        if (k.equals("Sắp hết hàng")) return WARNING_COLOR;
        return SUCCESS;
    }

    private void renderTable() {
        modelProduct.setRowCount(0);
        int stt = 1;
        for (SanPhamDTO sp : showProducts) {
            String tenLoai = loaiMap.getOrDefault(sp.getMaLoai(), "Loại " + sp.getMaLoai());
            modelProduct.addRow(new Object[]{
                stt++, sp.getMaSP(), sp.getTenSP(), tenLoai, sp.getThuongHieu(),
                sp.getMauSac() != null ? sp.getMauSac() : "-",
                formatMoney(sp.getGia()) + " đ",
                sp.getGiaGoc() != null ? formatMoney(sp.getGiaGoc()) + " đ" : "-",
                sp.getSoLuongTon(), sp.getThoiHanBaoHanhThang() + " tháng",
                getStatusDisplay(sp), sp.getTrangThai()
            });
        }
        if (lblRecordCount != null)
            lblRecordCount.setText("Hiển thị " + showProducts.size() + " / " + allProducts.size() + " sản phẩm");
    }

    private void updateStats() {
        int tong = allProducts.size(), con = 0, sap = 0, het = 0;
        for (SanPhamDTO sp : allProducts) {
            String k = getStatusKey(sp);
            if      (k.equals("Hết hàng"))     het++;
            else if (k.equals("Sắp hết hàng")) sap++;
            else                               con++;
        }
        if (lblTongSP  != null) lblTongSP.setText(String.valueOf(tong));
        if (lblConHang != null) lblConHang.setText(String.valueOf(con));
        if (lblSapHet  != null) lblSapHet.setText(String.valueOf(sap));
        if (lblHetHang != null) lblHetHang.setText(String.valueOf(het));
    }

    // ═══════════════════════════════════════════════════════════
    // TITLE PANEL
    // ═══════════════════════════════════════════════════════════
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
        panel.setOpaque(false); panel.setPreferredSize(new Dimension(0, 58));
        JLabel title = new JLabel("  QUẢN LÝ SẢN PHẨM");
        title.setFont(FONT_TITLE); title.setForeground(WHITE);
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        leftPanel.setOpaque(false); leftPanel.add(title);
        panel.add(leftPanel, BorderLayout.WEST);
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false); rightPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,16));
        JButton btnExcel = createHeaderButton("Xuất Excel",       new Color(46,125,50),  WHITE);
        JButton btnThem  = createHeaderButton("+ Thêm sản phẩm", new Color(255,215,40), PRIMARY_DARK);
        btnThem.addActionListener(e -> openFormDialog(null));
        btnExcel.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Chức năng xuất Excel đang phát triển!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        GridBagConstraints rgc = new GridBagConstraints();
        rgc.anchor = GridBagConstraints.CENTER; rgc.insets = new Insets(0,8,0,0);
        rgc.gridx = 0; rightPanel.add(btnExcel, rgc);
        rgc.gridx = 1; rightPanel.add(btnThem,  rgc);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════
    // CENTER / STATS / TABLE (không đổi logic)
    // ═══════════════════════════════════════════════════════════
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setBackground(CONTENT_BG);
        panel.add(createStatsRow(),  BorderLayout.NORTH);
        panel.add(createTableArea(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false); row.setPreferredSize(new Dimension(0, 82));
        lblTongSP  = makeStatVal(ACCENT);
        lblConHang = makeStatVal(SUCCESS);
        lblSapHet  = makeStatVal(WARNING_COLOR);
        lblHetHang = makeStatVal(DANGER);
        row.add(buildStatCard("Tổng sản phẩm", lblTongSP,  ACCENT,        0));
        row.add(buildStatCard("Còn hàng",      lblConHang, SUCCESS,       1));
        row.add(buildStatCard("Sắp hết hàng",  lblSapHet,  WARNING_COLOR, 2));
        row.add(buildStatCard("Hết hàng",      lblHetHang, DANGER,        3));
        return row;
    }

    private JLabel makeStatVal(Color c) {
        JLabel l = new JLabel("0"); l.setFont(FONT_STAT); l.setForeground(c); return l;
    }

    private JPanel buildStatCard(String label, JLabel val, Color accent, int iconType) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,18)); g2.fillRoundRect(4,4,getWidth()-4,getHeight()-4,14,14);
                g2.setColor(WHITE); g2.fillRoundRect(0,0,getWidth()-4,getHeight()-4,14,14);
                g2.setColor(accent); g2.fillRoundRect(0,0,6,getHeight()-4,6,6); g2.fillRect(3,0,3,getHeight()-4);
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setOpaque(false); card.setLayout(new GridBagLayout());
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lbl.setForeground(new Color(100,120,150));
        JPanel tc = new JPanel(); tc.setLayout(new BoxLayout(tc, BoxLayout.Y_AXIS)); tc.setOpaque(false);
        val.setAlignmentX(Component.LEFT_ALIGNMENT); lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        tc.add(val); tc.add(Box.createVerticalStrut(3)); tc.add(lbl);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.CENTER; gc.insets = new Insets(0,18,0,10);
        gc.gridx = 1; card.add(tc, gc);
        return card;
    }

    private JPanel createTableArea() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0,0,0,15)); g2.fillRoundRect(4,4,getWidth()-4,getHeight()-4,14,14);
                g2.setColor(WHITE); g2.fillRoundRect(0,0,getWidth()-4,getHeight()-4,14,14);
                g2.dispose(); super.paintComponent(g);
            }
        };
        wrapper.setOpaque(false); wrapper.setBorder(BorderFactory.createEmptyBorder(0,0,4,4));
        wrapper.add(createFilterBar(),  BorderLayout.NORTH);
        wrapper.add(createTablePanel(), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(0,6)); bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12,14,8,14));
        JPanel topRow = new JPanel(new BorderLayout(8,0)); topRow.setOpaque(false);
        JPanel searchBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(new Color(180,210,240)); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.dispose(); super.paintComponent(g);
            }
        };
        searchBar.setOpaque(false); searchBar.setPreferredSize(new Dimension(0,36));
        txtSearch = new JTextField(); txtSearch.setFont(FONT_NORMAL);
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0,8,0,0)); txtSearch.setOpaque(false);
        txtSearch.addKeyListener(new KeyAdapter() { @Override public void keyReleased(KeyEvent e) { applyFilter(); } });
        JButton btnSearch = createSmallButton("Tìm kiếm", PRIMARY, WHITE); btnSearch.setPreferredSize(new Dimension(105,36));
        btnSearch.addActionListener(e -> applyFilter());
        searchBar.add(txtSearch, BorderLayout.CENTER); searchBar.add(btnSearch, BorderLayout.EAST);
        JPanel rightCtrl = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); rightCtrl.setOpaque(false);
        JLabel lblSt = new JLabel("Trạng thái:"); lblSt.setFont(FONT_LABEL); lblSt.setForeground(PRIMARY);
        cbStatus = new JComboBox<>(STATUS_DISPLAY); cbStatus.setFont(FONT_NORMAL); cbStatus.setPreferredSize(new Dimension(160,36));
        cbStatus.addActionListener(e -> { filterStatus = STATUS_KEYS[cbStatus.getSelectedIndex()]; applyFilter(); });
        JButton btnReset = createSmallButton("Làm mới", new Color(90,100,115), WHITE); btnReset.setPreferredSize(new Dimension(100,36));
        btnReset.addActionListener(e -> {
            txtSearch.setText(""); filterLaptopBrand="Tat_ca"; filterStatus="Tat_ca";
            filterGroup="Tat_ca"; filterSubLoai=-1; activeGiaKey="Tat_ca";
            cbStatus.setSelectedIndex(0);
            if (cbSubLoai     != null) cbSubLoai.setSelectedIndex(0);
            if (cbLaptopBrand != null) cbLaptopBrand.setSelectedIndex(0);
            refreshGroupTabs(); refreshGiaTabs();
            if (subLoaiRow != null) subLoaiRow.setVisible(false);
            loadData();
        });
        rightCtrl.add(lblSt); rightCtrl.add(cbStatus); rightCtrl.add(Box.createHorizontalStrut(4)); rightCtrl.add(btnReset);
        topRow.add(searchBar, BorderLayout.CENTER); topRow.add(rightCtrl, BorderLayout.EAST);
        bar.add(topRow, BorderLayout.NORTH);

        JPanel groupRow = new JPanel(new BorderLayout(8,0)); groupRow.setOpaque(false);
        groupRow.setBorder(BorderFactory.createEmptyBorder(6,0,0,0));
        JLabel lblLoai = new JLabel("Loại:"); lblLoai.setFont(FONT_LABEL); lblLoai.setForeground(PRIMARY);
        lblLoai.setPreferredSize(new Dimension(42,30));
        JPanel groupTabs = new JPanel(new FlowLayout(FlowLayout.LEFT,5,2)); groupTabs.setOpaque(false); groupBtns.clear();

        JPanel laptopBrandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        laptopBrandRow.setOpaque(false); laptopBrandRow.setBorder(BorderFactory.createEmptyBorder(4,0,0,0));
        laptopBrandRow.setVisible(false);
        JLabel lblLaptopHang = new JLabel("Hãng:"); lblLaptopHang.setFont(FONT_LABEL); lblLaptopHang.setForeground(PRIMARY);
        cbLaptopBrand = new JComboBox<>(); cbLaptopBrand.setFont(FONT_NORMAL); cbLaptopBrand.setPreferredSize(new Dimension(180,32));
        cbLaptopBrand.setBorder(BorderFactory.createLineBorder(new Color(180,210,240),1));
        cbLaptopBrand.addActionListener(e -> {
            if (isLoadingBrands) return;
            filterLaptopBrand = cbLaptopBrand.getSelectedIndex() <= 0 ? "Tat_ca" : cbLaptopBrand.getSelectedItem().toString();
            applyFilter();
        });
        laptopBrandRow.add(lblLaptopHang); laptopBrandRow.add(cbLaptopBrand);

        String[][] groups = {{"Tat_ca","Tất cả"},{"Laptop","Laptop"},{"PhuKien","Phụ kiện"}};
        for (String[] g : groups) {
            final String gKey = g[0], gDisp = g[1];
            JButton btn = buildGroupTab(gKey, gDisp);
            btn.addActionListener(e -> {
                filterGroup=gKey; filterSubLoai=-1; filterLaptopBrand="Tat_ca";
                refreshGroupTabs();
                laptopBrandRow.setVisible(true);
                if (cbLaptopBrand != null) cbLaptopBrand.setSelectedIndex(0);
                if (subLoaiRow != null) {
                    subLoaiRow.setVisible(gKey.equals("PhuKien"));
                    if (cbSubLoai != null) cbSubLoai.setSelectedIndex(0);
                }
                groupRow.getParent().revalidate(); groupRow.getParent().repaint();
                applyFilter();
            });
            groupTabs.add(btn); groupBtns.add(btn);
        }
        groupRow.add(lblLoai, BorderLayout.WEST); groupRow.add(groupTabs, BorderLayout.CENTER);

        subLoaiRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); subLoaiRow.setOpaque(false);
        subLoaiRow.setBorder(BorderFactory.createEmptyBorder(4,0,0,0)); subLoaiRow.setVisible(false);
        JLabel lblSub = new JLabel("Loại:"); lblSub.setFont(FONT_LABEL); lblSub.setForeground(PRIMARY);
        String[] subItems = new String[MALOAI_PHUKIEN.length + 1]; subItems[0] = "Tất cả phụ kiện";
        for (int i = 0; i < MALOAI_PHUKIEN.length; i++)
            subItems[i+1] = loaiMap.getOrDefault(MALOAI_PHUKIEN[i], "Loại " + MALOAI_PHUKIEN[i]);
        cbSubLoai = new JComboBox<>(subItems); cbSubLoai.setFont(FONT_NORMAL); cbSubLoai.setPreferredSize(new Dimension(180,32));
        cbSubLoai.setBorder(BorderFactory.createLineBorder(new Color(180,210,240),1));
        cbSubLoai.addActionListener(e -> {
            if (isRebuildingSubLoai) return;
            int idx = cbSubLoai.getSelectedIndex();
            filterSubLoai = (idx == 0) ? -1 : MALOAI_PHUKIEN[idx-1];
            applyFilter();
        });
        subLoaiRow.add(lblSub); subLoaiRow.add(cbSubLoai);

        JPanel filterRows = new JPanel(); filterRows.setLayout(new BoxLayout(filterRows, BoxLayout.Y_AXIS)); filterRows.setOpaque(false);
        filterRows.add(groupRow); filterRows.add(laptopBrandRow); filterRows.add(subLoaiRow);

        JPanel giaRow = new JPanel(new BorderLayout(8,0)); giaRow.setOpaque(false);
        giaRow.setBorder(BorderFactory.createEmptyBorder(4,0,0,0));
        JLabel lblGia = new JLabel("Giá:"); lblGia.setFont(FONT_LABEL); lblGia.setForeground(PRIMARY);
        lblGia.setPreferredSize(new Dimension(42,30));
        JPanel giaTabs = new JPanel(new FlowLayout(FlowLayout.LEFT,5,2)); giaTabs.setOpaque(false);
        allGiaBtns.clear();
        for (String[] g : GIA_RANGES) {
            final String gKey = g[0], gDisp = g[1];
            JButton btn = buildGiaTab(gKey, gDisp);
            btn.addActionListener(e -> { activeGiaKey=gKey; refreshGiaTabs(); applyFilter(); });
            giaTabs.add(btn); allGiaBtns.add(btn);
        }
        giaRow.add(lblGia, BorderLayout.WEST); giaRow.add(giaTabs, BorderLayout.CENTER);
        filterRows.add(giaRow);
        bar.add(filterRows, BorderLayout.CENTER);
        return bar;
    }

    private JButton buildGroupTab(String gKey, String label) {
        final Font TF = new Font("Segoe UI", Font.BOLD, 12);
        Canvas cv = new Canvas(); FontMetrics fmC = cv.getFontMetrics(TF);
        final int prefW = fmC.stringWidth(label) + 30;
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = gKey.equals(filterGroup);
                g2.setColor(active ? ACCENT : (getModel().isRollover() ? new Color(210,235,255) : WHITE));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.setColor(active ? ACCENT : new Color(180,210,240));
                g2.setStroke(new BasicStroke(1.3f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.setFont(TF); g2.setColor(active ? WHITE : PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label,(getWidth()-fm.stringWidth(label))/2,(getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(prefW,30); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
        };
        btn.setText(""); btn.setToolTipText(label); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void refreshGroupTabs() { for (JButton b : groupBtns) b.repaint(); }
    private void refreshGiaTabs()   { for (JButton b : allGiaBtns) b.repaint(); }

    private JButton buildGiaTab(String key, String label) {
        final Font TF = new Font("Segoe UI", Font.BOLD, 12);
        Canvas cv = new Canvas(); FontMetrics fmC = cv.getFontMetrics(TF);
        final int prefW = fmC.stringWidth(label) + 30;
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = key.equals(activeGiaKey);
                g2.setColor(active ? ACCENT : (getModel().isRollover() ? new Color(210,235,255) : WHITE));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.setColor(active ? ACCENT : new Color(180,210,240));
                g2.setStroke(new BasicStroke(1.3f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.setFont(TF); g2.setColor(active ? WHITE : PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label,(getWidth()-fm.stringWidth(label))/2,(getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(prefW,30); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
        };
        btn.setText(""); btn.setToolTipText(label); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ═══════════════════════════════════════════════════════════
    // TABLE PANEL
    // ═══════════════════════════════════════════════════════════
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0,10,12,10));
        String[] colHeaders = {"STT","Mã SP","Tên sản phẩm","Loại SP","Thương hiệu","Màu sắc","Giá bán (đ)","Giá gốc (đ)","Tồn kho","Bảo hành","Trạng thái kho","Tình trạng"};
        modelProduct = new DefaultTableModel(colHeaders, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tableProduct = new JTable(modelProduct) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row%2==0 ? WHITE : ROW_ALT);
                else                     c.setBackground(new Color(187,222,251));
                c.setFont(FONT_NORMAL); c.setForeground(isRowSelected(row) ? PRIMARY_DARK : new Color(30,50,80));
                if (c instanceof JLabel) {
                    JLabel lbl = (JLabel) c;
                    if (col==0||col==1||col==8) lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    else                        lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    if (col==10) {
                        String v = modelProduct.getValueAt(row,col) != null ? modelProduct.getValueAt(row,col).toString() : "";
                        lbl.setFont(new Font("Segoe UI",Font.BOLD,12));
                        if      (v.equals("Hết hàng"))     lbl.setForeground(DANGER);
                        else if (v.equals("Sắp hết hàng")) lbl.setForeground(WARNING_COLOR);
                        else                               lbl.setForeground(SUCCESS);
                    }
                    if (col==6) { lbl.setFont(new Font("Segoe UI",Font.BOLD,13)); lbl.setForeground(DANGER); }
                }
                return c;
            }
        };
        tableProduct.setRowHeight(36); tableProduct.setFont(FONT_NORMAL);
        tableProduct.setGridColor(new Color(220,230,245)); tableProduct.setShowVerticalLines(true);
        tableProduct.setIntercellSpacing(new Dimension(0,1));
        tableProduct.setSelectionBackground(new Color(187,222,251)); tableProduct.setSelectionForeground(PRIMARY_DARK);
        tableProduct.setFillsViewportHeight(true);
        JTableHeader header = tableProduct.getTableHeader(); header.setPreferredSize(new Dimension(0,40));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,val,isSel,hasFocus,row,col);
                lbl.setBackground(TABLE_HEADER); lbl.setForeground(WHITE);
                lbl.setFont(new Font("Segoe UI",Font.BOLD,13)); lbl.setHorizontalAlignment(SwingConstants.CENTER); lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,0,1,new Color(60,120,200)),BorderFactory.createEmptyBorder(0,8,0,8)));
                return lbl;
            }
        });
        int[] cw = {40,60,200,100,100,75,125,110,65,90,110,90};
        for (int i=0; i<cw.length; i++) tableProduct.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);
        tableProduct.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2) { int row=tableProduct.getSelectedRow(); if(row>=0&&row<showProducts.size()) openDetailDialog(showProducts.get(row)); }
            }
        });
        JScrollPane scroll = new JScrollPane(tableProduct); scroll.setBorder(new LineBorder(new Color(180,210,240),1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel actionBar = new JPanel(new BorderLayout()); actionBar.setOpaque(false);
        actionBar.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));
        lblRecordCount = new JLabel("0 / 0 sản phẩm");
        lblRecordCount.setFont(new Font("Segoe UI",Font.ITALIC,12)); lblRecordCount.setForeground(new Color(100,120,150));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btnPanel.setOpaque(false);
        JButton btnDetail = createActionButton("Chi tiết",  new Color(90,60,180), WHITE);
        JButton btnThem   = createActionButton("+ Thêm",    PRIMARY,              WHITE);
        JButton btnSua    = createActionButton("Sửa",       new Color(0,150,136), WHITE);
        JButton btnXoa    = createActionButton("Ngừng bán", DANGER,               WHITE);
        btnDetail.addActionListener(e -> { int row=tableProduct.getSelectedRow(); if(row<0){showWarn("Vui lòng chọn sản phẩm!");return;} openDetailDialog(showProducts.get(row)); });
        btnThem.addActionListener(e -> openFormDialog(null));
        btnSua.addActionListener(e -> { int row=tableProduct.getSelectedRow(); if(row<0){showWarn("Vui lòng chọn sản phẩm cần sửa!");return;} openFormDialog(showProducts.get(row)); });
        btnXoa.addActionListener(e -> doXoa());
        btnPanel.add(btnDetail); btnPanel.add(btnThem); btnPanel.add(btnSua); btnPanel.add(btnXoa);
        actionBar.add(lblRecordCount, BorderLayout.WEST); actionBar.add(btnPanel, BorderLayout.EAST);
        panel.add(scroll, BorderLayout.CENTER); panel.add(actionBar, BorderLayout.SOUTH);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  FORM DIALOG — CÓ THÔNG SỐ KỸ THUẬT + LỌC THƯƠNG HIỆU THEO LOẠI SP
    // ═══════════════════════════════════════════════════════════════════════
    private void openFormDialog(SanPhamDTO spEdit) {
        boolean isEdit = spEdit != null;
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = (owner instanceof Frame)
            ? new JDialog((Frame)owner, isEdit?"Sửa sản phẩm":"Thêm sản phẩm mới", true)
            : new JDialog((Dialog)owner, isEdit?"Sửa sản phẩm":"Thêm sản phẩm mới", true);
        // Tính chiều cao tối đa = 90% chiều cao màn hình, tối đa 780px
        int screenH = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
        int dlgH    = Math.min(780, (int)(screenH * 0.90));
        dialog.setSize(750, dlgH); dialog.setLocationRelativeTo(owner); dialog.setResizable(true);

        JPanel main = new JPanel(new BorderLayout()); main.setBackground(CONTENT_BG);

        // Header
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,PRIMARY_DARK,getWidth(),0,PRIMARY));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        hdr.setOpaque(false); hdr.setPreferredSize(new Dimension(0,50));
        hdr.setBorder(BorderFactory.createEmptyBorder(0,16,0,16));
        JLabel hTitle = new JLabel(isEdit ? "✏  Chỉnh sửa thông tin sản phẩm" : "➕  Thêm sản phẩm mới");
        hTitle.setFont(new Font("Segoe UI",Font.BOLD,16)); hTitle.setForeground(WHITE);
        hdr.add(hTitle, BorderLayout.WEST); main.add(hdr, BorderLayout.NORTH);

        // ── FORM FIELDS ──────────────────────────────────────────────────
        JTextField fTen    = createFormField(isEdit ? spEdit.getTenSP() : "");
        JTextField fMau    = createFormField(isEdit && spEdit.getMauSac()!=null ? spEdit.getMauSac() : "");
        JTextField fGia    = createFormField(isEdit ? spEdit.getGia().toPlainString() : "");
        JTextField fGiaGoc = createFormField(isEdit && spEdit.getGiaGoc()!=null ? spEdit.getGiaGoc().toPlainString() : "");
        JTextField fSL     = createFormField(isEdit ? String.valueOf(spEdit.getSoLuongTon()) : "0");
        fSL.setEditable(false); fSL.setFocusable(false); fSL.setBackground(new Color(240,240,240));
        JTextField fMin = createFormField(isEdit ? String.valueOf(spEdit.getSoLuongToiThieu()) : "5");
        JTextField fMax = createFormField(isEdit ? String.valueOf(spEdit.getSoLuongToiDa())    : "100");
        JTextField fBH  = createFormField(isEdit ? String.valueOf(spEdit.getThoiHanBaoHanhThang()) : "12");

        // ── COMBO LOẠI SP (chọn TRƯỚC) ───────────────────────────────────
        ArrayList<LoaiSanPhamDTO> dsDanhSachLoai = loaiBUS.getAll();
        JComboBox<LoaiSanPhamDTO> cbMaLoai = new JComboBox<>();
        cbMaLoai.setFont(FONT_NORMAL); cbMaLoai.setPreferredSize(new Dimension(0,32));
        for (LoaiSanPhamDTO loai : dsDanhSachLoai) cbMaLoai.addItem(loai);
        if (isEdit) {
            for (int i=0; i<cbMaLoai.getItemCount(); i++) {
                if (cbMaLoai.getItemAt(i).getMaLoai() == spEdit.getMaLoai()) { cbMaLoai.setSelectedIndex(i); break; }
            }
        }

        // Khi đổi loại → reset NCC (brand sẽ reload theo NCC)
        cbMaLoai.addActionListener(e -> { /* brand sẽ reload khi chọn NCC */ });

        // ── PANEL THÔNG SỐ KỸ THUẬT (chỉ hiện khi Laptop) ───────────────
        ThongSoKyThuatDTO tsktEdit = (isEdit && spEdit.getMaLoai() == MALOAI_LAPTOP)
            ? tsktBUS.getByMaSP(spEdit.getMaSP()) : null;

        JTextField fCpu    = createFormField(tsktEdit!=null && tsktEdit.getCpu()!=null        ? tsktEdit.getCpu()        : "");
        JTextField fRam    = createFormField(tsktEdit!=null && tsktEdit.getRam()!=null        ? tsktEdit.getRam()        : "");
        JTextField fOCung  = createFormField(tsktEdit!=null && tsktEdit.getOCung()!=null      ? tsktEdit.getOCung()      : "");
        JTextField fManHinh= createFormField(tsktEdit!=null && tsktEdit.getManHinh()!=null    ? tsktEdit.getManHinh()    : "");
        JTextField fVga    = createFormField(tsktEdit!=null && tsktEdit.getVga()!=null        ? tsktEdit.getVga()        : "");
        JTextField fHdh    = createFormField(tsktEdit!=null && tsktEdit.getHeDieuHanh()!=null ? tsktEdit.getHeDieuHanh() : "");
        JTextField fPin    = createFormField(tsktEdit!=null && tsktEdit.getPin()!=null        ? tsktEdit.getPin()        : "");
        JTextField fTLuong = createFormField(tsktEdit!=null && tsktEdit.getTrongLuong()!=null ? tsktEdit.getTrongLuong() : "");
        JTextField fKetNoi = createFormField(tsktEdit!=null && tsktEdit.getKetNoi()!=null     ? tsktEdit.getKetNoi()     : "");

        // Placeholder hints
        setPlaceholder(fCpu,     "VD: Intel Core i7-1255U");
        setPlaceholder(fRam,     "VD: 16GB DDR5");
        setPlaceholder(fOCung,   "VD: 512GB NVMe SSD");
        setPlaceholder(fManHinh, "VD: 15.6\" FHD IPS 144Hz");
        setPlaceholder(fVga,     "VD: NVIDIA RTX 4060 6GB");
        setPlaceholder(fHdh,     "VD: Windows 11 Home");
        setPlaceholder(fPin,     "VD: 72Wh, ~8 giờ");
        setPlaceholder(fTLuong,  "VD: 1.8 kg");
        setPlaceholder(fKetNoi,  "VD: WiFi 6, BT 5.3, USB-C");

        // Panel TSKT với viền xanh lá
        JPanel tsktPanel = new JPanel(new GridBagLayout());
        tsktPanel.setBackground(LAPTOP_SPEC_BG);
        tsktPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LAPTOP_SPEC_BORDER, 2),
                "  🖥  Thông số kỹ thuật Laptop",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), LAPTOP_SPEC_BORDER),
            BorderFactory.createEmptyBorder(6, 12, 10, 12)));

        GridBagConstraints tgc = new GridBagConstraints();
        tgc.fill = GridBagConstraints.HORIZONTAL; tgc.insets = new Insets(5,6,5,6);
        addRow(tsktPanel, tgc, 0, "CPU (*)",         fCpu,     "RAM (*)",      fRam);
        addRow(tsktPanel, tgc, 1, "Ổ cứng (*)",      fOCung,   "Màn hình (*)", fManHinh);
        addRow(tsktPanel, tgc, 2, "Card đồ họa",     fVga,     "Hệ điều hành",fHdh);
        addRow(tsktPanel, tgc, 3, "Pin",              fPin,     "Trọng lượng", fTLuong);
        tgc.gridx=0; tgc.gridy=4; tgc.weightx=0;   tsktPanel.add(makeLbl("Kết nối"), tgc);
        tgc.gridx=1; tgc.gridwidth=3; tgc.weightx=1; tsktPanel.add(fKetNoi, tgc); tgc.gridwidth=1;

        // Ẩn/hiện TSKT theo loại SP
        boolean isLaptopNow = (cbMaLoai.getSelectedItem() instanceof LoaiSanPhamDTO)
            && ((LoaiSanPhamDTO)cbMaLoai.getSelectedItem()).getMaLoai() == MALOAI_LAPTOP;
        tsktPanel.setVisible(isLaptopNow);

        cbMaLoai.addActionListener(e -> {
            boolean isLaptop = (cbMaLoai.getSelectedItem() instanceof LoaiSanPhamDTO)
                && ((LoaiSanPhamDTO)cbMaLoai.getSelectedItem()).getMaLoai() == MALOAI_LAPTOP;
            tsktPanel.setVisible(isLaptop);
            dialog.revalidate(); dialog.repaint();
        });

        // ── NHÀ CUNG CẤP — ComboBox, chọn 1 NCC → tự điền thương hiệu ─────
        ArrayList<NhaCungCapDTO> dsNCC = nccBUS.getDanhSachHoatDong();

        // ── Dùng nccBrandsCache đã build sẵn trong background — 0 query DB khi mở form ──
        java.util.Map<Integer, java.util.Set<String>> nccBrandsMap = nccBrandsCache;

        // ── cbNCC: ComboBox chọn NCC ─────────────────────────────────────
        JComboBox<NhaCungCapDTO> cbNCC = new JComboBox<>();
        cbNCC.setFont(FONT_NORMAL); cbNCC.setPreferredSize(new Dimension(0,36));
        cbNCC.setBorder(BorderFactory.createLineBorder(new Color(180,210,240),1));
        cbNCC.addItem(null);
        for (NhaCungCapDTO ncc : dsNCC) cbNCC.addItem(ncc);
        cbNCC.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                if (value == null) {
                    setText("-- Chọn nhà cung cấp --");
                    setForeground(new Color(150,160,180));
                } else if (value instanceof NhaCungCapDTO) {
                    NhaCungCapDTO n = (NhaCungCapDTO)value;
                    java.util.Set<String> brands = nccBrandsMap.getOrDefault(n.getMaNCC(), new java.util.TreeSet<>());
                    String brandHint = brands.isEmpty() ? "" : "  [" + String.join(", ", brands) + "]";
                    setText(n.getTenNCC() + brandHint);
                    setForeground(PRIMARY_DARK);
                }
                setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
                return this;
            }
        });

        // ── cbBrand: ComboBox chọn thương hiệu (editable, reload theo NCC) ─
        // → Gear có nhiều brand thì user chọn được, NCC 1-brand thì tự điền luôn
        JComboBox<String> cbBrand = new JComboBox<>();
        cbBrand.setEditable(true);
        cbBrand.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cbBrand.setPreferredSize(new Dimension(0,32));
        cbBrand.setBorder(BorderFactory.createLineBorder(new Color(150,190,240),1));
        cbBrand.setBackground(new Color(240,244,250));

        // Helper: reload brand list khi đổi NCC
        Runnable reloadBrandCombo = () -> {
            NhaCungCapDTO selNCC = (NhaCungCapDTO) cbNCC.getSelectedItem();
            cbBrand.removeAllItems();
            if (selNCC != null) {
                java.util.Set<String> brands = nccBrandsMap.getOrDefault(selNCC.getMaNCC(), new java.util.TreeSet<>());
                for (String b : brands) cbBrand.addItem(b);
                if (brands.size() == 1) {
                    // Chỉ 1 brand → tự điền, disable chọn
                    cbBrand.setSelectedIndex(0);
                    cbBrand.setEnabled(false);
                    cbBrand.setBackground(new Color(230,235,245));
                } else if (brands.size() > 1) {
                    // Nhiều brand (Gear) → cho chọn, highlight
                    cbBrand.setSelectedIndex(-1);
                    cbBrand.setEnabled(true);
                    cbBrand.setBackground(new Color(255,252,230));  // nền vàng nhạt = cần chọn
                    cbBrand.getEditor().setItem("");
                } else {
                    // NCC chưa có SP → để trống, cho nhập tay
                    cbBrand.setEnabled(true);
                    cbBrand.setBackground(WHITE);
                    cbBrand.getEditor().setItem("");
                }
            } else {
                cbBrand.setEnabled(false);
                cbBrand.setBackground(new Color(230,235,245));
            }
        };

        // Khi đổi NCC → reload brand
        cbNCC.addActionListener(e -> reloadBrandCombo.run());

        // Nếu đang sửa → chọn NCC cũ
        if (isEdit) {
            ArrayList<Integer> linkedNCC = nccBUS.getDanhSachNccCuaSP(spEdit.getMaSP());
            if (!linkedNCC.isEmpty()) {
                int firstNCC = linkedNCC.get(0);
                for (int i=1; i<cbNCC.getItemCount(); i++) {
                    NhaCungCapDTO n = cbNCC.getItemAt(i);
                    if (n != null && n.getMaNCC() == firstNCC) { cbNCC.setSelectedIndex(i); break; }
                }
            }
            // Sau reloadBrandCombo, ghi đè brand cũ của SP
            if (spEdit.getThuongHieu() != null && !spEdit.getThuongHieu().isEmpty())
                cbBrand.getEditor().setItem(spEdit.getThuongHieu());
        } else {
            cbNCC.setSelectedIndex(0);
            reloadBrandCombo.run();
        }

        // JLabel lblNccHint = new JLabel(dsNCC.isEmpty()
        //     ? "⚠ Chưa có nhà cung cấp nào. Vào mục NCC để thêm trước."
        //     : "Chọn NCC → thương hiệu tự điền  •  Gear: chọn thương hiệu cụ thể");
        // lblNccHint.setFont(new Font("Segoe UI",Font.ITALIC,11));
        // lblNccHint.setForeground(dsNCC.isEmpty() ? DANGER : new Color(100,150,100));
        if (dsNCC.isEmpty()) cbNCC.setEnabled(false);

        // Layout NCC + Brand trong 1 panel dọc
        JPanel nccPanel = new JPanel(new GridBagLayout()); nccPanel.setOpaque(false);
        GridBagConstraints ngc = new GridBagConstraints();
        ngc.fill=GridBagConstraints.HORIZONTAL; ngc.weightx=1.0; ngc.gridx=0; ngc.insets=new Insets(0,0,4,0);
        ngc.gridy=0; nccPanel.add(cbNCC, ngc);
        ngc.gridy=1; nccPanel.add(cbBrand, ngc);
        // ngc.gridy=2; nccPanel.add(lblNccHint, ngc);

        // ── MÔ TẢ / TÌNH TRẠNG / HÌNH ẢNH ───────────────────────────────
        JTextArea fMoTa = new JTextArea(isEdit && spEdit.getMoTa()!=null ? spEdit.getMoTa() : "");
        fMoTa.setFont(FONT_NORMAL); fMoTa.setRows(2); fMoTa.setLineWrap(true); fMoTa.setWrapStyleWord(true);
        fMoTa.setBorder(new CompoundBorder(new LineBorder(new Color(180,210,240),1),BorderFactory.createEmptyBorder(4,8,4,8)));
        JComboBox<String> cbTT = new JComboBox<>(new String[]{"DangBan","NgungBan"});
        cbTT.setFont(FONT_NORMAL); cbTT.setPreferredSize(new Dimension(0,32));
        if (isEdit) cbTT.setSelectedItem(spEdit.getTrangThai());
        final String[] selectedImagePath = { isEdit && spEdit.getHinhAnh()!=null ? spEdit.getHinhAnh() : "" };
        JLabel lblImagePreview = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(new Color(230,238,250)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(180,210,240)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                if (getIcon()==null) {
                    int cx=getWidth()/2, cy=getHeight()/2;
                    g2.setColor(new Color(150,180,220)); g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
                    FontMetrics fm=g2.getFontMetrics(); String hint="Chưa có ảnh";
                    g2.drawString(hint,cx-fm.stringWidth(hint)/2,cy+6);
                } else super.paintComponent(g);
                g2.dispose();
            }
        };
        lblImagePreview.setPreferredSize(new Dimension(100,75)); lblImagePreview.setHorizontalAlignment(SwingConstants.CENTER); lblImagePreview.setOpaque(false);
        if (!selectedImagePath[0].isEmpty()) {
            try { lblImagePreview.setIcon(new ImageIcon(new ImageIcon(selectedImagePath[0]).getImage().getScaledInstance(96,71,Image.SCALE_SMOOTH))); } catch (Exception ignored) {}
        }
        JTextField fHinhAnhPath = createFormField(selectedImagePath[0]);
        fHinhAnhPath.setEditable(false); fHinhAnhPath.setBackground(new Color(240,240,240)); fHinhAnhPath.setFont(new Font("Segoe UI",Font.PLAIN,11));
        JButton btnChonAnh = buildImgBtn("📁 Chọn ảnh", ACCENT);
        JButton btnXoaAnh  = buildImgBtn("✕ Xóa ảnh",   DANGER);
        btnChonAnh.addActionListener(e2 -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Hình ảnh","jpg","jpeg","png","gif","bmp")); fc.setAcceptAllFileFilterUsed(false);
            if (fc.showOpenDialog(dialog)==JFileChooser.APPROVE_OPTION) {
                String path = fc.getSelectedFile().getAbsolutePath(); selectedImagePath[0]=path; fHinhAnhPath.setText(path);
                try { lblImagePreview.setIcon(new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(96,71,Image.SCALE_SMOOTH))); } catch (Exception ex) { lblImagePreview.setIcon(null); }
                lblImagePreview.repaint();
            }
        });
        btnXoaAnh.addActionListener(e2 -> { selectedImagePath[0]=""; fHinhAnhPath.setText(""); lblImagePreview.setIcon(null); lblImagePreview.repaint(); });
        JPanel imgPanel = new JPanel(new BorderLayout(8,0)); imgPanel.setOpaque(false);
        imgPanel.add(lblImagePreview, BorderLayout.WEST);
        JPanel imgRight = new JPanel(); imgRight.setLayout(new BoxLayout(imgRight,BoxLayout.Y_AXIS)); imgRight.setOpaque(false);
        fHinhAnhPath.setAlignmentX(Component.LEFT_ALIGNMENT); fHinhAnhPath.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
        JPanel imgBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); imgBtnRow.setOpaque(false);
        imgBtnRow.add(btnChonAnh); imgBtnRow.add(btnXoaAnh); imgBtnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblHint2 = new JLabel("Hỗ trợ: JPG, PNG, GIF, BMP");
        lblHint2.setFont(new Font("Segoe UI",Font.ITALIC,10)); lblHint2.setForeground(new Color(140,160,190)); lblHint2.setAlignmentX(Component.LEFT_ALIGNMENT);
        imgRight.add(Box.createVerticalStrut(4)); imgRight.add(fHinhAnhPath); imgRight.add(Box.createVerticalStrut(6)); imgRight.add(imgBtnRow); imgRight.add(Box.createVerticalStrut(4)); imgRight.add(lblHint2);
        imgPanel.add(imgRight, BorderLayout.CENTER);

        // ── GHÉP FORM ─────────────────────────────────────────────────────
        // Thứ tự: Loại SP → Thương hiệu → các field → NCC → TSKT (nếu laptop) → ảnh, mô tả
        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(16,24,16,24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill=GridBagConstraints.HORIZONTAL; gc.insets=new Insets(6,6,6,6);

        // Row 0: Loại SP — Tên SP
        addRow(form, gc, 0, "Loại sản phẩm (*)", cbMaLoai, "Tên sản phẩm (*)", fTen);
        // Row 1: NCC (full width — combobox)
        gc.gridx=0; gc.gridy=1; gc.weightx=0; form.add(makeLbl("Nhà cung cấp (*)"), gc);
        gc.gridx=1; gc.gridwidth=3; gc.weightx=1.0; form.add(nccPanel, gc); gc.gridwidth=1;
        // Row 2: Thương hiệu (auto) — Màu sắc
        addRow(form, gc, 2, "Thương hiệu (*)", cbBrand, "Màu sắc", fMau);
        // Row 3: Giá bán — Giá gốc
        addRow(form, gc, 3, "Giá bán (đ) (*)", fGia, "Giá gốc (đ) (*)", fGiaGoc);
        // Row 4: Tồn kho — Bảo hành
        addRow(form, gc, 4, "Số lượng tồn", fSL, "Bảo hành (tháng)", fBH);
        // Row 5: SL tối thiểu — SL tối đa
        addRow(form, gc, 5, "SL tối thiểu", fMin, "SL tối đa", fMax);
        // Row 6: Tình trạng
        gc.gridx=0; gc.gridy=6; gc.weightx=0; form.add(makeLbl("Tình trạng"), gc);
        gc.gridx=1; gc.weightx=0.5; form.add(cbTT, gc);
        // Row 7: Hình ảnh (full width)
        gc.gridx=0; gc.gridy=7; gc.weightx=0; form.add(makeLbl("Hình ảnh"), gc);
        gc.gridx=1; gc.gridwidth=3; gc.weightx=1.0; form.add(imgPanel, gc); gc.gridwidth=1;
        // Row 8: Mô tả (full width)
        gc.gridx=0; gc.gridy=8; gc.weightx=0; form.add(makeLbl("Mô tả"), gc);
        gc.gridx=1; gc.gridwidth=3; gc.weightx=1.0;
        JScrollPane moTaScroll = new JScrollPane(fMoTa); moTaScroll.setBorder(null);
        form.add(moTaScroll, gc); gc.gridwidth=1;
        // Row 9: TSKT panel (full width, chỉ khi laptop)
        gc.gridx=0; gc.gridy=9; gc.gridwidth=4; gc.weightx=1.0;
        gc.insets=new Insets(12,6,6,6);
        form.add(tsktPanel, gc);
        gc.gridwidth=1; gc.insets=new Insets(6,6,6,6);

        JScrollPane formScroll = new JScrollPane(form); formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.setMinimumSize(new Dimension(0, 200));
        main.add(formScroll, BorderLayout.CENTER);

        // ── FOOTER BUTTONS ────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
        footer.setBackground(CONTENT_BG); footer.setBorder(new MatteBorder(1,0,0,0,new Color(180,210,240)));
        JButton btnCancel = createActionButton("Hủy bỏ",                         new Color(90,100,115), WHITE);
        JButton btnSave   = createActionButton(isEdit?"Lưu thay đổi":"Thêm mới", PRIMARY,               WHITE);
        btnSave.setPreferredSize(new Dimension(140,38));
        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            // ── Validate cơ bản ──
            String tenSP      = fTen.getText().trim();
            Object brandObj   = cbBrand.getEditor().getItem();
            String thuongHieu = brandObj != null ? brandObj.toString().trim() : "";
            if (tenSP.isEmpty() || fGia.getText().trim().isEmpty() || fGiaGoc.getText().trim().isEmpty()) {
                warnOn(dialog,"Vui lòng điền đầy đủ các trường bắt buộc (*)!"); return;
            }
            if (cbMaLoai.getSelectedItem()==null) { warnOn(dialog,"Vui lòng chọn loại sản phẩm!"); return; }
            if (dsNCC.isEmpty()) { warnOn(dialog,"Chưa có nhà cung cấp nào trong hệ thống!"); return; }
            NhaCungCapDTO selectedNCC = (NhaCungCapDTO) cbNCC.getSelectedItem();
            if (selectedNCC == null) { warnOn(dialog,"Vui lòng chọn nhà cung cấp!"); return; }
            if (thuongHieu.isEmpty()) { warnOn(dialog,"Vui lòng chọn hoặc nhập thương hiệu!"); return; }

            // ── Validate TSKT nếu là Laptop ──
            int maLoaiChon = ((LoaiSanPhamDTO)cbMaLoai.getSelectedItem()).getMaLoai();
            boolean isLaptop = maLoaiChon == MALOAI_LAPTOP;
            if (isLaptop) {
                if (fCpu.getText().trim().isEmpty() || fRam.getText().trim().isEmpty()
                 || fOCung.getText().trim().isEmpty() || fManHinh.getText().trim().isEmpty()) {
                    warnOn(dialog, "Laptop phải có đầy đủ: CPU, RAM, Ổ cứng, Màn hình!"); return;
                }
            }

            // ── Parse số liệu ──
            BigDecimal gia, giaGoc; int soLuong,toiThieu,toiDa,baoHanh;
            try {
                gia       = new BigDecimal(fGia.getText().trim().replace(",",""));
                giaGoc    = new BigDecimal(fGiaGoc.getText().trim().replace(",",""));
                soLuong   = Integer.parseInt(fSL.getText().trim());
                toiThieu  = Integer.parseInt(fMin.getText().trim());
                toiDa     = Integer.parseInt(fMax.getText().trim());
                baoHanh   = Integer.parseInt(fBH.getText().trim());
            } catch (NumberFormatException ex) { warnOn(dialog,"Giá, số lượng, bảo hành phải là số hợp lệ!"); return; }
            if (gia.compareTo(BigDecimal.ZERO)<=0||giaGoc.compareTo(BigDecimal.ZERO)<=0) { warnOn(dialog,"Giá bán và giá gốc phải > 0!"); return; }
            if (toiDa>0&&toiDa<toiThieu) { warnOn(dialog,"SL tối đa phải >= SL tối thiểu!"); return; }

            // ── Build DTO sản phẩm ──
            SanPhamDTO sp = new SanPhamDTO();
            if (isEdit) sp.setMaSP(spEdit.getMaSP());
            sp.setTenSP(tenSP); sp.setThuongHieu(thuongHieu); sp.setMauSac(fMau.getText().trim());
            sp.setGia(gia); sp.setGiaGoc(giaGoc); sp.setSoLuongTon(soLuong);
            sp.setSoLuongToiThieu(toiThieu); sp.setSoLuongToiDa(toiDa); sp.setThoiHanBaoHanhThang(baoHanh);
            sp.setMaLoai(maLoaiChon); sp.setMoTa(fMoTa.getText().trim());
            sp.setTrangThai((String)cbTT.getSelectedItem());
            sp.setHinhAnh(selectedImagePath[0].isEmpty()?null:selectedImagePath[0]);

            // ── Lưu SP ──
            boolean ok = isEdit ? sanPhamBUS.suaSanPham(sp) : sanPhamBUS.themSanPham(sp);
            if (!ok) { warnOn(dialog, isEdit?"Cập nhật thất bại!":"Thêm sản phẩm thất bại!"); return; }

            // ── Lưu liên kết NCC (1 NCC duy nhất) ──
            int maSPFinal = isEdit ? sp.getMaSP() : sanPhamBUS.getMaSPMoiNhat();
            nccBUS.xoaLienKetSP(maSPFinal);
            nccBUS.linkSanPham(selectedNCC.getMaNCC(), maSPFinal);

            // ── Lưu TSKT nếu là Laptop ──
            if (isLaptop) {
                ThongSoKyThuatDTO tskt = new ThongSoKyThuatDTO();
                tskt.setMaSP(maSPFinal);
                tskt.setCpu(fCpu.getText().trim());
                tskt.setRam(fRam.getText().trim());
                tskt.setOCung(fOCung.getText().trim());
                tskt.setManHinh(fManHinh.getText().trim());
                tskt.setVga(fVga.getText().trim().isEmpty()    ? null : fVga.getText().trim());
                tskt.setHeDieuHanh(fHdh.getText().trim().isEmpty() ? null : fHdh.getText().trim());
                tskt.setPin(fPin.getText().trim().isEmpty()    ? null : fPin.getText().trim());
                tskt.setTrongLuong(fTLuong.getText().trim().isEmpty() ? null : fTLuong.getText().trim());
                tskt.setKetNoi(fKetNoi.getText().trim().isEmpty() ? null : fKetNoi.getText().trim());

                boolean tsktOk;
                if (isEdit && tsktBUS.getByMaSP(maSPFinal) != null) {
                    tsktOk = tsktBUS.update(tskt);
                } else {
                    tsktOk = tsktBUS.add(tskt);
                }
                if (!tsktOk) {
                    warnOn(dialog, "⚠ Đã lưu sản phẩm nhưng không lưu được thông số kỹ thuật!\nKiểm tra lại DB.");
                    dialog.dispose(); loadData(); return;
                }
            }

            JOptionPane.showMessageDialog(dialog, isEdit?"✅ Cập nhật thành công!":"✅ Thêm sản phẩm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose(); loadData();
        });

        footer.add(btnCancel); footer.add(btnSave);
        main.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(main); dialog.setVisible(true);
    }

    // ── Tooltip placeholder cho JTextField ──────────────────────────────
    private void setPlaceholder(JTextField field, String hint) {
        field.setToolTipText(hint);
        field.putClientProperty("placeholder", hint);
        field.addFocusListener(new FocusAdapter() {
            final Color normalFg = field.getForeground();
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals((String)field.getClientProperty("placeholderShown"))) {
                    field.setText(""); field.setForeground(normalFg);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(hint); field.setForeground(new Color(170,180,200));
                    field.putClientProperty("placeholderShown", hint);
                }
            }
        });
        // Hiện placeholder ngay nếu rỗng
        if (field.getText().trim().isEmpty()) {
            field.setText(hint); field.setForeground(new Color(170,180,200));
            field.putClientProperty("placeholderShown", hint);
        }
    }

    // Helper lấy value thật (bỏ qua placeholder)
    @SuppressWarnings("unused")
    private String getRealText(JTextField field) {
        String ph = (String) field.getClientProperty("placeholderShown");
        String txt = field.getText();
        return (ph != null && ph.equals(txt)) ? "" : txt.trim();
    }

    private void addRow(JPanel form, GridBagConstraints gc, int row, String l1, Component f1, String l2, Component f2) {
        gc.gridy=row;
        gc.gridx=0; gc.weightx=0;   form.add(makeLbl(l1), gc);
        gc.gridx=1; gc.weightx=1.0; form.add(f1, gc);
        gc.gridx=2; gc.weightx=0;   form.add(makeLbl(l2), gc);
        gc.gridx=3; gc.weightx=1.0; form.add(f2, gc);
    }

    // ═══════════════════════════════════════════════════════════
    // DETAIL DIALOG
    // ═══════════════════════════════════════════════════════════
    private void openDetailDialog(SanPhamDTO sp) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = (owner instanceof Frame)
            ? new JDialog((Frame)owner,"Chi tiết sản phẩm",true)
            : new JDialog((Dialog)owner,"Chi tiết sản phẩm",true);
        dialog.setSize(540, 680); dialog.setLocationRelativeTo(owner); dialog.setResizable(false);

        JPanel main = new JPanel(new BorderLayout()); main.setBackground(CONTENT_BG);
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,PRIMARY_DARK,getWidth(),0,ACCENT));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        hdr.setOpaque(false); hdr.setPreferredSize(new Dimension(0,50)); hdr.setBorder(BorderFactory.createEmptyBorder(0,16,0,16));
        JLabel ht = new JLabel("Chi tiết: "+sp.getTenSP()); ht.setFont(new Font("Segoe UI",Font.BOLD,15)); ht.setForeground(WHITE);
        hdr.add(ht, BorderLayout.WEST); main.add(hdr, BorderLayout.NORTH);

        String tenLoai = loaiBUS.getTenLoai(sp.getMaLoai());
        boolean isLaptopDetail = sp.getMaLoai() == MALOAI_LAPTOP;
        ThongSoKyThuatDTO tsktDetail = isLaptopDetail ? tsktBUS.getByMaSP(sp.getMaSP()) : null;

        JPanel card = new JPanel(new GridBagLayout()); card.setBackground(WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(16,24,16,24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor=GridBagConstraints.NORTHWEST; gc.insets=new Insets(5,8,5,8);

        Object[][] rows = {
            {"Mã sản phẩm",   sp.getMaSP()},
            {"Tên sản phẩm",  sp.getTenSP()},
            {"Loại SP",       tenLoai},
            {"Thương hiệu",   sp.getThuongHieu()},
            {"Màu sắc",       sp.getMauSac()!=null?sp.getMauSac():"-"},
            {"Giá bán",       formatMoney(sp.getGia())+" đ"},
            {"Giá gốc",       sp.getGiaGoc()!=null?formatMoney(sp.getGiaGoc())+" đ":"-"},
            {"Tồn kho",       sp.getSoLuongTon()+" sản phẩm"},
            {"Bảo hành",      sp.getThoiHanBaoHanhThang()+" tháng"},
            {"Trạng thái kho",getStatusDisplay(sp)},
            {"Tình trạng",    sp.getTrangThai()},
            {"Mô tả",         sp.getMoTa()!=null?sp.getMoTa():"-"}
        };
        int r = 0;
        for (Object[] row : rows) {
            gc.gridx=0; gc.gridy=r; gc.weightx=0;
            JLabel lbl = new JLabel(row[0]+":"); lbl.setFont(new Font("Segoe UI",Font.BOLD,12));
            lbl.setForeground(new Color(100,120,160)); lbl.setPreferredSize(new Dimension(130,22)); card.add(lbl,gc);
            gc.gridx=1; gc.weightx=1.0;
            JLabel val = new JLabel("<html>"+String.valueOf(row[1]).replace("\n","<br>")+"</html>");
            val.setFont(FONT_NORMAL); val.setForeground(PRIMARY_DARK);
            String key = (String)row[0];
            if (key.equals("Trạng thái kho")) { val.setForeground(getStatusColor(sp)); val.setFont(new Font("Segoe UI",Font.BOLD,13)); }
            if (key.equals("Giá bán"))         { val.setForeground(DANGER); val.setFont(new Font("Segoe UI",Font.BOLD,14)); }
            card.add(val,gc); r++;
        }

        // Hiện TSKT nếu là Laptop
        if (isLaptopDetail && tsktDetail != null) {
            gc.gridx=0; gc.gridy=r; gc.gridwidth=2; gc.weightx=1.0;
            JLabel sepLabel = new JLabel("  🖥  Thông số kỹ thuật Laptop");
            sepLabel.setFont(new Font("Segoe UI",Font.BOLD,13)); sepLabel.setForeground(LAPTOP_SPEC_BORDER);
            sepLabel.setOpaque(true); sepLabel.setBackground(LAPTOP_SPEC_BG);
            sepLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1,0,1,0,LAPTOP_SPEC_BORDER),
                BorderFactory.createEmptyBorder(4,8,4,8)));
            card.add(sepLabel,gc); gc.gridwidth=1; r++;

            String[][] tsRows = {
                {"CPU",        tsktDetail.getCpu()},
                {"RAM",        tsktDetail.getRam()},
                {"Ổ cứng",     tsktDetail.getOCung()},
                {"Màn hình",   tsktDetail.getManHinh()},
                {"Card đồ họa",tsktDetail.getVga()},
                {"Hệ điều hành",tsktDetail.getHeDieuHanh()},
                {"Pin",        tsktDetail.getPin()},
                {"Trọng lượng",tsktDetail.getTrongLuong()},
                {"Kết nối",    tsktDetail.getKetNoi()},
            };
            for (String[] tsRow : tsRows) {
                if (tsRow[1] == null || tsRow[1].isEmpty()) continue;
                gc.gridx=0; gc.gridy=r; gc.weightx=0;
                JLabel lbl = new JLabel(tsRow[0]+":"); lbl.setFont(new Font("Segoe UI",Font.BOLD,12));
                lbl.setForeground(LAPTOP_SPEC_BORDER); lbl.setPreferredSize(new Dimension(130,22)); card.add(lbl,gc);
                gc.gridx=1; gc.weightx=1.0;
                JLabel val = new JLabel(tsRow[1]); val.setFont(FONT_NORMAL); val.setForeground(PRIMARY_DARK);
                card.add(val,gc); r++;
            }
        }

        JScrollPane sc = new JScrollPane(card); sc.setBorder(null); main.add(sc, BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER,10,10)); footer.setBackground(CONTENT_BG);
        footer.setBorder(new MatteBorder(1,0,0,0,new Color(180,210,240)));
        JButton btnEdit  = createActionButton("✏ Sửa sản phẩm", PRIMARY, WHITE);
        JButton btnClose = createActionButton("Đóng", new Color(90,100,115), WHITE);
        btnEdit.addActionListener(e -> { dialog.dispose(); openFormDialog(sp); });
        btnClose.addActionListener(e -> dialog.dispose());
        footer.add(btnEdit); footer.add(btnClose);
        main.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(main); dialog.setVisible(true);
    }

    private void doXoa() {
        int row = tableProduct.getSelectedRow();
        if (row<0) { showWarn("Vui lòng chọn sản phẩm cần ngừng bán!"); return; }
        SanPhamDTO sp = showProducts.get(row);
        int ok = JOptionPane.showConfirmDialog(this,
            "Xác nhận ngừng bán:\n\""+sp.getTenSP()+"\"?\n(Sản phẩm sẽ ẩn khỏi danh sách bán hàng)",
            "Xác nhận ngừng bán", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok==JOptionPane.YES_OPTION) {
            boolean kq = sanPhamBUS.xoaSanPham(sp.getMaSP());
            if (kq) { JOptionPane.showMessageDialog(this,"Đã ngừng bán: "+sp.getTenSP()); loadData(); }
            else    { showWarn("Ngừng bán thất bại!"); }
        }
    }

    // ─── UI Helpers ────────────────────────────────────────────────────
    private JTextField createFormField(String txt) {
        JTextField f = new JTextField(txt); f.setFont(FONT_NORMAL); f.setPreferredSize(new Dimension(0,32));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(180,210,240),1),BorderFactory.createEmptyBorder(4,8,4,8)));
        return f;
    }
    private JButton buildImgBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?bg.darker():bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(WHITE); btn.setFont(new Font("Segoe UI",Font.BOLD,12)); btn.setFocusPainted(false);
        btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.setPreferredSize(new Dimension(110,30)); return btn;
    }
    private JLabel  makeLbl(String txt) { JLabel l=new JLabel(txt); l.setFont(FONT_LABEL); l.setForeground(PRIMARY); return l; }
    private void    showWarn(String msg) { JOptionPane.showMessageDialog(this,msg,"Thông báo",JOptionPane.WARNING_MESSAGE); }
    private void    warnOn(Component p, String msg) { JOptionPane.showMessageDialog(p,msg,"Thông báo",JOptionPane.WARNING_MESSAGE); }
    private JButton createSmallButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?bg.darker():bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(fg); btn.setFont(FONT_LABEL); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
    }
    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton btn = createSmallButton(text,bg,fg); btn.setPreferredSize(new Dimension(140,36)); return btn;
    }
    private JButton createHeaderButton(String text, Color bg, Color fg) {
        final Font BF = new Font("Segoe UI",Font.BOLD,13);
        Canvas cv = new Canvas(); FontMetrics fm0 = cv.getFontMetrics(BF); final int w = fm0.stringWidth(text)+44;
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?bg.darker():bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setFont(BF); g2.setColor(fg); FontMetrics fm=g2.getFontMetrics();
                g2.drawString(text,(getWidth()-fm.stringWidth(text))/2,(getHeight()-fm.getHeight())/2+fm.getAscent()); g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(w,36); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
        };
        btn.setText(""); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
    }
    private String formatMoney(BigDecimal v) {
        if (v==null) return "0";
        return NumberFormat.getNumberInstance(Locale.US).format(v.longValue());
    }
}
