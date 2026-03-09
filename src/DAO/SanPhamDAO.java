package DAO;

import DTO.SanPhamDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.util.ArrayList;

/**
 * SanPhamDAO — Tầng truy cập dữ liệu bảng SANPHAM.
 *
 * Bảng liên quan:
 *   - SANPHAM               : thông tin sản phẩm chính
 *   - NHACUNGCAP_SANPHAM    : bảng trung gian NCC ↔ SP (JOIN để lọc/kiểm tra)
 *
 * Quy ước xóa mềm: TrangThai = N'NgungBan' thay vì DELETE thật.
 * Tất cả các SELECT đều lọc WHERE TrangThai <> N'NgungBan' trừ getById().
 */
public class SanPhamDAO {

    // =========================================================================
    // HELPER PRIVATE
    // =========================================================================

    /**
     * Map một hàng ResultSet → SanPhamDTO.
     * Dùng chung cho mọi hàm SELECT để tránh lặp code.
     */
    private SanPhamDTO mapRow(ResultSet rs) throws SQLException {
        SanPhamDTO sp = new SanPhamDTO();
        sp.setMaSP(rs.getInt("MaSP"));
        sp.setTenSP(rs.getString("TenSP"));
        sp.setMaLoai(rs.getInt("MaLoai"));
        sp.setThuongHieu(rs.getString("ThuongHieu"));
        sp.setMauSac(rs.getString("MauSac"));
        sp.setGia(rs.getBigDecimal("Gia"));
        sp.setGiaGoc(rs.getBigDecimal("GiaGoc"));
        sp.setSoLuongTon(rs.getInt("SoLuongTon"));
        sp.setSoLuongToiThieu(rs.getInt("SoLuongToiThieu"));
        sp.setSoLuongToiDa(rs.getInt("SoLuongToiDa"));
        sp.setThoiHanBaoHanhThang(rs.getInt("ThoiHanBaoHanhThang"));
        sp.setMoTa(rs.getString("MoTa"));
        sp.setTrangThai(rs.getString("TrangThai"));
        sp.setHinhAnh(rs.getString("HinhAnh"));
        return sp;
    }

    // =========================================================================
    // LẤY DỮ LIỆU
    // =========================================================================

