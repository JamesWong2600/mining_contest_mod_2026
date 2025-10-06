package org.link_uuid.miningcontest.data.mysqlserver;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TableManager {

    public static void createTables() {
        String createPlayerTableSQL =
                "CREATE TABLE IF NOT EXISTS playerdata " +
                "(id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                " player VARCHAR(255), " +
                " UUID VARCHAR(255), " +
                " point INTEGER, " +
                " tp INTEGER, " +
                " pvppoint INTEGER, " +
                " server INTEGER, " +
                " pvpmode BOOLEAN)"
                ;

        String createAdminTableSQL =
                "CREATE TABLE IF NOT EXISTS admindata " +
                        "(id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                        " player VARCHAR(255), " +
                        " UUID VARCHAR(255), " +
                        " permission_level INTEGER) "
                ;


        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // 創建玩家表格
            stmt.execute(createPlayerTableSQL);
            System.out.println("Players table created/verified");

            stmt.execute(createAdminTableSQL);
            System.out.println("Players Admin created/verified");

            // 創建方塊表格

        } catch (SQLException e) {
            System.err.println("Failed to create tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

}