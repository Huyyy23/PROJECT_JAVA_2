package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import DTO.ThongKe.ThongKeDoanhThuDTO;
import DTO.ThongKe.ThongKeHoaDonBanDTO;
import DTO.ThongKe.ThongKeSanPhamBanDTO;
import DTO.ThongKe.ThongKeTheLoaiBanDTO;
import UTIL.DBConnection;

public class ThongKeDAO {

    private static final Logger LOGGER = Logger.getLogger(ThongKeDAO.class.getName());

    // Ho tro ca du lieu cu va moi sau khi doi schema/trang thai.
    private static final String[] COMPLETED_STATUS_VALUES = {
        "HoanThanh", "ThanhCong", "DaThanhToan", "Đã thanh toán"
    };

    private Connection getSafeConnection() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            throw new IllegalStateException(
                    "Khong the ket noi CSDL. Kiem tra cau hinh trong DBConnection.");
        }
        return conn;
    }

    private String completedFilter(String alias) {
        return alias + ".TrangThai IN (N'" + COMPLETED_STATUS_VALUES[0] + "', N'"
                + COMPLETED_STATUS_VALUES[1] + "', N'" + COMPLETED_STATUS_VALUES[2]
                + "', N'" + COMPLETED_STATUS_VALUES[3] + "')";
    }

    /* =====================================================
       DOANH THU THEO NAM
    ===================================================== */
    public ArrayList<ThongKeDoanhThuDTO> thongKeDoanhThuTheoNam() {
        ArrayList<ThongKeDoanhThuDTO> list = new ArrayList<>();

        String sql = """
            SELECT YEAR(HD.NgayLap) AS Nam,
                   SUM(ISNULL(HD.TongThanhToan, 0)) AS DoanhThu
            FROM HOADON HD
            WHERE %s
            GROUP BY YEAR(HD.NgayLap)
            ORDER BY Nam
        """.formatted(completedFilter("HD"));

        try (Connection conn = getSafeConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int nam = rs.getInt("Nam");
                long doanhThu = rs.getLong("DoanhThu");
                long von = tinhVonTrongNam(conn, nam);
                list.add(new ThongKeDoanhThuDTO(String.valueOf(nam), von, doanhThu, doanhThu - von));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Loi thong ke doanh thu theo nam", e);
        }
        return list;
    }

    private long tinhVonTrongNam(Connection conn, int nam) {
        String sql = """
            SELECT SUM(ISNULL(CTPN.ThanhTien, 0)) AS TongVon
            FROM CHITIETPHIEUNHAP CTPN
            JOIN PHIEUNHAP PN ON CTPN.MaPN = PN.MaPN
            WHERE YEAR(PN.NgayNhap) = ?
              AND %s
        """.formatted(completedFilter("PN"));

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nam);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("TongVon");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Loi tinh von trong nam", e);
        }
        return 0;
    }

    /* =====================================================
       DOANH THU THEO THANG
    ===================================================== */
    public ArrayList<ThongKeDoanhThuDTO> thongKeDoanhThuTheoThang(int nam) {
        ArrayList<ThongKeDoanhThuDTO> list = new ArrayList<>();

        String sql = """
            SELECT MONTH(HD.NgayLap) AS Thang,
                   SUM(ISNULL(HD.TongThanhToan, 0)) AS DoanhThu
            FROM HOADON HD
            WHERE YEAR(HD.NgayLap) = ?
              AND %s
            GROUP BY MONTH(HD.NgayLap)
            ORDER BY Thang
        """.formatted(completedFilter("HD"));

        try (Connection conn = getSafeConnection();
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
            LOGGER.log(Level.SEVERE, "Loi thong ke doanh thu theo thang", e);
        }
        return list;
    }

    private long tinhVonTrongThang(Connection conn, int nam, int thang) {
        String sql = """
            SELECT SUM(ISNULL(CTPN.ThanhTien, 0)) AS TongVon
            FROM CHITIETPHIEUNHAP CTPN
            JOIN PHIEUNHAP PN ON CTPN.MaPN = PN.MaPN
            WHERE YEAR(PN.NgayNhap) = ?
              AND MONTH(PN.NgayNhap) = ?
              AND %s
        """.formatted(completedFilter("PN"));

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nam);
            ps.setInt(2, thang);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("TongVon");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Loi tinh von trong thang", e);
        }
        return 0;
    }

    /* =====================================================
       DOANH THU THEO KHOANG THOI GIAN
    ===================================================== */
    public ArrayList<ThongKeDoanhThuDTO> thongKeDoanhThuTuNgayDenNgay(Date from, Date to) {
        ArrayList<ThongKeDoanhThuDTO> list = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        String sql = """
            SELECT SUM(ISNULL(HD.TongThanhToan, 0)) AS DoanhThu
            FROM HOADON HD
            WHERE CONVERT(date, HD.NgayLap) = ?
              AND %s
        """.formatted(completedFilter("HD"));

        Calendar cal = Calendar.getInstance();
        cal.setTime(from);

        try (Connection conn = getSafeConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            while (!cal.getTime().after(to)) {
                Date ngay = cal.getTime();
                ps.setDate(1, new java.sql.Date(ngay.getTime()));

                try (ResultSet rs = ps.executeQuery()) {
                    long doanhThu = 0;
                    if (rs.next()) {
                        doanhThu = rs.getLong("DoanhThu");
                    }
                    long von = tinhVonTrongNgay(conn, ngay);
                    list.add(new ThongKeDoanhThuDTO(df.format(ngay), von, doanhThu, doanhThu - von));
                }

                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Loi thong ke doanh thu theo ngay", e);
        }

        return list;
    }

    private long tinhVonTrongNgay(Connection conn, Date ngay) {
        String sql = """
            SELECT SUM(ISNULL(CTPN.ThanhTien, 0)) AS TongVon
            FROM CHITIETPHIEUNHAP CTPN
            JOIN PHIEUNHAP PN ON CTPN.MaPN = PN.MaPN
            WHERE CONVERT(date, PN.NgayNhap) = ?
              AND %s
        """.formatted(completedFilter("PN"));

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(ngay.getTime()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("TongVon");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Loi tinh von trong ngay", e);
        }
        return 0;
    }

    /* =====================================================
       THONG KE SAN PHAM BAN
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
              AND %s
            GROUP BY SP.MaSP, SP.TenSP
            ORDER BY SoLuong DESC
        """.formatted(completedFilter("HD"));

        try (Connection conn = getSafeConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(from.getTime()));
            ps.setDate(2, new java.sql.Date(to.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ThongKeSanPhamBanDTO(
                            rs.getInt("MaSP"),
                            rs.getString("TenSP"),
                            rs.getInt("SoLuong"),
                            rs.getInt("SoDon"),
                            rs.getLong("DoanhThu")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Loi thong ke san pham ban", e);
        }
        return list;
    }

    /* =====================================================
       THONG KE THEO LOAI SAN PHAM
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
              AND %s
            GROUP BY LSP.MaLoai, LSP.TenLoai
            ORDER BY SoLuong DESC
        """.formatted(completedFilter("HD"));

        try (Connection conn = getSafeConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(from.getTime()));
            ps.setDate(2, new java.sql.Date(to.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Loi thong ke loai san pham", e);
        }
        return list;
    }

    /* =====================================================
       THONG KE HOA DON
    ===================================================== */
    public ArrayList<ThongKeHoaDonBanDTO> thongKeHoaDonTrongKhoangThoiGian(Date from, Date to) {
        ArrayList<ThongKeHoaDonBanDTO> list = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        String sql = """
            WITH HDF AS (
                SELECT HD.MaHoaDon, HD.TongThanhToan
                FROM HOADON HD
                WHERE CONVERT(date, HD.NgayLap) = ?
                  AND %s
            )
            SELECT
                (SELECT COUNT(*) FROM HDF) AS SoDon,
                (SELECT ISNULL(SUM(CTHD.SoLuong), 0)
                 FROM CHITIETHOADON CTHD
                 JOIN HDF ON CTHD.MaHoaDon = HDF.MaHoaDon) AS SoSP,
                (SELECT COUNT(DISTINCT CTHD.MaSP)
                 FROM CHITIETHOADON CTHD
                 JOIN HDF ON CTHD.MaHoaDon = HDF.MaHoaDon) AS SoLoai,
                (SELECT ISNULL(SUM(TongThanhToan), 0) FROM HDF) AS DoanhThu
        """.formatted(completedFilter("HD"));

        Calendar cal = Calendar.getInstance();
        cal.setTime(from);

        try (Connection conn = getSafeConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            while (!cal.getTime().after(to)) {
                Date ngay = cal.getTime();
                ps.setDate(1, new java.sql.Date(ngay.getTime()));

                try (ResultSet rs = ps.executeQuery()) {
                    int soDon = 0;
                    int soSP = 0;
                    int soLoai = 0;
                    long doanhThu = 0;
                    if (rs.next()) {
                        soDon = rs.getInt("SoDon");
                        soSP = rs.getInt("SoSP");
                        soLoai = rs.getInt("SoLoai");
                        doanhThu = rs.getLong("DoanhThu");
                    }
                    list.add(new ThongKeHoaDonBanDTO(df.format(ngay), soDon, soSP, soLoai, doanhThu));
                }

                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Loi thong ke hoa don theo ngay", e);
        }
        return list;
    }

    /* =====================================================
       THONG KE TOP SAN PHAM BAN CHAY
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
              AND %s
            GROUP BY SP.MaSP, SP.TenSP
            ORDER BY SoLuong DESC
        """.formatted(completedFilter("HD"));

        try (Connection conn = getSafeConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, top);
            ps.setDate(2, new java.sql.Date(from.getTime()));
            ps.setDate(3, new java.sql.Date(to.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ThongKeSanPhamBanDTO(
                            rs.getInt("MaSP"),
                            rs.getString("TenSP"),
                            rs.getInt("SoLuong"),
                            rs.getInt("SoDon"),
                            rs.getLong("DoanhThu")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Loi thong ke top san pham", e);
        }
        return list;
    }

    /* =====================================================
       THONG KE SAN PHAM TON KHO
    ===================================================== */
    public ArrayList<Object[]> thongKeSanPhamTonKho() {
        ArrayList<Object[]> list = new ArrayList<>();

        String sql = """
            SELECT SP.MaSP, SP.TenSP, LSP.TenLoai, SP.ThuongHieu,
                   SP.SoLuongTon, SP.SoLuongToiThieu, SP.SoLuongToiDa,
                   CASE
                       WHEN SP.SoLuongTon < SP.SoLuongToiThieu THEN N'Thieu'
                       WHEN SP.SoLuongTon > SP.SoLuongToiDa THEN N'Thua'
                       ELSE N'Binh thuong'
                   END AS TrangThaiTonKho
            FROM SANPHAM SP
            JOIN LOAISANPHAM LSP ON SP.MaLoai = LSP.MaLoai
            WHERE SP.TrangThai <> N'NgungBan'
            ORDER BY SP.SoLuongTon ASC
        """;

        try (Connection conn = getSafeConnection();
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
            LOGGER.log(Level.SEVERE, "Loi thong ke ton kho", e);
        }
        return list;
    }

    /* =====================================================
       THONG KE DOANH THU THEO NHAN VIEN
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
              AND %s
            GROUP BY NV.MaNV, NV.TenNV
            ORDER BY DoanhThu DESC
        """.formatted(completedFilter("HD"));

        try (Connection conn = getSafeConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(from.getTime()));
            ps.setDate(2, new java.sql.Date(to.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("MaNV"),
                        rs.getString("TenNV"),
                        rs.getInt("SoDon"),
                        rs.getLong("DoanhThu")
                    };
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Loi thong ke doanh thu theo nhan vien", e);
        }
        return list;
    }
}
