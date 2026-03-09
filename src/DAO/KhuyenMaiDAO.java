package DAO;


import DTO.KhuyenMaiDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {

    public int them(KhuyenMaiDTO km) throws SQLException {
        String sql = "INSERT INTO KHUYENMAI " +
                "(TenChuongTrinh,MaCode,LoaiGiam,GiaTriGiam,GiaTriDonHangToiThieu," +
                "GiamToiDa,NgayBatDau,NgayKetThuc,SoLuongPhat,TrangThai) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString    (1, km.getTenChuongTrinh());
            ps.setString    (2, km.getMaCode());
            ps.setString    (3, km.getLoaiGiam());
            ps.setBigDecimal(4, km.getGiaTriGiam());
            ps.setBigDecimal(5, km.getGiaTriDonHangToiThieu());
            if (km.getGiamToiDa() != null) ps.setBigDecimal(6, km.getGiamToiDa());
            else ps.setNull(6, Types.DECIMAL);
            ps.setDate      (7, Date.valueOf(km.getNgayBatDau()));
            ps.setDate      (8, Date.valueOf(km.getNgayKetThuc()));
            if (km.getSoLuongPhat() != null) ps.setInt(9, km.getSoLuongPhat());
            else ps.setNull(9, Types.INTEGER);
            ps.setString    (10, "HoatDong");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("Không lấy được MaKhuyenMai");
            }
        }
    }

    public KhuyenMaiDTO layTheoMa(int ma) throws SQLException {
        String sql = "SELECT * FROM KHUYENMAI WHERE MaKhuyenMai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public KhuyenMaiDTO layTheoCode(String maCode) throws SQLException {
        String sql = "SELECT * FROM KHUYENMAI WHERE MaCode = ? AND TrangThai = N'HoatDong'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<KhuyenMaiDTO> layTatCa() throws SQLException {
        String sql = "SELECT * FROM KHUYENMAI ORDER BY NgayKetThuc DESC";
        List<KhuyenMaiDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<KhuyenMaiDTO> layDangHoatDong() throws SQLException {
        String sql = "SELECT * FROM KHUYENMAI WHERE TrangThai = N'HoatDong' " +
                "AND NgayBatDau <= CAST(GETDATE() AS DATE) " +
                "AND NgayKetThuc >= CAST(GETDATE() AS DATE)";
        List<KhuyenMaiDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean tangSoLuongDaDung(int ma) throws SQLException {
        String sql = "UPDATE KHUYENMAI SET SoLuongDaDung = SoLuongDaDung + 1 WHERE MaKhuyenMai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ma);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhat(KhuyenMaiDTO km) throws SQLException {
        String sql = "UPDATE KHUYENMAI SET TenChuongTrinh=?,GiaTriGiam=?,GiaTriDonHangToiThieu=?," +
                "GiamToiDa=?,NgayBatDau=?,NgayKetThuc=?,SoLuongPhat=? WHERE MaKhuyenMai=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString    (1, km.getTenChuongTrinh());
            ps.setBigDecimal(2, km.getGiaTriGiam());
            ps.setBigDecimal(3, km.getGiaTriDonHangToiThieu());
            if (km.getGiamToiDa() != null) ps.setBigDecimal(4, km.getGiamToiDa());
            else ps.setNull(4, Types.DECIMAL);
            ps.setDate      (5, Date.valueOf(km.getNgayBatDau()));
            ps.setDate      (6, Date.valueOf(km.getNgayKetThuc()));
            if (km.getSoLuongPhat() != null) ps.setInt(7, km.getSoLuongPhat());
            else ps.setNull(7, Types.INTEGER);
            ps.setInt       (8, km.getMaKhuyenMai());
            return ps.executeUpdate() > 0;
        }
    }

    // Xóa mềm
    public boolean xoaMem(int ma) throws SQLException {
        String sql = "UPDATE KHUYENMAI SET TrangThai = N'Tat' WHERE MaKhuyenMai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ma);
            return ps.executeUpdate() > 0;
        }
    }

    private KhuyenMaiDTO mapRow(ResultSet rs) throws SQLException {
        KhuyenMaiDTO km = new KhuyenMaiDTO();
        km.setMaKhuyenMai(rs.getInt("MaKhuyenMai"));
        km.setTenChuongTrinh(rs.getString("TenChuongTrinh"));
        km.setMaCode(rs.getString("MaCode"));
        km.setLoaiGiam(rs.getString("LoaiGiam"));
        km.setGiaTriGiam(rs.getBigDecimal("GiaTriGiam"));
        km.setGiaTriDonHangToiThieu(rs.getBigDecimal("GiaTriDonHangToiThieu"));
        km.setGiamToiDa(rs.getBigDecimal("GiamToiDa"));
        Date bd = rs.getDate("NgayBatDau"); if (bd != null) km.setNgayBatDau(bd.toLocalDate());
        Date kt = rs.getDate("NgayKetThuc"); if (kt != null) km.setNgayKetThuc(kt.toLocalDate());
        int slp = rs.getInt("SoLuongPhat"); if (!rs.wasNull()) km.setSoLuongPhat(slp);
        km.setSoLuongDaDung(rs.getInt("SoLuongDaDung"));
        km.setTrangThai(rs.getString("TrangThai"));
        return km;
    }
}
