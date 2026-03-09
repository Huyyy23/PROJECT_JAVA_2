package BUS;

import DAO.TaiKhoanDAO;
import DTO.TaiKhoanDTO;
import DTO.SharedData;

public class TaiKhoanBUS {
    private TaiKhoanDAO dao = new TaiKhoanDAO();

    public boolean login(String user, String pass) {
        // Kiểm tra cơ bản
        if (user.isEmpty() || pass.isEmpty()) {
            return false;
        }

        // Gọi DAO kiểm tra Database
        TaiKhoanDTO tk = dao.checkLogin(user, pass);
        
        if (tk != null) {
            // Dùng SharedData.login() thay vì ghi thủ công từng field
            SharedData.login(tk.getMaNV(), tk.getTenNV(), tk.getVaiTro());
            return true;
        }
        
        return false; // Thất bại
    }
}