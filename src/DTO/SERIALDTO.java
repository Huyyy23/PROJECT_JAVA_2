package DTO;

import java.time.LocalDate;

public class SERIALDTO {

    private int maSerial;
    private String serialCode;
    private int maSP;
    private Integer maChiTietPN;
    private String trangThai;
    private LocalDate ngayNhap;
    private LocalDate ngayXuat;

    public SERIALDTO(){}

    public SERIALDTO(String serialCode, int maSP, Integer maChiTietPN,
                     String trangThai, LocalDate ngayNhap) {
        this.serialCode = serialCode;
        this.maSP = maSP;
        this.maChiTietPN = maChiTietPN;
        this.trangThai = trangThai;
        this.ngayNhap = ngayNhap;
    }

    public int getMaSerial() {
        return maSerial;
    }

    public void setMaSerial(int maSerial) {
        this.maSerial = maSerial;
    }

    public String getSerialCode() {
        return serialCode;
    }

    public void setSerialCode(String serialCode) {
        this.serialCode = serialCode;
    }

    public int getMaSP() {
        return maSP;
    }

    public void setMaSP(int maSP) {
        this.maSP = maSP;
    }

    public Integer getMaChiTietPN() {
        return maChiTietPN;
    }

    public void setMaChiTietPN(Integer maChiTietPN) {
        this.maChiTietPN = maChiTietPN;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDate getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(LocalDate ngayNhap) {
        this.ngayNhap = ngayNhap;
    }

    public LocalDate getNgayXuat() {
        return ngayXuat;
    }

    public void setNgayXuat(LocalDate ngayXuat) {
        this.ngayXuat = ngayXuat;
    }

    @Override
    public String toString() {
        return "SERIALDTO{" +
                "maSerial=" + maSerial +
                ", serialCode='" + serialCode + '\'' +
                ", maSP=" + maSP +
                ", maChiTietPN=" + maChiTietPN +
                ", trangThai='" + trangThai + '\'' +
                ", ngayNhap=" + ngayNhap +
                ", ngayXuat=" + ngayXuat +
                '}';
    }
}