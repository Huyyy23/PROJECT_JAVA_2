package DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO cho bảng PHIEUNHAP
 *
 * CHANGELOG:
 *   (không thay đổi)
 */
public class PhieuNhapDTO {

    private int        maPN;
    private int        maNhaCungCap;
    private int        maNV;
    private LocalDate  ngayNhap;
    private BigDecimal tongTien;
    private String     ghiChu;
    private String     trangThai;   // HoanThanh | Huy | ChoXuLy

    public PhieuNhapDTO() {}

    public PhieuNhapDTO(int maNhaCungCap, int maNV,
                        LocalDate ngayNhap, BigDecimal tongTien,
                        String ghiChu, String trangThai) {
        this.maNhaCungCap = maNhaCungCap;
        this.maNV         = maNV;
        this.ngayNhap     = ngayNhap;
        this.tongTien     = tongTien;
        this.ghiChu       = ghiChu;
        this.trangThai    = trangThai;
    }

    public int getMaPN()                               { return maPN; }
    public void setMaPN(int maPN)                      { this.maPN = maPN; }

    public int getMaNhaCungCap()                       { return maNhaCungCap; }
    public void setMaNhaCungCap(int maNhaCungCap)      { this.maNhaCungCap = maNhaCungCap; }

    public int getMaNV()                               { return maNV; }
    public void setMaNV(int maNV)                      { this.maNV = maNV; }

    public LocalDate getNgayNhap()                     { return ngayNhap; }
    public void setNgayNhap(LocalDate ngayNhap)        { this.ngayNhap = ngayNhap; }

    public BigDecimal getTongTien()                    { return tongTien; }
    public void setTongTien(BigDecimal tongTien)       { this.tongTien = tongTien; }

    public String getGhiChu()                          { return ghiChu; }
    public void setGhiChu(String ghiChu)               { this.ghiChu = ghiChu; }

    public String getTrangThai()                       { return trangThai; }
    public void setTrangThai(String trangThai)         { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "PhieuNhapDTO{maPN=" + maPN +
               ", maNhaCungCap=" + maNhaCungCap +
               ", maNV=" + maNV +
               ", ngayNhap=" + ngayNhap +
               ", tongTien=" + tongTien +
               ", trangThai='" + trangThai + "'}";
    }
}
