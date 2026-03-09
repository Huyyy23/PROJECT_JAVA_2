package DAO;

import DTO.SanPhamDTO;
import UTIL.DBConnection; // Import bắt buộc để kết nối DB

import java.sql.*;
import java.util.ArrayList;

/**
 * SanPhamDAO — Tầng truy cập dữ liệu bảng SANPHAM.
 * Quy ước xóa mềm: TrangThai = N'NgungBan' thay vì DELETE thật.
 * Tất cả các SELECT đều lọc WHERE TrangThai <> N'NgungBan' trừ getById().
 */
public class SanPhamDAO {

    // =========================================================================
    // 1. LẤY DỮ LIỆU SẢN PHẨM
    // =========================================================================

    /**
     * Lấy toàn bộ sản phẩm đang bán (loại trừ NgungBan).
     */
    public ArrayList<SanPhamDTO> getDanhSachSanPham() {
        ArrayList<SanPhamDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM SANPHAM WHERE TrangThai <> N'NgungBan'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy 1 sản phẩm theo maSP (bao gồm cả sản phẩm đã NgungBan).
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
     * Lấy danh sách sản phẩm thuộc một nhà cung cấp (Lọc theo SP đang bán).
     * Dùng cho form Chọn sản phẩm nhập hàng.
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

    public boolean kiemTraSPThuocNCC(int maSP, int maNCC) {
        String sql = "SELECT 1 FROM NHACUNGCAP_SANPHAM WHERE MaSP = ? AND MaNhaCungCap = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, maSP);
            ps.setInt(2, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // 2. THÊM / SỬA / XÓA (MỀM)
    // =========================================================================

    public boolean themSanPham(SanPhamDTO sp) {
        String sql = "INSERT INTO SANPHAM(TenSP, MaLoai, ThuongHieu, MauSac, Gia, GiaGoc, "
                   + "SoLuongTon, SoLuongToiThieu, SoLuongToiDa, ThoiHanBaoHanhThang, MoTa, TrangThai, HinhAnh) "
                   + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1,     sp.getTenSP());
            ps.setInt(2,        sp.getMaLoai());
            ps.setString(3,     sp.getThuongHieu());
            ps.setString(4,     sp.getMauSac());
            ps.setBigDecimal(5, sp.getGia());
            ps.setBigDecimal(6, sp.getGiaGoc());
            ps.setInt(7,        sp.getSoLuongTon());
            ps.setInt(8,        sp.getSoLuongToiThieu());
            ps.setInt(9,        sp.getSoLuongToiDa());
            ps.setInt(10,       sp.getThoiHanBaoHanhThang());
            ps.setString(11,    sp.getMoTa());
            ps.setString(12,    sp.getTrangThai());
            ps.setString(13,    sp.getHinhAnh());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean suaSanPham(SanPhamDTO sp) {
        String sql = "UPDATE SANPHAM SET TenSP=?, MaLoai=?, ThuongHieu=?, MauSac=?, Gia=?, GiaGoc=?, "
                   + "SoLuongTon=?, SoLuongToiThieu=?, SoLuongToiDa=?, ThoiHanBaoHanhThang=?, MoTa=?, TrangThai=?, HinhAnh=? "
                   + "WHERE MaSP=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1,     sp.getTenSP());
            ps.setInt(2,        sp.getMaLoai());
            ps.setString(3,     sp.getThuongHieu());
            ps.setString(4,     sp.getMauSac());
            ps.setBigDecimal(5, sp.getGia());
            ps.setBigDecimal(6, sp.getGiaGoc());
            ps.setInt(7,        sp.getSoLuongTon());
            ps.setInt(8,        sp.getSoLuongToiThieu());
            ps.setInt(9,        sp.getSoLuongToiDa());
            ps.setInt(10,       sp.getThoiHanBaoHanhThang());
            ps.setString(11,    sp.getMoTa());
            ps.setString(12,    sp.getTrangThai());
            ps.setString(13,    sp.getHinhAnh());
            ps.setInt(14,       sp.getMaSP());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa mềm: Chuyển trạng thái sang 'NgungBan' để không dính lỗi Khóa Ngoại.
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
    // 3. TÌM KIẾM & HELPER ĐẶC BIỆT
    // =========================================================================

    public ArrayList<SanPhamDTO> timSanPham(String ten) {
        ArrayList<SanPhamDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM SANPHAM WHERE TenSP LIKE ? AND TrangThai <> N'NgungBan'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + ten + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

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

    // =========================================================================
    // 4. PRIVATE UTILS
    // =========================================================================

    /**
     * Hàm map dữ liệu dùng chung để code ngắn gọn hơn.
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
}
