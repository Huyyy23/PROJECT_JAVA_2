package DTO.ThongKe;

public class ThongKeDoanhThuDTO {

    private final String thoiGian;     // ngày / tháng / năm
    private final long tongVon;        // tổng tiền nhập
    private final long tongDoanhThu;   // tổng tiền bán
    private final long loiNhuan;       // doanh thu - vốn

    public ThongKeDoanhThuDTO(String thoiGian, long tongVon, long tongDoanhThu, long loiNhuan) {
        this.thoiGian = thoiGian;
        this.tongVon = tongVon;
        this.tongDoanhThu = tongDoanhThu;
        this.loiNhuan = loiNhuan;
    }

    public String getThoiGian() {
        return thoiGian;
    }

    public long getTongVon() {
        return tongVon;
    }

    public long getTongDoanhThu() {
        return tongDoanhThu;
    }

    public long getLoiNhuan() {
        return loiNhuan;
    }

    @Override
    public String toString() {
        return "ThongKeDoanhThuDTO{" +
                "thoiGian='" + thoiGian + '\'' +
                ", tongVon=" + tongVon +
                ", tongDoanhThu=" + tongDoanhThu +
                ", loiNhuan=" + loiNhuan +
                '}';
    }
}
