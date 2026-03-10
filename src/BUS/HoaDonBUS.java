package BUS;

import DAO.ChiTietHoaDonDAO;
import DAO.HoaDonDAO;
import DTO.ChiTietHoaDonDTO;
import DTO.HoaDonDTO;
import UTIL.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

public class HoaDonBUS {

    // =========================================================================
    // TẠO HÓA ĐƠN
    // phanTramGiam: % giảm giá (VD: 5.0 = giảm 5%)
    // Trả về MaHoaDon được DB sinh ra, -1 nếu thất bại
    // =========================================================================
    public int taoHoaDon(Integer maKhachHang, int maNV, BigDecimal phanTramGiam) {
        HoaDonDTO dto = new HoaDonDTO(
            maKhachHang,
            maNV,
            phanTramGiam != null ? phanTramGiam : BigDecimal.ZERO,
            null   // ghiChu
        );
        return HoaDonDAO.insert(dto);
    }

    // =========================================================================
    // CẬP NHẬT % GIẢM GIÁ SAU KHI ĐÃ TẠO HÓA ĐƠN
    // Dùng khi biết số tiền giảm tuyệt đối, tính ngược ra %
    // =========================================================================
    public boolean capNhatPhanTramGiam(int maHoaDon, BigDecimal phanTram) {
        return HoaDonDAO.capNhatPhanTramGiam(maHoaDon, phanTram);
    }

    // =========================================================================
    // THÊM SẢN PHẨM VÀO HÓA ĐƠN
    // Tự động lấy MaSerial TrongKho — giải quyết lỗi NOT NULL
    // Ném Exception nếu hết hàng hoặc không tìm được serial
    // =========================================================================
    public void themSPVaoHoaDon(int maHoaDon, int maSP,
                                BigDecimal donGia, int soLuong) throws Exception {

        // Lấy serial TrongKho cho sản phẩm này
        int maSerial = ChiTietHoaDonDAO.laySerialTrongKho(maSP);
        if (maSerial == -1) {
            throw new Exception("Sản phẩm mã " + maSP + " đã hết hàng (không còn serial trong kho)!");
        }

        ChiTietHoaDonDTO ct = new ChiTietHoaDonDTO(
            maHoaDon, maSP, maSerial, soLuong, donGia
        );

        boolean ok = ChiTietHoaDonDAO.insert(ct);
        if (!ok) {
            throw new Exception("Không thể thêm sản phẩm vào hóa đơn (MaSP=" + maSP + ")!");
        }
    }

    // =========================================================================
    // THANH TOÁN HÓA ĐƠN
    // Lưu bản ghi THANHTOAN và cập nhật trạng thái hóa đơn → HoanThanh
    // =========================================================================
    public void thanhToanHoaDon(int maHoaDon, BigDecimal tienGiam,
                                BigDecimal tongThanhToan, String phuongThuc) throws Exception {

        if (tongThanhToan == null || tongThanhToan.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("Tổng thanh toán không hợp lệ!");
        }

        // 1. UPDATE TongTienHang = SUM(ThanhTien) từ CHITIETHOADON
        //    taoHoaDon() INSERT với TongTienHang=0 → bước này điền giá trị thật
        //    Sau đó SQL Server tự tính lại các computed columns:
        //    TienGiamHang / TienTruocVAT / TienVAT / TongThanhToan
        capNhatTongTienHangTuChiTiet(maHoaDon);

        // 2. Cập nhật % giảm giá lên HOADON (nếu có)
        if (tienGiam != null && tienGiam.compareTo(BigDecimal.ZERO) > 0) {
            HoaDonDAO.capNhatPhanTramGiam(maHoaDon, tienGiam);
        }

        // 3. Lưu bản ghi thanh toán vào bảng THANHTOAN
        boolean luuOK = luuThanhToan(maHoaDon, tongThanhToan, phuongThuc);
        if (!luuOK) {
            throw new Exception("Không lưu được thông tin thanh toán!");
        }

        // 4. Cập nhật trạng thái hóa đơn → HoanThanh
        boolean ttOK = HoaDonDAO.updateTrangThai(maHoaDon, "HoanThanh");
        if (!ttOK) {
            throw new Exception("Không cập nhật được trạng thái hóa đơn!");
        }
    }

