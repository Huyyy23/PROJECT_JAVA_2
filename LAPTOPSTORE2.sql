use master
go
CREATE DATABASE LAPTOPSTORE;
GO

USE LAPTOPSTORE;
GO

/* =========================================================================
   PHẦN 1: TẠO CẤU TRÚC BẢNG (DDL)
========================================================================= */

CREATE TABLE LOAISANPHAM (
    MaLoai INT PRIMARY KEY IDENTITY(1,1),
    TenLoai NVARCHAR(100) NOT NULL,
    MoTa NVARCHAR(255)
);
GO

CREATE TABLE SANPHAM (
    MaSP INT PRIMARY KEY IDENTITY(1,1),
    TenSP NVARCHAR(150) NOT NULL,
    MaLoai INT NOT NULL,
    ThuongHieu NVARCHAR(100),
    MauSac NVARCHAR(50),
    Gia DECIMAL(18,2) NOT NULL CHECK(Gia >= 0),
    GiaGoc DECIMAL(18,2),
    SoLuongTon INT DEFAULT 0 CHECK(SoLuongTon >= 0),
    SoLuongToiThieu INT DEFAULT 0,
    SoLuongToiDa INT DEFAULT 0,
    ThoiHanBaoHanhThang INT DEFAULT 0,
    MoTa NVARCHAR(1000),
    HinhAnh NVARCHAR(500),
    TrangThai NVARCHAR(20) DEFAULT N'DangBan'
        CHECK (TrangThai IN (N'DangBan',N'NgungBan',N'HetHang')),
    FOREIGN KEY (MaLoai) REFERENCES LOAISANPHAM(MaLoai)
);
GO

CREATE TABLE THONGSOKYTHUAT (
    MaThongSo INT PRIMARY KEY IDENTITY(1,1),
    MaSP INT UNIQUE NOT NULL,
    CPU NVARCHAR(100),
    RAM NVARCHAR(50),
    OCung NVARCHAR(100),
    ManHinh NVARCHAR(100),
    VGA NVARCHAR(100),
    HeDieuHanh NVARCHAR(100),
    Pin NVARCHAR(100),
    TrongLuong NVARCHAR(50),
    KetNoi NVARCHAR(200),
    FOREIGN KEY (MaSP) REFERENCES SANPHAM(MaSP)
);
GO

CREATE TABLE NHANVIEN (
    MaNV INT PRIMARY KEY IDENTITY(1,1),
    TenNV NVARCHAR(150) NOT NULL,
    SoDienThoai VARCHAR(20) UNIQUE,
    Email NVARCHAR(150),
    DiaChi NVARCHAR(255),
    NgaySinh DATE,
    NgayVaoLam DATE DEFAULT GETDATE(),
    CCCD VARCHAR(12),
    GioiTinh NVARCHAR(10) CHECK (GioiTinh IN (N'Nam',N'Nu')),
    VaiTro NVARCHAR(20) CHECK (VaiTro IN (N'QuanLy',N'NhanVienBanHang')),
    TrangThai NVARCHAR(20) DEFAULT N'DangLam'
        CHECK (TrangThai IN (N'DangLam',N'NghiViec'))
);
GO

CREATE TABLE TAIKHOAN (
    MaTaiKhoan INT PRIMARY KEY IDENTITY(1,1),
    MaNV INT UNIQUE NOT NULL,
    TenDangNhap NVARCHAR(50) UNIQUE NOT NULL,
    MatKhauHash NVARCHAR(255) NOT NULL,
    TrangThai NVARCHAR(20) DEFAULT N'HoatDong'
        CHECK (TrangThai IN (N'HoatDong',N'KhoaTam',N'Huy')),
    LanDangNhapCuoi DATETIME,
    FOREIGN KEY (MaNV) REFERENCES NHANVIEN(MaNV)
);
GO

CREATE TABLE NHACUNGCAP (
    MaNhaCungCap INT PRIMARY KEY IDENTITY(1,1),
    TenNhaCungCap NVARCHAR(150) NOT NULL,
    SoDienThoai VARCHAR(20),
    Email NVARCHAR(150),
    DiaChi NVARCHAR(255),
    TrangThai NVARCHAR(20) DEFAULT N'HoatDong'
        CHECK (TrangThai IN (N'HoatDong',N'NgungHopTac'))
);
GO

