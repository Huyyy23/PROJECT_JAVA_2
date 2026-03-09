USE master
GO

CREATE DATABASE LAPTOPSTORE
GO

USE LAPTOPSTORE
GO

-- =================================================================
-- PHẦN 1: TẠO BẢNG
-- =================================================================

-- -----------------------------------------------------------------
-- 1. LOAISANPHAM
-- -----------------------------------------------------------------
CREATE TABLE LOAISANPHAM (
    MaLoai  INT           PRIMARY KEY IDENTITY(1,1),
    TenLoai NVARCHAR(100) NOT NULL,
    MoTa    NVARCHAR(255)
);
GO

-- -----------------------------------------------------------------
-- 2. SANPHAM
-- -----------------------------------------------------------------
CREATE TABLE SANPHAM (
    MaSP                INT           PRIMARY KEY IDENTITY(1,1),
    TenSP               NVARCHAR(150) NOT NULL,
    MaLoai              INT           NOT NULL,
    ThuongHieu          NVARCHAR(100),
    MauSac              NVARCHAR(50),
    Gia                 DECIMAL(18,2) NOT NULL CHECK (Gia >= 0),
    GiaGoc              DECIMAL(18,2),
    SoLuongTon          INT           DEFAULT 0 CHECK (SoLuongTon >= 0),
    SoLuongToiThieu     INT           DEFAULT 0,
    SoLuongToiDa        INT           DEFAULT 0,
    ThoiHanBaoHanhThang INT           DEFAULT 0,
    MoTa                NVARCHAR(1000),
    HinhAnh             NVARCHAR(500) NULL,
    TrangThai           NVARCHAR(20)  DEFAULT N'DangBan'
                        CHECK (TrangThai IN (N'DangBan', N'NgungBan', N'HetHang')),
    FOREIGN KEY (MaLoai) REFERENCES LOAISANPHAM(MaLoai)
);
GO

-- -----------------------------------------------------------------
-- 3. THONGSOKYTHUAT
-- -----------------------------------------------------------------
CREATE TABLE THONGSOKYTHUAT (
    MaThongSo  INT           PRIMARY KEY IDENTITY(1,1),
    MaSP       INT           UNIQUE NOT NULL,
    CPU        NVARCHAR(100),
    RAM        NVARCHAR(50),
    OCung      NVARCHAR(100),
    ManHinh    NVARCHAR(100),
    VGA        NVARCHAR(100),
    HeDieuHanh NVARCHAR(100),
    Pin        NVARCHAR(100),
    TrongLuong NVARCHAR(50),
    KetNoi     NVARCHAR(200),
    FOREIGN KEY (MaSP) REFERENCES SANPHAM(MaSP)
);
GO

-- -----------------------------------------------------------------
-- 4. NHANVIEN
-- -----------------------------------------------------------------
CREATE TABLE NHANVIEN (
    MaNV        INT           PRIMARY KEY IDENTITY(1,1),
    TenNV       NVARCHAR(150) NOT NULL,
    SoDienThoai VARCHAR(20)   UNIQUE,
    Email       NVARCHAR(150),
    DiaChi      NVARCHAR(255),
    NgaySinh    DATE,
    NgayVaoLam  DATE          DEFAULT GETDATE(),
    CCCD        VARCHAR(12),
    GioiTinh    NVARCHAR(10)  CHECK (GioiTinh IN (N'Nam', N'Nu')),
    VaiTro      NVARCHAR(20)  NOT NULL
                CHECK (VaiTro IN (N'QuanLy', N'NhanVienBanHang')),
    TrangThai   NVARCHAR(20)  DEFAULT N'DangLam'
                CHECK (TrangThai IN (N'DangLam', N'NghiViec'))
);
GO

-- -----------------------------------------------------------------
-- 5. TAIKHOAN
-- -----------------------------------------------------------------
CREATE TABLE TAIKHOAN (
    MaTaiKhoan      INT           PRIMARY KEY IDENTITY(1,1),
    MaNV            INT           UNIQUE NOT NULL,
    TenDangNhap     NVARCHAR(50)  UNIQUE NOT NULL,
    MatKhauHash     NVARCHAR(255) NOT NULL,
    TrangThai       NVARCHAR(20)  DEFAULT N'HoatDong'
                    CHECK (TrangThai IN (N'HoatDong', N'KhoaTam', N'Huy')),
    LanDangNhapCuoi DATETIME      NULL,
    FOREIGN KEY (MaNV) REFERENCES NHANVIEN(MaNV)
);
GO

-- -----------------------------------------------------------------
-- 6. NHACUNGCAP
-- -----------------------------------------------------------------
CREATE TABLE NHACUNGCAP (
    MaNhaCungCap  INT           PRIMARY KEY IDENTITY(1,1),
    TenNhaCungCap NVARCHAR(150) NOT NULL,
    SoDienThoai   VARCHAR(20),
    Email         NVARCHAR(150),
    DiaChi        NVARCHAR(255),
    TrangThai     NVARCHAR(20)  DEFAULT N'HoatDong'
                  CHECK (TrangThai IN (N'HoatDong', N'NgungHopTac'))
);
GO

-- -----------------------------------------------------------------
-- 7. PHIEUNHAP
-- -----------------------------------------------------------------
CREATE TABLE PHIEUNHAP (
    MaPN         INT           PRIMARY KEY IDENTITY(1,1),
    MaNhaCungCap INT           NOT NULL,
    MaNV         INT           NOT NULL,
    NgayNhap     DATE          DEFAULT GETDATE(),
    TongTien     DECIMAL(18,2),
    GhiChu       NVARCHAR(500),
    TrangThai    NVARCHAR(20)  DEFAULT N'HoanThanh'
                 CHECK (TrangThai IN (N'HoanThanh', N'Huy')),
    FOREIGN KEY (MaNhaCungCap) REFERENCES NHACUNGCAP(MaNhaCungCap),
    FOREIGN KEY (MaNV)         REFERENCES NHANVIEN(MaNV)
);
GO

-- -----------------------------------------------------------------
-- 8. CHITIETPHIEUNHAP
-- -----------------------------------------------------------------
CREATE TABLE CHITIETPHIEUNHAP (
    MaChiTietPN INT           PRIMARY KEY IDENTITY(1,1),
    MaPN        INT           NOT NULL,
    MaSP        INT           NOT NULL,
    SoLuong     INT           NOT NULL CHECK (SoLuong > 0),
    DonGiaNhap  DECIMAL(18,2) NOT NULL CHECK (DonGiaNhap >= 0),
    ThanhTien   AS (SoLuong * DonGiaNhap) PERSISTED,
    GhiChu      NVARCHAR(255),
    FOREIGN KEY (MaPN) REFERENCES PHIEUNHAP(MaPN),
    FOREIGN KEY (MaSP) REFERENCES SANPHAM(MaSP)
);
GO

-- -----------------------------------------------------------------
-- 9. SERIAL  (quản lý từng đơn vị sản phẩm theo số serial)
-- -----------------------------------------------------------------
CREATE TABLE SERIAL (
    MaSerial    INT          PRIMARY KEY IDENTITY(1,1),
    SerialCode  VARCHAR(50)  UNIQUE NOT NULL,
    MaSP        INT          NOT NULL,
    MaChiTietPN INT          NULL,
    TrangThai   NVARCHAR(20) DEFAULT N'TrongKho'
                CHECK (TrangThai IN (
                    N'TrongKho', N'DaBan', N'BaoHanh', N'DoiTra', N'Loi'
                )),
    NgayNhap    DATE,
    NgayXuat    DATE,
    FOREIGN KEY (MaSP)        REFERENCES SANPHAM(MaSP),
    FOREIGN KEY (MaChiTietPN) REFERENCES CHITIETPHIEUNHAP(MaChiTietPN)
);
GO

