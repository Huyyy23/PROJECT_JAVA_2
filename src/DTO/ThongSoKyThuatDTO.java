package DTO;

public class ThongSoKyThuatDTO {
    private int    maThongSo;
    private int    maSP;
    private String cpu;
    private String ram;
    private String oCung;
    private String manHinh;
    private String vga;
    private String heDieuHanh;
    private String pin;
    private String trongLuong;
    private String ketNoi;

    public ThongSoKyThuatDTO() {}

    public int    getMaThongSo()           { return maThongSo; }
    public void   setMaThongSo(int maThongSo)      { this.maThongSo = maThongSo; }
    public int    getMaSP()                { return maSP; }
    public void   setMaSP(int maSP)           { this.maSP = maSP; }
    public String getCpu()                 { return cpu; }
    public void   setCpu(String cpu)         { this.cpu = cpu   ; }
    public String getRam()                 { return ram; }
    public void   setRam(String ram)         { this.ram = ram; }
    public String getOCung()               { return oCung; }
    public void   setOCung(String oCung)       { this.oCung = oCung; }
    public String getManHinh()             { return manHinh; }
    public void   setManHinh(String manHinh)     { this.manHinh = manHinh; }
    public String getVga()                 { return vga; }
    public void   setVga(String vga)         { this.vga = vga; }
    public String getHeDieuHanh()          { return heDieuHanh; }
    public void   setHeDieuHanh(String heDieuHanh)  { this.heDieuHanh = heDieuHanh; }
    public String getPin()                 { return pin; }
    public void   setPin(String pin)         { this.pin = pin; }
    public String getTrongLuong()          { return trongLuong; }
    public void   setTrongLuong(String trongLuong)  { this.trongLuong = trongLuong; }
    public String getKetNoi()              { return ketNoi; }
    public void   setKetNoi(String ketNoi)      { this.ketNoi = ketNoi; }
}
