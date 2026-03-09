package DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BaoHanhDTO {
    private int        maBaoHanh;
    private Integer    maIMEI;         // null = phụ kiện
    private int        maSP;
    private int        maHoaDon;
    private Integer    maNVTiepNhan;
    private Integer    maNVXuLy;
    private LocalDate  ngayTiepNhan;
    private LocalDate  ngayHenTra;
    private LocalDate  ngayTra;
    private String     moTaLoi;
    private String     hinhThucXuLy;  // SuaChuaTaiCho | GuiHang | ThayTheMoi
    private String     ketQuaXuLy;
    private BigDecimal chiPhiPhatSinh;
    private String     trangThai;     // DangXuLy | DaGuiHang | ChoLinhKien | DaTraKhach

    public BaoHanhDTO() {}

    public int        getMaBaoHanh()                { return maBaoHanh; }
    public void       setMaBaoHanh(int maBaoHanh)           { this.maBaoHanh = maBaoHanh; }
    public Integer    getMaIMEI()                   { return maIMEI; }
    public void       setMaIMEI(Integer maIMEI)          { this.maIMEI = maIMEI; }
    public int        getMaSP()                     { return maSP; }
    public void       setMaSP(int maSP)                { this.maSP = maSP; }
    public int        getMaHoaDon()                 { return maHoaDon; }
    public void       setMaHoaDon(int maHoaDon)            { this.maHoaDon = maHoaDon; }
    public Integer    getMaNVTiepNhan()             { return maNVTiepNhan; }
    public void       setMaNVTiepNhan(Integer maNVTiepNhan)    { this.maNVTiepNhan = maNVTiepNhan; }
    public Integer    getMaNVXuLy()                 { return maNVXuLy; }
    public void       setMaNVXuLy(Integer maNVXuLy)        { this.maNVXuLy = maNVXuLy; }
    public LocalDate  getNgayTiepNhan()             { return ngayTiepNhan; }
    public void       setNgayTiepNhan(LocalDate ngayTiepNhan)  { this.ngayTiepNhan = ngayTiepNhan; }
    public LocalDate  getNgayHenTra()               { return ngayHenTra; }
    public void       setNgayHenTra(LocalDate ngayHenTra)    { this.ngayHenTra = ngayHenTra; }
    public LocalDate  getNgayTra()                  { return ngayTra; }
    public void       setNgayTra(LocalDate ngayTra)       { this.ngayTra = ngayTra; }
    public String     getMoTaLoi()                  { return moTaLoi; }
    public void       setMoTaLoi(String moTaLoi)          { this.moTaLoi = moTaLoi; }
    public String     getHinhThucXuLy()             { return hinhThucXuLy; }
    public void       setHinhThucXuLy(String hinhThucXuLy)     { this.hinhThucXuLy = hinhThucXuLy; }
    public String     getKetQuaXuLy()               { return ketQuaXuLy; }
    public void       setKetQuaXuLy(String ketQuaXuLy)       { this.ketQuaXuLy = ketQuaXuLy; }
    public BigDecimal getChiPhiPhatSinh()           { return chiPhiPhatSinh; }
    public void       setChiPhiPhatSinh(BigDecimal chiPhiPhatSinh){ this.chiPhiPhatSinh = chiPhiPhatSinh; }
    public String     getTrangThai()                { return trangThai; }
    public void       setTrangThai(String trangThai)        { this.trangThai = trangThai; }
}
