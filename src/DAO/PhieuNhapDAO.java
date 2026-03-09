package DAO;

import DTO.PhieuNhapDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng PHIEUNHAP
 * Thực hiện các thao tác CRUD với database
 */
public class PhieuNhapDAO {

    // ----------------------------------------------------------------
    // INSERT — Thêm phiếu nhập mới
    // ----------------------------------------------------------------
    public int insert(PhieuNhapDTO dto) throws SQLException {
        String sql = "INSERT INTO PHIEUNHAP " +
                     "(MaNhaCungCap, MaNV, NgayNhap, TongTien, GhiChu, TrangThai) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, dto.getMaNhaCungCap());
            ps.setInt(2, dto.getMaNV());

            if (dto.getNgayNhap() != null)
                ps.setDate(3, Date.valueOf(dto.getNgayNhap()));
            else
                ps.setDate(3, Date.valueOf(LocalDate.now()));

            if (dto.getTongTien() != null)
                ps.setBigDecimal(4, dto.getTongTien());
            else
                ps.setNull(4, Types.DECIMAL);

            ps.setString(5, dto.getGhiChu());
            ps.setString(6, dto.getTrangThai() != null
                             ? dto.getTrangThai() : "HoanThanh");

            ps.executeUpdate();

            // Lấy MaPN vừa tạo (IDENTITY)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // ----------------------------------------------------------------
    // SELECT ALL — Lấy tất cả phiếu nhập
    // ----------------------------------------------------------------
    public List<PhieuNhapDTO> getAll() throws SQLException {
        String sql = "SELECT MaPN, MaNhaCungCap, MaNV, NgayNhap, " +
                     "TongTien, GhiChu, TrangThai " +
                     "FROM PHIEUNHAP " +
                     "ORDER BY NgayNhap DESC";

        List<PhieuNhapDTO> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // SELECT BY ID — Lấy phiếu nhập theo MaPN
    // ----------------------------------------------------------------
    public PhieuNhapDTO getById(int maPN) throws SQLException {
        String sql = "SELECT MaPN, MaNhaCungCap, MaNV, NgayNhap, " +
                     "TongTien, GhiChu, TrangThai " +
                     "FROM PHIEUNHAP WHERE MaPN = ?";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPN);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ----------------------------------------------------------------
    // SELECT BY NHA CUNG CAP — Lấy phiếu nhập theo nhà cung cấp
    // ----------------------------------------------------------------
    public List<PhieuNhapDTO> getByNhaCungCap(int maNhaCungCap) throws SQLException {
        String sql = "SELECT MaPN, MaNhaCungCap, MaNV, NgayNhap, " +
                     "TongTien, GhiChu, TrangThai " +
                     "FROM PHIEUNHAP WHERE MaNhaCungCap = ? " +
                     "ORDER BY NgayNhap DESC";

        List<PhieuNhapDTO> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNhaCungCap);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // SELECT BY TRANG THAI — Lấy phiếu nhập theo trạng thái
    // ----------------------------------------------------------------
    public List<PhieuNhapDTO> getByTrangThai(String trangThai) throws SQLException {
        String sql = "SELECT MaPN, MaNhaCungCap, MaNV, NgayNhap, " +
                     "TongTien, GhiChu, TrangThai " +
                     "FROM PHIEUNHAP WHERE TrangThai = ? " +
                     "ORDER BY NgayNhap DESC";

        List<PhieuNhapDTO> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // SELECT BY KHOANG NGAY — Lấy phiếu nhập theo khoảng ngày
    // ----------------------------------------------------------------
    public List<PhieuNhapDTO> getByKhoangNgay(LocalDate tuNgay,
                                               LocalDate denNgay) throws SQLException {
        String sql = "SELECT MaPN, MaNhaCungCap, MaNV, NgayNhap, " +
                     "TongTien, GhiChu, TrangThai " +
                     "FROM PHIEUNHAP " +
                     "WHERE NgayNhap BETWEEN ? AND ? " +
                     "ORDER BY NgayNhap DESC";

        List<PhieuNhapDTO> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // UPDATE TONG TIEN — Cập nhật TongTien sau khi có chi tiết
    // ----------------------------------------------------------------
    public boolean updateTongTien(int maPN) throws SQLException {
        String sql = "UPDATE PHIEUNHAP " +
                     "SET TongTien = ( " +
                     "    SELECT ISNULL(SUM(ThanhTien), 0) " +
                     "    FROM CHITIETPHIEUNHAP WHERE MaPN = ? " +
                     ") " +
                     "WHERE MaPN = ?";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPN);
            ps.setInt(2, maPN);
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // HUY PHIEU NHAP — Cập nhật TrangThai = 'Huy'
    // Trigger trg_PhieuNhap_Huy sẽ tự động hoàn tác tồn kho
    // ----------------------------------------------------------------
    public boolean huyPhieuNhap(int maPN) throws SQLException {
        String sql = "UPDATE PHIEUNHAP SET TrangThai = N'Huy' WHERE MaPN = ?";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPN);
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // DELETE — Xóa phiếu nhập (chỉ xóa khi chưa có chi tiết)
    // ----------------------------------------------------------------
    public boolean delete(int maPN) throws SQLException {
        String sql = "DELETE FROM PHIEUNHAP WHERE MaPN = ? " +
                     "AND NOT EXISTS (" +
                     "    SELECT 1 FROM CHITIETPHIEUNHAP WHERE MaPN = ?" +
                     ")";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPN);
            ps.setInt(2, maPN);
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // Helper: Map ResultSet → DTO
    // ----------------------------------------------------------------
    private PhieuNhapDTO mapRow(ResultSet rs) throws SQLException {
        PhieuNhapDTO dto = new PhieuNhapDTO();
        dto.setMaPN(rs.getInt("MaPN"));
        dto.setMaNhaCungCap(rs.getInt("MaNhaCungCap"));
        dto.setMaNV(rs.getInt("MaNV"));

        Date ngayNhap = rs.getDate("NgayNhap");
        if (ngayNhap != null) dto.setNgayNhap(ngayNhap.toLocalDate());

        dto.setTongTien(rs.getBigDecimal("TongTien"));
        dto.setGhiChu(rs.getString("GhiChu"));
        dto.setTrangThai(rs.getString("TrangThai"));
        return dto;
    }

    public boolean updateTrangThai(int maPN, String trangThaiMoi) {
        // Tùy thuộc vào class kết nối DB của bạn tên là DBConnection hay JDBCUtil
        String sql = "UPDATE PHIEUNHAP SET TrangThai = ? WHERE MaPN = ?";
        try (Connection con = DBConnection.getConnection(); 
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThaiMoi);
            ps.setInt(2, maPN);
            
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}