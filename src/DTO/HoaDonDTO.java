package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HoaDonDTO {

    private int maHoaDon;
    private Integer maKhachHang;      // nullable
    private int maNV;
    private LocalDateTime ngayLap;
    private BigDecimal tongTienHang;
    private BigDecimal phanTramGiamHang;
    // Computed columns (chỉ đọc từ DB, không INSERT)
    private BigDecimal tienGiamHang;
    private BigDecimal tienTruocVAT;
    private BigDecimal tienVAT;
    private BigDecimal tongThanhToan;
    private String ghiChu;
    private String trangThai;

    public HoaDonDTO() {}

    // Constructor dùng khi tạo mới hóa đơn (INSERT)
    public HoaDonDTO(Integer maKhachHang, int maNV, BigDecimal phanTramGiamHang, String ghiChu) {
        this.maKhachHang = maKhachHang;
        this.maNV = maNV;
        this.phanTramGiamHang = phanTramGiamHang != null ? phanTramGiamHang : BigDecimal.ZERO;
        this.ghiChu = ghiChu;
        this.trangThai = "HoanThanh";
    }

    // Constructor đầy đủ dùng khi đọc từ DB
    public HoaDonDTO(int maHoaDon, Integer maKhachHang, int maNV,
                     LocalDateTime ngayLap, BigDecimal tongTienHang,
                     BigDecimal phanTramGiamHang, BigDecimal tienGiamHang,
                     BigDecimal tienTruocVAT, BigDecimal tienVAT,
                     BigDecimal tongThanhToan, String ghiChu, String trangThai) {
        this.maHoaDon = maHoaDon;
        this.maKhachHang = maKhachHang;
        this.maNV = maNV;
        this.ngayLap = ngayLap;
        this.tongTienHang = tongTienHang;
        this.phanTramGiamHang = phanTramGiamHang;
        this.tienGiamHang = tienGiamHang;
        this.tienTruocVAT = tienTruocVAT;
        this.tienVAT = tienVAT;
        this.tongThanhToan = tongThanhToan;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public int getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(int maHoaDon) { this.maHoaDon = maHoaDon; }

    public Integer getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(Integer maKhachHang) { this.maKhachHang = maKhachHang; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }

    public BigDecimal getTongTienHang() { return tongTienHang; }
    public void setTongTienHang(BigDecimal tongTienHang) { this.tongTienHang = tongTienHang; }

    public BigDecimal getPhanTramGiamHang() { return phanTramGiamHang; }
    public void setPhanTramGiamHang(BigDecimal phanTramGiamHang) { this.phanTramGiamHang = phanTramGiamHang; }

    public BigDecimal getTienGiamHang() { return tienGiamHang; }
    public void setTienGiamHang(BigDecimal tienGiamHang) { this.tienGiamHang = tienGiamHang; }

    public BigDecimal getTienTruocVAT() { return tienTruocVAT; }
    public void setTienTruocVAT(BigDecimal tienTruocVAT) { this.tienTruocVAT = tienTruocVAT; }

    public BigDecimal getTienVAT() { return tienVAT; }
    public void setTienVAT(BigDecimal tienVAT) { this.tienVAT = tienVAT; }

    public BigDecimal getTongThanhToan() { return tongThanhToan; }
    public void setTongThanhToan(BigDecimal tongThanhToan) { this.tongThanhToan = tongThanhToan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "HoaDonDTO{maHoaDon=" + maHoaDon +
               ", maKhachHang=" + maKhachHang +
               ", maNV=" + maNV +
               ", ngayLap=" + ngayLap +
               ", tongThanhToan=" + tongThanhToan +
               ", trangThai='" + trangThai + "'}";
    }
}