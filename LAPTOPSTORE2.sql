USE LAPTOPSTORE3
GO

/* =====================================================
   DỮ LIỆU MẪU: KHÁCH HÀNG
===================================================== */
INSERT INTO KHACHHANG (TenKhachHang, SoDienThoai, Email, DiaChi, NgaySinh, GioiTinh, HangKhachHang, PhanTramGiam)
VALUES
(N'Nguyễn Văn An',     '0911000001', 'an.nv@gmail.com',     N'Quận 1, TP HCM',         '1995-03-12', N'Nam', N'Vang',     5),
(N'Trần Thị Bích',     '0911000002', 'bich.tt@gmail.com',   N'Quận 3, TP HCM',         '1998-07-22', N'Nu',  N'Bac',      3),
(N'Lê Hoàng Cường',    '0911000003', 'cuong.lh@gmail.com',  N'Bình Dương',              '1993-11-05', N'Nam', N'KimCuong', 10),
(N'Phạm Ngọc Dung',    '0911000004', 'dung.pn@gmail.com',   N'Quận Tân Bình, TP HCM',  '2000-01-30', N'Nu',  N'Dong',     1),
(N'Huỳnh Minh Đức',    '0911000005', 'duc.hm@gmail.com',    N'Quận 7, TP HCM',         '1990-09-18', N'Nam', N'VoHang',   0),
(N'Vũ Thị Lan',        '0911000006', 'lan.vt@gmail.com',    N'Quận Bình Thạnh, TP HCM','1997-05-25', N'Nu',  N'Bac',      3),
(N'Đỗ Quang Huy',      '0911000007', 'huy.dq@gmail.com',    N'Quận 12, TP HCM',        '1992-08-14', N'Nam', N'Vang',     5);
GO

/* =====================================================
   DỮ LIỆU MẪU: HÓA ĐƠN (HOADON)
   10 hóa đơn trải đều 2024 → 2026
===================================================== */
INSERT INTO HOADON (MaKhachHang, MaNV, NgayLap, TongTienHang, PhanTramGiamHang, GhiChu, TrangThai)
VALUES
(3,    5, '2024-01-15 09:30:00', 0, 10, N'Khách VIP Kim Cương - Q1/2024',     N'HoanThanh'),
(1,    5, '2024-04-20 14:00:00', 0,  5, N'Khách hạng Vàng - Q2/2024',         N'HoanThanh'),
(6,    5, '2024-07-08 10:30:00', 0,  3, N'Khách hạng Bạc - Q3/2024',          N'HoanThanh'),
(2,    5, '2024-10-25 15:45:00', 0,  3, N'Mua cuối năm Q4/2024',              N'HoanThanh'),
(7,    5, '2024-12-05 11:00:00', 0,  5, N'Tháng 12/2024',                     N'HoanThanh'),
(5,    5, '2025-02-10 09:00:00', 0,  0, N'Khách vãng lai Q1/2025',            N'HoanThanh'),
(4,    5, '2025-05-18 13:30:00', 0,  1, N'Khách hạng Đồng - Q2/2025',        N'HoanThanh'),
(1,    5, '2025-08-22 16:00:00', 0,  5, N'Nguyễn Văn An mua lần 2 Q3/2025',  N'HoanThanh'),
(3,    5, '2025-11-30 10:15:00', 0, 10, N'Lê Hoàng Cường cuối năm 2025',     N'HoanThanh'),
(NULL, 5, '2026-01-20 08:45:00', 0,  0, N'Khách lẻ đầu năm 2026',            N'HoanThanh');
GO

/* =====================================================
   CHI TIẾT HÓA ĐƠN (CHITIETHOADON)

   Mapping MaSerial (theo thứ tự INSERT SERIAL gốc):
     MaSP=1  Lenovo Ideapad 3        → MaSerial 1–5
     MaSP=2  Lenovo Thinkpad E14     → MaSerial 6–10
     MaSP=3  Acer Aspire 5           → MaSerial 11–15
     MaSP=4  Acer Nitro 5            → MaSerial 16–20
     MaSP=5  Macbook Air M1          → MaSerial 21–25
     MaSP=6  Macbook Pro M2          → MaSerial 26–30
     MaSP=7  Dell Inspiron 15        → MaSerial 31–35
     MaSP=8  Dell XPS 13             → MaSerial 36–40
     MaSP=9  Asus Vivobook 15        → MaSerial 41–45
     MaSP=10 Asus ROG Strix          → MaSerial 46–50
     MaSP=11 Logitech K120           → MaSerial 51–55
     MaSP=12 Logitech G Pro Keyboard → MaSerial 56–60
     MaSP=13 Corsair K55             → MaSerial 61–65
     MaSP=14 Corsair K70             → MaSerial 66–70
     MaSP=15 Razer Blackwidow        → MaSerial 71–75
     MaSP=16 Razer Huntsman          → MaSerial 76–80
     MaSP=17 Logitech G102           → MaSerial 81–85
     MaSP=18 Logitech G Pro Wireless → MaSerial 86–90
===================================================== */

