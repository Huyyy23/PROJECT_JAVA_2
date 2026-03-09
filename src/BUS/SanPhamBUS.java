package BUS;

import DAO.SanPhamDAO;
import DTO.SanPhamDTO;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * SanPhamBUS — Tầng nghiệp vụ cho module Sản phẩm.
 *
 * Vai trò:
 *   - Là cầu nối duy nhất giữa GUI và SanPhamDAO.
 *   - Duy trì cache dsSanPham trong bộ nhớ để tránh query DB lặp lại
 *     khi lọc/tìm kiếm phía client.
 *   - Thực hiện validate nghiệp vụ trước khi gọi DAO.
 *
 * Lưu ý cache:
 *   Cache chỉ được cập nhật khi gọi getDanhSachSanPham().
 *   Nếu cần dữ liệu mới nhất (sau thêm/sửa/xóa), hãy gọi lại getDanhSachSanPham().
 */
public class SanPhamBUS {

    private final SanPhamDAO spDAO = new SanPhamDAO();

    /**
     * Cache danh sách sản phẩm trong bộ nhớ.
     * Được cập nhật mỗi khi gọi getDanhSachSanPham().
     */
    private ArrayList<SanPhamDTO> dsSanPham = new ArrayList<>();

    // =========================================================================
    // LẤY DỮ LIỆU
    // =========================================================================

    /**
     * Load toàn bộ danh sách sản phẩm từ DB, đồng thời cập nhật cache.
     * Gọi method này mỗi khi cần dữ liệu mới nhất,
     * ví dụ: sau khi thêm/sửa/xóa, hoặc khi mở panel lần đầu.
     *
     * @return Danh sách sản phẩm đang bán (loại trừ NgungBan)
     */
    public ArrayList<SanPhamDTO> getDanhSachSanPham() {
        dsSanPham = spDAO.getDanhSachSanPham();
        return dsSanPham;
    }

    /**
     * Tìm sản phẩm theo maSP.
     * Ưu tiên tìm trong cache trước để tránh query DB không cần thiết.
     * Nếu cache chưa load hoặc SP không có trong cache → fallback query DB.
     *
     * Dùng trong NhapHangPanelGUI khi:
     *   - Người dùng nhập mã SP tay vào txtMaSP rồi blur / Enter (lookupSP)
     *   - loadViewData() cần lấy tên SP khi xem phiếu cũ
     *
     * @param maSP  Mã sản phẩm cần tìm
     * @return SanPhamDTO nếu tìm thấy, null nếu không tồn tại
     */
    public SanPhamDTO timTheoMa(int maSP) {
        // Tìm trong cache trước
        for (SanPhamDTO sp : dsSanPham) {
            if (sp.getMaSP() == maSP) return sp;
        }
        // Cache chưa load hoặc SP không có trong cache → query thẳng DB
        return spDAO.getById(maSP);
    }

    /**
     * Lấy danh sách sản phẩm thuộc một nhà cung cấp cụ thể.
     * Gọi thẳng DAO (không qua cache) vì cần JOIN NHACUNGCAP_SANPHAM.
     *
     * Dùng trong NhapHangPanelGUI — popup "Chọn sản phẩm":
     *   Chỉ hiện SP của NCC đang chọn trong cbNCC, tránh chọn nhầm SP của NCC khác.
     *
     * @param maNCC  Mã nhà cung cấp
     * @return Danh sách SanPhamDTO thuộc NCC đó (chỉ SP đang bán)
     */
    public ArrayList<SanPhamDTO> getSanPhamByNhaCungCap(int maNCC) {
        if (maNCC <= 0) return new ArrayList<>();
        return spDAO.getSanPhamByNhaCungCap(maNCC);
    }

    /**
     * Kiểm tra sản phẩm có thuộc nhà cung cấp đang chọn không.
     * Ném IllegalArgumentException với thông báo hiển thị ngay trên GUI
     * nếu SP không thuộc NCC đó.
     *
     * Được gọi tại 2 điểm trong NhapHangPanelGUI:
     *   1. lookupSP()  — khi nhập mã SP tay, sau khi blur / Enter
     *   2. themDong()  — ngay trước khi thêm vào chiTietList (hard-block cuối)
     *
     * @param maSP    Mã sản phẩm cần kiểm tra
     * @param maNCC   Mã nhà cung cấp đang chọn trong cbNCC
     * @param tenSP   Tên SP (để điền vào thông báo lỗi cho rõ)
     * @param tenNCC  Tên NCC (để điền vào thông báo lỗi cho rõ)
     * @throws IllegalArgumentException nếu SP không thuộc NCC này
     */
    public void kiemTraSPThuocNCC(int maSP, int maNCC, String tenSP, String tenNCC) {
        if (maSP <= 0 || maNCC <= 0)
            throw new IllegalArgumentException("Mã SP hoặc mã NCC không hợp lệ!");

        boolean thuocNCC = spDAO.kiemTraSPThuocNCC(maSP, maNCC);
        if (!thuocNCC) {
            throw new IllegalArgumentException(
                "Sản phẩm \"" + tenSP + "\" không thuộc nhà cung cấp \"" + tenNCC + "\"!\n"
                + "Vui lòng chọn đúng nhà cung cấp hoặc chọn lại sản phẩm."
            );
        }
    }

    // =========================================================================
    // VALIDATE NGHIỆP VỤ (dùng nội bộ)
    // =========================================================================