CREATE TABLE NHACUNGCAP_SANPHAM (
    MaNhaCungCap INT NOT NULL,
    MaSP         INT NOT NULL,
    PRIMARY KEY (MaNhaCungCap, MaSP),
    FOREIGN KEY (MaNhaCungCap) REFERENCES NHACUNGCAP(MaNhaCungCap),
    FOREIGN KEY (MaSP)         REFERENCES SANPHAM(MaSP)
);
GO

CREATE TABLE PHIEUNHAP (
    MaPN INT PRIMARY KEY IDENTITY(1,1),
    MaNhaCungCap INT NOT NULL,
    MaNV INT NOT NULL,
    NgayNhap DATE DEFAULT GETDATE(),
    TongTien DECIMAL(18,2),
    GhiChu NVARCHAR(500),
    TrangThai NVARCHAR(20) DEFAULT N'HoanThanh'
        CHECK (TrangThai IN (N'HoanThanh',N'Huy',N'ChoXuLy')),
    FOREIGN KEY (MaNhaCungCap) REFERENCES NHACUNGCAP(MaNhaCungCap),
    FOREIGN KEY (MaNV) REFERENCES NHANVIEN(MaNV)
);
GO

CREATE TABLE CHITIETPHIEUNHAP (
    MaChiTietPN INT PRIMARY KEY IDENTITY(1,1),
    MaPN INT NOT NULL,
    MaSP INT NOT NULL,
    SoLuong INT NOT NULL CHECK (SoLuong > 0),
    DonGiaNhap DECIMAL(18,2) NOT NULL CHECK (DonGiaNhap >= 0),
    ThanhTien AS (SoLuong * DonGiaNhap) PERSISTED,
    FOREIGN KEY (MaPN) REFERENCES PHIEUNHAP(MaPN),
    FOREIGN KEY (MaSP) REFERENCES SANPHAM(MaSP)
);
GO

CREATE TABLE SERIAL (
    MaSerial INT PRIMARY KEY IDENTITY(1,1),
    SerialCode VARCHAR(50) UNIQUE NOT NULL,
    MaSP INT NOT NULL,
    MaChiTietPN INT,
    TrangThai NVARCHAR(20) DEFAULT N'TrongKho'
        CHECK (TrangThai IN (N'TrongKho',N'DaBan',N'BaoHanh',N'DoiTra',N'Loi')),
    NgayNhap DATE,
    NgayXuat DATE,
    FOREIGN KEY (MaSP) REFERENCES SANPHAM(MaSP),
    FOREIGN KEY (MaChiTietPN) REFERENCES CHITIETPHIEUNHAP(MaChiTietPN)
);
GO

CREATE TABLE KHACHHANG (
    MaKhachHang INT PRIMARY KEY IDENTITY(1,1),
    TenKhachHang NVARCHAR(150),
    SoDienThoai VARCHAR(20) UNIQUE,
    Email NVARCHAR(150),
    DiaChi NVARCHAR(255),
    NgaySinh DATE,
    GioiTinh NVARCHAR(10),
    DiemTichLuy INT DEFAULT 0 CHECK(DiemTichLuy >= 0),
    HangKhachHang NVARCHAR(20) DEFAULT N'VoHang'
        CHECK (HangKhachHang IN (N'KimCuong',N'Vang',N'Bac',N'Dong',N'VoHang')),
    PhanTramGiam DECIMAL(5,2) DEFAULT 0 CHECK(PhanTramGiam >= 0),
    NgayDangKy DATE DEFAULT GETDATE()
);
GO