-- HD001 (2024-01) — Lê Hoàng Cường: Macbook Pro M2 + Corsair K70 + Logitech G Pro Wireless
INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia) VALUES
(1,  6, 26, 1, 35000000),
(1, 14, 66, 1,  3500000),
(1, 18, 86, 1,  3200000);
GO

-- HD002 (2024-04) — Nguyễn Văn An: Asus ROG Strix + Razer Huntsman
INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia) VALUES
(2, 10, 46, 1, 30000000),
(2, 16, 76, 1,  4200000);
GO

-- HD003 (2024-07) — Vũ Thị Lan: Lenovo Thinkpad E14 + Logitech G Pro Keyboard
INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia) VALUES
(3,  2,  6, 1, 20000000),
(3, 12, 56, 1,  2500000);
GO

-- HD004 (2024-10) — Trần Thị Bích: Dell Inspiron 15 + Logitech K120 + Logitech G102
INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia) VALUES
(4,  7, 31, 1, 18000000),
(4, 11, 51, 1,   300000),
(4, 17, 81, 1,   400000);
GO

-- HD005 (2024-12) — Đỗ Quang Huy: Dell XPS 13 + Razer Blackwidow
INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia) VALUES
(5,  8, 36, 1, 32000000),
(5, 15, 71, 1,  2800000);
GO

-- HD006 (2025-02) — Huỳnh Minh Đức: Acer Nitro 5 + Corsair K55
INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia) VALUES
(6,  4, 16, 1, 23000000),
(6, 13, 61, 1,  1500000);
GO

-- HD007 (2025-05) — Phạm Ngọc Dung: Lenovo Ideapad 3 + Logitech G102 + Logitech K120
INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia) VALUES
(7,  1,  1, 1, 15000000),
(7, 17, 82, 1,   400000),
(7, 11, 52, 1,   300000);
GO

-- HD008 (2025-08) — Nguyễn Văn An: Macbook Air M1 + Logitech G Pro Wireless
INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia) VALUES
(8,  5, 21, 1, 25000000),
(8, 18, 87, 1,  3200000);
GO

-- HD009 (2025-11) — Lê Hoàng Cường: Asus Vivobook 15 + Razer Huntsman + Corsair K70
INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia) VALUES
(9,  9, 41, 1, 17000000),
(9, 16, 77, 1,  4200000),
(9, 14, 67, 1,  3500000);
GO

-- HD010 (2026-01) — Khách lẻ: Acer Aspire 5 + Corsair K55 + Logitech G102
INSERT INTO CHITIETHOADON (MaHoaDon, MaSP, MaSerial, SoLuong, DonGia) VALUES
(10,  3, 11, 1, 14000000),
(10, 13, 62, 1,  1500000),
(10, 17, 83, 1,   400000);
GO

/* =====================================================
   CẬP NHẬT TongTienHang TỪ CHITIETHOADON
===================================================== */
UPDATE hd
SET hd.TongTienHang = ct.TongTien
FROM HOADON hd
JOIN (
    SELECT MaHoaDon, SUM(ThanhTien) AS TongTien
    FROM CHITIETHOADON
    GROUP BY MaHoaDon
) ct ON hd.MaHoaDon = ct.MaHoaDon;
GO

/* =====================================================
   CẬP NHẬT SERIAL ĐÃ BÁN → TrangThai = 'DaBan'
===================================================== */
UPDATE s
SET s.TrangThai = N'DaBan',
    s.NgayXuat  = CAST(hd.NgayLap AS DATE)
FROM SERIAL s
JOIN CHITIETHOADON ct ON ct.MaSerial  = s.MaSerial
JOIN HOADON hd        ON hd.MaHoaDon = ct.MaHoaDon;
GO

/* =====================================================
   THANH TOÁN — đa dạng phương thức
===================================================== */
INSERT INTO THANHTOAN (MaHoaDon, NgayThanhToan, SoTien, PhuongThuc, TrangThai)
SELECT hd.MaHoaDon, hd.NgayLap, hd.TongThanhToan, pm.PhuongThuc, N'ThanhCong'
FROM HOADON hd
JOIN (VALUES
    (1,  N'TheNganHang'),
    (2,  N'TienMat'),
    (3,  N'ChuyenKhoan'),
    (4,  N'MoMo'),
    (5,  N'VNPAY'),
    (6,  N'TienMat'),
    (7,  N'ZaloPay'),
    (8,  N'TheTinDung'),
    (9,  N'ChuyenKhoan'),
    (10, N'TienMat')
) pm(MaHoaDon, PhuongThuc) ON hd.MaHoaDon = pm.MaHoaDon;
GO