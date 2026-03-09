package DAO;

import DTO.SERIALDTO;
import UTIL.DBConnection;

import java.sql.*;
import java.util.ArrayList;

public class SERIALDAO {

    public ArrayList<SERIALDTO> getAll(){
        ArrayList<SERIALDTO> list = new ArrayList<>();

        try{
            Connection con = DBConnection.getConnection();
            String sql = "SELECT * FROM SERIAL";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                SERIALDTO s = new SERIALDTO();

                s.setMaSerial(rs.getInt("MaSerial"));
                s.setSerialCode(rs.getString("SerialCode"));
                s.setMaSP(rs.getInt("MaSP"));
                s.setMaChiTietPN((Integer)rs.getObject("MaChiTietPN"));
                s.setTrangThai(rs.getString("TrangThai"));

                Date ngayNhap = rs.getDate("NgayNhap");
                if(ngayNhap != null)
                    s.setNgayNhap(ngayNhap.toLocalDate());

                Date ngayXuat = rs.getDate("NgayXuat");
                if(ngayXuat != null)
                    s.setNgayXuat(ngayXuat.toLocalDate());

                list.add(s);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public boolean insert(SERIALDTO s){
        try{
            Connection con = DBConnection.getConnection();

            String sql = "INSERT INTO SERIAL(SerialCode,MaSP,MaChiTietPN,TrangThai,NgayNhap) VALUES(?,?,?,?,?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, s.getSerialCode());
            ps.setInt(2, s.getMaSP());

            if(s.getMaChiTietPN()==null)
                ps.setNull(3, Types.INTEGER);
            else
                ps.setInt(3, s.getMaChiTietPN());

            ps.setString(4, s.getTrangThai());

            if(s.getNgayNhap()!=null)
                ps.setDate(5, Date.valueOf(s.getNgayNhap()));
            else
                ps.setNull(5, Types.DATE);

            return ps.executeUpdate()>0;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateTrangThai(int maSerial,String trangThai){
        try{
            Connection con = DBConnection.getConnection();

            String sql="UPDATE SERIAL SET TrangThai=? WHERE MaSerial=?";
            PreparedStatement ps=con.prepareStatement(sql);

            ps.setString(1,trangThai);
            ps.setInt(2,maSerial);

            return ps.executeUpdate()>0;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    

}