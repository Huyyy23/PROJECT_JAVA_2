package UTIL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
        "jdbc:sqlserver://localhost:1433;"
        + "databaseName=LAPTOPSTORE1;"
        + "encrypt=true;trustServerCertificate=true";

    private static final String USER = "sa";   
    private static final String PASSWORD = "13376655";  

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
