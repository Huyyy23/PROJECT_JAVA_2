package DTO.ThongKe;

public class ThongKeTheLoaiBanDTO {

    private final int maLoai;
    private final String loaiSP;
    private final int tongSoLuongBan;
    private final int soHoaDon;
    private final int soSanPham;
    private final long doanhThu;

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
