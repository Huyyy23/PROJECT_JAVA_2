package DAO;

import DTO.ChiTietPhieuNhapDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.util.ArrayList;

/**
 * DAO cho bảng CHITIETPHIEUNHAP
 */
public class ChiTietPhieuNhapDAO {

    // ----------------------------------------------------------------
    // INSERT — Thêm một dòng chi tiết
    // ThanhTien là computed column → KHÔNG insert, DB tự tính
    // ----------------------------------------------------------------
    public int insert(ChiTietPhieuNhapDTO dto) {
        String sql = "INSERT INTO CHITIETPHIEUNHAP " +
                     "(MaPN, MaSP, SoLuong, DonGiaNhap, GhiChu) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, dto.getMaPN());
            ps.setInt(2, dto.getMaSP());
            ps.setInt(3, dto.getSoLuong());
            ps.setBigDecimal(4, dto.getDonGiaNhap());
            ps.setString(5, dto.getGhiChu());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ----------------------------------------------------------------
    // GET BY MAPHIEU — Lấy tất cả chi tiết của 1 phiếu nhập
    // ----------------------------------------------------------------
    public ArrayList<ChiTietPhieuNhapDTO> getByMaPN(int maPN) {
        ArrayList<ChiTietPhieuNhapDTO> list = new ArrayList<>();
        String sql = "SELECT MaChiTietPN, MaPN, MaSP, SoLuong, " +
                     "DonGiaNhap, ThanhTien, GhiChu " +
                     "FROM CHITIETPHIEUNHAP WHERE MaPN = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maPN);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChiTietPhieuNhapDTO dto = new ChiTietPhieuNhapDTO();
                dto.setMaChiTietPN(rs.getInt("MaChiTietPN"));
                dto.setMaPN(rs.getInt("MaPN"));
                dto.setMaSP(rs.getInt("MaSP"));
                dto.setSoLuong(rs.getInt("SoLuong"));
                dto.setDonGiaNhap(rs.getBigDecimal("DonGiaNhap"));
                dto.setThanhTien(rs.getBigDecimal("ThanhTien"));
                dto.setGhiChu(rs.getString("GhiChu"));
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ----------------------------------------------------------------
    // DELETE BY MAPHIEU — Xóa toàn bộ chi tiết của 1 phiếu
    // ----------------------------------------------------------------
    // Xóa mềm: chỉ đánh dấu DaXoa = 1 để giữ lịch sử, không xóa bản ghi thật
    public boolean deleteByMaPN(int maPN) {
        String sql = "UPDATE CHITIETPHIEUNHAP " +
                     "SET DaXoa = 1 " +
                     "WHERE MaPN = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maPN);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}