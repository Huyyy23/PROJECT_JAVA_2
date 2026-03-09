package BUS;

import DAO.ChiTietPhieuNhapDAO;
import DAO.PhieuNhapDAO;
import DTO.ChiTietPhieuNhapDTO;
import DTO.PhieuNhapDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

/**
 * BUS cho PhieuNhap — tất cả logic nghiệp vụ nằm ở đây.
 * BUS là nơi duy nhất quản lý Connection + transaction.
 * DAO chỉ nhận Connection truyền vào, không tự commit/rollback.
 *
 * CHANGELOG:
 *   2026-03-09 - [SỬA TOÀN BỘ] Tập trung transaction vào BUS:
 *                  luuPhieu()  : 1 transaction cho insert PHIEUNHAP + CHITIETPHIEUNHAP
 *                  thanhToan() : 1 transaction cho sinh SERIAL + update TrangThai
 *                  Trước đây mỗi DAO tự lấy Connection riêng → không dùng chung
 *                  transaction → phiếu/serial có thể không được lưu khi lỗi giữa chừng.
 */
public class PhieuNhapBUS {

    private final PhieuNhapDAO        phieuNhapDAO = new PhieuNhapDAO();
    private final ChiTietPhieuNhapDAO chiTietDAO   = new ChiTietPhieuNhapDAO();

    // ================================================================
    // LUU PHIEU — insert PHIEUNHAP + tất cả CHITIETPHIEUNHAP trong 1 transaction
    // Luôn lưu với TrangThai = ChoXuLy, serial chưa sinh
    // Trả về maPN mới, ném exception nếu thất bại
    // ================================================================
    public int luuPhieu(PhieuNhapDTO phieuDTO,
                        List<ChiTietPhieuNhapDTO> dsChiTiet) throws SQLException {
        if (dsChiTiet == null || dsChiTiet.isEmpty())
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất 1 sản phẩm!");
        if (phieuDTO.getMaNV() <= 0)
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ!");
        if (phieuDTO.getMaNhaCungCap() <= 0)
            throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ!");

        Connection con = DBConnection.getConnection();
        try {
            con.setAutoCommit(false);

            // 1. Insert PHIEUNHAP (luôn ChoXuLy trước)
            phieuDTO.setTrangThai("ChoXuLy");
            int maPN = phieuNhapDAO.insert(con, phieuDTO);

            // 2. Insert từng dòng CHITIETPHIEUNHAP
            for (ChiTietPhieuNhapDTO ct : dsChiTiet) {
                ct.setMaPN(maPN);
                chiTietDAO.insert(con, ct);
            }

            // 3. Cập nhật tổng tiền
            phieuNhapDAO.updateTongTien(con, maPN);

            con.commit();
            return maPN;

        } catch (Exception e) {
            try { con.rollback(); } catch (SQLException ignored) {}
            throw new SQLException("Lưu phiếu thất bại: " + e.getMessage(), e);
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ================================================================
    // THANH TOAN — sinh SERIAL + đổi TrangThai → HoanThanh trong 1 transaction
    // Chỉ cho phép phiếu đang ChoXuLy
    // ================================================================
    public void thanhToan(int maPN) throws SQLException {
        if (maPN <= 0) throw new IllegalArgumentException("Mã phiếu nhập không hợp lệ!");

        // Kiểm tra trạng thái hiện tại (đọc ngoài transaction — chỉ đọc)
        PhieuNhapDTO pn = phieuNhapDAO.getById(maPN);
        if (pn == null)
            throw new IllegalArgumentException("Không tìm thấy phiếu nhập #" + maPN);
        if (!"ChoXuLy".equals(pn.getTrangThai()))
            throw new IllegalStateException(
                "Chỉ phiếu 'Chờ xử lý' mới được thanh toán!\n" +
                "Phiếu #" + maPN + " đang ở trạng thái: " + pn.getTrangThai());

        Connection con = DBConnection.getConnection();
        try {
            con.setAutoCommit(false);

            // 1. Sinh serial cho toàn bộ chi tiết của phiếu
            chiTietDAO.insertSerialsByMaPN(con, maPN);

            // 2. Đổi TrangThai → HoanThanh (trigger SQL sẽ cộng tồn kho)
            phieuNhapDAO.updateTrangThai(con, maPN, "HoanThanh");

            con.commit();

        } catch (Exception e) {
            try { con.rollback(); } catch (SQLException ignored) {}
            throw new SQLException("Thanh toán thất bại: " + e.getMessage(), e);
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ================================================================
    // HUY PHIEU — chỉ đổi TrangThai, không động đến serial
    // ================================================================
    public void huyPhieu(int maPN) throws SQLException {
        PhieuNhapDTO pn = phieuNhapDAO.getById(maPN);
        if (pn == null)
            throw new IllegalArgumentException("Không tìm thấy phiếu nhập #" + maPN);
        if ("HoanThanh".equals(pn.getTrangThai()))
            throw new IllegalStateException("Phiếu đã hoàn thành không thể hủy!");

        Connection con = DBConnection.getConnection();
        try {
            con.setAutoCommit(false);
            phieuNhapDAO.updateTrangThai(con, maPN, "Huy");
            con.commit();
        } catch (Exception e) {
            try { con.rollback(); } catch (SQLException ignored) {}
            throw new SQLException("Hủy phiếu thất bại: " + e.getMessage(), e);
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ================================================================
    // GET ALL WITH NAMES — cho bảng UI
    // ================================================================
    public List<Object[]> getAllWithNames() throws SQLException {
        return phieuNhapDAO.getAllWithNames();
    }

    // ================================================================
    // GET BY ID
    // ================================================================
    public PhieuNhapDTO getById(int maPN) throws SQLException {
        return phieuNhapDAO.getById(maPN);
    }

    // ================================================================
    // GET BY TRANG THAI
    // ================================================================
    public List<PhieuNhapDTO> getByTrangThai(String trangThai) throws SQLException {
        return phieuNhapDAO.getByTrangThai(trangThai);
    }

    // ================================================================
    // GET BY NHA CUNG CAP
    // ================================================================
    public List<PhieuNhapDTO> getByNhaCungCap(int maNCC) throws SQLException {
        return phieuNhapDAO.getByNhaCungCap(maNCC);
    }

    // ================================================================
    // GET BY KHOANG NGAY
    // ================================================================
    public List<PhieuNhapDTO> getByKhoangNgay(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        if (tuNgay.isAfter(denNgay))
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc!");
        return phieuNhapDAO.getByKhoangNgay(tuNgay, denNgay);
    }
}
