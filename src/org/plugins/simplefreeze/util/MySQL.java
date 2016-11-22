package org.plugins.simplefreeze.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {

	private Connection connection;
	private String host = "";
	private String username = "";
	private String password = "";

	public synchronized void openConnection() {
		try {
			connection = DriverManager.getConnection(host, username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void closeConnection() {
		try {
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disableConnection() {
		try {
			if (!(connection == null || connection.isClosed())) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public void setHost(String string) {
		this.host = string;
	}
	
	public void setUsername(String string) {
		this.username = string;
	}
	
	public void setPassword(String string) {
		this.password = string;
	}
}