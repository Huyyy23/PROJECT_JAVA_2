package DTO;

import java.sql.Date;

public class KhachHangDTO {

    private int    maKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private Date   ngaySinh;
    private String gioiTinh;
    private int    diemTichLuy;
    private String hangKhachHang;
    private double phanTramGiam;
    private Date   ngayDangKy;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public KhachHangDTO() {}

    public KhachHangDTO(int maKhachHang, String tenKhachHang, String soDienThoai,
                        String email, String diaChi, Date ngaySinh, String gioiTinh,
                        int diemTichLuy, String hangKhachHang, double phanTramGiam,
                        Date ngayDangKy) {
        this.maKhachHang  = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai  = soDienThoai;
        this.email        = email;
        this.diaChi       = diaChi;
        this.ngaySinh     = ngaySinh;
        this.gioiTinh     = gioiTinh;
        this.diemTichLuy  = diemTichLuy;
        this.hangKhachHang = hangKhachHang;
        this.phanTramGiam  = phanTramGiam;
        this.ngayDangKy   = ngayDangKy;
    }

    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================
    public int getMaKhachHang()               { return maKhachHang; }
    public void setMaKhachHang(int v)         { this.maKhachHang = v; }

    public String getTenKhachHang()           { return tenKhachHang; }
    public void setTenKhachHang(String v)     { this.tenKhachHang = v; }

    public String getSoDienThoai()            { return soDienThoai; }
    public void setSoDienThoai(String v)      { this.soDienThoai = v; }

    public String getEmail()                  { return email; }
    public void setEmail(String v)            { this.email = v; }

    public String getDiaChi()                 { return diaChi; }
    public void setDiaChi(String v)           { this.diaChi = v; }

    public Date getNgaySinh()                 { return ngaySinh; }
    public void setNgaySinh(Date v)           { this.ngaySinh = v; }

    public String getGioiTinh()               { return gioiTinh; }
    public void setGioiTinh(String v)         { this.gioiTinh = v; }

    public int getDiemTichLuy()               { return diemTichLuy; }
    public void setDiemTichLuy(int v)         { this.diemTichLuy = v; }

    public String getHangKhachHang()          { return hangKhachHang; }
    public void setHangKhachHang(String v)    { this.hangKhachHang = v; }

    public double getPhanTramGiam()           { return phanTramGiam; }
    public void setPhanTramGiam(double v)     { this.phanTramGiam = v; }

    public Date getNgayDangKy()               { return ngayDangKy; }
    public void setNgayDangKy(Date v)         { this.ngayDangKy = v; }
}