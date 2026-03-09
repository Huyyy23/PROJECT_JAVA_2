package BUS;

import DAO.PhieuNhapDAO;
import DTO.PhieuNhapDTO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PhieuNhapBUS {

    private final PhieuNhapDAO phieuNhapDAO;

    public PhieuNhapBUS() {
        this.phieuNhapDAO = new PhieuNhapDAO();
    }

    // Lấy danh sách tất cả phiếu nhập
    public List<PhieuNhapDTO> getAll() throws SQLException {
        return phieuNhapDAO.getAll();
    }

    // Thêm phiếu nhập mới
    public int addPhieuNhap(PhieuNhapDTO dto) throws SQLException, IllegalArgumentException {
        // Validation cơ bản
        if (dto.getMaNhaCungCap() <= 0) {
            throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ!");
        }
        if (dto.getMaNV() <= 0) {
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ!");
        }
        
        return phieuNhapDAO.insert(dto);
    }

    // Tìm phiếu nhập theo MaPN
    public PhieuNhapDTO getById(int maPN) throws SQLException {
        if (maPN <= 0) return null;
        return phieuNhapDAO.getById(maPN);
    }

    // Lấy phiếu nhập theo Nhà cung cấp
    public List<PhieuNhapDTO> getByNhaCungCap(int maNhaCungCap) throws SQLException {
        if (maNhaCungCap <= 0) throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ!");
        return phieuNhapDAO.getByNhaCungCap(maNhaCungCap);
    }

    // Lấy phiếu nhập theo trạng thái (HoanThanh, Huy)
    public List<PhieuNhapDTO> getByTrangThai(String trangThai) throws SQLException {
        if (trangThai == null || trangThai.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái không được để trống!");
        }
        return phieuNhapDAO.getByTrangThai(trangThai);
    }

    // Lọc phiếu nhập theo khoảng ngày
    public List<PhieuNhapDTO> getByKhoangNgay(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        if (tuNgay == null || denNgay == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và kết thúc không được để trống!");
        }
        if (tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc!");
        }
        return phieuNhapDAO.getByKhoangNgay(tuNgay, denNgay);
    }

    // Cập nhật tổng tiền của phiếu nhập (Gọi sau khi đã thêm xong chi tiết)
    public boolean updateTongTien(int maPN) throws SQLException {
        if (maPN <= 0) return false;
        return phieuNhapDAO.updateTongTien(maPN);
    }

    // Hủy phiếu nhập
    public boolean huyPhieuNhap(int maPN) throws SQLException {
        if (maPN <= 0) return false;
        return phieuNhapDAO.huyPhieuNhap(maPN);
    }

    // Xóa phiếu nhập (chỉ khi chưa có chi tiết)
    public boolean deletePhieuNhap(int maPN) throws SQLException {
        if (maPN <= 0) return false;
        return phieuNhapDAO.delete(maPN);
    }
    public boolean updateTrangThai(int maPN, String trangThaiMoi) {
        // Validate dữ liệu cơ bản
        if (maPN <= 0) {
            System.err.println("Mã phiếu nhập không hợp lệ!");
            return false;
        }
        if (trangThaiMoi == null || trangThaiMoi.trim().isEmpty()) {
            System.err.println("Trạng thái mới không được để trống!");
            return false;
        }
        
        // Gọi xuống DAO để thực thi
        return phieuNhapDAO.updateTrangThai(maPN, trangThaiMoi);
    }
}