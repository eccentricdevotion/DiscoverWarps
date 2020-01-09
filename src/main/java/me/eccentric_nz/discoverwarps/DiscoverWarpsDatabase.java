package me.eccentric_nz.discoverwarps;

import java.sql.*;

public class DiscoverWarpsDatabase {

    private static final DiscoverWarpsDatabase instance = new DiscoverWarpsDatabase();
    public Connection connection = null;
    public Statement statement = null;
    private DiscoverWarps plugin;

    public static synchronized DiscoverWarpsDatabase getInstance() {
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(String path) throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public void createTables() {
        ResultSet rsNew = null;
        ResultSet rsWG = null;
        ResultSet rsI = null;
        ResultSet rsUUID = null;
        ResultSet rsRegions = null;
        try {
            statement = connection.createStatement();
            String queryWarps = "CREATE TABLE IF NOT EXISTS discoverwarps (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT COLLATE NOCASE, world TEXT, x INTEGER, y INTEGER, z INTEGER, enabled INTEGER, auto INTEGER DEFAULT 0, cost INTEGER DEFAULT 0, icon TEXT DEFAULT 'STONE_PRESSURE_PLATE')";
            statement.executeUpdate(queryWarps);
            String queryVisited = "CREATE TABLE IF NOT EXISTS players (pid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, uuid TEXT DEFAULT '', player TEXT COLLATE NOCASE DEFAULT '', visited TEXT DEFAULT '', regions TEXT DEFAULT '')";
            statement.executeUpdate(queryVisited);
            // update discoverwarps if there is no auto column
            String queryAuto = "SELECT sql FROM sqlite_master WHERE tbl_name = 'discoverwarps' AND sql LIKE '%auto INTEGER%'";
            rsNew = statement.executeQuery(queryAuto);
            if (!rsNew.next()) {
                String queryAlter1 = "ALTER TABLE discoverwarps ADD auto INTEGER DEFAULT 0";
                String queryAlter2 = "ALTER TABLE discoverwarps ADD cost INTEGER DEFAULT 0";
                statement.executeUpdate(queryAlter1);
                statement.executeUpdate(queryAlter2);
                System.out.println("[DiscoverWarps] Added new fields to database!");
            }
            rsNew.close();
            // update discoverwarps if there is no region column
            String queryWG = "SELECT sql FROM sqlite_master WHERE tbl_name = 'discoverwarps' AND sql LIKE '%region TEXT%'";
            rsWG = statement.executeQuery(queryWG);
            if (!rsWG.next()) {
                String queryAlterWG = "ALTER TABLE discoverwarps ADD region TEXT DEFAULT ''";
                statement.executeUpdate(queryAlterWG);
            }
            // update discoverwarps if there is no icon column
            String queryI = "SELECT sql FROM sqlite_master WHERE tbl_name = 'discoverwarps' AND sql LIKE '%icon TEXT%'";
            rsI = statement.executeQuery(queryI);
            if (!rsI.next()) {
                String queryAlterWG = "ALTER TABLE discoverwarps ADD icon TEXT DEFAULT 'STONE_PRESSURE_PLATE'";
                statement.executeUpdate(queryAlterWG);
            }
            rsI.close();
            // update players if there is no uuid column
            String queryUUID = "SELECT sql FROM sqlite_master WHERE tbl_name = 'players' AND sql LIKE '%uuid TEXT%'";
            rsUUID = statement.executeQuery(queryUUID);
            if (!rsUUID.next()) {
                String queryAlterUUID = "ALTER TABLE players ADD uuid TEXT DEFAULT ''";
                statement.executeUpdate(queryAlterUUID);
            }
            rsUUID.close();
            // update players if there is no regions column
            String queryRegions = "SELECT sql FROM sqlite_master WHERE tbl_name = 'players' AND sql LIKE '%regions TEXT%'";
            rsRegions = statement.executeQuery(queryRegions);
            if (!rsRegions.next()) {
                String queryAlterRegions = "ALTER TABLE players ADD regions TEXT DEFAULT ''";
                statement.executeUpdate(queryAlterRegions);
            }
            rsRegions.close();
        } catch (SQLException e) {
            plugin.debug("Create table error: " + e);
        } finally {
            if (rsNew != null) {
                try {
                    rsNew.close();
                } catch (SQLException e) {
                }
            }
            if (rsWG != null) {
                try {
                    rsWG.close();
                } catch (SQLException e) {
                }
            }
            if (rsI != null) {
                try {
                    rsI.close();
                } catch (SQLException e) {
                }
            }
            if (rsUUID != null) {
                try {
                    rsUUID.close();
                } catch (SQLException e) {
                }
            }
            if (rsRegions != null) {
                try {
                    rsRegions.close();
                } catch (SQLException e) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone is not allowed.");
    }
}
