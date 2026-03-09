package DAO;

import DTO.LichSuBaoHanhDTO;
import UTIL.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LichSuBaoHanhDAO {

    public boolean ghiLichSu(LichSuBaoHanhDTO ls) {
        String sql = "INSERT INTO LICHSUBAOHANH (MaBaoHanh, MaNV, TrangThaiCu, TrangThaiMoi, GhiChu) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = DBConnection.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, ls.getMaBaoHanh());
            if (ls.getMaNV() != null)
                ps.setInt(2, ls.getMaNV());
            else
                ps.setNull(2, Types.INTEGER);
            ps.setString(3, ls.getTrangThaiCu());
            ps.setString(4, ls.getTrangThaiMoi());
            ps.setString(5, ls.getGhiChu());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<LichSuBaoHanhDTO> layLichSu(int maBaoHanh) {
        List<LichSuBaoHanhDTO> result = new ArrayList<>();
        String sql = "SELECT ls.MaLichSu, ls.MaBaoHanh, ls.ThoiGian, ls.MaNV, " +
                "       nv.TenNV, ls.TrangThaiCu, ls.TrangThaiMoi, ls.GhiChu " +
                "FROM LICHSUBAOHANH ls " +
                "LEFT JOIN NHANVIEN nv ON nv.MaNV = ls.MaNV " +
                "WHERE ls.MaBaoHanh = ? " +
                "ORDER BY ls.ThoiGian DESC";
        try (Connection cn = DBConnection.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, maBaoHanh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichSuBaoHanhDTO dto = new LichSuBaoHanhDTO();
                    dto.setMaLichSu(rs.getInt("MaLichSu"));
                    dto.setMaBaoHanh(rs.getInt("MaBaoHanh"));
                    Timestamp ts = rs.getTimestamp("ThoiGian");
                    dto.setThoiGian(ts != null ? ts.toLocalDateTime() : null);
                    int maNV = rs.getInt("MaNV");
                    dto.setMaNV(rs.wasNull() ? null : maNV);
                    String tenNV = rs.getString("TenNV");
                    dto.setTenNV(tenNV != null ? tenNV : (dto.getMaNV() != null ? "NV#" + dto.getMaNV() : "Hệ thống"));
                    dto.setTrangThaiCu(rs.getString("TrangThaiCu"));
                    dto.setTrangThaiMoi(rs.getString("TrangThaiMoi"));
                    dto.setGhiChu(rs.getString("GhiChu"));
                    result.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
