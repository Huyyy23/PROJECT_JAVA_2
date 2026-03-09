package UTIL;

import DTO.NhanVienDTO;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.itextpdf.layout.borders.SolidBorder;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Tiện ích xuất PDF cho màn hình Nhân Viên.
 * Thư viện cần có trong /lib:
 *   - itext-core (kernel, layout, io, commons) phiên bản 7.x
 *     Tải tại: https://mvnrepository.com/artifact/com.itextpdf/itext7-core
 *
 * Hỗ trợ Tiếng Việt qua font FreeSans (hoặc Arial Unicode) nhúng vào PDF.
 * Đặt font vào: src/fonts/FreeSans.ttf
 */
public class NVPDFUtils {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    // Màu sắc theo theme của ứng dụng
    private static final DeviceRgb PRIMARY      = new DeviceRgb(21, 101, 192);
    private static final DeviceRgb PRIMARY_DARK = new DeviceRgb(10, 60, 130);
    private static final DeviceRgb ROW_ALT      = new DeviceRgb(245, 250, 255);
    private static final DeviceRgb WHITE        = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb TEXT_DARK    = new DeviceRgb(10, 60, 130);

    // =========================================================================
    // EXPORT DANH SÁCH – xuất toàn bộ bảng ra PDF (nằm ngang A4)
    // =========================================================================
    /**
     * Mở hộp thoại lưu file, rồi xuất danh sách nhân viên ra PDF.
     */
    public static void exportDanhSach(Component parent, List<NhanVienDTO> danhSach) {
        if (danhSach == null || danhSach.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Không có dữ liệu để xuất!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File("DanhSachNhanVien.pdf"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".pdf")) file = new File(file.getAbsolutePath() + ".pdf");

        try {
            PdfFont font     = loadFont();
            PdfFont fontBold = loadFontBold();

            PdfDocument pdf  = new PdfDocument(new PdfWriter(file));
            Document    doc  = new Document(pdf, PageSize.A4.rotate()); // nằm ngang
            doc.setMargins(30, 30, 30, 30);

            // ── Tiêu đề ──────────────────────────────────────────────────────
            Paragraph title = new Paragraph("DANH SÁCH NHÂN VIÊN")
                    .setFont(fontBold).setFontSize(16)
                    .setFontColor(PRIMARY_DARK)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4);
            doc.add(title);

            // ── Ngày xuất ────────────────────────────────────────────────────
            String today = SDF.format(new java.util.Date());
            Paragraph sub = new Paragraph("Ngày xuất: " + today + "   |   Tổng: " + danhSach.size() + " nhân viên")
                    .setFont(font).setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(12);
            doc.add(sub);

            // ── Bảng dữ liệu ─────────────────────────────────────────────────
            float[] colWidths = {2f, 5f, 2.5f, 3.5f, 5f, 3.5f, 3f, 3f, 4.5f, 4f};
            Table table = new Table(UnitValue.createPercentArray(colWidths))
                    .useAllAvailableWidth();

            // Header
            String[] headers = {"Mã NV", "Họ và Tên", "Giới Tính", "Số ĐT",
                    "Email", "Địa Chỉ", "Ngày Sinh", "Ngày V.Làm", "Vai Trò", "Trạng Thái"};
            for (String h : headers) {
                table.addHeaderCell(
                    new Cell().add(new Paragraph(h).setFont(fontBold).setFontSize(10)
                            .setFontColor(WHITE))
                        .setBackgroundColor(PRIMARY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(6)
                        .setBorder(new SolidBorder(WHITE, 0.5f))
                );
            }

            // Dữ liệu
            for (int i = 0; i < danhSach.size(); i++) {
                NhanVienDTO nv = danhSach.get(i);
                DeviceRgb rowBg = (i % 2 == 0) ? WHITE : ROW_ALT;
                SolidBorder border = new SolidBorder(new DeviceRgb(180, 210, 240), 0.3f);

                String[] vals = {
                    String.valueOf(nv.getMaNV()),
                    nv.getTenNV(),
                    nv.getGioiTinh() != null ? nv.getGioiTinh() : "—",
                    nv.getSoDienThoai() != null ? nv.getSoDienThoai() : "—",
                    nv.getEmail() != null ? nv.getEmail() : "—",
                    nv.getDiaChi() != null ? nv.getDiaChi() : "—",
                    nv.getNgaySinh()   != null ? SDF.format(nv.getNgaySinh())   : "—",
                    nv.getNgayVaoLam() != null ? SDF.format(nv.getNgayVaoLam()) : "—",
                    formatVaiTro(nv.getVaiTro()),
                    formatTrangThai(nv.getTrangThai())
                };

                for (int j = 0; j < vals.length; j++) {
                    TextAlignment align = (j == 0) ? TextAlignment.CENTER : TextAlignment.LEFT;
                    Cell cell = new Cell()
                        .add(new Paragraph(vals[j]).setFont(font).setFontSize(9.5f)
                                .setFontColor(TEXT_DARK))
                        .setBackgroundColor(rowBg)
                        .setTextAlignment(align)
                        .setPaddingTop(5).setPaddingBottom(5)
                        .setPaddingLeft(6).setPaddingRight(6)
                        .setBorder(border);
                    table.addCell(cell);
                }
            }

            doc.add(table);

            // ── Footer ────────────────────────────────────────────────────────
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("© LaptopStore – Tài liệu nội bộ")
                    .setFont(font).setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.RIGHT));

