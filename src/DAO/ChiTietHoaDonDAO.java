package DAO;

import DTO.ChiTietHoaDonDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.util.ArrayList;

public class ChiTietHoaDonDAO {

    // Thêm 1 dòng chi tiết hóa đơn
    public static boolean insert(ChiTietHoaDonDTO dto) {
        String sql = "INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement pst = cn.prepareStatement(sql)) {

            pst.setInt(1, dto.getMaHoaDon());
            pst.setInt(2, dto.getMaSP());
            pst.setInt(3, dto.getMaSerial());   // bắt buộc NOT NULL
            pst.setInt(4, dto.getSoLuong());
            pst.setBigDecimal(5, dto.getDonGia());

            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy tất cả chi tiết theo MaHoaDon
    public static ArrayList<ChiTietHoaDonDTO> getByHoaDon(int maHoaDon) {
        ArrayList<ChiTietHoaDonDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM CHITIETHOADON WHERE MaHoaDon = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement pst = cn.prepareStatement(sql)) {

            pst.setInt(1, maHoaDon);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy MaSerial TrongKho đầu tiên của 1 sản phẩm (dùng khi bán hàng)
    public static int laySerialTrongKho(int maSP) {
        String sql = "SELECT TOP 1 MaSerial FROM SERIAL "
                   + "WHERE MaSP = ? AND TrangThai = N'TrongKho'";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement pst = cn.prepareStatement(sql)) {

            pst.setInt(1, maSP);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("MaSerial");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // không tìm thấy serial
    }

    // Map ResultSet → ChiTietHoaDonDTO
    private static ChiTietHoaDonDTO mapRow(ResultSet rs) throws SQLException {
        return new ChiTietHoaDonDTO(
            rs.getInt("MaChiTiet"),
            rs.getInt("MaHoaDon"),
            rs.getInt("MaSP"),
            rs.getInt("MaSerial"),
            rs.getInt("SoLuong"),
            rs.getBigDecimal("DonGia"),
            rs.getBigDecimal("ThanhTien")
        );
    }
}