    /**
     * Kiểm tra toàn bộ ràng buộc nghiệp vụ của SanPhamDTO.
     * Được gọi bởi themSanPham() và suaSanPham() trước khi gọi DAO.
     *
     * @param sp  DTO cần kiểm tra
     * @return true nếu hợp lệ, false nếu có lỗi
     */
    private boolean kiemTraDuLieu(SanPhamDTO sp) {
        if (sp.getTenSP() == null || sp.getTenSP().trim().isEmpty())
            return false;
        if (sp.getGia() == null || sp.getGia().compareTo(BigDecimal.ZERO) <= 0)
            return false;
        if (sp.getGiaGoc() == null || sp.getGiaGoc().compareTo(BigDecimal.ZERO) <= 0)
            return false;
        if (sp.getSoLuongTon() < 0)
            return false;
        if (sp.getSoLuongToiThieu() < 0)
            return false;
        if (sp.getSoLuongToiDa() < sp.getSoLuongToiThieu())
            return false;
        return true;
    }

    // =========================================================================
    // THÊM / SỬA / XÓA
    // =========================================================================

    /**
     * Thêm sản phẩm mới.
     * Validate trước khi gọi DAO. Nếu thành công, cập nhật cache.
     *
     * @param sp  DTO sản phẩm cần thêm
     * @return true nếu thêm thành công
     */
    public boolean themSanPham(SanPhamDTO sp) {
        if (!kiemTraDuLieu(sp)) return false;
        boolean kq = spDAO.themSanPham(sp);
        if (kq) dsSanPham.add(sp);
        return kq;
    }

    /**
     * Cập nhật thông tin sản phẩm.
     * Validate trước khi gọi DAO. Nếu thành công, cập nhật cache.
     *
     * @param sp  DTO sản phẩm với thông tin mới (phải có MaSP hợp lệ)
     * @return true nếu sửa thành công
     */
    public boolean suaSanPham(SanPhamDTO sp) {
        if (!kiemTraDuLieu(sp)) return false;
        boolean kq = spDAO.suaSanPham(sp);
        if (kq) {
            for (int i = 0; i < dsSanPham.size(); i++) {
                if (dsSanPham.get(i).getMaSP() == sp.getMaSP()) {
                    dsSanPham.set(i, sp);
                    break;
                }
            }
        }
        return kq;
    }

    /**
     * Xóa mềm sản phẩm (đổi TrangThai = NgungBan).
     * Nếu thành công, xóa khỏi cache.
     *
     * @param maSP  Mã sản phẩm cần xóa
     * @return true nếu xóa thành công
     */
    public boolean xoaSanPham(int maSP) {
        boolean kq = spDAO.xoaSanPham(maSP);
        if (kq) {
            for (int i = 0; i < dsSanPham.size(); i++) {
                if (dsSanPham.get(i).getMaSP() == maSP) {
                    dsSanPham.remove(i);
                    break;
                }
            }
        }
        return kq;
    }

    // =========================================================================
    // TÌM KIẾM / LỌC
    // =========================================================================

    /**
     * Tìm sản phẩm theo tên (LIKE, query DB, không qua cache).
     *
     * @param ten  Chuỗi tìm kiếm
     * @return Danh sách SP khớp tên
     */
    public ArrayList<SanPhamDTO> timSanPham(String ten) {
        return spDAO.timSanPham(ten);
    }

    /**
     * Lọc theo mã loại sản phẩm (dùng cache, không query DB).
     *
     * @param maLoai  Mã loại cần lọc
     * @return Danh sách SP thuộc loại đó trong cache
     */
    public ArrayList<SanPhamDTO> locTheoLoai(int maLoai) {
        ArrayList<SanPhamDTO> ketQua = new ArrayList<>();
        for (SanPhamDTO sp : dsSanPham)
            if (sp.getMaLoai() == maLoai) ketQua.add(sp);
        return ketQua;
    }

    /**
     * Lọc theo thương hiệu (dùng cache, không query DB, không phân biệt hoa thường).
     *
     * @param thuongHieu  Tên thương hiệu cần lọc
     * @return Danh sách SP thuộc thương hiệu đó trong cache
     */
    public ArrayList<SanPhamDTO> locTheoThuongHieu(String thuongHieu) {
        ArrayList<SanPhamDTO> ketQua = new ArrayList<>();
        for (SanPhamDTO sp : dsSanPham)
            if (sp.getThuongHieu().equalsIgnoreCase(thuongHieu)) ketQua.add(sp);
        return ketQua;
    }

    /**
     * Lọc theo khoảng giá bán (dùng cache, không query DB).
     *
     * @param min  Giá tối thiểu (inclusive)
     * @param max  Giá tối đa (inclusive)
     * @return Danh sách SP trong khoảng giá trong cache
     */
    public ArrayList<SanPhamDTO> locTheoGia(BigDecimal min, BigDecimal max) {
        ArrayList<SanPhamDTO> ketQua = new ArrayList<>();
        for (SanPhamDTO sp : dsSanPham)
            if (sp.getGia().compareTo(min) >= 0 && sp.getGia().compareTo(max) <= 0)
                ketQua.add(sp);
        return ketQua;
    }

    // =========================================================================
    // HELPER ĐẶC BIỆT
    // =========================================================================

    /**
     * Lấy MaSP lớn nhất hiện tại (= SP vừa INSERT gần nhất).
     * Dùng sau themSanPham() để lấy MaSP mới sinh — phục vụ link NCC
     * vào bảng NHACUNGCAP_SANPHAM ngay sau đó.
     *
     * LƯU Ý: Có thể không chính xác nếu nhiều NV thêm SP cùng lúc.
     * Nên dùng OUTPUT INSERTED.MaSP trong themSanPham() nếu cần an toàn hơn.
     *
     * @return MaSP lớn nhất, hoặc -1 nếu lỗi
     */
    public int getMaSPMoiNhat() {
        return spDAO.getMaSPMoiNhat();
    }
}