    /**
     * Lấy toàn bộ sản phẩm đang bán (loại trừ NgungBan).
     * Dùng trong SanPhamBUS.getDanhSachSanPham() để load cache ban đầu.
     */
    public ArrayList<SanPhamDTO> getDanhSachSanPham() {
        ArrayList<SanPhamDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM SANPHAM WHERE TrangThai <> N'NgungBan'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy 1 sản phẩm theo maSP (bao gồm cả NgungBan).
     * Dùng trong SanPhamBUS.timTheoMa() khi cache miss.
     *
     * @param maSP  Mã sản phẩm cần lấy
     * @return SanPhamDTO nếu tìm thấy, null nếu không tồn tại
     */
    public SanPhamDTO getById(int maSP) {
        String sql = "SELECT * FROM SANPHAM WHERE MaSP = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách sản phẩm thuộc một nhà cung cấp (JOIN NHACUNGCAP_SANPHAM).
     * Chỉ lấy SP đang bán (TrangThai <> NgungBan), sắp xếp theo tên SP.
     *
     * Dùng trong:
     *   - NhapHangPanelGUI: popup "Chọn sản phẩm" — chỉ hiện SP của NCC đang chọn
     *   - SanPhamBUS.getSanPhamByNhaCungCap()
     *
     * @param maNCC  Mã nhà cung cấp
     * @return Danh sách SanPhamDTO thuộc NCC đó
     */
    public ArrayList<SanPhamDTO> getSanPhamByNhaCungCap(int maNCC) {
        ArrayList<SanPhamDTO> result = new ArrayList<>();
        String sql = "SELECT sp.* FROM SANPHAM sp "
                   + "JOIN NHACUNGCAP_SANPHAM ns ON sp.MaSP = ns.MaSP "
                   + "WHERE ns.MaNhaCungCap = ? AND sp.TrangThai <> N'NgungBan' "
                   + "ORDER BY sp.TenSP";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Kiểm tra sản phẩm có thuộc nhà cung cấp không.
     * Query trực tiếp bảng NHACUNGCAP_SANPHAM — không qua cache.
     *
     * Dùng trong SanPhamBUS.kiemTraSPThuocNCC() — lớp bảo vệ cuối
     * trước khi thêm dòng vào phiếu nhập.
     *
     * @param maSP   Mã sản phẩm
     * @param maNCC  Mã nhà cung cấp
     * @return true nếu có liên kết, false nếu không
     */
    public boolean kiemTraSPThuocNCC(int maSP, int maNCC) {
        String sql = "SELECT 1 FROM NHACUNGCAP_SANPHAM WHERE MaSP = ? AND MaNhaCungCap = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maSP);
            ps.setInt(2, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // có ít nhất 1 row = tồn tại liên kết
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // THÊM / SỬA / XÓA
    // =========================================================================

    /**
     * Thêm sản phẩm mới vào DB.
     * MaSP là IDENTITY — SQL Server tự sinh, không cần truyền.
     *
     * @param sp  DTO chứa đầy đủ thông tin SP
     * @return true nếu INSERT thành công
     */
    public boolean themSanPham(SanPhamDTO sp) {
        String sql = "INSERT INTO SANPHAM("
                   + "TenSP, MaLoai, ThuongHieu, MauSac, Gia, GiaGoc, "
                   + "SoLuongTon, SoLuongToiThieu, SoLuongToiDa, "
                   + "ThoiHanBaoHanhThang, MoTa, TrangThai, HinhAnh"
                   + ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1,    sp.getTenSP());
            ps.setInt(2,       sp.getMaLoai());
            ps.setString(3,    sp.getThuongHieu());
            ps.setString(4,    sp.getMauSac());
            ps.setBigDecimal(5, sp.getGia());
            ps.setBigDecimal(6, sp.getGiaGoc());
            ps.setInt(7,       sp.getSoLuongTon());
            ps.setInt(8,       sp.getSoLuongToiThieu());
            ps.setInt(9,       sp.getSoLuongToiDa());
            ps.setInt(10,      sp.getThoiHanBaoHanhThang());
            ps.setString(11,   sp.getMoTa());
            ps.setString(12,   sp.getTrangThai());
            ps.setString(13,   sp.getHinhAnh());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật toàn bộ thông tin sản phẩm (trừ MaSP).
     *
     * @param sp  DTO chứa MaSP + thông tin mới
     * @return true nếu UPDATE thành công
     */
    public boolean suaSanPham(SanPhamDTO sp) {
        String sql = "UPDATE SANPHAM SET "
                   + "TenSP=?, MaLoai=?, ThuongHieu=?, MauSac=?, Gia=?, GiaGoc=?, "
                   + "SoLuongTon=?, SoLuongToiThieu=?, SoLuongToiDa=?, "
                   + "ThoiHanBaoHanhThang=?, MoTa=?, TrangThai=?, HinhAnh=? "
                   + "WHERE MaSP=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1,    sp.getTenSP());
            ps.setInt(2,       sp.getMaLoai());
            ps.setString(3,    sp.getThuongHieu());
            ps.setString(4,    sp.getMauSac());
            ps.setBigDecimal(5, sp.getGia());
            ps.setBigDecimal(6, sp.getGiaGoc());
            ps.setInt(7,       sp.getSoLuongTon());
            ps.setInt(8,       sp.getSoLuongToiThieu());
            ps.setInt(9,       sp.getSoLuongToiDa());
            ps.setInt(10,      sp.getThoiHanBaoHanhThang());
            ps.setString(11,   sp.getMoTa());
            ps.setString(12,   sp.getTrangThai());
            ps.setString(13,   sp.getHinhAnh());
            ps.setInt(14,      sp.getMaSP());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa mềm sản phẩm: đổi TrangThai = N'NgungBan' thay vì DELETE thật.
     * Giữ lại lịch sử bán hàng / phiếu nhập liên quan.
     *
     * @param maSP  Mã sản phẩm cần xóa
     * @return true nếu UPDATE thành công
     */
    public boolean xoaSanPham(int maSP) {
        String sql = "UPDATE SANPHAM SET TrangThai = N'NgungBan' WHERE MaSP=?";
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
    // TÌM KIẾM
    // =========================================================================

    /**
     * Tìm sản phẩm theo tên (LIKE %ten%).
     * Chỉ trả về SP đang bán (loại trừ NgungBan).
     *
     * @param ten  Chuỗi tìm kiếm (không phân biệt hoa thường do LIKE)
     * @return Danh sách SP khớp tên
     */
    public ArrayList<SanPhamDTO> timSanPham(String ten) {
        ArrayList<SanPhamDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM SANPHAM WHERE TenSP LIKE ? AND TrangThai <> N'NgungBan'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + ten + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // CÁC HELPER ĐẶC BIỆT
    // =========================================================================

    /**
     * Cập nhật chỉ cột HinhAnh mà không đụng các cột khác.
     * Dùng sau khi upload/chọn ảnh mới trong SanPhamPanel.
     *
     * @param maSP     Mã sản phẩm
     * @param hinhAnh  Đường dẫn / tên file ảnh mới
     * @return true nếu UPDATE thành công
     */
    public boolean updateHinhAnh(int maSP, String hinhAnh) {
        String sql = "UPDATE SANPHAM SET HinhAnh = ? WHERE MaSP = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hinhAnh);
            ps.setInt(2, maSP);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy MaSP lớn nhất hiện tại (= SP vừa INSERT, vì MaSP là IDENTITY tăng dần).
     * Dùng sau themSanPham() để biết MaSP mới sinh ra là bao nhiêu
     * — phục vụ link NCC vào bảng NHACUNGCAP_SANPHAM ngay sau đó.
     *
     * LƯU Ý: Có race condition nếu có nhiều NV thêm SP cùng lúc.
     * Nên dùng OUTPUT INSERTED.MaSP trong themSanPham() nếu cần an toàn hơn.
     *
     * @return MaSP lớn nhất, hoặc -1 nếu lỗi
     */
    public int getMaSPMoiNhat() {
        String sql = "SELECT TOP 1 MaSP FROM SANPHAM ORDER BY MaSP DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("MaSP");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}