            doc.close();

            // Hỏi người dùng có muốn mở file luôn không
            int open = JOptionPane.showConfirmDialog(parent,
                    "Xuất PDF thành công!\nFile: " + file.getAbsolutePath() + "\n\nMở file ngay?",
                    "Thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Lỗi khi xuất PDF: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // EXPORT CHI TIẾT – xuất thông tin 1 nhân viên (phiếu nhân sự)
    // =========================================================================
    /**
     * Xuất phiếu thông tin chi tiết của một nhân viên ra PDF.
     */
    public static void exportChiTiet(Component parent, NhanVienDTO nv) {
        if (nv == null) {
            JOptionPane.showMessageDialog(parent, "Vui lòng chọn một nhân viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu Phiếu Nhân Sự");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File("PhieuNhanSu_NV" + nv.getMaNV() + ".pdf"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".pdf")) file = new File(file.getAbsolutePath() + ".pdf");

        try {
            PdfFont font     = loadFont();
            PdfFont fontBold = loadFontBold();

            PdfDocument pdf = new PdfDocument(new PdfWriter(file));
            Document    doc = new Document(pdf, PageSize.A4);
            doc.setMargins(50, 50, 50, 50);

            // ── Header phiếu ─────────────────────────────────────────────────
            doc.add(new Paragraph("LAPTOP STORE")
                    .setFont(fontBold).setFontSize(18)
                    .setFontColor(PRIMARY)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("PHIẾU THÔNG TIN NHÂN SỰ")
                    .setFont(fontBold).setFontSize(14)
                    .setFontColor(PRIMARY_DARK)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4));
            doc.add(new Paragraph("Ngày in: " + SDF.format(new java.util.Date()))
                    .setFont(font).setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // ── Đường kẻ ──────────────────────────────────────────────────────
            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f)));
            doc.add(new Paragraph("\n"));

            // ── Bảng thông tin 2 cột ─────────────────────────────────────────
            Table info = new Table(UnitValue.createPercentArray(new float[]{3f, 7f, 3f, 7f}))
                    .useAllAvailableWidth().setMarginBottom(16);

            addInfoRow(info, fontBold, font, "Mã nhân viên:",  String.valueOf(nv.getMaNV()),
                                              "Họ và Tên:",    nv.getTenNV());
            addInfoRow(info, fontBold, font, "Giới tính:",     nv.getGioiTinh(),
                                              "Số điện thoại:", nv.getSoDienThoai());
            addInfoRow(info, fontBold, font, "Email:",         nv.getEmail(),
                                              "CCCD:",          nv.getCccd());
            addInfoRow(info, fontBold, font, "Địa chỉ:",       nv.getDiaChi(),
                                              "Vai trò:",       formatVaiTro(nv.getVaiTro()));
            addInfoRow(info, fontBold, font,
                    "Ngày sinh:",   nv.getNgaySinh()   != null ? SDF.format(nv.getNgaySinh())   : "—",
                    "Ngày vào làm:", nv.getNgayVaoLam() != null ? SDF.format(nv.getNgayVaoLam()) : "—");
            addInfoRow(info, fontBold, font, "Trạng thái:",    formatTrangThai(nv.getTrangThai()), null, null);

