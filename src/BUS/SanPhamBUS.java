package BUS;

import DAO.SanPhamDAO;
import DTO.SanPhamDTO;
import java.math.BigDecimal;
import java.util.ArrayList;

public class SanPhamBUS {

    private final SanPhamDAO spDAO = new SanPhamDAO();
    private ArrayList<SanPhamDTO> dsSanPham = new ArrayList<>();

    // =========================================================================
    // 1. LẤY DỮ LIỆU
    // =========================================================================

    public ArrayList<SanPhamDTO> getDanhSachSanPham() {
        dsSanPham = spDAO.getDanhSachSanPham();
        return dsSanPham;
    }

    public SanPhamDTO timTheoMa(int maSP) {
        // Ưu tiên tìm trong cache trước
        for (SanPhamDTO sp : dsSanPham) {
            if (sp.getMaSP() == maSP) return sp;
        }
        // Cache miss -> Tìm trong DB
        return spDAO.getById(maSP);
    }

    public ArrayList<SanPhamDTO> getSanPhamByNhaCungCap(int maNCC) {
        if (maNCC <= 0) return new ArrayList<>();
        return spDAO.getSanPhamByNhaCungCap(maNCC);
    }

    public void kiemTraSPThuocNCC(int maSP, int maNCC, String tenSP, String tenNCC) {
        if (maSP <= 0 || maNCC <= 0) {
            throw new IllegalArgumentException("Mã SP hoặc mã NCC không hợp lệ!");
        }

        boolean thuocNCC = spDAO.kiemTraSPThuocNCC(maSP, maNCC);
        if (!thuocNCC) {
            throw new IllegalArgumentException(
                "Sản phẩm \"" + tenSP + "\" không thuộc nhà cung cấp \"" + tenNCC + "\"!\n"
                + "Vui lòng chọn đúng nhà cung cấp hoặc chọn lại sản phẩm."
            );
        }
    }

    // =========================================================================
    // 2. VALIDATE NGHIỆP VỤ
    // =========================================================================

    private boolean kiemTraDuLieu(SanPhamDTO sp) {
        if (sp.getTenSP() == null || sp.getTenSP().trim().isEmpty()) return false;
        if (sp.getGia() == null || sp.getGia().compareTo(BigDecimal.ZERO) <= 0) return false;
        if (sp.getGiaGoc() == null || sp.getGiaGoc().compareTo(BigDecimal.ZERO) <= 0) return false;
        if (sp.getSoLuongTon() < 0) return false;
        if (sp.getSoLuongToiThieu() < 0) return false;
        if (sp.getSoLuongToiDa() < sp.getSoLuongToiThieu()) return false;
        return true;
    }

    // =========================================================================
    // 3. THÊM / SỬA / XÓA
    // =========================================================================

    public boolean themSanPham(SanPhamDTO sp) {
        if (!kiemTraDuLieu(sp)) return false;
        boolean kq = spDAO.themSanPham(sp);
        if (kq) dsSanPham.add(sp);
        return kq;
    }

    public boolean suaSanPham(SanPhamDTO sp) {
        if (!kiemTraDuLieu(sp)) return false;
        boolean kq = spDAO.suaSanPham(sp);
        if (kq) {
            for (int i = 0; i < dsSanPham.size(); i++) {
                if (dsSanPham.get(i).getMaSP() == sp.getMaSP()) {
                    dsSanPham.set(i, sp);
                    break;
                }
            }
        }
        return kq;
    }

    public boolean xoaSanPham(int maSP) {
        boolean kq = spDAO.xoaSanPham(maSP);
        if (kq) {
            for (int i = 0; i < dsSanPham.size(); i++) {
                if (dsSanPham.get(i).getMaSP() == maSP) {
                    dsSanPham.remove(i);
                    break;
                }
            }
        }
        return kq;
    }

    // =========================================================================
    // 4. TÌM KIẾM / LỌC TRONG CACHE
    // =========================================================================

    public ArrayList<SanPhamDTO> timSanPham(String ten) {
        return spDAO.timSanPham(ten);
    }

    public ArrayList<SanPhamDTO> locTheoLoai(int maLoai) {
        ArrayList<SanPhamDTO> ketQua = new ArrayList<>();
        for (SanPhamDTO sp : dsSanPham) {
            if (sp.getMaLoai() == maLoai) ketQua.add(sp);
        }
        return ketQua;
    }

    public ArrayList<SanPhamDTO> locTheoThuongHieu(String thuongHieu) {
        ArrayList<SanPhamDTO> ketQua = new ArrayList<>();
        for (SanPhamDTO sp : dsSanPham) {
            if (sp.getThuongHieu().equalsIgnoreCase(thuongHieu)) ketQua.add(sp);
        }
        return ketQua;
    }

    public ArrayList<SanPhamDTO> locTheoGia(BigDecimal min, BigDecimal max) {
        ArrayList<SanPhamDTO> ketQua = new ArrayList<>();
        for (SanPhamDTO sp : dsSanPham) {
            if (sp.getGia().compareTo(min) >= 0 && sp.getGia().compareTo(max) <= 0) {
                ketQua.add(sp);
            }
        }
        return ketQua;
    }

    public int getMaSPMoiNhat() {
        return spDAO.getMaSPMoiNhat();
    }
}
