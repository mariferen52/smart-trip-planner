package com.isep.smarttripplanner.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager db;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:smart_trip_planner.db";

    private DatabaseManager(){}

    public static DatabaseManager getDb(){
        if(db == null){
            db = new DatabaseManager();
            return db;
        }else
            return db;
    }

    public Connection getConnection() throws SQLException {
        if(connection == null || connection.isClosed())
            connection = DriverManager.getConnection(DB_URL);
        return connection;
    }

}
