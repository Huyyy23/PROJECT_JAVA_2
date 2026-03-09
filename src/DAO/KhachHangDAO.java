package DAO;

import DTO.KhachHangDTO;
import UTIL.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    public List<KhachHangDTO> getAll() {
        List<KhachHangDTO> list = new ArrayList<>();
        String sql = "SELECT MaKhachHang, TenKhachHang, GioiTinh, SoDienThoai, Email, "
                   + "DiaChi, NgaySinh, NgayDangKy, DiemTichLuy, HangKhachHang, PhanTramGiam "
                   + "FROM KHACHHANG ORDER BY MaKhachHang";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO.getAll] " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(KhachHangDTO kh) {
        // Đã xóa HangKhachHang và PhanTramGiam ra khỏi câu lệnh INSERT
        String sql = "INSERT INTO KHACHHANG "
                   + "(TenKhachHang, GioiTinh, SoDienThoai, Email, DiaChi, "
                   + " NgaySinh, NgayDangKy, DiemTichLuy) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setParams(ps, kh, false);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO.insert] " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(KhachHangDTO kh) {
        // Đã xóa HangKhachHang và PhanTramGiam ra khỏi câu lệnh UPDATE
        String sql = "UPDATE KHACHHANG SET "
                   + "TenKhachHang=?, GioiTinh=?, SoDienThoai=?, Email=?, DiaChi=?, "
                   + "NgaySinh=?, NgayDangKy=?, DiemTichLuy=? "
                   + "WHERE MaKhachHang=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setParams(ps, kh, true);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO.update] " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean delete(int maKhachHang) {
        String sql = "DELETE FROM KHACHHANG WHERE MaKhachHang=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKhachHang);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO.delete] " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<KhachHangDTO> search(String keyword) {
        List<KhachHangDTO> list = new ArrayList<>();
        String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String sql = "SELECT MaKhachHang, TenKhachHang, GioiTinh, SoDienThoai, Email, "
                   + "DiaChi, NgaySinh, NgayDangKy, DiemTichLuy, HangKhachHang, PhanTramGiam "
                   + "FROM KHACHHANG "
                   + "WHERE TenKhachHang LIKE ? OR SoDienThoai LIKE ? "
                   + "   OR Email LIKE ? OR CAST(MaKhachHang AS NVARCHAR) LIKE ? "
                   + "ORDER BY MaKhachHang";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO.search] " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private KhachHangDTO mapRow(ResultSet rs) throws SQLException {
        KhachHangDTO kh = new KhachHangDTO();
        kh.setMaKhachHang(rs.getInt("MaKhachHang"));
        kh.setTenKhachHang(rs.getString("TenKhachHang"));
        kh.setGioiTinh(rs.getString("GioiTinh"));
        kh.setSoDienThoai(rs.getString("SoDienThoai"));
        kh.setEmail(rs.getString("Email"));
        kh.setDiaChi(rs.getString("DiaChi"));
        kh.setNgaySinh(rs.getDate("NgaySinh"));
        kh.setNgayDangKy(rs.getDate("NgayDangKy"));
        kh.setDiemTichLuy(rs.getInt("DiemTichLuy"));
        kh.setHangKhachHang(rs.getString("HangKhachHang"));
        kh.setPhanTramGiam(rs.getDouble("PhanTramGiam"));
        return kh;
    }

    private void setParams(PreparedStatement ps, KhachHangDTO kh, boolean isUpdate)
            throws SQLException {
        ps.setNString(1, kh.getTenKhachHang());
        
        // --- ĐOẠN CODE CẦN SỬA ---
        // Chuẩn hóa giới tính: Nếu nhận được "Nữ" hoặc "Nu" (không phân biệt hoa thường) thì gán là "Nu", còn lại là "Nam"
        String gioiTinh = kh.getGioiTinh();
        if (gioiTinh != null && (gioiTinh.equalsIgnoreCase("Nữ") || gioiTinh.equalsIgnoreCase("Nu"))) {
            ps.setString(2, "Nu"); 
        } else {
            ps.setString(2, "Nam");
        }
        // -------------------------

        ps.setString(3, kh.getSoDienThoai());
        ps.setString(4, kh.getEmail());
        ps.setNString(5, kh.getDiaChi());

        if (kh.getNgaySinh() != null)
            ps.setDate(6, new java.sql.Date(kh.getNgaySinh().getTime()));
        else
            ps.setNull(6, Types.DATE);

        if (kh.getNgayDangKy() != null)
            ps.setDate(7, new java.sql.Date(kh.getNgayDangKy().getTime()));
        else
            ps.setDate(7, new java.sql.Date(System.currentTimeMillis()));

        ps.setInt(8, Math.max(0, kh.getDiemTichLuy()));

        if (isUpdate) {
            ps.setInt(9, kh.getMaKhachHang());
        }
    }
}