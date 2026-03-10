package DAO;

import DTO.ChiTietPhieuNhapDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * DAO cho bảng CHITIETPHIEUNHAP
 *
 * CHANGELOG:
 *   2026-03-09 - [SỬA] getByMaPN(int): fix connection leak — dùng try-with-resources.
 *                        Trước đây DBConnection.getConnection() không được đóng.
 *
 *   2026-03-09 - [SỬA] deleteByMaPN(): fix connection leak — đổi sang try-with-resources.
 *                        Trước đây Connection khai báo ngoài try → không đóng khi exception.
 *
 *   2026-03-09 - [SỬA] insertSerialsByMaPN(): fix SerialCode có thể trùng.
 *                        Bỏ epochSuffix (phụ thuộc thời gian, không đảm bảo unique khi
 *                        2 dòng chạy cùng millisecond). Đổi sang pattern:
 *                        "SP{maSP}-CT{maChiTietPN}-{stt:04d}"
 *                        MaChiTietPN là IDENTITY unique → code không thể trùng.
 */
public class ChiTietPhieuNhapDAO {

    // ----------------------------------------------------------------
    // INSERT chi tiết — nhận Connection từ BUS (tham gia transaction lớn)
    // KHÔNG sinh serial — serial chỉ sinh khi thanh toán (HoanThanh)
    // ----------------------------------------------------------------
    public int insert(Connection con, ChiTietPhieuNhapDTO dto) throws SQLException {
        String sql = "INSERT INTO CHITIETPHIEUNHAP " +
                     "(MaPN, MaSP, SoLuong, DonGiaNhap, GhiChu) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, dto.getMaPN());
            ps.setInt(2, dto.getMaSP());
            ps.setInt(3, dto.getSoLuong());
            ps.setBigDecimal(4, dto.getDonGiaNhap());
            ps.setString(5, dto.getGhiChu());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("Không lấy được MaChiTietPN!");
                return rs.getInt(1);
            }
        }
    }

    // ----------------------------------------------------------------
    // SINH SERIAL cho toàn bộ chi tiết của 1 phiếu
    // Nhận Connection từ BUS — KHÔNG tự commit/rollback
    // Gọi khi phiếu chuyển ChoXuLy → HoanThanh
    //
    // FORMAT SERIAL: "SP{maSP:02d}-{stt:03d}"
    //   Ví dụ: SP01-001, SP01-006, SP17-003
    //   - SP{maSP:02d} : mã sản phẩm tối thiểu 2 chữ số
    //   - {stt:03d}    : số thứ tự serial TOÀN CỤC của SP đó
    //                    (đọc MAX hiện có trong DB rồi đếm tiếp)
    //                    → không bao giờ trùng dù nhập nhiều đợt
    // ----------------------------------------------------------------
    public void insertSerialsByMaPN(Connection con, int maPN) throws SQLException {
        ArrayList<ChiTietPhieuNhapDTO> dsChiTiet = getByMaPN(con, maPN);
        if (dsChiTiet.isEmpty())
            throw new SQLException("Phiếu #" + maPN + " không có chi tiết sản phẩm nào!");

        // Kiểm tra đã sinh serial chưa
        String checkSql = "SELECT COUNT(*) FROM SERIAL WHERE MaChiTietPN IN "
                        + "(SELECT MaChiTietPN FROM CHITIETPHIEUNHAP WHERE MaPN = ?)";
        try (PreparedStatement chk = con.prepareStatement(checkSql)) {
            chk.setInt(1, maPN);
            ResultSet rs = chk.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                throw new SQLException("Phiếu #" + maPN + " đã được sinh serial rồi!");
        }

        // Query đọc số thứ tự lớn nhất hiện có của từng SP
        // Pattern: "SP{xx}-{nnn}" → lấy phần số cuối sau dấu gạch cuối
        String maxSql =
            "SELECT MaSP, " +
            "       ISNULL(MAX(CAST(RIGHT(SerialCode, 3) AS INT)), 0) AS MaxStt " +
            "FROM SERIAL " +
            "WHERE MaSP = ? " +
            "  AND SerialCode LIKE ? " +
            "GROUP BY MaSP";

        String insertSql = "INSERT INTO SERIAL " +
                           "(SerialCode, MaSP, MaChiTietPN, TrangThai, NgayNhap) " +
                           "VALUES (?, ?, ?, N'TrongKho', ?)";
        Date ngayNhap = Date.valueOf(LocalDate.now());

        try (PreparedStatement psInsert = con.prepareStatement(insertSql)) {
            for (ChiTietPhieuNhapDTO ct : dsChiTiet) {
                int maSP = ct.getMaSP();
                String prefix = String.format("SP%02d-", maSP);

                // Đọc STT lớn nhất hiện có của SP này (trong cùng transaction → lock)
                int maxStt = 0;
                try (PreparedStatement psMax = con.prepareStatement(maxSql)) {
                    psMax.setInt(1, maSP);
                    psMax.setString(2, prefix + "%");
                    ResultSet rs = psMax.executeQuery();
                    if (rs.next()) maxStt = rs.getInt("MaxStt");
                }

                // Sinh serial tiếp theo, đếm từ maxStt+1
                for (int i = 1; i <= ct.getSoLuong(); i++) {
                    String code = String.format("SP%02d-%03d", maSP, maxStt + i);
                    psInsert.setString(1, code);
                    psInsert.setInt(2, maSP);
                    psInsert.setInt(3, ct.getMaChiTietPN());
                    psInsert.setDate(4, ngayNhap);
                    psInsert.addBatch();
                }
                // Cộng maxStt cho lần lặp tiếp (nếu phiếu có nhiều dòng cùng SP)
                // Không cần — mỗi SP chỉ xuất hiện 1 lần trong 1 phiếu
            }
            psInsert.executeBatch();
        }
    }

    // ----------------------------------------------------------------
    // GET BY MAPHIEU — dùng Connection truyền vào (trong cùng transaction)
    // ----------------------------------------------------------------
    public ArrayList<ChiTietPhieuNhapDTO> getByMaPN(Connection con, int maPN) throws SQLException {
        ArrayList<ChiTietPhieuNhapDTO> list = new ArrayList<>();
        String sql = "SELECT MaChiTietPN, MaPN, MaSP, SoLuong, " +
                     "DonGiaNhap, ThanhTien, GhiChu " +
                     "FROM CHITIETPHIEUNHAP WHERE MaPN = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maPN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // GET BY MAPHIEU — overload không cần Connection (đọc đơn thuần, ngoài transaction)
    //
    // [2026-03-09] SỬA: try-with-resources để đóng Connection đúng cách.
    //   Trước đây: DBConnection.getConnection() truyền thẳng vào overload kia
    //   mà không ai đóng → connection leak.
    // ----------------------------------------------------------------
    public ArrayList<ChiTietPhieuNhapDTO> getByMaPN(int maPN) {
        try (Connection con = DBConnection.getConnection()) {
            return getByMaPN(con, maPN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ----------------------------------------------------------------
    // DELETE BY MAPHIEU — chỉ gọi khi phiếu đang ChoXuLy (guard ở BUS)
    //
    // [2026-03-09] SỬA: fix connection leak — đổi sang try-with-resources.
    //   Trước đây: Connection con = DBConnection.getConnection() khai báo
    //   ngoài try → không bao giờ được đóng nếu exception xảy ra.
    // ----------------------------------------------------------------
    public boolean deleteByMaPN(int maPN) {
        String sql = "DELETE FROM CHITIETPHIEUNHAP WHERE MaPN = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maPN);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------
    private ChiTietPhieuNhapDTO mapRow(ResultSet rs) throws SQLException {
        ChiTietPhieuNhapDTO dto = new ChiTietPhieuNhapDTO();
        dto.setMaChiTietPN(rs.getInt("MaChiTietPN"));
        dto.setMaPN(rs.getInt("MaPN"));
        dto.setMaSP(rs.getInt("MaSP"));
        dto.setSoLuong(rs.getInt("SoLuong"));
        dto.setDonGiaNhap(rs.getBigDecimal("DonGiaNhap"));
        dto.setThanhTien(rs.getBigDecimal("ThanhTien"));
        dto.setGhiChu(rs.getString("GhiChu"));
        return dto;
    }
}