-- -----------------------------------------------------------------
-- 10. KHACHHANG
--
-- Hạng tự động tính theo DiemTichLuy (computed column PERSISTED):
--   VoHan    :     0 –   99 điểm →  0% giảm
--   Dong     :   100 –  499 điểm →  3% giảm
--   Bac      :   500 – 1999 điểm →  5% giảm
--   Vang     :  2000 – 4999 điểm →  8% giảm
--   KimCuong :  5000+       điểm → 12% giảm
--
-- Quy đổi điểm: mỗi 100,000đ TongThanhToan = 1 điểm (cộng qua trigger)
-- -----------------------------------------------------------------
CREATE TABLE KHACHHANG (
    MaKhachHang   INT           PRIMARY KEY IDENTITY(1,1),
    TenKhachHang  NVARCHAR(150),
    SoDienThoai   VARCHAR(20)   UNIQUE,
    Email         NVARCHAR(150),
    DiaChi        NVARCHAR(255),
    NgaySinh      DATE,
    GioiTinh      NVARCHAR(10)  CHECK (GioiTinh IN (N'Nam', N'Nu', N'Khac')),
    DiemTichLuy   INT           DEFAULT 0 CHECK (DiemTichLuy >= 0),

    HangKhachHang AS (
        CASE
            WHEN DiemTichLuy >= 2000 THEN N'KimCuong'
            WHEN DiemTichLuy >= 1000 THEN N'Vang'
            WHEN DiemTichLuy >=  400 THEN N'Bac'
            WHEN DiemTichLuy >=  150 THEN N'Dong'
            ELSE                          N'VoHang'
        END
    ) PERSISTED,

    PhanTramGiam AS (
        CASE
            WHEN DiemTichLuy >= 2000 THEN CAST(12 AS DECIMAL(5,2)) -- Kim cương
            WHEN DiemTichLuy >= 1000 THEN CAST(8 AS DECIMAL(5,2))  -- Vàng
            WHEN DiemTichLuy >=  400 THEN CAST(5 AS DECIMAL(5,2))  -- Bạc
            WHEN DiemTichLuy >=  150 THEN CAST(2 AS DECIMAL(5,2))  -- Đồng
            ELSE                          CAST(0 AS DECIMAL(5,2))  -- Vô hạng
        END
    ) PERSISTED,

    NgayDangKy DATE DEFAULT GETDATE()
);
GO

-- -----------------------------------------------------------------
-- 11. HOADON
--
-- PhanTramGiamHang : lưu lại % giảm tại thời điểm bán
--                    (snapshot từ KH.PhanTramGiam, không thay đổi sau)
-- TienGiamHang     : tính tự động = TongTienHang * PhanTramGiamHang / 100
-- TongThanhToan    : (TongTienHang - TienGiamHang) * 1.10  (VAT 10%)
-- -----------------------------------------------------------------
CREATE TABLE HOADON (
    MaHoaDon         INT           PRIMARY KEY IDENTITY(1,1),
    MaKhachHang      INT,
    MaNV             INT           NOT NULL,
    NgayLap          DATETIME      DEFAULT GETDATE(),
    TongTienHang     DECIMAL(18,2) DEFAULT 0,
    PhanTramGiamHang DECIMAL(5,2)  DEFAULT 0,
    TienGiamHang     AS (TongTienHang * PhanTramGiamHang / 100) PERSISTED,
    TienTruocVAT     AS (TongTienHang - TongTienHang * PhanTramGiamHang / 100) PERSISTED,
    TienVAT          AS ((TongTienHang - TongTienHang * PhanTramGiamHang / 100) * 0.10) PERSISTED,
    TongThanhToan    AS ((TongTienHang - TongTienHang * PhanTramGiamHang / 100) * 1.10) PERSISTED,
    GhiChu           NVARCHAR(500),
    TrangThai        NVARCHAR(20)  DEFAULT N'HoanThanh'
                     CHECK (TrangThai IN (N'HoanThanh', N'Huy', N'ChoXuLy')),
    FOREIGN KEY (MaKhachHang) REFERENCES KHACHHANG(MaKhachHang),
    FOREIGN KEY (MaNV)        REFERENCES NHANVIEN(MaNV)
);
GO

-- -----------------------------------------------------------------
-- 12. CHITIETHOADON
-- -----------------------------------------------------------------
CREATE TABLE CHITIETHOADON (
    MaChiTiet INT           PRIMARY KEY IDENTITY(1,1),
    MaHoaDon  INT           NOT NULL,
    MaSP      INT           NOT NULL,
    MaSerial  INT           NOT NULL,
    SoLuong   INT           DEFAULT 1 CHECK (SoLuong > 0),
    DonGia    DECIMAL(18,2) NOT NULL CHECK (DonGia >= 0),
    ThanhTien AS (SoLuong * DonGia) PERSISTED,
    FOREIGN KEY (MaHoaDon) REFERENCES HOADON(MaHoaDon),
    FOREIGN KEY (MaSP)     REFERENCES SANPHAM(MaSP),
    FOREIGN KEY (MaSerial) REFERENCES SERIAL(MaSerial)
);
GO

-- -----------------------------------------------------------------
-- 13. THANHTOAN
-- -----------------------------------------------------------------
CREATE TABLE THANHTOAN (
    MaThanhToan   INT           PRIMARY KEY IDENTITY(1,1),
    MaHoaDon      INT           NOT NULL,
    NgayThanhToan DATETIME      DEFAULT GETDATE(),
    SoTien        DECIMAL(18,2) NOT NULL CHECK (SoTien > 0),
    PhuongThuc    NVARCHAR(50)  NOT NULL
                  CHECK (PhuongThuc IN (
                      N'TienMat', N'ChuyenKhoan', N'TheNganHang',
                      N'TheTinDung', N'VNPAY', N'MoMo', N'ZaloPay'
                  )),
    TrangThai     NVARCHAR(20)  DEFAULT N'ThanhCong'
                  CHECK (TrangThai IN (N'ThanhCong', N'ThatBai', N'ChoXuLy')),
    GhiChu        NVARCHAR(255),
    FOREIGN KEY (MaHoaDon) REFERENCES HOADON(MaHoaDon)
);
GO

-- -----------------------------------------------------------------
-- 14. BAOHANH
-- -----------------------------------------------------------------
CREATE TABLE BAOHANH (
    MaBaoHanh      INT           PRIMARY KEY IDENTITY(1,1),
    MaSerial       INT           NOT NULL,
    MaSP           INT           NOT NULL,
    MaHoaDon       INT           NOT NULL,
    MaNVTiepNhan   INT,
    MaNVXuLy       INT,
    NgayTiepNhan   DATE          DEFAULT GETDATE(),
    NgayHenTra     DATE,
    NgayTra        DATE,
    MoTaLoi        NVARCHAR(500),
    HinhThucXuLy   NVARCHAR(20)
                   CHECK (HinhThucXuLy IN (
                       N'SuaChuaTaiCho', N'GuiHang', N'ThayTheMoi'
                   )),
    KetQuaXuLy     NVARCHAR(500),
    ChiPhiPhatSinh DECIMAL(18,2) DEFAULT 0,
    TrangThai      NVARCHAR(20)  DEFAULT N'DangXuLy'
                   CHECK (TrangThai IN (
                       N'DangXuLy', N'DaGuiHang', N'ChoLinhKien', N'DaTraKhach'
                   )),
    FOREIGN KEY (MaSerial)     REFERENCES SERIAL(MaSerial),
    FOREIGN KEY (MaSP)         REFERENCES SANPHAM(MaSP),
    FOREIGN KEY (MaHoaDon)     REFERENCES HOADON(MaHoaDon),
    FOREIGN KEY (MaNVTiepNhan) REFERENCES NHANVIEN(MaNV),
    FOREIGN KEY (MaNVXuLy)     REFERENCES NHANVIEN(MaNV)
);
GO

