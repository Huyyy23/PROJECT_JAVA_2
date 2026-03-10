package DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DoiTraDTO {

    // ── Khoá chính ───────────────────────────────────────────────────────
    private int maDoiTra;

    // ── Khoá ngoại & dữ liệu cốt lõi ─────────────────────────────────────
    private int    maHoaDon;
    private int    maSP;
    private int    maSerial;
    private int    soLuongTra;
    private String loaiDoiTra;          // DoiSanPham | TraHang | BaoHanh

    private Integer maSPMoi;            // nullable
    private Integer maSerialMoi;        // nullable

    private BigDecimal tienChenhLech;
    private String     lyDo;
    private int        maNV;

    private LocalDate ngayYeuCau;
    private LocalDate ngayXuLy;         // nullable

    private String trangThai;           // ChoDuyet | DangXuLy | TuChoi | HoanThanh
    private String ghiChu;              // nullable

    // ── Trường hiển thị (JOIN) ────────────────────────────────────────────
    private String tenSP;               // SANPHAM.TenSP
    private String serialCode;          // SERIAL.SerialCode  (sản phẩm cũ)
    private String tenSPMoi;            // SANPHAM.TenSP       (sản phẩm mới)
    private String serialCodeMoi;       // SERIAL.SerialCode   (serial mới)
    private String tenNV;               // NHANVIEN.TenNV

    // ════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTORS
    // ════════════════════════════════════════════════════════════════════════

    public DoiTraDTO() {}

    /** Constructor đầy đủ – dùng khi INSERT mới (chưa có MaDoiTra) */
    public DoiTraDTO(int maHoaDon, int maSP, int maSerial, int soLuongTra,
                     String loaiDoiTra, Integer maSPMoi, Integer maSerialMoi,
                     BigDecimal tienChenhLech, String lyDo, int maNV,
                     LocalDate ngayYeuCau, LocalDate ngayXuLy,
                     String trangThai, String ghiChu) {
        this.maHoaDon      = maHoaDon;
        this.maSP          = maSP;
        this.maSerial      = maSerial;
        this.soLuongTra    = soLuongTra;
        this.loaiDoiTra    = loaiDoiTra;
        this.maSPMoi       = maSPMoi;
        this.maSerialMoi   = maSerialMoi;
        this.tienChenhLech = tienChenhLech;
        this.lyDo          = lyDo;
        this.maNV          = maNV;
        this.ngayYeuCau    = ngayYeuCau;
        this.ngayXuLy      = ngayXuLy;
        this.trangThai     = trangThai;
        this.ghiChu        = ghiChu;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  GETTERS & SETTERS
    // ════════════════════════════════════════════════════════════════════════

    public int getMaDoiTra()              { return maDoiTra; }
    public void setMaDoiTra(int v)        { this.maDoiTra = v; }

    public int getMaHoaDon()              { return maHoaDon; }
    public void setMaHoaDon(int v)        { this.maHoaDon = v; }

    public int getMaSP()                  { return maSP; }
    public void setMaSP(int v)            { this.maSP = v; }

    public int getMaSerial()              { return maSerial; }
    public void setMaSerial(int v)        { this.maSerial = v; }

    public int getSoLuongTra()            { return soLuongTra; }
    public void setSoLuongTra(int v)      { this.soLuongTra = v; }

    public String getLoaiDoiTra()         { return loaiDoiTra; }
    public void setLoaiDoiTra(String v)   { this.loaiDoiTra = v; }

    public Integer getMaSPMoi()           { return maSPMoi; }
    public void setMaSPMoi(Integer v)     { this.maSPMoi = v; }

    public Integer getMaSerialMoi()       { return maSerialMoi; }
    public void setMaSerialMoi(Integer v) { this.maSerialMoi = v; }

    public BigDecimal getTienChenhLech()           { return tienChenhLech; }
    public void setTienChenhLech(BigDecimal v)     { this.tienChenhLech = v; }

    public String getLyDo()               { return lyDo; }
    public void setLyDo(String v)         { this.lyDo = v; }

    public int getMaNV()                  { return maNV; }
    public void setMaNV(int v)            { this.maNV = v; }

    public LocalDate getNgayYeuCau()      { return ngayYeuCau; }
    public void setNgayYeuCau(LocalDate v){ this.ngayYeuCau = v; }

    public LocalDate getNgayXuLy()        { return ngayXuLy; }
    public void setNgayXuLy(LocalDate v)  { this.ngayXuLy = v; }

    public String getTrangThai()          { return trangThai; }
    public void setTrangThai(String v)    { this.trangThai = v; }

    public String getGhiChu()             { return ghiChu; }
    public void setGhiChu(String v)       { this.ghiChu = v; }

    // ── Display fields ────────────────────────────────────────────────────
    public String getTenSP()              { return tenSP; }
    public void setTenSP(String v)        { this.tenSP = v; }

    public String getSerialCode()         { return serialCode; }
    public void setSerialCode(String v)   { this.serialCode = v; }

    public String getTenSPMoi()           { return tenSPMoi; }
    public void setTenSPMoi(String v)     { this.tenSPMoi = v; }

    public String getSerialCodeMoi()      { return serialCodeMoi; }
    public void setSerialCodeMoi(String v){ this.serialCodeMoi = v; }

    public String getTenNV()              { return tenNV; }
    public void setTenNV(String v)        { this.tenNV = v; }

    // ════════════════════════════════════════════════════════════════════════
    //  CONSTANTS – giá trị hợp lệ của các cột ENUM trong DB
    // ════════════════════════════════════════════════════════════════════════
    public static final String LOAI_DOI_SAN_PHAM = "DoiSanPham";
    public static final String LOAI_TRA_HANG     = "TraHang";
    public static final String LOAI_BAO_HANH     = "BaoHanh";

    public static final String TRANG_THAI_CHO_DUYET  = "ChoDuyet";
    public static final String TRANG_THAI_DANG_XU_LY = "DangXuLy";
    public static final String TRANG_THAI_TU_CHOI    = "TuChoi";
    public static final String TRANG_THAI_HOAN_THANH = "HoanThanh";

    @Override
    public String toString() {
        return "DoiTraDTO{maDoiTra=" + maDoiTra
                + ", maHoaDon=" + maHoaDon
                + ", tenSP='" + tenSP + '\''
                + ", loaiDoiTra='" + loaiDoiTra + '\''
                + ", trangThai='" + trangThai + '\''
                + ", ngayYeuCau=" + ngayYeuCau + '}';
    }
}