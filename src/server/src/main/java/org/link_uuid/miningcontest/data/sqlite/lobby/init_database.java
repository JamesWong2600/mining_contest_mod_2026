package org.link_uuid.miningcontest.data.sqlite.lobby;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.link_uuid.miningcontest.MiningContestCommon.MOD_ID;

public class init_database {
    public static Connection getReadOnlyConnection() {
        try {
            Path dbPath = FabricLoader.getInstance().getConfigDir()
                    .resolve(MOD_ID)
                    .resolve("database.db");

            if (!dbPath.toFile().exists()) {
                System.err.println("Database file not found: " + dbPath);
                return null;
            }

            String url = "jdbc:sqlite:" + dbPath.toString();
            Connection connection = DriverManager.getConnection(url);

            System.out.println("Connected to SQLite database (read-only): " + dbPath);
            return connection;

        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            return null;
        }
    }
}