            doc.add(info);

            // ── Chữ ký ────────────────────────────────────────────────────────
            doc.add(new Paragraph("\n\n"));
            Table sign = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
                    .useAllAvailableWidth();
            sign.addCell(noBorder(new Paragraph("Nhân viên ký tên").setFont(fontBold).setFontSize(11)
                    .setFontColor(TEXT_DARK).setTextAlignment(TextAlignment.CENTER)));
            sign.addCell(noBorder(new Paragraph("Quản lý xác nhận").setFont(fontBold).setFontSize(11)
                    .setFontColor(TEXT_DARK).setTextAlignment(TextAlignment.CENTER)));
            sign.addCell(noBorder(new Paragraph("\n\n\n(Ký và ghi rõ họ tên)").setFont(font).setFontSize(10)
                    .setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER)));
            sign.addCell(noBorder(new Paragraph("\n\n\n(Ký và ghi rõ họ tên)").setFont(font).setFontSize(10)
                    .setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER)));
            doc.add(sign);

            doc.close();

            int open = JOptionPane.showConfirmDialog(parent,
                    "Xuất phiếu nhân sự thành công!\nFile: " + file.getAbsolutePath() + "\n\nMở file ngay?",
                    "Thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Lỗi khi xuất PDF: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static PdfFont loadFont() throws Exception {
        String[] paths = {
            "C:/Windows/Fonts/arial.ttf",
            "C:/Windows/Fonts/Arial.ttf"
        };
        for (String path : paths) {
            if (new java.io.File(path).exists()) {
                return PdfFontFactory.createFont(path,
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }
        }
        throw new Exception("Không tìm thấy font Arial. Kiểm tra C:/Windows/Fonts/arial.ttf");
    }

    private static PdfFont loadFontBold() throws Exception {
        String[] paths = {
            "C:/Windows/Fonts/arialbd.ttf",
            "C:/Windows/Fonts/Arialbd.ttf"
        };
        for (String path : paths) {
            if (new java.io.File(path).exists()) {
                return PdfFontFactory.createFont(path,
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }
        }
        return loadFont(); // fallback về regular
    }

    private static void addInfoRow(Table table, PdfFont labelFont, PdfFont valueFont,
                                    String l1, String v1, String l2, String v2) {
        SolidBorder border = new SolidBorder(new DeviceRgb(180, 210, 240), 0.5f);

        table.addCell(new Cell().add(new Paragraph(l1).setFont(labelFont).setFontSize(10.5f)
                .setFontColor(TEXT_DARK)).setPadding(7).setBorder(border)
                .setBackgroundColor(ROW_ALT));
        table.addCell(new Cell().add(new Paragraph(v1 != null && !v1.isEmpty() ? v1 : "—")
                .setFont(valueFont).setFontSize(10.5f)).setPadding(7).setBorder(border));

        if (l2 != null) {
            table.addCell(new Cell().add(new Paragraph(l2).setFont(labelFont).setFontSize(10.5f)
                    .setFontColor(TEXT_DARK)).setPadding(7).setBorder(border)
                    .setBackgroundColor(ROW_ALT));
            table.addCell(new Cell().add(new Paragraph(v2 != null && !v2.isEmpty() ? v2 : "—")
                    .setFont(valueFont).setFontSize(10.5f)).setPadding(7).setBorder(border));
        } else {
            // Merge 2 cột còn lại
            table.addCell(new Cell(1, 3).add(new Paragraph(""))
                    .setPadding(7).setBorder(border));
        }
    }

    private static Cell noBorder(Paragraph p) {
        return new Cell().add(p).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(4);
    }

    private static String formatVaiTro(String raw) {
        if (raw == null) return "—";
        if (raw.equalsIgnoreCase("NhanVienBanHang")) return "Nhân viên bán hàng";
        if (raw.equalsIgnoreCase("QuanLy"))          return "Quản lý";
        return raw;
    }

    private static String formatTrangThai(String raw) {
        if (raw == null) return "—";
        if (raw.equalsIgnoreCase("DangLam")) return "Đang làm việc";
        if (raw.equalsIgnoreCase("NghiViec")) return "Đã nghỉ việc";
        return raw;
    }
}