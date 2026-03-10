package GUI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import BUS.ThongKeBUS;
import DTO.ThongKe.ThongKeDoanhThuDTO;
import DTO.ThongKe.ThongKeHoaDonBanDTO;
import DTO.ThongKe.ThongKeSanPhamBanDTO;
import DTO.ThongKe.ThongKeTheLoaiBanDTO;

public class ThongKeGUI extends JPanel {

    // ─── Theme (sync Main / BanHang) ──────────────────────────────────────
    private static final Color PRIMARY = new Color(21, 101, 192);
    private static final Color PRIMARY_DARK = new Color(10, 60, 130);
    private static final Color ACCENT = new Color(0, 188, 212);
    private static final Color CONTENT_BG = new Color(236, 242, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TABLE_HEADER = new Color(21, 101, 192);
    private static final Color TABLE_STRIPE = new Color(248, 252, 255);
    private static final Color SUCCESS = new Color(67, 160, 71);
    private static final Color DANGER = new Color(198, 40, 40);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TAB = new Font("Segoe UI", Font.BOLD, 13);

    // ─── BUS ────────────────────────────────────────────────────────────────
    private final ThongKeBUS tkBUS = new ThongKeBUS();

    // ─── Tab: Doanh thu theo tháng ──────────────────────────────────────────
    private JTable tableTheoNam;
    private DefaultTableModel modelTheoNam;
    private RevenueBarChartPanel chartPanelTheoNam;

    private JComboBox<String> cbNam;
    private JTable tableTheoThang;
    private DefaultTableModel modelTheoThang;
    private RevenueBarChartPanel chartPanelTheoThang;

    private JComboBox<String> cbNamNgayTrongThang;
    private JComboBox<String> cbThangNgayTrongThang;
    private JTable tableTheoNgayTrongThang;
    private DefaultTableModel modelTheoNgayTrongThang;
    private RevenueBarChartPanel chartPanelTheoNgayTrongThang;

    // ─── Tab: Doanh thu từ ngày đến ngày ────────────────────────────────────
    private JComboBox<String> DoanhThuCbThoiGian;
    private JSpinner DoanhThuSpinnerBatDau;
    private JSpinner DoanhThuSpinnerKetThuc;
    private JTable TableTuNgayDenNgay;
    private DefaultTableModel modelTuNgayDenNgay;

    // ─── Tab: Sản phẩm bán chạy ─────────────────────────────────────────────
    private JComboBox<String> CbThoiGianSanPham;
    private JSpinner spSpinnerBatDau;
    private JSpinner spSpinnerKetThuc;
    private JComboBox<String> cbTopSanPham;
    private JTable TableSanPham;
    private DefaultTableModel modelSanPham;

    // ─── Tab: Thể loại bán chạy ─────────────────────────────────────────────
    private JComboBox<String> CbThoiGianTheLoai;
    private JSpinner tlSpinnerBatDau;
    private JSpinner tlSpinnerKetThuc;
    private JComboBox<String> cbTopTheLoai;
    private JTable TableTheLoai;
    private DefaultTableModel modelTheLoai;

    // ─── Tab: Hóa đơn theo thời gian ────────────────────────────────────────
    private JComboBox<String> CbThoiGian;
    private JSpinner tgSpinnerBatDau;
    private JSpinner tgSpinnerKetThuc;
    private JTable TableThoiGian;
    private DefaultTableModel modelThoiGian;

    // ─── Tab: Doanh số theo nhân viên ───────────────────────────────────────
    private JComboBox<String> cbThoiGianNhanVien;
    private JComboBox<String> cbMaNhanVien;
    private JSpinner nvSpinnerBatDau;
    private JSpinner nvSpinnerKetThuc;
    private JTable tableNhanVien;
    private DefaultTableModel modelNhanVien;
    private boolean updatingNhanVienCombo = false;

    // ─── Spinner date format ─────────────────────────────────────────────────
    private static final String DATE_FORMAT = "dd/MM/yyyy";

    // ════════════════════════════════════════════════════════════════════════
    public ThongKeGUI() {
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildHeader(), BorderLayout.NORTH);

        // Tab chính
        JTabbedPane mainTabs = new JTabbedPane();
        styleTabbedPane(mainTabs);
        mainTabs.addTab("Doanh thu",        buildDoanhThuPanel());
        mainTabs.addTab("Bán hàng",         buildBanHangPanel());
        mainTabs.addTab("Hóa đơn theo TG",  buildHoaDonPanel());
        mainTabs.addTab("Nhân viên",        buildNhanVienPanel());
        mainTabs.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        add(mainTabs, BorderLayout.CENTER);

        // Load dữ liệu mặc định
        loadTheoNam();
        loadTheoThang();
        loadTheoNgayTrongThang();
        loadTuNgayDenNgay();
        loadSanPham();
        loadTheLoai();
        loadHoaDon();
        loadNhanVien();
    }

    private JPanel buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 18;

