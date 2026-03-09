package GUI;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

public class LaptopStore extends JFrame {

    private static final Color PRIMARY      = new Color(21, 101, 192);
    private static final Color PRIMARY_DARK = new Color(10, 60, 130);
    private static final Color ACCENT       = new Color(0, 188, 212);
    private static final Color CONTENT_BG   = new Color(236, 242, 250);
    private static final Color SIDEBAR_TEXT = Color.WHITE;

    private static final Font MENU_FONT   = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font MENU_BOLD   = new Font("Segoe UI", Font.BOLD, 17);

    private JPanel activeItem = null;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel menuPanel; 

    public LaptopStore() {
        setTitle("Phần mềm quản lý cửa hàng Laptop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel body = new JPanel(new BorderLayout());
        body.add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CONTENT_BG);

        contentPanel.add(new BanHangPanel2(), "Bán hàng");
        contentPanel.add(new NhapHangPanelGUI(), "Nhập hàng");
        contentPanel.add(new SanPhamPanel(), "Sản phẩm");
        contentPanel.add(new NhanVienPanel(), "Nhân viên");
        contentPanel.add(new HoaDonPanel(), "Hóa đơn");
        contentPanel.add(new KhachHangPanel(), "Khách hàng");
        contentPanel.add(new NhaCungCapPanel(), "Nhà cung cấp");
        contentPanel.add(new BaoHanhPanel(), "Bảo hành");
        contentPanel.add(createPlaceholder("Đổi trả", "🔄"), "Đổi trả");
        contentPanel.add(new ThongKeGUI(), "Thống kê");

        body.add(contentPanel, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        
        setVisible(true);
        
        // BỔ SUNG 05/03/2026: Gọi hàm phân quyền sau khi giao diện sẵn sàng
        checkPermission();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, 0, getHeight(), new Color(15, 80, 160)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, ACCENT));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(createLogoPanel());
        topContainer.add(createUserInfoPanel());
        topContainer.add(Box.createVerticalStrut(10));
        
        sidebar.add(topContainer, BorderLayout.NORTH);

        menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        String[][] items = {
            {"🛒","Bán hàng"}, {"🧾","Hóa đơn"}, {"🚚","Nhập hàng"}, {"💻","Sản phẩm"},
            {"👤","Nhân viên"}, {"🤝","Khách hàng"}, {"🏭","Nhà cung cấp"},
            {"🛡️","Bảo hành"}, {"🔄","Đổi trả"}, {"📊","Thống kê"}
        };
        for (String[] item : items) menuPanel.add(createMenuItem(item[0], item[1]));

    

        sidebar.add(menuPanel, BorderLayout.CENTER);