CREATE TABLE HOADON (
    MaHoaDon INT PRIMARY KEY IDENTITY(1,1),
    MaKhachHang INT,
    MaNV INT NOT NULL,
    NgayLap DATETIME DEFAULT GETDATE(),
    TongTienHang DECIMAL(18,2) DEFAULT 0,
    PhanTramGiamHang DECIMAL(5,2) DEFAULT 0,
    GhiChu NVARCHAR(500),
    TrangThai NVARCHAR(20) DEFAULT N'HoanThanh'
        CHECK (TrangThai IN (N'HoanThanh',N'Huy',N'ChoXuLy')),
    TienGiamHang  AS (TongTienHang * PhanTramGiamHang / 100)                         PERSISTED,
    TienTruocVAT  AS (TongTienHang - TongTienHang * PhanTramGiamHang / 100)          PERSISTED,
    TienVAT       AS ((TongTienHang - TongTienHang * PhanTramGiamHang / 100) * 0.10) PERSISTED,
    TongThanhToan AS ((TongTienHang - TongTienHang * PhanTramGiamHang / 100) * 1.10) PERSISTED,
    FOREIGN KEY (MaKhachHang) REFERENCES KHACHHANG(MaKhachHang),
    FOREIGN KEY (MaNV) REFERENCES NHANVIEN(MaNV)
);
GO

CREATE TABLE CHITIETHOADON (
    MaChiTiet INT PRIMARY KEY IDENTITY(1,1),
    MaHoaDon INT NOT NULL,
    MaSP INT NOT NULL,
    MaSerial INT NOT NULL,
    SoLuong INT DEFAULT 1 CHECK(SoLuong>0),
    DonGia DECIMAL(18,2) NOT NULL,
    ThanhTien AS (SoLuong * DonGia) PERSISTED,
    FOREIGN KEY (MaHoaDon) REFERENCES HOADON(MaHoaDon),
    FOREIGN KEY (MaSP) REFERENCES SANPHAM(MaSP),
    FOREIGN KEY (MaSerial) REFERENCES SERIAL(MaSerial)
);
GO

CREATE TABLE THANHTOAN (
    MaThanhToan INT PRIMARY KEY IDENTITY(1,1),
    MaHoaDon INT NOT NULL,
    NgayThanhToan DATETIME DEFAULT GETDATE(),
    SoTien DECIMAL(18,2) NOT NULL,
    PhuongThuc NVARCHAR(50)
        CHECK (PhuongThuc IN (N'TienMat',N'ChuyenKhoan',N'TheNganHang',
                              N'TheTinDung',N'VNPAY',N'MoMo',N'ZaloPay')),
    TrangThai NVARCHAR(20) DEFAULT N'ThanhCong'
        CHECK (TrangThai IN (N'ThanhCong',N'ThatBai',N'ChoXuLy')),
    FOREIGN KEY (MaHoaDon) REFERENCES HOADON(MaHoaDon)
);
GO

CREATE TABLE BAOHANH (
    MaBaoHanh INT PRIMARY KEY IDENTITY(1,1),
    MaHoaDon INT NOT NULL,
    MaSP INT NOT NULL,
    MaSerial INT NOT NULL,
    NgayKichHoat DATE DEFAULT GETDATE(),
    NgayHetHan DATE,
    TrangThai NVARCHAR(50) DEFAULT N'DangBaoHanh',
    FOREIGN KEY (MaHoaDon) REFERENCES HOADON(MaHoaDon),
    FOREIGN KEY (MaSP) REFERENCES SANPHAM(MaSP),
    FOREIGN KEY (MaSerial) REFERENCES SERIAL(MaSerial)
);
GO

CREATE TABLE LICHSUBAOHANH (
    MaLichSu     INT          PRIMARY KEY IDENTITY(1,1),
    MaBaoHanh    INT          NOT NULL,
    ThoiGian     DATETIME     NOT NULL DEFAULT GETDATE(),
    MaNV         INT          NULL,
    TrangThaiCu  NVARCHAR(50) NULL,
    TrangThaiMoi NVARCHAR(50) NULL,
    GhiChu       NVARCHAR(500) NULL,
    FOREIGN KEY (MaBaoHanh) REFERENCES BAOHANH(MaBaoHanh) ON DELETE CASCADE,
    FOREIGN KEY (MaNV)      REFERENCES NHANVIEN(MaNV)
);
GO

