package DTO;

public class ThongKeTheLoaiBanDTO {

    private int maLoai;
    private String loaiSP;
    private int tongSoLuongBan;
    private int soHoaDon;
    private int soSanPham;
    private long doanhThu;

    public ThongKeTheLoaiBanDTO(int maLoai, String loaiSP, int tongSoLuongBan,
                                 int soHoaDon, int soSanPham, long doanhThu) {
        this.maLoai = maLoai;
        this.loaiSP = loaiSP;
        this.tongSoLuongBan = tongSoLuongBan;
        this.soHoaDon = soHoaDon;
        this.soSanPham = soSanPham;
        this.doanhThu = doanhThu;
    }

    public int getMaLoai() {
        return maLoai;
    }

    public String getLoaiSP() {
        return loaiSP;
    }

    public int getTongSoLuongBan() {
        return tongSoLuongBan;
    }

    public int getSoHoaDon() {
        return soHoaDon;
    }

    public int getSoSanPham() {
        return soSanPham;
    }

    public long getDoanhThu() {
        return doanhThu;
    }
}
