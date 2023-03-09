package org.plugins.simplefreeze.util;

import com.zaxxer.hikari.HikariDataSource;
import org.plugins.simplefreeze.SimpleFreezeMain;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQL {

    private final SimpleFreezeMain plugin;

    private HikariDataSource hikari;

    public MySQL(SimpleFreezeMain plugin) {
        this.plugin = plugin;
        this.connectToDatabase();
    }

    public void connectToDatabase() {
        String address = this.plugin.getConfig().getString("mysql.hostname");
        String name = this.plugin.getConfig().getString("mysql.database-name");
        String username = this.plugin.getConfig().getString("mysql.username");
        String password = this.plugin.getConfig().getString("mysql.password");

        this.hikari = new HikariDataSource();
        this.hikari.setMaximumPoolSize(10);
        this.hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        this.hikari.addDataSourceProperty("serverName", address);
        this.hikari.addDataSourceProperty("port", "3306");
        this.hikari.addDataSourceProperty("databaseName", name);
        this.hikari.addDataSourceProperty("user", username);
        this.hikari.addDataSourceProperty("password", password);
    }

    public Connection getConnection() {
        Connection connection = null;

        try {
            connection = this.hikari.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return connection;
    }

    public void closeHikari() {
        this.hikari.close();
    }
}