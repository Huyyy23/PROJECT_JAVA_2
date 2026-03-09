package BUS;

import DAO.NhaCungCapDAO;
import DTO.NhaCungCapDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NhaCungCapBUS — Tầng nghiệp vụ cho module Nhà cung cấp.
 *
 * Vai trò:
 *   - Là cầu nối duy nhất giữa GUI và NhaCungCapDAO.
 *   - Thực hiện validate nghiệp vụ (tên, SĐT, email) trước khi gọi DAO.
 *   - Cung cấp các hàm tiện ích hiển thị (formatMa, formatTrangThai)
 *     để GUI không phải xử lý logic hiển thị.
 *
 * Quy ước trạng thái:
 *   - 1 = "Đang hợp tác" (HoatDong trong DB)
 *   - 2 = "Ngừng hợp tác" (NgungHoatDong trong DB)
 *   Việc chuyển đổi số ↔ chuỗi DB được xử lý hoàn toàn ở tầng DAO.
 */
public class NhaCungCapBUS {

    private final NhaCungCapDAO dao = new NhaCungCapDAO();

    // =========================================================================
    // LẤY DỮ LIỆU
    // =========================================================================

    /**
     * Lấy toàn bộ danh sách nhà cung cấp (cả hoạt động lẫn ngừng).
     * Dùng trong NhaCungCapPanel để hiển thị bảng quản lý đầy đủ.
     *
     * @return Danh sách tất cả NCC
     */
    public List<NhaCungCapDTO> getAll() {
        return dao.selectAll();
    }

    /**
     * Alias của getAll() trả về ArrayList — dùng trong NhapHangPanelGUI.loadData()
     * để tra tên NCC khi hiển thị bảng phiếu nhập.
     *
     * @return Danh sách tất cả NCC dạng ArrayList
     */
    public ArrayList<NhaCungCapDTO> getDanhSachTatCa() {
        return new ArrayList<>(dao.selectAll());
    }

    /**
     * Chỉ lấy NCC đang hoạt động (trangThai == 1).
     *
     * Dùng trong:
     *   - NhapHangPanelGUI: cbNCC chỉ hiện NCC đang hợp tác — tránh chọn NCC đã ngừng
     *   - SanPhamPanel: combobox chọn NCC khi thêm/sửa SP
     *
     * @return Danh sách NCC có trangThai == 1
     */
    public ArrayList<NhaCungCapDTO> getDanhSachHoatDong() {
        ArrayList<NhaCungCapDTO> result = new ArrayList<>();
        for (NhaCungCapDTO ncc : dao.selectAll()) {
            if (ncc.getTrangThai() == 1) result.add(ncc);
        }
        return result;
    }

    /**
     * Tìm kiếm nhà cung cấp theo từ khóa (mã định dạng, tên, SĐT).
     * Tìm không phân biệt hoa thường, từ khóa rỗng = trả về tất cả.
     *
     * @param keyword  Từ khóa tìm kiếm
     * @return Danh sách NCC thỏa điều kiện
     */
    public List<NhaCungCapDTO> search(String keyword) {
        List<NhaCungCapDTO> all = dao.selectAll();
        if (all == null) return List.of();

        String kw = (keyword == null) ? "" : keyword.trim().toLowerCase();
        if (kw.isEmpty()) return all;

        return all.stream()
                .filter(ncc -> {
                    String ma  = formatMa(ncc.getMaNCC()).toLowerCase();
                    String ten = ncc.getTenNCC() != null ? ncc.getTenNCC().toLowerCase() : "";
                    String sdt = ncc.getSDT()    != null ? ncc.getSDT()                  : "";
                    return ma.contains(kw) || ten.contains(kw) || sdt.contains(kw);
                })
                .collect(Collectors.toList());
    }

    // =========================================================================
    // THÊM / CẬP NHẬT / XÓA / ĐỔI TRẠNG THÁI
    // =========================================================================