-- -----------------------------------------------------------------
-- 15. DOITRA
-- -----------------------------------------------------------------
CREATE TABLE DOITRA (
    MaDoiTra   INT           PRIMARY KEY IDENTITY(1,1),
    MaHoaDon   INT           NOT NULL,
    MaSP       INT           NOT NULL,
    MaSerial   INT           NOT NULL,
    SoLuongTra INT           DEFAULT 1 CHECK (SoLuongTra > 0),
    LyDo       NVARCHAR(500),
    MaNV       INT           NOT NULL,
    NgayYeuCau DATE          DEFAULT GETDATE(),
    TrangThai  NVARCHAR(20)  DEFAULT N'DangXuLy'
               CHECK (TrangThai IN (N'DangXuLy', N'HoanThanh')),
    GhiChu     NVARCHAR(500),
    FOREIGN KEY (MaHoaDon) REFERENCES HOADON(MaHoaDon),
    FOREIGN KEY (MaSP)     REFERENCES SANPHAM(MaSP),
    FOREIGN KEY (MaSerial) REFERENCES SERIAL(MaSerial),
    FOREIGN KEY (MaNV)     REFERENCES NHANVIEN(MaNV)
);
GO

-- =================================================================
-- INDEX
-- =================================================================
CREATE INDEX IX_SP_MaLoai        ON SANPHAM(MaLoai);
CREATE INDEX IX_SP_TrangThai     ON SANPHAM(TrangThai);
CREATE INDEX IX_SERIAL_MaSP      ON SERIAL(MaSP);
CREATE INDEX IX_SERIAL_TrangThai ON SERIAL(TrangThai);
CREATE INDEX IX_KH_Hang          ON KHACHHANG(HangKhachHang);
CREATE INDEX IX_HD_NgayLap       ON HOADON(NgayLap);
CREATE INDEX IX_HD_MaKhachHang   ON HOADON(MaKhachHang);
CREATE INDEX IX_HD_MaNV          ON HOADON(MaNV);
CREATE INDEX IX_CTHD_MaHoaDon    ON CHITIETHOADON(MaHoaDon);
CREATE INDEX IX_TT_MaHoaDon      ON THANHTOAN(MaHoaDon);
CREATE INDEX IX_BH_TrangThai     ON BAOHANH(TrangThai);
CREATE INDEX IX_DT_MaHoaDon      ON DOITRA(MaHoaDon);
GO

-- =================================================================
-- PHẦN 2: TRIGGER
-- =================================================================

-- -----------------------------------------------------------------
-- TRIGGER 1: Khi INSERT vào CHITIETPHIEUNHAP
-- Cộng SoLuongTon, cập nhật TrangThai nếu trước đó HetHang
-- -----------------------------------------------------------------
CREATE TRIGGER trg_ChiTietPhieuNhap_AfterInsert
ON CHITIETPHIEUNHAP
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE sp
    SET sp.SoLuongTon = sp.SoLuongTon + i.SoLuong
    FROM SANPHAM sp
    JOIN inserted i ON sp.MaSP = i.MaSP;

    UPDATE sp
    SET sp.TrangThai = N'DangBan'
    FROM SANPHAM sp
    JOIN inserted i ON sp.MaSP = i.MaSP
    WHERE sp.TrangThai = N'HetHang';
END
GO

-- -----------------------------------------------------------------
-- TRIGGER 2: Khi INSERT vào SERIAL
-- Kiểm tra số lượng serial không vượt SoLuong trong CHITIETPHIEUNHAP
-- -----------------------------------------------------------------
CREATE TRIGGER trg_Serial_KiemTraSoLuong
ON SERIAL
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN CHITIETPHIEUNHAP ct ON i.MaChiTietPN = ct.MaChiTietPN
        WHERE i.MaChiTietPN IS NOT NULL
          AND (
              SELECT COUNT(*) FROM SERIAL
              WHERE MaChiTietPN = i.MaChiTietPN
          ) > ct.SoLuong
    )
    BEGIN
        RAISERROR(N'Số lượng serial vượt quá SoLuong trong phiếu nhập', 16, 1);
        ROLLBACK;
        RETURN;
    END
END
GO

-- -----------------------------------------------------------------
-- TRIGGER 3: Khi UPDATE TrangThai = 'Huy' trên PHIEUNHAP
-- Trừ lại SoLuongTon, chuyển serial TrongKho về Loi
-- Không cho hủy nếu đã có serial được bán
-- -----------------------------------------------------------------
CREATE TRIGGER trg_PhieuNhap_Huy
ON PHIEUNHAP
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (
        SELECT 1 FROM inserted i
        JOIN deleted d ON i.MaPN = d.MaPN
        WHERE i.TrangThai = N'Huy' AND d.TrangThai != N'Huy'
    ) RETURN;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN CHITIETPHIEUNHAP ct ON i.MaPN = ct.MaPN
        JOIN SERIAL s             ON ct.MaChiTietPN = s.MaChiTietPN
        WHERE i.TrangThai = N'Huy' AND s.TrangThai = N'DaBan'
    )
    BEGIN
        RAISERROR(N'Không thể hủy phiếu nhập vì đã có sản phẩm được bán ra', 16, 1);
        ROLLBACK; RETURN;
    END

    UPDATE sp
    SET sp.SoLuongTon = sp.SoLuongTon - ct.SoLuong
    FROM SANPHAM sp
    JOIN CHITIETPHIEUNHAP ct ON sp.MaSP = ct.MaSP
    JOIN inserted i           ON ct.MaPN = i.MaPN
    WHERE i.TrangThai = N'Huy';

    UPDATE s
    SET s.TrangThai = N'Loi'
    FROM SERIAL s
    JOIN CHITIETPHIEUNHAP ct ON s.MaChiTietPN = ct.MaChiTietPN
    JOIN inserted i           ON ct.MaPN = i.MaPN
    WHERE i.TrangThai = N'Huy' AND s.TrangThai = N'TrongKho';
END
GO

-- -----------------------------------------------------------------
-- TRIGGER 4: Khi INSERT vào CHITIETHOADON (bán hàng)
-- Kiểm tra serial TrongKho, cập nhật DaBan, trừ SoLuongTon
-- -----------------------------------------------------------------
CREATE TRIGGER trg_ChiTietHoaDon_AfterInsert
ON CHITIETHOADON
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1 FROM inserted i
        JOIN SERIAL s ON i.MaSerial = s.MaSerial
        WHERE s.TrangThai != N'TrongKho'
    )
    BEGIN
        RAISERROR(N'Serial này không ở trạng thái TrongKho, không thể bán', 16, 1);
        ROLLBACK; RETURN;
    END

    IF EXISTS (
        SELECT 1 FROM inserted i
        JOIN SERIAL s ON i.MaSerial = s.MaSerial
        WHERE i.MaSP != s.MaSP
    )
    BEGIN
        RAISERROR(N'Serial không thuộc sản phẩm đã chọn', 16, 1);
        ROLLBACK; RETURN;
    END

    UPDATE s
    SET s.TrangThai = N'DaBan', s.NgayXuat = CAST(GETDATE() AS DATE)
    FROM SERIAL s
    JOIN inserted i ON s.MaSerial = i.MaSerial;

    UPDATE sp
    SET sp.SoLuongTon = sp.SoLuongTon - i.SoLuong
    FROM SANPHAM sp
    JOIN inserted i ON sp.MaSP = i.MaSP;

    UPDATE sp
    SET sp.TrangThai = N'HetHang'
    FROM SANPHAM sp
    JOIN inserted i ON sp.MaSP = i.MaSP
    WHERE sp.SoLuongTon = 0;