CREATE INDEX IX_LICHSUBAOHANH_MaBaoHanh ON LICHSUBAOHANH(MaBaoHanh);
GO

CREATE TABLE DOITRA (
    MaDoiTra      INT            PRIMARY KEY IDENTITY(1,1),
    MaHoaDon      INT            NOT NULL,
    MaSP          INT            NOT NULL,
    MaSerial      INT            NOT NULL,
    SoLuongTra    INT            DEFAULT 1 CHECK (SoLuongTra > 0),
    LoaiDoiTra    NVARCHAR(20)
        CHECK (LoaiDoiTra IN (N'DoiSanPham', N'TraHang', N'BaoHanh')),
    MaSPMoi       INT            NULL,
    MaSerialMoi   INT            NULL,
    TienChenhLech DECIMAL(12,2)  DEFAULT 0,
    LyDo          NVARCHAR(500),
    MaNV          INT            NOT NULL,
    NgayYeuCau    DATE           DEFAULT GETDATE(),
    NgayXuLy      DATE           NULL,
    TrangThai     NVARCHAR(20)   DEFAULT N'ChoDuyet'
        CHECK (TrangThai IN (N'ChoDuyet', N'DangXuLy', N'TuChoi', N'HoanThanh')),
    GhiChu        NVARCHAR(500),
    FOREIGN KEY (MaHoaDon)    REFERENCES HOADON(MaHoaDon),
    FOREIGN KEY (MaSP)        REFERENCES SANPHAM(MaSP),
    FOREIGN KEY (MaSerial)    REFERENCES SERIAL(MaSerial),
    FOREIGN KEY (MaSPMoi)     REFERENCES SANPHAM(MaSP),
    FOREIGN KEY (MaNV)        REFERENCES NHANVIEN(MaNV)
);

CREATE INDEX IX_DOITRA_MaHoaDon  ON DOITRA(MaHoaDon);
CREATE INDEX IX_DOITRA_TrangThai ON DOITRA(TrangThai);
GO

/* =========================================================================
   PHẦN 2: THÊM DỮ LIỆU CƠ BẢN (DATA GỐC)
========================================================================= */

INSERT INTO NHANVIEN (TenNV, SoDienThoai, Email, NgaySinh, NgayVaoLam, VaiTro, TrangThai) VALUES
(N'Võ Đức Hoàng Vinh', '0901111111', 'vinh.vdh@laptopstore.vn', '2006-01-15', '2020-01-10', N'QuanLy',N'DangLam'),
(N'Đặng Lương Thế Anh','0902222222', 'anh.dlt@laptopstore.vn',  '2006-10-22', '2021-03-15', N'QuanLy', N'DangLam'),
(N'Trương Quốc Thái', '0903333333', 'thai.tq@laptopstore.vn',  '2006-12-10', '2022-06-01', N'QuanLy', N'DangLam'),
(N'Phan Ngọc Vinh', '0904444444', 'vinh.pn@laptopstore.vn',  '2006-03-25', '2023-01-10', N'QuanLy', N'DangLam'),
(N'Phan Thị Hoa','0903333331', 'hoa.pt@laptopstore.vn',  '1999-12-10', '2022-06-01', N'NhanVienBanHang', N'DangLam');
GO

INSERT INTO TAIKHOAN (MaNV, TenDangNhap, MatKhauHash, TrangThai) VALUES
(1, 'vinh',  '1',  N'HoatDong'),
(2, 'anhthe', '1', N'HoatDong'),
(3, 'thaitruong', '1', N'HoatDong'),
(4, 'vinhphan', '1', N'HoatDong'),
(5, 'hoaphan', '1', N'HoatDong');
GO

