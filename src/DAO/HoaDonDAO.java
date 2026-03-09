package DAO;

import DTO.HoaDonDTO;
import UTIL.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

public class HoaDonDAO {

    // =========================================================================
    // INSERT — Tạo hóa đơn mới, trả về MaHoaDon (-1 nếu lỗi)
    // Chỉ truyền các cột có thể SET; TongTienHang, computed columns do DB tự tính
    // =========================================================================
    public static int insert(HoaDonDTO dto) {
        String sql = "INSERT INTO HOADON (MaKhachHang, MaNV, PhanTramGiamHang, GhiChu, TrangThai) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (dto.getMaKhachHang() == null)
                ps.setNull(1, Types.INTEGER);
            else
                ps.setInt(1, dto.getMaKhachHang());

            ps.setInt(2, dto.getMaNV());
            ps.setBigDecimal(3,
                dto.getPhanTramGiamHang() != null ? dto.getPhanTramGiamHang() : BigDecimal.ZERO);
            ps.setString(4, dto.getGhiChu());
            ps.setString(5,
                dto.getTrangThai() != null ? dto.getTrangThai() : "HoanThanh");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // =========================================================================
    // GET BY ID
    // =========================================================================
    public static HoaDonDTO getById(int maHoaDon) {
        String sql = "SELECT * FROM HOADON WHERE MaHoaDon = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // =========================================================================
    // GET ALL
    // =========================================================================
    public static ArrayList<HoaDonDTO> getAll() {
        ArrayList<HoaDonDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM HOADON ORDER BY NgayLap DESC";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // CẬP NHẬT TRẠNG THÁI (HoanThanh / Huy / ChoXuLy)
    // =========================================================================
    public static boolean updateTrangThai(int maHoaDon, String trangThai) {
        String sql = "UPDATE HOADON SET TrangThai = ? WHERE MaHoaDon = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setInt(2, maHoaDon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // CẬP NHẬT % GIẢM GIÁ
    // phanTram: giá trị phần trăm (VD: 5.0 = giảm 5%)
    // DB tự tính lại TienGiamHang, TienTruocVAT, TienVAT, TongThanhToan
    // =========================================================================
    public static boolean capNhatPhanTramGiam(int maHoaDon, BigDecimal phanTram) {
        String sql = "UPDATE HOADON SET PhanTramGiamHang = ? WHERE MaHoaDon = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, phanTram != null ? phanTram : BigDecimal.ZERO);
            ps.setInt(2, maHoaDon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // CẬP NHẬT KHÁCH HÀNG CHO HÓA ĐƠN
    // =========================================================================
    public static boolean capNhatKhachHang(int maHoaDon, Integer maKhachHang) {
        String sql = "UPDATE HOADON SET MaKhachHang = ? WHERE MaHoaDon = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (maKhachHang == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, maKhachHang);
            ps.setInt(2, maHoaDon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    // =========================================================================
    // BỔ SUNG 08/03/2026 — Cập nhật 08/03/2026 18:12 — PARK ORDER (Lưu Hóa Đơn Tạm)
    // getDanhSachHoaDonCho() trả HoaDonDTO thuần, BUS tự ghép tên NV+KH
    // =========================================================================

    // Thêm hóa đơn với TrangThai = 'ChoXuLy', trả về MaHoaDon (IDENTITY), -1 nếu lỗi
    public static int insertHoaDonCho(HoaDonDTO dto) {
        String sql = "INSERT INTO HOADON (MaKhachHang, MaNV, TongTienHang, PhanTramGiamHang, GhiChu, TrangThai) "
                   + "VALUES (?, ?, ?, ?, ?, N'ChoXuLy')";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (dto.getMaKhachHang() == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, dto.getMaKhachHang());
            ps.setInt(2,        dto.getMaNV());
            ps.setBigDecimal(3, dto.getTongTienHang()     != null ? dto.getTongTienHang()     : BigDecimal.ZERO);
            ps.setBigDecimal(4, dto.getPhanTramGiamHang() != null ? dto.getPhanTramGiamHang() : BigDecimal.ZERO);
            ps.setString(5,     dto.getGhiChu());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    // Lấy danh sách hóa đơn ChoXuLy — trả HoaDonDTO thuần (maNV, maKhachHang)
    // BUS tự ghép TenNV + TenKhachHang khi cần hiển thị — DAO không JOIN tên
    public static ArrayList<HoaDonDTO> getDanhSachHoaDonCho() {
        ArrayList<HoaDonDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM HOADON WHERE TrangThai = N'ChoXuLy' ORDER BY NgayLap DESC";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Lấy chi tiết hóa đơn kèm TenSP — dùng khi load lại giỏ hàng từ đơn chờ
    public static ArrayList<DTO.ChiTietHoaDonDTO> getChiTietByMaHD(int maHoaDon) {
        ArrayList<DTO.ChiTietHoaDonDTO> list = new ArrayList<>();
        String sql = "SELECT ct.MaChiTiet, ct.MaHoaDon, ct.MaSP, ct.MaSerial, "
                   + "ct.SoLuong, ct.DonGia, ct.ThanhTien, sp.TenSP "
                   + "FROM CHITIETHOADON ct JOIN SANPHAM sp ON ct.MaSP = sp.MaSP "
                   + "WHERE ct.MaHoaDon = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DTO.ChiTietHoaDonDTO ct = new DTO.ChiTietHoaDonDTO(
                        rs.getInt("MaHoaDon"), rs.getInt("MaSP"),
                        rs.getInt("MaSerial"), rs.getInt("SoLuong"),
                        rs.getBigDecimal("DonGia")
                    );
                    ct.setMaChiTiet(rs.getInt("MaChiTiet"));
                    ct.setTenSP(rs.getString("TenSP"));
                    ct.setThanhTien(rs.getBigDecimal("ThanhTien"));
                    list.add(ct);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Xóa cứng 1 dòng CHITIETHOADON — trigger AfterDelete tự hoàn trả kho + serial
    public static boolean deleteChiTiet(int maChiTiet) {
        String sql = "DELETE FROM CHITIETHOADON WHERE MaChiTiet = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, maChiTiet);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Hủy hóa đơn ChoXuLy quá hạn → trả số dòng bị hủy
    // Trigger trg_HoaDon_Huy tự hoàn trả serial + kho
    public static int huyHoaDonQuaHan(int soGio) {
        String sql = "UPDATE HOADON SET TrangThai = N'Huy' "
                   + "WHERE TrangThai = N'ChoXuLy' AND NgayLap < DATEADD(HOUR, -?, GETDATE())";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, soGio);
            return ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // =========================================================================
    // MAP ResultSet → HoaDonDTO
    // =========================================================================
    private static HoaDonDTO mapRow(ResultSet rs) throws SQLException {
        HoaDonDTO dto = new HoaDonDTO();
        dto.setMaHoaDon(rs.getInt("MaHoaDon"));

        int maKH = rs.getInt("MaKhachHang");
        dto.setMaKhachHang(rs.wasNull() ? null : maKH);

        dto.setMaNV(rs.getInt("MaNV"));

        Timestamp ts = rs.getTimestamp("NgayLap");
        if (ts != null) dto.setNgayLap(ts.toLocalDateTime());

        dto.setTongTienHang(rs.getBigDecimal("TongTienHang"));
        dto.setPhanTramGiamHang(rs.getBigDecimal("PhanTramGiamHang"));
        dto.setTienGiamHang(rs.getBigDecimal("TienGiamHang"));
        dto.setTienTruocVAT(rs.getBigDecimal("TienTruocVAT"));
        dto.setTienVAT(rs.getBigDecimal("TienVAT"));
        dto.setTongThanhToan(rs.getBigDecimal("TongThanhToan"));
        dto.setGhiChu(rs.getString("GhiChu"));
        dto.setTrangThai(rs.getString("TrangThai"));
        return dto;
    }
    // =========================================================================
    // CẬP NHẬT GHI CHÚ HÓA ĐƠN
    // =========================================================================
    public static boolean capNhatGhiChu(int maHoaDon, String ghiChu) {
        String sql = "UPDATE HOADON SET GhiChu = ? WHERE MaHoaDon = ?";
        try (Connection cn = DBConnection.getConnection();
            PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, ghiChu);
            ps.setInt(2, maHoaDon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}