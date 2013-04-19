package me.eccentric_nz.plugins.discoverwarps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DiscoverWarpsDatabase {

    private static DiscoverWarpsDatabase instance = new DiscoverWarpsDatabase();
    public Connection connection = null;
    public Statement statement = null;
    private DiscoverWarps plugin;

    public static synchronized DiscoverWarpsDatabase getInstance() {
        return instance;
    }

    public void setConnection(String path) throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public Connection getConnection() {
        return connection;
    }

    public void createTables() {
        ResultSet rsNew = null;
        try {
            statement = connection.createStatement();
            String queryWarps = "CREATE TABLE IF NOT EXISTS discoverwarps (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, world TEXT, x INTEGER, y INTEGER, z INTEGER, enabled INTEGER, auto INTEGER DEFAULT 0, cost INTEGER DEFAULT 0)";
            statement.executeUpdate(queryWarps);
            String queryVisited = "CREATE TABLE IF NOT EXISTS players (pid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, player TEXT, visited TEXT)";
            statement.executeUpdate(queryVisited);
            // update discoverwarps if there is no auto column
            String queryAuto = "SELECT sql FROM sqlite_master WHERE tbl_name = 'discoverwarps' AND sql LIKE '%auto INTEGER%'";
            rsNew = statement.executeQuery(queryAuto);
            if (!rsNew.next()) {
                String queryAlter1 = "ALTER TABLE discoverwarps ADD auto INTEGER DEFAULT 0";
                String queryAlter2 = "ALTER TABLE discoverwarps ADD cost INTEGER DEFAULT 0";
                statement.executeUpdate(queryAlter1);
                statement.executeUpdate(queryAlter2);
                System.out.println(DiscoverWarpsConstants.MY_PLUGIN_NAME + " Added new fields to database!");
            }
            rsNew.close();
        } catch (SQLException e) {
            plugin.debug("Create table error: " + e);
        } finally {
            if (rsNew != null) {
                try {
                    rsNew.close();
                } catch (Exception e) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone is not allowed.");
    }
}
