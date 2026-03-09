package UTIL;

import DTO.HoaDonDTO;
import DTO.ChiTietHoaDonDTO;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tiện ích xuất PDF cho màn hình Hóa Đơn.
 * Thư viện: iText 7.x (com.itextpdf:itext7-core)
 * Font:     Arial  C:/Windows/Fonts/arial.ttf + arialbd.ttf
 */
public class HoaDonPDFUtils {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DF  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat     CF  = new DecimalFormat("#,###");

    private static final DeviceRgb PRIMARY      = new DeviceRgb(21,  101, 192);
    private static final DeviceRgb PRIMARY_DARK = new DeviceRgb(10,  60,  130);
    private static final DeviceRgb ROW_ALT      = new DeviceRgb(245, 250, 255);
    private static final DeviceRgb WHITE        = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb TEXT_DARK    = new DeviceRgb(10,  60,  130);
    private static final DeviceRgb GREEN        = new DeviceRgb(27,  120, 60);
    private static final DeviceRgb RED_C        = new DeviceRgb(180, 30,  30);
    private static final DeviceRgb ORANGE       = new DeviceRgb(200, 120, 0);

    // =========================================================================
    // EXPORT DANH SACH HOA DON  (A4 nam ngang)
    // =========================================================================
    public static void exportDanhSach(Component parent, List<HoaDonDTO> danhSach) {
        if (danhSach == null || danhSach.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Khong co du lieu de xuat!", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Luu file PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File("DanhSachHoaDon.pdf"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".pdf")) file = new File(file.getAbsolutePath() + ".pdf");

        try {
            PdfFont font     = loadFont();
            PdfFont fontBold = loadFontBold();

            PdfDocument pdf = new PdfDocument(new PdfWriter(file));
            Document    doc = new Document(pdf, PageSize.A4.rotate());
            doc.setMargins(30, 30, 30, 30);

            // Tieu de
            doc.add(new Paragraph("DANH SACH HOA DON BAN HANG")
                    .setFont(fontBold).setFontSize(16).setFontColor(PRIMARY_DARK)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));

            String today = java.time.LocalDate.now().format(DF);
            doc.add(new Paragraph("Ngay xuat: " + today + "   |   Tong: " + danhSach.size() + " hoa don")
                    .setFont(font).setFontSize(10).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(12));

            // Bang du lieu
            float[] cw = {2f, 2f, 2f, 4f, 4.5f, 2f, 4f, 4f, 3f, 4.5f, 3f};
            Table table = new Table(UnitValue.createPercentArray(cw)).useAllAvailableWidth();

            String[] headers = {
                "Ma HD", "Ma KH", "Ma NV", "Ngay Lap",
                "Tien Hang (d)", "% Giam", "Tien Giam (d)",
                "Truoc VAT (d)", "VAT (d)", "Tong TT (d)", "Trang Thai"
            };
            for (String h : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setFont(fontBold).setFontSize(9f).setFontColor(WHITE))
                        .setBackgroundColor(PRIMARY).setTextAlignment(TextAlignment.CENTER)
                        .setPadding(5).setBorder(new SolidBorder(WHITE, 0.5f)));
            }

            for (int i = 0; i < danhSach.size(); i++) {
                HoaDonDTO  hd     = danhSach.get(i);
                DeviceRgb  rowBg  = (i % 2 == 0) ? WHITE : ROW_ALT;
                SolidBorder brd   = new SolidBorder(new DeviceRgb(180, 210, 240), 0.3f);
                DeviceRgb  ttClr  = colorTrangThai(hd.getTrangThai());

                addCell(table, font, String.valueOf(hd.getMaHoaDon()),                                          rowBg, brd, TextAlignment.CENTER);
                addCell(table, font, hd.getMaKhachHang() != null ? String.valueOf(hd.getMaKhachHang()) : "-",  rowBg, brd, TextAlignment.CENTER);
                addCell(table, font, String.valueOf(hd.getMaNV()),                                              rowBg, brd, TextAlignment.CENTER);
                addCell(table, font, hd.getNgayLap() != null ? hd.getNgayLap().format(DTF) : "-",              rowBg, brd, TextAlignment.LEFT);
                addCellRight(table, font, fmt(hd.getTongTienHang()),                                            rowBg, brd);
                addCell(table, font, hd.getPhanTramGiamHang() != null
                        ? hd.getPhanTramGiamHang().stripTrailingZeros().toPlainString() + "%" : "0%",          rowBg, brd, TextAlignment.CENTER);
                addCellRight(table, font, fmt(hd.getTienGiamHang()),                                            rowBg, brd);
                addCellRight(table, font, fmt(hd.getTienTruocVAT()),                                            rowBg, brd);
                addCellRight(table, font, fmt(hd.getTienVAT()),                                                 rowBg, brd);
                addCellRight(table, font, fmt(hd.getTongThanhToan()),                                           rowBg, brd);
                table.addCell(new Cell()
                        .add(new Paragraph(formatTrangThai(hd.getTrangThai()))
                                .setFont(fontBold).setFontSize(9f).setFontColor(ttClr))
                        .setBackgroundColor(rowBg).setTextAlignment(TextAlignment.CENTER)
                        .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(5).setPaddingRight(5)
                        .setBorder(brd));
            }

