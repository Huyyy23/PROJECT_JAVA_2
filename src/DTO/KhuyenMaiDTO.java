package DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public class KhuyenMaiDTO {
    private int        maKhuyenMai;
    private String     tenChuongTrinh;
    private String     maCode;
    private String     loaiGiam;             // PhanTram | SoTien
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriDonHangToiThieu;
    private BigDecimal giamToiDa;            // null = không giới hạn
    private LocalDate  ngayBatDau;
    private LocalDate  ngayKetThuc;
    private Integer    soLuongPhat;          // null = không giới hạn
    private int        soLuongDaDung;
    private String     trangThai;            // HoatDong | Tat | HetHan

    public KhuyenMaiDTO() {}

    public int        getMaKhuyenMai()                     { return maKhuyenMai; }
    public void       setMaKhuyenMai(int maKhuyenMai)      { this.maKhuyenMai = maKhuyenMai; }
    public String     getTenChuongTrinh()                  { return tenChuongTrinh; }
    public void       setTenChuongTrinh(String tenChuongTrinh) { this.tenChuongTrinh = tenChuongTrinh; }
    public String     getMaCode()                          { return maCode; }
    public void       setMaCode(String maCode)             { this.maCode = maCode; }
    public String     getLoaiGiam()                        { return loaiGiam; }
    public void       setLoaiGiam(String loaiGiam)                { this.loaiGiam = loaiGiam; }
    public BigDecimal getGiaTriGiam()                      { return giaTriGiam; }
    public void       setGiaTriGiam(BigDecimal giaTriGiam)          { this.giaTriGiam = giaTriGiam; }
    public BigDecimal getGiaTriDonHangToiThieu()           { return giaTriDonHangToiThieu; }
    public void       setGiaTriDonHangToiThieu(BigDecimal giaTriDonHangToiThieu){ this.giaTriDonHangToiThieu = giaTriDonHangToiThieu; }
    public BigDecimal getGiamToiDa()                       { return giamToiDa; }
    public void       setGiamToiDa(BigDecimal giamToiDa)           { this.giamToiDa = giamToiDa; }
    public LocalDate  getNgayBatDau()                      { return ngayBatDau; }
    public void       setNgayBatDau(LocalDate ngayBatDau)           { this.ngayBatDau = ngayBatDau; }
    public LocalDate  getNgayKetThuc()                     { return ngayKetThuc; }
    public void       setNgayKetThuc(LocalDate ngayKetThuc)          { this.ngayKetThuc = ngayKetThuc; }
    public Integer    getSoLuongPhat()                     { return soLuongPhat; }
    public void       setSoLuongPhat(Integer soLuongPhat)            { this.soLuongPhat = soLuongPhat; }
    public int        getSoLuongDaDung()                   { return soLuongDaDung; }
    public void       setSoLuongDaDung(int soLuongDaDung)              { this.soLuongDaDung = soLuongDaDung; }
    public String     getTrangThai()                       { return trangThai; }
    public void       setTrangThai(String trangThai)               { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | Giảm: %s",
            maKhuyenMai, tenChuongTrinh, maCode,
            "PhanTram".equals(loaiGiam) ? giaTriGiam + "%" : String.format("%,.0fđ", giaTriGiam));
    }
}
