package GUI;

import BUS.HoaDonBUS;
import BUS.KhachHangBUS;
import BUS.LoaiSanPhamBUS;
import BUS.NhanVienBUS;
import BUS.SanPhamBUS;
import DTO.ChiTietHoaDonDTO;
import DTO.HoaDonDTO;
import DTO.KhachHangDTO;
import DTO.LoaiSanPhamDTO;
import DTO.NhanVienDTO;
import DTO.SanPhamDTO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import javax.swing.*;
import UTIL.HoaDonPDFUtils;
import javax.swing.border.*;
import javax.swing.table.*;

public class BanHangPanel2 extends JPanel {

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color PRIMARY      = new Color(21, 101, 192);
    private static final Color PRIMARY_DARK = new Color(10, 60, 130);
    private static final Color CONTENT_BG   = new Color(236, 242, 250);
    private static final Color WHITE        = Color.WHITE;
    private static final Color ROW_ALT      = new Color(245, 250, 255);
    private static final Color TABLE_HEADER = new Color(21, 101, 192);
    private static final Color SUCCESS      = new Color(46, 125, 50);
    private static final Color DANGER       = new Color(198, 40, 40);
    private static final Color TAB_ACTIVE   = new Color(0, 188, 212);
    private static final Color TAB_INACTIVE = new Color(180, 210, 240);
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color CARD_HOVER   = new Color(227, 242, 253);
    private static final Color CARD_BORDER  = new Color(187, 222, 251);
    private static final Color PRICE_COLOR  = new Color(198, 40, 40);
    private static final Color CART_BG      = new Color(245, 248, 255);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_LABEL      = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_NORMAL     = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_TAB        = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_CARD_NAME  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_CARD_PRICE = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_CARD_BRAND = new Font("Segoe UI", Font.PLAIN, 11);

    // ── Kích thước ảnh card ───────────────────────────────────────────────────
    private static final int IMG_W = 150;
    private static final int IMG_H = 130;

    // ── Filter data — Hãng laptop (hardcode vì không có BUS riêng) ──────────
    private static final String[][] GIA_RANGES = {
            {"Tat ca","T\u1ea5t c\u1ea3","0","999999999"},
            {"duoi5","D\u01b0\u1edbi 5 tri\u1ec7u","0","5000000"},
            {"5den15","5 - 15 tri\u1ec7u","5000000","15000000"},
            {"15den30","15 - 30 tri\u1ec7u","15000000","30000000"},
            {"tren30","Tr\u00ean 30 tri\u1ec7u","30000000","999999999"},
    };
    private static final int   MALOAI_LAPTOP  = 1;
    private static final int[] MALOAI_PHUKIEN = {2, 3, 4, 5};

    // ── State ─────────────────────────────────────────────────────────────────
    private JTextField  txtSearch, txtMaHD, txtNgayLap;
    private JPanel      productGrid;
    private JScrollPane gridScroll;
    // Filter state — giống SanPhamPanel
    private String filterGroup       = "Tat_ca";   // Tat_ca | Laptop | PhuKien
    private String filterLaptopBrand = "Tat_ca";   // tên hãng hoặc "Tat_ca"
    private String activeGiaKey      = "Tat ca";
    private int    filterSubLoai     = -1;          // -1 = tất cả phụ kiện
    private java.util.List<JButton> allGiaBtns  = new ArrayList<>();
    private java.util.List<JButton> groupBtns   = new ArrayList<>();
    // Danh sách loại từ DB
    private ArrayList<LoaiSanPhamDTO> danhSachLoai = new ArrayList<>();
    private java.util.Map<Integer, String> loaiMap = new java.util.HashMap<>();
    // ComboBox hãng + phụ kiện con
    private JComboBox<String> cbLaptopBrand;
    private JComboBox<String> cbSubLoai;
    private JPanel            subLoaiRow;
    private boolean isLoadingBrands    = false;
    private boolean isRebuildingSubLoai = false;

    private final SanPhamBUS        sanPhamBUS   = new SanPhamBUS();
    private final HoaDonBUS         hoaDonBUS    = new HoaDonBUS();

    // References đến panel khác — set từ MainFrame sau khi khởi tạo
    private GUI.HoaDonPanel  hoaDonPanelRef  = null;
    private GUI.BaoHanhPanel baoHanhPanelRef = null;

    /** Gọi từ MainFrame: setRelatedPanels(hoaDonPanel, baoHanhPanel) */
    public void setRelatedPanels(GUI.HoaDonPanel hoaDon, GUI.BaoHanhPanel baoHanh) {
        this.hoaDonPanelRef  = hoaDon;
        this.baoHanhPanelRef = baoHanh;
    }
    private final LoaiSanPhamBUS    loaiBUS      = new LoaiSanPhamBUS();
    private final KhachHangBUS      khachHangBUS = new KhachHangBUS();
    private final NhanVienBUS       nhanVienBUS  = new NhanVienBUS();
    private ArrayList<SanPhamDTO> allProducts = new ArrayList<>();
    private ArrayList<Object[]>   cartItems   = new ArrayList<>();
    // Lưu tồn kho gốc từ DB — không thay đổi khi thêm/xóa giỏ hàng
    private java.util.HashMap<Integer, Integer> tonKhoGoc = new java.util.HashMap<>();
    private int maNVHienTai = 1;
    // Park Order
    private int maHoaDonChoDangMo = 0;

    // ── Khách hàng tìm được + % giảm ─────────────────────────────────────────
    private KhachHangDTO khachHangHienTai = null;
    private double       phanTramGiamKH   = 0.0;

    // ── Giỏ hàng widgets ─────────────────────────────────────────────────────
    private DefaultTableModel cartModel;
    private JTable            cartTable;
    private JTextField        txtKhachHang, txtSDT, txtTienNhan;
    private JLabel            lblTongTien, lblThanhTien, lblTienThua, lblGiamInfo;
    private WarningLabel      warnSDT, warnTienNhan;
    private JRadioButton      rbTienMat, rbChuyenKhoan;
    private double            tongTien        = 0;
    private boolean           isRecalculating = false;
    // Toast inline trong giỏ hàng
    private JLabel            lblToast        = null;
    private javax.swing.Timer toastTimer      = null;

