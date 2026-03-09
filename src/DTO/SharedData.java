package DTO;

/**
 * Lưu thông tin phiên làm việc của nhân viên đang đăng nhập.
 * Được set một lần khi login thành công, dùng xuyên suốt ứng dụng.
 */
public class SharedData {

    /** Mã nhân viên — dùng để insert vào DB (PHIEUNHAP.MaNV, HOADON.MaNV...) */
    public static int    currentMaNV  = 0;

    /** Tên nhân viên — dùng để hiển thị trên giao diện */
    public static String currentTenNV = null;

    /** Vai trò — dùng để phân quyền menu */
    public static String currentRole = null;

    // Ngăn khởi tạo
    private SharedData() {}

    /** Gọi khi đăng nhập thành công */
    public static void login(int maNV, String tenNV, String Role) {
        currentMaNV   = maNV;
        currentTenNV  = tenNV;
        currentRole = Role;
    }

    /** Gọi khi đăng xuất */
    public static void logout() {
        currentMaNV   = 0;
        currentTenNV  = null;
        currentRole = null;
    }
}