            doc.add(table);
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("(c) LaptopStore - Tai lieu noi bo")
                    .setFont(font).setFontSize(8).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.RIGHT));
            doc.close();

            int open = JOptionPane.showConfirmDialog(parent,
                    "Xuat PDF thanh cong!\nFile: " + file.getAbsolutePath() + "\n\nMo file ngay?",
                    "Thanh cong", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported())
                Desktop.getDesktop().open(file);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Loi khi xuat PDF: " + e.getMessage(),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // EXPORT HOA DON CHI TIET  (A4 dung - in bill)
    // =========================================================================
    public static void exportChiTiet(Component parent, HoaDonDTO hd, List<ChiTietHoaDonDTO> chiTiets) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Luu Hoa Don PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File("HoaDon_" + hd.getMaHoaDon() + ".pdf"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".pdf")) file = new File(file.getAbsolutePath() + ".pdf");

        try {
            PdfFont font     = loadFont();
            PdfFont fontBold = loadFontBold();

            PdfDocument pdf = new PdfDocument(new PdfWriter(file));
            Document    doc = new Document(pdf, PageSize.A4);
            doc.setMargins(50, 50, 50, 50);

            // Header phieu
            doc.add(new Paragraph("LAPTOP STORE")
                    .setFont(fontBold).setFontSize(20).setFontColor(PRIMARY)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("HOA DON BAN HANG")
                    .setFont(fontBold).setFontSize(15).setFontColor(PRIMARY_DARK)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
            doc.add(new Paragraph("So: #" + hd.getMaHoaDon()
                            + "   |   Ngay: " + (hd.getNgayLap() != null ? hd.getNgayLap().format(DTF) : "-"))
                    .setFont(font).setFontSize(10).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(16));

            doc.add(new LineSeparator(new SolidLine(1f)));
            doc.add(new Paragraph("\n"));

            // Thong tin dau hoa don
            Table info = new Table(UnitValue.createPercentArray(new float[]{3f, 7f, 3f, 7f}))
                    .useAllAvailableWidth().setMarginBottom(16);
            addInfoRow(info, fontBold, font,
                    "Ma hoa don:", String.valueOf(hd.getMaHoaDon()),
                    "Trang thai:", formatTrangThai(hd.getTrangThai()));
            addInfoRow(info, fontBold, font,
                    "Khach hang:", hd.getMaKhachHang() != null ? "KH #" + hd.getMaKhachHang() : "Vang lai",
                    "Nhan vien:", "NV #" + hd.getMaNV());
            addInfoRow(info, fontBold, font,
                    "Ghi chu:", hd.getGhiChu() != null && !hd.getGhiChu().isEmpty() ? hd.getGhiChu() : "-",
                    null, null);
            doc.add(info);

            // Bang san pham
            doc.add(new Paragraph("Chi Tiet San Pham")
                    .setFont(fontBold).setFontSize(12).setFontColor(PRIMARY_DARK).setMarginBottom(6));

            float[] cw = {1f, 5f, 1.5f, 2.5f, 1.2f, 3f, 3f};
            Table tbl = new Table(UnitValue.createPercentArray(cw)).useAllAvailableWidth();
            String[] hdrs = {"STT", "Ten San Pham", "Ma SP", "Ma Serial", "SL", "Don Gia (d)", "Thanh Tien (d)"};
            for (String h : hdrs) {
                tbl.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setFont(fontBold).setFontSize(9.5f).setFontColor(WHITE))
                        .setBackgroundColor(PRIMARY).setTextAlignment(TextAlignment.CENTER)
                        .setPadding(6).setBorder(new SolidBorder(WHITE, 0.5f)));
            }

            if (chiTiets != null) {
                for (int i = 0; i < chiTiets.size(); i++) {
                    ChiTietHoaDonDTO ct = chiTiets.get(i);
                    DeviceRgb bg = (i % 2 == 0) ? WHITE : ROW_ALT;
                    SolidBorder brd = new SolidBorder(new DeviceRgb(180, 210, 240), 0.3f);
                    addCell(tbl, font, String.valueOf(i + 1),                          bg, brd, TextAlignment.CENTER);
                    addCell(tbl, font, ct.getTenSP() != null ? ct.getTenSP() : "-",   bg, brd, TextAlignment.LEFT);
                    addCell(tbl, font, String.valueOf(ct.getMaSP()),                   bg, brd, TextAlignment.CENTER);
                    addCell(tbl, font, String.valueOf(ct.getMaSerial()),               bg, brd, TextAlignment.CENTER);
                    addCell(tbl, font, String.valueOf(ct.getSoLuong()),                bg, brd, TextAlignment.CENTER);
                    addCellRight(tbl, font, fmt(ct.getDonGia()),                       bg, brd);
                    addCellRight(tbl, font, fmt(ct.getThanhTien()),                    bg, brd);
                }
            }
            doc.add(tbl);
            doc.add(new Paragraph("\n"));

            // Bang tong ket
            Table sum = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
                    .setWidth(UnitValue.createPercentValue(55))
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setMarginBottom(20);
            SolidBorder sb = new SolidBorder(new DeviceRgb(180, 210, 240), 0.5f);
            String pct = hd.getPhanTramGiamHang() != null
                    ? hd.getPhanTramGiamHang().stripTrailingZeros().toPlainString() : "0";
            addSumRow(sum, fontBold, font, "Tong tien hang:",      fmt(hd.getTongTienHang())  + " d", sb);
            addSumRow(sum, fontBold, font, "Giam gia (" + pct + "%):", fmt(hd.getTienGiamHang()) + " d", sb);
            addSumRow(sum, fontBold, font, "Truoc VAT:",            fmt(hd.getTienTruocVAT())  + " d", sb);
            addSumRow(sum, fontBold, font, "VAT (10%):",            fmt(hd.getTienVAT())       + " d", sb);
            // Dong tong thanh toan noi bat
            sum.addCell(new Cell().add(new Paragraph("TONG THANH TOAN:")
                    .setFont(fontBold).setFontSize(11).setFontColor(WHITE))
                    .setBackgroundColor(PRIMARY).setPadding(8).setBorder(sb));
            sum.addCell(new Cell().add(new Paragraph(fmt(hd.getTongThanhToan()) + " VND")
                    .setFont(fontBold).setFontSize(11).setFontColor(WHITE))
                    .setBackgroundColor(PRIMARY).setPadding(8).setBorder(sb)
                    .setTextAlignment(TextAlignment.RIGHT));
            doc.add(sum);

            // Chu ky
            doc.add(new LineSeparator(new SolidLine(0.5f)));
            doc.add(new Paragraph("\n"));
            Table sign = new Table(UnitValue.createPercentArray(new float[]{1f, 1f})).useAllAvailableWidth();
            sign.addCell(noBorder(new Paragraph("Khach hang xac nhan")
                    .setFont(fontBold).setFontSize(11).setFontColor(TEXT_DARK).setTextAlignment(TextAlignment.CENTER)));
            sign.addCell(noBorder(new Paragraph("Nhan vien ban hang")
                    .setFont(fontBold).setFontSize(11).setFontColor(TEXT_DARK).setTextAlignment(TextAlignment.CENTER)));
            sign.addCell(noBorder(new Paragraph("\n\n\n(Ky va ghi ro ho ten)")
                    .setFont(font).setFontSize(10).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER)));
            sign.addCell(noBorder(new Paragraph("\n\n\n(Ky va ghi ro ho ten)")
                    .setFont(font).setFontSize(10).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER)));
            doc.add(sign);

            doc.add(new Paragraph("\n(c) LaptopStore - Cam on quy khach!")
                    .setFont(font).setFontSize(8).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.close();

            int open = JOptionPane.showConfirmDialog(parent,
                    "Xuat hoa don PDF thanh cong!\nFile: " + file.getAbsolutePath() + "\n\nMo file ngay?",
                    "Thanh cong", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported())
                Desktop.getDesktop().open(file);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Loi khi xuat PDF: " + e.getMessage(),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // HELPERS - CELL
    // =========================================================================
    private static void addCell(Table tbl, PdfFont font, String text,
                                 DeviceRgb bg, SolidBorder brd, TextAlignment align) {
        tbl.addCell(new Cell()
                .add(new Paragraph(text != null ? text : "-").setFont(font).setFontSize(9.5f).setFontColor(TEXT_DARK))
                .setBackgroundColor(bg).setTextAlignment(align)
                .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(5).setPaddingRight(5)
                .setBorder(brd));
    }

    private static void addCellRight(Table tbl, PdfFont font, String text,
                                      DeviceRgb bg, SolidBorder brd) {
        addCell(tbl, font, text, bg, brd, TextAlignment.RIGHT);
    }

    private static void addInfoRow(Table tbl, PdfFont lf, PdfFont vf,
                                    String l1, String v1, String l2, String v2) {
        SolidBorder brd = new SolidBorder(new DeviceRgb(180, 210, 240), 0.5f);
        tbl.addCell(new Cell().add(new Paragraph(l1).setFont(lf).setFontSize(10.5f)
                .setFontColor(TEXT_DARK)).setPadding(7).setBorder(brd).setBackgroundColor(ROW_ALT));
        tbl.addCell(new Cell().add(new Paragraph(v1 != null ? v1 : "-").setFont(vf).setFontSize(10.5f))
                .setPadding(7).setBorder(brd));
        if (l2 != null) {
            tbl.addCell(new Cell().add(new Paragraph(l2).setFont(lf).setFontSize(10.5f)
                    .setFontColor(TEXT_DARK)).setPadding(7).setBorder(brd).setBackgroundColor(ROW_ALT));
            tbl.addCell(new Cell().add(new Paragraph(v2 != null ? v2 : "-").setFont(vf).setFontSize(10.5f))
                    .setPadding(7).setBorder(brd));
        } else {
            tbl.addCell(new Cell(1, 3).add(new Paragraph("")).setPadding(7).setBorder(brd));
        }
    }

    private static void addSumRow(Table tbl, PdfFont lf, PdfFont vf,
                                   String label, String value, SolidBorder brd) {
        tbl.addCell(new Cell().add(new Paragraph(label).setFont(lf).setFontSize(10f)
                .setFontColor(TEXT_DARK)).setPadding(6).setBorder(brd).setBackgroundColor(ROW_ALT));
        tbl.addCell(new Cell().add(new Paragraph(value).setFont(vf).setFontSize(10f)
                .setFontColor(TEXT_DARK)).setPadding(6).setBorder(brd).setTextAlignment(TextAlignment.RIGHT));
    }

    private static Cell noBorder(Paragraph p) {
        return new Cell().add(p).setBorder(Border.NO_BORDER).setPadding(4);
    }

    // =========================================================================
    // HELPERS - FONT & FORMAT
    // =========================================================================
    private static PdfFont loadFont() throws Exception {
        for (String p : new String[]{"C:/Windows/Fonts/arial.ttf", "C:/Windows/Fonts/Arial.ttf"}) {
            if (new java.io.File(p).exists())
                return PdfFontFactory.createFont(p, PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        }
        throw new Exception("Khong tim thay font Arial. Kiem tra C:/Windows/Fonts/arial.ttf");
    }

    private static PdfFont loadFontBold() throws Exception {
        for (String p : new String[]{"C:/Windows/Fonts/arialbd.ttf", "C:/Windows/Fonts/Arialbd.ttf"}) {
            if (new java.io.File(p).exists())
                return PdfFontFactory.createFont(p, PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        }
        return loadFont();
    }

    private static String fmt(BigDecimal val) {
        return val != null ? CF.format(val) : "0";
    }

    public static String formatTrangThai(String raw) {
        if (raw == null) return "-";
        switch (raw) {
            case "HoanThanh": return "Hoan thanh";
            case "Huy":       return "Da huy";
            case "ChoXuLy":   return "Cho xu ly";
            default:          return raw;
        }
    }

    private static DeviceRgb colorTrangThai(String raw) {
        if ("HoanThanh".equals(raw)) return GREEN;
        if ("Huy".equals(raw))       return RED_C;
        if ("ChoXuLy".equals(raw))   return ORANGE;
        return TEXT_DARK;
    }
}