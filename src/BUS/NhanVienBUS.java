package BUS;

import DAO.NhanVienDAO;
import DTO.NhanVienDTO;

import java.util.List;
import java.util.stream.Collectors;

public class NhanVienBUS {

    private final NhanVienDAO dao = new NhanVienDAO();

    // =========================================================================
    // LẤY DỮ LIỆU
    // =========================================================================

    /** Lấy toàn bộ danh sách nhân viên */
    public List<NhanVienDTO> getAll() {
        return dao.getAll();
    }

    /**
     * Tìm kiếm nhân viên theo từ khóa (tên, mã, số điện thoại).
     * Lọc thêm theo vai trò và trạng thái nếu không phải "Tất cả".
     *
     * @param keyword      Từ khóa tìm kiếm (có thể rỗng)
     * @param vaiTro       Vai trò cần lọc, hoặc "Tất cả vai trò" để bỏ qua
     * @param trangThai    Trạng thái cần lọc, hoặc "Tất cả trạng thái" để bỏ qua
     * @return Danh sách nhân viên thỏa điều kiện
     */
    public List<NhanVienDTO> search(String keyword, String vaiTro, String trangThai) {
        List<NhanVienDTO> all = dao.getAll();
        if (all == null) return List.of();

        String kw = (keyword == null) ? "" : keyword.trim().toLowerCase();

        return all.stream()
                .filter(nv -> {
                    if (kw.isEmpty()) return true;
                    return (nv.getTenNV() != null && nv.getTenNV().toLowerCase().contains(kw))
                            || String.valueOf(nv.getMaNV()).contains(kw)
                            || (nv.getSoDienThoai() != null && nv.getSoDienThoai().contains(kw));
                })
                .filter(nv -> {
                    if (vaiTro == null || vaiTro.equals("Tất cả vai trò")) return true;
                    return formatVaiTro(nv.getVaiTro()).equals(vaiTro);
                })
                .filter(nv -> {
                    if (trangThai == null || trangThai.equals("Tất cả trạng thái")) return true;
                    return formatTrangThai(nv.getTrangThai()).equals(trangThai);
                })
                .collect(Collectors.toList());
    }


    // =========================================================================
    // BỔ SUNG 08/03/2026 18:08 — Tìm nhân viên theo mã
    // Dùng khi cần lấy TenNV để hiển thị (vd: danh sách đơn chờ)
    // Tìm trong cache getAll() — không query DB thêm
    // =========================================================================
    public NhanVienDTO timTheoMa(int maNV) {
        return dao.getAll().stream()
                .filter(nv -> nv.getMaNV() == maNV)
                .findFirst()
                .orElse(null);
    }

    // =========================================================================
    // THÊM / CẬP NHẬT / XÓA
    // =========================================================================

    /**
     * Thêm nhân viên mới sau khi kiểm tra nghiệp vụ.
     *
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public boolean insert(NhanVienDTO nv) {
        validate(nv);
        return dao.insert(nv);
    }

    /**
     * Cập nhật thông tin nhân viên sau khi kiểm tra nghiệp vụ.
     *
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public boolean update(NhanVienDTO nv) {
        validate(nv);
        return dao.update(nv);
    }

    /**
     * Xóa nhân viên theo mã.
     *
     * @throws IllegalArgumentException nếu mã nhân viên không hợp lệ
     */
    public boolean delete(int maNV) {
        if (maNV <= 0) throw new IllegalArgumentException("Mã nhân viên không hợp lệ!");
        return dao.delete(maNV);
    }

    // =========================================================================
    // VALIDATION – nghiệp vụ tập trung tại đây, GUI chỉ cần gọi
    // =========================================================================

    /**
     * Kiểm tra toàn bộ ràng buộc nghiệp vụ của một NhanVienDTO.
     * Ném {@link IllegalArgumentException} với thông báo cụ thể nếu có lỗi.
     */
    public void validate(NhanVienDTO nv) {
        if (nv == null)
            throw new IllegalArgumentException("Dữ liệu nhân viên không được để trống!");

        if (nv.getTenNV() == null || nv.getTenNV().trim().isEmpty())
            throw new IllegalArgumentException("Họ và Tên không được để trống!");

        if (nv.getTenNV().trim().length() > 100)
            throw new IllegalArgumentException("Họ và Tên không được vượt quá 100 ký tự!");

        String email = nv.getEmail();
        if (email != null && !email.isEmpty() && !isValidEmail(email))
            throw new IllegalArgumentException("Email không đúng định dạng!");

        String sdt = nv.getSoDienThoai();
        if (sdt != null && !sdt.isEmpty() && !isValidPhone(sdt))
            throw new IllegalArgumentException("Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0!");

        String cccd = nv.getCccd();
        if (cccd != null && !cccd.isEmpty() && !cccd.matches("\\d{12}"))
            throw new IllegalArgumentException("Số CCCD phải gồm đúng 12 chữ số!");

        if (nv.getNgaySinh() != null && nv.getNgayVaoLam() != null
                && nv.getNgaySinh().after(nv.getNgayVaoLam()))
            throw new IllegalArgumentException("Ngày sinh không được sau ngày vào làm!");
    }

    // =========================================================================
    // HELPERS – dùng chung GUI & BUS
    // =========================================================================

    public String formatVaiTro(String raw) {
        if (raw == null) return "";
        if (raw.equalsIgnoreCase("NhanVienBanHang")) return "Nhân viên bán hàng";
        if (raw.equalsIgnoreCase("QuanLy"))          return "Quản lý";
        return raw;
    }

    public String parseVaiTro(String display) {
        if (display == null) return "";
        if (display.equals("Nhân viên bán hàng")) return "NhanVienBanHang";
        if (display.equals("Quản lý"))            return "QuanLy";
        return display;
    }

    public String formatTrangThai(String raw) {
        if (raw == null) return "";
        if (raw.equalsIgnoreCase("DangLam") || raw.equals("1")) return "Đang làm việc";
        if (raw.equalsIgnoreCase("NghiViec") || raw.equals("0")) return "Đã nghỉ việc";
        return raw;
    }

    public String parseTrangThai(String display) {
        if (display == null) return "";
        if (display.equals("Đang làm việc")) return "DangLam";
        if (display.equals("Đã nghỉ việc"))  return "NghiViec";
        return display;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^0\\d{9}$");
    }
}