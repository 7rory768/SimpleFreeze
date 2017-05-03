package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.util.MySQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLManager {

    private final SimpleFreezeMain plugin;
    private final MySQL mySQL;
    private final FreezeManager freezeManager;
    private final PlayerManager playerManager;

    private BukkitTask freezeTask;
    private BukkitTask unfreezeTask;

    public SQLManager(SimpleFreezeMain plugin, MySQL mySQL, FreezeManager freezeManager, PlayerManager playerManager) {
        this.plugin = plugin;
        this.mySQL = mySQL;
        this.freezeManager = freezeManager;
        this.playerManager = playerManager;
    }

    public void setupTables() {
        Connection connection = null;
        PreparedStatement ps = null;

        String update = "CREATE TABLE IF NOT EXISTS sf_" + this.plugin.getServerID().toLowerCase() + "_freezes (freezee_name VARCHAR(16) NOT NULL UNIQUE, freezee_uuid VARCHAR(36) NOT NULL UNIQUE, freezer_name VARCHAR(16) NOT NULL, freezer_uuid VARCHAR(36), unfreeze_date LONG, reason VARCHAR(100), servers VARCHAR(100), source_server VARCHAR(50));";

        try {

            connection = this.mySQL.getConnection();

            ps = connection.prepareStatement(update);
            ps.execute();
            ps.close();

            update = "CREATE TABLE IF NOT EXISTS sf_" + this.plugin.getServerID().toLowerCase() + "_unfreezes (unfreezee_name VARCHAR(16) NOT NULL UNIQUE, unfreezee_uuid VARCHAR(36) NOT NULL UNIQUE, unfreezer_name VARCHAR(16), source_server VARCHAR(50));";

            ps = connection.prepareStatement(update);
            ps.execute();

            update = "CREATE TABLE IF NOT EXISTS sf_" + this.plugin.getServerID().toLowerCase() + "_frozenlist (player_uuid VARCHAR(36) NOT NULL UNIQUE);";

            ps = connection.prepareStatement(update);
            ps.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void setupTasks() {
        this.freezeTask = new BukkitRunnable() {
            @Override
            public void run() {
                Connection connection = null;
                PreparedStatement ps = null;
                ResultSet res = null;

                String request = "SELECT * FROM sf_" + plugin.getServerID().toLowerCase() + "_freezes;";

                try {
                    connection = mySQL.getConnection();

                    ps = connection.prepareStatement(request);
                    if (ps != null) {
                        res = ps.executeQuery();
                        List<String> names = new ArrayList<>();
                        if (res != null) {
                            while (res.next()) {
                                String freezeeName = res.getString("freezee_name");
                                String reason = res.getString("reason");
                                String serversString = res.getString("servers");
                                String sourceServer = res.getString("source_server");
                                Long unfreezeDate = res.getLong("unfreeze_date");

                                UUID freezeeUUID = null;
                                UUID freezerUUID = null;
                                if (res.getString("freezer_uuid") != null) {
                                    freezerUUID = UUID.fromString(res.getString("freezer_uuid"));
                                }
                                if (res.getString("freezee_uuid") != null) {
                                    freezeeUUID = UUID.fromString(res.getString("freezee_uuid"));
                                } else {
                                    Player onlineP = Bukkit.getPlayer(freezeeName);
                                    OfflinePlayer offlineP = Bukkit.getOfflinePlayer(freezeeName);
                                    if (onlineP != null) {
                                        freezeeUUID = onlineP.getUniqueId();
                                    } else if (offlineP != null) {
                                        if (offlineP.hasPlayedBefore()) {
                                            freezeeUUID = offlineP.getUniqueId();
                                        }
                                    }
                                }

                                names.add(freezeeName);
                                if (!playerManager.isFrozen(freezeeUUID)) {
                                    if (unfreezeDate != null && unfreezeDate > 0) {
                                        freezeManager.tempFreeze(freezeeUUID, freezerUUID, null, (unfreezeDate - System.currentTimeMillis() + 1000L) / 1000L, reason, serversString);
                                    } else {
                                        freezeManager.freeze(freezeeUUID, freezerUUID, null, reason, serversString);
                                    }
                                    freezeManager.notifyOfSQLFreeze(freezeeName, freezeeUUID, serversString, sourceServer, reason);
                                }
                            }
                            try {
                                ps.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            ps.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        for (String name : names) {
                            String update = "DELETE FROM sf_" + plugin.getServerID().toLowerCase() + "_freezes WHERE freezee_name = '" + name + "';";
                            ps = connection.prepareStatement(update);
                            ps.execute();
                            ps.close();
                        }
                    }

                    request = "SELECT * FROM sf_" + plugin.getServerID().toLowerCase() + "_unfreezes;";
                    ps = connection.prepareStatement(request);
                    if (ps != null) {
                        res = ps.executeQuery();
                        List<String> names = new ArrayList<>();
                        List<UUID> uuids = new ArrayList<>();
                        if (res != null) {
                            while (res.next()) {
                                String unfreezeeName = res.getString("unfreezee_name");
                                String unfreezerName = res.getString("unfreezer_name");
                                String sourceServer = res.getString("source_server");

                                UUID unfreezeeUUID = null;
                                if (res.getString("unfreezee_uuid") != null) {
                                    unfreezeeUUID = UUID.fromString(res.getString("unfreezee_uuid"));
                                } else {
                                    Player onlineP = Bukkit.getPlayer(unfreezeeName);
                                    OfflinePlayer offlineP = Bukkit.getOfflinePlayer(unfreezeeName);
                                    if (onlineP != null) {
                                        unfreezeeUUID = onlineP.getUniqueId();
                                    } else if (offlineP != null) {
                                        if (offlineP.hasPlayedBefore()) {
                                            unfreezeeUUID = offlineP.getUniqueId();
                                        }
                                    }
                                }

                                names.add(unfreezeeName);
                                if (unfreezeeUUID != null) {
                                    uuids.add(unfreezeeUUID);
                                }
                                if (playerManager.isFrozen(unfreezeeUUID)) {
                                    freezeManager.unfreeze(unfreezeeUUID);
                                }
                                freezeManager.notifyOfUnfreeze(unfreezeeUUID, unfreezeeName, unfreezerName, sourceServer);
                            }
                            res.close();
                        }
                        try {
                            ps.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        for (String name : names) {
                            String update = "DELETE FROM sf_" + plugin.getServerID().toLowerCase() + "_unfreezes WHERE unfreezee_name = '" + name + "';";
                            ps = connection.prepareStatement(update);
                            ps.execute();
                            ps.close();
                        }

                        for (UUID uuid : uuids) {
                            String update = "DELETE FROM sf_" + plugin.getServerID().toLowerCase() + "_frozenlist WHERE player_uuid = '" + uuid + "';";
                            ps = connection.prepareStatement(update);
                            ps.execute();
                            ps.close();
                        }
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin, 20L, 20L);
    }

    public List<String> getServerIDs() {
        List<String> serverIDs = new ArrayList<>();
        if (!this.plugin.usingMySQL()) {
            return serverIDs;
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        String request = "SHOW TABLES;";

        try {

            connection = this.mySQL.getConnection();

            ps = connection.prepareStatement(request);
            if (ps != null) {
                res = ps.executeQuery();
                if (res != null) {
                    while (res.next()) {
                        String tableName = res.getString(1);
                        String serverID = null;
                        if (tableName.startsWith("sf_") && tableName.endsWith("_freezes")) {
                            serverID = tableName.substring(3, tableName.length() - 8);
                        }
                        if (serverID != null) {
                            serverIDs.add(serverID);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

            if (res != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return serverIDs;
    }

    public void addFreeze(String freezeeName, UUID freezeeUUID, String freezerName, UUID freezerUUID, Long unfreezeDate, String reason, List<String> servers) {
        Connection connection = null;
        PreparedStatement ps = null;

        String serversString = "";
        for (String server : servers) {
            serversString += server + ", ";
        }
        serversString = serversString.length() > 1 ? serversString.substring(0, serversString.length() - 2) : serversString;
        int commaIndex = serversString.indexOf(",");
        if (commaIndex > 0) {
            serversString = serversString.substring(0, commaIndex) + " and" + serversString.substring(commaIndex + 1, serversString.length());
        }

        String valuesString = "(freezee_name, freezee_uuid, freezer_name, freezer_uuid, unfreeze_date, reason, servers, source_server) VALUES ('" + freezeeName + "', '" + freezeeUUID.toString() + "', '" + freezerName + "', ?, ?, '" + reason + "', '" + serversString + "', '" + this.plugin.getServerID().toLowerCase() + "')";

        try {
            connection = this.mySQL.getConnection();

            for (String server : servers) {
                try {
                    String update = "INSERT INTO sf_" + server.toLowerCase() + "_freezes " + valuesString + ";";
                    ps = connection.prepareStatement(update);

                    if (freezerUUID == null) {
                        ps.setNull(1, Types.VARCHAR);
                    } else {
                        ps.setString(1, freezerUUID.toString());
                    }

                    if (unfreezeDate == null) {
                        ps.setNull(2, Types.BLOB);
                    } else {
                        ps.setLong(2, unfreezeDate);
                    }

                    ps.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addUnfreeze(String unfreezeeName, UUID unfreezeeUUID, String unfreezerName) {
        Connection connection = null;
        PreparedStatement ps = null;

        String valuesString = "(unfreezee_name, unfreezee_uuid, unfreezer_name, source_server) VALUES ('" + unfreezeeName + "', '" + unfreezeeUUID.toString() + "', '" + unfreezerName + "', '" + plugin.getServerID().toLowerCase() + "')";

        try {
            connection = this.mySQL.getConnection();

            for (String server : this.getServerIDs()) {
                try {
                    String request = "SELECT * FROM sf_" + server.toLowerCase() + "_frozenlist WHERE player_uuid = '" + unfreezeeUUID.toString() + "';";
                    ps = connection.prepareStatement(request);
                    ResultSet rs = ps.executeQuery();
                    boolean frozen = false;
                    if (rs.next()) {
                        frozen = true;
                    }
                    rs.close();
                    ps.close();
                    if (frozen) {
                        String update = "INSERT INTO sf_" + server.toLowerCase() + "_unfreezes " + valuesString + ";";
                        ps = connection.prepareStatement(update);
                        ps.execute();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean checkIfFrozen(UUID uuid) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        String request;
        String uuidStr = uuid.toString();
        boolean frozen = false;
        try {
            connection = mySQL.getConnection();

            serverLoop:
            for (String server : this.getServerIDs()) {
                request = "SELECT * FROM sf_" + server + "_frozenlist;";
                ps = connection.prepareStatement(request);
                res = ps.executeQuery();

                if (res != null) {
                    while (res.next()) {
                        if (uuidStr.equals(res.getString("player_uuid"))) {
                            frozen = true;
                            break serverLoop;
                        }
                    }
                    try {
                        res.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return frozen;
    }

    public void syncFrozenList(List<UUID> frozenList) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        String request;
        List<UUID> removeList = new ArrayList<>();
        try {
            connection = this.mySQL.getConnection();

            request = "SELECT * FROM `sf_" + this.plugin.getServerID().toLowerCase() + "_frozenlist`;";
            ps = connection.prepareStatement(request);
            res = ps.executeQuery();

            if (res != null) {
                while (res.next()) {
                    UUID playerUUID = UUID.fromString(res.getString("player_uuid"));
                    if (!frozenList.contains(playerUUID)) {
                        removeList.add(playerUUID);
                    } else {
                        frozenList.remove(playerUUID);
                    }
                }
                try {
                    res.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            for (UUID uuid : frozenList) {
                String update = "INSERT INTO sf_" + this.plugin.getServerID().toLowerCase() + "_frozenlist (player_uuid) VALUES ('" + uuid.toString() + "');";
                try {
                    ps = connection.prepareStatement(update);
                    if (ps != null) {
                        ps.execute();
                        ps.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            for (UUID uuid : removeList) {
                String update = "DELETE FROM sf_" + this.plugin.getServerID().toLowerCase() + "_frozenlist WHERE player_uuid = '" + uuid.toString() + "';";
                try {
                    ps = connection.prepareStatement(update);
                    if (ps != null) {
                        ps.execute();
                        ps.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addToFrozenList(UUID uuid) {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = this.mySQL.getConnection();

            String update = "INSERT INTO sf_" + this.plugin.getServerID().toLowerCase() + "_frozenlist (player_uuid) VALUES ('" + uuid.toString() + "');";
            ps = connection.prepareStatement(update);
            ps.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeFromFrozenList(UUID uuid) {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = this.mySQL.getConnection();

            String update = "DELETE FROM sf_" + this.plugin.getServerID().toLowerCase() + "_frozenlist WHERE player_uuid = '" + uuid.toString() + "';";
            ps = connection.prepareStatement(update);
            ps.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<String> getFrozenServers(UUID uuid) {
        List<String> frozenServers = new ArrayList<String>();

        if (!this.plugin.usingMySQL()) {
            return frozenServers;
        }

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        String request;
        String uuidStr = uuid.toString();
        try {
            connection = mySQL.getConnection();

            for (String server : this.getServerIDs()) {
                request = "SELECT * FROM sf_" + server.toLowerCase() + "_frozenlist;";
                ps = connection.prepareStatement(request);
                res = ps.executeQuery();

                if (res != null) {
                    while (res.next()) {
                        if (uuidStr.equals(res.getString("player_uuid"))) {
                            frozenServers.add(server);
                        }
                    }
                    try {
                        res.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return frozenServers;
    }

}
