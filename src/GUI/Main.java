package GUI; // Hoặc package main tùy bạn cấu trúc

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // 1. Thiết lập giao diện chung (Look and Feel) cho toàn bộ App
        // Sử dụng giao diện hệ thống (Windows/Mac) thay vì giao diện Java cổ điển
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 2. Khởi chạy luồng giao diện (Event Dispatch Thread) - Chuẩn an toàn của Java Swing
        SwingUtilities.invokeLater(() -> {
            // Mở form đăng nhập đầu tiên
            DangNhap loginForm = new DangNhap();
            loginForm.setVisible(true);
        });
    }
}