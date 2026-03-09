package DTO;

public class LoaiSanPhamDTO {
    private int    maLoai;
    private String tenLoai;
    private String moTa;

    public LoaiSanPhamDTO() {}

    public LoaiSanPhamDTO(int maLoai, String tenLoai, String moTa) {
        this.maLoai  = maLoai;
        this.tenLoai = tenLoai;
        this.moTa    = moTa;
    }

    public int    getMaLoai()  { return maLoai; }
    public String getTenLoai() { return tenLoai; }
    public String getMoTa()    { return moTa; }

    public void setMaLoai(int maLoai)      { this.maLoai  = maLoai; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }
    public void setMoTa(String moTa)       { this.moTa    = moTa; }

    @Override
    public String toString() {
        return tenLoai != null ? tenLoai : "";
    }
}