                // Shadow nhẹ dưới header
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 3, arc, arc);

                // Nền gradient bo góc
                g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

                // Viền mảnh
                g2.setColor(new Color(255, 255, 255, 35));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JComponent statsIcon = new JComponent() {
            { setPreferredSize(new Dimension(32, 32)); setOpaque(false); }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int baseY = getHeight() - 7;
                g2.drawLine(3, baseY, getWidth() - 3, baseY);

                g2.fillRoundRect(7, baseY - 9, 4, 9, 2, 2);
                g2.fillRoundRect(14, baseY - 14, 4, 14, 2, 2);
                g2.fillRoundRect(21, baseY - 18, 4, 18, 2, 2);

                g2.drawLine(9, baseY - 11, 16, baseY - 16);
                g2.drawLine(16, baseY - 16, 23, baseY - 20);
                g2.dispose();
            }
        };

        JLabel title = new JLabel("QUẢN LÝ THỐNG KÊ");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);
        GridBagConstraints lgc = new GridBagConstraints();
        lgc.gridx = 0;
        lgc.insets = new Insets(0, 0, 0, 6);
        left.add(statsIcon, lgc);
        lgc.gridx = 1;
        lgc.insets = new Insets(0, 0, 0, 0);
        left.add(title, lgc);

        header.add(left, BorderLayout.WEST);

        wrap.add(header, BorderLayout.CENTER);
        return wrap;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL: DOANH THU
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildDoanhThuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);

        JTabbedPane subTabs = new JTabbedPane();
        styleTabbedPane(subTabs);
        subTabs.addTab("Theo năm",            buildTheoNamPanel());
        subTabs.addTab("Theo tháng trong năm", buildTheoThangPanel());
        subTabs.addTab("Từng ngày trong tháng", buildTheoNgayTrongThangPanel());
        subTabs.addTab("Từ ngày đến ngày",      buildTuNgayDenNgayPanel());
        panel.add(subTabs, BorderLayout.CENTER);
        return panel;
    }

    // ── Sub-tab: Doanh thu theo năm ─────────────────────────────────────────
    private JPanel buildTheoNamPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        styleToolbar(toolbar);
        toolbar.add(new JLabel("Doanh thu theo năm"));

        JButton btnExcel = makeButton("Xuất Excel");
        JButton btnPdf = makeButton("In PDF");
        btnExcel.addActionListener(e -> exportExcel(tableTheoNam, "Doanh thu theo năm"));
        btnPdf.addActionListener(e -> exportPdf(tableTheoNam, "Doanh thu theo năm"));
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnExcel);
        toolbar.add(btnPdf);

        modelTheoNam = new DefaultTableModel(
                new String[]{"Năm", "Vốn", "Doanh thu", "Lợi nhuận"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableTheoNam = new JTable(modelTheoNam);
        styleTable(tableTheoNam);

        chartPanelTheoNam = new RevenueBarChartPanel();

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(createTableScrollPane(tableTheoNam), BorderLayout.CENTER);
        panel.add(chartPanelTheoNam, BorderLayout.SOUTH);
        return panel;
    }

    // ── Sub-tab: Doanh thu theo tháng ───────────────────────────────────────
    private JPanel buildTheoThangPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        styleToolbar(toolbar);
        toolbar.add(new JLabel("Chọn năm:"));
        cbNam = new JComboBox<>(buildYearItems());
        styleComboBox(cbNam);
        cbNam.addActionListener(e -> loadTheoThang());
        toolbar.add(cbNam);

        JButton btnExcel = makeButton("Xuất Excel");
        JButton btnPdf   = makeButton("In PDF");
        btnExcel.addActionListener(e -> exportExcel(tableTheoThang, "Doanh thu theo tháng"));
        btnPdf.addActionListener(e   -> exportPdf(tableTheoThang,   "Doanh thu theo tháng"));
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnExcel);
        toolbar.add(btnPdf);

        // Bảng
        modelTheoThang = new DefaultTableModel(
                new String[]{"Tháng", "Vốn", "Doanh thu", "Lợi nhuận"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableTheoThang = new JTable(modelTheoThang);
        styleTable(tableTheoThang);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(createTableScrollPane(tableTheoThang), BorderLayout.CENTER);
        // Thêm biểu đồ doanh thu theo tháng
        ArrayList<ThongKeDoanhThuDTO> data = tkBUS.thongKeDoanhThuTheoThang(getSelectedYear());
        chartPanelTheoThang = new RevenueBarChartPanel();
        chartPanelTheoThang.setData(data);
        panel.add(chartPanelTheoThang, BorderLayout.SOUTH);
        return panel;
    }

    private int getSelectedYear() {
        if (cbNam != null && cbNam.getSelectedItem() != null) {
            return Integer.parseInt(cbNam.getSelectedItem().toString());
        }
        return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
    }

    // ── Sub-tab: Doanh thu từng ngày trong tháng ────────────────────────────
    private JPanel buildTheoNgayTrongThangPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        styleToolbar(toolbar);

        toolbar.add(new JLabel("Năm:"));
        cbNamNgayTrongThang = new JComboBox<>(buildYearItems());
        styleComboBox(cbNamNgayTrongThang);
        cbNamNgayTrongThang.addActionListener(e -> loadTheoNgayTrongThang());
        toolbar.add(cbNamNgayTrongThang);

        toolbar.add(new JLabel("Tháng:"));
        cbThangNgayTrongThang = new JComboBox<>(buildMonthItems());
        styleComboBox(cbThangNgayTrongThang);
        cbThangNgayTrongThang.addActionListener(e -> loadTheoNgayTrongThang());
        toolbar.add(cbThangNgayTrongThang);

        JButton btnExcel = makeButton("Xuất Excel");
        JButton btnPdf = makeButton("In PDF");
        btnExcel.addActionListener(e -> exportExcel(tableTheoNgayTrongThang, "Doanh thu từng ngày trong tháng"));
        btnPdf.addActionListener(e -> exportPdf(tableTheoNgayTrongThang, "Doanh thu từng ngày trong tháng"));
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnExcel);
        toolbar.add(btnPdf);

        modelTheoNgayTrongThang = new DefaultTableModel(
                new String[]{"Ngày", "Vốn", "Doanh thu", "Lợi nhuận"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableTheoNgayTrongThang = new JTable(modelTheoNgayTrongThang);
        styleTable(tableTheoNgayTrongThang);

        chartPanelTheoNgayTrongThang = new RevenueBarChartPanel();

        // Mặc định là tháng hiện tại
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        cbThangNgayTrongThang.setSelectedItem(String.format("%02d", currentMonth));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(createTableScrollPane(tableTheoNgayTrongThang), BorderLayout.CENTER);
        panel.add(chartPanelTheoNgayTrongThang, BorderLayout.SOUTH);
        return panel;
    }

    private static class RevenueBarChartPanel extends JPanel {
        private final Color colorVon = new Color(0, 188, 212);
        private final Color colorDoanhThu = new Color(21, 101, 192);
        private final Color colorLoiNhuan = new Color(111, 66, 193);
        private java.util.List<ThongKeDoanhThuDTO> data = new ArrayList<>();

        RevenueBarChartPanel() {
            setBackground(CARD_BG);
            setPreferredSize(new Dimension(900, 320));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 225, 245)),
                    BorderFactory.createEmptyBorder(6, 6, 4, 6)));
        }

        void setData(java.util.List<ThongKeDoanhThuDTO> data) {
            this.data = (data == null) ? new ArrayList<>() : data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int left = 58, right = 18, top = 18, bottom = 48;
            int chartW = Math.max(100, w - left - right);
            int chartH = Math.max(80, h - top - bottom);

            long max = 1;
            for (ThongKeDoanhThuDTO d : data) {
                max = Math.max(max, Math.max(d.getTongVon(), Math.max(d.getTongDoanhThu(), d.getLoiNhuan())));
            }

            // Grid + trục Y
            g2.setColor(new Color(205, 205, 205));
            for (int i = 0; i <= 10; i++) {
                int y = top + i * chartH / 10;
                g2.draw(new Line2D.Double(left, y, left + chartW, y));
                long val = (long) ((10 - i) * (double) max / 10);
                g2.setColor(new Color(120, 120, 120));
                g2.drawString(String.format("%,d", val), 10, y + 5);
                g2.setColor(new Color(205, 205, 205));
            }

            g2.setColor(new Color(130, 130, 130));
            g2.drawLine(left, top + chartH, left + chartW, top + chartH);

            if (!data.isEmpty()) {
                int groups = data.size();
                int groupW = Math.max(16, chartW / groups);
                int barW = Math.max(4, Math.min(14, groupW / 5));

                for (int i = 0; i < groups; i++) {
                    ThongKeDoanhThuDTO d = data.get(i);
                    int gx = left + i * groupW + (groupW - 3 * barW) / 2;

                    int hVon = (int) (chartH * (double) d.getTongVon() / max);
                    int hDoanhThu = (int) (chartH * (double) d.getTongDoanhThu() / max);
                    int hLoiNhuan = (int) (chartH * (double) d.getLoiNhuan() / max);

                    g2.setColor(colorVon);
                    g2.fillRect(gx, top + chartH - hVon, barW, hVon);

                    g2.setColor(colorDoanhThu);
                    g2.fillRect(gx + barW, top + chartH - hDoanhThu, barW, hDoanhThu);

                    g2.setColor(colorLoiNhuan);
                    g2.fillRect(gx + 2 * barW, top + chartH - hLoiNhuan, barW, hLoiNhuan);

                    int stride = (groups <= 31) ? 1 : Math.max(1, (int) Math.ceil(groups / 10.0));
                    if (i % stride == 0 || i == groups - 1) {
                        g2.setColor(new Color(110, 110, 110));
                        String label = formatXAxisLabel(d.getThoiGian(), groups);
                        int textX = gx - 2;
                        if (groups > 12) {
                            textX = gx;
                        }
                        g2.drawString(label, textX, top + chartH + 16);
                    }
                }
            }

            // Legend
            int ly = h - 16;
            drawLegend(g2, left + chartW / 2 - 120, ly, colorVon, "Vốn");
            drawLegend(g2, left + chartW / 2 - 30, ly, colorDoanhThu, "Doanh thu");
            drawLegend(g2, left + chartW / 2 + 80, ly, colorLoiNhuan, "Lợi nhuận");

            g2.dispose();
        }

        private void drawLegend(Graphics2D g2, int x, int y, Color c, String text) {
            g2.setColor(c);
            g2.fillOval(x, y - 8, 8, 8);
            g2.setColor(new Color(100, 100, 100));
            g2.drawString(text, x + 12, y);
        }

        // Nếu dữ liệu dày (theo ngày trong tháng), chỉ giữ số ngày để trục X gọn và dễ đọc.
        private String formatXAxisLabel(String thoiGian, int groups) {
            if (thoiGian == null) return "";
            if (groups > 12 && thoiGian.contains("/")) {
                String[] parts = thoiGian.split("/");
                if (parts.length >= 1) {
                    return parts[0];
                }
            }
            if (groups <= 12 && thoiGian.length() > 8) {
                return thoiGian.substring(0, 8);
            }
            return thoiGian;
        }
    }

    // ── Sub-tab: Từ ngày đến ngày ────────────────────────────────────────────
    private JPanel buildTuNgayDenNgayPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        styleToolbar(toolbar);
        toolbar.add(new JLabel("Khoảng thời gian:"));
        DoanhThuCbThoiGian = buildThoiGianComboBox();
        styleComboBox(DoanhThuCbThoiGian);
        toolbar.add(DoanhThuCbThoiGian);

        DoanhThuSpinnerBatDau  = buildDateSpinner();
        DoanhThuSpinnerKetThuc = buildDateSpinner();
        toolbar.add(new JLabel("Từ:"));
        toolbar.add(DoanhThuSpinnerBatDau);
        toolbar.add(new JLabel("Đến:"));
        toolbar.add(DoanhThuSpinnerKetThuc);

        JButton btnExcel = makeButton("Xuất Excel");
        JButton btnPdf   = makeButton("In PDF");
        btnExcel.addActionListener(e -> exportExcel(TableTuNgayDenNgay, "Doanh thu từ ngày đến ngày"));
        btnPdf.addActionListener(e   -> exportPdf(TableTuNgayDenNgay,   "Doanh thu từ ngày đến ngày"));
        toolbar.add(btnExcel);
        toolbar.add(btnPdf);

        // Sự kiện
        DoanhThuCbThoiGian.addActionListener(e -> {
            applyDateRange(DoanhThuSpinnerBatDau, DoanhThuSpinnerKetThuc, DoanhThuCbThoiGian);
            loadTuNgayDenNgay();
        });
        addSpinnerChangeListener(DoanhThuSpinnerBatDau,  this::loadTuNgayDenNgay);
        addSpinnerChangeListener(DoanhThuSpinnerKetThuc, this::loadTuNgayDenNgay);

        modelTuNgayDenNgay = new DefaultTableModel(
                new String[]{"Ngày", "Vốn", "Doanh thu", "Lợi nhuận"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        TableTuNgayDenNgay = new JTable(modelTuNgayDenNgay);
        styleTable(TableTuNgayDenNgay);

        // Đặt mặc định "7 ngày qua"
        DoanhThuCbThoiGian.setSelectedItem("7 ngày qua");

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(createTableScrollPane(TableTuNgayDenNgay), BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL: BÁN HÀNG
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildBanHangPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        JTabbedPane subTabs = new JTabbedPane();
        styleTabbedPane(subTabs);
        subTabs.addTab("Sản phẩm",  buildSanPhamPanel());
        subTabs.addTab("Thể loại",  buildTheLoaiPanel());
        panel.add(subTabs, BorderLayout.CENTER);
        return panel;
    }

    // ── Sub-tab: Sản phẩm bán chạy ─────────────────────────────────────────
    private JPanel buildSanPhamPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        styleToolbar(toolbar);
        toolbar.add(new JLabel("Khoảng thời gian:"));
        CbThoiGianSanPham = buildThoiGianComboBox();
        styleComboBox(CbThoiGianSanPham);
        toolbar.add(CbThoiGianSanPham);

        spSpinnerBatDau  = buildDateSpinner();
        spSpinnerKetThuc = buildDateSpinner();
        toolbar.add(new JLabel("Từ:"));
        toolbar.add(spSpinnerBatDau);
        toolbar.add(new JLabel("Đến:"));
        toolbar.add(spSpinnerKetThuc);

        toolbar.add(new JLabel("Hiển thị:"));
        cbTopSanPham = new JComboBox<>(new String[]{"Tất cả", "Top 3", "Top 5", "Top 10", "Top 20"});
        styleComboBox(cbTopSanPham);
        toolbar.add(cbTopSanPham);

        JButton btnExcel = makeButton("Xuất Excel");
        JButton btnPdf   = makeButton("In PDF");
        btnExcel.addActionListener(e -> exportExcel(TableSanPham, "Sản phẩm bán chạy"));
        btnPdf.addActionListener(e   -> exportPdf(TableSanPham,   "Sản phẩm bán chạy"));
        toolbar.add(btnExcel);
        toolbar.add(btnPdf);

        // Sự kiện
        CbThoiGianSanPham.addActionListener(e -> {
            applyDateRange(spSpinnerBatDau, spSpinnerKetThuc, CbThoiGianSanPham);
            loadSanPham();
        });
        addSpinnerChangeListener(spSpinnerBatDau,  this::loadSanPham);
        addSpinnerChangeListener(spSpinnerKetThuc, this::loadSanPham);
        cbTopSanPham.addActionListener(e -> loadSanPham());

        modelSanPham = new DefaultTableModel(
                new String[]{"STT", "Mã SP", "Tên SP", "SL bán", "SL đơn", "Doanh thu"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        TableSanPham = new JTable(modelSanPham);
        styleTable(TableSanPham);

        // Đặt mặc định "7 ngày qua"
        CbThoiGianSanPham.setSelectedItem("7 ngày qua");

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(createTableScrollPane(TableSanPham), BorderLayout.CENTER);
        return panel;
    }

    // ── Sub-tab: Thể loại bán chạy ─────────────────────────────────────────
    private JPanel buildTheLoaiPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        styleToolbar(toolbar);
        toolbar.add(new JLabel("Khoảng thời gian:"));
        CbThoiGianTheLoai = buildThoiGianComboBox();
        styleComboBox(CbThoiGianTheLoai);
        toolbar.add(CbThoiGianTheLoai);

        tlSpinnerBatDau  = buildDateSpinner();
        tlSpinnerKetThuc = buildDateSpinner();
        toolbar.add(new JLabel("Từ:"));
        toolbar.add(tlSpinnerBatDau);
        toolbar.add(new JLabel("Đến:"));
        toolbar.add(tlSpinnerKetThuc);

        toolbar.add(new JLabel("Hiển thị:"));
        cbTopTheLoai = new JComboBox<>(new String[]{"Tất cả", "Top 3", "Top 5", "Top 10", "Top 20"});
        styleComboBox(cbTopTheLoai);
        toolbar.add(cbTopTheLoai);

        JButton btnExcel = makeButton("Xuất Excel");
        JButton btnPdf   = makeButton("In PDF");
        btnExcel.addActionListener(e -> exportExcel(TableTheLoai, "Thể loại bán chạy"));
        btnPdf.addActionListener(e   -> exportPdf(TableTheLoai,   "Thể loại bán chạy"));
        toolbar.add(btnExcel);
        toolbar.add(btnPdf);

        // Sự kiện
        CbThoiGianTheLoai.addActionListener(e -> {
            applyDateRange(tlSpinnerBatDau, tlSpinnerKetThuc, CbThoiGianTheLoai);
            loadTheLoai();
        });
        addSpinnerChangeListener(tlSpinnerBatDau,  this::loadTheLoai);
        addSpinnerChangeListener(tlSpinnerKetThuc, this::loadTheLoai);
        cbTopTheLoai.addActionListener(e -> loadTheLoai());

        modelTheLoai = new DefaultTableModel(
                new String[]{"STT", "Mã TL", "Tên TL", "SL bán", "Số loại SP", "SL đơn", "Doanh thu"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        TableTheLoai = new JTable(modelTheLoai);
        styleTable(TableTheLoai);

        // Đặt mặc định "7 ngày qua"
        CbThoiGianTheLoai.setSelectedItem("7 ngày qua");

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(createTableScrollPane(TableTheLoai), BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL: HÓA ĐƠN THEO THỜI GIAN
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildHoaDonPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        styleToolbar(toolbar);
        toolbar.add(new JLabel("Khoảng thời gian:"));
        CbThoiGian = buildThoiGianComboBox();
        styleComboBox(CbThoiGian);
        toolbar.add(CbThoiGian);

        tgSpinnerBatDau  = buildDateSpinner();
        tgSpinnerKetThuc = buildDateSpinner();
        toolbar.add(new JLabel("Từ:"));
        toolbar.add(tgSpinnerBatDau);
        toolbar.add(new JLabel("Đến:"));
        toolbar.add(tgSpinnerKetThuc);

        JButton btnExcel = makeButton("Xuất Excel");
        JButton btnPdf   = makeButton("In PDF");
        btnExcel.addActionListener(e -> exportExcel(TableThoiGian, "Hóa đơn theo thời gian"));
        btnPdf.addActionListener(e   -> exportPdf(TableThoiGian,   "Hóa đơn theo thời gian"));
        toolbar.add(btnExcel);
        toolbar.add(btnPdf);

        // Sự kiện
        CbThoiGian.addActionListener(e -> {
            applyDateRange(tgSpinnerBatDau, tgSpinnerKetThuc, CbThoiGian);
            loadHoaDon();
        });
        addSpinnerChangeListener(tgSpinnerBatDau,  this::loadHoaDon);
        addSpinnerChangeListener(tgSpinnerKetThuc, this::loadHoaDon);

        modelThoiGian = new DefaultTableModel(
                new String[]{"STT", "Thời gian", "Số lượng đơn", "Số lượng SP", "Số loại SP", "Doanh thu"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        TableThoiGian = new JTable(modelThoiGian);
        styleTable(TableThoiGian);

        // Đặt mặc định "7 ngày qua"
        CbThoiGian.setSelectedItem("7 ngày qua");

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(createTableScrollPane(TableThoiGian), BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL: DOANH SỐ THEO NHÂN VIÊN
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildNhanVienPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        styleToolbar(toolbar);
        toolbar.add(new JLabel("Khoảng thời gian:"));

        cbThoiGianNhanVien = buildThoiGianComboBox();
        styleComboBox(cbThoiGianNhanVien);
        toolbar.add(cbThoiGianNhanVien);

        toolbar.add(new JLabel("Mã NV:"));
        cbMaNhanVien = new JComboBox<>(new String[]{"Tất cả"});
        styleComboBox(cbMaNhanVien);
        toolbar.add(cbMaNhanVien);

        nvSpinnerBatDau = buildDateSpinner();
        nvSpinnerKetThuc = buildDateSpinner();
        toolbar.add(new JLabel("Từ:"));
        toolbar.add(nvSpinnerBatDau);
        toolbar.add(new JLabel("Đến:"));
        toolbar.add(nvSpinnerKetThuc);

        JButton btnExcel = makeButton("Xuất Excel");
        JButton btnPdf = makeButton("In PDF");
        btnExcel.addActionListener(e -> exportExcel(tableNhanVien, "Doanh số theo nhân viên"));
        btnPdf.addActionListener(e -> exportPdf(tableNhanVien, "Doanh số theo nhân viên"));
        toolbar.add(btnExcel);
        toolbar.add(btnPdf);

        cbThoiGianNhanVien.addActionListener(e -> {
            applyDateRange(nvSpinnerBatDau, nvSpinnerKetThuc, cbThoiGianNhanVien);
            loadNhanVien();
        });
        cbMaNhanVien.addActionListener(e -> {
            if (!updatingNhanVienCombo) {
                loadNhanVien();
            }
        });
        addSpinnerChangeListener(nvSpinnerBatDau, this::loadNhanVien);
        addSpinnerChangeListener(nvSpinnerKetThuc, this::loadNhanVien);

        modelNhanVien = new DefaultTableModel(
                new String[]{"STT", "Mã NV", "Tên nhân viên", "Số đơn đã xử lý", "Doanh số"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableNhanVien = new JTable(modelNhanVien);
        styleTable(tableNhanVien);

        cbThoiGianNhanVien.setSelectedItem("7 ngày qua");

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(createTableScrollPane(tableNhanVien), BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LOAD DỮ LIỆU
    // ════════════════════════════════════════════════════════════════════════

    private void loadTheoThang() {
        if (cbNam == null || cbNam.getSelectedItem() == null) return;
        int nam = Integer.parseInt(cbNam.getSelectedItem().toString());
        ArrayList<ThongKeDoanhThuDTO> result = tkBUS.thongKeDoanhThuTheoThang(nam);
        modelTheoThang.setRowCount(0);
        for (ThongKeDoanhThuDTO dto : result) {
            modelTheoThang.addRow(new Object[]{
                dto.getThoiGian(),
                formatVND(dto.getTongVon()),
                formatVND(dto.getTongDoanhThu()),
                formatVND(dto.getLoiNhuan())
            });
        }
        if (chartPanelTheoThang != null) {
            chartPanelTheoThang.setData(result);
        }
    }

    private void loadTheoNam() {
        if (modelTheoNam == null) return;
        ArrayList<ThongKeDoanhThuDTO> result = tkBUS.thongKeDoanhThuTheoNam();
        modelTheoNam.setRowCount(0);
        for (ThongKeDoanhThuDTO dto : result) {
            modelTheoNam.addRow(new Object[]{
                dto.getThoiGian(),
                formatVND(dto.getTongVon()),
                formatVND(dto.getTongDoanhThu()),
                formatVND(dto.getLoiNhuan())
            });
        }
        if (chartPanelTheoNam != null) {
            chartPanelTheoNam.setData(result);
        }
    }

    private void loadTheoNgayTrongThang() {
        if (cbNamNgayTrongThang == null || cbThangNgayTrongThang == null || modelTheoNgayTrongThang == null) return;

        int nam = Integer.parseInt(String.valueOf(cbNamNgayTrongThang.getSelectedItem()));
        int thang = Integer.parseInt(String.valueOf(cbThangNgayTrongThang.getSelectedItem()));

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, nam);
        cal.set(Calendar.MONTH, thang - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = cal.getTime();

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = cal.getTime();

        ArrayList<ThongKeDoanhThuDTO> rawResult = tkBUS.thongKeDoanhThuTuNgayDenNgay(startDate, endDate);

        // Dam bao luon co du 28/29/30/31 ngay tren bieu do cua thang duoc chon.
        Map<String, ThongKeDoanhThuDTO> byDay = new HashMap<>();
        for (ThongKeDoanhThuDTO dto : rawResult) {
            byDay.put(dto.getThoiGian(), dto);
        }

        ArrayList<ThongKeDoanhThuDTO> result = new ArrayList<>();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= maxDay; day++) {
            String key = String.format("%02d/%02d/%04d", day, thang, nam);
            ThongKeDoanhThuDTO dto = byDay.get(key);
            if (dto == null) {
                dto = new ThongKeDoanhThuDTO(key, 0, 0, 0);
            }
            result.add(dto);
        }

        modelTheoNgayTrongThang.setRowCount(0);
        for (ThongKeDoanhThuDTO dto : result) {
            modelTheoNgayTrongThang.addRow(new Object[]{
                dto.getThoiGian(),
                formatVND(dto.getTongVon()),
                formatVND(dto.getTongDoanhThu()),
                formatVND(dto.getLoiNhuan())
            });
        }
        if (chartPanelTheoNgayTrongThang != null) {
            chartPanelTheoNgayTrongThang.setData(result);
        }
    }

    private void loadTuNgayDenNgay() {
        if (DoanhThuSpinnerBatDau == null) return;
        Date startDate = getDateFromSpinner(DoanhThuSpinnerBatDau);
        Date endDate   = getDateFromSpinner(DoanhThuSpinnerKetThuc);
        if (!validateDateRange(startDate, endDate)) return;

        ArrayList<ThongKeDoanhThuDTO> result = tkBUS.thongKeDoanhThuTuNgayDenNgay(startDate, endDate);
        modelTuNgayDenNgay.setRowCount(0);
        for (ThongKeDoanhThuDTO dto : result) {
            modelTuNgayDenNgay.addRow(new Object[]{
                dto.getThoiGian(),
                formatVND(dto.getTongVon()),
                formatVND(dto.getTongDoanhThu()),
                formatVND(dto.getLoiNhuan())
            });
        }
    }

    private void loadSanPham() {
        if (spSpinnerBatDau == null) return;
        Date startDate = getDateFromSpinner(spSpinnerBatDau);
        Date endDate   = getDateFromSpinner(spSpinnerKetThuc);
        if (!validateDateRange(startDate, endDate)) return;

        ArrayList<ThongKeSanPhamBanDTO> result = tkBUS.thongKeSanPhamBanTrongKhoangThoiGian(startDate, endDate);
        int limit = getRowLimit(cbTopSanPham);
        modelSanPham.setRowCount(0);
        int stt = 1;
        for (ThongKeSanPhamBanDTO dto : result) {
            if (stt > limit) break;
            modelSanPham.addRow(new Object[]{
                stt++,
                formatID("SP", dto.getMaSP()),
                dto.getTenSP(),
                dto.getTongSoLuongBan(),
                dto.getSoHoaDon(),
                formatVND(dto.getDoanhThu())
            });
        }
    }

    private void loadTheLoai() {
        if (tlSpinnerBatDau == null) return;
        Date startDate = getDateFromSpinner(tlSpinnerBatDau);
        Date endDate   = getDateFromSpinner(tlSpinnerKetThuc);
        if (!validateDateRange(startDate, endDate)) return;

        // BUS mới: thongKeLoaiSanPhamTrongKhoangThoiGian
        ArrayList<ThongKeTheLoaiBanDTO> result = tkBUS.thongKeLoaiSanPhamTrongKhoangThoiGian(startDate, endDate);
        int limit = getRowLimit(cbTopTheLoai);
        modelTheLoai.setRowCount(0);
        int stt = 1;
        for (ThongKeTheLoaiBanDTO dto : result) {
            if (stt > limit) break;
            modelTheLoai.addRow(new Object[]{
                stt++,
                dto.getMaLoai(),
                dto.getLoaiSP(),
                dto.getTongSoLuongBan(),
                dto.getSoSanPham(),
                dto.getSoHoaDon(),
                formatVND(dto.getDoanhThu())
            });
        }
    }

    private void loadHoaDon() {
        if (tgSpinnerBatDau == null) return;
        Date startDate = getDateFromSpinner(tgSpinnerBatDau);
        Date endDate   = getDateFromSpinner(tgSpinnerKetThuc);
        if (!validateDateRange(startDate, endDate)) return;

        ArrayList<ThongKeHoaDonBanDTO> result = tkBUS.thongKeHoaDonTrongKhoangThoiGian(startDate, endDate);
        modelThoiGian.setRowCount(0);
        int stt = 1;
        for (ThongKeHoaDonBanDTO dto : result) {
            modelThoiGian.addRow(new Object[]{
                stt++,
                dto.getThoiGian(),
                dto.getSoHoaDon(),
                dto.getTongSoSanPham(),
                dto.getTongLoaiSanPham(),
                formatVND(dto.getTongDoanhThu())
            });
        }
    }

    private void loadNhanVien() {
        if (nvSpinnerBatDau == null || nvSpinnerKetThuc == null || modelNhanVien == null) return;

        Date startDate = getDateFromSpinner(nvSpinnerBatDau);
        Date endDate = getDateFromSpinner(nvSpinnerKetThuc);
        if (!validateDateRange(startDate, endDate)) return;

        ArrayList<Object[]> result = tkBUS.thongKeDoanhThuTheoNhanVien(startDate, endDate);

        // Cập nhật danh sách mã nhân viên trong combobox theo dữ liệu hiện có.
        String selectedCode = cbMaNhanVien != null && cbMaNhanVien.getSelectedItem() != null
                ? cbMaNhanVien.getSelectedItem().toString()
                : "Tất cả";
        Set<String> maNvItems = new LinkedHashSet<>();
        for (Object[] row : result) {
            int maNV = ((Number) row[0]).intValue();
            maNvItems.add(formatID("NV", maNV));
        }

        if (cbMaNhanVien != null) {
            updatingNhanVienCombo = true;
            cbMaNhanVien.removeAllItems();
            cbMaNhanVien.addItem("Tất cả");
            for (String item : maNvItems) {
                cbMaNhanVien.addItem(item);
            }
            if (maNvItems.contains(selectedCode)) {
                cbMaNhanVien.setSelectedItem(selectedCode);
            } else {
                cbMaNhanVien.setSelectedItem("Tất cả");
            }
            updatingNhanVienCombo = false;
        }

        String filterCode = cbMaNhanVien != null && cbMaNhanVien.getSelectedItem() != null
                ? cbMaNhanVien.getSelectedItem().toString()
                : "Tất cả";

        modelNhanVien.setRowCount(0);
        int stt = 1;
        for (Object[] row : result) {
            int maNV = ((Number) row[0]).intValue();
            String tenNV = String.valueOf(row[1]);
            int soDon = ((Number) row[2]).intValue();
            double doanhSo = ((Number) row[3]).doubleValue();
            String maNVFormat = formatID("NV", maNV);

            if (!"Tất cả".equals(filterCode) && !filterCode.equals(maNVFormat)) {
                continue;
            }

            modelNhanVien.addRow(new Object[]{
                stt++,
                maNVFormat,
                tenNV,
                soDon,
                formatVND(doanhSo)
            });
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS: DATE / SPINNER
    // ════════════════════════════════════════════════════════════════════════

    /** Tạo JSpinner chọn ngày dd/MM/yyyy */
    private JSpinner buildDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, DATE_FORMAT);
        spinner.setEditor(editor);
        // Keep spinner colors stable regardless of external Look&Feel overrides.
        editor.getTextField().setBackground(Color.WHITE);
        editor.getTextField().setForeground(PRIMARY_DARK);
        editor.getTextField().setCaretColor(PRIMARY_DARK);
        editor.getTextField().setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        spinner.setPreferredSize(new Dimension(110, 28));
        spinner.setFont(FONT_NORMAL);
        spinner.setBorder(BorderFactory.createLineBorder(new Color(185, 205, 232)));
        return spinner;
    }

    private Date getDateFromSpinner(JSpinner spinner) {
        return (Date) spinner.getValue();
    }

    private void addSpinnerChangeListener(JSpinner spinner, Runnable action) {
        spinner.addChangeListener(e -> action.run());
    }

    /** Tạo ComboBox khoảng thời gian nhanh */
    private JComboBox<String> buildThoiGianComboBox() {
        return new JComboBox<>(new String[]{
            "Hôm nay", "Hôm qua", "7 ngày qua", "30 ngày qua",
            "Tháng trước", "Tháng này", "Năm trước", "Năm nay", "Tùy chọn khác"
        });
    }

    /** Áp dụng khoảng thời gian nhanh vào hai spinner */
    private void applyDateRange(JSpinner from, JSpinner to, JComboBox<String> cb) {
        String sel = (String) cb.getSelectedItem();
        if (sel == null) return;
        Calendar cal = Calendar.getInstance();
        Date start;
        Date end;

        switch (sel) {
            case "Hôm nay" -> { start = cal.getTime(); end = cal.getTime(); }
            case "Hôm qua" -> {
                cal.add(Calendar.DATE, -1);
                start = cal.getTime(); end = cal.getTime();
            }
            case "7 ngày qua" -> {
                end = cal.getTime();
                cal.add(Calendar.DATE, -6);
                start = cal.getTime();
            }
            case "30 ngày qua" -> {
                end = cal.getTime();
                cal.add(Calendar.DATE, -29);
                start = cal.getTime();
            }
            case "Tháng trước" -> {
                cal.add(Calendar.MONTH, -1);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                start = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                end = cal.getTime();
            }
            case "Tháng này" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                start = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                end = cal.getTime();
            }
            case "Năm trước" -> {
                cal.add(Calendar.YEAR, -1);
                cal.set(Calendar.MONTH, 0); cal.set(Calendar.DAY_OF_MONTH, 1);
                start = cal.getTime();
                cal.set(Calendar.MONTH, 11); cal.set(Calendar.DAY_OF_MONTH, 31);
                end = cal.getTime();
            }
            case "Năm nay" -> {
                cal.set(Calendar.MONTH, 0); cal.set(Calendar.DAY_OF_MONTH, 1);
                start = cal.getTime();
                cal.set(Calendar.MONTH, 11); cal.set(Calendar.DAY_OF_MONTH, 31);
                end = cal.getTime();
            }
            default -> { /* Tùy chọn khác - không thay đổi spinner */ return; }
        }
        if (start != null) from.setValue(start);
        if (end   != null) to.setValue(end);
    }

    /** Kiểm tra ngày hợp lệ, hiển thị lỗi nếu không */
    private boolean validateDateRange(Date start, Date end) {
        if (start == null || end == null) return false;
        if (start.after(end)) {
            JOptionPane.showMessageDialog(this,
                "Ngày bắt đầu không được lớn hơn ngày kết thúc", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS: EXPORT (stub — kết nối ExcelExporter / PdfExporter của bạn)
    // ════════════════════════════════════════════════════════════════════════

    private void exportExcel(JTable table, String title) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Excel (*.xls)", "xls"));
        fc.setDialogTitle("Lưu file Excel");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".xls")) path += ".xls";
            try {
                String html = buildExcelHtml(table, title);
                Files.writeString(Path.of(path), html, StandardCharsets.UTF_8);
                JOptionPane.showMessageDialog(this, "Đã xuất Excel: " + path);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Xuất Excel thất bại: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportPdf(JTable table, String title) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        fc.setDialogTitle("Lưu file PDF");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".pdf")) path += ".pdf";
            try {
                writeSimplePdf(table, title, Path.of(path));
                JOptionPane.showMessageDialog(this, "Đã xuất PDF: " + path);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Xuất PDF thất bại: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String buildExcelHtml(JTable table, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset=\"UTF-8\"></head><body>");
        sb.append("<h3>").append(escapeHtml(title)).append("</h3>");
        sb.append("<table border='1' cellspacing='0' cellpadding='4'>");

        sb.append("<tr style='background:#1565C0;color:white;'>");
        for (int c = 0; c < table.getColumnCount(); c++) {
            sb.append("<th>").append(escapeHtml(table.getColumnName(c))).append("</th>");
        }
        sb.append("</tr>");

        for (int r = 0; r < table.getRowCount(); r++) {
            sb.append("<tr>");
            for (int c = 0; c < table.getColumnCount(); c++) {
                Object v = table.getValueAt(r, c);
                sb.append("<td>").append(escapeHtml(v == null ? "" : String.valueOf(v))).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table></body></html>");
        return sb.toString();
    }

    private void writeSimplePdf(JTable table, String title, Path outFile) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(title);
        lines.add("");

        StringBuilder header = new StringBuilder();
        for (int c = 0; c < table.getColumnCount(); c++) {
            if (c > 0) header.append(" | ");
            header.append(table.getColumnName(c));
        }
        lines.add(header.toString());

        for (int r = 0; r < table.getRowCount(); r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < table.getColumnCount(); c++) {
                if (c > 0) row.append(" | ");
                Object v = table.getValueAt(r, c);
                row.append(v == null ? "" : String.valueOf(v));
            }
            lines.add(row.toString());
        }

        StringBuilder stream = new StringBuilder();
        stream.append("BT\n/F1 10 Tf\n50 800 Td\n");
        int maxLines = 46;
        for (int i = 0; i < lines.size() && i < maxLines; i++) {
            String line = lines.get(i);
            if (line.length() > 120) line = line.substring(0, 120);
            if (i == 0) {
                stream.append("(").append(escapePdf(line)).append(") Tj\n");
            } else {
                stream.append("0 -15 Td\n(").append(escapePdf(line)).append(") Tj\n");
            }
        }
        if (lines.size() > maxLines) {
            stream.append("0 -15 Td\n(... du lieu con tiep, xem file Excel de day du ...) Tj\n");
        }
        stream.append("ET");

        byte[] streamBytes = stream.toString().getBytes(StandardCharsets.ISO_8859_1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ArrayList<Integer> offsets = new ArrayList<>();
        offsets.add(0); // object 0

        writeAscii(baos, "%PDF-1.4\n");

        offsets.add(baos.size());
        writeAscii(baos, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets.add(baos.size());
        writeAscii(baos, "2 0 obj\n<< /Type /Pages /Count 1 /Kids [3 0 R] >>\nendobj\n");

        offsets.add(baos.size());
        writeAscii(baos, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] ");
        writeAscii(baos, "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");

        offsets.add(baos.size());
        writeAscii(baos, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        offsets.add(baos.size());
        writeAscii(baos, "5 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
        baos.write(streamBytes);
        writeAscii(baos, "\nendstream\nendobj\n");

        int xrefPos = baos.size();
        writeAscii(baos, "xref\n0 6\n");
        writeAscii(baos, "0000000000 65535 f \n");
        for (int i = 1; i <= 5; i++) {
            writeAscii(baos, String.format("%010d 00000 n \n", offsets.get(i)));
        }
        writeAscii(baos, "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n");
        writeAscii(baos, String.valueOf(xrefPos));
        writeAscii(baos, "\n%%EOF");

        Files.write(outFile, baos.toByteArray());
    }

    private void writeAscii(ByteArrayOutputStream baos, String s) throws IOException {
        baos.write(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    private String escapePdf(String text) {
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS: UI
    // ════════════════════════════════════════════════════════════════════════

    private void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFont(FONT_NORMAL);
        table.setGridColor(new Color(215, 225, 238));
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setSelectionForeground(PRIMARY_DARK);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_LABEL);
        header.setReorderingAllowed(false);
        header.setOpaque(true);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(CENTER);
                lbl.setBackground(TABLE_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(FONT_LABEL);
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(14, 78, 156)));
                lbl.setOpaque(true);
                return lbl;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, col);
                lbl.setFont(FONT_NORMAL);
                lbl.setOpaque(true);
                if (isSelected) {
                    lbl.setBackground(tbl.getSelectionBackground());
                    lbl.setForeground(tbl.getSelectionForeground());
                } else {
                    lbl.setBackground((row % 2 == 0) ? Color.WHITE : TABLE_STRIPE);
                    lbl.setForeground(PRIMARY_DARK);
                }
                return lbl;
            }
        });
    }

    private JButton makeButton(String text) {
        final Color bg;
        final Color fg;
        if (text != null && text.toLowerCase().contains("excel")) {
            bg = SUCCESS;
            fg = Color.WHITE;
        } else if (text != null && text.toLowerCase().contains("pdf")) {
            bg = DANGER;
            fg = Color.WHITE;
        } else {
            bg = PRIMARY;
            fg = Color.WHITE;
        }

        Canvas cv = new Canvas();
        Font btnFont = new Font("Segoe UI", Font.BOLD, 13);
        FontMetrics fm0 = cv.getFontMetrics(btnFont);
        final int w = fm0.stringWidth(text) + 36;

        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color base = getModel().isEnabled() ? bg : new Color(180, 180, 180);
                g2.setColor(getModel().isRollover() ? base.darker() : base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setColor(fg);
                g2.setFont(btnFont);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text,
                        (getWidth() - fm.stringWidth(text)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }

            @Override public Dimension getPreferredSize() { return new Dimension(w, 36); }
            @Override public Dimension getMinimumSize() { return getPreferredSize(); }
            @Override public Dimension getMaximumSize() { return getPreferredSize(); }
        };

        btn.setText("");
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTabbedPane(JTabbedPane tabs) {
        tabs.setFont(FONT_TAB);
        tabs.setBackground(CONTENT_BG);
        tabs.setForeground(PRIMARY_DARK);
        tabs.setOpaque(false);
        tabs.setBorder(BorderFactory.createEmptyBorder(4, 2, 2, 2));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setFocusable(false);
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected void installDefaults() {
                super.installDefaults();
                highlight = ACCENT;
                lightHighlight = ACCENT;
                shadow = new Color(200, 215, 235);
                darkShadow = new Color(180, 200, 225);
                focus = ACCENT;
                tabInsets = new Insets(7, 16, 7, 16);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
                tabAreaInsets = new Insets(2, 2, 2, 2);
                contentBorderInsets = new Insets(8, 0, 0, 0);
            }

            @Override
            public void paint(Graphics g, javax.swing.JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int areaH = calculateTabAreaHeight(tabPane.getTabPlacement(), runCount, maxTabHeight);

                // Nền khu vực tab để tạo cảm giác card hiện đại.
                g2.setColor(new Color(232, 240, 250));
                g2.fillRoundRect(0, 0, c.getWidth() - 1, Math.max(32, areaH + 3), 14, 14);
                g2.setColor(new Color(205, 220, 240));
                g2.drawRoundRect(0, 0, c.getWidth() - 1, Math.max(32, areaH + 3), 14, 14);
                g2.dispose();

                super.paint(g, c);
            }

            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 6;
            }

            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 10;
            }

            @Override protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 14;
                int px = x + 2;
                int py = y + 2;
                int pw = w - 4;
                int ph = h - 4;

                if (isSelected) {
                    g2.setColor(new Color(0, 0, 0, 18));
                    g2.fillRoundRect(px, py + 1, pw, ph, arc, arc);
                    g2.setPaint(new GradientPaint(px, py, new Color(238, 250, 255), px + pw, py, new Color(218, 240, 252)));
                    g2.fillRoundRect(px, py, pw, ph, arc, arc);
                    g2.setColor(new Color(158, 203, 232));
                    g2.drawRoundRect(px, py, pw, ph, arc, arc);
                } else {
                    g2.setColor(new Color(243, 248, 255));
                    g2.fillRoundRect(px, py, pw, ph, arc, arc);
                    g2.setColor(new Color(210, 225, 242));
                    g2.drawRoundRect(px, py, pw, ph, arc, arc);
                }
                g2.dispose();
            }

            @Override protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // Intentionally no border: avoid double lines caused by LAF + custom painting.
            }

            @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                // Suppress default tab border to keep a clean, flat tab row.
            }

            @Override protected void paintFocusIndicator(Graphics g, int tabPlacement,
                    Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // No focus ring to prevent extra lines/artifacts on some system themes.
            }

            @Override protected void paintText(Graphics g, int tabPlacement, Font font,
                    FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setFont(isSelected ? FONT_TAB.deriveFont(Font.BOLD) : FONT_TAB.deriveFont(Font.PLAIN));
                g.setColor(isSelected ? PRIMARY : PRIMARY_DARK);
                g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            }
        });
    }

    private void styleToolbar(JPanel toolbar) {
        toolbar.setBackground(CARD_BG);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 230, 245)),
                BorderFactory.createEmptyBorder(2, 2, 6, 2)));
        toolbar.addContainerListener(new java.awt.event.ContainerAdapter() {
            @Override public void componentAdded(java.awt.event.ContainerEvent e) {
                if (e.getChild() instanceof JLabel label) {
                    label.setFont(FONT_LABEL);
                    label.setForeground(PRIMARY_DARK);
                }
            }
        });
        for (Component c : toolbar.getComponents()) {
            if (c instanceof JLabel label) {
                label.setFont(FONT_LABEL);
                label.setForeground(PRIMARY_DARK);
            }
        }
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setFont(FONT_NORMAL);
        cb.setBackground(Color.WHITE);
        cb.setForeground(PRIMARY_DARK);
        cb.setOpaque(true);
        cb.setFocusable(false);
        cb.setBorder(BorderFactory.createLineBorder(new Color(185, 205, 232)));
        cb.setPreferredSize(new Dimension(Math.max(cb.getPreferredSize().width, 120), 30));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setFont(FONT_NORMAL);
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                if (isSelected) {
                    lbl.setBackground(new Color(225, 238, 255));
                    lbl.setForeground(PRIMARY_DARK);
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(PRIMARY_DARK);
                }
                return lbl;
            }
        });
    }

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createLineBorder(new Color(205, 220, 240)));
        return sp;
    }

    /** Tạo danh sách năm từ năm hiện tại trở về 10 năm trước */
    private String[] buildYearItems() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[11];
        for (int i = 0; i < 11; i++) {
            years[i] = String.valueOf(currentYear - i);
        }
        return years;
    }

    private String[] buildMonthItems() {
        String[] months = new String[12];
        for (int i = 1; i <= 12; i++) {
            months[i - 1] = String.format("%02d", i);
        }
        return months;
    }

    private int getRowLimit(JComboBox<String> cb) {
        if (cb == null) return Integer.MAX_VALUE;
        return switch ((String) cb.getSelectedItem()) {
            case "Top 3"  -> 3;
            case "Top 5"  -> 5;
            case "Top 10" -> 10;
            case "Top 20" -> 20;
            default       -> Integer.MAX_VALUE;
        };
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS: FORMAT
    // ════════════════════════════════════════════════════════════════════════

    private String formatVND(double amount) {
        return String.format("%,.0f đ", amount);
    }

    private String formatID(String prefix, int id) {
        return String.format("%s%05d", prefix, id);
    }
}