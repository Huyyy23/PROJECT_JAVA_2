package DTO;

public class ThongKeSanPhamBanDTO {

    private int maSP;
    private String tenSP;
    private int tongSoLuongBan;
    private int soHoaDon;
    private long doanhThu;

    public ThongKeSanPhamBanDTO(int maSP, String tenSP,
                                int tongSoLuongBan, int soHoaDon, long doanhThu) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.tongSoLuongBan = tongSoLuongBan;
        this.soHoaDon = soHoaDon;
        this.doanhThu = doanhThu;
    }

    public int getMaSP() {
        return maSP;
    }

    public String getTenSP() {
        return tenSP;
    }

    public int getTongSoLuongBan() {
        return tongSoLuongBan;
    }

    public int getSoHoaDon() {
        return soHoaDon;
    }

    public long getDoanhThu() {
        return doanhThu;
    }

    @Override
    public String toString() {
        return "ThongKeSanPhamBanDTO{" +
                "maSP=" + maSP +
                ", tenSP='" + tenSP + '\'' +
                ", tongSoLuongBan=" + tongSoLuongBan +
                ", soHoaDon=" + soHoaDon +
                ", doanhThu=" + doanhThu +
                '}';
    }
}
