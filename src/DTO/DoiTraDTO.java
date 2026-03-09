package DTO;


import java.time.LocalDate;

public class DoiTraDTO {
    private int       maDoiTra;
    private int       maHoaDon;
    private int       maSP;
    private Integer   maIMEI;     // null = phụ kiện
    private int       soLuongTra;
    private String    lyDo;
    private int       maNV;
    private LocalDate ngayYeuCau;
    private String    trangThai;  // DangXuLy | HoanThanh
    private String    ghiChu;    // ghi IMEI máy mới sau khi đổi

    public DoiTraDTO() {}

    public int       getMaDoiTra()              { return maDoiTra; }
    public void      setMaDoiTra(int maDoiTra)         { this.maDoiTra = maDoiTra; }
    public int       getMaHoaDon()              { return maHoaDon; }
    public void      setMaHoaDon(int maHoaDon)         { this.maHoaDon = maHoaDon; }
    public int       getMaSP()                  { return maSP; }
    public void      setMaSP(int maSP)             { this.maSP = maSP; }
    public Integer   getMaIMEI()                { return maIMEI; }
    public void      setMaIMEI(Integer maIMEI)       { this.maIMEI = maIMEI; }
    public int       getSoLuongTra()            { return soLuongTra; }
    public void      setSoLuongTra(int soLuongTra)       { this.soLuongTra = soLuongTra; }
    public String    getLyDo()                  { return lyDo; }
    public void      setLyDo(String lyDo)          { this.lyDo = lyDo; }
    public int       getMaNV()                  { return maNV; }
    public void      setMaNV(int maNV)             { this.maNV = maNV; }
    public LocalDate getNgayYeuCau()            { return ngayYeuCau; }
    public void      setNgayYeuCau(LocalDate ngayYeuCau) { this.ngayYeuCau = ngayYeuCau; }
    public String    getTrangThai()             { return trangThai; }
    public void      setTrangThai(String trangThai)     { this.trangThai = trangThai; }
    public String    getGhiChu()                { return ghiChu; }
    public void      setGhiChu(String ghiChu)        { this.ghiChu = ghiChu; }
}