        JLabel version = new JLabel("v1.0.0", SwingConstants.CENTER);
        version.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        version.setForeground(new Color(150, 200, 255, 150));
        version.setBorder(BorderFactory.createEmptyBorder(6, 0, 8, 0));
        sidebar.add(version, BorderLayout.SOUTH);
        return sidebar;
    }

    // BỔ SUNG 05/03/2026: Hàm thực hiện ẩn menu dựa trên vai trò
    // BỔ SUNG/CẬP NHẬT NGÀY 05/03/2026: Ẩn thêm menu Nhân viên
    private void checkPermission() {
        String role = DTO.SharedData.currentRole;
        
        // Nếu là nhân viên thường, không cho phép quản lý nhân sự và xem doanh thu
        if ("NhanVienBanHang".equals(role)) {
            for (Component c : menuPanel.getComponents()) {
                if (c instanceof JPanel) {
                    // Ẩn menu Thống kê
                    if (isTargetMenu((JPanel)c, "Thống kê")) {
                        c.setVisible(false);
                    }
                    // Ẩn menu Nhân viên
                    if (isTargetMenu((JPanel)c, "Nhân viên")) {
                        c.setVisible(false);
                    }
                }
            }
            // Cập nhật lại giao diện sau khi ẩn các thành phần
            menuPanel.revalidate();
            menuPanel.repaint();
        }
    }

    // BỔ SUNG 05/03/2026: Hàm phụ trợ tìm đúng menu item dựa vào text
    private boolean isTargetMenu(JPanel panel, String text) {
        for (Component c : panel.getComponents()) {
            if (c instanceof JPanel) { // Tầng inner trong code của bạn
                for (Component inner : ((JPanel)c).getComponents()) {
                    if (inner instanceof JLabel && ((JLabel)inner).getText().equals(text)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private JPanel createLogoPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2 - 5, r = 52;

                for (int i = 5; i > 0; i--) {
                    g2.setColor(new Color(0, 188, 212, 12 * i));
                    g2.fillPolygon(hexagon(cx, cy, r + i * 4));
                }
                g2.setPaint(new GradientPaint(cx - r, cy - r, new Color(30, 130, 220), cx + r, cy + r, new Color(10, 60, 140)));
                g2.fillPolygon(hexagon(cx, cy, r));
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawPolygon(hexagon(cx, cy, r));
                g2.setColor(new Color(10, 50, 120, 200));
                g2.fillPolygon(hexagon(cx, cy, r - 13));

                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("LAPTOP", cx - fm.stringWidth("LAPTOP") / 2, cy - 4);
                g2.setColor(ACCENT);
                g2.drawString("STORE", cx - fm.stringWidth("STORE") / 2, cy + 12);

                g2.setColor(new Color(200, 235, 255));
                g2.fillRoundRect(cx - 14, cy + 18, 28, 16, 3, 3);
                g2.setColor(new Color(10, 50, 120));
                g2.fillRect(cx - 11, cy + 20, 22, 11);
                g2.setColor(ACCENT);
                g2.fillRect(cx - 18, cy + 34, 36, 3);
                g2.dispose();
            }
            private Polygon hexagon(int cx, int cy, int r) {
                Polygon p = new Polygon();
                for (int i = 0; i < 6; i++) {
                    double a = Math.toRadians(60 * i - 30);
                    p.addPoint((int)(cx + r * Math.cos(a)), (int)(cy + r * Math.sin(a)));
                }
                return p;
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(220, 155));
        panel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(0, 188, 212, 100)));
        return panel;
    }

    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 15)); 
                g2.fillRoundRect(10, 0, getWidth() - 20, getHeight(), 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(220, 60));
        // Căn chỉnh lại lề để có đủ không gian cho nút đăng xuất bên phải
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 15));

        String name = DTO.SharedData.currentTenNV != null ? DTO.SharedData.currentTenNV : "Chưa đăng nhập";
        String role = DTO.SharedData.currentRole != null ? 
                    (DTO.SharedData.currentRole.equals("QuanLy") ? "Quản Lý" : "Nhân viên Bán Hàng") : "";

        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel(role);
        lblRole.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblRole.setForeground(ACCENT);

        JPanel infoText = new JPanel(new GridLayout(2, 1, 0, 2));
        infoText.setOpaque(false);
        infoText.add(lblName);
        infoText.add(lblRole);

        // --- BẮT ĐẦU: KHUNG NÚT ĐĂNG XUẤT ---
        // --- BẮT ĐẦU: KHUNG NÚT ĐĂNG XUẤT (VẼ BẰNG CODE - NÉT 100%) ---
        JLabel btnLogout = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Bật cờ khử răng cưa và làm nét cao nhất
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Trạng thái màu khi hover (Đỏ rực) và bình thường (Trắng)
                Color iconColor = getForeground();
                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                
                // Vẽ cái khung cửa sắc nét
                g2.drawLine(cx - 2, cy - 8, cx - 8, cy - 8); // Cạnh trên
                g2.drawLine(cx - 8, cy - 8, cx - 8, cy + 8); // Cạnh trái
                g2.drawLine(cx - 8, cy + 8, cx - 2, cy + 8); // Cạnh dưới
                
                // Vẽ mũi tên đi ra sắc nét
                g2.drawLine(cx - 3, cy, cx + 7, cy); // Trục mũi tên
                g2.drawLine(cx + 4, cy - 3, cx + 8, cy); // Cánh trên mũi tên
                g2.drawLine(cx + 4, cy + 3, cx + 8, cy); // Cánh dưới mũi tên
                
                g2.dispose();
            }
        }; 
        btnLogout.setPreferredSize(new Dimension(32, 32));
        btnLogout.setToolTipText("Đăng xuất");
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setForeground(Color.WHITE); // Màu mặc định

        // Sự kiện hover đổi màu đỏ rực và click đăng xuất
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogout.setForeground(new Color(255, 82, 82)); // Đỏ rực khi di chuột vào
                btnLogout.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnLogout.setForeground(Color.WHITE); // Trắng khi chuột đi ra
                btnLogout.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                int confirm = JOptionPane.showConfirmDialog(LaptopStore.this, 
                        "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", 
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dispose(); 
                    new DangNhap().setVisible(true);
                }
            }
        });
        // --- KẾT THÚC: KHUNG NÚT ĐĂNG XUẤT ---

        // Phục hồi lại đoạn code bị xóa mất
        panel.add(infoText, BorderLayout.CENTER);
        panel.add(btnLogout, BorderLayout.EAST); 
        
        return panel; // Thêm lại câu lệnh return
    }

    private JPanel createMenuItem(String iconText, String labelText) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(220, 54));
        wrapper.setPreferredSize(new Dimension(220, 54));
        wrapper.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel stripe = new JPanel();
        stripe.setBackground(ACCENT);
        stripe.setPreferredSize(new Dimension(4, 54));
        stripe.setVisible(false);

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setOpaque(false);

        JLabel icon = new JLabel(iconText);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        icon.setForeground(new Color(180, 215, 255));

        JLabel text = new JLabel(labelText);
        text.setFont(MENU_FONT);
        text.setForeground(SIDEBAR_TEXT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 16, 0, 0);
        gbc.gridy = 0;

        gbc.gridx = 0;
        inner.add(icon, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 10, 0, 0);
        inner.add(text, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1.0;
        inner.add(Box.createHorizontalGlue(), gbc);

        wrapper.add(stripe, BorderLayout.WEST);
        wrapper.add(inner, BorderLayout.CENTER);

        wrapper.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (wrapper != activeItem) {
                    inner.setOpaque(true);
                    inner.setBackground(new Color(255, 255, 255, 20));
                    text.setForeground(new Color(200, 235, 255));
                    wrapper.repaint();
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (wrapper != activeItem) {
                    inner.setOpaque(false);
                    text.setForeground(SIDEBAR_TEXT);
                    wrapper.repaint();
                }
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (activeItem != null) {
                    for (Component c : activeItem.getComponents()) {
                        if (c instanceof JPanel && ((JPanel)c).getPreferredSize().width == 4)
                            c.setVisible(false);
                        if (c instanceof JPanel) {
                            ((JPanel)c).setOpaque(false);
                            for (Component cc : ((JPanel)c).getComponents()) {
                                if (cc instanceof JLabel) {
                                    JLabel l = (JLabel)cc;
                                    if (l.getText().length() <= 3)
                                        l.setForeground(new Color(180, 215, 255));
                                    else { l.setFont(MENU_FONT); l.setForeground(SIDEBAR_TEXT); }
                                }
                            }
                        }
                    }
                    activeItem.repaint();
                }
                stripe.setVisible(true);
                inner.setOpaque(true);
                inner.setBackground(new Color(255, 255, 255, 35));
                icon.setForeground(ACCENT);
                text.setFont(MENU_BOLD);
                text.setForeground(Color.WHITE);
                activeItem = wrapper;
                cardLayout.show(contentPanel, labelText);
                wrapper.repaint();
            }
        });
        return wrapper;
    }

    private JPanel createPlaceholder(String name, String emoji) {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(CONTENT_BG);

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(6, 6, getWidth() - 6, getHeight() - 6, 18, 18);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 18, 18);
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, getWidth() - 6, 7, 10, 10);
                g2.fillRect(0, 4, getWidth() - 6, 3);
                g2.setColor(ACCENT);
                g2.fillOval(getWidth() - 32, -2, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(480, 310));
        card.setBorder(BorderFactory.createEmptyBorder(40, 50, 35, 50));

        JLabel iconLabel = new JLabel(emoji, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 62));
        iconLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(name);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(PRIMARY);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 8, 0));

        JPanel line = new JPanel();
        line.setBackground(ACCENT);
        line.setMaximumSize(new Dimension(180, 3));
        line.setPreferredSize(new Dimension(180, 3));
        line.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Chức năng đang được phát triển...");
        subLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subLabel.setForeground(new Color(150, 160, 175));
        subLabel.setAlignmentX(CENTER_ALIGNMENT);
        subLabel.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        card.add(iconLabel);
        card.add(titleLabel);
        card.add(line);
        card.add(subLabel);

        outer.add(card);
        return outer;
    }

}