END
GO

-- -----------------------------------------------------------------
-- TRIGGER 5: Tự động tính lại TongTienHang trên HOADON
-- -----------------------------------------------------------------
CREATE TRIGGER trg_ChiTietHoaDon_CapNhatTongTien
ON CHITIETHOADON
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    WITH HoaDonAnhHuong AS (
        SELECT MaHoaDon FROM inserted
        UNION
        SELECT MaHoaDon FROM deleted
    )
    UPDATE hd
    SET hd.TongTienHang = (
        SELECT ISNULL(SUM(ct.ThanhTien), 0)
        FROM CHITIETHOADON ct
        WHERE ct.MaHoaDon = hd.MaHoaDon
    )
    FROM HOADON hd
    JOIN HoaDonAnhHuong ah ON hd.MaHoaDon = ah.MaHoaDon;
END
GO

-- -----------------------------------------------------------------
-- TRIGGER 6: Khi hóa đơn chuyển sang HoanThanh → cộng điểm
-- Quy đổi: FLOOR(TongThanhToan / 100000) điểm
-- HangKhachHang và PhanTramGiam tự cập nhật vì là computed column
-- -----------------------------------------------------------------
CREATE TRIGGER trg_HoaDon_CongDiem
ON HOADON
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Chỉ cộng điểm cho hóa đơn mới HoanThanh (chưa từng HoanThanh trước đó)
    UPDATE kh
    SET kh.DiemTichLuy = kh.DiemTichLuy
        + FLOOR(i.TongThanhToan / 100000)
    FROM KHACHHANG kh
    JOIN inserted i ON kh.MaKhachHang = i.MaKhachHang
    WHERE i.TrangThai   = N'HoanThanh'
      AND i.MaKhachHang IS NOT NULL
      AND NOT EXISTS (
          SELECT 1 FROM deleted d
          WHERE d.MaHoaDon  = i.MaHoaDon
            AND d.TrangThai = N'HoanThanh'
      );
END
GO

-- -----------------------------------------------------------------
-- TRIGGER 7: Khi UPDATE TrangThai = 'Huy' trên HOADON
-- Hoàn trả serial, cộng lại SoLuongTon, trừ lại điểm tích lũy
-- Không cho hủy nếu đang có bảo hành chưa xong
-- -----------------------------------------------------------------
CREATE TRIGGER trg_HoaDon_Huy
ON HOADON
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (
        SELECT 1 FROM inserted i
        JOIN deleted d ON i.MaHoaDon = d.MaHoaDon
        WHERE i.TrangThai = N'Huy' AND d.TrangThai != N'Huy'
    ) RETURN;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN BAOHANH bh ON i.MaHoaDon = bh.MaHoaDon
        WHERE i.TrangThai = N'Huy' AND bh.TrangThai != N'DaTraKhach'
    )
    BEGIN
        RAISERROR(N'Không thể hủy hóa đơn vì đang có phiếu bảo hành chưa hoàn thành', 16, 1);
        ROLLBACK; RETURN;
    END

    -- Hoàn trả serial về TrongKho
    UPDATE s
    SET s.TrangThai = N'TrongKho', s.NgayXuat = NULL
    FROM SERIAL s
    JOIN CHITIETHOADON ct ON s.MaSerial = ct.MaSerial
    JOIN inserted i        ON ct.MaHoaDon = i.MaHoaDon
    WHERE i.TrangThai = N'Huy';

    -- Cộng lại SoLuongTon
    UPDATE sp
    SET sp.SoLuongTon = sp.SoLuongTon + ct.SoLuong
    FROM SANPHAM sp
    JOIN CHITIETHOADON ct ON sp.MaSP = ct.MaSP
    JOIN inserted i        ON ct.MaHoaDon = i.MaHoaDon
    WHERE i.TrangThai = N'Huy';

    -- Cập nhật DangBan nếu trước đó HetHang
    UPDATE sp
    SET sp.TrangThai = N'DangBan'
    FROM SANPHAM sp
    JOIN CHITIETHOADON ct ON sp.MaSP = ct.MaSP
    JOIN inserted i        ON ct.MaHoaDon = i.MaHoaDon
    WHERE i.TrangThai = N'Huy' AND sp.TrangThai = N'HetHang';

    -- Trừ lại điểm đã cộng, đảm bảo không âm
    UPDATE kh
    SET kh.DiemTichLuy = CASE
        WHEN kh.DiemTichLuy - FLOOR(i.TongThanhToan / 100000) < 0
        THEN 0
        ELSE kh.DiemTichLuy - FLOOR(i.TongThanhToan / 100000)
    END
    FROM KHACHHANG kh
    JOIN inserted i ON kh.MaKhachHang = i.MaKhachHang
    WHERE i.TrangThai = N'Huy' AND i.MaKhachHang IS NOT NULL;
END
GO

-- =================================================================
-- PHẦN 3: DỮ LIỆU MẪU
-- =================================================================

-- -----------------------------------------------------------------
-- 1. LOAISANPHAM
-- -----------------------------------------------------------------
INSERT INTO LOAISANPHAM (TenLoai, MoTa) VALUES
(N'Laptop',   N'Máy tính xách tay'),
(N'Bàn phím', N'Bàn phím cơ, membrane, không dây'),
(N'Chuột',    N'Chuột có dây, không dây, gaming'),
(N'Màn hình', N'Màn hình HD chiến mọi thể loại'),
(N'RAM',      N'RAM PC và Laptop');
GO

-- -----------------------------------------------------------------
-- 2. SANPHAM
-- -----------------------------------------------------------------
INSERT INTO SANPHAM
    (TenSP, MaLoai, ThuongHieu, MauSac, Gia, GiaGoc,
     SoLuongTon, SoLuongToiThieu, SoLuongToiDa, ThoiHanBaoHanhThang, TrangThai)
