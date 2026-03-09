package DTO;

import java.math.BigDecimal;

public class ChiTietHoaDonDTO {

    private int maChiTiet;
    private int maHoaDon;
    private int maSP;
    private int maSerial;       // KHÔNG NULL trong DB — bắt buộc phải có
    private int soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien; // Computed column, chỉ đọc
    // BỔ SUNG 09/03/2026 — field tạm để hiển thị, không INSERT vào DB
    private String tenSP;

    public ChiTietHoaDonDTO() {}

    // Constructor dùng khi INSERT
    public ChiTietHoaDonDTO(int maHoaDon, int maSP, int maSerial,
                             int soLuong, BigDecimal donGia) {
        this.maHoaDon = maHoaDon;
        this.maSP = maSP;
        this.maSerial = maSerial;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    // Constructor đầy đủ dùng khi đọc từ DB
    public ChiTietHoaDonDTO(int maChiTiet, int maHoaDon, int maSP,
                             int maSerial, int soLuong,
                             BigDecimal donGia, BigDecimal thanhTien) {
        this.maChiTiet = maChiTiet;
        this.maHoaDon = maHoaDon;
        this.maSP = maSP;
        this.maSerial = maSerial;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public int getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(int maChiTiet) { this.maChiTiet = maChiTiet; }

    public int getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(int maHoaDon) { this.maHoaDon = maHoaDon; }

    public int getMaSP() { return maSP; }
    public void setMaSP(int maSP) { this.maSP = maSP; }

    public int getMaSerial() { return maSerial; }
    public void setMaSerial(int maSerial) { this.maSerial = maSerial; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }

    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }
    @Override
    public String toString() {
        return "ChiTietHoaDonDTO{maChiTiet=" + maChiTiet +
               ", maHoaDon=" + maHoaDon +
               ", maSP=" + maSP +
               ", maSerial=" + maSerial +
               ", soLuong=" + soLuong +
               ", donGia=" + donGia +
               ", thanhTien=" + thanhTien + "}";
    }
}