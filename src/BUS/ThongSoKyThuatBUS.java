package BUS;

import DAO.ThongSoKyThuatDAO;
import DTO.ThongSoKyThuatDTO;

public class ThongSoKyThuatBUS {

    private final ThongSoKyThuatDAO tsktDAO = new ThongSoKyThuatDAO();

    // 1. Lấy thông tin theo mã sản phẩm
    // Sửa lỗi: Đổi findByMaSanPham -> getByMaSP (cho khớp với DAO)
    public ThongSoKyThuatDTO getByMaSP(int maSP) {
        return tsktDAO.getByMaSP(maSP);
    }

    // 2. Thêm mới thông số kỹ thuật
    public boolean add(ThongSoKyThuatDTO ts) {
        // Sửa lỗi Line 28: Kiểm tra dữ liệu đầu vào
        // Giả sử bạn muốn kiểm tra Mã SP phải > 0
        if (ts.getMaSP() <= 0) {
            return false;
        }
        
        // Nếu muốn kiểm tra CPU không được rỗng (Kiểu String)
        if (ts.getCpu() == null || ts.getCpu().trim().isEmpty()) {
             return false;
        }

        // Sửa lỗi Line 40: existsByMaSanPham -> isExist (Hàm vừa thêm ở Bước 1)
        if (tsktDAO.isExist(ts.getMaSP())) {
            return false; // Đã tồn tại thì không thêm
        }
        
        return tsktDAO.insert(ts);
    }

    // 3. Cập nhật thông số
    public boolean update(ThongSoKyThuatDTO ts) {
        // Sửa lỗi Line 53: existsByMaSanPham -> isExist
        if (!tsktDAO.isExist(ts.getMaSP())) {
            return false; // Không tồn tại thì không sửa được
        }
        return tsktDAO.update(ts);
    }

    // 4. Xóa thông số
    public boolean delete(int maSP) {
        // Sửa lỗi Line 77: existsByMaSanPham -> isExist
        if (!tsktDAO.isExist(maSP)) {
            return false;
        }
        // Sửa lỗi Line 87: deleteByMaSanPham -> deleteByMaSP (cho khớp với DAO)
        return tsktDAO.deleteByMaSP(maSP);
    }
}
