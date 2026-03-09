package UTIL;

import DTO.PhieuNhapDTO;
import DTO.ChiTietPhieuNhapDTO;
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
 * Tiện ích Export Excel cho màn hình Phiếu Nhập.
 * Thư viện cần có trong /lib:
 *   - poi-5.x.x.jar  /  poi-ooxml-5.x.x.jar
 *   - poi-ooxml-full-5.x.x.jar  /  commons-collections4  /  xmlbeans
 */
public class PhieuNhapExcelUtils {

    private static final DateTimeFormatter DF  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat     CF  = new DecimalFormat("#,###");

    private static final byte[] COLOR_PRIMARY      = {(byte)21,  (byte)101, (byte)192};
    private static final byte[] COLOR_PRIMARY_DARK = {(byte)10,  (byte)60,  (byte)130};
    private static final byte[] COLOR_ROW_ALT      = {(byte)245, (byte)250, (byte)255};
    private static final byte[] COLOR_TOTAL        = {(byte)232, (byte)245, (byte)233}; // xanh lá nhạt

    // =========================================================================
    // EXPORT DANH SÁCH PHIẾU NHẬP
    // =========================================================================
    public static void exportDanhSach(Component parent, List<PhieuNhapDTO> danhSach) {
        if (danhSach == null || danhSach.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Không có dữ liệu để xuất!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File("DanhSachPhieuNhap.xlsx"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".xlsx")) file = new File(file.getAbsolutePath() + ".xlsx");

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Danh Sach Phieu Nhap");

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
            tc.setCellValue("DANH SÁCH PHIẾU NHẬP HÀNG");
            tc.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            // Hàng header cột
            String[] headers = {
                "Mã PN", "Mã NCC", "Mã NV", "Ngày Nhập",
                "Tổng Tiền (đ)", "Ghi Chú", "Trạng Thái"
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
                PhieuNhapDTO pn = danhSach.get(i);
                Row row         = sheet.createRow(i + 2);
                row.setHeightInPoints(22);
                XSSFCellStyle ds = (i % 2 == 0) ? dataStyle : dataAlt;
                XSSFCellStyle ns = (i % 2 == 0) ? numStyle  : numAlt;

                createCell(row, 0, String.valueOf(pn.getMaPN()),                                           ds);
                createCell(row, 1, String.valueOf(pn.getMaNhaCungCap()),                                   ds);
                createCell(row, 2, String.valueOf(pn.getMaNV()),                                           ds);
                createCell(row, 3, pn.getNgayNhap() != null ? pn.getNgayNhap().format(DF) : "—",         ds);
                createCell(row, 4, fmt(pn.getTongTien()),                                                  ns);
                createCell(row, 5, pn.getGhiChu() != null ? pn.getGhiChu() : "",                         ds);
                createCell(row, 6, formatTrangThai(pn.getTrangThai()),                                     ds);
            }

            // Độ rộng cột
            int[] widths = {8, 10, 8, 16, 18, 26, 14};
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
    // EXPORT CHI TIẾT 1 PHIẾU NHẬP (đầu phiếu + bảng sản phẩm + tổng kết)
    // =========================================================================
    public static void exportChiTiet(Component parent, PhieuNhapDTO pn, List<ChiTietPhieuNhapDTO> chiTiets) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu Phiếu Nhập Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File("PhieuNhap_" + pn.getMaPN() + ".xlsx"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".xlsx")) file = new File(file.getAbsolutePath() + ".xlsx");

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("PhieuNhap_" + pn.getMaPN());

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
            tc.setCellValue("PHIẾU NHẬP HÀNG  —  #PN" + String.format("%03d", pn.getMaPN()));
            tc.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            r++; // trống

            // Thông tin đầu phiếu (2 cột: label | value | label | value)
            r = infoRow(sheet, r, labelStyle, valueStyle,
                    "Mã phiếu nhập:", "PN" + String.format("%03d", pn.getMaPN()),
                    "Ngày nhập:", pn.getNgayNhap() != null ? pn.getNgayNhap().format(DF) : "—");
            r = infoRow(sheet, r, labelStyle, valueStyle,
                    "Mã nhà cung cấp:", String.valueOf(pn.getMaNhaCungCap()),
                    "Mã nhân viên:", String.valueOf(pn.getMaNV()));
            r = infoRow(sheet, r, labelStyle, valueStyle,
                    "Trạng thái:", formatTrangThai(pn.getTrangThai()),
                    "Ghi chú:", pn.getGhiChu() != null ? pn.getGhiChu() : "—");

            r++; // trống

            // Header bảng chi tiết
            String[] headers = {"STT", "Mã SP", "Số Lượng", "Đơn Giá Nhập (đ)", "Thành Tiền (đ)", "Ghi Chú"};
            Row headerRow = sheet.createRow(r++);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Dữ liệu chi tiết
            if (chiTiets != null) {
                for (int i = 0; i < chiTiets.size(); i++) {
                    ChiTietPhieuNhapDTO ct = chiTiets.get(i);
                    Row row = sheet.createRow(r++);
                    row.setHeightInPoints(22);
                    XSSFCellStyle ds = (i % 2 == 0) ? dataStyle : dataAlt;
                    XSSFCellStyle ns = (i % 2 == 0) ? numStyle  : numAlt;

                    createCell(row, 0, String.valueOf(i + 1),                                    ds);
                    createCell(row, 1, String.valueOf(ct.getMaSP()),                             ds);
                    createCell(row, 2, String.valueOf(ct.getSoLuong()),                          ns);
                    createCell(row, 3, fmt(ct.getDonGiaNhap()),                                  ns);
                    createCell(row, 4, fmt(ct.getThanhTien()),                                   ns);
                    createCell(row, 5, ct.getGhiChu() != null ? ct.getGhiChu() : "",            ds);
                }
            }

            r++; // trống

            // Tổng kết — cột 4-5
            Row totalRow = sheet.createRow(r);
            totalRow.setHeightInPoints(26);
            Cell lbl = totalRow.createCell(3); lbl.setCellValue("TỔNG TIỀN NHẬP:"); lbl.setCellStyle(totalStyle);
            Cell val = totalRow.createCell(4); val.setCellValue(fmt(pn.getTongTien()) + " VNĐ"); val.setCellStyle(totalStyle);

            // Độ rộng cột
            int[] widths = {6, 10, 12, 20, 20, 26};
            for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i] * 256);

            try (FileOutputStream fos = new FileOutputStream(file)) { wb.write(fos); }

            int open = JOptionPane.showConfirmDialog(parent,
                    "Xuất phiếu nhập Excel thành công!\nFile: " + file.getAbsolutePath() + "\n\nMở file ngay?",
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
        createCell(r, 3, l2, lbl); createCell(r, 4, v2, val);
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
            default:          return raw;
        }
    }
}