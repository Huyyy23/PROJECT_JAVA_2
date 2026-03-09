package GUI;

import javax.swing.*;
import javax.swing.border.*;

import BUS.TaiKhoanBUS;

import java.awt.*;


public class DangNhap extends JFrame {

    private static final Color PRIMARY      = new Color(21, 101, 192);
    private static final Color PRIMARY_DARK = new Color(10, 60, 130);
    private static final Color ACCENT       = new Color(0, 188, 212);
    private static final Color LABEL_COLOR  = new Color(200, 230, 255);
    private static final Color FIELD_BORDER = new Color(0, 188, 212);

    public DangNhap() {
        setTitle("Đăng Nhập - Phần mềm quản lý cửa hàng Laptop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền gradient xanh đậm góc trái trên → xanh nhạt góc phải dưới
                g2.setPaint(new GradientPaint(0, 0, new Color(10, 60, 130), getWidth(), getHeight(), new Color(180, 220, 255)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Vòng tròn ánh sáng trang trí góc trái dưới
                g2.setColor(new Color(0, 188, 212, 30));
                g2.fillOval(-100, getHeight() - 300, 500, 500);
                // Vòng tròn ánh sáng góc phải trên
                g2.setColor(new Color(100, 180, 255, 25));
                g2.fillOval(getWidth() - 250, -150, 450, 450);
                g2.dispose();
            }
        };
        mainPanel.setBorder(new LineBorder(ACCENT, 2));
        setContentPane(mainPanel);

        // ===== LEFT: LOGO =====
        JPanel logoPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2 - 20;
                int r = 72;

                // Glow rings
                for (int i = 5; i > 0; i--) {
                    g2.setColor(new Color(0, 188, 212, 10 * i));
                    g2.fillPolygon(hexagon(cx, cy, r + i * 5));
                }

                // Outer hex
                g2.setPaint(new GradientPaint(cx - r, cy - r, new Color(30, 130, 220), cx + r, cy + r, PRIMARY_DARK));
                g2.fillPolygon(hexagon(cx, cy, r));
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawPolygon(hexagon(cx, cy, r));

                // Inner hex
                g2.setColor(new Color(10, 50, 120, 200));
                g2.fillPolygon(hexagon(cx, cy, r - 18));

                // LAPTOP
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("LAPTOP", cx - fm.stringWidth("LAPTOP") / 2, cy - 5);

                // STORE cyan
                g2.setColor(ACCENT);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                fm = g2.getFontMetrics();
                g2.drawString("STORE", cx - fm.stringWidth("STORE") / 2, cy + 14);

                // Laptop icon
                g2.setColor(new Color(200, 235, 255));
                g2.fillRoundRect(cx - 18, cy + 24, 36, 20, 4, 4);
                g2.setColor(new Color(10, 50, 120));
                g2.fillRect(cx - 14, cy + 27, 28, 14);
                g2.setColor(ACCENT);
                g2.fillRect(cx - 22, cy + 44, 44, 4);

                // Store name
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g2.setColor(Color.WHITE);
                fm = g2.getFontMetrics();
                String name = "LAPTOP STORE";
                g2.drawString(name, cx - fm.stringWidth(name) / 2, cy + r + 30);

                // Accent underline
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(2f));
                int lw = fm.stringWidth(name);
                g2.drawLine(cx - lw / 2, cy + r + 35, cx + lw / 2, cy + r + 35);

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
        logoPanel.setOpaque(false);
        logoPanel.setBounds(50, 50, 460, 480);
        mainPanel.add(logoPanel);

        // ===== RIGHT: FORM =====

        // Title
        JLabel titleLabel = new JLabel("ĐĂNG NHẬP", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(580, 90, 480, 45);
        mainPanel.add(titleLabel);

        // Accent underline dưới title
        JPanel titleLine = new JPanel();
        titleLine.setBackground(ACCENT);
        titleLine.setOpaque(true);
        titleLine.setBounds(580, 138, 200, 3);
        mainPanel.add(titleLine);

        // Tài Khoản label
        JLabel userLabel = new JLabel("Tài Khoản:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(LABEL_COLOR);
        userLabel.setBounds(580, 175, 150, 25);
        mainPanel.add(userLabel);

        // Tài Khoản field
        JTextField userField = createTextField();
        userField.setBounds(580, 203, 380, 44);
        mainPanel.add(userField);

        // Mật khẩu label
        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passLabel.setForeground(LABEL_COLOR);
        passLabel.setBounds(580, 275, 150, 25);
        mainPanel.add(passLabel);

        // Mật khẩu field
        JPasswordField passField = new JPasswordField();
        passField.setBounds(580, 303, 380, 44);
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passField.setBorder(new CompoundBorder(
            new LineBorder(FIELD_BORDER, 1),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        passField.setBackground(Color.WHITE);
        mainPanel.add(passField);

        // Login button
        JButton loginBtn = new JButton("ĐĂNG NHẬP") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                    ? new Color(10, 80, 170)
                    : PRIMARY;
                g2.setPaint(new GradientPaint(0, 0, bg.brighter(), getWidth(), getHeight(), PRIMARY_DARK));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Cyan top highlight
                g2.setColor(new Color(0, 188, 212, 150));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(16, 1, getWidth() - 16, 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginBtn.setBounds(630, 390, 280, 50);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setOpaque(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginBtn.addActionListener(e -> {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());
            
            // Kiểm tra nhanh phía giao diện
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            TaiKhoanBUS bus = new TaiKhoanBUS();
            
            // Bây giờ bus.login trả về boolean, không còn lỗi Type Mismatch
            if (bus.login(user, pass)) {
                this.dispose();     // Đóng form đăng nhập
                new LaptopStore();  // Mở Dashboard chính
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
            }
        });

        mainPanel.add(loginBtn);

        setVisible(true);
    }

    private JTextField createTextField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(new CompoundBorder(
            new LineBorder(FIELD_BORDER, 1),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        f.setBackground(Color.WHITE);
        return f;
    }

}