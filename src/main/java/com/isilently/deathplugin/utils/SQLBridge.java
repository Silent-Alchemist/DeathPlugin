package com.isilently.deathplugin.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

public class SQLBridge {
    private Connection con;
    private final JavaPlugin plugin;

    public SQLBridge(JavaPlugin plugin) {
        this.plugin = plugin;
        connect();
        setupTable();
    }

    private void connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File dbFile = new File(dataFolder, "database.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            con = DriverManager.getConnection(url);
            plugin.getLogger().info("Successfully connected to local SQLite database.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to SQLite database!", e);
        }
    }

    private void setupTable() {
        String sql = "CREATE TABLE IF NOT EXISTS player_data (" +
                "UUID TEXT PRIMARY KEY, " +
                "Name TEXT NOT NULL, " +
                "Money INTEGER NOT NULL, " +
                "LastOnline TEXT NOT NULL, " +
                "Banned BOOLEAN NOT NULL" +
                ");";
        try (Statement stm = con.createStatement()) {
            stm.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create tables!", e);
        }
    }

    public boolean playerExist(UUID uuid) {
        String sql = "SELECT 1 FROM player_data WHERE UUID = ?";
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, uuid.toString());
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean playerExist(String name) {
        String sql = "SELECT 1 FROM player_data WHERE Name = ?";
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, name);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public UUID getUUID(String name) {
        String sql = "SELECT UUID FROM player_data WHERE Name = ?";
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, name);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next())
                    return UUID.fromString(rs.getString("UUID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isBanned(UUID uuid) {
        String sql = "SELECT Banned FROM player_data WHERE UUID = ?";
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, uuid.toString());
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next())
                    return rs.getBoolean("Banned");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void banPlayer(UUID uuid, boolean banStatus) {
        String sql = "UPDATE player_data SET Banned = ? WHERE UUID = ?";
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setBoolean(1, banStatus);
            pstm.setString(2, uuid.toString());
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlayerData(UUID uuid, String name) {
        String sql = "INSERT INTO player_data(UUID, Name, Money, LastOnline, Banned) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, uuid.toString());
            pstm.setString(2, name);
            pstm.setInt(3, 0);
            pstm.setString(4, LocalDate.now().toString());
            pstm.setBoolean(5, false);
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean newDay(UUID uuid) {
        String selectSql = "SELECT LastOnline FROM player_data WHERE UUID = ?";
        String updateSql = "UPDATE player_data SET LastOnline = ? WHERE UUID = ?";

        try (PreparedStatement selectPstm = con.prepareStatement(selectSql)) {
            selectPstm.setString(1, uuid.toString());
            try (ResultSet rs = selectPstm.executeQuery()) {
                if (rs.next()) {
                    LocalDate lastOnline = LocalDate.parse(rs.getString("LastOnline"));
                    LocalDate today = LocalDate.now();

                    if (today.isAfter(lastOnline)) {
                        try (PreparedStatement updatePstm = con.prepareStatement(updateSql)) {
                            updatePstm.setString(1, today.toString());
                            updatePstm.setString(2, uuid.toString());
                            updatePstm.executeUpdate();
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getMoney(UUID uuid) {
        String sql = "SELECT Money FROM player_data WHERE UUID = ?";
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, uuid.toString());
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next())
                    return rs.getInt("Money");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setMoney(UUID uuid, int amount) {
        String sql = "UPDATE player_data SET Money = ? WHERE UUID = ?";
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setInt(1, amount);
            pstm.setString(2, uuid.toString());
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}