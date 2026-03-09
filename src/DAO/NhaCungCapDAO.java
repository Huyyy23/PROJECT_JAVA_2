package DAO;

import DTO.NhaCungCapDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.util.ArrayList;

/**
 * NhaCungCapDAO — Tầng truy cập dữ liệu bảng NHACUNGCAP.
 *
 * Bảng liên quan:
 *   - LAPTOPSTORE.dbo.NHACUNGCAP           : thông tin nhà cung cấp chính
 *   - LAPTOPSTORE.dbo.NHACUNGCAP_SANPHAM   : bảng trung gian NCC ↔ SP
 *
 * Quy ước trạng thái:
 *   - DB lưu dạng chuỗi: "HoatDong" / "NgungHoatDong"
 *   - DTO/BUS dùng số:   1 = HoatDong,   2 = NgungHoatDong
 *   - Mọi chuyển đổi chuỗi ↔ số đều xử lý tại lớp DAO này,
 *     tầng BUS và GUI không cần biết về chuỗi DB.
 */
public class NhaCungCapDAO {

    private final String TABLE_NAME = "LAPTOPSTORE.dbo.NHACUNGCAP";

    // =========================================================================
    // HELPER PRIVATE
    // =========================================================================

    /**
     * Chuyển số trạng thái (int) → chuỗi lưu DB.
     * 1 → "HoatDong",  khác → "NgungHoatDong"
     */
    private String toDBTrangThai(int trangThai) {
        return (trangThai == 1) ? "HoatDong" : "NgungHopTac"; 
    }

    /**
     * Chuyển chuỗi trạng thái từ DB → số nguyên cho DTO.
     * "HoatDong" (không phân biệt hoa thường) → 1,  khác → 2
     */
    private int fromDBTrangThai(String trangThaiChuoi) {
        if (trangThaiChuoi != null
                && trangThaiChuoi.trim().equalsIgnoreCase("HoatDong")) return 1;
        return 2;
    }

    // =========================================================================
    // LẤY DỮ LIỆU
    // =========================================================================