    /**
     * Thêm nhà cung cấp mới.
     * Validate dữ liệu trước khi gọi DAO.
     *
     * @param ncc  DTO chứa thông tin NCC mới
     * @return true nếu thêm thành công
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public boolean insert(NhaCungCapDTO ncc) {
        validate(ncc);
        return dao.insert(ncc);
    }

    /**
     * Cập nhật thông tin nhà cung cấp (không đổi trạng thái).
     * Validate dữ liệu trước khi gọi DAO.
     *
     * @param ncc  DTO với thông tin mới (phải có MaNhaCungCap hợp lệ)
     * @return true nếu cập nhật thành công
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public boolean update(NhaCungCapDTO ncc) {
        validate(ncc);
        return dao.update(ncc);
    }

    /**
     * Đổi trạng thái nhà cung cấp.
     *
     * @param maNCC         Mã nhà cung cấp
     * @param trangThaiMoi  1 = Đang hợp tác,  2 = Ngừng hợp tác
     * @return true nếu cập nhật thành công
     * @throws IllegalArgumentException nếu mã hoặc trạng thái không hợp lệ
     */
    public boolean updateTrangThai(int maNCC, int trangThaiMoi) {
        if (maNCC <= 0)
            throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ!");
        if (trangThaiMoi != 1 && trangThaiMoi != 2)
            throw new IllegalArgumentException("Trạng thái không hợp lệ (chỉ nhận 1 hoặc 2)!");
        return dao.updateTrangThai(maNCC, trangThaiMoi);
    }

    /**
     * Toggle trạng thái (đổi ngược lại so với hiện tại).
     * Tiện dùng cho nút "Đổi trạng thái" trong GUI — không cần if/else ở GUI.
     * Nếu thành công, cập nhật luôn object trong bộ nhớ để GUI phản ánh ngay.
     *
     * @param ncc  DTO hiện tại (trangThai sẽ được cập nhật sau khi toggle)
     * @return true nếu toggle thành công
     */
    public boolean toggleTrangThai(NhaCungCapDTO ncc) {
        int moiHon = (ncc.getTrangThai() == 1) ? 2 : 1;
        boolean ok = dao.updateTrangThai(ncc.getMaNCC(), moiHon);
        if (ok) ncc.setTrangThai(moiHon); // cập nhật object trong bộ nhớ
        return ok;
    }

    /**
     * Xóa nhà cung cấp theo mã (DELETE thật).
     * Nếu NCC còn liên kết SP hoặc phiếu nhập, DB sẽ từ chối (FK).
     *
     * @param maNCC  Mã nhà cung cấp cần xóa
     * @return true nếu xóa thành công
     * @throws IllegalArgumentException nếu mã không hợp lệ
     */
    public boolean delete(int maNCC) {
        if (maNCC <= 0)
            throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ!");
        return dao.delete(maNCC);
    }

    // =========================================================================
    // BẢNG TRUNG GIAN NCC ↔ SP
    // =========================================================================

    /**
     * Lấy danh sách MaNCC đang liên kết với 1 sản phẩm.
     * Dùng trong SanPhamPanel khi sửa SP — hiển thị NCC đang cung cấp SP đó.
     *
     * @param maSP  Mã sản phẩm cần tra cứu
     * @return Danh sách MaNhaCungCap
     */
    public ArrayList<Integer> getDanhSachNccCuaSP(int maSP) {
        return dao.getNccByMaSP(maSP);
    }

    /**
     * Xóa toàn bộ liên kết NCC của 1 sản phẩm.
     * Gọi trước linkSanPham() khi cập nhật danh sách NCC của SP.
     *
     * @param maSP  Mã sản phẩm cần xóa liên kết
     * @return true nếu thành công
     */
    public boolean xoaLienKetSP(int maSP) {
        return dao.unlinkAllNccOfSP(maSP);
    }

