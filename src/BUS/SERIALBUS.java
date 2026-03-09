package BUS;

import DAO.SERIALDAO;
import DTO.SERIALDTO;
import java.util.ArrayList;

public class SERIALBUS {

    private SERIALDAO serialDAO = new SERIALDAO();
    private ArrayList<SERIALDTO> listSerial;

    public ArrayList<SERIALDTO> getAll(){
        listSerial = serialDAO.getAll();
        return listSerial;
    }

    public boolean themSerial(SERIALDTO s){

        if(s.getSerialCode()==null || s.getSerialCode().trim().isEmpty()){
            System.out.println("Serial khong duoc rong");
            return false;
        }

        return serialDAO.insert(s);
    }

    public boolean capNhatTrangThai(int maSerial,String trangThai){
        return serialDAO.updateTrangThai(maSerial,trangThai);
    }


}