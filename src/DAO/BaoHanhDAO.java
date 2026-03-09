package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import DTO.BaoHanhDTO;
import UTIL.DBConnection;

public class BaoHanhDAO {

    // =========================================================================
    // THÊM BẢO HÀNH
    // =========================================================================
    public int them(BaoHanhDTO bh) {
        String sql = "INSERT INTO BAOHANH " +
                "(MaSerial, MaSP, MaHoaDon, MaNVTiepNhan, NgayHenTra, MoTaLoi, HinhThucXuLy, TrangThai) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, N'DangXuLy')";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bh.getMaIMEI() != null ? bh.getMaIMEI() : 0);
            ps.setInt(2, bh.getMaSP());
            ps.setInt(3, bh.getMaHoaDon());
            if (bh.getMaNVTiepNhan() != null)
                ps.setInt(4, bh.getMaNVTiepNhan());
            else
                ps.setNull(4, Types.INTEGER);
            ps.setDate(5, bh.getNgayHenTra() != null ? Date.valueOf(bh.getNgayHenTra()) : null);
            ps.setString(6, bh.getMoTaLoi());
            ps.setString(7, bh.getHinhThucXuLy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
                throw new SQLException("Không lấy được MaBaoHanh");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // =========================================================================
    // LẤY BẢO HÀNH THEO MÃ
    // =========================================================================
    public BaoHanhDTO layTheoMa(int ma) {
        String sql = "SELECT * FROM BAOHANH WHERE MaBaoHanh = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // =========================================================================
    // LẤY TẤT CẢ BẢO HÀNH
    // =========================================================================
    public List<BaoHanhDTO> getAll() {
        String sql = "SELECT * FROM BAOHANH ORDER BY NgayTiepNhan DESC";
        List<BaoHanhDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // LẤY BẢO HÀNH ĐANG XỬ LÝ
    // =========================================================================
    public List<BaoHanhDTO> layDangXuLy() {
        String sql = "SELECT * FROM BAOHANH WHERE TrangThai != N'DaTraKhach' ORDER BY NgayTiepNhan";
        List<BaoHanhDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // LẤY BẢO HÀNH THEO SERIAL
    // =========================================================================
    public List<BaoHanhDTO> layTheoIMEI(int maSerial) {
        String sql = "SELECT * FROM BAOHANH WHERE MaSerial = ? ORDER BY NgayTiepNhan DESC";
        List<BaoHanhDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maSerial);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // LẤY BẢO HÀNH THEO HÓA ĐƠN
    // =========================================================================
    public List<BaoHanhDTO> layTheoHoaDon(int maHoaDon) {
        String sql = "SELECT * FROM BAOHANH WHERE MaHoaDon = ? ORDER BY NgayTiepNhan DESC";
        List<BaoHanhDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // CẬP NHẬT TRẠNG THÁI BẢO HÀNH
    // =========================================================================
    public boolean capNhatTrangThai(int ma, String trangThai, Integer maNVXuLy, String ketQua) {
        String sql = "UPDATE BAOHANH SET TrangThai=?, MaNVXuLy=?, KetQuaXuLy=?, " +
                "NgayTra=CASE WHEN ?=N'DaTraKhach' THEN CAST(GETDATE() AS DATE) ELSE NgayTra END " +
                "WHERE MaBaoHanh=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            if (maNVXuLy != null)
                ps.setInt(2, maNVXuLy);
            else
                ps.setNull(2, Types.INTEGER);
            ps.setString(3, ketQua);
            ps.setString(4, trangThai);
            ps.setInt(5, ma);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // CẬP NHẬT TRẠNG THÁI + CHI PHÍ
    // =========================================================================
    public boolean capNhatTrangThaiVaChiPhi(int ma, String trangThai, Integer maNVXuLy,
            String ketQua, java.math.BigDecimal chiPhi) {
        String sql = "UPDATE BAOHANH SET TrangThai=?, MaNVXuLy=?, KetQuaXuLy=?, ChiPhiPhatSinh=?, " +
                "NgayTra=CASE WHEN ?=N'DaTraKhach' THEN CAST(GETDATE() AS DATE) ELSE NgayTra END " +
                "WHERE MaBaoHanh=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            if (maNVXuLy != null)
                ps.setInt(2, maNVXuLy);
            else
                ps.setNull(2, Types.INTEGER);
            ps.setString(3, ketQua);
            if (chiPhi != null)
                ps.setBigDecimal(4, chiPhi);
            else
                ps.setBigDecimal(4, java.math.BigDecimal.ZERO);
            ps.setString(5, trangThai);
            ps.setInt(6, ma);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // XÓA BẢO HÀNH
    // =========================================================================
    public boolean xoa(int ma) {
        String sql = "DELETE FROM BAOHANH WHERE MaBaoHanh = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ma);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // TÌM KIẾM - LIKE TRÊN CÁC TRƯỜNG
    // =========================================================================
    public List<BaoHanhDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty())
            return getAll();
        List<BaoHanhDTO> list = new ArrayList<>();
        String kw = "%" + keyword.trim() + "%";
        String sql = "SELECT * FROM BAOHANH WHERE " +
                "CAST(MaBaoHanh AS NVARCHAR) LIKE ? OR " +
                "CAST(MaSerial AS NVARCHAR) LIKE ? OR " +
                "CAST(MaSP AS NVARCHAR) LIKE ? OR " +
                "MoTaLoi LIKE ? OR " +
                "TrangThai LIKE ? " +
                "ORDER BY NgayTiepNhan DESC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++)
                ps.setString(i, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // MAPPING
    // =========================================================================
    private BaoHanhDTO mapRow(ResultSet rs) throws SQLException {
        BaoHanhDTO bh = new BaoHanhDTO();
        bh.setMaBaoHanh(rs.getInt("MaBaoHanh"));

        int maSerial = rs.getInt("MaSerial");
        if (!rs.wasNull())
            bh.setMaIMEI(maSerial);

        bh.setMaSP(rs.getInt("MaSP"));
        bh.setMaHoaDon(rs.getInt("MaHoaDon"));

        int maNVTN = rs.getInt("MaNVTiepNhan");
        if (!rs.wasNull())
            bh.setMaNVTiepNhan(maNVTN);

        int maNVXL = rs.getInt("MaNVXuLy");
        if (!rs.wasNull())
            bh.setMaNVXuLy(maNVXL);

        Date tn = rs.getDate("NgayTiepNhan");
        if (tn != null)
            bh.setNgayTiepNhan(tn.toLocalDate());

        Date ht = rs.getDate("NgayHenTra");
        if (ht != null)
            bh.setNgayHenTra(ht.toLocalDate());

        Date tr = rs.getDate("NgayTra");
        if (tr != null)
            bh.setNgayTra(tr.toLocalDate());

        bh.setMoTaLoi(rs.getString("MoTaLoi"));
        bh.setHinhThucXuLy(rs.getString("HinhThucXuLy"));
        bh.setKetQuaXuLy(rs.getString("KetQuaXuLy"));
        bh.setChiPhiPhatSinh(rs.getBigDecimal("ChiPhiPhatSinh"));
        bh.setTrangThai(rs.getString("TrangThai"));

        return bh;
    }
}
