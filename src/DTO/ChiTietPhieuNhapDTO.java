package DTO;

import java.math.BigDecimal;

/**
 * DTO cho bảng CHITIETPHIEUNHAP
 * ThanhTien là computed column (SoLuong * DonGiaNhap), chỉ đọc
 */
public class ChiTietPhieuNhapDTO {

    private int        maChiTietPN;
    private int        maPN;
    private int        maSP;
    private int        soLuong;
    private BigDecimal donGiaNhap;
    private BigDecimal thanhTien;   // computed, chỉ đọc từ DB
    private String     ghiChu;

    // ----------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------
    public ChiTietPhieuNhapDTO() {}

    public ChiTietPhieuNhapDTO(int maPN, int maSP,
                                int soLuong, BigDecimal donGiaNhap,
                                String ghiChu) {
        this.maPN       = maPN;
        this.maSP       = maSP;
        this.soLuong    = soLuong;
        this.donGiaNhap = donGiaNhap;
        this.ghiChu     = ghiChu;
    }

    // ----------------------------------------------------------------
    // Getter / Setter
    // ----------------------------------------------------------------
    public int getMaChiTietPN()                        { return maChiTietPN; }
    public void setMaChiTietPN(int maChiTietPN)        { this.maChiTietPN = maChiTietPN; }

    public int getMaPN()                  { return maPN; }
    public void setMaPN(int maPN)         { this.maPN = maPN; }

    public int getMaSP()                  { return maSP; }
    public void setMaSP(int maSP)         { this.maSP = maSP; }

    public int getSoLuong()               { return soLuong; }
    public void setSoLuong(int soLuong)   { this.soLuong = soLuong; }

    public BigDecimal getDonGiaNhap()                    { return donGiaNhap; }
    public void setDonGiaNhap(BigDecimal donGiaNhap)     { this.donGiaNhap = donGiaNhap; }

    public BigDecimal getThanhTien()                     { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien)       { this.thanhTien = thanhTien; }

    public String getGhiChu()                { return ghiChu; }
    public void setGhiChu(String ghiChu)     { this.ghiChu = ghiChu; }

    // ----------------------------------------------------------------
    // toString
    // ----------------------------------------------------------------
    @Override
    public String toString() {
        return "ChiTietPhieuNhapDTO{" +
               "maChiTietPN=" + maChiTietPN +
               ", maPN=" + maPN +
               ", maSP=" + maSP +
               ", soLuong=" + soLuong +
               ", donGiaNhap=" + donGiaNhap +
               ", thanhTien=" + thanhTien +
               '}';
    }
}