package DTO;

public class NhaCungCapDTO {
    private int maNCC;
    private String tenNCC;
    private String sdt;
    private String email;
    private String diaChi;
    private int trangThai; // 1 = còn hợp tác, 2 = ngừng

    public NhaCungCapDTO(){
        trangThai = 1;// còn hợp tác
    }
    public NhaCungCapDTO(int maNCC, String tenNCC, String sdt, String email, String diaChi, int trangThai){
        this.maNCC = maNCC;
        this.tenNCC = tenNCC;
        this.sdt = sdt;
        this.email = email;
        this.diaChi = diaChi;
        this.trangThai = trangThai;
    }
    //Getters & Setter

    public int getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(int maNCC) {
        this.maNCC = maNCC;
    }

    public String getTenNCC() {
        return tenNCC;
    }

    public void setTenNCC(String tenNCC) {
        this.tenNCC = tenNCC;
    }

    public String getSDT() {
        return sdt;
    }

    public void setSDT(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public int getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(int trangThai) {
        this.trangThai = trangThai;
    }

    public String toString() {
        return tenNCC != null ? tenNCC : "";
    }
}
