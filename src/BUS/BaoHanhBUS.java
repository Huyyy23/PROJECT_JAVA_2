package BUS;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import DAO.BaoHanhDAO;
import DTO.BaoHanhDTO;

public class BaoHanhBUS {

    private final BaoHanhDAO dao = new BaoHanhDAO();

    // =========================================================================
    // LẤY DỮ LIỆU
    // =========================================================================

    /**
     * Lấy toàn bộ danh sách bảo hành
     */
    public List<BaoHanhDTO> getAll() {
        return dao.getAll();
    }

    /**
     * Lấy bảo hành theo mã
     */
    public BaoHanhDTO getById(int maBaoHanh) {
        if (maBaoHanh <= 0) {
            throw new IllegalArgumentException("Mã bảo hành phải > 0");
        }
        return dao.layTheoMa(maBaoHanh);
    }

    /**
     * Lấy danh sách bảo hành đang xử lý
     */
    public List<BaoHanhDTO> getDangXuLy() {
        return dao.layDangXuLy();
    }

    /**
     * Lấy danh sách bảo hành theo Serial/IMEI
     */
    public List<BaoHanhDTO> getBySerial(int maSerial) {
        if (maSerial <= 0) {
            throw new IllegalArgumentException("Mã Serial phải > 0");
        }
        return dao.layTheoIMEI(maSerial);
    }

    /**
     * Lấy danh sách bảo hành theo Hóa đơn
     */
    public List<BaoHanhDTO> getByHoaDon(int maHoaDon) {
        if (maHoaDon <= 0) {
            throw new IllegalArgumentException("Mã hóa đơn phải > 0");
        }
        return dao.layTheoHoaDon(maHoaDon);
    }

    /**
     * Tìm kiếm bảo hành theo từ khóa
     */
    public List<BaoHanhDTO> search(String keyword) {
        return dao.search(keyword);
    }

    // =========================================================================
    // THÊM / CẬP NHẬT / XÓA
    // =========================================================================

    /**
     * Thêm bảo hành mới
     */
    public int them(BaoHanhDTO bh) {
        validate(bh);
        
        // Kiểm tra ngày hẹn trả phải >= ngày hiện tại
        if (bh.getNgayHenTra() != null && bh.getNgayHenTra().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày hẹn trả không được trước ngày hiện tại");
        }

        int maBaoHanh = dao.them(bh);
        if (maBaoHanh <= 0) {
            throw new RuntimeException("Không thể thêm bảo hành");
        }
        return maBaoHanh;
    }

    /**
     * Cập nhật trạng thái bảo hành
     */
    public boolean updateStatus(int maBaoHanh, String trangThai) {
        if (maBaoHanh <= 0) {
            throw new IllegalArgumentException("Mã bảo hành phải > 0");
        }
        validateTrangThai(trangThai);
        
        BaoHanhDTO bh = dao.layTheoMa(maBaoHanh);
        if (bh == null) {
            throw new IllegalArgumentException("Bảo hành không tồn tại");
        }

        return dao.capNhatTrangThai(maBaoHanh, trangThai, null, null);
    }

    /**
     * Cập nhật trạng thái + Nhân viên xử lý + Kết quả
     */
    public boolean updateStatusFull(int maBaoHanh, String trangThai, Integer maNVXuLy, String ketQua) {
        if (maBaoHanh <= 0) {
            throw new IllegalArgumentException("Mã bảo hành phải > 0");
        }
        validateTrangThai(trangThai);
        
        if (maNVXuLy != null && maNVXuLy <= 0) {
            throw new IllegalArgumentException("Mã nhân viên xử lý phải > 0");
        }

        BaoHanhDTO bh = dao.layTheoMa(maBaoHanh);
        if (bh == null) {
            throw new IllegalArgumentException("Bảo hành không tồn tại");
        }

        return dao.capNhatTrangThai(maBaoHanh, trangThai, maNVXuLy, ketQua);
    }

    /**
     * Cập nhật trạng thái + Chi phí phát sinh
     */
    public boolean updateStatusWithCost(int maBaoHanh, String trangThai, Integer maNVXuLy,
                                        String ketQua, BigDecimal chiPhi) {
        if (maBaoHanh <= 0) {
            throw new IllegalArgumentException("Mã bảo hành phải > 0");
        }
        validateTrangThai(trangThai);

        BaoHanhDTO bh = dao.layTheoMa(maBaoHanh);
        if (bh == null) {
            throw new IllegalArgumentException("Bảo hành không tồn tại");
        }

        if (chiPhi != null && chiPhi.signum() < 0) {
            throw new IllegalArgumentException("Chi phí không được âm");
        }

        return dao.capNhatTrangThaiVaChiPhi(maBaoHanh, trangThai, maNVXuLy, ketQua, chiPhi);
    }

