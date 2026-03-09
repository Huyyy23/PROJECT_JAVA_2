package BUS;

import DAO.ChiTietPhieuNhapDAO;
import DTO.ChiTietPhieuNhapDTO;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * BUS cho ChiTietPhieuNhap — validation + gọi DAO
 */
public class ChiTietPhieuNhapBUS {

    private final ChiTietPhieuNhapDAO dao = new ChiTietPhieuNhapDAO();

    // ----------------------------------------------------------------
    // THEM — Thêm một dòng chi tiết, trả về maChiTietPN mới
    // ----------------------------------------------------------------
    public int them(ChiTietPhieuNhapDTO dto) {
        validate(dto);
        return dao.insert(dto);
    }

    // ----------------------------------------------------------------
    // GET BY MAPHIEU
    // ----------------------------------------------------------------
    public ArrayList<ChiTietPhieuNhapDTO> getByMaPN(int maPN) {
        if (maPN <= 0) throw new IllegalArgumentException("Mã phiếu nhập không hợp lệ!");
        return dao.getByMaPN(maPN);
    }

    // ----------------------------------------------------------------
    // DELETE BY MAPHIEU
    // ----------------------------------------------------------------
    public boolean xoaTheoMaPN(int maPN) {
        if (maPN <= 0) throw new IllegalArgumentException("Mã phiếu nhập không hợp lệ!");
        return dao.deleteByMaPN(maPN);
    }

    // ----------------------------------------------------------------
    // VALIDATION
    // ----------------------------------------------------------------
    private void validate(ChiTietPhieuNhapDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("Dữ liệu chi tiết không được null!");
        if (dto.getMaPN() <= 0)
            throw new IllegalArgumentException("Mã phiếu nhập không hợp lệ!");
        if (dto.getMaSP() <= 0)
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ!");
        if (dto.getSoLuong() <= 0)
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0!");
        if (dto.getDonGiaNhap() == null
                || dto.getDonGiaNhap().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Đơn giá nhập phải lớn hơn 0!");
    }
}