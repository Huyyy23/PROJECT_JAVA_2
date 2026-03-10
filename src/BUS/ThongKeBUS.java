package BUS;

import DAO.ThongKeDAO;
import DTO.ThongKe.ThongKeDoanhThuDTO;
import DTO.ThongKe.ThongKeHoaDonBanDTO;
import DTO.ThongKe.ThongKeSanPhamBanDTO;
import DTO.ThongKe.ThongKeTheLoaiBanDTO;
import java.util.ArrayList;
import java.util.Date;

public class ThongKeBUS {

    private final ThongKeDAO thongKeDAO = new ThongKeDAO();

    /* =====================================================
       DOANH THU
    ===================================================== */

    public ArrayList<ThongKeDoanhThuDTO> thongKeDoanhThuTheoNam() {
        return thongKeDAO.thongKeDoanhThuTheoNam();
    }

    public ArrayList<ThongKeDoanhThuDTO> thongKeDoanhThuTheoThang(int nam) {
        return thongKeDAO.thongKeDoanhThuTheoThang(nam);
    }

    public ArrayList<ThongKeDoanhThuDTO> thongKeDoanhThuTuNgayDenNgay(Date ngayBatDau, Date ngayKetThuc) {
        return thongKeDAO.thongKeDoanhThuTuNgayDenNgay(ngayBatDau, ngayKetThuc);
    }

    /* =====================================================
       THỐNG KÊ SẢN PHẨM BÁN
    ===================================================== */

    public ArrayList<ThongKeSanPhamBanDTO> thongKeSanPhamBanTrongKhoangThoiGian(
            Date ngayBatDau, Date ngayKetThuc) {
        return thongKeDAO.thongKeSanPhamBanTrongKhoangThoiGian(ngayBatDau, ngayKetThuc);
    }

    public ArrayList<ThongKeSanPhamBanDTO> thongKeTopSanPhamBanChay(
            int top, Date ngayBatDau, Date ngayKetThuc) {
        return thongKeDAO.thongKeTopSanPhamBanChay(top, ngayBatDau, ngayKetThuc);
    }

    /* =====================================================
       THỐNG KÊ LOẠI SẢN PHẨM
    ===================================================== */

    public ArrayList<ThongKeTheLoaiBanDTO> thongKeLoaiSanPhamTrongKhoangThoiGian(
            Date ngayBatDau, Date ngayKetThuc) {
        return thongKeDAO.thongKeLoaiSanPhamTrongKhoangThoiGian(ngayBatDau, ngayKetThuc);
    }

    /* =====================================================
       THỐNG KÊ HÓA ĐƠN
    ===================================================== */

    public ArrayList<ThongKeHoaDonBanDTO> thongKeHoaDonTrongKhoangThoiGian(
            Date ngayBatDau, Date ngayKetThuc) {
        return thongKeDAO.thongKeHoaDonTrongKhoangThoiGian(ngayBatDau, ngayKetThuc);
    }

    /* =====================================================
       THỐNG KÊ TỒN KHO
    ===================================================== */

    public ArrayList<Object[]> thongKeSanPhamTonKho() {
        return thongKeDAO.thongKeSanPhamTonKho();
    }

    /* =====================================================
       THỐNG KÊ NHÂN VIÊN
    ===================================================== */

    public ArrayList<Object[]> thongKeDoanhThuTheoNhanVien(Date ngayBatDau, Date ngayKetThuc) {
        return thongKeDAO.thongKeDoanhThuTheoNhanVien(ngayBatDau, ngayKetThuc);
    }
}