VALUES
-- LAPTOP (MaLoai=1)
(N'Dell Inspiron 15 3520',     1, N'Dell',      N'Bac',   16990000, 14500000, 0, 3, 10, 12, N'DangBan'),
(N'Dell XPS 13 Plus',          1, N'Dell',      N'Bac',   42990000, 38000000, 0, 2,  8, 24, N'DangBan'),
(N'HP Pavilion 15',            1, N'HP',        N'Xanh',  17490000, 15000000, 0, 3, 10, 12, N'DangBan'),
(N'HP Envy x360 14',           1, N'HP',        N'Bac',   28990000, 25500000, 0, 2,  8, 24, N'DangBan'),
(N'Asus VivoBook 15 X1504',    1, N'Asus',      N'Bac',   14990000, 12800000, 0, 3, 10, 12, N'DangBan'),
(N'Asus ROG Zephyrus G14',     1, N'Asus',      N'Xam',   42990000, 38500000, 0, 2,  6, 24, N'DangBan'),
(N'Acer Aspire 5 A515',        1, N'Acer',      N'Xam',   13990000, 11500000, 0, 3, 10, 12, N'DangBan'),
(N'Lenovo IdeaPad Slim 5',     1, N'Lenovo',    N'Xanh',  18490000, 16000000, 0, 3, 10, 12, N'DangBan'),
(N'Lenovo ThinkPad X1 Carbon', 1, N'Lenovo',    N'Den',   52990000, 47000000, 0, 2,  6, 24, N'DangBan'),
(N'MacBook Air M2 13 inch',    1, N'Apple',     N'Vang',  28990000, 25000000, 0, 2,  8, 12, N'DangBan'),
-- BAN PHIM (MaLoai=2)
(N'Logitech MX Keys S',        2, N'Logitech',  N'Den',    1990000,  1600000, 0, 5, 30, 6, N'DangBan'),
(N'Keychron K2 Pro',           2, N'Keychron',  N'Xam',    2490000,  2000000, 0, 5, 25, 6, N'DangBan'),
(N'Akko 3068B Plus',           2, N'Akko',      N'Trang',  1290000,  1000000, 0, 8, 40, 6, N'DangBan'),
(N'Corsair K70 RGB Pro',       2, N'Corsair',   N'Den',    3290000,  2800000, 0, 4, 20, 6, N'DangBan'),
(N'HP 230 Wireless Keyboard',  2, N'HP',        N'Den',     490000,   380000, 0,10, 50, 6, N'DangBan'),
-- CHUOT (MaLoai=3)
(N'Logitech MX Master 3S',     3, N'Logitech',  N'Den',    2290000,  1900000, 0, 5, 30, 6, N'DangBan'),
(N'Razer DeathAdder V3',       3, N'Razer',     N'Den',    1890000,  1550000, 0, 5, 25, 6, N'DangBan'),
(N'Asus ROG Gladius III',      3, N'Asus',      N'Den',    1690000,  1400000, 0, 4, 20, 6, N'DangBan'),
(N'Microsoft Arc Mouse',       3, N'Microsoft', N'Xanh',    990000,   800000, 0, 6, 35, 6, N'DangBan'),
(N'HP X500 Wired Mouse',       3, N'HP',        N'Den',     290000,   220000, 0,10, 60, 6, N'DangBan');
GO

-- -----------------------------------------------------------------
-- 3. THONGSOKYTHUAT
-- -----------------------------------------------------------------
INSERT INTO THONGSOKYTHUAT
    (MaSP, CPU, RAM, OCung, ManHinh, VGA, HeDieuHanh, Pin, TrongLuong, KetNoi)
VALUES
(1,  N'Intel Core i5-1235U',  N'8GB DDR4',    N'512GB SSD', N'15.6 inch FHD',        N'Intel Iris Xe',  N'Windows 11',    N'41Wh',   N'1.73kg', N'USB-A x2, USB-C, HDMI, Wifi 5, BT 5'),
(2,  N'Intel Core i7-1280P',  N'16GB LPDDR5', N'512GB SSD', N'13.4 inch OLED',       N'Intel Iris Xe',  N'Windows 11',    N'55Wh',   N'1.26kg', N'USB-C x2 TB4, Wifi 6E, BT 5.2'),
(3,  N'Intel Core i5-1235U',  N'8GB DDR4',    N'512GB SSD', N'15.6 inch FHD',        N'Intel Iris Xe',  N'Windows 11',    N'43Wh',   N'1.75kg', N'USB-A x2, USB-C, HDMI, Wifi 5, BT 5'),
(4,  N'Intel Core i7-1255U',  N'16GB DDR4',   N'512GB SSD', N'14 inch FHD IPS',      N'Intel Iris Xe',  N'Windows 11',    N'66Wh',   N'1.36kg', N'USB-A, USB-C x2, HDMI, Wifi 6, BT 5.3'),
(5,  N'Intel Core i5-1235U',  N'8GB DDR4',    N'512GB SSD', N'15.6 inch FHD',        N'Intel UHD',      N'Windows 11',    N'42Wh',   N'1.70kg', N'USB-A x2, USB-C, HDMI, Wifi 5, BT 5'),
(6,  N'AMD Ryzen 9 7940HS',   N'16GB DDR5',   N'1TB SSD',   N'14 inch QHD+',         N'RTX 4060 8GB',   N'Windows 11',    N'76Wh',   N'1.65kg', N'USB-A, USB-C x2 TB4, HDMI, Wifi 6E, BT 5.3'),
(7,  N'Intel Core i5-1235U',  N'8GB DDR4',    N'512GB SSD', N'15.6 inch FHD',        N'Intel Iris Xe',  N'Windows 11',    N'50Wh',   N'1.78kg', N'USB-A x2, USB-C, HDMI, Wifi 5, BT 5'),
(8,  N'Intel Core i5-1335U',  N'16GB DDR5',   N'512GB SSD', N'15.6 inch FHD',        N'Intel Iris Xe',  N'Windows 11',    N'57Wh',   N'1.46kg', N'USB-A x2, USB-C, HDMI, Wifi 6, BT 5.1'),
(9,  N'Intel Core i7-1365U',  N'16GB LPDDR5', N'512GB SSD', N'14 inch IPS',          N'Intel Iris Xe',  N'Windows 11',    N'57Wh',   N'1.12kg', N'USB-C x2 TB4, HDMI, Wifi 6E, BT 5.3'),
(10, N'Apple M2 8 Core',      N'8GB Unified', N'256GB SSD', N'13.6 inch Retina',     N'Apple M2 8GPU',  N'macOS Ventura', N'52.6Wh', N'1.24kg', N'USB-C x2 TB, Wifi 6, BT 5.3');
GO

-- -----------------------------------------------------------------
-- 4. NHANVIEN
-- -----------------------------------------------------------------
INSERT INTO NHANVIEN (TenNV, SoDienThoai, Email, NgaySinh, NgayVaoLam, VaiTro, TrangThai) VALUES
(N'Võ Đức Hoàng Vinh', '0901111111', 'vinh.nv@laptopstore.vn', '2006-01-15', '2020-01-10', N'QuanLy',          N'DangLam'),
(N'Đặng Lương Thế Anh',    '0902222222', 'anh.tt@laptopstore.vn',  '2006-10-22', '2021-03-15', N'NhanVienBanHang', N'DangLam'),
(N'Trương Quốc Thái',      '0903333333', 'thai.lv@laptopstore.vn',  '1998-12-10', '2022-06-01', N'NhanVienBanHang', N'DangLam'),
(N'Pham Thi Hoa',    '0904444444', 'hoa.pt@laptopstore.vn',  '1997-03-25', '2023-01-10', N'NhanVienBanHang', N'DangLam');
GO

-- -----------------------------------------------------------------
-- 5. TAIKHOAN
-- -----------------------------------------------------------------
INSERT INTO TAIKHOAN (MaNV, TenDangNhap, MatKhauHash, TrangThai) VALUES
(1, 'admin',  '1',  N'HoatDong'),
(2, 'anhthe', '1', N'HoatDong'),
(3, 'thaitruong', '1', N'HoatDong'),
(4, 'hoa.pt', '1', N'HoatDong');
GO

