package org.link_uuid.miningcontest.data.mysqlserver;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import org.link_uuid.miningcontest.data.config.json_init;
import org.link_uuid.miningcontest.data.config.json_init.*;
import org.link_uuid.miningcontest.data.variable.variable;
import com.mysql.cj.jdbc.Driver;
import static org.link_uuid.miningcontest.data.mysqlserver.TableManager.createTables;

public class DatabaseManager {
    private static HikariDataSource dataSource = new HikariDataSource();

    public static void initialize() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver not found. Please check your dependencies.", e);
        }
        // 資料庫連接配置
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setJdbcUrl("jdbc:mysql://"+json_init.config.MysqlHost.toString()+":"+String.valueOf(json_init.config.MysqlPort)+"/"+json_init.config.MysqlDB);
        dataSource.setUsername(json_init.config.MysqlUser);
        dataSource.setPassword(json_init.config.MysqlPassword);

        // 連接池配置
        dataSource.setMaximumPoolSize(10);

        // 初始化表格
        createTables();
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource not initialized");
        }
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}