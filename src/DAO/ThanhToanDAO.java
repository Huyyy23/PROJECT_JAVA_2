package DAO;

import DTO.ThanhToanDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThanhToanDAO {

    public int them(ThanhToanDTO tt) throws SQLException {
        String sql = "INSERT INTO THANHTOAN (MaHoaDon,SoTien,PhuongThuc,TrangThai,GhiChu) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt       (1, tt.getMaHoaDon());
            ps.setBigDecimal(2, tt.getSoTien());
            ps.setString    (3, tt.getPhuongThuc());
            ps.setString    (4, tt.getTrangThai() != null ? tt.getTrangThai() : "ThanhCong");
            ps.setString    (5, tt.getGhiChu());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("Không lấy được MaThanhToan");
            }
        }
    }

    public List<ThanhToanDTO> layTheoHoaDon(int maHD) throws SQLException {
        String sql = "SELECT * FROM THANHTOAN WHERE MaHoaDon = ? ORDER BY NgayThanhToan";
        List<ThanhToanDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private ThanhToanDTO mapRow(ResultSet rs) throws SQLException {
        ThanhToanDTO tt = new ThanhToanDTO();
        tt.setMaThanhToan(rs.getInt("MaThanhToan"));
        tt.setMaHoaDon(rs.getInt("MaHoaDon"));
        Timestamp ngay = rs.getTimestamp("NgayThanhToan");
        if (ngay != null) tt.setNgayThanhToan(ngay.toLocalDateTime());
        tt.setSoTien(rs.getBigDecimal("SoTien"));
        tt.setPhuongThuc(rs.getString("PhuongThuc"));
        tt.setTrangThai(rs.getString("TrangThai"));
        tt.setGhiChu(rs.getString("GhiChu"));
        return tt;
    }
}