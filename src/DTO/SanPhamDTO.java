package DTO;

import java.math.BigDecimal;

public class SanPhamDTO {

    // =========================================================================
    // 1. CÁC THUỘC TÍNH (FIELDS)
    // =========================================================================
    private int maSP;
    private String tenSP;
    private int maLoai;
    private String thuongHieu;
    private String mauSac;
    private BigDecimal gia;
    private BigDecimal giaGoc;
    private int soLuongTon;
    private int soLuongToiThieu;
    private int soLuongToiDa;
    private int thoiHanBaoHanhThang;
    private String moTa;
    private String trangThai;
    private String hinhAnh; // Lưu tên file hoặc đường dẫn ảnh, VD: "laptop_asus.png"

    // =========================================================================
    // 2. CONSTRUCTORS
    // =========================================================================

    // 2.1. Constructor rỗng (Mặc định cần có cho các Framework hoặc khi tạo object trống)
    public SanPhamDTO() {
    }

    // 2.2. Constructor KHÔNG CÓ hình ảnh (Dùng khi thêm mới sản phẩm chưa kịp up ảnh)
    public SanPhamDTO(int maSP, String tenSP, int maLoai, String thuongHieu, String mauSac,
                      BigDecimal gia, BigDecimal giaGoc, int soLuongTon, int soLuongToiThieu,
                      int soLuongToiDa, int thoiHanBaoHanhThang, String moTa, String trangThai) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.maLoai = maLoai;
        this.thuongHieu = thuongHieu;
        this.mauSac = mauSac;
        this.gia = gia;
        this.giaGoc = giaGoc;
        this.soLuongTon = soLuongTon;
        this.soLuongToiThieu = soLuongToiThieu;
        this.soLuongToiDa = soLuongToiDa;
        this.thoiHanBaoHanhThang = thoiHanBaoHanhThang;
        this.moTa = moTa;
        this.trangThai = trangThai;
    }

    // 2.3. Constructor ĐẦY ĐỦ (Dùng khi lấy dữ liệu từ Database lên hiển thị)
    public SanPhamDTO(int maSP, String tenSP, int maLoai, String thuongHieu, String mauSac,
                      BigDecimal gia, BigDecimal giaGoc, int soLuongTon, int soLuongToiThieu,
                      int soLuongToiDa, int thoiHanBaoHanhThang, String moTa, String trangThai, 
                      String hinhAnh) {
        // Gọi lại constructor 2.2 để tái sử dụng code (Constructor Chaining)
        this(maSP, tenSP, maLoai, thuongHieu, mauSac, gia, giaGoc, soLuongTon, 
             soLuongToiThieu, soLuongToiDa, thoiHanBaoHanhThang, moTa, trangThai);
        this.hinhAnh = hinhAnh; // Chỉ gán thêm thuộc tính hình ảnh
    }

    // =========================================================================
    // 3. GETTER & SETTER (Trình bày dạng Inline cho gọn gàng, dễ đọc)
    // =========================================================================

    public int getMaSP() { return maSP; }
    public void setMaSP(int maSP) { this.maSP = maSP; }

    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    public int getMaLoai() { return maLoai; }
    public void setMaLoai(int maLoai) { this.maLoai = maLoai; }

    public String getThuongHieu() { return thuongHieu; }
    public void setThuongHieu(String thuongHieu) { this.thuongHieu = thuongHieu; }

    public String getMauSac() { return mauSac; }
    public void setMauSac(String mauSac) { this.mauSac = mauSac; }

    public BigDecimal getGia() { return gia; }
    public void setGia(BigDecimal gia) { this.gia = gia; }

    public BigDecimal getGiaGoc() { return giaGoc; }
    public void setGiaGoc(BigDecimal giaGoc) { this.giaGoc = giaGoc; }

    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    public int getSoLuongToiThieu() { return soLuongToiThieu; }
    public void setSoLuongToiThieu(int soLuongToiThieu) { this.soLuongToiThieu = soLuongToiThieu; }

    public int getSoLuongToiDa() { return soLuongToiDa; }
    public void setSoLuongToiDa(int soLuongToiDa) { this.soLuongToiDa = soLuongToiDa; }

    public int getThoiHanBaoHanhThang() { return thoiHanBaoHanhThang; }
    public void setThoiHanBaoHanhThang(int thoiHanBaoHanhThang) { this.thoiHanBaoHanhThang = thoiHanBaoHanhThang; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
}
