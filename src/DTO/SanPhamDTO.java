package DTO;

import java.math.BigDecimal;

public class SanPhamDTO {
    
    private int maSP;
    private String tenSP;
    private int maLoai;
    private String thuongHieu;
    private String mauSac;
    private BigDecimal gia;
    private BigDecimal giaGoc;
    private int soLuongTon;
    private int soLuongToiThieu;
    private int soLuongToiDa;
    private int thoiHanBaoHanhThang;
    private String moTa;
    private String trangThai;
    private String hinhAnh;

    public SanPhamDTO() {
    }

    public SanPhamDTO(int maSP, String tenSP, int maLoai, String thuongHieu, String mauSac,
                      BigDecimal gia, BigDecimal giaGoc, int soLuongTon, int soLuongToiThieu,
                      int soLuongToiDa, int thoiHanBaoHanhThang, String moTa, String trangThai,
                      String hinhAnh) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.maLoai = maLoai;
        this.thuongHieu = thuongHieu;
        this.mauSac = mauSac;
        this.gia = gia;
        this.giaGoc = giaGoc;
        this.soLuongTon = soLuongTon;
        this.soLuongToiThieu = soLuongToiThieu;
        this.soLuongToiDa = soLuongToiDa;
        this.thoiHanBaoHanhThang = thoiHanBaoHanhThang;
        this.moTa = moTa;
        this.trangThai = trangThai;
        this.hinhAnh = hinhAnh;
    }

    public int getMaSP() {
        return maSP;
    }

    public void setMaSP(int maSP) {
        this.maSP = maSP;
    }

    public String getTenSP() {
        return tenSP;
    }

    public void setTenSP(String tenSP) {
        this.tenSP = tenSP;
    }

    public int getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(int maLoai) {
        this.maLoai = maLoai;
    }

    public String getThuongHieu() {
        return thuongHieu;
    }

    public void setThuongHieu(String thuongHieu) {
        this.thuongHieu = thuongHieu;
    }

    public String getMauSac() {
        return mauSac;
    }

    public void setMauSac(String mauSac) {
        this.mauSac = mauSac;
    }

    public BigDecimal getGia() {
        return gia;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }

    public BigDecimal getGiaGoc() {
        return giaGoc;
    }

    public void setGiaGoc(BigDecimal giaGoc) {
        this.giaGoc = giaGoc;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public int getSoLuongToiThieu() {
        return soLuongToiThieu;
    }

    public void setSoLuongToiThieu(int soLuongToiThieu) {
        this.soLuongToiThieu = soLuongToiThieu;
    }

    public int getSoLuongToiDa() {
        return soLuongToiDa;
    }

    public void setSoLuongToiDa(int soLuongToiDa) {
        this.soLuongToiDa = soLuongToiDa;
    }

    public int getThoiHanBaoHanhThang() {
        return thoiHanBaoHanhThang;
    }

    public void setThoiHanBaoHanhThang(int thoiHanBaoHanhThang) {
        this.thoiHanBaoHanhThang = thoiHanBaoHanhThang;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
}