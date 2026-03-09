package DTO;

public class TaiKhoanDTO {
    private int maNV;
    private String tenDangNhap;
    private String vaiTro; // Lấy từ bảng NHANVIEN
    private String tenNV;  // Lấy từ bảng NHANVIEN để hiển thị chào mừng

    public TaiKhoanDTO() {}

    // Getter và Setter
    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public String getTenNV() { return tenNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }
}