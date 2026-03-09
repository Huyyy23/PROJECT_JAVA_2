package BUS;

import DAO.KhachHangDAO;
import DTO.KhachHangDTO;
import java.util.List;
import java.util.stream.Collectors;

public class KhachHangBUS {

    private final KhachHangDAO dao = new KhachHangDAO();

    // =========================================================================
    // NGƯỠNG ĐIỂM – khớp với ảnh yêu cầu
    // =========================================================================
    private static final int NGUONG_KIM_CUONG = 2000;  // 12%
    private static final int NGUONG_VANG      = 1000;  //  8%
    private static final int NGUONG_BAC       =  400;  //  5%
    private static final int NGUONG_DONG      =  150;  //  2%
    // < 150 điểm → Vô hạng, 0%

    // Tên hạng lưu vào DB (không dấu để tránh encoding issues)
    public static final String HANG_KIM_CUONG = "KimCuong";
    public static final String HANG_VANG      = "Vang";
    public static final String HANG_BAC       = "Bac";
    public static final String HANG_DONG      = "Dong";
    public static final String HANG_VO_HANG   = "VoHang";

    // =========================================================================
    // LẤY DỮ LIỆU
    // =========================================================================

    /** Lấy toàn bộ danh sách khách hàng */
    public List<KhachHangDTO> getAll() {
        return dao.getAll();
    }

    /**
     * Tìm kiếm + lọc theo hạng.
     * @param keyword  Từ khóa (tên, SĐT, mã…) — null/rỗng = bỏ qua
     * @param hang     Hạng cần lọc (dùng hằng HANG_*) — null = bỏ qua
     */
    public List<KhachHangDTO> search(String keyword, String hang) {
        List<KhachHangDTO> all = dao.getAll();
        if (all == null) return List.of();

        String kw = (keyword == null) ? "" : keyword.trim().toLowerCase();

        return all.stream()
                .filter(kh -> {
                    if (kw.isEmpty()) return true;
                    return (kh.getTenKhachHang() != null && kh.getTenKhachHang().toLowerCase().contains(kw))
                            || String.valueOf(kh.getMaKhachHang()).contains(kw)
                            || (kh.getSoDienThoai() != null && kh.getSoDienThoai().contains(kw))
                            || (kh.getEmail() != null && kh.getEmail().toLowerCase().contains(kw));
                })
                .filter(kh -> {
                    if (hang == null || hang.isBlank()) return true;
                    // So sánh cả 2 chiều: DB không dấu ↔ UI có dấu
                    String dbHang = kh.getHangKhachHang();
                    if (dbHang == null) dbHang = HANG_VO_HANG;
                    return dbHang.equalsIgnoreCase(hang)
                        || normalizeHang(dbHang).equalsIgnoreCase(hang)
                        || normalizeHang(hang).equalsIgnoreCase(dbHang);
                })
                .collect(Collectors.toList());
    }


    // =========================================================================
    // BỔ SUNG 08/03/2026 18:08 — Tìm khách hàng theo mã
    // Dùng khi cần lấy TenKhachHang để hiển thị (vd: danh sách đơn chờ)
    // Tìm trong cache getAll() — không query DB thêm
    // =========================================================================
    public KhachHangDTO timTheoMa(int maKhachHang) {
        return dao.getAll().stream()
                .filter(kh -> kh.getMaKhachHang() == maKhachHang)
                .findFirst()
                .orElse(null);
    }

    // =========================================================================
    // THÊM / CẬP NHẬT / XÓA
    // =========================================================================

    /**
     * Thêm khách hàng mới.
     * Tự động tính hạng & % giảm từ điểm tích lũy trước khi lưu.
     */
    public boolean insert(KhachHangDTO kh) {
        applyHangVaGiam(kh);  // tính hạng & % giảm
        return dao.insert(kh);
    }

    /**
     * Cập nhật thông tin khách hàng.
     * Tự động tính lại hạng & % giảm.
     */
    public boolean update(KhachHangDTO kh) {
        applyHangVaGiam(kh);  // tính lại hạng & % giảm
        return dao.update(kh);
    }

    /**
     * Xóa khách hàng theo mã.
     */
    public boolean delete(int maKhachHang) {
        if (maKhachHang <= 0)
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ!");
        return dao.delete(maKhachHang);
    }

