package DAO;

import DTO.NhanVienDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    // =========================================================================
    // LẤY TẤT CẢ NHÂN VIÊN
    // =========================================================================
    public List<NhanVienDTO> getAll() {
        List<NhanVienDTO> list = new ArrayList<>();
        String sql = "SELECT maNV, tenNV, gioiTinh, soDienThoai, email, diaChi, " +
                     "ngaySinh, ngayVaoLam, vaiTro, trangThai, cccd FROM NhanVien";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================================
    // TÌM KIẾM THEO TẤT CẢ CÁC TRƯỜNG — LIKE trên mọi cột DTO
    // trangThai là String nên so sánh trực tiếp không cần CAST
    // ngaySinh / ngayVaoLam dùng CONVERT để so sánh chuỗi ngày
    // =========================================================================
    public List<NhanVienDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAll();
        List<NhanVienDTO> list = new ArrayList<>();
        String kw = "%" + keyword.trim() + "%";

        String sql = "SELECT maNV, tenNV, gioiTinh, soDienThoai, email, diaChi, " +
                     "       ngaySinh, ngayVaoLam, vaiTro, trangThai, cccd " +
                     "FROM NhanVien " +
                     "WHERE CAST(maNV AS NVARCHAR)                      LIKE ? " +
                     "   OR tenNV                                        LIKE ? " +
                     "   OR gioiTinh                                     LIKE ? " +
                     "   OR soDienThoai                                  LIKE ? " +
                     "   OR email                                        LIKE ? " +
                     "   OR diaChi                                       LIKE ? " +
                     "   OR CONVERT(NVARCHAR, ngaySinh,   105)           LIKE ? " +
                     "   OR CONVERT(NVARCHAR, ngayVaoLam, 105)           LIKE ? " +
                     "   OR vaiTro                                       LIKE ? " +
                     "   OR trangThai                                    LIKE ? " +
                     "   OR cccd                                         LIKE ? ";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 11; i++) ps.setString(i, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================================
    // THÊM NHÂN VIÊN
    // =========================================================================
    public boolean insert(NhanVienDTO nv) {
        String sql = "INSERT INTO NhanVien " +
                     "(tenNV, gioiTinh, soDienThoai, email, diaChi, " +
                     " ngaySinh, ngayVaoLam, vaiTro, trangThai, cccd) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getTenNV());
            ps.setString(2, nv.getGioiTinh());
            ps.setString(3, nv.getSoDienThoai());
            ps.setString(4, nv.getEmail());
            ps.setString(5, nv.getDiaChi());
            ps.setDate(6,   nv.getNgaySinh());
            ps.setDate(7,   nv.getNgayVaoLam());
            ps.setString(8, nv.getVaiTro());
            ps.setString(9, nv.getTrangThai());
            ps.setString(10,nv.getCccd());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // =========================================================================
    // CẬP NHẬT NHÂN VIÊN
    // =========================================================================
    public boolean update(NhanVienDTO nv) {
        String sql = "UPDATE NhanVien SET " +
                     "tenNV=?, gioiTinh=?, soDienThoai=?, email=?, diaChi=?, " +
                     "ngaySinh=?, ngayVaoLam=?, vaiTro=?, trangThai=?, cccd=? " +
                     "WHERE maNV=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Debug — in ra console để kiểm tra giá trị trước khi UPDATE
            System.out.println("[update] maNV="       + nv.getMaNV());
            System.out.println("[update] tenNV="      + nv.getTenNV());
            System.out.println("[update] gioiTinh="   + nv.getGioiTinh());
            System.out.println("[update] soDienThoai="+ nv.getSoDienThoai());
            System.out.println("[update] email="      + nv.getEmail());
            System.out.println("[update] diaChi="     + nv.getDiaChi());
            System.out.println("[update] ngaySinh="   + nv.getNgaySinh());
            System.out.println("[update] ngayVaoLam=" + nv.getNgayVaoLam());
            System.out.println("[update] vaiTro="     + nv.getVaiTro());
            System.out.println("[update] trangThai="  + nv.getTrangThai());
            System.out.println("[update] cccd="       + nv.getCccd());

            ps.setString(1, nv.getTenNV());
            ps.setString(2, nv.getGioiTinh());
            ps.setString(3, nv.getSoDienThoai());
            ps.setString(4, nv.getEmail());
            ps.setString(5, nv.getDiaChi());

            // setDate an toàn — nếu null thì setNull thay vì để lỗi
            if (nv.getNgaySinh() != null) {
                ps.setDate(6, nv.getNgaySinh());
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }
            if (nv.getNgayVaoLam() != null) {
                ps.setDate(7, nv.getNgayVaoLam());
            } else {
                ps.setNull(7, java.sql.Types.DATE);
            }

            ps.setString(8,  nv.getVaiTro());
            ps.setString(9,  nv.getTrangThai());
            ps.setString(10, nv.getCccd());
            ps.setInt(11,    nv.getMaNV());

            int rows = ps.executeUpdate();
            System.out.println("[update] rows affected=" + rows);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[update] SQLException: " + e.getMessage());
            e.printStackTrace();
            // Ném lỗi lên GUI để hiện thông báo chính xác
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // =========================================================================
    // XÓA MỀM NHÂN VIÊN
    // Không DELETE thật — chỉ đổi trangThai → "NghiViec" để giữ lịch sử.
    // Gọi khi bấm nút "Xóa" trên GUI hoặc khi NV chính thức nghỉ việc.
    // =========================================================================
    public boolean delete(int maNV) {
        String sql = "UPDATE NhanVien SET trangThai = N'NghiViec' WHERE maNV = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // =========================================================================
    // HELPER — map ResultSet → NhanVienDTO
    // =========================================================================
    private NhanVienDTO mapRow(ResultSet rs) throws SQLException {
        return new NhanVienDTO(
            rs.getInt("maNV"),
            rs.getString("tenNV"),
            rs.getString("gioiTinh"),
            rs.getString("soDienThoai"),
            rs.getString("email"),
            rs.getString("diaChi"),
            rs.getDate("ngaySinh"),
            rs.getDate("ngayVaoLam"),
            rs.getString("vaiTro"),
            rs.getString("trangThai"),
            rs.getString("cccd")
        );
    }
}