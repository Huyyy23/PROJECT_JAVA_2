package BUS;

import DAO.LoaiSanPhamDAO;
import DTO.LoaiSanPhamDTO;

import java.util.ArrayList;

/**
 * BUS cho LoaiSanPham.
 * GUI chỉ được gọi tầng này — không được import DAO trực tiếp.
 */
public class LoaiSanPhamBUS {

    private final LoaiSanPhamDAO loaiDAO = new LoaiSanPhamDAO();

    // Cache — tránh query DB liên tục
    private ArrayList<LoaiSanPhamDTO> dsLoai = new ArrayList<>();

    // ----------------------------------------------------------------
    // GET ALL — lấy tất cả loại sản phẩm
    // ----------------------------------------------------------------
    public ArrayList<LoaiSanPhamDTO> getAll() {
        dsLoai = loaiDAO.getAll();
        return dsLoai;
    }

    // ----------------------------------------------------------------
    // GET BY ID — tra tên loại theo mã (dùng cache)
    // ----------------------------------------------------------------
    public LoaiSanPhamDTO getById(int maLoai) {
        for (LoaiSanPhamDTO l : dsLoai) {
            if (l.getMaLoai() == maLoai) return l;
        }
        // Fallback nếu cache chưa load
        return loaiDAO.getById(maLoai);
    }

    // ----------------------------------------------------------------
    // GET TEN LOAI — trả về tên, dùng cho hiển thị nhanh
    // ----------------------------------------------------------------
    public String getTenLoai(int maLoai) {
        LoaiSanPhamDTO l = getById(maLoai);
        return l != null ? l.getTenLoai() : "Loại " + maLoai;
    }
}