    /**
     * Lấy toàn bộ danh sách nhà cung cấp (cả hoạt động lẫn ngừng).
     * Dùng trong NhaCungCapBUS.getAll() và getDanhSachHoatDong().
     *
     * @return Danh sách tất cả NCC, rỗng nếu lỗi kết nối
     */
    public ArrayList<NhaCungCapDTO> selectAll() {
        ArrayList<NhaCungCapDTO> ketQua = new ArrayList<>();
        Connection con = DBConnection.getConnection();
        if (con == null) {
            System.err.println("LỖI: Không kết nối được SQL Server.");
            return ketQua;
        }
        try {
            String sql = "SELECT * FROM " + TABLE_NAME;
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ketQua.add(new NhaCungCapDTO(
                    rs.getInt("MaNhaCungCap"),
                    rs.getString("TenNhaCungCap"),
                    rs.getString("SoDienThoai"),
                    rs.getString("Email"),
                    rs.getString("DiaChi"),
                    fromDBTrangThai(rs.getString("TrangThai"))
                ));
            }
            rs.close(); pst.close(); con.close();
        } catch (SQLException e) {
            System.err.println("LỖI SQL KHI SELECT: " + e.getMessage());
        }
        return ketQua;
    }

    // =========================================================================
    // THÊM / SỬA / XÓA / ĐỔI TRẠNG THÁI
    // =========================================================================

    /**
     * Thêm nhà cung cấp mới.
     * MaNhaCungCap là IDENTITY — SQL Server tự sinh, không cần truyền.
     *
     * @param ncc  DTO chứa thông tin NCC mới
     * @return true nếu INSERT thành công
     */
    public boolean insert(NhaCungCapDTO ncc) {
        int ketQua = 0;
        Connection con = DBConnection.getConnection();
        if (con == null) return false;
        try {
            String sql = "INSERT INTO " + TABLE_NAME
                       + " (TenNhaCungCap, SoDienThoai, Email, DiaChi, TrangThai)"
                       + " VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, ncc.getTenNCC());
            pst.setString(2, ncc.getSDT());
            pst.setString(3, ncc.getEmail());
            pst.setString(4, ncc.getDiaChi());
            pst.setString(5, toDBTrangThai(ncc.getTrangThai()));
            ketQua = pst.executeUpdate();
            pst.close(); con.close();
        } catch (SQLException e) {
            System.err.println("LỖI SQL KHI INSERT: " + e.getMessage());
        }
        return ketQua > 0;
    }

    /**
     * Cập nhật thông tin NCC (tên, SĐT, email, địa chỉ).
     * Không đổi TrangThai — dùng updateTrangThai() riêng cho trường hợp đó.
     *
     * @param ncc  DTO với thông tin mới (phải có MaNhaCungCap hợp lệ)
     * @return true nếu UPDATE thành công
     */
    public boolean update(NhaCungCapDTO ncc) {
        int ketQua = 0;
        Connection con = DBConnection.getConnection();
        if (con == null) return false;
        try {
            String sql = "UPDATE " + TABLE_NAME
                       + " SET TenNhaCungCap=?, SoDienThoai=?, Email=?, DiaChi=?"
                       + " WHERE MaNhaCungCap=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, ncc.getTenNCC());
            pst.setString(2, ncc.getSDT());
            pst.setString(3, ncc.getEmail());
            pst.setString(4, ncc.getDiaChi());
            pst.setInt(5, ncc.getMaNCC());
            ketQua = pst.executeUpdate();
            pst.close(); con.close();
        } catch (SQLException e) {
            System.err.println("LỖI SQL KHI UPDATE: " + e.getMessage());
        }
        return ketQua > 0;
    }

    /**
     * Đổi trạng thái nhà cung cấp (hoạt động ↔ ngừng).
     * Tách riêng khỏi update() để tránh vô tình ghi đè thông tin khác.
     *
     * @param maNCC         Mã nhà cung cấp
     * @param trangThaiMoi  1 = HoatDong,  2 = NgungHoatDong
     * @return true nếu UPDATE thành công
     */
    public boolean updateTrangThai(int maNCC, int trangThaiMoi) {
        int ketQua = 0;
        Connection con = DBConnection.getConnection();
        if (con == null) return false;
        try {
            String sql = "UPDATE " + TABLE_NAME + " SET TrangThai=? WHERE MaNhaCungCap=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, toDBTrangThai(trangThaiMoi));
            pst.setInt(2, maNCC);
            ketQua = pst.executeUpdate();
            pst.close(); con.close();
        } catch (SQLException e) {
            System.err.println("LỖI SQL KHI UPDATE TRẠNG THÁI: " + e.getMessage());
        }
        return ketQua > 0;
    }

    /**
     * Xóa nhà cung cấp theo mã (DELETE thật — cẩn thận FK).
     * Nếu có ràng buộc khóa ngoại, DB sẽ ném lỗi và trả về false.
     *
     * @param maNCC  Mã nhà cung cấp cần xóa
     * @return true nếu DELETE thành công
     */
    public boolean delete(int maNCC) {
        int ketQua = 0;
        Connection con = DBConnection.getConnection();
        if (con == null) return false;
        try {
            String sql = "DELETE FROM " + TABLE_NAME + " WHERE MaNhaCungCap=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, maNCC);
            ketQua = pst.executeUpdate();
            pst.close(); con.close();
        } catch (SQLException e) {
            System.err.println("LỖI SQL KHI XÓA: " + e.getMessage());
        }
        return ketQua > 0;
    }

    // =========================================================================
    // BẢNG TRUNG GIAN: NHACUNGCAP_SANPHAM
    // =========================================================================

    /**
     * Lấy danh sách MaNhaCungCap đang liên kết với 1 sản phẩm.
     * Chiều: 1 SP → nhiều NCC.
     *
     * Dùng trong:
     *   - NhaCungCapBUS.getDanhSachNccCuaSP() — SanPhamPanel hiển thị NCC của SP
     *
     * @param maSP  Mã sản phẩm cần tra cứu NCC
     * @return Danh sách MaNhaCungCap liên kết với SP đó
     */
    public ArrayList<Integer> getNccByMaSP(int maSP) {
        ArrayList<Integer> list = new ArrayList<>();
        Connection con = DBConnection.getConnection();
        if (con == null) return list;
        try {
            String sql = "SELECT MaNhaCungCap FROM LAPTOPSTORE.dbo.NHACUNGCAP_SANPHAM WHERE MaSP = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, maSP);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(rs.getInt("MaNhaCungCap"));
            rs.close(); pst.close(); con.close();
        } catch (SQLException e) {
            System.err.println("LOI SQL KHI LAY NCC CUA SP: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy danh sách MaSP đang liên kết với 1 nhà cung cấp.
     * Chiều: 1 NCC → nhiều SP (ngược với getNccByMaSP).
     *
     * Dùng trong:
     *   - SanPhamBUS.getSanPhamByNhaCungCap() — lọc SP khi mở popup chọn SP
     *   - SanPhamBUS.kiemTraSPThuocNCC()      — validate SP có thuộc NCC không
     *
     * @param maNCC  Mã nhà cung cấp cần tra cứu SP
     * @return Danh sách MaSP liên kết với NCC đó
     */
    public ArrayList<Integer> getMaSPByNCC(int maNCC) {
        ArrayList<Integer> list = new ArrayList<>();
        Connection con = DBConnection.getConnection();
        if (con == null) return list;
        try {
            String sql = "SELECT MaSP FROM LAPTOPSTORE.dbo.NHACUNGCAP_SANPHAM WHERE MaNhaCungCap = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, maNCC);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(rs.getInt("MaSP"));
            rs.close(); pst.close(); con.close();
        } catch (SQLException e) {
            System.err.println("LOI SQL KHI LAY MaSP CUA NCC: " + e.getMessage());
        }
        return list;
    }

    /**
     * Xóa toàn bộ liên kết NCC của 1 sản phẩm.
     * Thường gọi trước khi link lại NCC mới — tránh duplicate key.
     *
     * Luồng chuẩn khi sửa SP trong SanPhamPanel:
     *   1. unlinkAllNccOfSP(maSP)         — xóa link cũ
     *   2. linkSanPham(maNCC, maSP) x N   — thêm link mới
     *
     * @param maSP  Mã sản phẩm cần xóa toàn bộ liên kết
     * @return true nếu DELETE thành công (kể cả không có row nào để xóa)
     */
    public boolean unlinkAllNccOfSP(int maSP) {
        Connection con = DBConnection.getConnection();
        if (con == null) return false;
        try {
            String sql = "DELETE FROM LAPTOPSTORE.dbo.NHACUNGCAP_SANPHAM WHERE MaSP = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, maSP);
            pst.executeUpdate();
            pst.close(); con.close();
            return true;
        } catch (SQLException e) {
            System.err.println("LOI SQL KHI XOA LINK NCC-SP: " + e.getMessage());
        }
        return false;
    }

    /**
     * Thêm 1 liên kết NCC ↔ SP vào bảng trung gian.
     * Nếu cặp (maNCC, maSP) đã tồn tại, DB sẽ ném lỗi PK trùng.
     * Nên gọi unlinkAllNccOfSP() trước khi link lại nếu không chắc.
     *
     * @param maNCC  Mã nhà cung cấp
     * @param maSP   Mã sản phẩm
     * @return true nếu INSERT thành công
     */
    public boolean linkSanPham(int maNCC, int maSP) {
        Connection con = DBConnection.getConnection();
        if (con == null) return false;
        try {
            String sql = "INSERT INTO LAPTOPSTORE.dbo.NHACUNGCAP_SANPHAM (MaNhaCungCap, MaSP)"
                       + " VALUES (?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, maNCC);
            pst.setInt(2, maSP);
            int kq = pst.executeUpdate();
            pst.close(); con.close();
            return kq > 0;
        } catch (SQLException e) {
            System.err.println("LOI SQL KHI LINK NCC-SP: " + e.getMessage());
        }
        return false;
    }
}