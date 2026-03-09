package UTIL;

import DTO.HoaDonDTO;
import DTO.ChiTietHoaDonDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tiện ích Export Excel cho màn hình Hóa Đơn.
 * Thư viện cần có trong /lib:
 *   - poi-5.x.x.jar  /  poi-ooxml-5.x.x.jar
 *   - poi-ooxml-full-5.x.x.jar  /  commons-collections4  /  xmlbeans
 */
public class HoaDonExcelUtils {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat     CF  = new DecimalFormat("#,###");

    // Màu xanh chủ đạo giống NhanVienPanel
    private static final byte[] COLOR_PRIMARY      = {(byte)21,  (byte)101, (byte)192};
    private static final byte[] COLOR_PRIMARY_DARK = {(byte)10,  (byte)60,  (byte)130};
    private static final byte[] COLOR_ROW_ALT      = {(byte)245, (byte)250, (byte)255};

    // =========================================================================
    // EXPORT DANH SÁCH HÓA ĐƠN
    // =========================================================================
    public static void exportDanhSach(Component parent, List<HoaDonDTO> danhSach) {
        if (danhSach == null || danhSach.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Không có dữ liệu để xuất!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File("DanhSachHoaDon.xlsx"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".xlsx")) file = new File(file.getAbsolutePath() + ".xlsx");

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Danh Sach Hoa Don");

            XSSFCellStyle titleStyle  = makeTitleStyle(wb);
            XSSFCellStyle headerStyle = makeHeaderStyle(wb);
            XSSFCellStyle dataStyle   = makeDataStyle(wb, false);
            XSSFCellStyle dataAlt     = makeDataStyle(wb, true);
            XSSFCellStyle numStyle    = makeNumStyle(wb, false);
            XSSFCellStyle numAlt      = makeNumStyle(wb, true);

            // Hàng tiêu đề lớn
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell tc = titleRow.createCell(0);
            tc.setCellValue("DANH SÁCH HÓA ĐƠN BÁN HÀNG");
            tc.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

            // Hàng header cột
            String[] headers = {
                "Mã HĐ", "Mã KH", "Mã NV", "Ngày Lập",
                "Tiền Hàng (đ)", "% Giảm", "Tiền Giảm (đ)",
                "Trước VAT (đ)", "VAT (đ)", "Tổng TT (đ)",
                "Ghi Chú", "Trạng Thái"
            };
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Dữ liệu
            for (int i = 0; i < danhSach.size(); i++) {
                HoaDonDTO hd  = danhSach.get(i);
                Row row       = sheet.createRow(i + 2);
                row.setHeightInPoints(22);
                XSSFCellStyle ds = (i % 2 == 0) ? dataStyle : dataAlt;
                XSSFCellStyle ns = (i % 2 == 0) ? numStyle  : numAlt;

                createCell(row, 0,  String.valueOf(hd.getMaHoaDon()),                                              ds);
                createCell(row, 1,  hd.getMaKhachHang() != null ? String.valueOf(hd.getMaKhachHang()) : "Vãng lai", ds);
                createCell(row, 2,  String.valueOf(hd.getMaNV()),                                                  ds);
                createCell(row, 3,  hd.getNgayLap() != null ? hd.getNgayLap().format(DTF) : "—",                  ds);
                createCell(row, 4,  fmt(hd.getTongTienHang()),                                                     ns);
                createCell(row, 5,  hd.getPhanTramGiamHang() != null
                                        ? hd.getPhanTramGiamHang().stripTrailingZeros().toPlainString() + "%" : "0%", ds);
                createCell(row, 6,  fmt(hd.getTienGiamHang()),                                                    ns);
                createCell(row, 7,  fmt(hd.getTienTruocVAT()),                                                    ns);
                createCell(row, 8,  fmt(hd.getTienVAT()),                                                         ns);
                createCell(row, 9,  fmt(hd.getTongThanhToan()),                                                   ns);
                createCell(row, 10, hd.getGhiChu() != null ? hd.getGhiChu() : "",                                ds);
                createCell(row, 11, formatTrangThai(hd.getTrangThai()),                                           ds);
            }

            // Độ rộng cột
            int[] widths = {8, 8, 8, 18, 16, 8, 16, 16, 14, 18, 22, 13};
            for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i] * 256);

            try (FileOutputStream fos = new FileOutputStream(file)) { wb.write(fos); }

            JOptionPane.showMessageDialog(parent,
                    "Xuất Excel thành công!\nFile: " + file.getAbsolutePath(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Lỗi khi xuất Excel: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // EXPORT CHI TIẾT 1 HÓA ĐƠN (đầu phiếu + bảng sản phẩm + tổng kết)
    // =========================================================================
    public static void exportChiTiet(Component parent, HoaDonDTO hd, List<ChiTietHoaDonDTO> chiTiets) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu Hóa Đơn Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File("HoaDon_" + hd.getMaHoaDon() + ".xlsx"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".xlsx")) file = new File(file.getAbsolutePath() + ".xlsx");

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("HoaDon_" + hd.getMaHoaDon());

            XSSFCellStyle titleStyle  = makeTitleStyle(wb);
            XSSFCellStyle headerStyle = makeHeaderStyle(wb);
            XSSFCellStyle labelStyle  = makeLabelStyle(wb);
            XSSFCellStyle valueStyle  = makeValueStyle(wb);
            XSSFCellStyle dataStyle   = makeDataStyle(wb, false);
            XSSFCellStyle dataAlt     = makeDataStyle(wb, true);
            XSSFCellStyle numStyle    = makeNumStyle(wb, false);
            XSSFCellStyle numAlt      = makeNumStyle(wb, true);
            XSSFCellStyle totalStyle  = makeTotalStyle(wb);

            int r = 0;

            // Tiêu đề
            Row titleRow = sheet.createRow(r++);
            titleRow.setHeightInPoints(32);
            Cell tc = titleRow.createCell(0);
            tc.setCellValue("HÓA ĐƠN BÁN HÀNG  —  #" + hd.getMaHoaDon());
            tc.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            r++; // trống

            // Thông tin đầu hóa đơn
            r = infoRow(sheet, r, labelStyle, valueStyle,
                    "Mã hóa đơn:", String.valueOf(hd.getMaHoaDon()),
                    "Trạng thái:", formatTrangThai(hd.getTrangThai()));
            r = infoRow(sheet, r, labelStyle, valueStyle,
                    "Khách hàng:", hd.getMaKhachHang() != null ? "KH #" + hd.getMaKhachHang() : "Vãng lai",
                    "Nhân viên:", "NV #" + hd.getMaNV());
            r = infoRow(sheet, r, labelStyle, valueStyle,
                    "Ngày lập:", hd.getNgayLap() != null ? hd.getNgayLap().format(DTF) : "—",
                    "Ghi chú:", hd.getGhiChu() != null ? hd.getGhiChu() : "");

            r++; // trống

            // Header bảng sản phẩm
            String[] hdrs = {"STT", "Tên Sản Phẩm", "Mã SP", "Mã Serial", "Số Lượng", "Đơn Giá (đ)", "Thành Tiền (đ)"};
            Row hRow = sheet.createRow(r++);
            hRow.setHeightInPoints(24);
            for (int i = 0; i < hdrs.length; i++) {
                Cell c = hRow.createCell(i);
                c.setCellValue(hdrs[i]);
                c.setCellStyle(headerStyle);
            }

            // Dữ liệu sản phẩm
            if (chiTiets != null) {
                for (int i = 0; i < chiTiets.size(); i++) {
                    ChiTietHoaDonDTO ct = chiTiets.get(i);
                    Row row = sheet.createRow(r++);
                    row.setHeightInPoints(22);
                    XSSFCellStyle ds = (i % 2 == 0) ? dataStyle : dataAlt;
                    XSSFCellStyle ns = (i % 2 == 0) ? numStyle  : numAlt;
                    createCell(row, 0, String.valueOf(i + 1),                              ds);
                    createCell(row, 1, ct.getTenSP() != null ? ct.getTenSP() : "—",       ds);
                    createCell(row, 2, String.valueOf(ct.getMaSP()),                       ds);
                    createCell(row, 3, String.valueOf(ct.getMaSerial()),                   ds);
                    createCell(row, 4, String.valueOf(ct.getSoLuong()),                    ds);
                    createCell(row, 5, fmt(ct.getDonGia()),                                ns);
                    createCell(row, 6, fmt(ct.getThanhTien()),                             ns);
                }
            }

            r++; // trống

            // Tổng kết — cột 5-6
            r = summaryRow(sheet, r, labelStyle, valueStyle, "Tổng tiền hàng:",   fmt(hd.getTongTienHang()));
            String pctStr = hd.getPhanTramGiamHang() != null
                    ? hd.getPhanTramGiamHang().stripTrailingZeros().toPlainString() : "0";
            r = summaryRow(sheet, r, labelStyle, valueStyle, "Giảm giá (" + pctStr + "%):", fmt(hd.getTienGiamHang()));
            r = summaryRow(sheet, r, labelStyle, valueStyle, "Trước VAT:",         fmt(hd.getTienTruocVAT()));
            r = summaryRow(sheet, r, labelStyle, valueStyle, "VAT (10%):",         fmt(hd.getTienVAT()));

            // Dòng tổng thanh toán nổi bật
            Row totalRow = sheet.createRow(r++);
            totalRow.setHeightInPoints(26);
            Cell lbl = totalRow.createCell(5); lbl.setCellValue("TỔNG THANH TOÁN:");  lbl.setCellStyle(totalStyle);
            Cell val = totalRow.createCell(6); val.setCellValue(fmt(hd.getTongThanhToan()) + " VNĐ"); val.setCellStyle(totalStyle);

            // Độ rộng cột
            int[] widths = {6, 30, 8, 14, 10, 18, 18};
            for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i] * 256);

            try (FileOutputStream fos = new FileOutputStream(file)) { wb.write(fos); }

            int open = JOptionPane.showConfirmDialog(parent,
                    "Xuất hóa đơn Excel thành công!\nFile: " + file.getAbsolutePath() + "\n\nMở file ngay?",
                    "Thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported())
                Desktop.getDesktop().open(file);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Lỗi khi xuất Excel: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // STYLE FACTORY
    // =========================================================================
    private static XSSFCellStyle makeTitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 15);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(COLOR_PRIMARY, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private static XSSFCellStyle makeHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 11);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(COLOR_PRIMARY_DARK, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s); return s;
    }

    private static XSSFCellStyle makeLabelStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 11);
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(COLOR_ROW_ALT, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s); return s;
    }

    private static XSSFCellStyle makeValueStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont(); f.setFontHeightInPoints((short) 11);
        s.setFont(f);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s); return s;
    }

    private static XSSFCellStyle makeDataStyle(XSSFWorkbook wb, boolean alt) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont(); f.setFontHeightInPoints((short) 11);
        s.setFont(f);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        if (alt) {
            s.setFillForegroundColor(new XSSFColor(COLOR_ROW_ALT, null));
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        setBorder(s); return s;
    }

    private static XSSFCellStyle makeNumStyle(XSSFWorkbook wb, boolean alt) {
        XSSFCellStyle s = makeDataStyle(wb, alt);
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private static XSSFCellStyle makeTotalStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 12);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(COLOR_PRIMARY, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.RIGHT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s); return s;
    }

    // =========================================================================
    // ROW HELPERS
    // =========================================================================
    private static int infoRow(XSSFSheet sheet, int idx,
                                XSSFCellStyle lbl, XSSFCellStyle val,
                                String l1, String v1, String l2, String v2) {
        Row r = sheet.createRow(idx); r.setHeightInPoints(22);
        createCell(r, 0, l1, lbl); createCell(r, 1, v1, val);
        createCell(r, 4, l2, lbl); createCell(r, 5, v2, val);
        return idx + 1;
    }

    private static int summaryRow(XSSFSheet sheet, int idx,
                                   XSSFCellStyle lbl, XSSFCellStyle val,
                                   String label, String value) {
        Row r = sheet.createRow(idx); r.setHeightInPoints(20);
        createCell(r, 5, label, lbl);
        createCell(r, 6, value, val);
        return idx + 1;
    }

    // =========================================================================
    // CELL / FORMAT HELPERS
    // =========================================================================
    private static void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private static void setBorder(XSSFCellStyle style) {
        style.setBorderTop(BorderStyle.THIN);    style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);   style.setBorderRight(BorderStyle.THIN);
    }

    private static String fmt(BigDecimal val) {
        return val != null ? CF.format(val) : "0";
    }

    public static String formatTrangThai(String raw) {
        if (raw == null) return "—";
        switch (raw) {
            case "HoanThanh": return "Hoàn thành";
            case "Huy":       return "Đã hủy";
            case "ChoXuLy":   return "Chờ xử lý";
            default:          return raw;
        }
    }
}