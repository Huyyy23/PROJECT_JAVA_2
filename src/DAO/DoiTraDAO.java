package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import DTO.DoiTraDTO;
import UTIL.DBConnection;

/**
 * DAO thực hiện tất cả thao tác CRUD + truy vấn nghiệp vụ cho bảng DOITRA.
 *
 * Quy ước:
 *  - Sử dụng UTIL.DBConnection để lấy kết nối (mỗi lần gọi mở 1 connection mới,
 *    tự đóng qua try-with-resources).
 *  - Mọi lỗi SQL được ném lại dưới dạng RuntimeException để BUS xử lý.
 *  - Các truy vấn SELECT đều JOIN để lấy thêm TenSP, SerialCode, TenNV.
 */
public class DoiTraDAO {

    // ════════════════════════════════════════════════════════════════════════
    //  SQL FRAGMENTS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * BASE SELECT – dùng lại ở mọi truy vấn để đảm bảo nhất quán.
     * Kết quả trả về đầy đủ các cột của DOITRA + 5 display columns.
     */
    private static final String BASE_SELECT = """
            SELECT
                dt.MaDoiTra,
                dt.MaHoaDon,
                dt.MaSP,
                dt.MaSerial,
                dt.SoLuongTra,
                dt.LoaiDoiTra,
                dt.MaSPMoi,
                dt.MaSerialMoi,
                dt.TienChenhLech,
                dt.LyDo,
                dt.MaNV,
                dt.NgayYeuCau,
                dt.NgayXuLy,
                dt.TrangThai,
                dt.GhiChu,
                sp.TenSP                AS TenSP,
                sr.SerialCode           AS SerialCode,
                spMoi.TenSP             AS TenSPMoi,
                srMoi.SerialCode        AS SerialCodeMoi,
                nv.TenNV                AS TenNV
            FROM   DOITRA dt
            JOIN   SANPHAM   sp    ON sp.MaSP         = dt.MaSP
            JOIN   SERIAL    sr    ON sr.MaSerial      = dt.MaSerial
            LEFT JOIN SANPHAM   spMoi ON spMoi.MaSP    = dt.MaSPMoi
            LEFT JOIN SERIAL    srMoi ON srMoi.MaSerial= dt.MaSerialMoi
            JOIN   NHANVIEN  nv    ON nv.MaNV          = dt.MaNV
            """;

