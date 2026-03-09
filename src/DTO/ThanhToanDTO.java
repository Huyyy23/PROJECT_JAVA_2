package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ThanhToanDTO {
    private int           maThanhToan;
    private int           maHoaDon;
    private LocalDateTime ngayThanhToan;
    private BigDecimal    soTien;
    private String        phuongThuc; // TienMat|ChuyenKhoan|TheNganHang|TheTinDung|VNPAY|MoMo|ZaloPay
    private String        trangThai;  // ThanhCong | ThatBai | ChoXuLy
    private String        ghiChu;

    public ThanhToanDTO() {}

    public int           getMaThanhToan()                 { return maThanhToan; }
    public void          setMaThanhToan(int maThanhToan)            { this.maThanhToan = maThanhToan; }
    public int           getMaHoaDon()                    { return maHoaDon; }
    public void          setMaHoaDon(int maHoaDon)               { this.maHoaDon = maHoaDon; }
    public LocalDateTime getNgayThanhToan()               { return ngayThanhToan; }
    public void          setNgayThanhToan(LocalDateTime ngayThanhToan){ this.ngayThanhToan = ngayThanhToan; }
    public BigDecimal    getSoTien()                      { return soTien; }
    public void          setSoTien(BigDecimal soTien)          { this.soTien = soTien; }
    public String        getPhuongThuc()                  { return phuongThuc; }
    public void          setPhuongThuc(String phuongThuc)          { this.phuongThuc = phuongThuc; }
    public String        getTrangThai()                   { return trangThai; }
    public void          setTrangThai(String trangThai)           { this.trangThai = trangThai; }
    public String        getGhiChu()                      { return ghiChu; }
    public void          setGhiChu(String ghiChu)              { this.ghiChu = ghiChu; }

    @Override
    public String toString() {
        return String.format("[%d] HĐ:%d | %,.0fđ | %s | %s",
            maThanhToan, maHoaDon, soTien, phuongThuc, trangThai);
    }
}