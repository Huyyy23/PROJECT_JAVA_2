package DTO;

import java.time.LocalDateTime;

public class LichSuBaoHanhDTO {

    private int           maLichSu;
    private int           maBaoHanh;
    private LocalDateTime thoiGian;
    private Integer       maNV;
    private String        tenNV;        // join từ NHANVIEN, không lưu DB
    private String        trangThaiCu;
    private String        trangThaiMoi;
    private String        ghiChu;

    public LichSuBaoHanhDTO() {}

    public LichSuBaoHanhDTO(int maBaoHanh, Integer maNV,
                             String trangThaiCu, String trangThaiMoi,
                             String ghiChu) {
        this.maBaoHanh    = maBaoHanh;
        this.maNV         = maNV;
        this.trangThaiCu  = trangThaiCu;
        this.trangThaiMoi = trangThaiMoi;
        this.ghiChu       = ghiChu;
    }

    public int           getMaLichSu()                          { return maLichSu; }
    public void          setMaLichSu(int maLichSu)              { this.maLichSu = maLichSu; }
    public int           getMaBaoHanh()                         { return maBaoHanh; }
    public void          setMaBaoHanh(int maBaoHanh)            { this.maBaoHanh = maBaoHanh; }
    public LocalDateTime getThoiGian()                          { return thoiGian; }
    public void          setThoiGian(LocalDateTime thoiGian)    { this.thoiGian = thoiGian; }
    public Integer       getMaNV()                              { return maNV; }
    public void          setMaNV(Integer maNV)                  { this.maNV = maNV; }
    public String        getTenNV()                             { return tenNV; }
    public void          setTenNV(String tenNV)                 { this.tenNV = tenNV; }
    public String        getTrangThaiCu()                       { return trangThaiCu; }
    public void          setTrangThaiCu(String trangThaiCu)     { this.trangThaiCu = trangThaiCu; }
    public String        getTrangThaiMoi()                      { return trangThaiMoi; }
    public void          setTrangThaiMoi(String trangThaiMoi)   { this.trangThaiMoi = trangThaiMoi; }
    public String        getGhiChu()                            { return ghiChu; }
    public void          setGhiChu(String ghiChu)               { this.ghiChu = ghiChu; }
}
