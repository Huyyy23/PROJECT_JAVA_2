package DAO;

import DTO.LoaiSanPhamDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.util.ArrayList;

public class LoaiSanPhamDAO {

    public ArrayList<LoaiSanPhamDTO> getAll() {
        ArrayList<LoaiSanPhamDTO> list = new ArrayList<>();
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT * FROM LOAISANPHAM";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Thêm getById để LoaiSanPhamBUS có thể fallback khi cache rỗng
    public LoaiSanPhamDTO getById(int maLoai) {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT * FROM LOAISANPHAM WHERE MaLoai = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maLoai);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(LoaiSanPhamDTO l) {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "INSERT INTO LOAISANPHAM(TenLoai, MoTa) VALUES (?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, l.getTenLoai());
            ps.setString(2, l.getMoTa());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(LoaiSanPhamDTO l) {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "UPDATE LOAISANPHAM SET TenLoai=?, MoTa=? WHERE MaLoai=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, l.getTenLoai());
            ps.setString(2, l.getMoTa());
            ps.setInt(3, l.getMaLoai());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Helper dùng chung — tránh duplicate code
    private LoaiSanPhamDTO mapRow(ResultSet rs) throws SQLException {
        LoaiSanPhamDTO l = new LoaiSanPhamDTO();
        l.setMaLoai(rs.getInt("MaLoai"));
        l.setTenLoai(rs.getString("TenLoai"));
        l.setMoTa(rs.getString("MoTa"));
        return l;
    }
}