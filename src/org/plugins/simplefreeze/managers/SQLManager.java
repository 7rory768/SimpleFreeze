package org.plugins.simplefreeze.managers;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.util.MySQL;

public class SQLManager extends MySQL {
	
	private final SimpleFreezeMain plugin;
	
	public SQLManager(SimpleFreezeMain plugin) {
		this.plugin = plugin;
	}
	
	private void setupMySQL() {
		this.setUsername(this.plugin.getConfig().getString("mysql.username"));
		this.setPassword(this.plugin.getConfig().getString("mysql.password"));
		this.setHost("jdbc:mysql://" + this.plugin.getConfig().getString("mysql.hostname") + ":" + this.plugin.getConfig().getString("mysql.port") + "/" + this.plugin.getConfig().getString("mysql.database"));
		try {
			this.openConnection();
			PreparedStatement ps = this.getConnection()
					.prepareStatement("CREATE TABLE IF NOT EXISTS simplefreeze (UUID VARCHAR(36), STARTDATE LONG, ENDDATE LONG, FREEZER VARCHAR(36), PRIMARY KEY (UUID));");
			ps.executeUpdate();
			ps.close();
			Bukkit.getServer().getConsoleSender().sendMessage("[SimpleFreeze] " + ChatColor.GREEN + "Successfully connected to MySQL");
			// ADD PLAYERS TO FROZEN LIST IF NEEDED
		} catch (SQLException e) {
			Bukkit.getServer().getConsoleSender().sendMessage("[SimpleFreeze] " + ChatColor.RED + "Couldn't connect to MySQL, check your mysql config values");
		}
	}
	
}