-- -----------------------------------------------------------------
-- 6. NHACUNGCAP
-- -----------------------------------------------------------------
INSERT INTO NHACUNGCAP (TenNhaCungCap, SoDienThoai, Email, DiaChi, TrangThai) VALUES
(N'Cong ty TNHH Phan phoi Dell VN', '02812345678', 'contact@dell-vn.com',    N'123 Nguyen Hue, Q1, TP.HCM',      N'HoatDong'),
(N'HP Vietnam Distribution',        '02887654321', 'sales@hp-vietnam.com',    N'456 Le Loi, Q1, TP.HCM',          N'HoatDong'),
(N'Asus Viet Nam',                  '02811112222', 'asus@asuspartner.vn',     N'789 Dien Bien Phu, Q3, TP.HCM',   N'HoatDong'),
(N'Acer Vietnam',                   '02833334444', 'acer@acervietnam.vn',     N'321 CMT8, Q10, TP.HCM',           N'HoatDong'),
(N'Lenovo Vietnam',                 '02855556666', 'lenovo@lenovovn.com',     N'654 Hoang Van Thu, Tan Binh',      N'HoatDong'),
(N'Apple Premium Reseller',         '02877778888', 'apple@applereseller.vn',  N'987 Dong Khoi, Q1, TP.HCM',       N'HoatDong'),
(N'Logitech Vietnam',               '02899990000', 'logitech@logitechvn.com', N'111 Phan Xich Long, Phu Nhuan',   N'HoatDong'),
(N'Phu kien Gaming Viet',           '02866667777', 'sales@pkgaming.vn',       N'222 Lac Long Quan, Q11, TP.HCM',  N'HoatDong');
GO

-- -----------------------------------------------------------------
-- 7. KHACHHANG
-- DiemTichLuy đặt sẵn → HangKhachHang và PhanTramGiam tự tính:
--   KH1: 150đ  → Dong     (3%)
--   KH2: 3200đ → Vang     (8%)
--   KH3:  80đ  → VoHan    (0%)
--   KH4: 520đ  → Bac      (5%)
--   KH5:  40đ  → VoHan    (0%)
-- -----------------------------------------------------------------
INSERT INTO KHACHHANG (TenKhachHang, SoDienThoai, Email, NgaySinh, GioiTinh, DiemTichLuy) VALUES
(N'Nguyen Thi Mai',   '0911111111', 'mai.nt@gmail.com',   '1992-04-10', N'Nu',  150),
(N'Tran Van Hung',    '0922222222', 'hung.tv@gmail.com',  '1988-07-22', N'Nam', 3200),
(N'Le Thi Thanh',     '0933333333', 'thanh.lt@gmail.com', '1995-11-05', N'Nu',   80),
(N'Pham Van Duc',     '0944444444', 'duc.pv@gmail.com',   '1990-02-28', N'Nam', 520),
(N'Hoang Minh Tuan',  '0955555555', 'tuan.hm@gmail.com',  '1998-09-15', N'Nam',  40);
GO

-- -----------------------------------------------------------------
-- 8. PHIEUNHAP
-- -----------------------------------------------------------------
INSERT INTO PHIEUNHAP (MaNhaCungCap, MaNV, NgayNhap, TongTien, TrangThai) VALUES
(1, 1, '2024-10-01', 172000000, N'HoanThanh'),
(2, 1, '2024-10-10', 136500000, N'HoanThanh'),
(3, 1, '2024-10-15', 167700000, N'HoanThanh'),
(4, 1, '2024-10-22',  46000000, N'HoanThanh'),
(5, 1, '2024-11-01', 189000000, N'HoanThanh'),
(6, 1, '2024-11-10', 100000000, N'HoanThanh'),
(7, 1, '2024-10-01',  46800000, N'HoanThanh'),
(8, 1, '2024-10-01',  89150000, N'HoanThanh');
GO

-- -----------------------------------------------------------------
-- 9. CHITIETPHIEUNHAP
-- -----------------------------------------------------------------
DISABLE TRIGGER trg_ChiTietPhieuNhap_AfterInsert ON CHITIETPHIEUNHAP;
GO
DISABLE TRIGGER trg_Serial_KiemTraSoLuong ON SERIAL;
GO

INSERT INTO CHITIETPHIEUNHAP (MaPN, MaSP, SoLuong, DonGiaNhap) VALUES
-- MaPN=1 Dell (MaChiTietPN 1,2)
(1,  1, 4, 14500000), (1,  2, 3, 38000000),
-- MaPN=2 HP (MaChiTietPN 3,4)
(2,  3, 4, 15000000), (2,  4, 3, 25500000),
-- MaPN=3 Asus (MaChiTietPN 5,6)
(3,  5, 4, 12800000), (3,  6, 3, 38500000),
-- MaPN=4 Acer (MaChiTietPN 7)
(4,  7, 4, 11500000),
-- MaPN=5 Lenovo (MaChiTietPN 8,9)
(5,  8, 3, 16000000), (5,  9, 3, 47000000),
-- MaPN=6 Apple (MaChiTietPN 10)
(6, 10, 4, 25000000),
-- MaPN=7 Logitech (MaChiTietPN 11,12)
(7, 11,15,  1600000), (7, 16,12,  1900000),
-- MaPN=8 Gaming (MaChiTietPN 13-20)
(8, 12,10,  2000000), (8, 13,20,  1000000), (8, 14, 8,  2800000),
(8, 15,25,   380000), (8, 17,10,  1550000), (8, 18, 8,  1400000),
(8, 19,15,   800000), (8, 20,30,   220000);
GO

-- Cập nhật SoLuongTon thủ công (trigger bị tắt tạm)
UPDATE SANPHAM SET SoLuongTon =  4 WHERE MaSP =  1;
UPDATE SANPHAM SET SoLuongTon =  3 WHERE MaSP =  2;
UPDATE SANPHAM SET SoLuongTon =  4 WHERE MaSP =  3;
UPDATE SANPHAM SET SoLuongTon =  3 WHERE MaSP =  4;
UPDATE SANPHAM SET SoLuongTon =  4 WHERE MaSP =  5;
UPDATE SANPHAM SET SoLuongTon =  3 WHERE MaSP =  6;
UPDATE SANPHAM SET SoLuongTon =  4 WHERE MaSP =  7;
UPDATE SANPHAM SET SoLuongTon =  3 WHERE MaSP =  8;
UPDATE SANPHAM SET SoLuongTon =  3 WHERE MaSP =  9;
UPDATE SANPHAM SET SoLuongTon =  4 WHERE MaSP = 10;
UPDATE SANPHAM SET SoLuongTon = 15 WHERE MaSP = 11;
UPDATE SANPHAM SET SoLuongTon = 10 WHERE MaSP = 12;
UPDATE SANPHAM SET SoLuongTon = 20 WHERE MaSP = 13;
UPDATE SANPHAM SET SoLuongTon =  8 WHERE MaSP = 14;
UPDATE SANPHAM SET SoLuongTon = 25 WHERE MaSP = 15;
UPDATE SANPHAM SET SoLuongTon = 12 WHERE MaSP = 16;
UPDATE SANPHAM SET SoLuongTon = 10 WHERE MaSP = 17;
UPDATE SANPHAM SET SoLuongTon =  8 WHERE MaSP = 18;
UPDATE SANPHAM SET SoLuongTon = 15 WHERE MaSP = 19;
UPDATE SANPHAM SET SoLuongTon = 30 WHERE MaSP = 20;
GO

