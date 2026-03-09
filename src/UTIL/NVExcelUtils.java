package UTIL;

import DTO.NhanVienDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Tiện ích Import / Export Excel cho màn hình Nhân Viên.
 * Thư viện cần có trong /lib:
 *   - poi-5.x.x.jar
 *   - poi-ooxml-5.x.x.jar
 *   - poi-ooxml-full-5.x.x.jar (hoặc poi-ooxml-schemas)
 *   - commons-collections4-4.x.jar
 *   - xmlbeans-5.x.jar
 */
public class NVExcelUtils {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    // =========================================================================
    // EXPORT – xuất danh sách nhân viên ra file .xlsx
    // =========================================================================
    /**
     * Mở hộp thoại chọn nơi lưu, sau đó xuất danh sách nhân viên ra Excel.
     *
     * @param parent   Component cha (để căn giữa hộp thoại)
     * @param danhSach Danh sách nhân viên cần xuất
     */
    public static void exportExcel(Component parent, List<NhanVienDTO> danhSach) {
        if (danhSach == null || danhSach.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Không có dữ liệu để xuất!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hộp thoại chọn file lưu
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File("DanhSachNhanVien.xlsx"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".xlsx")) file = new File(file.getAbsolutePath() + ".xlsx");

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Danh Sach Nhan Vien");

            // ── Style tiêu đề lớn ────────────────────────────────────────────
            XSSFCellStyle titleStyle = wb.createCellStyle();
            XSSFFont titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)21, (byte)101, (byte)192}, null));
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // ── Style header cột ─────────────────────────────────────────────
            XSSFCellStyle headerStyle = wb.createCellStyle();
            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)10, (byte)60, (byte)130}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(headerStyle);

            // ── Style dữ liệu ─────────────────────────────────────────────────
            XSSFCellStyle dataStyle = wb.createCellStyle();
            XSSFFont dataFont = wb.createFont();
            dataFont.setFontHeightInPoints((short) 11);
            dataStyle.setFont(dataFont);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(dataStyle);

            XSSFCellStyle dataAltStyle = wb.createCellStyle();
            dataAltStyle.cloneStyleFrom(dataStyle);
            dataAltStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)245, (byte)250, (byte)255}, null));
            dataAltStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ── Hàng tiêu đề lớn (merge A1:K1) ──────────────────────────────
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH NHÂN VIÊN");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

            // ── Hàng header cột ───────────────────────────────────────────────
            String[] headers = {"Mã NV", "Họ và Tên", "Giới Tính", "Số Điện Thoại",
                    "Email", "Địa Chỉ", "Ngày Sinh", "Ngày Vào Làm", "CCCD", "Vai Trò", "Trạng Thái"};
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // ── Dữ liệu ───────────────────────────────────────────────────────
            for (int i = 0; i < danhSach.size(); i++) {
                NhanVienDTO nv = danhSach.get(i);
                Row row = sheet.createRow(i + 2);
                row.setHeightInPoints(22);
                XSSFCellStyle style = (i % 2 == 0) ? dataStyle : dataAltStyle;

                createCell(row, 0, String.valueOf(nv.getMaNV()), style);
                createCell(row, 1, nv.getTenNV(), style);
                createCell(row, 2, nv.getGioiTinh(), style);
                createCell(row, 3, nv.getSoDienThoai(), style);
                createCell(row, 4, nv.getEmail(), style);
                createCell(row, 5, nv.getDiaChi(), style);
                createCell(row, 6, nv.getNgaySinh()   != null ? SDF.format(nv.getNgaySinh())   : "", style);
                createCell(row, 7, nv.getNgayVaoLam() != null ? SDF.format(nv.getNgayVaoLam()) : "", style);
                createCell(row, 8, nv.getCccd(), style);
                createCell(row, 9, formatVaiTro(nv.getVaiTro()), style);
                createCell(row, 10, formatTrangThai(nv.getTrangThai()), style);
            }

            // ── Tự động điều chỉnh độ rộng cột ───────────────────────────────
            int[] colWidths = {8, 25, 10, 15, 28, 25, 13, 14, 15, 22, 16};
            for (int i = 0; i < colWidths.length; i++) {
                sheet.setColumnWidth(i, colWidths[i] * 256);
            }

            // ── Ghi file ──────────────────────────────────────────────────────
            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }

            JOptionPane.showMessageDialog(parent,
                    "Xuất Excel thành công!\nFile: " + file.getAbsolutePath(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Lỗi khi xuất Excel: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // IMPORT – đọc file .xlsx và trả về danh sách NhanVienDTO
    // =========================================================================
    /**
     * Mở hộp thoại chọn file Excel, đọc và trả về danh sách nhân viên.
     * Bỏ qua hàng tiêu đề (hàng 1) và hàng header (hàng 2).
     * Nếu có lỗi ở hàng nào sẽ bỏ qua hàng đó và tiếp tục.
     *
     * @param parent Component cha
     * @return Danh sách NhanVienDTO đọc được, hoặc null nếu người dùng hủy
     */
    public static List<NhanVienDTO> importExcel(Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file Excel để nhập");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return null;

        File file = chooser.getSelectedFile();
        List<NhanVienDTO> result = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook wb = new XSSFWorkbook(fis)) {

            XSSFSheet sheet = wb.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            // Bắt đầu từ hàng index 2 (hàng 3 trong Excel = bỏ qua tiêu đề + header)
            for (int i = 2; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Bỏ qua hàng trống (kiểm tra cột Tên NV)
                if (getCellStr(row, 1).trim().isEmpty()) continue;

                try {
                    NhanVienDTO nv = new NhanVienDTO();
                    // Cột 0: Mã NV — bỏ qua khi import (DB tự sinh)
                    nv.setTenNV(getCellStr(row, 1));
                    nv.setGioiTinh(getCellStr(row, 2));
                    nv.setSoDienThoai(getCellStr(row, 3));
                    nv.setEmail(getCellStr(row, 4));
                    nv.setDiaChi(getCellStr(row, 5));
                    nv.setNgaySinh(parseDate(getCellStr(row, 6)));
                    nv.setNgayVaoLam(parseDate(getCellStr(row, 7)));
                    nv.setCccd(getCellStr(row, 8));
                    nv.setVaiTro(parseVaiTro(getCellStr(row, 9)));
                    nv.setTrangThai(parseTrangThai(getCellStr(row, 10)));
                    result.add(nv);
                } catch (Exception e) {
                    errors.add("Hàng " + (i + 1) + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Không thể đọc file Excel: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Thông báo kết quả
        StringBuilder msg = new StringBuilder("Nhập thành công " + result.size() + " nhân viên.");
        if (!errors.isEmpty()) {
            msg.append("\n\nCác hàng bị lỗi (đã bỏ qua):");
            for (String err : errors) msg.append("\n  - ").append(err);
        }
        JOptionPane.showMessageDialog(parent, msg.toString(),
                errors.isEmpty() ? "Thành công" : "Thành công (có cảnh báo)",
                errors.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

        return result;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private static void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private static void setBorder(XSSFCellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private static String getCellStr(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC: return DateUtil.isCellDateFormatted(cell)
                    ? SDF.format(cell.getDateCellValue())
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default:      return "";
        }
    }

    private static Date parseDate(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try { return new Date(SDF.parse(str.trim()).getTime()); }
        catch (Exception e) { return null; }
    }

    private static String formatVaiTro(String raw) {
        if (raw == null) return "";
        if (raw.equalsIgnoreCase("NhanVienBanHang")) return "Nhân viên bán hàng";
        if (raw.equalsIgnoreCase("QuanLy"))          return "Quản lý";
        return raw;
    }

    private static String parseVaiTro(String display) {
        if (display == null) return "";
        if (display.equals("Nhân viên bán hàng")) return "NhanVienBanHang";
        if (display.equals("Quản lý"))            return "QuanLy";
        return display; // cho phép ghi thẳng giá trị DB
    }

    private static String formatTrangThai(String raw) {
        if (raw == null) return "";
        if (raw.equalsIgnoreCase("DangLam")) return "Đang làm việc";
        if (raw.equalsIgnoreCase("NghiViec")) return "Đã nghỉ việc";
        return raw;
    }

    private static String parseTrangThai(String display) {
        if (display == null) return "";
        if (display.equals("Đang làm việc")) return "DangLam";
        if (display.equals("Đã nghỉ việc"))  return "NghiViec";
        return display;
    }
}