    // =========================================================================
    // INNER CLASS: WarningLabel — thông báo lỗi inline đơn giản (JLabel thuần)
    // =========================================================================
    private static class WarningLabel extends JLabel {
        WarningLabel() {
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(new Color(198, 40, 40));
            setVisible(false);
        }
        void showWarning(String msg) {
            setText(msg);
            setVisible(true);
            Container p = getParent();
            if (p != null) { p.revalidate(); p.repaint(); }
        }
        void hideWarning() {
            setVisible(false);
            Container p = getParent();
            if (p != null) { p.revalidate(); p.repaint(); }
        }
    }

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public BanHangPanel2() {
        setLayout(new BorderLayout(8, 8));
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(createTitlePanel(), BorderLayout.NORTH);

        // Khởi tạo field ẩn trước khi tạo cart panel
        txtMaHD    = createSmallField("HD001", 58);   txtMaHD.setEditable(false);
        txtNgayLap = createSmallField(java.time.LocalDate.now().toString(), 82); txtNgayLap.setEditable(false);

        // Chia: sản phẩm (trái ~75%) | giỏ hàng (phải ~25%)
        JPanel leftPanel  = createLeftPanel();
        JPanel rightPanel = createCartPanel();
        leftPanel .setMinimumSize(new Dimension(260, 200));
        rightPanel.setMinimumSize(new Dimension(280, 200));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setResizeWeight(0.70);
        split.setDividerSize(0);          // ẩn divider hoàn toàn
        split.setBorder(null);
        split.setBackground(CONTENT_BG);
        split.setEnabled(false);          // không cho kéo
        // Đặt divider đúng vị trí sau khi UI ready
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.70));
        add(split, BorderLayout.CENTER);

        loadProductData();
        // Áp dụng HAND_CURSOR cho toàn bộ component sau khi UI dựng xong
        SwingUtilities.invokeLater(() -> setCursorAll(this, new Cursor(Cursor.HAND_CURSOR)));
    }

    // =========================================================================
    // LEFT PANEL
    // =========================================================================
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CONTENT_BG);
        panel.add(createFilterPanel(),      BorderLayout.NORTH);
        panel.add(createProductGridPanel(), BorderLayout.CENTER);
        return panel;
    }

    private void loadProductData() {
        // Load loaiMap từ DB
        danhSachLoai = loaiBUS.getAll();
        loaiMap.clear();
        for (LoaiSanPhamDTO l : danhSachLoai) loaiMap.put(l.getMaLoai(), l.getTenLoai());
        // Rebuild cbSubLoai sau khi loaiMap có dữ liệu
        rebuildSubLoaiCombo();
        allProducts  = sanPhamBUS.getDanhSachSanPham();
        // Snapshot tồn kho gốc từ DB
        tonKhoGoc.clear();
        for (SanPhamDTO sp : allProducts)
            tonKhoGoc.put(sp.getMaSP(), sp.getSoLuongTon());
        // Đổ hãng động vào cbLaptopBrand
        loadDynamicBrands();
        applyAllFilters();
    }

    private void rebuildSubLoaiCombo() {
        if (cbSubLoai == null) return;
        isRebuildingSubLoai = true;
        try {
            String[] items = new String[MALOAI_PHUKIEN.length + 1];
            items[0] = "T\u1ea5t c\u1ea3 ph\u1ee5 ki\u1ec7n";
            for (int i = 0; i < MALOAI_PHUKIEN.length; i++)
                items[i+1] = loaiMap.getOrDefault(MALOAI_PHUKIEN[i], "Lo\u1ea1i " + MALOAI_PHUKIEN[i]);
            cbSubLoai.removeAllItems();
            for (String s : items) cbSubLoai.addItem(s);
            cbSubLoai.setSelectedIndex(0);
        } finally { isRebuildingSubLoai = false; }
    }

    private void loadDynamicBrands() {
        if (cbLaptopBrand == null) return;
        isLoadingBrands = true;
        try {
            java.util.Set<String> brands = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            for (SanPhamDTO sp : allProducts)
                if (sp.getThuongHieu() != null && !sp.getThuongHieu().trim().isEmpty())
                    brands.add(sp.getThuongHieu().trim());
            String cur = cbLaptopBrand.getSelectedItem() != null
                    ? cbLaptopBrand.getSelectedItem().toString() : "T\u1ea5t c\u1ea3 h\u00e3ng";
            cbLaptopBrand.removeAllItems();
            cbLaptopBrand.addItem("T\u1ea5t c\u1ea3 h\u00e3ng");
            for (String b : brands) cbLaptopBrand.addItem(b);
            if (!cur.equals("T\u1ea5t c\u1ea3 h\u00e3ng") && brands.contains(cur))
                cbLaptopBrand.setSelectedItem(cur);
            else
                cbLaptopBrand.setSelectedIndex(0);
        } finally { isLoadingBrands = false; }
    }

    private void renderProductGrid(ArrayList<SanPhamDTO> list) {
        productGrid.removeAll();
        for (SanPhamDTO sp : list) {
            if (!sp.getTrangThai().equals("DangBan")) continue;
            if (sp.getSoLuongTon() <= 0) continue;
            productGrid.add(createProductCard(sp));
        }
        productGrid.revalidate();
        productGrid.repaint();
        // Cập nhật lại cursor cho các card mới được render
        SwingUtilities.invokeLater(() -> setCursorAll(productGrid, new Cursor(Cursor.HAND_CURSOR)));
    }

    // =========================================================================
    // PRODUCT CARD
    // =========================================================================
    private JPanel createProductCard(SanPhamDTO sp) {
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BORDER); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        card.setBackground(CARD_BG); card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        card.setPreferredSize(new Dimension(200, 265));
        card.setMaximumSize(new Dimension(200, 265));

        ImageIcon iconLoaded = createProductIcon(sp);
        Image finalImage = iconLoaded.getImage();

        JPanel imgPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setColor(new Color(240, 245, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                int pw=getWidth(), ph=getHeight(), iw=finalImage.getWidth(null), ih=finalImage.getHeight(null);
                if (iw>0 && ih>0) {
                    double scale=Math.max((double)pw/iw,(double)ph/ih);
                    int sw=(int)(iw*scale),sh=(int)(ih*scale),sx=(pw-sw)/2,sy=(ph-sh)/2;
                    g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0,0,pw,ph,10,10));
                    g2.drawImage(finalImage,sx,sy,sw,sh,null);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        imgPanel.setOpaque(false); imgPanel.setPreferredSize(new Dimension(0, 135));
        JLabel imgLabel = new JLabel(); imgLabel.setOpaque(false);
        imgPanel.add(imgLabel, BorderLayout.CENTER);

        JLabel badge = new JLabel("  C\u00f2n " + sp.getSoLuongTon() + "  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10)); badge.setForeground(WHITE);
        badge.setBackground(sp.getSoLuongTon()>0 ? new Color(46,125,50) : DANGER);
        badge.setOpaque(true); badge.setBorder(BorderFactory.createEmptyBorder(2,4,2,4));
        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT,4,4));
        badgeWrap.setOpaque(false); badgeWrap.add(badge);
        imgPanel.add(badgeWrap, BorderLayout.SOUTH);
        card.add(imgPanel, BorderLayout.CENTER);

        JPanel info = new JPanel(); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false); info.setBorder(BorderFactory.createEmptyBorder(8,2,4,2));
        JLabel lblBrand = new JLabel(sp.getThuongHieu().toUpperCase());
        lblBrand.setFont(FONT_CARD_BRAND); lblBrand.setForeground(new Color(100,130,170));
        lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblName = new JLabel("<html><body style='width:130px'>"+sp.getTenSP()+"</body></html>");
        lblName.setFont(FONT_CARD_NAME); lblName.setForeground(PRIMARY_DARK);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblPrice = new JLabel(formatMoney(sp.getGia().doubleValue())+" \u0111");
        lblPrice.setFont(FONT_CARD_PRICE); lblPrice.setForeground(PRICE_COLOR);
        lblPrice.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(lblBrand); info.add(Box.createVerticalStrut(3));
        info.add(lblName);  info.add(Box.createVerticalStrut(5));
        info.add(lblPrice);
        card.add(info, BorderLayout.SOUTH);

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(CARD_HOVER); card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { card.setBackground(CARD_BG);   card.repaint(); }
            @Override public void mouseClicked(MouseEvent e) { addToCart(sp); }
        };
        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
        for (Component c : new Component[]{card,imgPanel,imgLabel,badgeWrap,badge,info,lblBrand,lblName,lblPrice}) {
            c.addMouseListener(ma);
            c.setCursor(handCursor);
        }
        return card;
    }

    // =========================================================================
    // ẢNH SẢN PHẨM
    // =========================================================================
    private ImageIcon createProductIcon(SanPhamDTO sp) {
        try {
            // Quy định ảnh chuẩn: sp_<MaSP>.jpg — thử trước tiên
            String spFileName = "sp_" + sp.getMaSP() + ".jpg";
            java.io.File fSp  = new java.io.File("images/" + spFileName);
            if (fSp.exists()) { BufferedImage r = javax.imageio.ImageIO.read(fSp); if (r != null) return scaleAndCrop(r); }
            java.io.File fSp2 = new java.io.File(spFileName);
            if (fSp2.exists()) { BufferedImage r = javax.imageio.ImageIO.read(fSp2); if (r != null) return scaleAndCrop(r); }

            // Fallback: dùng tên file trong DB (getHinhAnh)
            String fn = sp.getHinhAnh();
            if (fn==null||fn.trim().isEmpty()) throw new Exception("No image");
            fn=fn.trim();
            java.io.File f=new java.io.File(fn); if(f.exists()){BufferedImage r=javax.imageio.ImageIO.read(f);if(r!=null)return scaleAndCrop(r);}
            java.io.File f2=new java.io.File("images/"+fn); if(f2.exists()){BufferedImage r=javax.imageio.ImageIO.read(f2);if(r!=null)return scaleAndCrop(r);}
            java.io.File f3=new java.io.File("images/"+normalizeFolder(sp.getThuongHieu())+"/"+fn); if(f3.exists()){BufferedImage r=javax.imageio.ImageIO.read(f3);if(r!=null)return scaleAndCrop(r);}
            throw new Exception("Not found: "+fn);
        } catch(Exception e){ System.err.println("LOI anh ["+sp.getTenSP()+"]: "+e.getMessage()); return createNoImageIcon(); }
    }
    private ImageIcon scaleAndCrop(BufferedImage raw) {
        double sc=Math.max((double)IMG_W/raw.getWidth(),(double)IMG_H/raw.getHeight());
        int sw=(int)(raw.getWidth()*sc),sh=(int)(raw.getHeight()*sc);
        BufferedImage s=new BufferedImage(sw,sh,BufferedImage.TYPE_INT_ARGB);
        Graphics2D gs=s.createGraphics(); gs.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gs.drawImage(raw,0,0,sw,sh,null); gs.dispose();
        return new ImageIcon(s.getSubimage(Math.max(0,(sw-IMG_W)/2),Math.max(0,(sh-IMG_H)/2),Math.min(IMG_W,sw),Math.min(IMG_H,sh)));
    }
    private ImageIcon createNoImageIcon() {
        BufferedImage img=new BufferedImage(IMG_W,IMG_H,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2=img.createGraphics(); g2.setColor(new Color(225,230,240)); g2.fillRoundRect(0,0,IMG_W,IMG_H,10,10);
        g2.setColor(new Color(170,180,195)); g2.setFont(new Font("Segoe UI",Font.BOLD,12));
        FontMetrics fm=g2.getFontMetrics(); String t="NO IMAGE";
        g2.drawString(t,(IMG_W-fm.stringWidth(t))/2,IMG_H/2+fm.getAscent()/2); g2.dispose(); return new ImageIcon(img);
    }
    private String normalizeFolder(String h) {
        if(h==null) return "OTHER";
        switch(h.trim().toUpperCase()){
            case "DELL":return "DELL";case "ASUS":return "ASUS";case "HP":return "HP";
            case "ACER":return "ACER";case "MSI":return "MSI";case "LENOVO":return "LENOVO";
            case "APPLE":return "APPLE";case "GIGABYTE":return "GIGABYTE";case "LOGITECH":return "LOGITECH";
            default:return h.trim().toUpperCase();
        }
    }

    // =========================================================================
    // CART — thêm sản phẩm
    // =========================================================================
    private void addToCart(SanPhamDTO sp) {
        // soLuongTon trong DTO đã được giảm dần sau mỗi lần click
        // → chỉ cần > 0 là còn hàng để thêm
        if (sp.getSoLuongTon() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "S\u1ea3n ph\u1ea9m \u0111\u00e3 h\u1ebft h\u00e0ng!",
                    "Th\u00f4ng b\u00e1o", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maSP = String.valueOf(sp.getMaSP());
        boolean found = false;
        for (Object[] item : cartItems) {
            if (String.valueOf(item[0]).equals(maSP)) {
                int sl = (int) item[3];
                item[3] = sl + 1;
                item[4] = sp.getGia().doubleValue() * (sl + 1);
                showToast("T\u0103ng SL: " + sp.getTenSP() + " \u2192 " + (sl + 1));
                found = true;
                break;
            }
        }
        if (!found) {
            cartItems.add(new Object[]{ maSP, sp.getTenSP(), sp.getGia().doubleValue(), 1, sp.getGia().doubleValue() });
            showToast("\u0110\u00e3 th\u00eam: " + sp.getTenSP());
        }

        // Giảm tồn kho trong DTO ngay → badge "Còn X" cập nhật liền
        sp.setSoLuongTon(sp.getSoLuongTon() - 1);
        applyAllFilters();
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0); int i=1;
        for (Object[] item : cartItems)
            cartModel.addRow(new Object[]{ i++, item[1], formatMoney((double)item[2]), item[3], formatMoney((double)item[4]), false });
        recalcTotal();
    }

    // =========================================================================
    // TITLE PANEL
    // =========================================================================
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,PRIMARY_DARK,getWidth(),0,PRIMARY));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12); g2.dispose();
            }
        };
        panel.setOpaque(false); panel.setPreferredSize(new Dimension(0,58));

        JPanel leftPanel=new JPanel(new GridBagLayout()); leftPanel.setOpaque(false);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0,14,0,0));
        JComponent cartIcon=new JComponent(){{setPreferredSize(new Dimension(28,58));setOpaque(false);}
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                int cy=getHeight()/2-4;
                g2.drawLine(2,cy-6,6,cy-6);g2.drawLine(6,cy-6,9,cy+6);g2.drawLine(9,cy+6,22,cy+6);
                g2.drawLine(22,cy+6,25,cy-1);g2.drawLine(25,cy-1,9,cy-1);g2.fillOval(11,cy+8,4,4);g2.fillOval(19,cy+8,4,4);g2.dispose();
            }
        };
        JLabel title=new JLabel("  QU\u1ea2N L\u00dd B\u00c1N H\u00c0NG"); title.setFont(FONT_TITLE); title.setForeground(WHITE);
        GridBagConstraints lgc=new GridBagConstraints(); lgc.anchor=GridBagConstraints.CENTER; lgc.insets=new Insets(0,0,0,4);
        lgc.gridx=0; leftPanel.add(cartIcon,lgc); lgc.gridx=1; lgc.insets=new Insets(0,0,0,0); leftPanel.add(title,lgc);
        panel.add(leftPanel,BorderLayout.WEST);

        // ── Nút Đơn chờ ở góc phải header chính ─────────────────────────────
        final BanHangPanel2 self = this;
        JPanel rightPanel = new JPanel(new GridBagLayout()); rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,16));
        JButton btnDonCho = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền pill vàng
                Color bg2 = getModel().isRollover() ? new Color(255,215,30) : new Color(255,185,0);
                g2.setColor(bg2);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                // Tính toán vị trí căn giữa (icon 14px + gap 5px + text)
                g2.setFont(new Font("Segoe UI",Font.BOLD,12));
                FontMetrics fm2=g2.getFontMetrics();
                String txt2="\u0110\u01a1n ch\u1edd";
                int iconW=14, gap=5, tw2=fm2.stringWidth(txt2);
                int totalW2=iconW+gap+tw2;
                int startX2=(getWidth()-totalW2)/2;
                int midY=getHeight()/2;
                // Vẽ icon đồng hồ
                int icx=startX2+iconW/2, icy=midY;
                g2.setColor(new Color(20,50,110));
                g2.setStroke(new BasicStroke(1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g2.drawOval(icx-iconW/2,icy-iconW/2,iconW,iconW);
                g2.drawLine(icx,icy-4,icx,icy);
                g2.drawLine(icx,icy,icx+3,icy+2);
                // Vẽ chữ
                int ty2=(getHeight()-fm2.getHeight())/2+fm2.getAscent();
                g2.drawString(txt2, startX2+iconW+gap, ty2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){ return new Dimension(118,34); }
        };
        btnDonCho.setContentAreaFilled(false); btnDonCho.setBorderPainted(false);
        btnDonCho.setFocusPainted(false); btnDonCho.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDonCho.setToolTipText("Xem h\u00f3a \u0111\u01a1n \u0111ang ch\u1edd x\u1eed l\u00fd");
        btnDonCho.addActionListener(e -> self.showDanhSachDonCho());
        GridBagConstraints rc=new GridBagConstraints(); rc.anchor=GridBagConstraints.CENTER;
        rightPanel.add(btnDonCho, rc);
        panel.add(rightPanel,BorderLayout.EAST);

        return panel;
    }

    // =========================================================================
    // FILTER PANEL — Loại (từ DB) → Sub-filter → Giá
    // =========================================================================
    // =========================================================================
    // FILTER PANEL — giống SanPhamPanel: Loại tab + Hãng combo + Phụ kiện combo + Giá tab
    // =========================================================================
    private JPanel createFilterPanel() {
        JPanel bar = new JPanel(new BorderLayout(0, 0));
        bar.setBackground(CONTENT_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        // ── Hàng trên: Search + nút Tìm kiếm ────────────────────────────────────
        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setBackground(CONTENT_BG);
        topRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JPanel searchBar = new JPanel(new BorderLayout());
        searchBar.setBackground(WHITE);
        searchBar.setBorder(new CompoundBorder(new LineBorder(new Color(180,210,240),1,true), BorderFactory.createEmptyBorder()));
        searchBar.setPreferredSize(new Dimension(0, 36));
        JComponent searchIcon = new JComponent() {
            { setPreferredSize(new Dimension(38, 36)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=14, cy=getHeight()/2-1, r=7;
                g2.setColor(new Color(160,185,220));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(cx-r,cy-r,r*2,r*2); g2.drawLine(cx+r-2,cy+r-2,cx+r+4,cy+r+4);
                g2.dispose();
            }
        };
        txtSearch = new JTextField();
        txtSearch.setFont(FONT_NORMAL);
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0,4,0,0));
        txtSearch.setOpaque(false);
        txtSearch.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyAllFilters(); }
        });
        JButton btnSearch = createActionButton("Tìm kiếm", PRIMARY, WHITE);
        btnSearch.setPreferredSize(new Dimension(105, 36));
        btnSearch.addActionListener(e -> applyAllFilters());
        searchBar.add(searchIcon, BorderLayout.WEST);
        searchBar.add(txtSearch,  BorderLayout.CENTER);
        searchBar.add(btnSearch,  BorderLayout.EAST);
        topRow.add(searchBar, BorderLayout.CENTER);
        bar.add(topRow, BorderLayout.NORTH);

        // ── Các hàng filter bên dưới ──────────────────────────────────────────────────
        JPanel filterRows = new JPanel();
        filterRows.setLayout(new BoxLayout(filterRows, BoxLayout.Y_AXIS));
        filterRows.setOpaque(false);

        // ── Hàng 1: Loại (tab button: Tất cả | Laptop | Phụ kiện) ───────────────────────
        JPanel groupRow = new JPanel(new BorderLayout(8, 0)); groupRow.setOpaque(false);
        groupRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        JLabel lblLoai = new JLabel("Loại:"); lblLoai.setFont(FONT_LABEL); lblLoai.setForeground(PRIMARY);
        lblLoai.setPreferredSize(new Dimension(42, 30));
        JPanel groupTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2)); groupTabs.setOpaque(false);
        groupBtns.clear();
        String[][] groups = {{"Tat_ca","Tất cả"},{"Laptop","Laptop"},{"PhuKien","Phụ kiện"}};
        for (String[] g : groups) {
            final String gKey = g[0], gDisp = g[1];
            JButton btn = buildGroupTab(gKey, gDisp);
            btn.addActionListener(e -> {
                filterGroup = gKey; filterSubLoai = -1; filterLaptopBrand = "Tat_ca";
                refreshGroupTabs();
                if (cbLaptopBrand != null) cbLaptopBrand.setSelectedIndex(0);
                if (subLoaiRow    != null) {
                    subLoaiRow.setVisible(gKey.equals("PhuKien"));
                    if (cbSubLoai != null) cbSubLoai.setSelectedIndex(0);
                }
                filterRows.revalidate(); filterRows.repaint();
                applyAllFilters();
            });
            groupTabs.add(btn); groupBtns.add(btn);
        }
        groupRow.add(lblLoai,   BorderLayout.WEST);
        groupRow.add(groupTabs, BorderLayout.CENTER);

        // ── Hàng 2: Hãng (combo, hiện cho mọi tab) ───────────────────────────────────
        JPanel laptopBrandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        laptopBrandRow.setOpaque(false);
        laptopBrandRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        laptopBrandRow.setVisible(true);
        JLabel lblHang = new JLabel("Hãng:"); lblHang.setFont(FONT_LABEL); lblHang.setForeground(PRIMARY);
        cbLaptopBrand = new JComboBox<>();
        cbLaptopBrand.setFont(FONT_NORMAL);
        cbLaptopBrand.setPreferredSize(new Dimension(180, 32));
        cbLaptopBrand.setBorder(BorderFactory.createLineBorder(new Color(180,210,240), 1));
        cbLaptopBrand.addActionListener(e -> {
            if (isLoadingBrands) return;
            filterLaptopBrand = cbLaptopBrand.getSelectedIndex() <= 0
                    ? "Tat_ca" : cbLaptopBrand.getSelectedItem().toString();
            applyAllFilters();
        });
        laptopBrandRow.add(lblHang); laptopBrandRow.add(cbLaptopBrand);

        // ── Hàng 3: Phụ kiện con (combo, chỉ hiện khi tab PhuKien) ──────────────────────
        subLoaiRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        subLoaiRow.setOpaque(false);
        subLoaiRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        subLoaiRow.setVisible(false);
        JLabel lblSub = new JLabel("Loại:"); lblSub.setFont(FONT_LABEL); lblSub.setForeground(PRIMARY);
        String[] subItems = new String[MALOAI_PHUKIEN.length + 1];
        subItems[0] = "Tất cả phụ kiện";
        for (int i = 0; i < MALOAI_PHUKIEN.length; i++)
            subItems[i+1] = loaiMap.getOrDefault(MALOAI_PHUKIEN[i], "Loại " + MALOAI_PHUKIEN[i]);
        cbSubLoai = new JComboBox<>(subItems);
        cbSubLoai.setFont(FONT_NORMAL); cbSubLoai.setPreferredSize(new Dimension(180, 32));
        cbSubLoai.setBorder(BorderFactory.createLineBorder(new Color(180,210,240), 1));
        cbSubLoai.addActionListener(e -> {
            if (isRebuildingSubLoai) return;
            int idx = cbSubLoai.getSelectedIndex();
            filterSubLoai = (idx == 0) ? -1 : MALOAI_PHUKIEN[idx - 1];
            applyAllFilters();
        });
        subLoaiRow.add(lblSub); subLoaiRow.add(cbSubLoai);

        // ── Hàng 4: Giá (tab button) ────────────────────────────────────────────────────
        JPanel giaRow = new JPanel(new BorderLayout(8, 0)); giaRow.setOpaque(false);
        giaRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        JLabel lblGia = new JLabel("Giá:"); lblGia.setFont(FONT_LABEL); lblGia.setForeground(PRIMARY);
        lblGia.setPreferredSize(new Dimension(42, 30));
        JPanel giaTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2)); giaTabs.setOpaque(false);
        allGiaBtns.clear();
        for (String[] g : GIA_RANGES) {
            final String key = g[0], disp = g[1];
            JButton btn = buildGroupTab(key, disp);
            btn.addActionListener(e -> { activeGiaKey = key; refreshGroupTabs(); applyAllFilters(); });
            giaTabs.add(btn); allGiaBtns.add(btn);
        }
        giaRow.add(lblGia,   BorderLayout.WEST);
        giaRow.add(giaTabs,  BorderLayout.CENTER);

        filterRows.add(groupRow);
        filterRows.add(laptopBrandRow);
        filterRows.add(subLoaiRow);
        filterRows.add(giaRow);
        bar.add(filterRows, BorderLayout.CENTER);
        return bar;
    }

    private JButton buildGroupTab(String key, String label) {
        Canvas cv = new Canvas();
        FontMetrics fmC = cv.getFontMetrics(new Font("Segoe UI", Font.BOLD, 12));
        final int prefW = fmC.stringWidth(label) + 30;
        final Font TF   = new Font("Segoe UI", Font.BOLD, 12);
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                boolean active = key.equals(filterGroup) || key.equals(activeGiaKey);
                Color bg2 = active ? TAB_ACTIVE : (getModel().isRollover() ? new Color(210,235,255) : WHITE);
                g2.setColor(bg2); g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.setColor(active ? TAB_ACTIVE : TAB_INACTIVE);
                g2.setStroke(new BasicStroke(1.3f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.setFont(TF); g2.setColor(active ? WHITE : PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label,(getWidth()-fm.stringWidth(label))/2,(getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(prefW, 30); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
        };
        btn.setText(""); btn.setToolTipText(label);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void refreshGroupTabs() {
        for (JButton b : groupBtns)  b.repaint();
        for (JButton b : allGiaBtns) b.repaint();
    }


    /** Áp dụng tất cả filter: loại (maLoai) + hãng + giá + search */
    private void applyAllFilters() {
        String kw = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        double minGia = 0, maxGia = 999_999_999;
        for (String[] g : GIA_RANGES) {
            if (g[0].equals(activeGiaKey)) { minGia = Double.parseDouble(g[2]); maxGia = Double.parseDouble(g[3]); break; }
        }
        final double fMin = minGia, fMax = maxGia;

        ArrayList<SanPhamDTO> out = new ArrayList<>();
        for (SanPhamDTO sp : allProducts) {
            // Lọc hãng — áp dụng cho mọi tab (giống SanPhamPanel)
            if (!filterLaptopBrand.equals("Tat_ca") &&
                    !sp.getThuongHieu().equalsIgnoreCase(filterLaptopBrand)) continue;

            // Lọc theo nhóm Loại
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
            // Lọc giá
            double gia = sp.getGia().doubleValue();
            if (gia < fMin || gia > fMax) continue;

            // Lọc search
            if (!kw.isEmpty()) {
                boolean match = sp.getTenSP().toLowerCase().contains(kw)
                        || sp.getThuongHieu().toLowerCase().contains(kw)
                        || String.valueOf(sp.getMaSP()).contains(kw)
                        || (sp.getMauSac() != null && sp.getMauSac().toLowerCase().contains(kw));
                if (!match) continue;
            }
            out.add(sp);
        }
        renderProductGrid(out);
    }

    private JPanel createProductGridPanel(){
        JPanel wrapper=new JPanel(new BorderLayout()); wrapper.setBackground(WHITE); wrapper.setBorder(new LineBorder(new Color(180,210,240),1));
        productGrid=new JPanel(new WrapLayout(FlowLayout.LEFT,10,10));
        productGrid.setBackground(new Color(246,249,254)); productGrid.setBorder(BorderFactory.createEmptyBorder(12,12,2,12));
        gridScroll=new JScrollPane(productGrid); gridScroll.setBorder(null); gridScroll.getVerticalScrollBar().setUnitIncrement(16);
        gridScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // Khi resize viewport, buộc productGrid tính lại layout theo chiều rộng mới
        gridScroll.getViewport().addChangeListener(e -> {
            int vw = gridScroll.getViewport().getWidth();
            if (vw > 0) {
                productGrid.setPreferredSize(null); // reset để WrapLayout tự tính
                productGrid.revalidate();
                gridScroll.revalidate();
            }
        });
        wrapper.add(gridScroll,BorderLayout.CENTER); return wrapper;
    }

    // WrapLayout: FlowLayout nhưng tự xuống dòng và không kéo dài component
    private static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) { return layoutSize(target, false); }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                // Lấy width từ parent (viewport) nếu có
                int targetWidth = target.getWidth();
                Container parent = target.getParent();
                if (parent instanceof javax.swing.JViewport) {
                    targetWidth = parent.getWidth();
                }
                if (targetWidth <= 0) targetWidth = 800; // fallback

                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - insets.left - insets.right;
                int x = 0, y = insets.top + vgap, rowH = 0;
                boolean firstInRow = true;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component c = target.getComponent(i);
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    int spaceNeeded = firstInRow ? d.width : d.width + hgap;
                    if (!firstInRow && x + spaceNeeded > maxWidth) {
                        y += rowH + vgap; x = 0; rowH = 0; firstInRow = true; spaceNeeded = d.width;
                    }
                    x += spaceNeeded; rowH = Math.max(rowH, d.height); firstInRow = false;
                }
                y += rowH + vgap + insets.bottom;
                return new Dimension(targetWidth, y);
            }
        }
    }

    // =========================================================================
    // CART PANEL — bên phải ~25%
    // =========================================================================
    private JPanel createCartPanel() {
        JPanel panel=new JPanel(new BorderLayout()); panel.setBackground(CART_BG);
        panel.setBorder(new MatteBorder(0,1,0,0,new Color(180,210,240)));

        // ── Header giỏ hàng — 1 dòng ngang, tất cả co vừa ──────────────────
        JPanel header=new JPanel(new GridBagLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,PRIMARY_DARK,getWidth(),0,PRIMARY));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0,44));
        header.setBorder(BorderFactory.createEmptyBorder(0,8,0,6));

        // Icon cart
        JComponent cartIconW=new JComponent(){{setPreferredSize(new Dimension(18,18));setOpaque(false);}
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE); g2.setStroke(new BasicStroke(1.6f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                int cy=getHeight()/2;
                g2.drawLine(1,cy-4,3,cy-4); g2.drawLine(3,cy-4,6,cy+4);
                g2.drawLine(6,cy+4,16,cy+4); g2.drawLine(16,cy+4,18,cy-1); g2.drawLine(18,cy-1,6,cy-1);
                g2.fillOval(7,cy+5,3,3); g2.fillOval(13,cy+5,3,3); g2.dispose();
            }
        };
        JLabel lblCart=new JLabel(" GI\u1eCE H\u00c0NG");
        lblCart.setFont(new Font("Segoe UI",Font.BOLD,13)); lblCart.setForeground(WHITE);

        // Field Mã HD và Ngày — font nhỏ, không có border tốn chỗ
        Font fldFont = new Font("Segoe UI", Font.PLAIN, 12);
        txtMaHD.setFont(fldFont);    txtMaHD.setPreferredSize(new Dimension(56,24));
        txtNgayLap.setFont(fldFont); txtNgayLap.setPreferredSize(new Dimension(88,24));

        JLabel lbMa   = makeInlineLabel("M\u00e3 HD:");
        JLabel lbNgay = makeInlineLabel("Ng\u00e0y:");
        lbMa  .setFont(new Font("Segoe UI",Font.PLAIN,11));
        lbNgay.setFont(new Font("Segoe UI",Font.PLAIN,11));

        GridBagConstraints hc = new GridBagConstraints();
        hc.anchor = GridBagConstraints.CENTER;
        hc.insets = new Insets(0,2,0,2);

        hc.gridx=0; hc.weightx=0;   header.add(cartIconW, hc);
        hc.gridx=1; hc.weightx=0;   header.add(lblCart,   hc);
        hc.gridx=2; hc.weightx=1.0; // spacer
        JPanel spacer=new JPanel(); spacer.setOpaque(false);
        header.add(spacer, hc);
        hc.gridx=3; hc.weightx=0;   header.add(lbMa,      hc);
        hc.gridx=4; hc.weightx=0;   header.add(txtMaHD,   hc);
        hc.gridx=5; hc.weightx=0;   header.add(lbNgay,    hc);
        hc.gridx=6; hc.weightx=0;   header.add(txtNgayLap,hc);

        panel.add(header,BorderLayout.NORTH);

        // ── Bảng giỏ hàng ────────────────────────────────────────────────────
        String[] cols={"STT","S\u1ea3n ph\u1ea9m","\u0110\u01a1n gi\u00e1","SL","Th\u00e0nh ti\u1ec1n",""};
        cartModel=new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return c==3||c==5;}
            @Override public Class<?> getColumnClass(int c){return c==5?Boolean.class:Object.class;}
        };
        cartTable=new JTable(cartModel){
            @Override public Component prepareRenderer(TableCellRenderer r,int row,int col){
                Component c=super.prepareRenderer(r,row,col);
                if(!isRowSelected(row)) c.setBackground(row%2==0?WHITE:ROW_ALT);
                else c.setBackground(new Color(187,222,251));
                c.setFont(FONT_SMALL);
                if(c instanceof JLabel) ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        ((DefaultTableCellRenderer)cartTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        cartTable.setRowHeight(30); cartTable.setFont(FONT_SMALL);
        cartTable.setGridColor(new Color(220,230,245)); cartTable.setShowVerticalLines(true);
        cartTable.setSelectionBackground(new Color(187,222,251)); cartTable.setSelectionForeground(PRIMARY_DARK);
        cartTable.setIntercellSpacing(new Dimension(0,1));

        // Fix header luôn hiển thị đúng màu
        JTableHeader tableHeader = cartTable.getTableHeader();
        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                lbl.setBackground(TABLE_HEADER);
                lbl.setForeground(WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(40, 120, 210)));
                return lbl;
            }
        });
        tableHeader.setBackground(TABLE_HEADER);
        tableHeader.setForeground(WHITE);
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tableHeader.setPreferredSize(new Dimension(0, 30));
        tableHeader.setReorderingAllowed(false);
        tableHeader.setResizingAllowed(false);
        cartModel.addTableModelListener(e->{ if(e.getColumn()==3 && !isRecalculating) recalcTotal(); });

        // Tắt auto-resize để các cột giữ đúng tỉ lệ đã set
        cartTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        // Cursor HAND khi hover trên dòng có dữ liệu
        cartTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = cartTable.rowAtPoint(e.getPoint());
                cartTable.setCursor(row >= 0
                        ? new Cursor(Cursor.HAND_CURSOR)
                        : new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        cartTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                cartTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        TableColumnModel tcm = cartTable.getColumnModel();
        // STT
        tcm.getColumn(0).setPreferredWidth(32); tcm.getColumn(0).setMinWidth(30); tcm.getColumn(0).setMaxWidth(40);
        // Sản phẩm — co giãn tự do
        tcm.getColumn(1).setPreferredWidth(110);
        // Đơn giá
        tcm.getColumn(2).setPreferredWidth(72); tcm.getColumn(2).setMinWidth(65);
        // SL
        tcm.getColumn(3).setPreferredWidth(34); tcm.getColumn(3).setMinWidth(30); tcm.getColumn(3).setMaxWidth(45);
        // Thành tiền
        tcm.getColumn(4).setPreferredWidth(80); tcm.getColumn(4).setMinWidth(70);
        // Cột X xóa — cố định 32px, header trống
        tcm.getColumn(5).setPreferredWidth(32); tcm.getColumn(5).setMinWidth(32); tcm.getColumn(5).setMaxWidth(32);

        // ── Renderer vẽ dấu X bằng Graphics2D ───────────────────────────────
        TableCellRenderer xRenderer = new TableCellRenderer() {
            private final JPanel cell = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int cx = getWidth()/2, cy = getHeight()/2, r = 8;
                    // Vòng tròn đỏ
                    g2.setColor(new Color(211,47,47));
                    g2.fillOval(cx-r, cy-r, r*2, r*2);
                    // Dấu X trắng
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int p = 4;
                    g2.drawLine(cx-p, cy-p, cx+p, cy+p);
                    g2.drawLine(cx+p, cy-p, cx-p, cy+p);
                    g2.dispose();
                }
            };
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                cell.setBackground(sel ? new Color(187,222,251) : (row%2==0 ? WHITE : ROW_ALT));
                cell.setOpaque(true);
                return cell;
            }
        };

        // ── Editor: click vào X → xóa dòng ngay (DefaultCellEditor chuẩn Java) ──
        JPanel xEditorCell = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth()/2, cy = getHeight()/2, r = 8;
                g2.setColor(new Color(183,28,28));
                g2.fillOval(cx-r, cy-r, r*2, r*2);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int p = 4;
                g2.drawLine(cx-p, cy-p, cx+p, cy+p);
                g2.drawLine(cx+p, cy-p, cx-p, cy+p);
                g2.dispose();
            }
        };
        TableCellEditor xEditor = new DefaultCellEditor(new JCheckBox()) {
            @Override
            public Component getTableCellEditorComponent(
                    JTable t, Object v, boolean sel, int row, int col) {
                xEditorCell.setBackground(sel ? new Color(187,222,251)
                        : (row%2==0 ? WHITE : ROW_ALT));
                SwingUtilities.invokeLater(() -> {
                    int editRow = (t.getEditingRow() >= 0) ? t.getEditingRow() : row;
                    stopCellEditing();
                    if (editRow >= 0 && editRow < cartItems.size()) {
                        Object[] item = cartItems.get(editRow);
                        int maSPInt = Integer.parseInt(item[0].toString().trim());
                        int slHoan  = Integer.parseInt(item[3].toString());
                        for (SanPhamDTO sp : allProducts) {
                            if (sp.getMaSP() == maSPInt) {
                                int goc = tonKhoGoc.getOrDefault(maSPInt, 0);
                                sp.setSoLuongTon(Math.min(sp.getSoLuongTon() + slHoan, goc));
                                break;
                            }
                        }
                        cartItems.remove(editRow);
                        cartModel.removeRow(editRow);
                        for (int i = 0; i < cartModel.getRowCount(); i++)
                            cartModel.setValueAt(i + 1, i, 0);
                        applyAllFilters();
                        recalcTotal();
                    }
                });
                return xEditorCell;
            }
            @Override public Object getCellEditorValue() { return false; }
        };

        tcm.getColumn(5).setCellRenderer(xRenderer);
        tcm.getColumn(5).setCellEditor(xEditor);
        JScrollPane sc=new JScrollPane(cartTable); sc.setBorder(new LineBorder(new Color(180,210,240),1));

        // ── Toast hiển thị ở dải nhỏ phía trên footer ────────────────────────
        lblToast = new JLabel("", SwingConstants.CENTER);
        lblToast.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblToast.setForeground(WHITE);
        lblToast.setOpaque(true);
        lblToast.setBackground(new Color(46,125,50));
        lblToast.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        lblToast.setVisible(false);
        lblToast.setPreferredSize(new Dimension(0, 30));

        JPanel centerWithToast = new JPanel(new BorderLayout(0,0));
        centerWithToast.add(sc, BorderLayout.CENTER);
        centerWithToast.add(lblToast, BorderLayout.SOUTH);
        centerWithToast.setBackground(CART_BG);
        panel.add(centerWithToast, BorderLayout.CENTER);

        panel.add(buildCartFooter(),BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCartFooter() {
        JPanel outer=new JPanel(new BorderLayout(0,4)); outer.setBackground(CART_BG);
        outer.setBorder(BorderFactory.createEmptyBorder(5,6,6,6));

        JPanel cust=new JPanel(new GridBagLayout()); cust.setBackground(WHITE);
        cust.setBorder(new CompoundBorder(new LineBorder(new Color(180,210,240),1),
                BorderFactory.createEmptyBorder(5,7,5,7)));
        txtKhachHang=new JTextField("Kh\u00e1ch l\u1ebb"); txtSDT=new JTextField();
        styleCartField(txtKhachHang); styleCartField(txtSDT);
        // Khóa txtKhachHang — tự động cập nhật theo SĐT
        txtKhachHang.setEditable(false);
        txtKhachHang.setBackground(new Color(240,244,250));
        txtKhachHang.setForeground(new Color(100,130,170));
        txtKhachHang.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        txtKhachHang.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        GridBagConstraints gc=new GridBagConstraints();
        gc.insets=new Insets(2,2,2,2); gc.anchor=GridBagConstraints.WEST;

        gc.gridx=0; gc.gridy=0; gc.fill=GridBagConstraints.NONE; gc.weightx=0;
        cust.add(makeCartLabel("Kh\u00e1ch h\u00e0ng:"),gc);
        gc.gridx=1; gc.gridy=0; gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1.0; gc.gridwidth=3;
        cust.add(txtKhachHang,gc); gc.gridwidth=1;

        gc.gridx=0; gc.gridy=1; gc.fill=GridBagConstraints.NONE; gc.weightx=0;
        cust.add(makeCartLabel("S\u0110T:"),gc);
        gc.gridx=1; gc.gridy=1; gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1.0; gc.gridwidth=3;
        cust.add(txtSDT,gc); gc.gridwidth=1;

        // Row 2: cảnh báo SĐT
        warnSDT = new WarningLabel();
        gc.gridx=1; gc.gridy=2; gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1.0; gc.gridwidth=3;
        gc.insets=new Insets(0,2,2,2); cust.add(warnSDT,gc); gc.gridwidth=1; gc.insets=new Insets(2,2,2,2);

        gc.gridx=0; gc.gridy=3; gc.fill=GridBagConstraints.NONE; gc.weightx=0;
        cust.add(makeCartLabel("Thanh to\u00e1n:"),gc);
        ButtonGroup bg=new ButtonGroup();
        rbTienMat=new JRadioButton("Ti\u1ec1n m\u1eb7t",true);
        rbChuyenKhoan=new JRadioButton("Chuy\u1ec3n kho\u1ea3n",false);
        styleRadio(rbTienMat); styleRadio(rbChuyenKhoan);
        bg.add(rbTienMat); bg.add(rbChuyenKhoan);
        rbTienMat.addActionListener(e->onPayChange()); rbChuyenKhoan.addActionListener(e->onPayChange());
        JPanel payRow=new JPanel(new FlowLayout(FlowLayout.LEFT,4,0)); payRow.setBackground(WHITE);
        payRow.add(rbTienMat); payRow.add(rbChuyenKhoan);
        gc.gridx=1; gc.gridy=3; gc.gridwidth=3; gc.fill=GridBagConstraints.HORIZONTAL;
        cust.add(payRow,gc); gc.gridwidth=1;

        // Hàng 4: Giảm — label readonly tự động từ hạng KH
        gc.gridx=0; gc.gridy=4; gc.fill=GridBagConstraints.NONE; gc.weightx=0;
        cust.add(makeCartLabel("Gi\u1ea3m:"),gc);
        lblGiamInfo = new JLabel("Kh\u00f4ng c\u00f3");
        lblGiamInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGiamInfo.setForeground(new Color(100, 130, 170));
        lblGiamInfo.setBorder(new CompoundBorder(new LineBorder(new Color(180,210,240),1),
                BorderFactory.createEmptyBorder(4,8,4,8)));
        lblGiamInfo.setBackground(new Color(245,248,255)); lblGiamInfo.setOpaque(true);
        gc.gridx=1; gc.gridy=4; gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1.0; gc.gridwidth=3;
        cust.add(lblGiamInfo,gc); gc.gridwidth=1;

        gc.gridx=0; gc.gridy=5; gc.fill=GridBagConstraints.NONE; gc.weightx=0;
        cust.add(makeCartLabel("Ti\u1ec1n nh\u1eadn:"),gc);
        txtTienNhan=new JTextField("0"); styleCartField(txtTienNhan);
        gc.gridx=1; gc.gridy=5; gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1.0; gc.gridwidth=3;
        cust.add(txtTienNhan,gc); gc.gridwidth=1;

        // Row 6: cảnh báo tiền nhận
        warnTienNhan = new WarningLabel();
        gc.gridx=1; gc.gridy=6; gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1.0; gc.gridwidth=3;
        gc.insets=new Insets(0,2,2,2); cust.add(warnTienNhan,gc); gc.gridwidth=1; gc.insets=new Insets(2,2,2,2);

        // Listener SĐT: validate ký tự ngay lập tức, debounce tra cứu DB 400ms
        javax.swing.Timer[] debounce = { null };
        javax.swing.event.DocumentListener khListener = new javax.swing.event.DocumentListener() {
            private void onChanged() {
                String sdt = txtSDT.getText().trim();
                if (sdt.isEmpty()) {
                    warnSDT.hideWarning();
                    txtSDT.setBorder(new CompoundBorder(
                            new LineBorder(new Color(180,210,240),1),
                            BorderFactory.createEmptyBorder(2,6,2,6)));
                    clearKhachHang(); return;
                }
                // Hiện lỗi ngay nếu có ký tự không phải số
                if (!sdt.matches("\\d+")) {
                    warnSDT.showWarning("S\u0110T kh\u00f4ng h\u1ee3p l\u1ec7 (ch\u1ec9 \u0111\u01b0\u1ee3c nh\u1eadp s\u1ed1)");
                    txtSDT.setBorder(new CompoundBorder(
                            new LineBorder(DANGER,1),
                            BorderFactory.createEmptyBorder(2,6,2,6)));
                    clearKhachHang(); return;
                }
                // Đủ 10 số mới tra DB, chưa đủ thì ẩn cảnh báo
                warnSDT.hideWarning();
                txtSDT.setBorder(new CompoundBorder(
                        new LineBorder(new Color(180,210,240),1),
                        BorderFactory.createEmptyBorder(2,6,2,6)));
                if (debounce[0] != null) debounce[0].stop();
                debounce[0] = new javax.swing.Timer(400, e -> lookupKhachHang());
                debounce[0].setRepeats(false); debounce[0].start();
            }
            @Override public void insertUpdate (javax.swing.event.DocumentEvent e) { onChanged(); }
            @Override public void removeUpdate (javax.swing.event.DocumentEvent e) { onChanged(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onChanged(); }
        };
        // Chỉ lắng nghe SĐT — tên KH tự động điền, không cho nhập tay
        txtSDT.getDocument().addDocumentListener(khListener);
        // Listener tiền nhận: validate ngay khi gõ bằng DocumentListener
        txtTienNhan.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate (javax.swing.event.DocumentEvent e) { validateTienNhan(); recalcChange(); }
            @Override public void removeUpdate (javax.swing.event.DocumentEvent e) { validateTienNhan(); recalcChange(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { validateTienNhan(); recalcChange(); }
        });
        outer.add(cust,BorderLayout.NORTH);

        JPanel summary=new JPanel(new GridLayout(3,1,0,0));
        summary.setBackground(new Color(8,50,110));
        summary.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
        lblTongTien =new JLabel("T\u1ed5ng ti\u1ec1n h\u00e0ng:  0 \u0111");
        lblThanhTien=new JLabel("Th\u00e0nh ti\u1ec1n (+10% VAT):  0 \u0111");
        lblTienThua =new JLabel("Ti\u1ec1n th\u1eeba:  0 \u0111");
        lblTongTien .setFont(FONT_SMALL); lblTongTien .setForeground(new Color(255,220,100));
        lblThanhTien.setFont(new Font("Segoe UI",Font.BOLD,13)); lblThanhTien.setForeground(new Color(100,255,180));
        lblTienThua .setFont(FONT_SMALL); lblTienThua .setForeground(WHITE);
        summary.add(lblTongTien); summary.add(lblThanhTien); summary.add(lblTienThua);
        outer.add(summary,BorderLayout.CENTER);

        JPanel btnRow=new JPanel(new GridLayout(1,3,4,0)); btnRow.setBackground(CART_BG);
        btnRow.setBorder(BorderFactory.createEmptyBorder(4,0,0,0));
        JButton btnReset  =createActionButton("L\u00e0m m\u1edbi",new Color(90,100,115),WHITE);
        JButton btnLuuTam =createActionButton("L\u01b0u Order",new Color(230,120,0),WHITE);
        JButton btnTT     =createActionButton("THANH TO\u00c1N",SUCCESS,WHITE);
        btnReset .setFont(new Font("Segoe UI",Font.BOLD,11));
        btnLuuTam.setFont(new Font("Segoe UI",Font.BOLD,11));
        btnTT    .setFont(new Font("Segoe UI",Font.BOLD,12));
        for(JButton b:new JButton[]{btnReset,btnLuuTam,btnTT}) b.setPreferredSize(new Dimension(0,38));
        btnReset .addActionListener(e->resetCart());
        btnLuuTam.addActionListener(e->doLuuHoaDonTam());
        btnTT    .addActionListener(e->doCheckout());
        btnRow.add(btnReset); btnRow.add(btnLuuTam); btnRow.add(btnTT);
        outer.add(btnRow,BorderLayout.SOUTH);
        return outer;
    }

    // =========================================================================
    // TRA CỨU KHÁCH HÀNG THEO TÊN + SĐT → hiện giảm giá tự động
    // =========================================================================
    private void lookupKhachHang() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) { clearKhachHang(); return; }

        // Tìm KH theo SĐT trong DB
        List<KhachHangDTO> ds = khachHangBUS.search("", null);
        KhachHangDTO found = null;
        for (KhachHangDTO kh : ds) {
            if (sdt.equals(kh.getSoDienThoai())) { found = kh; break; }
        }

        if (found != null) {
            khachHangHienTai = found;
            phanTramGiamKH   = khachHangBUS.getPhanTramGiam(found);
            // Tự động điền tên KH, đổi màu chữ về bình thường
            txtKhachHang.setText(found.getTenKhachHang());
            txtKhachHang.setForeground(new Color(10,60,130));
            txtKhachHang.setFont(new Font("Segoe UI", Font.BOLD, 13));
            String hangHienThi = KhachHangBUS.hangDisplayName(found.getHangKhachHang());
            if (phanTramGiamKH > 0) {
                lblGiamInfo.setText((int)phanTramGiamKH + "% - H\u1ea1ng " + hangHienThi);
                lblGiamInfo.setForeground(new Color(21,101,192));
            } else {
                lblGiamInfo.setText("0% - H\u1ea1ng " + hangHienThi);
                lblGiamInfo.setForeground(new Color(100,130,170));
            }
        } else {
            clearKhachHang();
        }
        recalcTotal();
    }

    private void clearKhachHang() {
        khachHangHienTai = null; phanTramGiamKH = 0.0;
        // Reset tên về "Khách lẻ" với style italic xám
        txtKhachHang.setText("Kh\u00e1ch l\u1ebb");
        txtKhachHang.setForeground(new Color(100,130,170));
        txtKhachHang.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        if (lblGiamInfo != null) {
            lblGiamInfo.setText("Kh\u00f4ng c\u00f3");
            lblGiamInfo.setForeground(new Color(100,130,170));
        }
    }

    // =========================================================================
    // VALIDATE SĐT & TIỀN NHẬN
    // =========================================================================
    private boolean validateSDT(String sdt) {
        if (sdt.isEmpty()) { if(warnSDT!=null) warnSDT.hideWarning(); styleCartField(txtSDT); return true; }
        if (!sdt.matches("\\d+")) {
            if(warnSDT!=null) warnSDT.showWarning("S\u0110T kh\u00f4ng h\u1ee3p l\u1ec7 (ch\u1ec9 \u0111\u01b0\u1ee3c nh\u1eadp s\u1ed1)");
            txtSDT.setBorder(new CompoundBorder(new LineBorder(DANGER,1),BorderFactory.createEmptyBorder(2,6,2,6))); return false;
        }
        if (sdt.length() != 10) {
            if(warnSDT!=null) warnSDT.showWarning("S\u0110T ph\u1ea3i c\u00f3 \u0111\u00fang 10 ch\u1eef s\u1ed1");
            txtSDT.setBorder(new CompoundBorder(new LineBorder(DANGER,1),BorderFactory.createEmptyBorder(2,6,2,6))); return false;
        }
        if(warnSDT!=null) warnSDT.hideWarning(); styleCartField(txtSDT); return true;
    }

    private boolean validateTienNhan() {
        if (!rbTienMat.isSelected()) { if(warnTienNhan!=null) warnTienNhan.hideWarning(); return true; }
        String raw = txtTienNhan.getText().trim().replace(",","").replace(".","");
        if (raw.isEmpty() || raw.equals("0")) { if(warnTienNhan!=null) warnTienNhan.hideWarning(); styleCartField(txtTienNhan); return true; }
        if (!raw.matches("\\d+")) {
            if(warnTienNhan!=null) warnTienNhan.showWarning("Ti\u1ec1n nh\u1eadn kh\u00f4ng h\u1ee3p l\u1ec7 (ch\u1ec9 \u0111\u01b0\u1ee3c nh\u1eadp s\u1ed1)");
            txtTienNhan.setBorder(new CompoundBorder(new LineBorder(DANGER,1),BorderFactory.createEmptyBorder(2,6,2,6))); return false;
        }
        if(warnTienNhan!=null) warnTienNhan.hideWarning(); styleCartField(txtTienNhan); return true;
    }

    // =========================================================================
    // TÍNH TIỀN
    // =========================================================================
    private void recalcTotal() {
        if (isRecalculating) return;
        isRecalculating = true;
        try {
            tongTien = 0;
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                try {
                    int sl = Integer.parseInt(cartModel.getValueAt(i, 3).toString().trim());
                    if (sl < 1) { sl = 1; cartModel.setValueAt(1, i, 3); }
                    if (i < cartItems.size()) {
                        int maSPInt = Integer.parseInt(cartItems.get(i)[0].toString().trim());
                        int slCu    = Integer.parseInt(cartItems.get(i)[3].toString());
                        int tonGoc  = tonKhoGoc.getOrDefault(maSPInt, 0);
                        if (sl != slCu) {
                            if (sl > tonGoc) {
                                sl = tonGoc; cartModel.setValueAt(sl, i, 3);
                                JOptionPane.showMessageDialog(this,
                                        "Kh\u00f4ng \u0111\u1ee7 s\u1ed1 l\u01b0\u1ee3ng!\nT\u1ed3n kho ch\u1ec9 c\u00f2n " + tonGoc + " s\u1ea3n ph\u1ea9m.",
                                        "Kh\u00f4ng \u0111\u1ee7 s\u1ed1 l\u01b0\u1ee3ng", JOptionPane.WARNING_MESSAGE);
                            }
                            for (SanPhamDTO sp : allProducts)
                                if (sp.getMaSP() == maSPInt) { sp.setSoLuongTon(tonGoc - sl); break; }
                            cartItems.get(i)[3] = sl;
                            applyAllFilters();
                        }
                    }
                    double gia   = Double.parseDouble(cartModel.getValueAt(i, 2).toString().replace(",","").replace(".","").trim());
                    double thanh = sl * gia;
                    cartModel.setValueAt(formatMoney(thanh), i, 4);
                    tongTien += thanh;
                    if (i < cartItems.size()) cartItems.get(i)[4] = thanh;
                } catch (Exception ignored) {}
            }
            double giam        = tongTien * phanTramGiamKH / 100.0;
            double vat         = Math.max(0, tongTien - giam) * 0.10;
            double thanhTienVAT = Math.max(0, tongTien - giam + vat);
            lblTongTien .setText("T\u1ed5ng ti\u1ec1n h\u00e0ng:  " + formatMoney(tongTien) + " \u0111");
            lblThanhTien.setText("Th\u00e0nh ti\u1ec1n (+10% VAT):  " + formatMoney(thanhTienVAT) + " \u0111");
            recalcChange();
        } finally { isRecalculating = false; }
    }
    private void recalcChange() {
        double khach=0;
        try{khach=Double.parseDouble(txtTienNhan.getText().replace(",",""));}catch(Exception ignored){}
        double giam  = tongTien * phanTramGiamKH / 100.0;
        double vat   = Math.max(0, tongTien - giam) * 0.10;
        double thanh = Math.max(0, tongTien - giam + vat);
        double thua  = khach - thanh;
        lblTienThua.setText("Ti\u1ec1n th\u1eeba:  "+formatMoney(Math.max(0,thua))+" \u0111");
        lblTienThua.setForeground(thua>=0?new Color(100,255,180):new Color(255,100,100));
    }
    private void onPayChange(){
        boolean isMat=rbTienMat.isSelected();
        txtTienNhan.setEnabled(isMat); txtTienNhan.setBackground(isMat?WHITE:new Color(240,242,248));
        if(!isMat){ txtTienNhan.setText("0"); if(warnTienNhan!=null) warnTienNhan.hideWarning(); recalcChange(); }
    }
    private void removeCartRow(){
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui l\u00f2ng ch\u1ecdn d\u00f2ng c\u1ea7n x\u00f3a!", "Th\u00f4ng b\u00e1o", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (row < cartItems.size()) {
            Object[] item = cartItems.get(row);
            int maSPInt = Integer.parseInt(item[0].toString().trim());
            int slHoan  = Integer.parseInt(item[3].toString());
            for (SanPhamDTO sp : allProducts) {
                if (sp.getMaSP() == maSPInt) {
                    int goc = tonKhoGoc.getOrDefault(maSPInt, 0);
                    sp.setSoLuongTon(Math.min(sp.getSoLuongTon() + slHoan, goc)); break;
                }
            }
            cartItems.remove(row);
        }
        cartModel.removeRow(row);
        for (int i = 0; i < cartModel.getRowCount(); i++) cartModel.setValueAt(i + 1, i, 0);
        applyAllFilters(); recalcTotal();
    }
    private void resetCart(){
        cartModel.setRowCount(0); cartItems.clear();
        txtKhachHang.setText("Kh\u00e1ch l\u1ebb");
        txtKhachHang.setForeground(new Color(100,130,170));
        txtKhachHang.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        txtSDT.setText("");
        txtTienNhan.setText("0");
        khachHangHienTai = null; phanTramGiamKH = 0.0;
        if (lblGiamInfo != null) { lblGiamInfo.setText("Kh\u00f4ng c\u00f3"); lblGiamInfo.setForeground(new Color(100,130,170)); }
        if (warnSDT != null) warnSDT.hideWarning();
        if (warnTienNhan != null) warnTienNhan.hideWarning();
        styleCartField(txtSDT); styleCartField(txtTienNhan);
        rbTienMat.setSelected(true); onPayChange(); tongTien=0;
        lblTongTien .setText("T\u1ed5ng ti\u1ec1n h\u00e0ng:  0 \u0111");
        lblThanhTien.setText("Th\u00e0nh ti\u1ec1n (+10% VAT):  0 \u0111");
        lblTienThua .setText("Ti\u1ec1n th\u1eeba:  0 \u0111");
        loadProductData();
    }

    // =========================================================================
    // LƯU HÓA ĐƠN TẠM (PARK ORDER)
    // =========================================================================
    private void doLuuHoaDonTam() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Gi\u1ecf h\u00e0ng tr\u1ed1ng, kh\u00f4ng th\u1ec3 l\u01b0u!","Th\u00f4ng b\u00e1o",JOptionPane.WARNING_MESSAGE); return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "L\u01b0u t\u1ea1m h\u00f3a \u0111\u01a1n n\u00e0y?\nGi\u1ecf h\u00e0ng s\u1ebd \u0111\u01b0\u1ee3c reset \u0111\u1ec3 ti\u1ebfp t\u1ee5c b\u00e1n.",
                "X\u00e1c nh\u1eadn", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            List<ChiTietHoaDonDTO> chiTietList = new ArrayList<>();
            for (Object[] item : cartItems) {
                ChiTietHoaDonDTO ct = new ChiTietHoaDonDTO(0,
                        Integer.parseInt(item[0].toString()), 0,
                        Integer.parseInt(item[3].toString()),
                        new BigDecimal(item[2].toString()));
                chiTietList.add(ct);
            }
            HoaDonDTO hd = new HoaDonDTO();
            hd.setMaNV(maNVHienTai);
            hd.setTongTienHang(BigDecimal.valueOf(tongTien));
            String tenKH = txtKhachHang.getText().trim();
            String sdt   = txtSDT.getText().trim();
            if (!sdt.isEmpty()) { Integer maKH = hoaDonBUS.timHoacTaoKhachHang(tenKH, sdt); hd.setMaKhachHang(maKH); }
            int maHD = hoaDonBUS.luuHoaDonTam(hd, chiTietList);
            maHoaDonChoDangMo = 0; resetCart();
            JOptionPane.showMessageDialog(this,
                    "\u0110\u00e3 l\u01b0u t\u1ea1m h\u00f3a \u0111\u01a1n " + String.format("HD%03d", maHD) + "!\nGi\u1ecf h\u00e0ng s\u1eb5n s\u00e0ng cho \u0111\u01a1n m\u1edbi.",
                    "L\u01b0u th\u00e0nh c\u00f4ng", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,"L\u1ed7i khi l\u01b0u t\u1ea1m:\n"+ex.getMessage(),"L\u1ed7i",JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // DANH SÁCH ĐƠN CHỜ
    // =========================================================================
    private void showDanhSachDonCho() {
        ArrayList<HoaDonDTO> dsDonCho;
        try { dsDonCho = hoaDonBUS.getDanhSachHoaDonCho(); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this,"Kh\u00f4ng t\u1ea3i \u0111\u01b0\u1ee3c danh s\u00e1ch!\n"+ex.getMessage(),"L\u1ed7i",JOptionPane.ERROR_MESSAGE); return;
        }
        if (dsDonCho.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Kh\u00f4ng c\u00f3 h\u00f3a \u0111\u01a1n \u0111ang ch\u1edd!","Th\u00f4ng b\u00e1o",JOptionPane.INFORMATION_MESSAGE); return;
        }
        JDialog dlg = new JDialog((java.awt.Frame)SwingUtilities.getWindowAncestor(this),"\u0110\u01a1n H\u00e0ng \u0110ang Ch\u1edd X\u1eed L\u00fd",true);
        dlg.setSize(760,500); dlg.setLocationRelativeTo(this); dlg.setLayout(new BorderLayout(0,0));
        JPanel titleBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,PRIMARY_DARK,getWidth(),0,PRIMARY));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        titleBar.setOpaque(false); titleBar.setPreferredSize(new Dimension(0,42));
        titleBar.setBorder(BorderFactory.createEmptyBorder(0,14,0,10));
        JLabel lblTitle = new JLabel("\u23f0  Danh s\u00e1ch \u0111\u01a1n ch\u1edd x\u1eed l\u00fd");
        lblTitle.setFont(new Font("Segoe UI",Font.BOLD,14)); lblTitle.setForeground(WHITE);
        titleBar.add(lblTitle, BorderLayout.CENTER);
        dlg.add(titleBar, BorderLayout.NORTH);

        String[] cols2 = {"M\u00e3 HD","Th\u1eddi gian","Nh\u00e2n vi\u00ean","Kh\u00e1ch h\u00e0ng","T\u1ed5ng ti\u1ec1n"};
        DefaultTableModel tblModel = new DefaultTableModel(cols2,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        for (HoaDonDTO hd2 : dsDonCho) {
            NhanVienDTO nv = nhanVienBUS.timTheoMa(hd2.getMaNV());
            String tenNV = nv != null ? nv.getTenNV() : "---";
            String tenKH2 = "Kh\u00e1ch l\u1ebb";
            if (hd2.getMaKhachHang() != null) {
                KhachHangDTO kh2 = khachHangBUS.timTheoMa(hd2.getMaKhachHang());
                if (kh2 != null) tenKH2 = kh2.getTenKhachHang();
            }
            String ngay2 = hd2.getNgayLap() != null
                    ? hd2.getNgayLap().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "--";
            String tien2 = (hd2.getTongTienHang()!=null?formatMoney(hd2.getTongTienHang().doubleValue()):"0")+" \u0111";
            tblModel.addRow(new Object[]{String.format("HD%03d",hd2.getMaHoaDon()),ngay2,tenNV,tenKH2,tien2});
        }
        JTable tbl2 = new JTable(tblModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row%2==0 ? WHITE : ROW_ALT);
                    c.setForeground(PRIMARY_DARK);
                } else {
                    c.setBackground(new Color(187,222,251));
                    c.setForeground(PRIMARY_DARK);
                }
                c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (c instanceof JLabel) ((JLabel)c).setHorizontalAlignment(SwingConstants.LEFT);
                return c;
            }
        };
        tbl2.setFont(new Font("Segoe UI",Font.PLAIN,13)); tbl2.setRowHeight(36);
        tbl2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tbl2.setGridColor(new Color(220,230,245)); tbl2.setShowVerticalLines(true);
        tbl2.setIntercellSpacing(new Dimension(0,1));
        tbl2.setSelectionBackground(new Color(187,222,251)); tbl2.setSelectionForeground(PRIMARY_DARK);
        // Fix header màu PRIMARY, chữ WHITE
        tbl2.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl2 = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                lbl2.setBackground(PRIMARY); lbl2.setForeground(WHITE);
                lbl2.setFont(new Font("Segoe UI",Font.BOLD,12));
                lbl2.setHorizontalAlignment(SwingConstants.CENTER);
                lbl2.setOpaque(true);
                lbl2.setBorder(BorderFactory.createMatteBorder(0,0,1,1,new Color(40,120,210)));
                return lbl2;
            }
        });
        tbl2.getTableHeader().setBackground(PRIMARY); tbl2.getTableHeader().setForeground(WHITE);
        tbl2.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        tbl2.getTableHeader().setPreferredSize(new Dimension(0,32));

        JPanel pAction = new JPanel(new BorderLayout(8,6));
        pAction.setBackground(new Color(240,245,255));
        pAction.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1,0,0,0,new Color(200,215,240)),
                BorderFactory.createEmptyBorder(10,14,10,14)));
        pAction.setVisible(false);
        JLabel lblChon = new JLabel();
        lblChon.setFont(new Font("Segoe UI",Font.BOLD,12)); lblChon.setForeground(PRIMARY_DARK);
        JButton btnSua2   = makeOrderBtn("\u270e  S\u1eeda Order",   new Color(21,101,192));
        JButton btnHuy2   = makeOrderBtn("\u2715  H\u1ee7y Order",   DANGER);
        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); pBtns.setOpaque(false);
        pBtns.add(btnSua2); pBtns.add(btnHuy2);
        pAction.add(lblChon,BorderLayout.WEST); pAction.add(pBtns,BorderLayout.EAST);

        tbl2.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            int row = tbl2.getSelectedRow();
            if (row < 0) { pAction.setVisible(false); return; }
            HoaDonDTO hd2 = dsDonCho.get(row);
            lblChon.setText("\u0110ang ch\u1ecdn: "+String.format("HD%03d",hd2.getMaHoaDon())+"  |  "+tblModel.getValueAt(row,3));
            pAction.setVisible(true);
        });

        btnSua2.addActionListener(ev -> {
            int row = tbl2.getSelectedRow(); if (row<0) return;
            HoaDonDTO hd2 = dsDonCho.get(row);
            if (!cartItems.isEmpty()) {
                int cf = JOptionPane.showConfirmDialog(dlg,"Gi\u1ecf h\u00e0ng hi\u1ec7n t\u1ea1i s\u1ebd b\u1ecb x\u00f3a. Ti\u1ebfp t\u1ee5c?","X\u00e1c nh\u1eadn",JOptionPane.YES_NO_OPTION);
                if (cf!=JOptionPane.YES_OPTION) return;
            }
            try {
                ArrayList<ChiTietHoaDonDTO> chiTiet = hoaDonBUS.getChiTietHoaDonCho(hd2.getMaHoaDon());
                // KHÔNG hủy hóa đơn — giữ nguyên trạng thái ChoXuLy
                // Ghi nhớ mã HD để doCheckout() cập nhật đúng HD này
                resetCart();
                maHoaDonChoDangMo = hd2.getMaHoaDon();
                for (ChiTietHoaDonDTO ct : chiTiet) {
                    double dg = ct.getDonGia().doubleValue();
                    double tt = ct.getThanhTien()!=null?ct.getThanhTien().doubleValue():dg*ct.getSoLuong();
                    cartItems.add(new Object[]{ct.getMaSP(),ct.getTenSP(),dg,ct.getSoLuong(),tt});
                    for (SanPhamDTO sp : allProducts)
                        if (sp.getMaSP()==ct.getMaSP()) { sp.setSoLuongTon(Math.max(0,sp.getSoLuongTon()-ct.getSoLuong())); break; }
                }
                refreshCartTable(); applyAllFilters(); dlg.dispose();
                showToast("\u0110\u00e3 t\u1ea3i \u0111\u01a1n "+String.format("HD%03d",hd2.getMaHoaDon())+" v\u00e0o gi\u1ecf (\u0111ang s\u1eeda)");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg,"L\u1ed7i t\u1ea3i \u0111\u01a1n:\n"+ex.getMessage(),"L\u1ed7i",JOptionPane.ERROR_MESSAGE);
            }
        });

        btnHuy2.addActionListener(ev -> {
            int row = tbl2.getSelectedRow(); if (row<0) return;
            HoaDonDTO hd2 = dsDonCho.get(row);
            JDialog dlgHuy=new JDialog(dlg,"H\u1ee7y \u0111\u01a1n \u2014 "+String.format("HD%03d",hd2.getMaHoaDon()),true);
            dlgHuy.setSize(400,240); dlgHuy.setLocationRelativeTo(dlg); dlgHuy.setLayout(new BorderLayout());
            dlgHuy.getContentPane().setBackground(WHITE);
            JPanel pHuy=new JPanel(new BorderLayout(6,8)); pHuy.setBackground(WHITE); pHuy.setBorder(BorderFactory.createEmptyBorder(14,16,8,16));
            JLabel lblNote=new JLabel("L\u00fd do h\u1ee7y (ghi ch\u00fa):"); lblNote.setFont(new Font("Segoe UI",Font.BOLD,12));
            JTextArea txtNote=new JTextArea(3,20); txtNote.setFont(new Font("Segoe UI",Font.PLAIN,12));
            txtNote.setLineWrap(true); txtNote.setWrapStyleWord(true);
            txtNote.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(180,210,240),1),BorderFactory.createEmptyBorder(4,6,4,6)));
            pHuy.add(lblNote,BorderLayout.NORTH); pHuy.add(new JScrollPane(txtNote),BorderLayout.CENTER);
            JButton btnXNHuy=makeOrderBtn("\u2715  X\u00e1c nh\u1eadn h\u1ee7y",DANGER); btnXNHuy.setPreferredSize(new Dimension(180,36));
            btnXNHuy.addActionListener(e3 -> {
                try {
                    String lyDo=txtNote.getText().trim();
                    if (!lyDo.isEmpty()) hoaDonBUS.capNhatGhiChuHoaDon(hd2.getMaHoaDon(),lyDo);
                    hoaDonBUS.huyHoaDonCho(hd2.getMaHoaDon(),hd2.getTrangThai());
                    JOptionPane.showMessageDialog(dlgHuy,"\u0110\u00e3 h\u1ee7y h\u00f3a \u0111\u01a1n "+String.format("HD%03d",hd2.getMaHoaDon()),"Th\u00e0nh c\u00f4ng",JOptionPane.INFORMATION_MESSAGE);
                    dlgHuy.dispose(); dlg.dispose();
                } catch (Exception ex) { JOptionPane.showMessageDialog(dlgHuy,"L\u1ed7i h\u1ee7y:\n"+ex.getMessage(),"L\u1ed7i",JOptionPane.ERROR_MESSAGE); }
            });
            JPanel pS2=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,10)); pS2.setBackground(WHITE);
            pS2.add(btnXNHuy); JButton btnDongHuy=new JButton("\u0110\u00f3ng"); btnDongHuy.addActionListener(e3->dlgHuy.dispose()); pS2.add(btnDongHuy);
            dlgHuy.add(pHuy,BorderLayout.CENTER); dlgHuy.add(pS2,BorderLayout.SOUTH); dlgHuy.setVisible(true);
        });

        JPanel pClose=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,6)); pClose.setBackground(WHITE);
        JButton btnDong=new JButton("\u0110\u00f3ng"); btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnDong.addActionListener(e->dlg.dispose()); pClose.add(btnDong);
        JPanel south=new JPanel(new BorderLayout()); south.setBackground(WHITE);
        south.add(pAction,BorderLayout.CENTER); south.add(pClose,BorderLayout.SOUTH);
        dlg.add(new JScrollPane(tbl2),BorderLayout.CENTER); dlg.add(south,BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JButton makeOrderBtn(String label, Color bg) {
        // Xác định loại icon: "sua"=bút, "tt"=tick, "huy"=X, "xacnhan"=tick, khác=tick
        final String lbl = label;
        final Color bgColor = bg;
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền bo góc
                Color draw = getModel().isRollover() ? bgColor.darker() : bgColor;
                g2.setColor(draw);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                // Vẽ icon 16x16 ở bên trái
                int iconX = 12, iconY = getHeight()/2;
                g2.setColor(WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (lbl.contains("S\u1eeda") || lbl.contains("Sua")) {
                    // Icon bút viết
                    int[] px = {iconX-4, iconX+4, iconX+6, iconX-2};
                    int[] py = {iconY+4, iconY-4, iconY-2, iconY+6};
                    g2.drawPolygon(px, py, 4);
                    g2.drawLine(iconX-4, iconY+4, iconX-6, iconY+7);
                    g2.drawLine(iconX-6, iconY+7, iconX-3, iconY+5);
                } else if (lbl.contains("H\u1ee7y") || lbl.contains("Huy")) {
                    // Icon X tròn
                    g2.drawOval(iconX-7, iconY-7, 14, 14);
                    int p=4;
                    g2.drawLine(iconX-p+1, iconY-p+1, iconX+p-1, iconY+p-1);
                    g2.drawLine(iconX+p-1, iconY-p+1, iconX-p+1, iconY+p-1);
                } else {
                    // Icon tick
                    g2.drawLine(iconX-5, iconY, iconX-1, iconY+4);
                    g2.drawLine(iconX-1, iconY+4, iconX+6, iconY-5);
                }
                // Text
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                // Lấy text hiển thị: bỏ phần icon prefix nếu có
                String txt = lbl.replaceAll("^[^a-zA-Z\\u00C0-\\u024F\\u1E00-\\u1EFF]+", "").trim();
                int tx = iconX + 10;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(txt, tx, ty);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(148,38); }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false); btn.setText("");
        return btn;
    }
    private JLabel makeDialogLabel(String text) {
        JLabel lbl = new JLabel(text); lbl.setFont(new Font("Segoe UI",Font.PLAIN,12)); return lbl;
    }

    // =========================================================================
    // THANH TOÁN
    // =========================================================================
    private void doCheckout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,"Gi\u1ecf h\u00e0ng \u0111ang tr\u1ed1ng!","Th\u00f4ng b\u00e1o",JOptionPane.WARNING_MESSAGE); return;
        }
        String tenKH = txtKhachHang.getText().trim();
        if (tenKH.isEmpty()) tenKH = "Kh\u00e1ch l\u1ebb";
        double giam     = tongTien * phanTramGiamKH / 100.0;
        double thanhToan = Math.max(0, (tongTien - giam) * 1.10);
        if (rbTienMat.isSelected()) {
            double tienNhan=0;
            try{tienNhan=Double.parseDouble(txtTienNhan.getText().replace(",",""));}catch(Exception ignored){}
            if (tienNhan==0) { JOptionPane.showMessageDialog(this,"Vui l\u00f2ng nh\u1eadp ti\u1ec1n nh\u1eadn!","Thi\u1ebfu th\u00f4ng tin",JOptionPane.WARNING_MESSAGE); txtTienNhan.requestFocus(); return; }
            if (tienNhan<thanhToan) {
                JOptionPane.showMessageDialog(this,String.format("Ti\u1ec1n kh\u00e1ch \u0111\u01b0a (%s \u0111) kh\u00f4ng \u0111\u1ee7!\nC\u1ea7n t\u1ed1i thi\u1ec3u: %s \u0111",formatMoney(tienNhan),formatMoney(thanhToan)),"Ti\u1ec1n kh\u00f4ng \u0111\u1ee7",JOptionPane.ERROR_MESSAGE);
                txtTienNhan.requestFocus(); txtTienNhan.selectAll(); return;
            }
        }
        String pt = rbTienMat.isSelected() ? "TienMat" : "ChuyenKhoan";
        String ptHienThi = rbTienMat.isSelected() ? "Ti\u1ec1n m\u1eb7t" : "Chuy\u1ec3n kho\u1ea3n";
        String giamHienThi = phanTramGiamKH>0
                ? String.format("%.0f%% - H\u1ea1ng %s",phanTramGiamKH,KhachHangBUS.hangDisplayName(khachHangHienTai!=null?khachHangHienTai.getHangKhachHang():null))
                : "Kh\u00f4ng c\u00f3";
        int confirm = JOptionPane.showConfirmDialog(this,
                "X\u00e1c nh\u1eadn thanh to\u00e1n?\nKh\u00e1ch h\u00e0ng: "+tenKH+"\nGi\u1ea3m gi\u00e1: "+giamHienThi+"\nTh\u00e0nh ti\u1ec1n: "+formatMoney(thanhToan)+" \u0111\nPh\u01b0\u01a1ng th\u1ee9c: "+ptHienThi,
                "X\u00e1c nh\u1eadn", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            String sdt = txtSDT.getText().trim();
            Integer maKH = null;
            if (khachHangHienTai!=null) { maKH = khachHangHienTai.getMaKhachHang(); }
            else if (!sdt.isEmpty()) { maKH = hoaDonBUS.timHoacTaoKhachHang(tenKH, sdt); }
            int maHD;
            if (maHoaDonChoDangMo > 0) {
                // Đang sửa đơn chờ → dùng lại HD cũ, xóa chi tiết cũ rồi thêm lại
                maHD = maHoaDonChoDangMo;
                hoaDonBUS.xoaChiTietHoaDon(maHD);
            } else {
                // Đơn mới → tạo HD mới
                maHD = hoaDonBUS.taoHoaDon(maKH, maNVHienTai, BigDecimal.ZERO);
                if (maHD==-1) throw new Exception("Kh\u00f4ng t\u1ea1o \u0111\u01b0\u1ee3c h\u00f3a \u0111\u01a1n!");
            }
            for (Object[] item : cartItems) {
                int    maSP    = Integer.parseInt(item[0].toString().trim());
                double donGia  = Double.parseDouble(item[2].toString().trim());
                int    soLuong = Integer.parseInt(item[3].toString().trim());
                if (soLuong<1) soLuong=1;
                hoaDonBUS.themSPVaoHoaDon(maHD, maSP, BigDecimal.valueOf(donGia), soLuong);
            }
            if (giam>0) hoaDonBUS.capNhatPhanTramGiam(maHD, BigDecimal.valueOf(phanTramGiamKH));
            hoaDonBUS.thanhToanHoaDon(maHD, BigDecimal.valueOf(giam), BigDecimal.valueOf(thanhToan), pt);

            // ── CẤP PHIẾU BẢO HÀNH TỰ ĐỘNG ──────────────────────────────────
            // Tạo bảo hành cho từng SP có ThoiHanBaoHanhThang > 0
            try {
                hoaDonBUS.capPhieuBaoHanhSauBan(maHD, maNVHienTai);
            } catch (Exception eBH) {
                System.err.println("[BaoHanh] Lỗi khi cấp phiếu BH cho HĐ#" + maHD + ": " + eBH.getMessage());
            }
            // ─────────────────────────────────────────────────────────────────

            // ── TÍCH ĐIỂM CHO KHÁCH HÀNG THÀNH VIÊN ──────────────────────────
            // Quy tắc: cứ 1.000.000đ (tiền hàng sau giảm, trước VAT) = 1 điểm
            int diemCong = 0;
            KhachHangDTO khSauMua = null;
            if (khachHangHienTai != null) {
                double soTienTinhDiem = tongTien - giam; // tiền trước VAT, sau giảm giá
                diemCong = (int)(soTienTinhDiem / 1_000_000);
                if (diemCong > 0) {
                    khSauMua = khachHangHienTai;
                    khachHangBUS.congDiem(khSauMua, diemCong);
                }
            }
            // ─────────────────────────────────────────────────────────────────

            String thongBaoDiem = "";
            if (diemCong > 0 && khSauMua != null) {
                thongBaoDiem = "\n\u0110i\u1ec3m t\u00edch l\u0169y: +" + diemCong + " \u0111i\u1ec3m"
                        + " (T\u1ed5ng: " + khSauMua.getDiemTichLuy() + " \u0111i\u1ec3m)"
                        + "\nH\u1ea1ng hi\u1ec7n t\u1ea1i: " + KhachHangBUS.hangDisplayName(khSauMua.getHangKhachHang());
            }

            // ── CUSTOM DIALOG THANH TOÁN THÀNH CÔNG ──────────────────────────
            final int finalMaHD = maHD;
            final String finalTenKH = tenKH;
            final double finalThanhToan = thanhToan;
            final String finalGiamHienThi = giamHienThi;
            final String finalPtHienThi = ptHienThi;
            final String finalThongBaoDiem = thongBaoDiem;

            Window owner = SwingUtilities.getWindowAncestor(this);
            JDialog successDlg = (owner instanceof Frame)
                    ? new JDialog((Frame) owner, "Thanh to\u00e1n th\u00e0nh c\u00f4ng", true)
                    : new JDialog((Dialog) owner, "Thanh to\u00e1n th\u00e0nh c\u00f4ng", true);
            successDlg.setSize(440, 320);
            successDlg.setLocationRelativeTo(this);
            successDlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            successDlg.setResizable(false);

            JPanel dlgRoot = new JPanel(new BorderLayout(0, 0));
            dlgRoot.setBackground(Color.WHITE);

            // ── Header xanh lá ──
            JPanel dlgHeader = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(46, 125, 50));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    // Icon tick tròn
                    int cx = 36, cy = getHeight() / 2, r = 14;
                    g2.setColor(Color.WHITE);
                    g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                    g2.setColor(new Color(46, 125, 50));
                    g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx - 6, cy, cx - 1, cy + 6);
                    g2.drawLine(cx - 1, cy + 6, cx + 8, cy - 7);
                    g2.dispose();
                }
            };
            dlgHeader.setOpaque(false);
            dlgHeader.setPreferredSize(new Dimension(0, 58));
            dlgHeader.setBorder(BorderFactory.createEmptyBorder(0, 58, 0, 16));
            JLabel lblSuccessTitle = new JLabel("Thanh to\u00e1n th\u00e0nh c\u00f4ng!");
            lblSuccessTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblSuccessTitle.setForeground(Color.WHITE);
            JLabel lblMaHD2 = new JLabel(String.format("HD%03d", finalMaHD));
            lblMaHD2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblMaHD2.setForeground(new Color(200, 240, 200));
            JPanel headerText = new JPanel(new GridBagLayout());
            headerText.setOpaque(false);
            GridBagConstraints hgc = new GridBagConstraints();
            hgc.gridx = 0; hgc.gridy = 0; hgc.anchor = GridBagConstraints.WEST;
            headerText.add(lblSuccessTitle, hgc);
            hgc.gridy = 1;
            headerText.add(lblMaHD2, hgc);
            dlgHeader.add(headerText, BorderLayout.CENTER);
            dlgRoot.add(dlgHeader, BorderLayout.NORTH);

            // ── Nội dung thông tin ──
            JPanel dlgBody = new JPanel(new GridBagLayout());
            dlgBody.setBackground(Color.WHITE);
            dlgBody.setBorder(BorderFactory.createEmptyBorder(14, 24, 10, 24));
            GridBagConstraints bgc = new GridBagConstraints();
            bgc.anchor = GridBagConstraints.WEST;
            bgc.insets = new Insets(3, 4, 3, 4);

            String[][] infoRows = {
                    {"Kh\u00e1ch h\u00e0ng:", finalTenKH},
                    {"Gi\u1ea3m gi\u00e1:", finalGiamHienThi},
                    {"Th\u00e0nh ti\u1ec1n:", formatMoney(finalThanhToan) + " \u0111"},
                    {"Ph\u01b0\u01a1ng th\u1ee9c:", finalPtHienThi},
            };
            Color labelColor = new Color(21, 101, 192);
            for (int ri = 0; ri < infoRows.length; ri++) {
                bgc.gridx = 0; bgc.gridy = ri; bgc.weightx = 0;
                JLabel lKey = new JLabel(infoRows[ri][0]);
                lKey.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lKey.setForeground(labelColor);
                dlgBody.add(lKey, bgc);
                bgc.gridx = 1; bgc.weightx = 1.0;
                JLabel lVal = new JLabel(infoRows[ri][1]);
                lVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lVal.setForeground(new Color(30, 40, 60));
                // Làm nổi bật tiền
                if (ri == 2) { lVal.setFont(new Font("Segoe UI", Font.BOLD, 14)); lVal.setForeground(new Color(46, 125, 50)); }
                dlgBody.add(lVal, bgc);
            }
            // Điểm tích lũy (nếu có)
            if (!finalThongBaoDiem.isEmpty()) {
                bgc.gridx = 0; bgc.gridy = infoRows.length; bgc.gridwidth = 2; bgc.weightx = 1.0;
                JLabel lblDiem = new JLabel("<html><font color='#1565C0'>" +
                        finalThongBaoDiem.trim().replace("\n", "<br>") + "</font></html>");
                lblDiem.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                lblDiem.setBorder(BorderFactory.createCompoundBorder(
                        new javax.swing.border.LineBorder(new Color(187, 222, 251), 1),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)));
                lblDiem.setBackground(new Color(245, 250, 255));
                lblDiem.setOpaque(true);
                bgc.insets = new Insets(8, 4, 4, 4); bgc.fill = GridBagConstraints.HORIZONTAL;
                dlgBody.add(lblDiem, bgc);
                bgc.gridwidth = 1; bgc.fill = GridBagConstraints.NONE;
            }
            dlgRoot.add(dlgBody, BorderLayout.CENTER);

            // ── Footer: 2 nút ──
            JPanel dlgFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            dlgFooter.setBackground(new Color(245, 248, 255));
            dlgFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 235, 250)));

            // Nút xuất PDF — xanh dương outline
            JButton btnXuatPDF = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    Color btnBg = getModel().isRollover() ? new Color(21, 101, 192) : Color.WHITE;
                    Color btnFg = getModel().isRollover() ? Color.WHITE : new Color(21, 101, 192);
                    g2.setColor(btnBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(21, 101, 192));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    // Icon trang PDF nhỏ
                    int ix = 12, iy = getHeight() / 2;
                    g2.setColor(getModel().isRollover() ? Color.WHITE : new Color(198, 40, 40));
                    g2.fillRoundRect(ix - 5, iy - 8, 11, 14, 3, 3);
                    g2.setColor(getModel().isRollover() ? new Color(21, 101, 192) : Color.WHITE);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawLine(ix - 3, iy - 3, ix + 3, iy - 3);
                    g2.drawLine(ix - 3, iy,     ix + 3, iy);
                    g2.drawLine(ix - 3, iy + 3, ix + 1, iy + 3);
                    // Text
                    Font tf = new Font("Segoe UI", Font.BOLD, 12);
                    g2.setFont(tf); g2.setColor(btnFg);
                    FontMetrics fm = g2.getFontMetrics();
                    String txt = "Xu\u1ea5t h\u00f3a \u0111\u01a1n PDF";
                    g2.drawString(txt, ix + 10, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
                @Override public Dimension getPreferredSize() { return new Dimension(168, 38); }
            };
            btnXuatPDF.setContentAreaFilled(false); btnXuatPDF.setBorderPainted(false);
            btnXuatPDF.setFocusPainted(false); btnXuatPDF.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Nút Đóng
            JButton btnDong = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? new Color(70, 80, 100) : new Color(90, 100, 115));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    Font tf = new Font("Segoe UI", Font.BOLD, 12);
                    g2.setFont(tf); g2.setColor(Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics();
                    String txt = "\u0110\u00f3ng";
                    g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
                            (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
                @Override public Dimension getPreferredSize() { return new Dimension(80, 38); }
            };
            btnDong.setContentAreaFilled(false); btnDong.setBorderPainted(false);
            btnDong.setFocusPainted(false); btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnXuatPDF.addActionListener(ev -> {
                successDlg.dispose();
                // Lấy HoaDonDTO và chiết tiết từ BUS để xuất PDF
                try {
                    HoaDonDTO hdExport = hoaDonBUS.timHoaDonTheoMa(finalMaHD);
                    if (hdExport == null) {
                        JOptionPane.showMessageDialog(BanHangPanel2.this,
                                "Kh\u00f4ng t\u00ecm th\u1ea5y h\u00f3a \u0111\u01a1n #" + finalMaHD + "!",
                                "L\u1ed7i", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    java.util.List<ChiTietHoaDonDTO> ctList =
                            hoaDonBUS.getCTHoaDon(finalMaHD);
                    HoaDonPDFUtils.exportChiTiet(BanHangPanel2.this, hdExport, ctList);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(BanHangPanel2.this,
                            "L\u1ed7i xu\u1ea5t PDF: " + ex.getMessage(),
                            "L\u1ed7i", JOptionPane.ERROR_MESSAGE);
                }
            });
            btnDong.addActionListener(ev -> successDlg.dispose());
            // ESC đóng dialog
            successDlg.getRootPane().registerKeyboardAction(
                    ev -> successDlg.dispose(),
                    KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            dlgFooter.add(btnXuatPDF);
            dlgFooter.add(btnDong);
            dlgRoot.add(dlgFooter, BorderLayout.SOUTH);

            successDlg.setContentPane(dlgRoot);
            successDlg.setVisible(true);
            // ─────────────────────────────────────────────────────────────────

            txtMaHD.setText(String.format("HD%03d",maHD+1));
            resetCart();

            // ── Notify các panel liên quan reload dữ liệu mới ──────────────
            // Dùng SwingUtilities.invokeLater để tránh block EDT
            final int finalMaHDForRefresh = maHD;
            
            // ─────────────────────────────────────────────────────────────────
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"L\u1ed7i:\n"+ex.getMessage(),"L\u1ed7i",JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // TOAST
    // =========================================================================
    private void showToast(String msg){
        if (lblToast == null) return;
        // Huỷ timer cũ nếu đang chạy
        if (toastTimer != null && toastTimer.isRunning()) toastTimer.stop();
        lblToast.setText(msg);
        lblToast.setVisible(true);
        lblToast.getParent().revalidate();
        lblToast.getParent().repaint();
        toastTimer = new javax.swing.Timer(1600, e -> {
            lblToast.setVisible(false);
            lblToast.getParent().revalidate();
        });
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private void styleCartField(JTextField f){
        f.setFont(FONT_NORMAL);
        f.setBorder(new CompoundBorder(new LineBorder(new Color(180,210,240),1),BorderFactory.createEmptyBorder(2,6,2,6)));
        f.setCursor(new Cursor(Cursor.TEXT_CURSOR));
    }
    private JLabel makeCartLabel(String t){JLabel l=new JLabel(t);l.setFont(FONT_LABEL);l.setForeground(PRIMARY);return l;}
    private void styleRadio(JRadioButton rb){
        rb.setFont(new Font("Segoe UI",Font.PLAIN,12));rb.setForeground(PRIMARY_DARK);rb.setBackground(WHITE);
        rb.setFocusPainted(false);rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    private JButton createActionButton(String text,Color bg,Color fg){
        JButton btn=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?bg.darker():bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(fg);btn.setFont(FONT_LABEL);btn.setFocusPainted(false);btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);btn.setOpaque(false);btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120,38)); return btn;
    }
    private JTextField createSmallField(String text,int w){
        JTextField f=new JTextField(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(new Color(255,255,255,55));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),4,4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(new Font("Segoe UI",Font.BOLD,12));
        f.setForeground(WHITE);
        f.setOpaque(false);
        f.setBorder(new CompoundBorder(new LineBorder(new Color(255,255,255,100),1),BorderFactory.createEmptyBorder(2,5,2,5)));
        f.setPreferredSize(new Dimension(w,24));
        f.setCaretColor(WHITE);
        return f;
    }
    private JLabel makeLabel(String t)      {JLabel l=new JLabel(t);l.setFont(FONT_LABEL);l.setForeground(PRIMARY);return l;}
    private JLabel makeInlineLabel(String t){JLabel l=new JLabel(t);l.setFont(FONT_SMALL);l.setForeground(new Color(200,230,255));return l;}
    private String formatMoney(double v)    {return NumberFormat.getNumberInstance(Locale.US).format((long)v);}

    /**
     * Đệ quy set cursor cho container và toàn bộ con cháu.
     * Bỏ qua JTextField/JTextArea (đã có TEXT_CURSOR riêng) và
     * JTable (cursor được quản lý bởi MouseMotionListener).
     */
    private void setCursorAll(java.awt.Container root, Cursor cursor) {
        for (Component c : root.getComponents()) {
            if (c instanceof JTextField || c instanceof javax.swing.JTextArea
                    || c instanceof JTable) continue;
            if (c instanceof AbstractButton) {
                c.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                c.setCursor(cursor);
            }
            if (c instanceof java.awt.Container) {
                setCursorAll((java.awt.Container) c, cursor);
            }
        }
    }
}
