package BUS;

import DAO.DoiTraDAO;
import DTO.DoiTraDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Business Logic Layer cho nghiệp vụ Đổi/Trả hàng (DOITRA).
 *
 * Trách nhiệm:
 *  1. Validate dữ liệu đầu vào trước khi gọi DAO.
 *  2. Áp dụng các quy tắc nghiệp vụ (ví dụ: không được xoá phiếu đã duyệt).
 *  3. Là cầu nối duy nhất giữa GUI và DAO – GUI không gọi DAO trực tiếp.
 */
public class DoiTraBUS {

    private final DoiTraDAO dao = new DoiTraDAO();

    // ════════════════════════════════════════════════════════════════════════
    //  CREATE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Thêm mới phiếu đổi/trả.
     * @return MaDoiTra được sinh ra, hoặc -1 nếu thất bại.
     * @throws IllegalArgumentException khi dữ liệu không hợp lệ.
     */
    public int themPhieuDoiTra(DoiTraDTO dto) {
        validate(dto);

        // Mặc định trạng thái ban đầu là ChoDuyet
        if (dto.getTrangThai() == null || dto.getTrangThai().isBlank()) {
            dto.setTrangThai(DoiTraDTO.TRANG_THAI_CHO_DUYET);
        }
        // Ngày yêu cầu mặc định hôm nay
        if (dto.getNgayYeuCau() == null) {
            dto.setNgayYeuCau(LocalDate.now());
        }
        // Nếu là TraHang / BaoHanh thì không cần SP mới
        if (DoiTraDTO.LOAI_TRA_HANG.equals(dto.getLoaiDoiTra())
                || DoiTraDTO.LOAI_BAO_HANH.equals(dto.getLoaiDoiTra())) {
            dto.setMaSPMoi(null);
            dto.setMaSerialMoi(null);
        }
        return dao.insert(dto);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  READ
    // ════════════════════════════════════════════════════════════════════════

    public List<DoiTraDTO> layTatCa() {
        return dao.findAll();
    }

    public List<DoiTraDTO> layTheoTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank())
            throw new IllegalArgumentException("TrangThai không được để trống");
        return dao.findByTrangThai(trangThai);
    }

    public List<DoiTraDTO> layTheoLoai(String loaiDoiTra) {
        if (loaiDoiTra == null || loaiDoiTra.isBlank())
            throw new IllegalArgumentException("LoaiDoiTra không được để trống");
        return dao.findByLoai(loaiDoiTra);
    }

    public List<DoiTraDTO> layTheoKhoangNgay(LocalDate tuNgay, LocalDate denNgay) {
        validateDateRange(tuNgay, denNgay);
        return dao.findByNgayYeuCau(tuNgay, denNgay);
    }

    public List<DoiTraDTO> layTheoTrangThaiVaNgay(String trangThai,
                                                    LocalDate tuNgay,
                                                    LocalDate denNgay) {
        validateDateRange(tuNgay, denNgay);
        return dao.findByTrangThaiAndNgay(trangThai, tuNgay, denNgay);
    }

    public List<DoiTraDTO> layTheoHoaDon(int maHoaDon) {
        if (maHoaDon <= 0)
            throw new IllegalArgumentException("MaHoaDon không hợp lệ");
        return dao.findByHoaDon(maHoaDon);
    }

    public List<DoiTraDTO> layTheoNhanVien(int maNV) {
        if (maNV <= 0)
            throw new IllegalArgumentException("MaNV không hợp lệ");
        return dao.findByNhanVien(maNV);
    }

    public DoiTraDTO layTheoMa(int maDoiTra) {
        if (maDoiTra <= 0)
            throw new IllegalArgumentException("MaDoiTra không hợp lệ");
        return dao.findById(maDoiTra);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UPDATE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Cập nhật toàn bộ phiếu (chỉ cho phép khi đang ở trạng thái ChoDuyet).
     */
    public boolean capNhatPhieu(DoiTraDTO dto) {
        validate(dto);
        DoiTraDTO existing = dao.findById(dto.getMaDoiTra());
        if (existing == null)
            throw new IllegalStateException("Không tìm thấy phiếu đổi/trả #" + dto.getMaDoiTra());
        if (!DoiTraDTO.TRANG_THAI_CHO_DUYET.equals(existing.getTrangThai()))
            throw new IllegalStateException(
                    "Chỉ có thể chỉnh sửa phiếu ở trạng thái 'ChoDuyet'. "
                    + "Trạng thái hiện tại: " + existing.getTrangThai());
        return dao.update(dto);
    }

    /**
     * Duyệt phiếu – chuyển trạng thái từ ChoDuyet/DangXuLy → HoanThanh.
     */
    public boolean duyetPhieu(int maDoiTra, int maNVDuyet, String ghiChu) {
        DoiTraDTO existing = requireExists(maDoiTra);
        if (DoiTraDTO.TRANG_THAI_HOAN_THANH.equals(existing.getTrangThai()))
            throw new IllegalStateException("Phiếu đã được xử lý hoàn thành trước đó.");
        if (DoiTraDTO.TRANG_THAI_TU_CHOI.equals(existing.getTrangThai()))
            throw new IllegalStateException("Phiếu đã bị từ chối, không thể duyệt.");
        return dao.updateTrangThai(maDoiTra,
                DoiTraDTO.TRANG_THAI_HOAN_THANH,
                LocalDate.now(),
                ghiChu);
    }

    /**
     * Chuyển phiếu sang trạng thái DangXuLy.
     */
    public boolean batDauXuLy(int maDoiTra, String ghiChu) {
        DoiTraDTO existing = requireExists(maDoiTra);
        if (!DoiTraDTO.TRANG_THAI_CHO_DUYET.equals(existing.getTrangThai()))
            throw new IllegalStateException(
                    "Chỉ có thể bắt đầu xử lý phiếu ở trạng thái 'ChoDuyet'.");
        return dao.updateTrangThai(maDoiTra,
                DoiTraDTO.TRANG_THAI_DANG_XU_LY,
                null, ghiChu);
    }

    /**
     * Từ chối phiếu (ChoDuyet / DangXuLy → TuChoi).
     */
    public boolean tuChoiPhieu(int maDoiTra, String lyDoTuChoi) {
        if (lyDoTuChoi == null || lyDoTuChoi.isBlank())
            throw new IllegalArgumentException("Phải nhập lý do từ chối.");
        DoiTraDTO existing = requireExists(maDoiTra);
        if (DoiTraDTO.TRANG_THAI_HOAN_THANH.equals(existing.getTrangThai()))
            throw new IllegalStateException("Phiếu đã hoàn thành, không thể từ chối.");
        return dao.updateTrangThai(maDoiTra,
                DoiTraDTO.TRANG_THAI_TU_CHOI,
                LocalDate.now(),
                lyDoTuChoi);
    }

    /**
     * Xử lý đổi sản phẩm: cập nhật sản phẩm mới + serial mới + tiền chênh lệch,
     * sau đó chốt trạng thái HoanThanh.
     */
    public boolean xuLyDoiSanPham(int maDoiTra,
                                  int maSPMoi,
                                  int maSerialMoi,
                                  BigDecimal tienChenhLech,
                                  String ghiChu) {
        if (maSPMoi <= 0 || maSerialMoi <= 0)
            throw new IllegalArgumentException("Sản phẩm mới và serial mới không hợp lệ.");

        DoiTraDTO existing = requireExists(maDoiTra);
        if (!DoiTraDTO.LOAI_DOI_SAN_PHAM.equals(existing.getLoaiDoiTra()))
            throw new IllegalStateException("Phiếu này không phải loại đổi sản phẩm.");
        if (DoiTraDTO.TRANG_THAI_HOAN_THANH.equals(existing.getTrangThai())
                || DoiTraDTO.TRANG_THAI_TU_CHOI.equals(existing.getTrangThai()))
            throw new IllegalStateException("Phiếu đã kết thúc xử lý.");

        existing.setMaSPMoi(maSPMoi);
        existing.setMaSerialMoi(maSerialMoi);
        existing.setTienChenhLech(tienChenhLech != null ? tienChenhLech : BigDecimal.ZERO);
        existing.setNgayXuLy(LocalDate.now());
        existing.setTrangThai(DoiTraDTO.TRANG_THAI_HOAN_THANH);
        existing.setGhiChu(ghiChu);
        return dao.update(existing);
    }

    /**
     * Xác nhận hoàn tiền cho yêu cầu trả hàng.
     */
    public boolean xacNhanHoanTien(int maDoiTra, String ghiChu) {
        DoiTraDTO existing = requireExists(maDoiTra);
        if (!DoiTraDTO.LOAI_TRA_HANG.equals(existing.getLoaiDoiTra()))
            throw new IllegalStateException("Phiếu này không phải loại trả hàng.");
        if (DoiTraDTO.TRANG_THAI_HOAN_THANH.equals(existing.getTrangThai())
                || DoiTraDTO.TRANG_THAI_TU_CHOI.equals(existing.getTrangThai()))
            throw new IllegalStateException("Phiếu đã kết thúc xử lý.");

        return dao.updateTrangThai(
                maDoiTra,
                DoiTraDTO.TRANG_THAI_HOAN_THANH,
                LocalDate.now(),
                ghiChu
        );
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DELETE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Xoá phiếu (chỉ khi TrangThai = ChoDuyet).
     */
    public boolean xoaPhieu(int maDoiTra) {
        requireExists(maDoiTra);
        boolean ok = dao.delete(maDoiTra);
        if (!ok)
            throw new IllegalStateException(
                    "Không thể xoá phiếu #" + maDoiTra
                    + ". Chỉ được xoá phiếu đang ở trạng thái 'ChoDuyet'.");
        return true;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  THỐNG KÊ – dùng trong ThongKeGUI
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Thống kê số phiếu và tổng tiền chênh lệch theo trạng thái.
     * @return List of Object[]{String trangThai, int soPhieu, BigDecimal tongTienChenhLech}
     */
    public List<Object[]> thongKeTheoTrangThai(LocalDate tuNgay, LocalDate denNgay) {
        validateDateRange(tuNgay, denNgay);
        return dao.thongKeTheoTrangThai(tuNgay, denNgay);
    }

    /**
     * Thống kê số phiếu và tổng tiền chênh lệch theo loại đổi trả.
     * @return List of Object[]{String loaiDoiTra, int soPhieu, BigDecimal tongTienChenhLech}
     */
    public List<Object[]> thongKeTheoLoai(LocalDate tuNgay, LocalDate denNgay) {
        validateDateRange(tuNgay, denNgay);
        return dao.thongKeTheoLoai(tuNgay, denNgay);
    }

    /**
     * Top sản phẩm bị đổi/trả nhiều nhất.
     * @param limit số lượng kết quả tối đa (0 = tất cả)
     * @return List of Object[]{int maSP, String tenSP, int soPhieu, int tongSoLuongTra}
     */
    public List<Object[]> thongKeSanPhamDoiTraNhieu(LocalDate tuNgay,
                                                     LocalDate denNgay,
                                                     int limit) {
        validateDateRange(tuNgay, denNgay);
        return dao.thongKeSanPhamDoiTraNhieu(tuNgay, denNgay, limit);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  VALIDATION HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private void validate(DoiTraDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("Dữ liệu phiếu đổi/trả không được null.");
        if (dto.getMaHoaDon() <= 0)
            throw new IllegalArgumentException("MaHoaDon không hợp lệ.");
        if (dto.getMaSP() <= 0)
            throw new IllegalArgumentException("MaSP không hợp lệ.");
        if (dto.getMaSerial() <= 0)
            throw new IllegalArgumentException("MaSerial không hợp lệ.");
        if (dto.getSoLuongTra() <= 0)
            throw new IllegalArgumentException("SoLuongTra phải lớn hơn 0.");
        if (dto.getLoaiDoiTra() == null
                || (!dto.getLoaiDoiTra().equals(DoiTraDTO.LOAI_DOI_SAN_PHAM)
                    && !dto.getLoaiDoiTra().equals(DoiTraDTO.LOAI_TRA_HANG)
                    && !dto.getLoaiDoiTra().equals(DoiTraDTO.LOAI_BAO_HANH))) {
            throw new IllegalArgumentException(
                    "LoaiDoiTra phải là: DoiSanPham | TraHang | BaoHanh.");
        }
        // Đổi sản phẩm thì bắt buộc có SP mới
        if (DoiTraDTO.LOAI_DOI_SAN_PHAM.equals(dto.getLoaiDoiTra())) {
            if (dto.getMaSPMoi() == null || dto.getMaSPMoi() <= 0)
                throw new IllegalArgumentException(
                        "LoaiDoiTra = 'DoiSanPham' yêu cầu phải chọn sản phẩm mới (MaSPMoi).");
            if (dto.getMaSerialMoi() == null || dto.getMaSerialMoi() <= 0)
                throw new IllegalArgumentException(
                        "LoaiDoiTra = 'DoiSanPham' yêu cầu phải chọn serial mới (MaSerialMoi).");
        }
        if (dto.getMaNV() <= 0)
            throw new IllegalArgumentException("MaNV không hợp lệ.");
        if (dto.getTienChenhLech() != null
                && dto.getTienChenhLech().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("TienChenhLech không được âm.");
    }

    private void validateDateRange(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null)
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được null.");
        if (tuNgay.isAfter(denNgay))
            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được lớn hơn ngày kết thúc.");
    }

    private DoiTraDTO requireExists(int maDoiTra) {
        DoiTraDTO dto = dao.findById(maDoiTra);
        if (dto == null)
            throw new IllegalStateException("Không tìm thấy phiếu đổi/trả #" + maDoiTra);
        return dto;
    }
}