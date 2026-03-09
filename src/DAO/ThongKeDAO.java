package DAO;

import DTO.ThongKeDoanhThuDTO;
import DTO.ThongKeHoaDonBanDTO;
import DTO.ThongKeSanPhamBanDTO;
import DTO.ThongKeTheLoaiBanDTO;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import UTIL.DBConnection;

public class ThongKeDAO {

    /* =====================================================
       DOANH THU THEO NĂM
    ===================================================== */
    public ArrayList<ThongKeDoanhThuDTO> thongKeDoanhThuTheoNam() {
        ArrayList<ThongKeDoanhThuDTO> list = new ArrayList<>();

        String sql = """
            SELECT YEAR(NgayLap) AS Nam,
                   SUM(ISNULL(TongThanhToan, 0)) AS DoanhThu
            FROM HOADON
            WHERE TrangThai = N'HoanThanh'
            GROUP BY YEAR(NgayLap)
            ORDER BY Nam
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int nam = rs.getInt("Nam");
                long doanhThu = rs.getLong("DoanhThu");
                long von = tinhVonTrongNam(conn, nam);
                list.add(new ThongKeDoanhThuDTO(
                        String.valueOf(nam),
                        von,
                        doanhThu,
                        doanhThu - von
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private long tinhVonTrongNam(Connection conn, int nam) {
        String sql = """
            SELECT SUM(ISNULL(CTPN.ThanhTien, 0)) AS TongVon
            FROM CHITIETPHIEUNHAP CTPN
            JOIN PHIEUNHAP PN ON CTPN.MaPN = PN.MaPN
            WHERE YEAR(PN.NgayNhap) = ? AND PN.TrangThai = N'HoanThanh'
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nam);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("TongVon");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /* =====================================================
       DOANH THU THEO THÁNG
    ===================================================== */
    public ArrayList<ThongKeDoanhThuDTO> thongKeDoanhThuTheoThang(int nam) {
        ArrayList<ThongKeDoanhThuDTO> list = new ArrayList<>();

        String sql = """
            SELECT MONTH(NgayLap) AS Thang,
                   SUM(ISNULL(TongThanhToan, 0)) AS DoanhThu
            FROM HOADON
            WHERE YEAR(NgayLap) = ? AND TrangThai = N'HoanThanh'
            GROUP BY MONTH(NgayLap)
            ORDER BY Thang
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, nam);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int thang = rs.getInt("Thang");
                long doanhThu = rs.getLong("DoanhThu");
                long von = tinhVonTrongThang(conn, nam, thang);
                list.add(new ThongKeDoanhThuDTO(
                        String.format("%02d/%d", thang, nam),
                        von,
                        doanhThu,
                        doanhThu - von
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private long tinhVonTrongThang(Connection conn, int nam, int thang) {
        String sql = """
            SELECT SUM(ISNULL(CTPN.ThanhTien, 0)) AS TongVon
            FROM CHITIETPHIEUNHAP CTPN
            JOIN PHIEUNHAP PN ON CTPN.MaPN = PN.MaPN
            WHERE YEAR(PN.NgayNhap) = ? AND MONTH(PN.NgayNhap) = ?
              AND PN.TrangThai = N'HoanThanh'
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nam);
            ps.setInt(2, thang);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("TongVon");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /* =====================================================
       DOANH THU THEO KHOẢNG THỜI GIAN
    ===================================================== */
    public ArrayList<ThongKeDoanhThuDTO> thongKeDoanhThuTuNgayDenNgay(Date from, Date to) {
        ArrayList<ThongKeDoanhThuDTO> list = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        Calendar cal = Calendar.getInstance();
        cal.setTime(from);

        while (!cal.getTime().after(to)) {
            Date ngay = cal.getTime();

            String sql = """
                SELECT SUM(ISNULL(TongThanhToan, 0)) AS DoanhThu
                FROM HOADON
                WHERE CONVERT(date, NgayLap) = ? AND TrangThai = N'HoanThanh'
            """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setDate(1, new java.sql.Date(ngay.getTime()));
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    long doanhThu = rs.getLong("DoanhThu");
                    long von = tinhVonTrongNgay(conn, ngay);
                    list.add(new ThongKeDoanhThuDTO(
                            df.format(ngay),
                            von,
                            doanhThu,
                            doanhThu - von
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return list;
    }

    private long tinhVonTrongNgay(Connection conn, Date ngay) {
        String sql = """
            SELECT SUM(ISNULL(CTPN.ThanhTien, 0)) AS TongVon
            FROM CHITIETPHIEUNHAP CTPN
            JOIN PHIEUNHAP PN ON CTPN.MaPN = PN.MaPN
            WHERE CONVERT(date, PN.NgayNhap) = ? AND PN.TrangThai = N'HoanThanh'
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(ngay.getTime()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("TongVon");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /* =====================================================
       THỐNG KÊ SẢN PHẨM BÁN
    ===================================================== */
    public ArrayList<ThongKeSanPhamBanDTO> thongKeSanPhamBanTrongKhoangThoiGian(Date from, Date to) {
        ArrayList<ThongKeSanPhamBanDTO> list = new ArrayList<>();

        String sql = """
            SELECT SP.MaSP, SP.TenSP,
                   SUM(CTHD.SoLuong) AS SoLuong,
                   COUNT(DISTINCT HD.MaHoaDon) AS SoDon,
                   SUM(ISNULL(CTHD.ThanhTien, 0)) AS DoanhThu
            FROM CHITIETHOADON CTHD
            JOIN HOADON HD ON CTHD.MaHoaDon = HD.MaHoaDon
            JOIN SANPHAM SP ON CTHD.MaSP = SP.MaSP
            WHERE CONVERT(date, HD.NgayLap) BETWEEN ? AND ?
              AND HD.TrangThai = N'HoanThanh'
            GROUP BY SP.MaSP, SP.TenSP
            ORDER BY SoLuong DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(from.getTime()));
            ps.setDate(2, new java.sql.Date(to.getTime()));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ThongKeSanPhamBanDTO(
                        rs.getInt("MaSP"),
                        rs.getString("TenSP"),
                        rs.getInt("SoLuong"),
                        rs.getInt("SoDon"),
                        rs.getLong("DoanhThu")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /* =====================================================
       THỐNG KÊ THEO LOẠI SẢN PHẨM
    ===================================================== */
    public ArrayList<ThongKeTheLoaiBanDTO> thongKeLoaiSanPhamTrongKhoangThoiGian(Date from, Date to) {
        ArrayList<ThongKeTheLoaiBanDTO> list = new ArrayList<>();

        String sql = """
            SELECT LSP.MaLoai, LSP.TenLoai,
                   SUM(CTHD.SoLuong) AS SoLuong,
                   COUNT(DISTINCT HD.MaHoaDon) AS SoDon,
                   COUNT(DISTINCT SP.MaSP) AS SoSP,
                   SUM(ISNULL(CTHD.ThanhTien, 0)) AS DoanhThu
            FROM CHITIETHOADON CTHD
            JOIN HOADON HD ON CTHD.MaHoaDon = HD.MaHoaDon
            JOIN SANPHAM SP ON CTHD.MaSP = SP.MaSP
            JOIN LOAISANPHAM LSP ON SP.MaLoai = LSP.MaLoai
            WHERE CONVERT(date, HD.NgayLap) BETWEEN ? AND ?
              AND HD.TrangThai = N'HoanThanh'
                        GROUP BY LSP.MaLoai, LSP.TenLoai
            ORDER BY SoLuong DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(from.getTime()));
            ps.setDate(2, new java.sql.Date(to.getTime()));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ThongKeTheLoaiBanDTO(
                        rs.getInt("MaLoai"),
                        rs.getString("TenLoai"),
                        rs.getInt("SoLuong"),
                        rs.getInt("SoDon"),
                        rs.getInt("SoSP"),
                        rs.getLong("DoanhThu")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /* =====================================================
       THỐNG KÊ HÓA ĐƠN
    ===================================================== */
    public ArrayList<ThongKeHoaDonBanDTO> thongKeHoaDonTrongKhoangThoiGian(Date from, Date to) {
        ArrayList<ThongKeHoaDonBanDTO> list = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        Calendar cal = Calendar.getInstance();
        cal.setTime(from);

        while (!cal.getTime().after(to)) {
            Date ngay = cal.getTime();

            String sql = """
                SELECT COUNT(DISTINCT HD.MaHoaDon) AS SoDon,
                       ISNULL(SUM(CTHD.SoLuong), 0) AS SoSP,
                       COUNT(DISTINCT CTHD.MaSP) AS SoLoai,
                       ISNULL(SUM(CTHD.ThanhTien), 0) AS DoanhThu
                FROM HOADON HD
                LEFT JOIN CHITIETHOADON CTHD ON HD.MaHoaDon = CTHD.MaHoaDon
                WHERE CONVERT(date, HD.NgayLap) = ?
                  AND HD.TrangThai = N'HoanThanh'
            """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setDate(1, new java.sql.Date(ngay.getTime()));
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    list.add(new ThongKeHoaDonBanDTO(
                            df.format(ngay),
                            rs.getInt("SoDon"),
                            rs.getInt("SoSP"),
                            rs.getInt("SoLoai"),
                            rs.getLong("DoanhThu")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return list;
    }

    /* =====================================================
       THỐNG KÊ TOP SẢN PHẨM BÁN CHẠY
    ===================================================== */
    public ArrayList<ThongKeSanPhamBanDTO> thongKeTopSanPhamBanChay(int top, Date from, Date to) {
        ArrayList<ThongKeSanPhamBanDTO> list = new ArrayList<>();

        String sql = """
            SELECT TOP (?) SP.MaSP, SP.TenSP,
                   SUM(CTHD.SoLuong) AS SoLuong,
                   COUNT(DISTINCT HD.MaHoaDon) AS SoDon,
                   SUM(ISNULL(CTHD.ThanhTien, 0)) AS DoanhThu
            FROM CHITIETHOADON CTHD
            JOIN HOADON HD ON CTHD.MaHoaDon = HD.MaHoaDon
            JOIN SANPHAM SP ON CTHD.MaSP = SP.MaSP
            WHERE CONVERT(date, HD.NgayLap) BETWEEN ? AND ?
              AND HD.TrangThai = N'HoanThanh'
            GROUP BY SP.MaSP, SP.TenSP
            ORDER BY SoLuong DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, top);
            ps.setDate(2, new java.sql.Date(from.getTime()));
            ps.setDate(3, new java.sql.Date(to.getTime()));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ThongKeSanPhamBanDTO(
                        rs.getInt("MaSP"),
                        rs.getString("TenSP"),
                        rs.getInt("SoLuong"),
                        rs.getInt("SoDon"),
                        rs.getLong("DoanhThu")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /* =====================================================
       THỐNG KÊ SẢN PHẨM TỒN KHO
    ===================================================== */
    public ArrayList<Object[]> thongKeSanPhamTonKho() {
        ArrayList<Object[]> list = new ArrayList<>();

        String sql = """
            SELECT SP.MaSP, SP.TenSP, LSP.TenLoai, SP.ThuongHieu,
                   SP.SoLuongTon, SP.SoLuongToiThieu, SP.SoLuongToiDa,
                   CASE 
                       WHEN SP.SoLuongTon < SP.SoLuongToiThieu THEN N'Thiếu'
                       WHEN SP.SoLuongTon > SP.SoLuongToiDa THEN N'Thừa'
                       ELSE N'Bình thường'
                   END AS TrangThaiTonKho
            FROM SANPHAM SP
            JOIN LOAISANPHAM LSP ON SP.MaLoai = LSP.MaLoai
            WHERE SP.TrangThai != N'NgungBan'
            ORDER BY SP.SoLuongTon ASC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaSP"),
                    rs.getString("TenSP"),
                    rs.getString("TenLoai"),
                    rs.getString("ThuongHieu"),
                    rs.getInt("SoLuongTon"),
                    rs.getInt("SoLuongToiThieu"),
                    rs.getInt("SoLuongToiDa"),
                    rs.getString("TrangThaiTonKho")
                };
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /* =====================================================
       THỐNG KÊ DOANH THU THEO NHÂN VIÊN
    ===================================================== */
    public ArrayList<Object[]> thongKeDoanhThuTheoNhanVien(Date from, Date to) {
        ArrayList<Object[]> list = new ArrayList<>();

        String sql = """
            SELECT NV.MaNV, NV.TenNV,
                   COUNT(DISTINCT HD.MaHoaDon) AS SoDon,
                   SUM(ISNULL(HD.TongThanhToan, 0)) AS DoanhThu
            FROM HOADON HD
            JOIN NHANVIEN NV ON HD.MaNV = NV.MaNV
            WHERE CONVERT(date, HD.NgayLap) BETWEEN ? AND ?
              AND HD.TrangThai = N'HoanThanh'
            GROUP BY NV.MaNV, NV.TenNV
            ORDER BY DoanhThu DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(from.getTime()));
            ps.setDate(2, new java.sql.Date(to.getTime()));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaNV"),
                    rs.getString("TenNV"),
                    rs.getInt("SoDon"),
                    rs.getLong("DoanhThu")
                };
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}