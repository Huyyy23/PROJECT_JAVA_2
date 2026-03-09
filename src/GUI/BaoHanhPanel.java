package GUI;

import BUS.BaoHanhBUS;
import BUS.NhanVienBUS;
import BUS.SERIALBUS;
import BUS.SanPhamBUS;
import DTO.BaoHanhDTO;
import DTO.HoaDonDTO;
import DTO.NhanVienDTO;
import DTO.SERIALDTO;
import DTO.SanPhamDTO;
import UTIL.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.Locale;

/**
 * BaoHanhPanel — Quản lý bảo hành hoàn chỉnh
 * Chức năng: Tạo phiếu | Tra cứu | Cập nhật | Lịch sử | In phiếu | Báo cáo | Xuất Excel/PDF
 */
public class BaoHanhPanel extends JPanel {

    // ── Colors ────────────────────────────────────────────
    private static final Color PRIMARY       = new Color(21, 101, 192);
    private static final Color PRIMARY_DARK  = new Color(10,  60, 130);
    private static final Color ACCENT        = new Color(0,  188, 212);
    private static final Color CONTENT_BG    = new Color(236, 242, 250);
    private static final Color WHITE         = Color.WHITE;
    private static final Color ROW_ALT       = new Color(245, 250, 255);
    private static final Color TABLE_HEADER  = new Color(21, 101, 192);
    private static final Color SUCCESS       = new Color(46,  125,  50);
    private static final Color DANGER        = new Color(198,  40,  40);
    private static final Color WARNING_COLOR = new Color(230, 120,   0);
    private static final Color BORDER_COLOR  = new Color(180, 210, 240);
    private static final Color STATUS_DANG   = new Color(255, 152,   0);
    private static final Color STATUS_GUI    = new Color(33,  150, 243);
    private static final Color STATUS_CHO    = new Color(156,  39, 176);
    private static final Color STATUS_XONG   = new Color(46,  125,  50);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_STAT   = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  13);

    private static final DateTimeFormatter DTF   = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat      NF    = NumberFormat.getNumberInstance(Locale.US);

    // ── BUS ───────────────────────────────────────────────────────────────────
    private final BaoHanhBUS       bus       = new BaoHanhBUS();
    private final BUS.LichSuBaoHanhBUS lichSuBUS = new BUS.LichSuBaoHanhBUS();
    private final NhanVienBUS nvBus     = new NhanVienBUS();
    private final SERIALBUS   serialBus = new SERIALBUS();
    private final SanPhamBUS  spBus     = new SanPhamBUS();

    // ── Data ─────────────────────────────────────────────────────────────────
    private List<BaoHanhDTO> allList  = new ArrayList<>();
    private List<BaoHanhDTO> showList = new ArrayList<>();

    // ── Cache tên ─────────────────────────────────────────────────────────────
    private final Map<Integer, String> cacheNV = new HashMap<>();
    private final Map<Integer, String> cacheSP = new HashMap<>();

    // ── Components ───────────────────────────────────────────────────────────
    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        txtSearch;
    private JComboBox<String> cbFilterTT;

    // ── Stat labels ──────────────────────────────────────────────────────────
    private JLabel lblTong, lblDangXuLy, lblDaGui, lblChoLK, lblDaTraKhach;

    private boolean dataLoaded = false;

    // =========================================================================
    public BaoHanhPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        buildUI();
        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) {
                if (!dataLoaded) { dataLoaded = true; loadData(); }
            }
        });
    }

    // =========================================================================
    // LOAD DATA (SwingWorker)
    // =========================================================================
    private void loadData() {
        cacheNV.clear(); cacheSP.clear();
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"…","Đang tải…","","","","","",""});

        SwingWorker<List<BaoHanhDTO>, Void> w = new SwingWorker<>() {
            @Override protected List<BaoHanhDTO> doInBackground() {
                List<BaoHanhDTO> data = bus.getAll();
                if (data == null) data = new ArrayList<>();
                for (BaoHanhDTO bh : data) prefetchNames(bh);
                return data;
            }
            @Override protected void done() {
                try {
                    allList = get();
                    applyFilter();
                    updateStats();
                } catch (Exception ex) {
                    tableModel.setRowCount(0);
                    tableModel.addRow(new Object[]{"","Lỗi: "+ex.getMessage(),"","","","","",""});
                }
            }
        };
        w.execute();
    }

    private void prefetchNames(BaoHanhDTO bh) {
        if (bh.getMaNVTiepNhan() != null && !cacheNV.containsKey(bh.getMaNVTiepNhan()))
            cacheNV.put(bh.getMaNVTiepNhan(), fetchTenNV(bh.getMaNVTiepNhan()));
        if (bh.getMaNVXuLy() != null && !cacheNV.containsKey(bh.getMaNVXuLy()))
            cacheNV.put(bh.getMaNVXuLy(), fetchTenNV(bh.getMaNVXuLy()));
        if (!cacheSP.containsKey(bh.getMaSP()))
            cacheSP.put(bh.getMaSP(), fetchTenSP(bh.getMaSP()));
    }

    // =========================================================================
    // FILTER & RENDER
    // =========================================================================
    private void applyFilter() {
        String kw     = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        String ttSel  = cbFilterTT != null ? (String) cbFilterTT.getSelectedItem() : "Tất cả";
        showList.clear();
        tableModel.setRowCount(0);

        for (BaoHanhDTO bh : allList) {
            if (ttSel != null && !ttSel.equals("Tất cả") && !mapTT(bh.getTrangThai()).equals(ttSel)) continue;
            String tenSP = cacheSP.getOrDefault(bh.getMaSP(), "SP#" + bh.getMaSP());
            String tenNV = bh.getMaNVTiepNhan() != null ? cacheNV.getOrDefault(bh.getMaNVTiepNhan(),"—") : "—";
            String combined = (bh.getMaBaoHanh() + " " + tenSP + " " + bh.getMoTaLoi() + " " + tenNV).toLowerCase();
            if (!kw.isEmpty() && !combined.contains(kw)) continue;

            showList.add(bh);
            tableModel.addRow(new Object[]{
                showList.size(),
                bh.getMaBaoHanh(),
                tenSP,
                bh.getMaHoaDon(),
                bh.getNgayTiepNhan() != null ? bh.getNgayTiepNhan().format(DTF) : "—",
                bh.getNgayHenTra()   != null ? bh.getNgayHenTra().format(DTF)   : "—",
                tenNV,
                bh.getTrangThai()
            });
        }
        updateStats();
    }

    private void updateStats() {
        int tong=allList.size(), dx=0, dg=0, clk=0, dtk=0;
        for (BaoHanhDTO b : allList) switch (b.getTrangThai() != null ? b.getTrangThai() : "") {
            case "DangXuLy"    -> dx++;
            case "DaGuiHang"   -> dg++;
            case "ChoLinhKien" -> clk++;
            case "DaTraKhach"  -> dtk++;
        }
        if (lblTong       != null) lblTong      .setText(String.valueOf(tong));
        if (lblDangXuLy   != null) lblDangXuLy  .setText(String.valueOf(dx));
        if (lblDaGui      != null) lblDaGui      .setText(String.valueOf(dg));
        if (lblChoLK      != null) lblChoLK      .setText(String.valueOf(clk));
        if (lblDaTraKhach != null) lblDaTraKhach .setText(String.valueOf(dtk));
    }

    // =========================================================================
    // BUILD UI
    // =========================================================================
    private void buildUI() {
        add(buildTitlePanel(),  BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
    }

    // ─── TITLE ────────────────────────────────────────────────────────────────
    private JPanel buildTitlePanel() {
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

        // Shield icon
        JComponent icon = new JComponent() {
            { setPreferredSize(new Dimension(36,58)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                int cx=getWidth()/2, cy=getHeight()/2-2;
                int[] px={cx-10,cx+10,cx+10,cx,cx-10};
                int[] py={cy-12,cy-12,cy+2,cy+12,cy+2};
                g2.fillPolygon(px,py,5);
                g2.setColor(PRIMARY);
                g2.setStroke(new BasicStroke(1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g2.drawLine(cx-4,cy-2,cx-1,cy+3);
                g2.drawLine(cx-1,cy+3,cx+5,cy-4);
                g2.dispose();
            }
        };

        JLabel title = new JLabel(" QUẢN LÝ BẢO HÀNH");
        title.setFont(FONT_TITLE); title.setForeground(WHITE);

        JPanel left = new JPanel(new GridBagLayout()); left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(0,14,0,0));
        GridBagConstraints lgc = new GridBagConstraints();
        lgc.anchor=GridBagConstraints.CENTER; lgc.insets=new Insets(0,0,0,4);
        lgc.gridx=0; left.add(icon,lgc);
        lgc.gridx=1; lgc.insets=new Insets(0,0,0,0); left.add(title,lgc);
        panel.add(left, BorderLayout.WEST);

        // Header buttons
        JPanel right = new JPanel(new GridBagLayout()); right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(0,0,0,16));
        GridBagConstraints rgc = new GridBagConstraints();
        rgc.anchor=GridBagConstraints.CENTER; rgc.insets=new Insets(0,6,0,0);

        JButton btnTao     = buildHeaderBtn("Tạo phiếu",        new Color(255,215,40), PRIMARY_DARK);
        JButton btnBaoCao  = buildHeaderBtn("Báo cáo",            new Color(141,54,255),  WHITE);
        JButton btnXuatEx  = buildHeaderBtn("Xuất Excel",         new Color(67,160,71),  WHITE);
        JButton btnXuatPDF = buildHeaderBtn("Xuất PDF",           DANGER,                WHITE);
        JButton btnRefresh = buildHeaderBtn("Làm mới",         new Color(90,100,115), WHITE);

        btnTao    .addActionListener(e -> showTaoPhieuDialog());
        btnBaoCao .addActionListener(e -> showBaoCaoDialog());
        btnXuatEx .addActionListener(e -> xuatExcel());
        btnXuatPDF.addActionListener(e -> xuatPDF());
        btnRefresh.addActionListener(e -> { dataLoaded=false; loadData(); });

        rgc.gridx=0; right.add(btnTao,rgc);
        rgc.gridx=1; right.add(btnBaoCao,rgc);
        rgc.gridx=2; right.add(btnXuatEx,rgc);
        rgc.gridx=3; right.add(btnXuatPDF,rgc);
        rgc.gridx=4; right.add(btnRefresh,rgc);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // ─── CENTER ───────────────────────────────────────────────────────────────
    private JPanel buildCenterPanel() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(CONTENT_BG);
        p.add(buildStatsRow(),  BorderLayout.NORTH);
        p.add(buildTableArea(), BorderLayout.CENTER);
        return p;
    }

    // ─── STATS ROW (5 card) ───────────────────────────────────────────────────
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1,5,10,0));
        row.setOpaque(false); row.setPreferredSize(new Dimension(0,82));

        lblTong       = makeStatVal(ACCENT);
        lblDangXuLy   = makeStatVal(STATUS_DANG);
        lblDaGui      = makeStatVal(STATUS_GUI);
        lblChoLK      = makeStatVal(STATUS_CHO);
        lblDaTraKhach = makeStatVal(STATUS_XONG);

        row.add(buildStatCard("Tổng phiếu",     lblTong,       ACCENT,       0));
        row.add(buildStatCard("Đang xử lý",     lblDangXuLy,   STATUS_DANG,  1));
        row.add(buildStatCard("Đã gửi hàng",    lblDaGui,      STATUS_GUI,   2));
        row.add(buildStatCard("Chờ linh kiện",  lblChoLK,      STATUS_CHO,   3));
        row.add(buildStatCard("Đã trả khách",   lblDaTraKhach, STATUS_XONG,  4));
        return row;
    }

    private JLabel makeStatVal(Color c) {
        JLabel l=new JLabel("0"); l.setFont(FONT_STAT); l.setForeground(c); return l;
    }

    private JPanel buildStatCard(String label, JLabel val, Color accent, int iconType) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,18));
                g2.fillRoundRect(4,4,getWidth()-4,getHeight()-4,14,14);
                g2.setColor(WHITE);
                g2.fillRoundRect(0,0,getWidth()-4,getHeight()-4,14,14);
                g2.setColor(accent);
                g2.fillRoundRect(0,0,6,getHeight()-4,6,6);
                g2.fillRect(3,0,3,getHeight()-4);
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setOpaque(false); card.setLayout(new GridBagLayout());
        JComponent ico = new JComponent() {
            { setPreferredSize(new Dimension(44,44)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int s=34,ox=(getWidth()-s)/2,oy=(getHeight()-s)/2;
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),28));
                g2.fillOval(ox-4,oy-4,s+8,s+8);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                int cx=getWidth()/2,cy=getHeight()/2;
                switch(iconType){
                    case 0->{ int[]px={cx-8,cx+8,cx+8,cx,cx-8},py={cy-10,cy-10,cy+2,cy+10,cy+2}; g2.fillPolygon(px,py,5); }
                    case 1->{ g2.drawOval(cx-12,cy-12,24,24); g2.drawLine(cx-5,cy,cx-1,cy+5); g2.drawLine(cx-1,cy+5,cx+7,cy-5); }
                    case 2->{ g2.drawRect(cx-10,cy-8,20,16); g2.drawLine(cx-10,cy-1,cx+10,cy-1); g2.drawLine(cx-3,cy-8,cx-3,cy-1); g2.drawLine(cx+3,cy-8,cx+3,cy-1); }
                    case 3->{ g2.drawOval(cx-11,cy-11,22,22); g2.drawLine(cx,cy-5,cx,cy+1); g2.fillOval(cx-2,cy+4,4,4); }
                    case 4->{ g2.drawOval(cx-12,cy-12,24,24); g2.drawLine(cx-6,cy,cx-1,cy+6); g2.drawLine(cx-1,cy+6,cx+8,cy-5); }
                }
                g2.dispose();
            }
        };
        JLabel lbl=new JLabel(label); lbl.setFont(new Font("Segoe UI",Font.PLAIN,12)); lbl.setForeground(new Color(100,120,150));
        JPanel tc=new JPanel(); tc.setLayout(new BoxLayout(tc,BoxLayout.Y_AXIS)); tc.setOpaque(false);
        val.setAlignmentX(Component.LEFT_ALIGNMENT); lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        tc.add(val); tc.add(Box.createVerticalStrut(3)); tc.add(lbl);
        GridBagConstraints gc=new GridBagConstraints();
        gc.anchor=GridBagConstraints.CENTER; gc.insets=new Insets(0,12,0,8);
        gc.gridx=0; card.add(ico,gc);
        gc.gridx=1; gc.insets=new Insets(0,0,0,10); card.add(tc,gc);
        return card;
    }

    // ─── TABLE AREA ───────────────────────────────────────────────────────────
    private JPanel buildTableArea() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,15));
                g2.fillRoundRect(4,4,getWidth()-4,getHeight()-4,14,14);
                g2.setColor(WHITE);
                g2.fillRoundRect(0,0,getWidth()-4,getHeight()-4,14,14);
                g2.dispose(); super.paintComponent(g);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0,0,4,4));
        wrapper.add(buildFilterBar(),  BorderLayout.NORTH);
        wrapper.add(buildTablePanel(), BorderLayout.CENTER);
        return wrapper;
    }

    // ─── FILTER BAR ───────────────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(0,6));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12,14,8,14));

        // Search
        JPanel topRow = new JPanel(new BorderLayout(8,0)); topRow.setOpaque(false);
        JPanel searchBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(BORDER_COLOR); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.dispose(); super.paintComponent(g);
            }
        };
        searchBar.setOpaque(false); searchBar.setPreferredSize(new Dimension(0,36));

        JComponent searchIcon = new JComponent() {
            { setPreferredSize(new Dimension(38,36)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=14,cy=getHeight()/2-1,r=7;
                g2.setColor(new Color(160,185,220)); g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g2.drawOval(cx-r,cy-r,r*2,r*2); g2.drawLine(cx+r-2,cy+r-2,cx+r+4,cy+r+4); g2.dispose();
            }
        };
        txtSearch = new JTextField(); txtSearch.setFont(FONT_NORMAL);
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0,4,0,0)); txtSearch.setOpaque(false);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){applyFilter();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){applyFilter();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){applyFilter();}
        });
        JButton btnFind = createSmallBtn("Tìm", PRIMARY, WHITE); btnFind.setPreferredSize(new Dimension(80,36));
        btnFind.addActionListener(e->applyFilter());
        searchBar.add(searchIcon,BorderLayout.WEST); searchBar.add(txtSearch,BorderLayout.CENTER); searchBar.add(btnFind,BorderLayout.EAST);

        // Right controls
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); right.setOpaque(false);
        JLabel lblSt=new JLabel("Trạng thái:"); lblSt.setFont(FONT_LABEL); lblSt.setForeground(PRIMARY);
        cbFilterTT = new JComboBox<>(new String[]{"Tất cả","Đang xử lý","Đã gửi hàng","Chờ linh kiện","Đã trả khách"});
        cbFilterTT.setFont(FONT_NORMAL); cbFilterTT.setPreferredSize(new Dimension(175,36));
        cbFilterTT.addActionListener(e->applyFilter());
        JButton btnReset=createSmallBtn("Làm mới",new Color(90,100,115),WHITE); btnReset.setPreferredSize(new Dimension(100,36));
        btnReset.addActionListener(e->{ txtSearch.setText(""); cbFilterTT.setSelectedIndex(0); loadData(); });
        right.add(lblSt); right.add(cbFilterTT); right.add(Box.createHorizontalStrut(4)); right.add(btnReset);
        topRow.add(searchBar,BorderLayout.CENTER); topRow.add(right,BorderLayout.EAST);
        bar.add(topRow, BorderLayout.NORTH);
        return bar;
    }

    // ─── TABLE PANEL ──────────────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0,10,12,10));

        String[] cols={"STT","Mã BH","Sản phẩm","Mã HĐ","Ngày TN","Ngày hẹn","NV tiếp nhận","Trạng thái"};
        tableModel = new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };

        table = new JTable(tableModel){
            @Override public Component prepareRenderer(TableCellRenderer r,int row,int col){
                Component c=super.prepareRenderer(r,row,col);
                if (!isRowSelected(row)) c.setBackground(row%2==0?WHITE:ROW_ALT);
                else c.setBackground(new Color(187,222,251));
                c.setFont(FONT_NORMAL);
                c.setForeground(isRowSelected(row)?PRIMARY_DARK:new Color(30,50,80));
                if (c instanceof JLabel lbl){
                    lbl.setHorizontalAlignment(col<=3?SwingConstants.CENTER:SwingConstants.LEFT);
                    if (col==cols.length-1){
                        String v=tableModel.getValueAt(row,col)!=null?tableModel.getValueAt(row,col).toString():"";
                        lbl.setFont(new Font("Segoe UI",Font.BOLD,12));
                        switch(v){
                            case"DangXuLy"   ->lbl.setForeground(STATUS_DANG);
                            case"DaGuiHang"  ->lbl.setForeground(STATUS_GUI);
                            case"ChoLinhKien"->lbl.setForeground(STATUS_CHO);
                            case"DaTraKhach" ->lbl.setForeground(STATUS_XONG);
                        }
                    }
                }
                return c;
            }
        };
        table.setRowHeight(36); table.setFont(FONT_NORMAL);
        table.setGridColor(new Color(220,230,245)); table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(0,1));
        table.setFillsViewportHeight(true); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header=table.getTableHeader();
        header.setPreferredSize(new Dimension(0,40)); header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                JLabel lbl=(JLabel)super.getTableCellRendererComponent(t,v,s,f,r,c);
                lbl.setBackground(TABLE_HEADER); lbl.setForeground(WHITE); lbl.setFont(FONT_HEADER);
                lbl.setHorizontalAlignment(SwingConstants.CENTER); lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,0,1,new Color(60,120,200)),
                    BorderFactory.createEmptyBorder(0,8,0,8)));
                return lbl;
            }
        });
        int[] cw={38,55,200,60,105,105,150,120};
        for(int i=0;i<cw.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);

        // Double click → chi tiết
        table.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){
                if(e.getClickCount()==2) { BaoHanhDTO bh=getSelectedBH(); if(bh!=null) showChiTietDialog(bh); }
            }
        });

        JScrollPane scroll=new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER_COLOR,1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Action bar
        JPanel actionBar=new JPanel(new BorderLayout()); actionBar.setOpaque(false);
        actionBar.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));
        JLabel lblCount=new JLabel("0 / 0 phiếu");
        lblCount.setFont(new Font("Segoe UI",Font.ITALIC,12)); lblCount.setForeground(new Color(100,120,150));
        tableModel.addTableModelListener(e2->lblCount.setText(showList.size()+" / "+allList.size()+" phiếu"));

        JPanel btnPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btnPanel.setOpaque(false);
        JButton btnChiTiet  = createActionBtn("Chi tiết",   new Color(90,60,180), WHITE);
        JButton btnCapNhat  = createActionBtn("Cập nhật",   new Color(0,150,136), WHITE);
        JButton btnLichSu   = createActionBtn("Lịch sử",    new Color(33,150,243),WHITE);
        JButton btnInPhieu  = createActionBtn("In phiếu",   new Color(100,100,100),WHITE);
        JButton btnXoa      = createActionBtn("Xóa",         DANGER,              WHITE);

        btnChiTiet.addActionListener(e->{ BaoHanhDTO bh=getSelectedBH(); if(bh==null){warn("Chọn phiếu!");return;} showChiTietDialog(bh); });
        btnCapNhat.addActionListener(e->{ BaoHanhDTO bh=getSelectedBH(); if(bh==null){warn("Chọn phiếu!");return;} showCapNhatDialog(bh); });
        btnLichSu .addActionListener(e->{ BaoHanhDTO bh=getSelectedBH(); if(bh==null){warn("Chọn phiếu!");return;} showLichSuDialog(bh); });
        btnInPhieu.addActionListener(e->{ BaoHanhDTO bh=getSelectedBH(); if(bh==null){warn("Chọn phiếu!");return;} inPhieu(bh); });
        btnXoa    .addActionListener(e->doXoa());

        for(JButton b:new JButton[]{btnChiTiet,btnCapNhat,btnLichSu,btnInPhieu,btnXoa}) btnPanel.add(b);
        actionBar.add(lblCount,BorderLayout.WEST); actionBar.add(btnPanel,BorderLayout.EAST);
        panel.add(scroll,BorderLayout.CENTER); panel.add(actionBar,BorderLayout.SOUTH);
        return panel;
    }

    // =========================================================================
    // DIALOG: TẠO PHIẾU BẢO HÀNH
    // =========================================================================
    private void showTaoPhieuDialog() {
        JDialog dlg = makeDialog("Tạo Phiếu Bảo Hành Mới", 820, 740);

        // ── State ─────────────────────────────────────────────────────────────
        final int[] maHoaDonRef={-1}, maSPRef={-1}, maSerialRef={-1};
        final boolean[] valid={false};

        // ── BƯỚC 1: Bảng chọn serial ─────────────────────────────────────────
        JPanel pStep1 = makeStepPanel("Bước 1 — Chọn Serial cần bảo hành  (chỉ hiện serial đã bán, còn hạn BH)");

        // Search bar trong step1
        JTextField txtFilter = makeFormField("");
        txtFilter.setPreferredSize(new Dimension(0, 30));
        JButton btnLoc = createActionBtn("Lọc", PRIMARY, WHITE);
        JPanel filterRow = new JPanel(new BorderLayout(6,0)); filterRow.setBackground(WHITE);
        filterRow.add(new JLabel("  🔍 "), BorderLayout.WEST);
        filterRow.add(txtFilter, BorderLayout.CENTER);
        filterRow.add(btnLoc, BorderLayout.EAST);
        addStepRow(pStep1, "Tìm serial/SP:", filterRow);

        // Bảng serial
        String[] serialCols = {"Serial Code","Tên Sản Phẩm","BH (tháng)","Ngày Bán","Hết Hạn BH","Khách Hàng"};
        DefaultTableModel serialModel = new DefaultTableModel(serialCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable serialTable = new JTable(serialModel);
        serialTable.setFont(FONT_NORMAL); serialTable.setRowHeight(32);
        serialTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serialTable.setGridColor(new Color(220,230,245));
        serialTable.setShowVerticalLines(true);
        serialTable.setFillsViewportHeight(true);
        serialTable.setSelectionBackground(new Color(187,222,251));
        serialTable.setSelectionForeground(PRIMARY_DARK);
        JTableHeader sh = serialTable.getTableHeader();
        sh.setPreferredSize(new Dimension(0,36));
        sh.setReorderingAllowed(false);
        sh.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,val,isSel,hasFocus,row,col);
                lbl.setBackground(TABLE_HEADER); lbl.setForeground(WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setHorizontalAlignment(SwingConstants.CENTER); lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,0,1,new Color(60,120,200)),
                    BorderFactory.createEmptyBorder(0,8,0,8)));
                return lbl;
            }
        });
        int[] scw = {110,200,80,95,95,140};
        for(int i=0;i<scw.length;i++) serialTable.getColumnModel().getColumn(i).setPreferredWidth(scw[i]);

        JScrollPane serialScroll = new JScrollPane(serialTable);
        serialScroll.setBorder(new LineBorder(BORDER_COLOR,1));
        serialScroll.setPreferredSize(new Dimension(0, 170));

        JLabel lblHint = new JLabel("  Chưa tải dữ liệu...");
        lblHint.setFont(new Font("Segoe UI",Font.ITALIC,11));
        lblHint.setForeground(new Color(120,140,170));

        JPanel tableWrap = new JPanel(new BorderLayout(0,4)); tableWrap.setBackground(WHITE);
        tableWrap.add(serialScroll, BorderLayout.CENTER);
        tableWrap.add(lblHint, BorderLayout.SOUTH);
        addStepRow(pStep1, "Danh sách:", tableWrap);

        // ── BƯỚC 2: Thông tin (readonly, tự điền khi chọn serial) ────────────
        JPanel pStep2 = makeStepPanel("Bước 2 — Thông tin tra cứu  (tự động điền khi chọn serial)");
        JTextField txtTenSP   = makeReadField(""); JTextField txtMaHD    = makeReadField("");
        JTextField txtNgayMua = makeReadField(""); JTextField txtThoiHan = makeReadField("");
        JTextField txtHetHan  = makeReadField(""); JTextField txtKhachHang = makeReadField("");
        addStepRow(pStep2, "Sản phẩm:",    txtTenSP);
        addStepRow(pStep2, "Mã hóa đơn:", txtMaHD);
        addStepRow(pStep2, "Ngày mua:",    txtNgayMua);
        addStepRow(pStep2, "Thời hạn BH:",txtThoiHan);
        addStepRow(pStep2, "Hết hạn BH:", txtHetHan);
        addStepRow(pStep2, "Khách hàng:", txtKhachHang);

        // ── BƯỚC 3: Thông tin phiếu BH ───────────────────────────────────────
        JPanel pStep3 = makeStepPanel("Bước 3 — Thông tin phiếu bảo hành");
        JTextArea txtMoTa = new JTextArea(3,20);
        txtMoTa.setFont(FONT_NORMAL); txtMoTa.setLineWrap(true); txtMoTa.setWrapStyleWord(true);
        txtMoTa.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR,1),BorderFactory.createEmptyBorder(5,8,5,8)));
        JScrollPane moTaScr = new JScrollPane(txtMoTa); moTaScr.setPreferredSize(new Dimension(0,65));
        JComboBox<String> cbHinhThuc = makeCombo(new String[]{"SuaChuaTaiCho","GuiHang","ThayTheMoi"});
        JTextField txtNgayHen = makeFormField(LocalDate.now().plusDays(7).format(DTF));

        List<NhanVienDTO> dsNV = new ArrayList<>();
        try { dsNV = nvBus.getAll(); } catch(Exception ignored){}
        JComboBox<String> cbNV = new JComboBox<>(); cbNV.setFont(FONT_NORMAL);
        cbNV.addItem("— Không chọn —");
        final int[] nvIds = new int[dsNV.size()];
        for(int i=0;i<dsNV.size();i++){
            cbNV.addItem(dsNV.get(i).getTenNV()+" (#"+dsNV.get(i).getMaNV()+")");
            nvIds[i] = dsNV.get(i).getMaNV();
        }
        addStepRow(pStep3,"Mô tả lỗi (*):",   moTaScr);
        addStepRow(pStep3,"Hình thức xử lý:", cbHinhThuc);
        addStepRow(pStep3,"Ngày hẹn trả:",    txtNgayHen);
        addStepRow(pStep3,"NV tiếp nhận:",    cbNV);

        // ── Layout ────────────────────────────────────────────────────────────
        JPanel content = new JPanel(new GridBagLayout()); content.setBackground(WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(14,22,14,22));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill=GridBagConstraints.HORIZONTAL; gc.insets=new Insets(6,4,6,4); gc.weightx=1;
        gc.gridx=0; gc.gridy=0; content.add(pStep1,gc);
        gc.gridy=1; content.add(pStep2,gc);
        gc.gridy=2; content.add(pStep3,gc);
        JScrollPane scr = new JScrollPane(content); scr.setBorder(null);
        dlg.add(scr, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
        footerPanel.setBackground(CONTENT_BG);
        footerPanel.setBorder(new MatteBorder(1,0,0,0,BORDER_COLOR));
        JButton btnHuyPhieu  = createActionBtn("Hủy bỏ",   new Color(90,100,115), WHITE);
        JButton btnTaoPhieu  = createActionBtn("Tạo phiếu", SUCCESS,              WHITE);
        btnTaoPhieu.setPreferredSize(new Dimension(140,36));
        btnHuyPhieu.addActionListener(e -> dlg.dispose());
        footerPanel.add(btnHuyPhieu); footerPanel.add(btnTaoPhieu);
        dlg.add(footerPanel, BorderLayout.SOUTH);

        // ── Load danh sách serial hợp lệ từ DB ───────────────────────────────
        // Dùng inner class để lưu dữ liệu gốc cho filter
        final List<Object[]> allSerialRows = new ArrayList<>();

        Runnable loadSerialList = () -> {
            allSerialRows.clear();
            serialModel.setRowCount(0);
            // Query: serial DaBan + chưa có phiếu BH đang mở + còn hạn BH
            String sql =
                "SELECT s.MaSerial, s.SerialCode, sp.TenSP, sp.ThoiHanBaoHanhThang, " +
                "       h.MaHoaDon, h.NgayLap, h.MaKhachHang, kh.TenKhachHang " +
                "FROM SERIAL s " +
                "JOIN SANPHAM sp ON sp.MaSP = s.MaSP " +
                "JOIN CHITIETHOADON ct ON ct.MaSerial = s.MaSerial " +
                "JOIN HOADON h ON h.MaHoaDon = ct.MaHoaDon AND h.TrangThai = 'HoanThanh' " +
                "LEFT JOIN KHACHHANG kh ON kh.MaKhachHang = h.MaKhachHang " +
                "WHERE s.TrangThai = 'DaBan' " +
                "  AND sp.ThoiHanBaoHanhThang > 0 " +
                "  AND NOT EXISTS ( " +
                "      SELECT 1 FROM BAOHANH bh " +
                "      WHERE bh.MaSerial = s.MaSerial " +
                "        AND bh.TrangThai <> 'DaTraKhach' " +
                "  ) " +
                "ORDER BY h.NgayLap DESC";
            try (java.sql.Connection cn = UTIL.DBConnection.getConnection();
                 java.sql.PreparedStatement ps = cn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    int maSerial   = rs.getInt("MaSerial");
                    String code    = rs.getString("SerialCode");
                    String tenSP2  = rs.getString("TenSP");
                    int bh         = rs.getInt("ThoiHanBaoHanhThang");
                    int maHD       = rs.getInt("MaHoaDon");
                    java.sql.Timestamp ts = rs.getTimestamp("NgayLap");
                    LocalDate ngayBan = ts != null ? ts.toLocalDateTime().toLocalDate() : null;
                    LocalDate hetHan2 = ngayBan != null ? ngayBan.plusMonths(bh) : null;
                    // Bỏ qua nếu hết hạn
                    if (hetHan2 != null && LocalDate.now().isAfter(hetHan2)) continue;
                    String tenKH = rs.getString("TenKhachHang");
                    if (tenKH == null) tenKH = "Khách lẻ";
                    String ngayBanStr  = ngayBan  != null ? ngayBan.format(DTF)  : "—";
                    String hetHanStr   = hetHan2  != null ? hetHan2.format(DTF)  : "—";
                    Object[] row = {code, tenSP2, bh+" tháng", ngayBanStr, hetHanStr, tenKH,
                                    maSerial, maHD, tenSP2, bh, ngayBan}; // extra cols for state
                    allSerialRows.add(row);
                    serialModel.addRow(new Object[]{code, tenSP2, bh+" tháng", ngayBanStr, hetHanStr, tenKH});
                    count++;
                }
                lblHint.setText("  " + count + " serial hợp lệ — click chọn, double-click để xác nhận");
            } catch (Exception ex) {
                lblHint.setText("  Lỗi tải serial: " + ex.getMessage());
            }
        };

        // ── Filter danh sách khi gõ ───────────────────────────────────────────
        Runnable applySerialFilter = () -> {
            String kw = txtFilter.getText().trim().toLowerCase();
            serialModel.setRowCount(0);
            int count = 0;
            for (Object[] r : allSerialRows) {
                String code2   = r[0].toString().toLowerCase();
                String tenSP2  = r[1].toString().toLowerCase();
                String tenKH2  = r[5].toString().toLowerCase();
                if (kw.isEmpty() || code2.contains(kw) || tenSP2.contains(kw) || tenKH2.contains(kw)) {
                    serialModel.addRow(new Object[]{r[0],r[1],r[2],r[3],r[4],r[5]});
                    count++;
                }
            }
            lblHint.setText("  " + count + " serial phù hợp");
        };
        btnLoc.addActionListener(e -> applySerialFilter.run());
        txtFilter.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applySerialFilter.run(); }
        });

        // ── Khi chọn 1 dòng trong bảng → tự điền Bước 2 ─────────────────────
        Runnable onSerialSelected = () -> {
            int row = serialTable.getSelectedRow();
            if (row < 0) return;
            // Tìm lại dòng gốc từ allSerialRows theo serialCode (col 0)
            String selectedCode = serialModel.getValueAt(row, 0).toString();
            Object[] orig = allSerialRows.stream()
                .filter(r -> r[0].toString().equals(selectedCode))
                .findFirst().orElse(null);
            if (orig == null) return;

            int maSerial2  = (int) orig[6];
            int maHD2      = (int) orig[7];
            String tenSP2  = (String) orig[8];
            int thoiHan    = (int) orig[9];
            LocalDate ngayBan = (LocalDate) orig[10];
            LocalDate hetHan2 = ngayBan != null ? ngayBan.plusMonths(thoiHan) : null;

            // Lấy tên khách hàng
            String tenKH2 = orig[5].toString();

            txtTenSP    .setText(tenSP2);
            txtMaHD     .setText(String.valueOf(maHD2));
            txtNgayMua  .setText(ngayBan  != null ? ngayBan.format(DTF)  : "—");
            txtThoiHan  .setText(thoiHan + " tháng");
            txtHetHan   .setText(hetHan2  != null ? hetHan2.format(DTF)  : "—");
            txtKhachHang.setText(tenKH2);

            maSerialRef[0] = maSerial2;
            maSPRef[0]     = -1; // lấy từ serial qua DB đã join sẵn
            maHoaDonRef[0] = maHD2;
            valid[0]       = true;

            // Lấy maSP thực từ SERIAL table
            try (java.sql.Connection cn = UTIL.DBConnection.getConnection();
                 java.sql.PreparedStatement ps = cn.prepareStatement(
                     "SELECT MaSP FROM SERIAL WHERE MaSerial=?")) {
                ps.setInt(1, maSerial2);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) maSPRef[0] = rs.getInt("MaSP");
                }
            } catch (Exception ignored) {}
        };

        serialTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSerialSelected.run();
        });
        // Double-click → focus xuống Bước 3
        serialTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onSerialSelected.run();
                    txtMoTa.requestFocusInWindow();
                }
            }
        });

        // ── Action Tạo phiếu ─────────────────────────────────────────────────
        btnTaoPhieu.addActionListener(e -> {
            if (!valid[0]) {
                JOptionPane.showMessageDialog(dlg,"Vui lòng chọn serial từ danh sách trước!","",JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtMoTa.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg,"Nhập mô tả lỗi!","",JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                LocalDate ngayHen = LocalDate.parse(txtNgayHen.getText().trim(), DTF);
                if (ngayHen.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(dlg,"Ngày hẹn không được trước hôm nay!","",JOptionPane.WARNING_MESSAGE);
                    return;
                }
                BaoHanhDTO bh = new BaoHanhDTO();
                bh.setMaIMEI(maSerialRef[0]);
                bh.setMaSP(maSPRef[0]);
                bh.setMaHoaDon(maHoaDonRef[0]);
                bh.setNgayTiepNhan(LocalDate.now());
                bh.setNgayHenTra(ngayHen);
                bh.setMoTaLoi(txtMoTa.getText().trim());
                bh.setHinhThucXuLy((String) cbHinhThuc.getSelectedItem());
                bh.setTrangThai("DangXuLy");
                int selNV = cbNV.getSelectedIndex();
                if (selNV > 0) bh.setMaNVTiepNhan(nvIds[selNV-1]);
                int maBH = bus.them(bh);
                // Ghi lịch sử: tạo phiếu
                int maNVGhi = (selNV > 0) ? nvIds[selNV-1] : -1;
                lichSuBUS.ghiLichSu(maBH,
                    maNVGhi > 0 ? maNVGhi : null,
                    null, "DangXuLy",
                    "Tạo phiếu bảo hành mới");
                JOptionPane.showMessageDialog(dlg,
                    "Tạo phiếu bảo hành thành công!\nMã phiếu: #"+maBH,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose(); loadData();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dlg,"Ngày hẹn không đúng định dạng dd/MM/yyyy!","",JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg,"Lỗi: "+ex.getMessage(),"",JOptionPane.ERROR_MESSAGE);
            }
        });

        // Load serial ngay khi mở dialog
        SwingUtilities.invokeLater(() -> loadSerialList.run());

        dlg.setVisible(true);
    }

    // =========================================================================
    // DIALOG: CHI TIẾT
    // =========================================================================
    private void showChiTietDialog(BaoHanhDTO bh) {
        JDialog dlg = makeDialog("Chi tiết Phiếu BH #"+bh.getMaBaoHanh(), 560, 520);
        JPanel content=new JPanel(new GridBagLayout()); content.setBackground(WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(16,24,16,24));
        GridBagConstraints gc=new GridBagConstraints(); gc.fill=GridBagConstraints.HORIZONTAL; gc.insets=new Insets(5,4,5,4);

        String tenSP = cacheSP.getOrDefault(bh.getMaSP(),"SP#"+bh.getMaSP());
        String nvTN  = bh.getMaNVTiepNhan()!=null?cacheNV.getOrDefault(bh.getMaNVTiepNhan(),"—"):"—";
        String nvXL  = bh.getMaNVXuLy()!=null?cacheNV.getOrDefault(bh.getMaNVXuLy(),"—"):"—";

        addDetailRow2(content,gc,0,"Mã phiếu BH:",String.valueOf(bh.getMaBaoHanh()),"Mã hóa đơn:",String.valueOf(bh.getMaHoaDon()));
        addDetailRow2(content,gc,1,"Sản phẩm:",tenSP,"Mã Serial:",bh.getMaIMEI()!=null?String.valueOf(bh.getMaIMEI()):"—");
        addDetailRow2(content,gc,2,"Ngày tiếp nhận:",bh.getNgayTiepNhan()!=null?bh.getNgayTiepNhan().format(DTF):"—","Ngày hẹn trả:",bh.getNgayHenTra()!=null?bh.getNgayHenTra().format(DTF):"—");
        addDetailRow2(content,gc,3,"Ngày trả thực:",bh.getNgayTra()!=null?bh.getNgayTra().format(DTF):"—","Hình thức XL:",fmtHT(bh.getHinhThucXuLy()));
        addDetailRow2(content,gc,4,"NV tiếp nhận:",nvTN,"NV xử lý:",nvXL);
        addDetailRow2(content,gc,5,"Chi phí PS:",bh.getChiPhiPhatSinh()!=null?NF.format(bh.getChiPhiPhatSinh())+" đ":"0 đ","Trạng thái:",mapTT(bh.getTrangThai()));

        // Mô tả lỗi full
        gc.gridx=0; gc.gridy=6; gc.gridwidth=1; gc.weightx=0.25;
        JLabel lMT=new JLabel("Mô tả lỗi:"); lMT.setFont(FONT_LABEL); lMT.setForeground(PRIMARY); content.add(lMT,gc);
        gc.gridx=1; gc.gridwidth=3; gc.weightx=0.75;
        JTextArea taMT=new JTextArea(bh.getMoTaLoi()!=null?bh.getMoTaLoi():"");
        taMT.setFont(FONT_NORMAL); taMT.setEditable(false); taMT.setLineWrap(true); taMT.setBackground(new Color(245,248,255));
        taMT.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        JScrollPane scMT=new JScrollPane(taMT); scMT.setPreferredSize(new Dimension(0,60)); content.add(scMT,gc); gc.gridwidth=1;

        // Kết quả xử lý
        gc.gridx=0; gc.gridy=7; gc.weightx=0.25;
        JLabel lKQ=new JLabel("Kết quả XL:"); lKQ.setFont(FONT_LABEL); lKQ.setForeground(PRIMARY); content.add(lKQ,gc);
        gc.gridx=1; gc.gridwidth=3; gc.weightx=0.75;
        JTextArea taKQ=new JTextArea(bh.getKetQuaXuLy()!=null?bh.getKetQuaXuLy():"");
        taKQ.setFont(FONT_NORMAL); taKQ.setEditable(false); taKQ.setLineWrap(true); taKQ.setBackground(new Color(245,248,255));
        taKQ.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        JScrollPane scKQ=new JScrollPane(taKQ); scKQ.setPreferredSize(new Dimension(0,55)); content.add(scKQ,gc); gc.gridwidth=1;

        JScrollPane sc=new JScrollPane(content); sc.setBorder(null);
        dlg.add(sc, BorderLayout.CENTER);

        JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
        footer.setBackground(CONTENT_BG); footer.setBorder(new MatteBorder(1,0,0,0,BORDER_COLOR));
        JButton btnCapNhat=createActionBtn("Cập nhật",new Color(0,150,136),WHITE);
        JButton btnIn=createActionBtn("In phiếu",new Color(100,100,100),WHITE);
        JButton btnXoa=createActionBtn("Xóa phiếu",DANGER,WHITE);
        JButton btnDong=createActionBtn("Đóng",new Color(90,100,115),WHITE);
        btnCapNhat.addActionListener(e->{dlg.dispose();showCapNhatDialog(bh);});
        btnIn.addActionListener(e->inPhieu(bh));
        btnXoa.addActionListener(e->{ dlg.dispose(); doXoaDirect(bh); });
        btnDong.addActionListener(e->dlg.dispose());
        footer.add(btnCapNhat); footer.add(btnIn); footer.add(btnXoa); footer.add(btnDong);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    // DIALOG: CẬP NHẬT TRẠNG THÁI
    // =========================================================================
    private void showCapNhatDialog(BaoHanhDTO bh) {
        JDialog dlg = makeDialog("Cập Nhật — Phiếu BH #"+bh.getMaBaoHanh(), 520, 440);
        JPanel content=new JPanel(new GridBagLayout()); content.setBackground(WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(18,26,18,26));
        GridBagConstraints gc=new GridBagConstraints(); gc.fill=GridBagConstraints.HORIZONTAL; gc.insets=new Insets(7,5,7,5);

        JTextField tfMaBH=makeReadField(String.valueOf(bh.getMaBaoHanh()));
        JTextField tfSP=makeReadField(cacheSP.getOrDefault(bh.getMaSP(),"SP#"+bh.getMaSP()));
        JTextField tfTTCu=makeReadField(mapTT(bh.getTrangThai()));

        JComboBox<String> cbTT=makeCombo(new String[]{"DangXuLy","DaGuiHang","ChoLinhKien","DaTraKhach"});
        cbTT.setSelectedItem(bh.getTrangThai());

        // Combo NV xử lý
        List<NhanVienDTO> dsNV=new ArrayList<>();
        try{dsNV=nvBus.getAll();}catch(Exception ignored){}
        JComboBox<String> cbNVXL=new JComboBox<>(); cbNVXL.setFont(FONT_NORMAL); cbNVXL.addItem("— Không đổi —");
        final int[] nvIds=new int[dsNV.size()];
        for(int i=0;i<dsNV.size();i++){cbNVXL.addItem(dsNV.get(i).getTenNV()+" (#"+dsNV.get(i).getMaNV()+")"); nvIds[i]=dsNV.get(i).getMaNV();}
        if(bh.getMaNVXuLy()!=null){ for(int i=0;i<dsNV.size();i++){if(dsNV.get(i).getMaNV()==bh.getMaNVXuLy()){cbNVXL.setSelectedIndex(i+1);break;}}}

        JTextArea txtKetQua=new JTextArea(3,20); txtKetQua.setFont(FONT_NORMAL); txtKetQua.setLineWrap(true); txtKetQua.setWrapStyleWord(true);
        txtKetQua.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR,1),BorderFactory.createEmptyBorder(5,8,5,8)));
        if(bh.getKetQuaXuLy()!=null) txtKetQua.setText(bh.getKetQuaXuLy());
        JScrollPane scKQ=new JScrollPane(txtKetQua); scKQ.setPreferredSize(new Dimension(0,70));

        JTextField txtChiPhi=makeFormField(bh.getChiPhiPhatSinh()!=null?bh.getChiPhiPhatSinh().toPlainString():"");

        addSingleRow(content,gc,0,"Mã phiếu BH:",    tfMaBH);
        addSingleRow(content,gc,1,"Sản phẩm:",       tfSP);
        addSingleRow(content,gc,2,"Trạng thái hiện:", tfTTCu);
        addSingleRow(content,gc,3,"Trạng thái mới:", cbTT);
        addSingleRow(content,gc,4,"NV xử lý:",       cbNVXL);
        addSingleRow(content,gc,5,"Kết quả xử lý:",  scKQ);
        addSingleRow(content,gc,6,"Chi phí PS (đ):", txtChiPhi);

        dlg.add(content, BorderLayout.CENTER);

        // Tao footer truc tiep de tranh bug getText() tra ve "" voi custom paintComponent
        JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
        footer.setBackground(CONTENT_BG); footer.setBorder(new MatteBorder(1,0,0,0,BORDER_COLOR));
        JButton btnHuyCapNhat=createActionBtn("Hủy bỏ",new Color(90,100,115),WHITE);
        JButton btnLuuCapNhat=createActionBtn("Lưu thay đổi",SUCCESS,WHITE); btnLuuCapNhat.setPreferredSize(new Dimension(140,36));
        btnHuyCapNhat.addActionListener(e->dlg.dispose());
        btnLuuCapNhat.addActionListener(e->{
            try{
                String ttMoi=(String)cbTT.getSelectedItem();
                String ketQua=txtKetQua.getText().trim();
                String cpStr=txtChiPhi.getText().trim().replace(",","");
                BigDecimal cp=cpStr.isEmpty()?BigDecimal.ZERO:new BigDecimal(cpStr);
                int selNV=cbNVXL.getSelectedIndex();
                Integer maNVXL=selNV>0?nvIds[selNV-1]:bh.getMaNVXuLy();
                boolean ok=bus.updateStatusWithCost(bh.getMaBaoHanh(),ttMoi,maNVXL,ketQua.isEmpty()?null:ketQua,cp);
                if(ok){
                    // Ghi lịch sử cập nhật
                    String ghiChuLS = "Cập nhật trạng thái";
                    if(cp!=null && cp.compareTo(java.math.BigDecimal.ZERO)>0)
                        ghiChuLS += " | Chi phí: "+NF.format(cp)+" đ";
                    if(!ketQua.isEmpty()) ghiChuLS += " | Kết quả: "+ketQua;
                    lichSuBUS.ghiLichSu(bh.getMaBaoHanh(), maNVXL,
                        bh.getTrangThai(), ttMoi, ghiChuLS);
                    JOptionPane.showMessageDialog(dlg,"Cập Nhật Thành Công!","Thành Công",JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();loadData();
                }
                else JOptionPane.showMessageDialog(dlg,"Cập Nhật Thất Bại !","Lỗi",JOptionPane.ERROR_MESSAGE);
            }catch(NumberFormatException ex){JOptionPane.showMessageDialog(dlg,"Chi phi phai la so hop le!","",JOptionPane.ERROR_MESSAGE);}
            catch(Exception ex){JOptionPane.showMessageDialog(dlg,"Loi: "+ex.getMessage(),"",JOptionPane.ERROR_MESSAGE);}
        });
        footer.add(btnHuyCapNhat); footer.add(btnLuuCapNhat);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    // DIALOG: LỊCH SỬ BẢO HÀNH (theo serial)
    // =========================================================================
    private void showLichSuDialog(BaoHanhDTO bh) {
        JDialog dlg = makeDialog("Lịch Sử Cập Nhật — Phiếu BH #" + bh.getMaBaoHanh(), 780, 480);

        // ── Header info ───────────────────────────────────────────────────────
        JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        infoBar.setBackground(new Color(245, 250, 255));
        infoBar.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));

        String tenSP2 = cacheSP.getOrDefault(bh.getMaSP(), "SP#" + bh.getMaSP());
        addInfoChip(infoBar, "Phiếu BH",  "#" + bh.getMaBaoHanh());
        addInfoChip(infoBar, "Sản phẩm",  tenSP2);
        addInfoChip(infoBar, "Trạng thái", mapTT(bh.getTrangThai()));
        dlg.add(infoBar, BorderLayout.NORTH);

        // ── Bảng lịch sử ─────────────────────────────────────────────────────
        String[] cols = {"Thời gian", "Nhân viên", "Trạng thái cũ", "Trạng thái mới", "Ghi chú"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(model);
        tbl.setFont(FONT_NORMAL); tbl.setRowHeight(34);
        tbl.setGridColor(new Color(220, 230, 245));
        tbl.setSelectionBackground(new Color(187, 222, 251));
        tbl.setSelectionForeground(PRIMARY_DARK);
        tbl.setShowVerticalLines(true);
        tbl.setFillsViewportHeight(true);

        JTableHeader th = tbl.getTableHeader();
        th.setPreferredSize(new Dimension(0, 36));
        th.setReorderingAllowed(false);
        th.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,val,isSel,hasFocus,row,col);
                lbl.setBackground(TABLE_HEADER); lbl.setForeground(WHITE);
                lbl.setFont(FONT_HEADER); lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,0,1,new Color(60,120,200)),
                    BorderFactory.createEmptyBorder(0,8,0,8)));
                return lbl;
            }
        });

        // Độ rộng cột
        int[] cw = {145, 130, 120, 120, 230};
        for (int i = 0; i < cw.length; i++) tbl.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);

        // Renderer màu cho cột trạng thái cũ/mới
        DefaultTableCellRenderer ttRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,val,isSel,hasFocus,row,col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String v = val != null ? val.toString() : "";
                if (!isSel) {
                    if (v.contains("Đang xử lý"))      { lbl.setForeground(WARNING_COLOR); }
                    else if (v.contains("Đã gửi"))     { lbl.setForeground(PRIMARY); }
                    else if (v.contains("Chờ linh"))   { lbl.setForeground(new Color(90,60,180)); }
                    else if (v.contains("Đã trả"))     { lbl.setForeground(SUCCESS); }
                    else if (v.contains("Tạo mới"))    { lbl.setForeground(new Color(0,150,136)); }
                    else                               { lbl.setForeground(new Color(80,100,120)); }
                }
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                return lbl;
            }
        };
        tbl.getColumnModel().getColumn(2).setCellRenderer(ttRenderer);
        tbl.getColumnModel().getColumn(3).setCellRenderer(ttRenderer);

        // Alternating row color
        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSel, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t,val,isSel,hasFocus,row,col);
                if (!isSel) setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                setFont(FONT_NORMAL); setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
                return this;
            }
        });
        // Gán lại ttRenderer sau khi setDefaultRenderer (ưu tiên cao hơn)
        tbl.getColumnModel().getColumn(2).setCellRenderer(ttRenderer);
        tbl.getColumnModel().getColumn(3).setCellRenderer(ttRenderer);

        // ── Load dữ liệu từ LICHSUBAOHANH ────────────────────────────────────
        java.time.format.DateTimeFormatter dtfFull =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        java.util.List<DTO.LichSuBaoHanhDTO> rows = lichSuBUS.layLichSu(bh.getMaBaoHanh());

        if (rows.isEmpty()) {
            model.addRow(new Object[]{"—", "—", "—", "—", "Chưa có lịch sử cập nhật"});
        } else {
            for (DTO.LichSuBaoHanhDTO r : rows) {
                model.addRow(new Object[]{
                    r.getThoiGian() != null ? r.getThoiGian().format(dtfFull) : "—",
                    r.getTenNV(),
                    mapTT(r.getTrangThaiCu()),
                    mapTT(r.getTrangThaiMoi()),
                    r.getGhiChu() != null ? r.getGhiChu() : "—"
                });
            }
        }

        JScrollPane sc = new JScrollPane(tbl); sc.setBorder(new LineBorder(BORDER_COLOR, 1));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(WHITE);
        wrap.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel lblCount = new JLabel("  " + rows.size() + " lần cập nhật");
        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblCount.setForeground(new Color(100, 120, 150));
        wrap.add(lblCount, BorderLayout.NORTH);
        wrap.add(sc, BorderLayout.CENTER);
        dlg.add(wrap, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(CONTENT_BG); footer.setBorder(new MatteBorder(1,0,0,0,BORDER_COLOR));
        JButton btnDong = createActionBtn("Đóng", new Color(90,100,115), WHITE);
        btnDong.addActionListener(e -> dlg.dispose());
        footer.add(btnDong);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    /** Chip nhỏ hiển thị label + value trong infoBar */
    private void addInfoChip(JPanel parent, String label, String value) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        chip.setBackground(new Color(245, 250, 255));
        JLabel lbl = new JLabel(label + ": ");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(100, 120, 150));
        JLabel val = new JLabel(value != null ? value : "—");
        val.setFont(new Font("Segoe UI", Font.BOLD, 12));
        val.setForeground(PRIMARY_DARK);
        chip.add(lbl); chip.add(val);
        parent.add(chip);
    }

    // =========================================================================
    // IN PHIẾU BẢO HÀNH
    // =========================================================================
    private void inPhieu(BaoHanhDTO bh) {
        PrinterJob job=PrinterJob.getPrinterJob();
        job.setJobName("Phieu Bao Hanh #"+bh.getMaBaoHanh());
        job.setPrintable((graphics,pageFormat,pageIndex)->{
            if(pageIndex>0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2=(Graphics2D)graphics;
            g2.translate(pageFormat.getImageableX(),pageFormat.getImageableY());
            double w=pageFormat.getImageableWidth();
            g2.setColor(Color.BLACK);

            // Header store
            g2.setFont(new Font("Segoe UI",Font.BOLD,18));
            FontMetrics fm=g2.getFontMetrics();
            String storeName="LAPTOP STORE"; g2.drawString(storeName,(int)(w/2-fm.stringWidth(storeName)/2),30);
            g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
            fm=g2.getFontMetrics();
            String addr="Địa chỉ: ……… | ĐT: ………";
            g2.drawString(addr,(int)(w/2-fm.stringWidth(addr)/2),44);

            // Title
            g2.setFont(new Font("Segoe UI",Font.BOLD,14));
            fm=g2.getFontMetrics();
            String ttl="PHIẾU BẢO HÀNH"; g2.drawString(ttl,(int)(w/2-fm.stringWidth(ttl)/2),66);
            g2.setFont(new Font("Segoe UI",Font.PLAIN,9));
            fm=g2.getFontMetrics();
            String sub="Số: #"+bh.getMaBaoHanh(); g2.drawString(sub,(int)(w/2-fm.stringWidth(sub)/2),78);
            g2.drawLine(0,84,(int)w,84);

            // Body
            g2.setFont(new Font("Segoe UI",Font.PLAIN,11));
            String tenSP=cacheSP.getOrDefault(bh.getMaSP(),"SP#"+bh.getMaSP());
            String nvTN=bh.getMaNVTiepNhan()!=null?cacheNV.getOrDefault(bh.getMaNVTiepNhan(),"—"):"—";
            String[][] rows={
                {"Sản phẩm:",tenSP},
                {"Mã hóa đơn:",String.valueOf(bh.getMaHoaDon())},
                {"Mã Serial:",bh.getMaIMEI()!=null?String.valueOf(bh.getMaIMEI()):"—"},
                {"Ngày tiếp nhận:",bh.getNgayTiepNhan()!=null?bh.getNgayTiepNhan().format(DTF):"—"},
                {"Ngày hẹn trả:",bh.getNgayHenTra()!=null?bh.getNgayHenTra().format(DTF):"—"},
                {"NV tiếp nhận:",nvTN},
                {"Hình thức XL:",fmtHT(bh.getHinhThucXuLy())},
                {"Mô tả lỗi:",bh.getMoTaLoi()!=null?bh.getMoTaLoi():"—"},
                {"Trạng thái:",mapTT(bh.getTrangThai())},
            };
            int y=100;
            for(String[] r:rows){
                g2.setFont(new Font("Segoe UI",Font.BOLD,11)); g2.drawString(r[0],10,y);
                g2.setFont(new Font("Segoe UI",Font.PLAIN,11)); g2.drawString(r[1],150,y);
                y+=18;
            }
            g2.drawLine(0,y+5,(int)w,y+5);
            g2.setFont(new Font("Segoe UI",Font.ITALIC,9));
            g2.drawString("Ngày in: "+LocalDate.now().format(DTF),10,y+18);
            return Printable.PAGE_EXISTS;
        });
        if(job.printDialog()){
            try{job.print();}catch(PrinterException ex){
                JOptionPane.showMessageDialog(this,"Lỗi in: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);}
        }
    }

    // =========================================================================
    // DIALOG: BÁO CÁO
    // =========================================================================
    private void showBaoCaoDialog() {
        JDialog dlg = makeDialog("Báo Cáo Bảo Hành", 700, 520);
        JPanel content=new JPanel(new BorderLayout(0,10)); content.setBackground(WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(16,20,16,20));

        // Tổng hợp số liệu
        int tong=allList.size(), dx=0, dg=0, clk=0, dtk=0;
        BigDecimal tongCP=BigDecimal.ZERO;
        for(BaoHanhDTO b:allList){
            switch(b.getTrangThai()!=null?b.getTrangThai():""){
                case"DangXuLy"->dx++;case"DaGuiHang"->dg++;
                case"ChoLinhKien"->clk++;case"DaTraKhach"->dtk++;
            }
            if(b.getChiPhiPhatSinh()!=null) tongCP=tongCP.add(b.getChiPhiPhatSinh());
        }

        // Summary cards
        JPanel cards=new JPanel(new GridLayout(1,3,10,0)); cards.setOpaque(false);
        cards.add(makeBaoCaoCard("Tổng phiếu BH",  String.valueOf(tong), ACCENT));
        cards.add(makeBaoCaoCard("Hoàn thành",      String.valueOf(dtk),  SUCCESS));
        cards.add(makeBaoCaoCard("Tổng chi phí PS", NF.format(tongCP)+" đ", DANGER));

        // Chi tiết theo trạng thái
        String[] cols={"Trạng thái","Số phiếu","Tỷ lệ"};
        DefaultTableModel bModel=new DefaultTableModel(cols,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        Object[][] rows2={
            {"Đang xử lý",  dx,  tong>0?String.format("%.1f%%",dx *100.0/tong):"0%"},
            {"Đã gửi hàng", dg,  tong>0?String.format("%.1f%%",dg *100.0/tong):"0%"},
            {"Chờ linh kiện",clk,tong>0?String.format("%.1f%%",clk*100.0/tong):"0%"},
            {"Đã trả khách", dtk,tong>0?String.format("%.1f%%",dtk*100.0/tong):"0%"},
        };
        for(Object[] r:rows2) bModel.addRow(r);
        JTable bTable=new JTable(bModel); bTable.setFont(FONT_NORMAL); bTable.setRowHeight(34);
        bTable.setGridColor(new Color(220,230,245));
        JTableHeader bh=bTable.getTableHeader();
        bh.setPreferredSize(new Dimension(0,36));
        bh.setReorderingAllowed(false);
        bh.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,val,isSel,hasFocus,row,col);
                lbl.setBackground(TABLE_HEADER); lbl.setForeground(WHITE);
                lbl.setFont(FONT_HEADER); lbl.setHorizontalAlignment(SwingConstants.CENTER); lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,0,1,new Color(60,120,200)),
                    BorderFactory.createEmptyBorder(0,8,0,8)));
                return lbl;
            }
        });
        JScrollPane bSc=new JScrollPane(bTable); bSc.setBorder(new LineBorder(BORDER_COLOR,1));

        content.add(cards,BorderLayout.NORTH);
        JLabel lblDetail=new JLabel("Chi tiết theo trạng thái:"); lblDetail.setFont(FONT_LABEL); lblDetail.setForeground(PRIMARY);
        JPanel mid=new JPanel(new BorderLayout(0,6)); mid.setOpaque(false);
        mid.add(lblDetail,BorderLayout.NORTH); mid.add(bSc,BorderLayout.CENTER);
        content.add(mid,BorderLayout.CENTER);
        dlg.add(content,BorderLayout.CENTER);

        JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
        footer.setBackground(CONTENT_BG); footer.setBorder(new MatteBorder(1,0,0,0,BORDER_COLOR));
        JButton btnXEx=createActionBtn("Xuất Excel",new Color(67,160,71),WHITE);
        JButton btnXPDF=createActionBtn("Xuất PDF",DANGER,WHITE);
        JButton btnDong=createActionBtn("Đóng",new Color(90,100,115),WHITE);
        btnXEx.addActionListener(e->xuatExcel()); btnXPDF.addActionListener(e->xuatPDF());
        btnDong.addActionListener(e->dlg.dispose());
        footer.add(btnXEx); footer.add(btnXPDF); footer.add(btnDong);
        dlg.add(footer,BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JPanel makeBaoCaoCard(String label, String value, Color accent) {
        JPanel card=new JPanel() {
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,12)); g2.fillRoundRect(3,3,getWidth()-3,getHeight()-3,12,12);
                g2.setColor(WHITE); g2.fillRoundRect(0,0,getWidth()-3,getHeight()-3,12,12);
                g2.setColor(accent); g2.setStroke(new BasicStroke(3f)); g2.drawLine(16,getHeight()-6,getWidth()-20,getHeight()-6);
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setOpaque(false); card.setLayout(new GridBagLayout());
        JLabel lVal=new JLabel(value); lVal.setFont(new Font("Segoe UI",Font.BOLD,20)); lVal.setForeground(accent);
        JLabel lLbl=new JLabel(label); lLbl.setFont(new Font("Segoe UI",Font.PLAIN,12)); lLbl.setForeground(new Color(100,120,150));
        GridBagConstraints gc=new GridBagConstraints(); gc.anchor=GridBagConstraints.CENTER;
        gc.gridx=0; gc.gridy=0; card.add(lVal,gc);
        gc.gridy=1; card.add(lLbl,gc);
        card.setPreferredSize(new Dimension(0,80));
        return card;
    }

    // =========================================================================
    // XUẤT EXCEL
    // =========================================================================
    private void xuatExcel() {
        JFileChooser fc=new JFileChooser();
        fc.setDialogTitle("Lưu file Excel");
        fc.setSelectedFile(new java.io.File("BaoHanh_"+LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))+".csv"));
        if(fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;

        try(java.io.PrintWriter pw=new java.io.PrintWriter(fc.getSelectedFile(),"UTF-8")){
            pw.println("\uFEFFMã BH,Sản phẩm,Mã HĐ,Ngày TN,Ngày hẹn,NV TN,Hình thức,Mô tả lỗi,Kết quả,Chi phí,Trạng thái");
            for(BaoHanhDTO b:allList){
                String tenSP=cacheSP.getOrDefault(b.getMaSP(),"SP#"+b.getMaSP());
                String nvTN=b.getMaNVTiepNhan()!=null?cacheNV.getOrDefault(b.getMaNVTiepNhan(),"—"):"—";
                pw.printf("%d,\"%s\",%d,%s,%s,\"%s\",%s,\"%s\",\"%s\",%s,%s%n",
                    b.getMaBaoHanh(), tenSP, b.getMaHoaDon(),
                    b.getNgayTiepNhan()!=null?b.getNgayTiepNhan().format(DTF):"",
                    b.getNgayHenTra()!=null?b.getNgayHenTra().format(DTF):"",
                    nvTN, fmtHT(b.getHinhThucXuLy()),
                    b.getMoTaLoi()!=null?b.getMoTaLoi().replace("\"","''"):"",
                    b.getKetQuaXuLy()!=null?b.getKetQuaXuLy().replace("\"","''"):"",
                    b.getChiPhiPhatSinh()!=null?b.getChiPhiPhatSinh().toPlainString():"0",
                    mapTT(b.getTrangThai()));
            }
            JOptionPane.showMessageDialog(this,"Xuất Excel thành công!\n"+fc.getSelectedFile().getAbsolutePath(),"Thành công",JOptionPane.INFORMATION_MESSAGE);
        }catch(Exception ex){
            JOptionPane.showMessageDialog(this,"Lỗi xuất file: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // XUẤT PDF (dùng Java2D print → PDF)
    // =========================================================================
    private void xuatPDF() {
        JFileChooser fc=new JFileChooser();
        fc.setDialogTitle("Lưu file PDF");
        fc.setSelectedFile(new java.io.File("BaoHanh_"+LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))+".pdf"));
        if(fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;

        // Tạo JTextArea nội dung để in
        StringBuilder sb=new StringBuilder();
        sb.append("BÁO CÁO BẢO HÀNH — Ngày: ").append(LocalDate.now().format(DTF)).append("\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append(String.format("%-6s %-20s %-8s %-12s %-12s %-14s %-15s%n","Mã BH","Sản phẩm","Mã HĐ","Ngày TN","Ngày Hẹn","NV TN","Trạng thái"));
        sb.append("-".repeat(80)).append("\n");
        for(BaoHanhDTO b:allList){
            String tenSP=cacheSP.getOrDefault(b.getMaSP(),"SP#"+b.getMaSP());
            if(tenSP.length()>18) tenSP=tenSP.substring(0,18);
            String nvTN=b.getMaNVTiepNhan()!=null?cacheNV.getOrDefault(b.getMaNVTiepNhan(),"—"):"—";
            if(nvTN.length()>12) nvTN=nvTN.substring(0,12);
            sb.append(String.format("%-6d %-20s %-8d %-12s %-12s %-14s %-15s%n",
                b.getMaBaoHanh(),tenSP,b.getMaHoaDon(),
                b.getNgayTiepNhan()!=null?b.getNgayTiepNhan().format(DTF):"—",
                b.getNgayHenTra()!=null?b.getNgayHenTra().format(DTF):"—",
                nvTN, mapTT(b.getTrangThai())));
        }
        sb.append("=".repeat(80)).append("\n");
        sb.append("Tổng: ").append(allList.size()).append(" phiếu");

        JTextArea area=new JTextArea(sb.toString()); area.setFont(new Font("Monospaced",Font.PLAIN,9));
        PrinterJob job=PrinterJob.getPrinterJob();
        job.setJobName("BaoHanh_Export");
        // Ghi ra file PDF thông qua print service
        javax.print.attribute.HashPrintRequestAttributeSet attrs=new javax.print.attribute.HashPrintRequestAttributeSet();
        attrs.add(new javax.print.attribute.standard.Destination(fc.getSelectedFile().toURI()));
        try{
            job.setPrintable((g,pf,pi)->{
                if(pi>0) return Printable.NO_SUCH_PAGE;
                Graphics2D g2=(Graphics2D)g; g2.translate(pf.getImageableX(),pf.getImageableY());
                g2.setFont(new Font("Monospaced",Font.PLAIN,7)); g2.setColor(Color.BLACK);
                String[] lines=sb.toString().split("\n"); int y=10;
                for(String line:lines){ g2.drawString(line,0,y); y+=9; if(y>pf.getImageableHeight()-10) return Printable.NO_SUCH_PAGE; }
                return Printable.PAGE_EXISTS;
            });
            job.print(attrs);
            JOptionPane.showMessageDialog(this,"Xuất PDF thành công!\n"+fc.getSelectedFile().getAbsolutePath(),"Thành công",JOptionPane.INFORMATION_MESSAGE);
        }catch(Exception ex){
            JOptionPane.showMessageDialog(this,"Lỗi xuất PDF: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // XÓA
    // =========================================================================
    private void doXoa() {
        BaoHanhDTO bh=getSelectedBH();
        if(bh==null){warn("Vui lòng chọn phiếu cần xóa!");return;}
        doXoaDirect(bh);
    }

    private void doXoaDirect(BaoHanhDTO bh) {
        int cf=JOptionPane.showConfirmDialog(this,
            "Xóa phiếu bảo hành #"+bh.getMaBaoHanh()+"?\n(Chỉ xóa được phiếu đang xử lý)",
            "Xác nhận xóa",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(cf==JOptionPane.YES_OPTION){
            try{
                boolean ok=bus.delete(bh.getMaBaoHanh());
                if(ok){JOptionPane.showMessageDialog(this,"Đã xóa phiếu #"+bh.getMaBaoHanh(),"Thành công",JOptionPane.INFORMATION_MESSAGE);loadData();}
                else JOptionPane.showMessageDialog(this,"Xóa thất bại!","Lỗi",JOptionPane.ERROR_MESSAGE);
            }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);}
        }
    }

    // =========================================================================
    // HELPERS: DB
    // =========================================================================
    private SERIALDTO findSerialByCode(String code) {
        String sql="SELECT MaSerial,SerialCode,MaSP,MaChiTietPN,TrangThai FROM SERIAL WHERE SerialCode=?";
        try(Connection cn=DBConnection.getConnection(); PreparedStatement ps=cn.prepareStatement(sql)){
            ps.setString(1,code);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){SERIALDTO s=new SERIALDTO(); s.setMaSerial(rs.getInt("MaSerial")); s.setSerialCode(rs.getString("SerialCode")); s.setMaSP(rs.getInt("MaSP")); s.setTrangThai(rs.getString("TrangThai")); return s;}
            }
        }catch(Exception e){e.printStackTrace();}
        return null;
    }

    private HoaDonDTO findHoaDonBySerial(int maSerial) {
        String sql="SELECT h.MaHoaDon,h.MaKhachHang,h.MaNV,h.NgayLap,h.TongTienHang,h.PhanTramGiamHang,h.TienGiamHang,h.TienTruocVAT,h.TienVAT,h.TongThanhToan,h.GhiChu,h.TrangThai "
            +"FROM HOADON h JOIN CHITIETHOADON ct ON ct.MaHoaDon=h.MaHoaDon WHERE ct.MaSerial=? AND h.TrangThai='HoanThanh' ORDER BY h.NgayLap DESC";
        try(Connection cn=DBConnection.getConnection(); PreparedStatement ps=cn.prepareStatement(sql)){
            ps.setInt(1,maSerial);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()) return new HoaDonDTO(rs.getInt("MaHoaDon"),rs.getObject("MaKhachHang")!=null?rs.getInt("MaKhachHang"):null,rs.getInt("MaNV"),rs.getTimestamp("NgayLap")!=null?rs.getTimestamp("NgayLap").toLocalDateTime():null,rs.getBigDecimal("TongTienHang"),rs.getBigDecimal("PhanTramGiamHang"),rs.getBigDecimal("TienGiamHang"),rs.getBigDecimal("TienTruocVAT"),rs.getBigDecimal("TienVAT"),rs.getBigDecimal("TongThanhToan"),rs.getString("GhiChu"),rs.getString("TrangThai"));
            }
        }catch(Exception e){e.printStackTrace();}
        return null;
    }

    private String fetchTenNV(int maNV) {
        try{List<NhanVienDTO> all=nvBus.getAll(); for(NhanVienDTO nv:all) if(nv.getMaNV()==maNV) return nv.getTenNV();}catch(Exception ignored){}
        return "NV#"+maNV;
    }
    private String fetchTenSP(int maSP) {
        SanPhamDTO sp=spBus.timTheoMa(maSP); return sp!=null?sp.getTenSP():"SP#"+maSP;
    }

    // =========================================================================
    // HELPERS: UI
    // =========================================================================
    private BaoHanhDTO getSelectedBH() {
        int row=table.getSelectedRow(); if(row<0||row>=showList.size()) return null;
        return showList.get(row);
    }

    private JDialog makeDialog(String title, int w, int h) {
        Window owner=SwingUtilities.getWindowAncestor(this);
        JDialog dlg=(owner instanceof Frame)
            ?new JDialog((Frame)owner,title,true)
            :new JDialog((Dialog)owner,title,true);
        dlg.setSize(w,h); dlg.setLocationRelativeTo(this); dlg.setResizable(false);
        dlg.setLayout(new BorderLayout());
        dlg.add(makeDialogHeader(title), BorderLayout.NORTH);
        return dlg;
    }

    private JPanel makeDialogHeader(String title) {
        JPanel hdr=new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,PRIMARY_DARK,getWidth(),0,PRIMARY));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        hdr.setOpaque(false); hdr.setPreferredSize(new Dimension(0,50));
        hdr.setBorder(BorderFactory.createEmptyBorder(0,16,0,16));
        JLabel lbl=new JLabel(title); lbl.setFont(new Font("Segoe UI",Font.BOLD,15)); lbl.setForeground(WHITE);
        hdr.add(lbl,BorderLayout.WEST);
        return hdr;
    }

    private JPanel makeDialogFooter(JDialog dlg, String okLabel, Color okColor, java.util.function.Consumer<JButton> onOK) {
        JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
        footer.setBackground(CONTENT_BG); footer.setBorder(new MatteBorder(1,0,0,0,BORDER_COLOR));
        JButton btnHuy=createActionBtn("Hủy bỏ",new Color(90,100,115),WHITE);
        JButton btnOK=createActionBtn(okLabel,okColor,WHITE); btnOK.setPreferredSize(new Dimension(140,36));
        btnHuy.addActionListener(e->dlg.dispose());
        if(onOK!=null) btnOK.addActionListener(e->onOK.accept(btnOK));
        footer.add(btnHuy); footer.add(btnOK);
        return footer;
    }

    private JPanel makeStepPanel(String title) {
        JPanel p=new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setBackground(WHITE);
        p.setBorder(new CompoundBorder(
            new TitledBorder(new LineBorder(BORDER_COLOR,1,true),title,TitledBorder.LEFT,TitledBorder.TOP,new Font("Segoe UI",Font.BOLD,12),PRIMARY),
            BorderFactory.createEmptyBorder(6,8,8,8)));
        return p;
    }

    private void addStepRow(JPanel step, String label, JComponent field) {
        JPanel row=new JPanel(new BorderLayout(8,0)); row.setBackground(WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,Math.max(34,field.getPreferredSize().height+10)));
        JLabel lbl=new JLabel(label); lbl.setFont(FONT_LABEL); lbl.setForeground(PRIMARY); lbl.setPreferredSize(new Dimension(165,28));
        row.add(lbl,BorderLayout.WEST); row.add(field,BorderLayout.CENTER);
        row.setBorder(BorderFactory.createEmptyBorder(3,0,3,0)); step.add(row);
    }

    private void addDetailRow2(JPanel p, GridBagConstraints gc, int row, String l1,String v1,String l2,String v2) {
        gc.gridy=row; gc.gridwidth=1;
        gc.gridx=0; gc.weightx=0.18; JLabel lb1=new JLabel(l1); lb1.setFont(FONT_LABEL); lb1.setForeground(PRIMARY); p.add(lb1,gc);
        gc.gridx=1; gc.weightx=0.32; JLabel vl1=new JLabel(v1!=null?v1:"—"); vl1.setFont(FONT_NORMAL); vl1.setForeground(new Color(20,50,90)); p.add(vl1,gc);
        gc.gridx=2; gc.weightx=0.18; JLabel lb2=new JLabel(l2); lb2.setFont(FONT_LABEL); lb2.setForeground(PRIMARY); p.add(lb2,gc);
        gc.gridx=3; gc.weightx=0.32; JLabel vl2=new JLabel(v2!=null?v2:"—"); vl2.setFont(FONT_NORMAL); vl2.setForeground(new Color(20,50,90)); p.add(vl2,gc);
    }

    private void addSingleRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridy=row;
        gc.gridx=0; gc.weightx=0.32; gc.gridwidth=1; JLabel lbl=new JLabel(label); lbl.setFont(FONT_LABEL); lbl.setForeground(PRIMARY); p.add(lbl,gc);
        gc.gridx=1; gc.weightx=0.68; p.add(field,gc);
    }

    private void clearFields(JTextField... fields) { for(JTextField f:fields) f.setText(""); }

    private JTextField makeFormField(String text) {
        JTextField tf=new JTextField(text!=null?text:""); tf.setFont(FONT_NORMAL); tf.setPreferredSize(new Dimension(0,32));
        tf.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR,1),BorderFactory.createEmptyBorder(4,8,4,8)));
        return tf;
    }
    private JTextField makeReadField(String text) {
        JTextField tf=makeFormField(text); tf.setEditable(false); tf.setBackground(new Color(245,248,255)); return tf;
    }
    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb=new JComboBox<>(items); cb.setFont(FONT_NORMAL);
        cb.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR,1),BorderFactory.createEmptyBorder(2,6,2,6)));
        return cb;
    }

    private JButton createSmallBtn(String text, Color bg, Color fg) {
        JButton b=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled()?(getModel().isRollover()?bg.darker():bg):new Color(180,180,180));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.dispose(); super.paintComponent(g);
            }
        };
        b.setForeground(fg); b.setFont(FONT_LABEL);
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setBorder(BorderFactory.createEmptyBorder(6,14,6,14));
        return b;
    }
    private JButton createActionBtn(String text, Color bg, Color fg) {
        JButton b=createSmallBtn(text,bg,fg); b.setPreferredSize(new Dimension(110,36)); return b;
    }
    private JButton buildHeaderBtn(String text, Color bg, Color fg) {
        Canvas cv=new Canvas(); FontMetrics fm0=cv.getFontMetrics(new Font("Segoe UI",Font.BOLD,13));
        final int w=fm0.stringWidth(text)+36; final Font BF=new Font("Segoe UI",Font.BOLD,13);
        JButton btn=new JButton(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?bg.darker():bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setFont(BF); g2.setColor(fg);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(text,(getWidth()-fm.stringWidth(text))/2,(getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(w,36);}
            @Override public Dimension getMinimumSize(){return getPreferredSize();}
            @Override public Dimension getMaximumSize(){return getPreferredSize();}
        };
        btn.setText(""); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String mapTT(String tt) {
        if(tt==null) return "—";
        return switch(tt){ case"DangXuLy"->"Đang xử lý";case"DaGuiHang"->"Đã gửi hàng";case"ChoLinhKien"->"Chờ linh kiện";case"DaTraKhach"->"Đã trả khách";default->tt;};
    }
    private String fmtHT(String r) {
        if(r==null) return "—";
        return switch(r){case"SuaChuaTaiCho"->"Sửa tại chỗ";case"GuiHang"->"Gửi hàng";case"ThayTheMoi"->"Thay thế mới";default->r;};
    }
    private void warn(String msg){JOptionPane.showMessageDialog(this,msg,"Thông báo",JOptionPane.WARNING_MESSAGE);}
}