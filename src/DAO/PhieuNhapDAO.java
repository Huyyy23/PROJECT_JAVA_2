package DAO;

import DTO.PhieuNhapDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng PHIEUNHAP
 *
 * CHANGELOG:
 *   2026-03-09 - [THÊM] getAllWithNames(): JOIN NHANVIEN + NHACUNGCAP trả tên thật.
 *   2026-03-09 - [SỬA] updateTrangThai(): bỏ logic sinh serial ở đây —
 *                       serial được sinh tập trung ở PhieuNhapBUS.thanhToan()
 *                       trong 1 transaction duy nhất. DAO chỉ UPDATE TrangThai.
 */
public class PhieuNhapDAO {

    // ----------------------------------------------------------------
    // INSERT — dùng Connection truyền vào (để tham gia transaction lớn)
    // ----------------------------------------------------------------
    public int insert(Connection con, PhieuNhapDTO dto) throws SQLException {
        String sql = "INSERT INTO PHIEUNHAP " +
                     "(MaNhaCungCap, MaNV, NgayNhap, TongTien, GhiChu, TrangThai) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, dto.getMaNhaCungCap());
            ps.setInt(2, dto.getMaNV());
            ps.setDate(3, dto.getNgayNhap() != null
                    ? Date.valueOf(dto.getNgayNhap()) : Date.valueOf(LocalDate.now()));
            if (dto.getTongTien() != null) ps.setBigDecimal(4, dto.getTongTien());
            else                           ps.setNull(4, Types.DECIMAL);
            ps.setString(5, dto.getGhiChu());
            ps.setString(6, dto.getTrangThai() != null ? dto.getTrangThai() : "ChoXuLy");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Không lấy được MaPN sau khi insert!");
    }

    // ----------------------------------------------------------------
    // UPDATE TRANG THAI — chỉ đơn giản UPDATE, không làm gì thêm
    // ----------------------------------------------------------------
    public void updateTrangThai(Connection con, int maPN, String trangThai) throws SQLException {
        String sql = "UPDATE PHIEUNHAP SET TrangThai = ? WHERE MaPN = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setInt(2, maPN);
            if (ps.executeUpdate() == 0)
                throw new SQLException("Không tìm thấy phiếu #" + maPN);
        }
    }

    // ----------------------------------------------------------------
    // UPDATE TONG TIEN — dùng Connection truyền vào
    // ----------------------------------------------------------------
    public void updateTongTien(Connection con, int maPN) throws SQLException {
        String sql = "UPDATE PHIEUNHAP " +
                     "SET TongTien = (SELECT ISNULL(SUM(ThanhTien),0) " +
                     "                FROM CHITIETPHIEUNHAP WHERE MaPN = ?) " +
                     "WHERE MaPN = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maPN);
            ps.setInt(2, maPN);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // GET BY ID
    // ----------------------------------------------------------------
    public PhieuNhapDTO getById(int maPN) throws SQLException {
        String sql = "SELECT MaPN, MaNhaCungCap, MaNV, NgayNhap, " +
                     "TongTien, GhiChu, TrangThai FROM PHIEUNHAP WHERE MaPN = ?";
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
    // GET ALL
    // ----------------------------------------------------------------
    public List<PhieuNhapDTO> getAll() throws SQLException {
        String sql = "SELECT MaPN, MaNhaCungCap, MaNV, NgayNhap, " +
                     "TongTien, GhiChu, TrangThai FROM PHIEUNHAP ORDER BY NgayNhap DESC";
        List<PhieuNhapDTO> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ----------------------------------------------------------------
    // GET ALL WITH NAMES — [2026-03-09] JOIN tên NV + NCC
    // ----------------------------------------------------------------
    public List<Object[]> getAllWithNames() throws SQLException {
        String sql =
            "SELECT pn.MaPN, pn.NgayNhap, " +
            "       ncc.TenNhaCungCap, nv.TenNV, " +
            "       pn.TongTien, pn.TrangThai " +
            "FROM   PHIEUNHAP pn " +
            "JOIN   NHACUNGCAP ncc ON pn.MaNhaCungCap = ncc.MaNhaCungCap " +
            "JOIN   NHANVIEN   nv  ON pn.MaNV          = nv.MaNV " +
            "ORDER  BY pn.NgayNhap DESC";
        List<Object[]> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date d = rs.getDate("NgayNhap");
                list.add(new Object[]{
                    rs.getInt("MaPN"),
                    d != null ? d.toLocalDate().toString() : "",
                    rs.getString("TenNhaCungCap"),
                    rs.getString("TenNV"),
                    rs.getBigDecimal("TongTien"),
                    rs.getString("TrangThai")
                });
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // GET BY TRANG THAI
    // ----------------------------------------------------------------
    public List<PhieuNhapDTO> getByTrangThai(String trangThai) throws SQLException {
        String sql = "SELECT MaPN, MaNhaCungCap, MaNV, NgayNhap, TongTien, GhiChu, TrangThai " +
                     "FROM PHIEUNHAP WHERE TrangThai = ? ORDER BY NgayNhap DESC";
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
    // GET BY NHA CUNG CAP
    // ----------------------------------------------------------------
    public List<PhieuNhapDTO> getByNhaCungCap(int maNhaCungCap) throws SQLException {
        String sql = "SELECT MaPN, MaNhaCungCap, MaNV, NgayNhap, TongTien, GhiChu, TrangThai " +
                     "FROM PHIEUNHAP WHERE MaNhaCungCap = ? ORDER BY NgayNhap DESC";
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
    // GET BY KHOANG NGAY
    // ----------------------------------------------------------------
    public List<PhieuNhapDTO> getByKhoangNgay(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        String sql = "SELECT MaPN, MaNhaCungCap, MaNV, NgayNhap, TongTien, GhiChu, TrangThai " +
                     "FROM PHIEUNHAP WHERE NgayNhap BETWEEN ? AND ? ORDER BY NgayNhap DESC";
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
    // DELETE
    // ----------------------------------------------------------------
    public boolean delete(int maPN) throws SQLException {
        String sql = "DELETE FROM PHIEUNHAP WHERE MaPN = ? " +
                     "AND NOT EXISTS (SELECT 1 FROM CHITIETPHIEUNHAP WHERE MaPN = ?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPN);
            ps.setInt(2, maPN);
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------
    private PhieuNhapDTO mapRow(ResultSet rs) throws SQLException {
        PhieuNhapDTO dto = new PhieuNhapDTO();
        dto.setMaPN(rs.getInt("MaPN"));
        dto.setMaNhaCungCap(rs.getInt("MaNhaCungCap"));
        dto.setMaNV(rs.getInt("MaNV"));
        Date d = rs.getDate("NgayNhap");
        if (d != null) dto.setNgayNhap(d.toLocalDate());
        dto.setTongTien(rs.getBigDecimal("TongTien"));
        dto.setGhiChu(rs.getString("GhiChu"));
        dto.setTrangThai(rs.getString("TrangThai"));
        return dto;
    }
}