    /**
     * Xáo bảo hành
     */
    public boolean delete(int maBaoHanh) {
        if (maBaoHanh <= 0) {
            throw new IllegalArgumentException("Mã bảo hành phải > 0");
        }

        BaoHanhDTO bh = dao.layTheoMa(maBaoHanh);
        if (bh == null) {
            throw new IllegalArgumentException("Bảo hành không tồn tại");
        }

        // Chỉ cho phép xóa bảo hành ở trạng thái "DangXuLy"
        if (!"DangXuLy".equals(bh.getTrangThai())) {
            throw new IllegalArgumentException("Chỉ có thể xóa bảo hành đang xử lý");
        }

        return dao.xoa(maBaoHanh);
    }

    // =========================================================================
    // LOGIC KINH DOANH
    // =========================================================================

    /**
     * Chuyển bảo hành sang trạng thái "DaGuiHang"
     */
    public boolean guiHang(int maBaoHanh, Integer maNVXuLy, String trangThaiMoi) {
        BaoHanhDTO bh = dao.layTheoMa(maBaoHanh);
        if (bh == null) {
            throw new IllegalArgumentException("Bảo hành không tồn tại");
        }

        if (!"DangXuLy".equals(bh.getTrangThai())) {
            throw new IllegalArgumentException("Bảo hành phải ở trạng thái 'DangXuLy' để gửi hàng");
        }

        return dao.capNhatTrangThai(maBaoHanh, trangThaiMoi, maNVXuLy, "Đã gửi hàng");
    }

    /**
     * Hoàn thành bảo hành (trả khách)
     */
    public boolean traKhach(int maBaoHanh, Integer maNVXuLy, String ketQua, BigDecimal chiPhi) {
        BaoHanhDTO bh = dao.layTheoMa(maBaoHanh);
        if (bh == null) {
            throw new IllegalArgumentException("Bảo hành không tồn tại");
        }

        if ("DaTraKhach".equals(bh.getTrangThai())) {
            throw new IllegalArgumentException("Bảo hành đã được trả khách rồi");
        }

        return dao.capNhatTrangThaiVaChiPhi(maBaoHanh, "DaTraKhach", maNVXuLy, ketQua, chiPhi);
    }

    /**
     * Lấy tổng chi phí bảo hành
     */
    public BigDecimal getTongChiPhi() {
        List<BaoHanhDTO> list = dao.getAll();
        BigDecimal total = BigDecimal.ZERO;
        for (BaoHanhDTO bh : list) {
            if (bh.getChiPhiPhatSinh() != null) {
                total = total.add(bh.getChiPhiPhatSinh());
            }
        }
        return total;
    }

    /**
     * Lấy số lượng bảo hành đang xử lý
     */
    public int getCountDangXuLy() {
        return dao.layDangXuLy().size();
    }

    /**
     * Lấy số lượng bảo hành theo trạng thái
     */
    public int getCountByStatus(String trangThai) {
        validateTrangThai(trangThai);
        int count = 0;
        for (BaoHanhDTO bh : dao.getAll()) {
            if (trangThai.equals(bh.getTrangThai())) {
                count++;
            }
        }
        return count;
    }

    // =========================================================================
    // VALIDATION
    // =========================================================================

    private void validate(BaoHanhDTO bh) {
        if (bh == null) {
            throw new IllegalArgumentException("Bảo hành không được null");
        }

        if (bh.getMaSP() <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm phải > 0");
        }

        if (bh.getMaHoaDon() <= 0) {
            throw new IllegalArgumentException("Mã hóa đơn phải > 0");
        }

        if (bh.getMoTaLoi() == null || bh.getMoTaLoi().trim().isEmpty()) {
            throw new IllegalArgumentException("Mô tả lỗi không được rỗng");
        }

        if (bh.getMoTaLoi().length() > 500) {
            throw new IllegalArgumentException("Mô tả lỗi không được vượt 500 ký tự");
        }

        validateHinhThucXuLy(bh.getHinhThucXuLy());
    }

    private void validateTrangThai(String trangThai) {
        if (trangThai == null || trangThai.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái không được rỗng");
        }

        String[] validStatus = {"DangXuLy", "DaGuiHang", "ChoLinhKien", "DaTraKhach"};
        boolean valid = false;
        for (String status : validStatus) {
            if (status.equals(trangThai)) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ. Phải là: DangXuLy, DaGuiHang, ChoLinhKien, DaTraKhach");
        }
    }

    private void validateHinhThucXuLy(String hinhThuc) {
        if (hinhThuc == null || hinhThuc.trim().isEmpty()) {
            throw new IllegalArgumentException("Hình thức xử lý không được rỗng");
        }

        String[] validTypes = {"SuaChuaTaiCho", "GuiHang", "ThayTheMoi"};
        boolean valid = false;
        for (String type : validTypes) {
            if (type.equals(hinhThuc)) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            throw new IllegalArgumentException("Hình thức xử lý không hợp lệ. Phải là: SuaChuaTaiCho, GuiHang, ThayTheMoi");
        }
    }
}