    // =========================================================================
    // PRIVATE: tổng hợp TongTienHang = SUM(ThanhTien) từ CHITIETHOADON
    // rồi UPDATE vào HOADON — buộc SQL Server tính lại computed columns
    // =========================================================================
    private void capNhatTongTienHangTuChiTiet(int maHoaDon) throws Exception {
        String sql =
            "UPDATE HOADON SET TongTienHang = (" +
            "    SELECT ISNULL(SUM(ThanhTien), 0) FROM CHITIETHOADON WHERE MaHoaDon = ?" +
            ") WHERE MaHoaDon = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            ps.setInt(2, maHoaDon);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new Exception("Không thể cập nhật TongTienHang cho hóa đơn #"
                    + maHoaDon + ": " + e.getMessage());
        }
    }

    // =========================================================================
    // TÌM HOẶC TẠO KHÁCH HÀNG
    // Tìm theo SĐT → nếu chưa có thì tạo mới
    // Trả về MaKhachHang, null nếu SĐT rỗng
    // =========================================================================
    public Integer timHoacTaoKhachHang(String ten, String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) return null;
        sdt = sdt.trim();
        ten = (ten == null || ten.trim().isEmpty()) ? "Khách lẻ" : ten.trim();

        // Tìm khách hàng theo SĐT
        String sqlFind = "SELECT MaKhachHang FROM KHACHHANG WHERE SoDienThoai = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sqlFind)) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("MaKhachHang");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Chưa có → tạo khách hàng mới
        String sqlIns = "INSERT INTO KHACHHANG (TenKhachHang, SoDienThoai) VALUES (?, ?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sqlIns, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ten);
            ps.setString(2, sdt);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // =========================================================================
    // LẤY HÓA ĐƠN THEO MÃ
    // =========================================================================
    public HoaDonDTO timHoaDonTheoMa(int maHoaDon) {
        return HoaDonDAO.getById(maHoaDon);
    }

    // =========================================================================
    // LẤY TẤT CẢ HÓA ĐƠN
    // =========================================================================
    public ArrayList<HoaDonDTO> getDanhSachHoaDon() {
        return HoaDonDAO.getAll();
    }

    // =========================================================================
    // LẤY CHI TIẾT HÓA ĐƠN
    // =========================================================================
    public ArrayList<ChiTietHoaDonDTO> getCTHoaDon(int maHoaDon) {
        return ChiTietHoaDonDAO.getByHoaDon(maHoaDon);
    }

    // =========================================================================
    // HỦY HÓA ĐƠN
    // Trigger DB tự hoàn trả serial và cộng lại SoLuongTon
    // =========================================================================
    public boolean huyHoaDon(int maHoaDon) {
        return HoaDonDAO.updateTrangThai(maHoaDon, "Huy");
    }

    // =========================================================================
    // PRIVATE: lưu bản ghi vào bảng THANHTOAN
    // =========================================================================
    private boolean luuThanhToan(int maHoaDon, BigDecimal soTien, String phuongThuc) {
        // Map tên phương thức từ giao diện → đúng CHECK constraint của DB
        String pt = mapPhuongThuc(phuongThuc);

        String sql = "INSERT INTO THANHTOAN (MaHoaDon, SoTien, PhuongThuc, TrangThai) "
                   + "VALUES (?, ?, ?, N'ThanhCong')";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            ps.setBigDecimal(2, soTien);
            ps.setString(3, pt);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Map tên hiển thị → giá trị CHECK constraint trong DB
    private String mapPhuongThuc(String pt) {
        if (pt == null) return "TienMat";
        switch (pt.trim()) {
            case "Tiền mặt":
            case "TienMat":       return "TienMat";
            case "Chuyển khoản":
            case "ChuyenKhoan":   return "ChuyenKhoan";
            case "Thẻ ngân hàng":
            case "TheNganHang":   return "TheNganHang";
            case "Thẻ tín dụng":
            case "TheTinDung":    return "TheTinDung";
            case "VNPAY":         return "VNPAY";
            case "MoMo":          return "MoMo";
            case "ZaloPay":       return "ZaloPay";
            default:              return "TienMat";
        }
    }

    // =========================================================================
    // BỔ SUNG 08/03/2026 18:13 — PARK ORDER (Lưu Hóa Đơn Tạm)
    // =========================================================================

    // Lưu hóa đơn tạm: tạo HOADON ChoXuLy + insert từng ChiTiet
    // Trigger trg_ChiTietHoaDon_AfterInsert tự trừ kho + đánh DaBan Serial
    // Trả về maHoaDon, ném Exception nếu giỏ rỗng hoặc hết serial
    public int luuHoaDonTam(HoaDonDTO hd,
                            java.util.List<ChiTietHoaDonDTO> chiTietList) throws Exception {
        if (chiTietList == null || chiTietList.isEmpty())
            throw new IllegalArgumentException("Gi\u1ecf h\u00e0ng tr\u1ed1ng, kh\u00f4ng th\u1ec3 l\u01b0u t\u1ea1m!");
        int maHD = HoaDonDAO.insertHoaDonCho(hd);
        if (maHD == -1)
            throw new Exception("Kh\u00f4ng th\u1ec3 t\u1ea1o h\u00f3a \u0111\u01a1n ch\u1edd, vui l\u00f2ng th\u1eed l\u1ea1i!");
        for (ChiTietHoaDonDTO ct : chiTietList) {
            ct.setMaHoaDon(maHD);
            int maSerial = ChiTietHoaDonDAO.laySerialTrongKho(ct.getMaSP());
            if (maSerial == -1)
                throw new Exception("S\u1ea3n ph\u1ea9m m\u00e3 " + ct.getMaSP() + " \u0111\u00e3 h\u1ebft serial trong kho!");
            ct.setMaSerial(maSerial);
            if (!ChiTietHoaDonDAO.insert(ct))
                throw new Exception("Kh\u00f4ng th\u1ec3 th\u00eam chi ti\u1ebft SP m\u00e3 " + ct.getMaSP());
        }
        return maHD;
    }

    // Lấy danh sách đơn chờ, BUS ghép TenNV + TenKhachHang cho GUI hiển thị
    // GUI nhận ArrayList<HoaDonDTO>, tự gọi nhanVienBUS/khachHangBUS để lấy tên
    public ArrayList<HoaDonDTO> getDanhSachHoaDonCho() {
        return HoaDonDAO.getDanhSachHoaDonCho();
    }

    // Lấy chi tiết đơn chờ để load lại giỏ hàng
    public ArrayList<ChiTietHoaDonDTO> getChiTietHoaDonCho(int maHoaDon) {
        return HoaDonDAO.getChiTietByMaHD(maHoaDon);
    }

    // Xóa 1 SP khỏi đơn chờ — chỉ cho phép khi TrangThai = ChoXuLy
    // Trigger trg_ChiTietHoaDon_AfterDelete tự hoàn trả kho + serial
    public void xoaChiTietHoaDonCho(int maChiTiet, String trangThaiHoaDon) throws Exception {
        kiemTraChoPhepChinhSua(trangThaiHoaDon);
        if (!HoaDonDAO.deleteChiTiet(maChiTiet))
            throw new Exception("X\u00f3a chi ti\u1ebft th\u1ea5t b\u1ea1i, vui l\u00f2ng th\u1eed l\u1ea1i!");
    }

    // Xóa toàn bộ chi tiết của 1 hóa đơn (dùng khi sửa đơn chờ)
    // Trigger trg_ChiTietHoaDon_AfterDelete tự hoàn trả kho + serial cho từng dòng
    public void xoaChiTietHoaDon(int maHoaDon) throws Exception {
        java.sql.Connection cn = DBConnection.getConnection();
        try {
            java.sql.PreparedStatement ps = cn.prepareStatement(
                "DELETE FROM CHITIETHOADON WHERE MaHoaDon = ?");
            ps.setInt(1, maHoaDon);
            ps.executeUpdate();
            ps.close();
        } catch (Exception ex) {
            throw new Exception("Kh\u00f4ng th\u1ec3 x\u00f3a chi ti\u1ebft h\u00f3a \u0111\u01a1n #" + maHoaDon + ": " + ex.getMessage());
        }
    }

    // Hủy đơn chờ (ChoXuLy → Huy)
    // Trigger trg_HoaDon_Huy tự hoàn trả serial + kho
    public void huyHoaDonCho(int maHoaDon, String trangThaiHienTai) throws Exception {
        kiemTraChoPhepChinhSua(trangThaiHienTai);
        if (!HoaDonDAO.updateTrangThai(maHoaDon, "Huy"))
            throw new Exception("Kh\u00f4ng th\u1ec3 h\u1ee7y h\u00f3a \u0111\u01a1n #" + maHoaDon);
    }

    // Scheduler daemon — tự hủy đơn chờ quá 24 giờ, chạy mỗi 1 giờ
    // Gọi HoaDonBUS.startScheduler() 1 lần trong LaptopStore constructor
    private static java.util.concurrent.ScheduledExecutorService scheduler;

    public static void startScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HoaDon-Scheduler");
            t.setDaemon(true); return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int n = HoaDonDAO.huyHoaDonQuaHan(24);
                if (n > 0)
                    System.out.println("[Scheduler] \u0110\u00e3 h\u1ee7y " + n + " \u0111\u01a1n ch\u1edd qu\u00e1 24 gi\u1edd.");
            } catch (Exception ex) {
                System.err.println("[Scheduler] L\u1ed7i: " + ex.getMessage());
            }
        }, 0, 1, java.util.concurrent.TimeUnit.HOURS);
    }

    public static void stopScheduler() {
        if (scheduler != null) scheduler.shutdownNow();
    }

    // Chặn mọi thao tác chỉnh sửa nếu hóa đơn không còn ở ChoXuLy
    private void kiemTraChoPhepChinhSua(String trangThai) throws Exception {
        if ("HoanThanh".equals(trangThai))
            throw new IllegalStateException("H\u00f3a \u0111\u01a1n \u0111\u00e3 ho\u00e0n th\u00e0nh, kh\u00f4ng th\u1ec3 ch\u1ec9nh s\u1eeda!");
        if ("Huy".equals(trangThai))
            throw new IllegalStateException("H\u00f3a \u0111\u01a1n \u0111\u00e3 b\u1ecb h\u1ee7y, kh\u00f4ng th\u1ec3 ch\u1ec9nh s\u1eeda!");
        if (!"ChoXuLy".equals(trangThai))
            throw new IllegalStateException("Tr\u1ea1ng th\u00e1i kh\u00f4ng h\u1ee3p l\u1ec7: " + trangThai);
    }

    public void capNhatGhiChuHoaDon(int maHoaDon, String ghiChu) throws Exception {
        if (ghiChu == null || ghiChu.trim().isEmpty()) {
            throw new Exception("Lý do hủy/ghi chú không được để trống!");
        }
        
        boolean ok = HoaDonDAO.capNhatGhiChu(maHoaDon, ghiChu.trim());
        if (!ok) {
            throw new Exception("Không thể cập nhật ghi chú cho hóa đơn #" + maHoaDon);
        }
    }
}
