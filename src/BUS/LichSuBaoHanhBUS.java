package BUS;

import DAO.LichSuBaoHanhDAO;
import DTO.LichSuBaoHanhDTO;
import java.util.List;

public class LichSuBaoHanhBUS {

    private final LichSuBaoHanhDAO dao = new LichSuBaoHanhDAO();

    /**
     * Ghi 1 dòng lịch sử — dùng sau mỗi lần tạo/cập nhật phiếu BH
     */
    public boolean ghiLichSu(int maBaoHanh, Integer maNV,
            String trangThaiCu, String trangThaiMoi,
            String ghiChu) {
        if (maBaoHanh <= 0)
            throw new IllegalArgumentException("Mã bảo hành không hợp lệ");
        if (trangThaiMoi == null || trangThaiMoi.isBlank())
            throw new IllegalArgumentException("Trạng thái mới không được rỗng");

        LichSuBaoHanhDTO ls = new LichSuBaoHanhDTO(
                maBaoHanh, maNV, trangThaiCu, trangThaiMoi,
                ghiChu != null ? ghiChu.trim() : null);
        return dao.ghiLichSu(ls);
    }

    /**
     * Lấy toàn bộ lịch sử của 1 phiếu BH (mới nhất trên đầu)
     */
    public List<LichSuBaoHanhDTO> layLichSu(int maBaoHanh) {
        if (maBaoHanh <= 0)
            throw new IllegalArgumentException("Mã bảo hành không hợp lệ");
        return dao.layLichSu(maBaoHanh);
    }
}