INSERT INTO NHACUNGCAP (TenNhaCungCap, SoDienThoai, Email, DiaChi) VALUES
(N'Lenovo Việt Nam Distributor','02873008899','sales@lenovo.vn',N'Quận 1, TP Hồ Chí Minh'),
(N'Acer Việt Nam Distributor','02873001234','contact@acer.vn',N'Quận 3, TP Hồ Chí Minh'),
(N'Apple Authorized Distributor','02873005566','sales@apple.vn',N'Quận 1, TP Hồ Chí Minh'),
(N'Dell Việt Nam Distributor','02873002211','sales@dell.vn',N'Quận 7, TP Hồ Chí Minh'),
(N'ASUS Việt Nam Distributor','02873003344','contact@asus.vn',N'Quận Tân Bình, TP Hồ Chí Minh'),
(N'Gear Computer Distributor','02873009988','sales@gearvn.com',N'Quận Bình Thạnh, TP Hồ Chí Minh');
GO

INSERT INTO LOAISANPHAM (TenLoai, MoTa) VALUES
(N'Laptop',   N'Máy tính xách tay'),
(N'Bàn phím', N'Bàn phím cơ, membrane, không dây'),
(N'Chuột',    N'Chuột có dây, không dây, gaming'),
(N'Màn hình', N'Màn hình HD chiến mọi thể loại'),
(N'RAM',      N'RAM PC và Laptop');
GO

INSERT INTO SANPHAM (TenSP, ThuongHieu, Gia, SoLuongTon, MaLoai) VALUES
(N'Lenovo Ideapad 3',N'Lenovo',15000000,5,1),
(N'Lenovo Thinkpad E14',N'Lenovo',20000000,5,1),
(N'Acer Aspire 5',N'Acer',14000000,5,1),
(N'Acer Nitro 5',N'Acer',23000000,5,1),
(N'Apple Macbook Air M1',N'Apple',25000000,5,1),
(N'Apple Macbook Pro M2',N'Apple',35000000,5,1),
(N'Dell Inspiron 15',N'Dell',18000000,5,1),
(N'Dell XPS 13',N'Dell',32000000,5,1),
(N'Asus Vivobook 15',N'Asus',17000000,5,1),
(N'Asus ROG Strix',N'Asus',30000000,5,1),
(N'Logitech K120',N'Logitech',300000,5,2),
(N'Logitech G Pro Keyboard',N'Logitech',2500000,5,2),
(N'Corsair K55 RGB',N'Corsair',1500000,5,2),
(N'Corsair K70 RGB',N'Corsair',3500000,5,2),
(N'Razer Blackwidow',N'Razer',2800000,5,2),
(N'Razer Huntsman',N'Razer',4200000,5,2),
(N'Logitech G102',N'Logitech',400000,5,3),
(N'Logitech G Pro Wireless',N'Logitech',3200000,5,3);
GO

INSERT INTO THONGSOKYTHUAT
(MaSP,CPU,RAM,OCung,ManHinh,VGA,HeDieuHanh,Pin,TrongLuong,KetNoi)
VALUES
(1,'Intel i5 1135G7','8GB','512GB SSD','15.6 FHD','Intel Iris Xe','Windows 11','45Wh','1.65kg','WiFi,Bluetooth'),
(2,'Intel i7 1165G7','16GB','512GB SSD','14 FHD','Intel Iris Xe','Windows 11','50Wh','1.59kg','WiFi,Bluetooth'),
(3,'Intel i5 1240P','8GB','512GB SSD','15.6 FHD','Intel Iris Xe','Windows 11','48Wh','1.7kg','WiFi,Bluetooth'),
(4,'Intel i7 12650H','16GB','1TB SSD','15.6 144Hz','RTX 3050','Windows 11','57Wh','2.2kg','WiFi,Bluetooth'),
(5,'Apple M1','8GB','256GB SSD','13.3 Retina','Apple GPU','macOS','49Wh','1.29kg','WiFi,Bluetooth'),
(6,'Apple M2','16GB','512GB SSD','13.3 Retina','Apple GPU','macOS','58Wh','1.4kg','WiFi,Bluetooth'),
(7,'Intel i5 1235U','8GB','512GB SSD','15.6 FHD','Intel Iris Xe','Windows 11','54Wh','1.8kg','WiFi,Bluetooth'),
(8,'Intel i7 1260P','16GB','1TB SSD','13.4 FHD','Intel Iris Xe','Windows 11','55Wh','1.27kg','WiFi,Bluetooth'),
(9,'Intel i5 1240P','8GB','512GB SSD','15.6 FHD','Intel Iris Xe','Windows 11','50Wh','1.7kg','WiFi,Bluetooth'),
(10,'Intel i7 12700H','16GB','1TB SSD','15.6 165Hz','RTX 3060','Windows 11','90Wh','2.3kg','WiFi,Bluetooth');
GO

