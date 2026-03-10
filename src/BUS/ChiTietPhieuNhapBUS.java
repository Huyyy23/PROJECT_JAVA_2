package BUS;

import DAO.ChiTietPhieuNhapDAO;
import DAO.PhieuNhapDAO;
import DTO.ChiTietPhieuNhapDTO;
import DTO.PhieuNhapDTO;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * BUS cho ChiTietPhieuNhap
 *
 * CHANGELOG:
 *   2026-03-09 - [XÓA] them(): bỏ method này vì insert chi tiết giờ
 *                        được thực hiện bên trong PhieuNhapBUS.luuPhieu()
 *                        trong cùng 1 transaction. Không gọi riêng lẻ nữa.
 *   2026-03-09 - [SỬA] xoaTheoMaPN(): giữ nguyên guard kiểm tra ChoXuLy.
 */
public class ChiTietPhieuNhapBUS {

    private final ChiTietPhieuNhapDAO dao         = new ChiTietPhieuNhapDAO();
    private final PhieuNhapDAO        phieuNhapDAO = new PhieuNhapDAO();

    // ----------------------------------------------------------------
    // GET BY MAPHIEU — dùng để hiển thị chi tiết phiếu trên UI
    // ----------------------------------------------------------------
    public ArrayList<ChiTietPhieuNhapDTO> getByMaPN(int maPN) {
        if (maPN <= 0) throw new IllegalArgumentException("Mã phiếu nhập không hợp lệ!");
        return dao.getByMaPN(maPN);
    }

    // ----------------------------------------------------------------
    // DELETE BY MAPHIEU — chỉ cho phép khi phiếu đang ChoXuLy
    // ----------------------------------------------------------------
    public boolean xoaTheoMaPN(int maPN) {
        if (maPN <= 0) throw new IllegalArgumentException("Mã phiếu nhập không hợp lệ!");
        try {
            PhieuNhapDTO pn = phieuNhapDAO.getById(maPN);
            if (pn == null)
                throw new IllegalArgumentException("Không tìm thấy phiếu nhập #" + maPN);
            if (!"ChoXuLy".equals(pn.getTrangThai()))
                throw new IllegalStateException(
                    "Chỉ được xóa chi tiết của phiếu đang 'Chờ xử lý'!\n" +
                    "Phiếu #" + maPN + " hiện đang ở trạng thái: " + pn.getTrangThai());
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra trạng thái phiếu: " + e.getMessage(), e);
        }
        return dao.deleteByMaPN(maPN);
    }
}