    /**
     * Thêm 1 liên kết NCC ↔ SP vào bảng NHACUNGCAP_SANPHAM.
     * Nên gọi xoaLienKetSP() trước nếu muốn cập nhật lại toàn bộ.
     *
     * @param maNCC  Mã nhà cung cấp
     * @param maSP   Mã sản phẩm
     * @return true nếu link thành công
     */
    public boolean linkSanPham(int maNCC, int maSP) {
        return dao.linkSanPham(maNCC, maSP);
    }

    // =========================================================================
    // NGHIỆP VỤ — SINH MÃ MỚI
    // =========================================================================

    /**
     * Sinh mã NCC mới = max(maNCC hiện tại) + 1.
     * Trả về 1 nếu danh sách rỗng.
     * Dùng để hiển thị trước trong form thêm NCC (preview mã).
     *
     * @return Mã nguyên dương kế tiếp
     */
    public int generateNextId() {
        List<NhaCungCapDTO> all = dao.selectAll();
        if (all == null || all.isEmpty()) return 1;
        return all.stream().mapToInt(NhaCungCapDTO::getMaNCC).max().orElse(0) + 1;
    }

    // =========================================================================
    // VALIDATION
    // =========================================================================

    /**
     * Kiểm tra toàn bộ ràng buộc nghiệp vụ của NhaCungCapDTO.
     * Ném {@link IllegalArgumentException} với thông báo cụ thể nếu có lỗi.
     * Được gọi bởi insert() và update() trước khi gọi DAO.
     *
     * Ràng buộc:
     *   - Tên không được rỗng, tối đa 150 ký tự
     *   - SĐT (nếu có): đúng 10 chữ số, bắt đầu bằng 0
     *   - Email (nếu có): đúng định dạng chuẩn
     *
     * @param ncc  DTO cần kiểm tra
     * @throws IllegalArgumentException nếu có lỗi validate
     */
    public void validate(NhaCungCapDTO ncc) {
        if (ncc == null)
            throw new IllegalArgumentException("Dữ liệu nhà cung cấp không được để trống!");

        if (ncc.getTenNCC() == null || ncc.getTenNCC().trim().isEmpty())
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống!");

        if (ncc.getTenNCC().trim().length() > 150)
            throw new IllegalArgumentException("Tên nhà cung cấp không được vượt quá 150 ký tự!");

        String sdt = ncc.getSDT();
        if (sdt != null && !sdt.isEmpty() && !sdt.matches("^0\\d{9}$"))
            throw new IllegalArgumentException("Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0!");

        String email = ncc.getEmail();
        if (email != null && !email.isEmpty()
                && !email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"))
            throw new IllegalArgumentException("Email không đúng định dạng!");
    }

    // =========================================================================
    // HELPERS HIỂN THỊ (dùng chung GUI, không chứa logic nghiệp vụ)
    // =========================================================================

    /**
     * Định dạng mã hiển thị: 1 → "NCC0000001".
     * Dùng trong bảng danh sách NCC và tìm kiếm theo mã.
     *
     * @param id  Mã nguyên
     * @return Chuỗi định dạng NCC + 7 chữ số
     */
    public String formatMa(int id) {
        return String.format("NCC%07d", id);
    }

    /**
     * Chuyển số trạng thái → chuỗi hiển thị thân thiện.
     * 1 → "Đang hợp tác",  khác → "Ngừng hợp tác"
     *
     * @param trangThai  Số trạng thái (1 hoặc 2)
     * @return Chuỗi hiển thị
     */
    public String formatTrangThai(int trangThai) {
        return trangThai == 1 ? "Đang hợp tác" : "Ngừng hợp tác";
    }

    /**
     * Tạo thông báo xác nhận khi toggle trạng thái.
     * Dùng trong dialog confirm của GUI trước khi thực sự toggle.
     *
     * @param ncc  NCC hiện tại
     * @return Chuỗi câu hỏi xác nhận phù hợp
     */
    public String getToggleConfirmMessage(NhaCungCapDTO ncc) {
        return ncc.getTrangThai() == 1
                ? "Đổi sang Ngừng hợp tác?"
                : "Cho phép Đang hợp tác trở lại?";
    }
}