INSERT INTO PHIEUNHAP (MaNhaCungCap, MaNV, NgayNhap, TongTien, GhiChu)
VALUES
(1,1,GETDATE(),0,N'Nhập laptop Lenovo'),
(2,1,GETDATE(),0,N'Nhập laptop Acer'),
(3,1,GETDATE(),0,N'Nhập Macbook Apple'),
(4,1,GETDATE(),0,N'Nhập laptop Dell'),
(5,1,GETDATE(),0,N'Nhập laptop Asus'),
(6,1,GETDATE(),0,N'Nhập phụ kiện Logitech Corsair Razer');
GO

INSERT INTO CHITIETPHIEUNHAP (MaPN, MaSP, SoLuong, DonGiaNhap) VALUES
(1,1,5,13000000), (1,2,5,18000000),
(2,3,5,12000000), (2,4,5,20000000),
(3,5,5,23000000), (3,6,5,32000000),
(4,7,5,16000000), (4,8,5,29000000),
(5,9,5,15000000), (5,10,5,26000000),
(6,11,5,200000),  (6,12,5,2000000),
(6,13,5,1200000), (6,14,5,3000000),
(6,15,5,2300000), (6,16,5,3800000),
(6,17,5,300000),  (6,18,5,2800000);
GO

-- Cập nhật tổng tiền cho các phiếu nhập ban đầu
UPDATE pn
SET pn.TongTien = t.Tong
FROM PHIEUNHAP pn
JOIN (
    SELECT MaPN, SUM(ThanhTien) AS Tong
    FROM CHITIETPHIEUNHAP
    GROUP BY MaPN
) t ON pn.MaPN = t.MaPN;
GO

-- SERIAL cho 18 SP gốc (MaSP 1-18)
-- [SỬA LỖI 1]: Bỏ SP19/SP20/SP21 khỏi khối này — 3 SP đó chưa tồn tại ở thời điểm này!
--              Serial của chúng sẽ được insert ở Phần 3 sau khi SANPHAM 19-21 được tạo.
INSERT INTO SERIAL (SerialCode, MaSP, NgayNhap) VALUES

-- MaSP = 1  Lenovo Ideapad 3
('SP01-001',1,GETDATE()),
('SP01-002',1,GETDATE()),
('SP01-003',1,GETDATE()),
('SP01-004',1,GETDATE()),
('SP01-005',1,GETDATE()),

-- MaSP = 2 Lenovo Thinkpad E14
('SP02-001',2,GETDATE()),
('SP02-002',2,GETDATE()),
('SP02-003',2,GETDATE()),
('SP02-004',2,GETDATE()),
('SP02-005',2,GETDATE()),

-- MaSP = 3 Acer Aspire 5
('SP03-001',3,GETDATE()),
('SP03-002',3,GETDATE()),
('SP03-003',3,GETDATE()),
('SP03-004',3,GETDATE()),
('SP03-005',3,GETDATE()),

-- MaSP = 4 Acer Nitro 5
('SP04-001',4,GETDATE()),
('SP04-002',4,GETDATE()),
('SP04-003',4,GETDATE()),
('SP04-004',4,GETDATE()),
('SP04-005',4,GETDATE()),

-- MaSP = 5 Macbook Air M1
('SP05-001',5,GETDATE()),
('SP05-002',5,GETDATE()),
('SP05-003',5,GETDATE()),
('SP05-004',5,GETDATE()),
('SP05-005',5,GETDATE()),

-- MaSP = 6 Macbook Pro M2
('SP06-001',6,GETDATE()),
('SP06-002',6,GETDATE()),
('SP06-003',6,GETDATE()),
('SP06-004',6,GETDATE()),
('SP06-005',6,GETDATE()),