-- -----------------------------------------------------------------
-- 10. SERIAL
-- -----------------------------------------------------------------
INSERT INTO SERIAL (SerialCode, MaSP, MaChiTietPN, TrangThai, NgayNhap) VALUES
-- Dell Inspiron (MaSP=1, MaChiTietPN=1)
('SN-DELL-INS-001', 1, 1, N'TrongKho', '2024-10-01'),
('SN-DELL-INS-002', 1, 1, N'TrongKho', '2024-10-01'),
('SN-DELL-INS-003', 1, 1, N'DaBan',    '2024-10-01'),
('SN-DELL-INS-004', 1, 1, N'TrongKho', '2024-10-01'),
-- Dell XPS (MaSP=2, MaChiTietPN=2)
('SN-DELL-XPS-001', 2, 2, N'TrongKho', '2024-10-05'),
('SN-DELL-XPS-002', 2, 2, N'TrongKho', '2024-10-05'),
('SN-DELL-XPS-003', 2, 2, N'DaBan',    '2024-10-05'),
-- HP Pavilion (MaSP=3, MaChiTietPN=3)
('SN-HP-PAV-001',   3, 3, N'TrongKho', '2024-10-10'),
('SN-HP-PAV-002',   3, 3, N'TrongKho', '2024-10-10'),
('SN-HP-PAV-003',   3, 3, N'BaoHanh',  '2024-10-10'),
('SN-HP-PAV-004',   3, 3, N'TrongKho', '2024-10-10'),
-- HP Envy (MaSP=4, MaChiTietPN=4)
('SN-HP-ENV-001',   4, 4, N'TrongKho', '2024-10-12'),
('SN-HP-ENV-002',   4, 4, N'DaBan',    '2024-10-12'),
('SN-HP-ENV-003',   4, 4, N'TrongKho', '2024-10-12'),
-- Asus VivoBook (MaSP=5, MaChiTietPN=5)
('SN-ASUS-VB-001',  5, 5, N'TrongKho', '2024-10-15'),
('SN-ASUS-VB-002',  5, 5, N'TrongKho', '2024-10-15'),
('SN-ASUS-VB-003',  5, 5, N'TrongKho', '2024-10-15'),
('SN-ASUS-VB-004',  5, 5, N'DaBan',    '2024-10-15'),
-- Asus ROG (MaSP=6, MaChiTietPN=6)
('SN-ASUS-ROG-001', 6, 6, N'TrongKho', '2024-10-20'),
('SN-ASUS-ROG-002', 6, 6, N'TrongKho', '2024-10-20'),
('SN-ASUS-ROG-003', 6, 6, N'DaBan',    '2024-10-20'),
-- Acer Aspire (MaSP=7, MaChiTietPN=7)
('SN-ACER-A5-001',  7, 7, N'TrongKho', '2024-10-22'),
('SN-ACER-A5-002',  7, 7, N'TrongKho', '2024-10-22'),
('SN-ACER-A5-003',  7, 7, N'DoiTra',   '2024-10-22'),
('SN-ACER-A5-004',  7, 7, N'TrongKho', '2024-10-22'),
-- Lenovo IdeaPad (MaSP=8, MaChiTietPN=8)
('SN-LEN-IDP-001',  8, 8, N'TrongKho', '2024-11-01'),
('SN-LEN-IDP-002',  8, 8, N'TrongKho', '2024-11-01'),
('SN-LEN-IDP-003',  8, 8, N'DaBan',    '2024-11-01'),
-- Lenovo ThinkPad (MaSP=9, MaChiTietPN=9)
('SN-LEN-TPX-001',  9, 9, N'TrongKho', '2024-11-05'),
('SN-LEN-TPX-002',  9, 9, N'TrongKho', '2024-11-05'),
('SN-LEN-TPX-003',  9, 9, N'DaBan',    '2024-11-05'),
-- MacBook Air (MaSP=10, MaChiTietPN=10)
('SN-APPLE-MBA-001',10,10, N'TrongKho', '2024-11-10'),
('SN-APPLE-MBA-002',10,10, N'TrongKho', '2024-11-10'),
('SN-APPLE-MBA-003',10,10, N'DaBan',    '2024-11-10'),
('SN-APPLE-MBA-004',10,10, N'TrongKho', '2024-11-10'),
-- Logitech MX Keys S (MaSP=11, MaChiTietPN=11, 15 cai)
('SN-MXKEYS-001',11,11,N'TrongKho','2024-10-01'),('SN-MXKEYS-002',11,11,N'TrongKho','2024-10-01'),
('SN-MXKEYS-003',11,11,N'TrongKho','2024-10-01'),('SN-MXKEYS-004',11,11,N'TrongKho','2024-10-01'),
('SN-MXKEYS-005',11,11,N'TrongKho','2024-10-01'),('SN-MXKEYS-006',11,11,N'TrongKho','2024-10-01'),
('SN-MXKEYS-007',11,11,N'TrongKho','2024-10-01'),('SN-MXKEYS-008',11,11,N'TrongKho','2024-10-01'),
('SN-MXKEYS-009',11,11,N'TrongKho','2024-10-01'),('SN-MXKEYS-010',11,11,N'TrongKho','2024-10-01'),
('SN-MXKEYS-011',11,11,N'TrongKho','2024-10-01'),('SN-MXKEYS-012',11,11,N'TrongKho','2024-10-01'),
('SN-MXKEYS-013',11,11,N'TrongKho','2024-10-01'),('SN-MXKEYS-014',11,11,N'TrongKho','2024-10-01'),
('SN-MXKEYS-015',11,11,N'TrongKho','2024-10-01'),
-- Logitech MX Master 3S (MaSP=16, MaChiTietPN=12, 12 cai)
('SN-MXMS-001',16,12,N'TrongKho','2024-10-01'),('SN-MXMS-002',16,12,N'TrongKho','2024-10-01'),
('SN-MXMS-003',16,12,N'TrongKho','2024-10-01'),('SN-MXMS-004',16,12,N'TrongKho','2024-10-01'),
('SN-MXMS-005',16,12,N'TrongKho','2024-10-01'),('SN-MXMS-006',16,12,N'TrongKho','2024-10-01'),
('SN-MXMS-007',16,12,N'TrongKho','2024-10-01'),('SN-MXMS-008',16,12,N'TrongKho','2024-10-01'),
('SN-MXMS-009',16,12,N'TrongKho','2024-10-01'),('SN-MXMS-010',16,12,N'TrongKho','2024-10-01'),
('SN-MXMS-011',16,12,N'TrongKho','2024-10-01'),('SN-MXMS-012',16,12,N'TrongKho','2024-10-01');
GO

ENABLE TRIGGER trg_ChiTietPhieuNhap_AfterInsert ON CHITIETPHIEUNHAP;
GO
ENABLE TRIGGER trg_Serial_KiemTraSoLuong ON SERIAL;
GO

-- -----------------------------------------------------------------
-- 11. HOADON
-- PhanTramGiamHang = snapshot % giảm của KH tại thời điểm mua
--   KH1 (Dong  3%): HĐ1, HĐ6
--   KH2 (Vang  8%): HĐ2
--   KH3 (VoHan 0%): HĐ3
--   KH4 (Bac   5%): HĐ4
--   NULL (vãng lai): HĐ5
-- -----------------------------------------------------------------
DISABLE TRIGGER trg_ChiTietHoaDon_CapNhatTongTien ON CHITIETHOADON;
GO
DISABLE TRIGGER trg_ChiTietHoaDon_AfterInsert ON CHITIETHOADON;
GO
DISABLE TRIGGER trg_HoaDon_CongDiem ON HOADON;
GO

INSERT INTO HOADON (MaKhachHang, MaNV, NgayLap, TongTienHang, PhanTramGiamHang, TrangThai) VALUES
(1, 2, '2024-11-05 10:30:00', 16990000, 3, N'HoanThanh'),  -- HĐ1: KH1 Dong 3%
(2, 3, '2024-11-10 14:15:00', 45280000, 8, N'HoanThanh'),  -- HĐ2: KH2 Vang 8%
(3, 2, '2024-11-15 09:00:00',  3160000, 0, N'HoanThanh'),  -- HĐ3: KH3 VoHan 0%
(4, 4, '2024-11-30 16:45:00', 28990000, 5, N'HoanThanh'),  -- HĐ4: KH4 Bac 5%
(NULL, 3, '2024-12-01 11:00:00', 1290000, 0, N'HoanThanh'); -- HĐ5: vang lai
GO