    // ════════════════════════════════════════════════════════════════════════
    //  CREATE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Thêm mới một phiếu đổi/trả.
     * @return MaDoiTra được DB sinh ra (IDENTITY), hoặc -1 nếu thất bại.
     */
    public int insert(DoiTraDTO dto) {
        String sql = """
                INSERT INTO DOITRA
                    (MaHoaDon, MaSP, MaSerial, SoLuongTra, LoaiDoiTra,
                     MaSPMoi, MaSerialMoi, TienChenhLech,
                     LyDo, MaNV, NgayYeuCau, NgayXuLy, TrangThai, GhiChu)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new RuntimeException("DoiTraDAO.insert: Không thể kết nối CSDL.");
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, dto.getMaHoaDon());
            ps.setInt(2, dto.getMaSP());
            ps.setInt(3, dto.getMaSerial());
            ps.setInt(4, dto.getSoLuongTra());
            ps.setString(5, dto.getLoaiDoiTra());
            setNullableInt(ps, 6, dto.getMaSPMoi());
            setNullableInt(ps, 7, dto.getMaSerialMoi());
            ps.setBigDecimal(8, dto.getTienChenhLech() != null
                    ? dto.getTienChenhLech() : BigDecimal.ZERO);
            ps.setString(9,  dto.getLyDo());
            ps.setInt(10,    dto.getMaNV());
            ps.setDate(11,   toSqlDate(dto.getNgayYeuCau()));
            ps.setDate(12,   toSqlDate(dto.getNgayXuLy()));   // nullable
            ps.setString(13, dto.getTrangThai() != null
                    ? dto.getTrangThai() : DoiTraDTO.TRANG_THAI_CHO_DUYET);
            ps.setString(14, dto.getGhiChu());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DoiTraDAO.insert: " + e.getMessage(), e);
        } finally {
            closeConnection(conn);
        }
        return -1;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  READ – danh sách
    // ════════════════════════════════════════════════════════════════════════

    /** Lấy toàn bộ phiếu đổi/trả, mới nhất trước. */
    public List<DoiTraDTO> findAll() {
        return query(BASE_SELECT + " ORDER BY dt.NgayYeuCau DESC, dt.MaDoiTra DESC");
    }

    /** Lọc theo trạng thái (ChoDuyet / DangXuLy / TuChoi / HoanThanh). */
    public List<DoiTraDTO> findByTrangThai(String trangThai) {
        String sql = BASE_SELECT + " WHERE dt.TrangThai = ? ORDER BY dt.NgayYeuCau DESC";
        return queryWithParams(sql, trangThai);
    }

    /** Lọc theo loại đổi trả (DoiSanPham / TraHang / BaoHanh). */
    public List<DoiTraDTO> findByLoai(String loaiDoiTra) {
        String sql = BASE_SELECT + " WHERE dt.LoaiDoiTra = ? ORDER BY dt.NgayYeuCau DESC";
        return queryWithParams(sql, loaiDoiTra);
    }

    /** Lọc theo khoảng ngày yêu cầu [tuNgay, denNgay] (inclusive). */
    public List<DoiTraDTO> findByNgayYeuCau(LocalDate tuNgay, LocalDate denNgay) {
        String sql = BASE_SELECT
                + " WHERE dt.NgayYeuCau BETWEEN ? AND ?"
                + " ORDER BY dt.NgayYeuCau DESC";
        return queryWithParams(sql, Date.valueOf(tuNgay), Date.valueOf(denNgay));
    }

    /** Lọc kết hợp trạng thái + khoảng ngày. */
    public List<DoiTraDTO> findByTrangThaiAndNgay(String trangThai,
                                                   LocalDate tuNgay,
                                                   LocalDate denNgay) {
        String sql = BASE_SELECT
                + " WHERE dt.TrangThai = ? AND dt.NgayYeuCau BETWEEN ? AND ?"
                + " ORDER BY dt.NgayYeuCau DESC";
        return queryWithParams(sql, trangThai, Date.valueOf(tuNgay), Date.valueOf(denNgay));
    }

    /** Tìm tất cả phiếu đổi/trả theo mã hoá đơn. */
    public List<DoiTraDTO> findByHoaDon(int maHoaDon) {
        String sql = BASE_SELECT + " WHERE dt.MaHoaDon = ? ORDER BY dt.MaDoiTra DESC";
        return queryWithParams(sql, maHoaDon);
    }

    /** Tìm tất cả phiếu đổi/trả theo nhân viên xử lý. */
    public List<DoiTraDTO> findByNhanVien(int maNV) {
        String sql = BASE_SELECT + " WHERE dt.MaNV = ? ORDER BY dt.NgayYeuCau DESC";
        return queryWithParams(sql, maNV);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  READ – đơn lẻ
    // ════════════════════════════════════════════════════════════════════════

    /** Tìm phiếu đổi/trả theo khoá chính. Trả về null nếu không tìm thấy. */
    public DoiTraDTO findById(int maDoiTra) {
        String sql = BASE_SELECT + " WHERE dt.MaDoiTra = ?";
        List<DoiTraDTO> list = queryWithParams(sql, maDoiTra);
        return list.isEmpty() ? null : list.get(0);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UPDATE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Cập nhật toàn bộ thông tin phiếu đổi/trả (trừ khoá chính).
     * @return true nếu có ít nhất 1 dòng bị ảnh hưởng.
     */
    public boolean update(DoiTraDTO dto) {
        String sql = """
                UPDATE DOITRA SET
                    MaHoaDon       = ?,
                    MaSP           = ?,
                    MaSerial       = ?,
                    SoLuongTra     = ?,
                    LoaiDoiTra     = ?,
                    MaSPMoi        = ?,
                    MaSerialMoi    = ?,
                    TienChenhLech  = ?,
                    LyDo           = ?,
                    MaNV           = ?,
                    NgayYeuCau     = ?,
                    NgayXuLy       = ?,
                    TrangThai      = ?,
                    GhiChu         = ?
                WHERE MaDoiTra = ?
                """;
        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new RuntimeException("DoiTraDAO.update: Không thể kết nối CSDL.");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,    dto.getMaHoaDon());
            ps.setInt(2,    dto.getMaSP());
            ps.setInt(3,    dto.getMaSerial());
            ps.setInt(4,    dto.getSoLuongTra());
            ps.setString(5, dto.getLoaiDoiTra());
            setNullableInt(ps, 6,  dto.getMaSPMoi());
            setNullableInt(ps, 7,  dto.getMaSerialMoi());
            ps.setBigDecimal(8, dto.getTienChenhLech() != null
                    ? dto.getTienChenhLech() : BigDecimal.ZERO);
            ps.setString(9,  dto.getLyDo());
            ps.setInt(10,    dto.getMaNV());
            ps.setDate(11,   toSqlDate(dto.getNgayYeuCau()));
            ps.setDate(12,   toSqlDate(dto.getNgayXuLy()));
            ps.setString(13, dto.getTrangThai());
            ps.setString(14, dto.getGhiChu());
            ps.setInt(15,    dto.getMaDoiTra());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DoiTraDAO.update: " + e.getMessage(), e);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Chỉ cập nhật trạng thái + ngày xử lý + ghi chú (dành cho luồng duyệt/từ chối).
     */
    public boolean updateTrangThai(int maDoiTra, String trangThai,
                                   LocalDate ngayXuLy, String ghiChu) {
        String sql = """
                UPDATE DOITRA
                SET TrangThai = ?, NgayXuLy = ?, GhiChu = ?
                WHERE MaDoiTra = ?
                """;
        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new RuntimeException("DoiTraDAO.updateTrangThai: Không thể kết nối CSDL.");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setDate(2,   toSqlDate(ngayXuLy));
            ps.setString(3, ghiChu);
            ps.setInt(4,    maDoiTra);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DoiTraDAO.updateTrangThai: " + e.getMessage(), e);
        } finally {
            closeConnection(conn);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DELETE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Xoá phiếu đổi/trả theo khoá chính.
     * Lưu ý: chỉ cho phép xoá khi TrangThai = 'ChoDuyet'.
     * @return true nếu xoá thành công.
     */
    public boolean delete(int maDoiTra) {
        String sql = "DELETE FROM DOITRA WHERE MaDoiTra = ? AND TrangThai = 'ChoDuyet'";
        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new RuntimeException("DoiTraDAO.delete: Không thể kết nối CSDL.");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDoiTra);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DoiTraDAO.delete: " + e.getMessage(), e);
        } finally {
            closeConnection(conn);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  THỐNG KÊ
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Thống kê số lượng phiếu theo trạng thái trong khoảng ngày.
     * Trả về mảng Object[]: [TrangThai, SoPhieu, TongTienChenhLech]
     */
    public List<Object[]> thongKeTheoTrangThai(LocalDate tuNgay, LocalDate denNgay) {
        String sql = """
                SELECT
                    TrangThai,
                    COUNT(*)           AS SoPhieu,
                    SUM(TienChenhLech) AS TongTienChenhLech
                FROM DOITRA
                WHERE NgayYeuCau BETWEEN ? AND ?
                GROUP BY TrangThai
                ORDER BY TrangThai
                """;
        List<Object[]> result = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new RuntimeException("DoiTraDAO.thongKeTheoTrangThai: Không thể kết nối CSDL.");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                            rs.getString("TrangThai"),
                            rs.getInt("SoPhieu"),
                            rs.getBigDecimal("TongTienChenhLech")
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DoiTraDAO.thongKeTheoTrangThai: " + e.getMessage(), e);
        } finally {
            closeConnection(conn);
        }
        return result;
    }

    /**
     * Thống kê số lượng phiếu theo loại đổi trả trong khoảng ngày.
     * Trả về mảng Object[]: [LoaiDoiTra, SoPhieu, TongTienChenhLech]
     */
    public List<Object[]> thongKeTheoLoai(LocalDate tuNgay, LocalDate denNgay) {
        String sql = """
                SELECT
                    LoaiDoiTra,
                    COUNT(*)           AS SoPhieu,
                    SUM(TienChenhLech) AS TongTienChenhLech
                FROM DOITRA
                WHERE NgayYeuCau BETWEEN ? AND ?
                GROUP BY LoaiDoiTra
                ORDER BY SoPhieu DESC
                """;
        List<Object[]> result = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new RuntimeException("DoiTraDAO.thongKeTheoLoai: Không thể kết nối CSDL.");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                            rs.getString("LoaiDoiTra"),
                            rs.getInt("SoPhieu"),
                            rs.getBigDecimal("TongTienChenhLech")
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DoiTraDAO.thongKeTheoLoai: " + e.getMessage(), e);
        } finally {
            closeConnection(conn);
        }
        return result;
    }

    /**
     * Thống kê sản phẩm bị đổi/trả nhiều nhất trong khoảng ngày.
     * Trả về mảng Object[]: [MaSP, TenSP, SoPhieu, TongSoLuongTra]
     */
    public List<Object[]> thongKeSanPhamDoiTraNhieu(LocalDate tuNgay,
                                                     LocalDate denNgay,
                                                     int limit) {
        String sql = """
                SELECT TOP (?)
                    dt.MaSP,
                    sp.TenSP,
                    COUNT(*)            AS SoPhieu,
                    SUM(dt.SoLuongTra)  AS TongSoLuongTra
                FROM DOITRA dt
                JOIN SANPHAM sp ON sp.MaSP = dt.MaSP
                WHERE dt.NgayYeuCau BETWEEN ? AND ?
                GROUP BY dt.MaSP, sp.TenSP
                ORDER BY TongSoLuongTra DESC
                """;
        List<Object[]> result = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new RuntimeException("DoiTraDAO.thongKeSanPhamDoiTraNhieu: Không thể kết nối CSDL.");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit <= 0 ? Integer.MAX_VALUE : limit);
            ps.setDate(2, Date.valueOf(tuNgay));
            ps.setDate(3, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                            rs.getInt("MaSP"),
                            rs.getString("TenSP"),
                            rs.getInt("SoPhieu"),
                            rs.getInt("TongSoLuongTra")
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DoiTraDAO.thongKeSanPhamDoiTraNhieu: " + e.getMessage(), e);
        } finally {
            closeConnection(conn);
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPER METHODS – MAPPING
    // ════════════════════════════════════════════════════════════════════════

    /** Thực thi câu SELECT không tham số và ánh xạ ResultSet sang List<DoiTraDTO>. */
    private List<DoiTraDTO> query(String sql) {
        List<DoiTraDTO> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new RuntimeException("DoiTraDAO.query: Không thể kết nối CSDL.");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("DoiTraDAO.query: " + e.getMessage(), e);
        } finally {
            closeConnection(conn);
        }
        return list;
    }

    /** Thực thi câu SELECT có tham số động. */
    private List<DoiTraDTO> queryWithParams(String sql, Object... params) {
        List<DoiTraDTO> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new RuntimeException("DoiTraDAO.queryWithParams: Không thể kết nối CSDL.");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DoiTraDAO.queryWithParams: " + e.getMessage(), e);
        } finally {
            closeConnection(conn);
        }
        return list;
    }

    /** Ánh xạ một dòng ResultSet sang DoiTraDTO. */
    private DoiTraDTO mapRow(ResultSet rs) throws SQLException {
        DoiTraDTO dto = new DoiTraDTO();

        dto.setMaDoiTra(rs.getInt("MaDoiTra"));
        dto.setMaHoaDon(rs.getInt("MaHoaDon"));
        dto.setMaSP(rs.getInt("MaSP"));
        dto.setMaSerial(rs.getInt("MaSerial"));
        dto.setSoLuongTra(rs.getInt("SoLuongTra"));
        dto.setLoaiDoiTra(rs.getString("LoaiDoiTra"));

        int maSPMoi = rs.getInt("MaSPMoi");
        dto.setMaSPMoi(rs.wasNull() ? null : maSPMoi);

        int maSerialMoi = rs.getInt("MaSerialMoi");
        dto.setMaSerialMoi(rs.wasNull() ? null : maSerialMoi);

        dto.setTienChenhLech(rs.getBigDecimal("TienChenhLech"));
        dto.setLyDo(rs.getString("LyDo"));
        dto.setMaNV(rs.getInt("MaNV"));

        Date ngayYeuCau = rs.getDate("NgayYeuCau");
        dto.setNgayYeuCau(ngayYeuCau != null ? ngayYeuCau.toLocalDate() : null);

        Date ngayXuLy = rs.getDate("NgayXuLy");
        dto.setNgayXuLy(ngayXuLy != null ? ngayXuLy.toLocalDate() : null);

        dto.setTrangThai(rs.getString("TrangThai"));
        dto.setGhiChu(rs.getString("GhiChu"));

        // Display columns
        dto.setTenSP(rs.getString("TenSP"));
        dto.setSerialCode(rs.getString("SerialCode"));
        dto.setTenSPMoi(rs.getString("TenSPMoi"));
        dto.setSerialCodeMoi(rs.getString("SerialCodeMoi"));
        dto.setTenNV(rs.getString("TenNV"));

        return dto;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILITY
    // ════════════════════════════════════════════════════════════════════════

    private java.sql.Date toSqlDate(LocalDate ld) {
        return ld != null ? java.sql.Date.valueOf(ld) : null;
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer value)
            throws SQLException {
        if (value == null) ps.setNull(idx, Types.INTEGER);
        else               ps.setInt(idx, value);
    }

    /**
     * Đóng connection an toàn.
     * DBConnection.getConnection() mở connection mới mỗi lần gọi,
     * nên DAO tự đóng sau mỗi thao tác.
     */
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}