-- MaSP = 7 Dell Inspiron 15
('SP07-001',7,GETDATE()),
('SP07-002',7,GETDATE()),
('SP07-003',7,GETDATE()),
('SP07-004',7,GETDATE()),
('SP07-005',7,GETDATE()),

-- MaSP = 8 Dell XPS 13
('SP08-001',8,GETDATE()),
('SP08-002',8,GETDATE()),
('SP08-003',8,GETDATE()),
('SP08-004',8,GETDATE()),
('SP08-005',8,GETDATE()),

-- MaSP = 9 Asus Vivobook 15
('SP09-001',9,GETDATE()),
('SP09-002',9,GETDATE()),
('SP09-003',9,GETDATE()),
('SP09-004',9,GETDATE()),
('SP09-005',9,GETDATE()),

-- MaSP = 10 Asus ROG Strix
('SP10-001',10,GETDATE()),
('SP10-002',10,GETDATE()),
('SP10-003',10,GETDATE()),
('SP10-004',10,GETDATE()),
('SP10-005',10,GETDATE()),

-- MaSP = 11 Logitech K120
('SP11-001',11,GETDATE()),
('SP11-002',11,GETDATE()),
('SP11-003',11,GETDATE()),
('SP11-004',11,GETDATE()),
('SP11-005',11,GETDATE()),

-- MaSP = 12 Logitech G Pro Keyboard
('SP12-001',12,GETDATE()),
('SP12-002',12,GETDATE()),
('SP12-003',12,GETDATE()),
('SP12-004',12,GETDATE()),
('SP12-005',12,GETDATE()),

-- MaSP = 13 Corsair K55
('SP13-001',13,GETDATE()),
('SP13-002',13,GETDATE()),
('SP13-003',13,GETDATE()),
('SP13-004',13,GETDATE()),
('SP13-005',13,GETDATE()),

-- MaSP = 14 Corsair K70
('SP14-001',14,GETDATE()),
('SP14-002',14,GETDATE()),
('SP14-003',14,GETDATE()),
('SP14-004',14,GETDATE()),
('SP14-005',14,GETDATE()),

-- MaSP = 15 Razer Blackwidow
('SP15-001',15,GETDATE()),
('SP15-002',15,GETDATE()),
('SP15-003',15,GETDATE()),
('SP15-004',15,GETDATE()),
('SP15-005',15,GETDATE()),

-- MaSP = 16 Razer Huntsman
('SP16-001',16,GETDATE()),
('SP16-002',16,GETDATE()),
('SP16-003',16,GETDATE()),
('SP16-004',16,GETDATE()),
('SP16-005',16,GETDATE()),

-- MaSP = 17 Logitech G102
('SP17-001',17,GETDATE()),
('SP17-002',17,GETDATE()),
('SP17-003',17,GETDATE()),
('SP17-004',17,GETDATE()),
('SP17-005',17,GETDATE()),

-- MaSP = 18 Logitech G Pro Wireless
('SP18-001',18,GETDATE()),
('SP18-002',18,GETDATE()),
('SP18-003',18,GETDATE()),
('SP18-004',18,GETDATE()),
('SP18-005',18,GETDATE());
GO

/* =========================================================================
   PHẦN 3: BỔ SUNG DỮ LIỆU RAM MỚI VÀO PHIẾU NHẬP SỐ 7
   (Giữ nguyên 100% data của bạn, do chạy theo tuần tự nên Identity SQL tự khớp)
========================================================================= */

-- 1. THÊM SẢN PHẨM MỚI (Tự động sinh MaSP = 19, 20, 21)
INSERT INTO SANPHAM (TenSP, ThuongHieu, Gia, SoLuongTon, MaLoai) VALUES
(N'RAM Corsair Vengeance LPX 16GB DDR4', N'Corsair', 1200000, 5, 5),    -- MaSP = 19
(N'RAM Kingston Fury Beast 16GB DDR4',   N'Kingston', 1100000, 5, 5),   -- MaSP = 20
(N'RAM G.Skill Trident Z RGB 16GB DDR4', N'G.Skill',  1500000, 5, 5);   -- MaSP = 21
GO

