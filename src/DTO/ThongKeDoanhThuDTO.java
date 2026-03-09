package DTO;

public class ThongKeDoanhThuDTO {

    private String thoiGian;     // ngày / tháng / năm
    private long tongVon;        // tổng tiền nhập
    private long tongDoanhThu;   // tổng tiền bán
    private long loiNhuan;       // doanh thu - vốn

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
