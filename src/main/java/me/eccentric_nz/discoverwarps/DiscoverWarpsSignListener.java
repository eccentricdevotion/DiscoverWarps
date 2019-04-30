package me.eccentric_nz.discoverwarps;

import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class DiscoverWarpsSignListener implements Listener {

    DiscoverWarps plugin;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();

    public DiscoverWarpsSignListener(DiscoverWarps plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        Action a = event.getAction();
        Block b = event.getClickedBlock();
        if (a.equals(Action.RIGHT_CLICK_BLOCK) && Tag.SIGNS.getValues().contains(b.getType())) {
            Sign s = (Sign) b.getState();
            if (s.getLine(0).equalsIgnoreCase("[" + plugin.getConfig().getString("sign") + "]")) {
                Player p = event.getPlayer();
                String uuid = p.getUniqueId().toString();
                if (p.hasPermission("discoverwarps.use")) {
                    String plate = s.getLine(1);
                    Statement statement = null;
                    ResultSet rsPlate = null;
                    ResultSet rsPlayer = null;
                    try {
                        Connection connection = service.getConnection();
                        statement = connection.createStatement();
                        // get their current gamemode inventory from database
                        String getQuery = "SELECT * FROM discoverwarps WHERE name = '" + plate + "'";
                        rsPlate = statement.executeQuery(getQuery);
                        if (rsPlate.next()) {
                            // is a discoverplate
                            boolean enabled = rsPlate.getBoolean("enabled");
                            if (enabled) {
                                String id = rsPlate.getString("id");
                                String warp = rsPlate.getString("name");
                                World w = plugin.getServer().getWorld(rsPlate.getString("world"));
                                int x = rsPlate.getInt("x");
                                int y = rsPlate.getInt("y");
                                int z = rsPlate.getInt("z");
                                double cost = rsPlate.getDouble("cost");
                                boolean auto = rsPlate.getBoolean("auto");
                                String queryDiscover = "";
                                // check whether they have visited this plate before
                                String queryPlayer = "SELECT * FROM players WHERE uuid = '" + uuid + "'";
                                rsPlayer = statement.executeQuery(queryPlayer);
                                boolean firstplate = true;
                                boolean discovered = false;
                                if (rsPlayer.next()) {
                                    firstplate = false;
                                    String data = rsPlayer.getString("visited");
                                    String[] visited = data.split(",");
                                    if (Arrays.asList(visited).contains(id)) {
                                        discovered = true;
                                    }
                                    if (discovered == false && auto == false) {
                                        // check if there is a cost
                                        if (cost > 0 && plugin.getConfig().getBoolean("allow_buying")) {
                                            // check if they have sufficient balance
                                            double bal = plugin.economy.getBalance(p);
                                            if (cost > bal) {
                                                p.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.signs.no_money"));
                                                return;
                                            }
                                            plugin.economy.withdrawPlayer(p, cost);
                                            queryDiscover = "UPDATE players SET visited = '" + data + "," + id + "' WHERE uuid = '" + uuid + "'";
                                        } else {
                                            p.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.signs.needs_discover"), warp));
                                            return;
                                        }
                                    }
                                }
                                if (discovered == false && firstplate == true) {
                                    queryDiscover = "INSERT INTO players (uuid, visited) VALUES ('" + uuid + "','" + id + "')";
                                }
                                statement.executeUpdate(queryDiscover);
                                // warp to location
                                Location l = new Location(w, x, y, z);
                                l.setPitch(p.getLocation().getPitch());
                                l.setYaw(p.getLocation().getYaw());
                                new DiscoverWarpsMover(plugin).movePlayer(p, l, p.getLocation().getWorld());
                                if (discovered == false) {
                                    p.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.discovered"), warp));
                                }
                                rsPlayer.close();
                                rsPlate.close();
                                statement.close();
                            }
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not update player's visited data from sign, " + e);
                    } finally {
                        if (rsPlayer != null) {
                            try {
                                rsPlayer.close();
                            } catch (SQLException e) {
                            }
                        }
                        if (rsPlate != null) {
                            try {
                                rsPlate.close();
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
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String line1 = event.getLine(0);
        String firstline = "[" + plugin.getConfig().getString("sign") + "]";
        if (line1.equalsIgnoreCase(firstline)) {
            Player player = event.getPlayer();
            if (player.hasPermission("discoverwarps.admin")) {
                String line2 = event.getLine(1);
                Statement statement = null;
                ResultSet rsPlate = null;
                try {
                    Connection connection = service.getConnection();
                    statement = connection.createStatement();
                    // get their current gamemode inventory from database
                    String getQuery = "SELECT * FROM discoverwarps WHERE name = '" + line2 + "'";
                    rsPlate = statement.executeQuery(getQuery);
                    if (!rsPlate.next()) {
                        player.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                        event.setCancelled(true);
                        return;
                    }
                    double cost = rsPlate.getDouble("cost");
                    if (cost > 0) {
                        event.setLine(2, "" + cost);
                    }
                    player.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.signs.sign_made"));
                } catch (SQLException e) {
                    plugin.debug("Could not get data for sign, " + e);
                } finally {
                    if (rsPlate != null) {
                        try {
                            rsPlate.close();
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
            } else {
                player.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.signs.sign_permission"));
                event.setCancelled(true);
            }
        }
    }
}