-- 2. TẠO PHIẾU NHẬP MỚI (Tự động sinh MaPN = 7)
INSERT INTO PHIEUNHAP (MaNhaCungCap, MaNV, NgayNhap, TongTien, GhiChu)
VALUES (6, 1, GETDATE(), 0, N'Nhập lô RAM mới từ Gear Computer');
GO

-- 3. THÊM VÀO CHI TIẾT PHIẾU NHẬP
--    MaPN=7, MaSP=19/20/21 → MaChiTietPN tự động = 19, 20, 21
INSERT INTO CHITIETPHIEUNHAP (MaPN, MaSP, SoLuong, DonGiaNhap) VALUES
(7, 19, 5, 800000),   -- Nhập RAM Corsair  → MaChiTietPN = 19
(7, 20, 5, 750000),   -- Nhập RAM Kingston → MaChiTietPN = 20
(7, 21, 5, 1100000);  -- Nhập RAM G.Skill  → MaChiTietPN = 21
GO

-- 4. CẬP NHẬT LẠI TỔNG TIỀN CHO PHIẾU NHẬP SỐ 7
UPDATE pn
SET pn.TongTien = t.Tong
FROM PHIEUNHAP pn
JOIN (
    SELECT MaPN, SUM(ThanhTien) AS Tong
    FROM CHITIETPHIEUNHAP
    GROUP BY MaPN
) t ON pn.MaPN = t.MaPN
WHERE pn.MaPN = 7;
GO

-- 5. THÊM SERIAL CHO 3 SẢN PHẨM RAM
--    [SỬA LỖI 2]: Chỉ insert ở đây — KHÔNG có ở Phần 2 nữa → không còn DUPLICATE SerialCode
--    MaChiTietPN 19/20/21 đã tồn tại sau bước 3 ở trên → FK hợp lệ
INSERT INTO SERIAL (SerialCode, MaSP, MaChiTietPN, NgayNhap) VALUES
-- MaSP=19, MaChiTietPN=19 (RAM Corsair)
('SP19-001',19,19,GETDATE()), ('SP19-002',19,19,GETDATE()), ('SP19-003',19,19,GETDATE()),
('SP19-004',19,19,GETDATE()), ('SP19-005',19,19,GETDATE()),
-- MaSP=20, MaChiTietPN=20 (RAM Kingston)
('SP20-001',20,20,GETDATE()), ('SP20-002',20,20,GETDATE()), ('SP20-003',20,20,GETDATE()),
('SP20-004',20,20,GETDATE()), ('SP20-005',20,20,GETDATE()),
-- MaSP=21, MaChiTietPN=21 (RAM G.Skill)
('SP21-001',21,21,GETDATE()), ('SP21-002',21,21,GETDATE()), ('SP21-003',21,21,GETDATE()),
('SP21-004',21,21,GETDATE()), ('SP21-005',21,21,GETDATE());
GO

/* =========================================================================
   PHẦN 4: LIÊN KẾT NHÀ CUNG CẤP & SẢN PHẨM (MAPPING ĐẦY ĐỦ CÁC HÃNG)
========================================================================= */

INSERT INTO NHACUNGCAP_SANPHAM (MaNhaCungCap, MaSP)
SELECT 1, MaSP FROM SANPHAM WHERE ThuongHieu = N'Lenovo'
UNION ALL
SELECT 2, MaSP FROM SANPHAM WHERE ThuongHieu = N'Acer'
UNION ALL
SELECT 3, MaSP FROM SANPHAM WHERE ThuongHieu = N'Apple'
UNION ALL
SELECT 4, MaSP FROM SANPHAM WHERE ThuongHieu = N'Dell'
UNION ALL
SELECT 5, MaSP FROM SANPHAM WHERE ThuongHieu = N'Asus'
UNION ALL
-- Gear Computer: Logitech, Corsair, Razer, Kingston, G.Skill
SELECT 6, MaSP FROM SANPHAM WHERE ThuongHieu IN (N'Logitech', N'Corsair', N'Razer', N'Kingston', N'G.Skill');
GO