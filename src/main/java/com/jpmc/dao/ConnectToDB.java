package com.jpmc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectToDB {
    public static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
    public static final String URL_TO_CONNECT = "jdbc:mysql://localhost:3306/jpmc";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "#RoopRaj89";

    public Connection connectToDB() {
        Connection connection=null;
        try {
            //1. Load the Driver
            Class.forName(DRIVER_NAME);

            //2. Connect to the Database
            connection = DriverManager.getConnection(URL_TO_CONNECT, DB_USERNAME, DB_PASSWORD);

            //3. Prepared Statement - inside these "methods"

            //4. Close connection
//            connection.close(); //auto-close if used with try-catch.

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;

    }
}