-- -----------------------------------------------------------------
-- 12. CHITIETHOADON
-- MaSerial: laptop dùng serial cụ thể, phụ kiện dùng serial đầu tiên
--   MaSerial=3  → SN-DELL-INS-003 (MaSP=1)
--   MaSerial=7  → SN-DELL-XPS-003 (MaSP=2)
--   MaSerial=37 → SN-MXMS-001     (MaSP=16)
--   MaSerial=13 → SN-HP-ENV-002   (MaSP=4)
-- -----------------------------------------------------------------
INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia) VALUES
(1,  1,  3, 1, 16990000),  -- HĐ1: Dell Inspiron
(2,  2,  7, 1, 42990000),  -- HĐ2: Dell XPS
(2, 16, 37, 1,  2290000),  -- HĐ2: Logitech MX Master
(3, 13, 36, 1,  1290000),  -- HĐ3: Akko 3068B (dùng SN-MXKEYS-015 vì chưa có serial Akko mẫu)
(3, 20, 35, 1,   290000),  -- HĐ3: HP X500
(4,  4, 13, 1, 28990000),  -- HĐ4: HP Envy
(5, 13, 34, 1,  1290000);  -- HĐ5: Akko (vang lai)
GO

ENABLE TRIGGER trg_ChiTietHoaDon_CapNhatTongTien ON CHITIETHOADON;
GO
ENABLE TRIGGER trg_ChiTietHoaDon_AfterInsert ON CHITIETHOADON;
GO
ENABLE TRIGGER trg_HoaDon_CongDiem ON HOADON;
GO

-- -----------------------------------------------------------------
-- 13. THANHTOAN
-- TongThanhToan = TongTienHang * (1 - % / 100) * 1.10
--   HĐ1: 16990000 * 0.97 * 1.10 = 18,127,330
--   HĐ2: 45280000 * 0.92 * 1.10 = 45,843,040
--   HĐ3:  3160000 * 1.00 * 1.10 =  3,476,000
--   HĐ4: 28990000 * 0.95 * 1.10 = 30,299,550
--   HĐ5:  1290000 * 1.00 * 1.10 =  1,419,000
-- -----------------------------------------------------------------
INSERT INTO THANHTOAN (MaHoaDon, SoTien, PhuongThuc, TrangThai) VALUES
(1, 18127330, N'TienMat',     N'ThanhCong'),
(2, 45843040, N'ChuyenKhoan', N'ThanhCong'),
(3,  3476000, N'MoMo',        N'ThanhCong'),
(4, 30299550, N'TheNganHang', N'ThanhCong'),
(5,  1419000, N'TienMat',     N'ThanhCong');
GO

-- -----------------------------------------------------------------
-- 14. BAOHANH (HĐ6: bán HP Pavilion trước để có căn cứ bảo hành)
-- HĐ6: 17490000 * 0.97 * 1.10 = 18,661,230
-- -----------------------------------------------------------------
DISABLE TRIGGER trg_ChiTietHoaDon_AfterInsert ON CHITIETHOADON;
GO
INSERT INTO HOADON (MaKhachHang, MaNV, NgayLap, TongTienHang, PhanTramGiamHang, TrangThai)
VALUES (1, 2, '2024-10-15 09:00:00', 17490000, 3, N'HoanThanh');
GO
INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia)
VALUES (6, 3, 10, 1, 17490000);  -- HĐ6: HP Pavilion → SN-HP-PAV-003
GO
ENABLE TRIGGER trg_ChiTietHoaDon_AfterInsert ON CHITIETHOADON;
GO

INSERT INTO THANHTOAN (MaHoaDon, SoTien, PhuongThuc, TrangThai)
VALUES (6, 18661230, N'TienMat', N'ThanhCong');
GO

INSERT INTO BAOHANH
    (MaSerial, MaSP, MaHoaDon, MaNVTiepNhan, NgayTiepNhan,
     NgayHenTra, MoTaLoi, HinhThucXuLy, TrangThai)
VALUES
(10, 3, 6, 2, '2024-11-20', '2024-12-05',
 N'Man hinh bi soc ngang sau 1 thang su dung',
 N'GuiHang', N'DaGuiHang');
GO

-- -----------------------------------------------------------------
-- 15. DOITRA
-- -----------------------------------------------------------------
INSERT INTO DOITRA
    (MaHoaDon, MaSP, MaSerial, SoLuongTra, LyDo, MaNV, TrangThai, GhiChu)
VALUES
(1, 1, 3, 1,
 N'Ban phim khong nhan phim sau 2 tuan - loi nha san xuat',
 2, N'HoanThanh',
 N'Da doi may moi Serial: SN-DELL-INS-004');
GO

UPDATE SERIAL SET TrangThai = N'DoiTra'                         WHERE MaSerial = 3;
UPDATE SERIAL SET TrangThai = N'DaBan', NgayXuat = '2024-11-22' WHERE MaSerial = 4;
GO
USE LAPTOPSTORE
GO

-- =================================================================
-- PHẦN BỔ SUNG: BẢNG TRUNG GIAN NHÀ CUNG CẤP - SẢN PHẨM
-- Không làm ảnh hưởng đến cấu trúc của các bảng cũ
-- =================================================================

CREATE TABLE NHACUNGCAP_SANPHAM (
    MaNhaCungCap INT NOT NULL,
    MaSP         INT NOT NULL,
    
    -- Khóa chính kép: 1 Nhà cung cấp không thể thêm trùng 1 sản phẩm 2 lần
    PRIMARY KEY (MaNhaCungCap, MaSP),
    
    -- Khóa ngoại liên kết an toàn về 2 bảng gốc
    FOREIGN KEY (MaNhaCungCap) REFERENCES NHACUNGCAP(MaNhaCungCap),
    FOREIGN KEY (MaSP)         REFERENCES SANPHAM(MaSP)
);
GO

-- Đổ dữ liệu mapping dựa trên lịch sử nhập hàng đã có
INSERT INTO NHACUNGCAP_SANPHAM (MaNhaCungCap, MaSP) VALUES
-- 1. Dell VN (MaNCC=1) phân phối: Dell Inspiron (1), Dell XPS (2)
(1, 1), (1, 2),

-- 2. HP Vietnam (MaNCC=2) phân phối: HP Pavilion (3), HP Envy (4)
(2, 3), (2, 4),

-- 3. Asus Viet Nam (MaNCC=3) phân phối: Asus VivoBook (5), Asus ROG (6)
(3, 5), (3, 6),

-- 4. Acer Vietnam (MaNCC=4) phân phối: Acer Aspire (7)
(4, 7),

-- 5. Lenovo Vietnam (MaNCC=5) phân phối: Lenovo IdeaPad (8), ThinkPad (9)
(5, 8), (5, 9),

-- 6. Apple Reseller (MaNCC=6) phân phối: MacBook Air (10)
(6, 10),

-- 7. Logitech Vietnam (MaNCC=7) phân phối: MX Keys S (11), MX Master (16)
(7, 11), (7, 16),

-- 8. Phu kien Gaming Viet (MaNCC=8) phân phối: Keychron (12), Akko (13), Corsair (14), HP Keyboard (15), Razer (17), Asus Mouse (18), Microsoft (19), HP Mouse (20)
(8, 12), (8, 13), (8, 14), (8, 15), (8, 17), (8, 18), (8, 19), (8, 20);
GO