    // =========================================================================
    // NGHIỆP VỤ – HẠNG & % GIẢM
    // =========================================================================

    /**
     * Tính hạng & % giảm dựa trên điểm tích lũy.
     * Lưu tên hạng KHÔNG DẤU vào DTO để match DB.
     */
    public void applyHangVaGiam(KhachHangDTO kh) {
        int diem = kh.getDiemTichLuy();
        if (diem >= NGUONG_KIM_CUONG) {
            kh.setHangKhachHang(HANG_KIM_CUONG);
            kh.setPhanTramGiam(12.0);
        } else if (diem >= NGUONG_VANG) {
            kh.setHangKhachHang(HANG_VANG);
            kh.setPhanTramGiam(8.0);
        } else if (diem >= NGUONG_BAC) {
            kh.setHangKhachHang(HANG_BAC);
            kh.setPhanTramGiam(5.0);
        } else if (diem >= NGUONG_DONG) {
            kh.setHangKhachHang(HANG_DONG);
            kh.setPhanTramGiam(2.0);
        } else {
            kh.setHangKhachHang(HANG_VO_HANG);
            kh.setPhanTramGiam(0.0);
        }
    }

    /**
     * Cộng điểm tích lũy sau mỗi đơn hàng và cập nhật hạng.
     */
    public boolean congDiem(KhachHangDTO kh, int diemCong) {
        if (diemCong < 0) throw new IllegalArgumentException("Điểm cộng không được âm!");
        kh.setDiemTichLuy(kh.getDiemTichLuy() + diemCong);
        applyHangVaGiam(kh);
        return dao.update(kh);
    }

    // =========================================================================
    // VALIDATION
    // =========================================================================

    public void validate(KhachHangDTO kh) {
        if (kh == null)
            throw new IllegalArgumentException("Dữ liệu khách hàng không được để trống!");

        if (kh.getTenKhachHang() == null || kh.getTenKhachHang().trim().isEmpty())
            throw new IllegalArgumentException("Tên khách hàng không được để trống!");

        if (kh.getTenKhachHang().trim().length() > 100)
            throw new IllegalArgumentException("Tên khách hàng không được vượt quá 100 ký tự!");

        String sdt = kh.getSoDienThoai();
        if (sdt != null && !sdt.isEmpty() && !sdt.matches("^0\\d{9}$"))
            throw new IllegalArgumentException("Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0!");

        String email = kh.getEmail();
        if (email != null && !email.isEmpty()
                && !email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"))
            throw new IllegalArgumentException("Email không đúng định dạng!");

        if (kh.getDiemTichLuy() < 0)
            throw new IllegalArgumentException("Điểm tích lũy không được âm!");
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    /**
     * Chuyển tên hạng DB (không dấu) sang tên hiển thị (có dấu) và ngược lại.
     * Dùng chung cho cả BUS và Panel.
     */
    public static String normalizeHang(String raw) {
        if (raw == null || raw.isBlank()) return "VoHang";
        return switch (raw.trim()) {
            case "KimCuong", "Kim Cuong", "Kim Cương" -> "KimCuong";
            case "Vang",  "Vàng"                      -> "Vang";
            case "Bac",   "Bạc"                       -> "Bac";
            case "Dong",  "Đồng"                      -> "Dong";
            case "VoHang","Vo Hang","Vô hạng","Thuong","Thường" -> "VoHang";
            default -> raw;
        };
    }

    /** Tên hiển thị có dấu để show trên UI */
    public static String hangDisplayName(String dbHang) {
        if (dbHang == null) return "Vô hạng";
        return switch (normalizeHang(dbHang)) {
            case "KimCuong" -> "Kim Cương";
            case "Vang"     -> "Vàng";
            case "Bac"      -> "Bạc";
            case "Dong"     -> "Đồng";
            default         -> "Vô hạng";
        };
    }

    /** % giảm theo hạng */
    public double getPhanTramGiam(KhachHangDTO kh) {
        if (kh == null) return 0;
        return switch (normalizeHang(kh.getHangKhachHang())) {
            case "KimCuong" -> 12.0;
            case "Vang"     -> 8.0;
            case "Bac"      -> 5.0;
            case "Dong"     -> 2.0;
            default         -> 0.0;
        };
    }
}