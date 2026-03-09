package DAO;

import UTIL.DBConnection;
import DTO.ThongSoKyThuatDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ThongSoKyThuatDAO {

    // =========================================================================
    // LẤY DỮ LIỆU
    // =========================================================================
    public ThongSoKyThuatDTO getByMaSP(int maSP) {
        String sql = "SELECT * FROM THONGSOKYTHUAT WHERE MaSP = ?";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, maSP);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ThongSoKyThuatDTO ts = new ThongSoKyThuatDTO();
                    ts.setMaThongSo(rs.getInt("MaThongSo"));
                    ts.setMaSP(rs.getInt("MaSP"));
                    ts.setCpu(rs.getString("CPU"));
                    ts.setRam(rs.getString("RAM"));
                    ts.setOCung(rs.getString("OCung"));
                    ts.setManHinh(rs.getString("ManHinh"));
                    ts.setVga(rs.getString("VGA"));
                    ts.setHeDieuHanh(rs.getString("HeDieuHanh"));
                    ts.setPin(rs.getString("Pin"));
                    ts.setTrongLuong(rs.getString("TrongLuong"));
                    ts.setKetNoi(rs.getString("KetNoi"));
                    return ts;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // =========================================================================
    // THÊM MỚI
    // =========================================================================
    public boolean insert(ThongSoKyThuatDTO ts) {
        String sql = "INSERT INTO THONGSOKYTHUAT(MaSP, CPU, RAM, OCung, ManHinh, VGA, HeDieuHanh, Pin, TrongLuong, KetNoi) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ts.getMaSP());
            ps.setString(2, ts.getCpu());
            ps.setString(3, ts.getRam());
            ps.setString(4, ts.getOCung());
            ps.setString(5, ts.getManHinh());
            ps.setString(6, ts.getVga());
            ps.setString(7, ts.getHeDieuHanh());
            ps.setString(8, ts.getPin());
            ps.setString(9, ts.getTrongLuong());
            ps.setString(10, ts.getKetNoi());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // CẬP NHẬT
    // =========================================================================
    public boolean update(ThongSoKyThuatDTO ts) {
        String sql = "UPDATE THONGSOKYTHUAT "
                   + "SET CPU=?, RAM=?, OCung=?, ManHinh=?, VGA=?, HeDieuHanh=?, Pin=?, TrongLuong=?, KetNoi=? "
                   + "WHERE MaSP=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ts.getCpu());
            ps.setString(2, ts.getRam());
            ps.setString(3, ts.getOCung());
            ps.setString(4, ts.getManHinh());
            ps.setString(5, ts.getVga());
            ps.setString(6, ts.getHeDieuHanh());
            ps.setString(7, ts.getPin());
            ps.setString(8, ts.getTrongLuong());
            ps.setString(9, ts.getKetNoi());
            ps.setInt(10, ts.getMaSP()); // Điều kiện WHERE

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // XÓA (XÓA CỨNG - Vì bảng này là bảng con phụ thuộc 1-1 vào sản phẩm)
    // =========================================================================
    public boolean deleteByMaSP(int maSP) {
        String sql = "DELETE FROM THONGSOKYTHUAT WHERE MaSP=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, maSP);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // KIỂM TRA TỒN TẠI
    // =========================================================================
    public boolean isExist(int maSP) {
        String sql = "SELECT 1 FROM THONGSOKYTHUAT WHERE MaSP = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Trả về true nếu tìm thấy
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
