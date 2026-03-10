package DTO.ThongKe;

public class ThongKeHoaDonBanDTO {

    private final String thoiGian;
    private final int soHoaDon;
    private final int tongSoSanPham;
    private final int tongLoaiSanPham;
    private final long tongDoanhThu;

    public ThongKeHoaDonBanDTO(String thoiGian, int soHoaDon,
                               int tongSoSanPham, int tongLoaiSanPham, long tongDoanhThu) {
        this.thoiGian = thoiGian;
        this.soHoaDon = soHoaDon;
        this.tongSoSanPham = tongSoSanPham;
        this.tongLoaiSanPham = tongLoaiSanPham;
        this.tongDoanhThu = tongDoanhThu;
    }

    public String getThoiGian() {
        return thoiGian;
    }

    public int getSoHoaDon() {
        return soHoaDon;
    }

    public int getTongSoSanPham() {
        return tongSoSanPham;
    }

    public int getTongLoaiSanPham() {
        return tongLoaiSanPham;
    }

    public long getTongDoanhThu() {
        return tongDoanhThu;
    }

    @Override
    public String toString() {
        return "ThongKeHoaDonBanDTO{" +
                "thoiGian='" + thoiGian + '\'' +
                ", soHoaDon=" + soHoaDon +
                ", tongSoSanPham=" + tongSoSanPham +
                ", tongLoaiSanPham=" + tongLoaiSanPham +
                ", tongDoanhThu=" + tongDoanhThu +
                '}';
    }
}
