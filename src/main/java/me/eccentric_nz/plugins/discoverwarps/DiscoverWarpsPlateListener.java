package me.eccentric_nz.plugins.discoverwarps;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DiscoverWarpsPlateListener implements Listener {

    DiscoverWarps plugin;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();
    List<Material> validBlocks = new ArrayList<Material>();

    public DiscoverWarpsPlateListener(DiscoverWarps plugin) {
        this.plugin = plugin;
        validBlocks.add(Material.WOOD_PLATE);
        validBlocks.add(Material.STONE_PLATE);
    }

    @EventHandler
    public void onPlateStep(PlayerInteractEvent event) {
        Action a = event.getAction();
        Block b = event.getClickedBlock();
        if (a.equals(Action.PHYSICAL) && validBlocks.contains(b.getType())) {
            Player p = event.getPlayer();
            String name = p.getName();
            if (p.hasPermission("discoverwarps.use")) {
                Location l = b.getLocation();
                String w = l.getWorld().getName();
                int x = l.getBlockX();
                int y = l.getBlockY();
                int z = l.getBlockZ();
                boolean discovered = false;
                boolean firstplate = true;
                Statement statement = null;
                ResultSet rsPlate = null;
                ResultSet rsPlayer = null;
                try {
                    Connection connection = service.getConnection();
                    statement = connection.createStatement();
                    // get their current gamemode inventory from database
                    String getQuery = "SELECT * FROM discoverwarps WHERE world = '" + w + "' AND x = " + x + " AND y = " + y + " AND z = " + z;
                    rsPlate = statement.executeQuery(getQuery);
                    if (rsPlate.next()) {
                        // is a discoverplate
                        boolean enabled = rsPlate.getBoolean("enabled");
                        if (enabled) {
                            String id = rsPlate.getString("id");
                            String warp = rsPlate.getString("name");
                            String queryDiscover = "";
                            // check whether they have visited this plate before
                            String queryPlayer = "SELECT * FROM players WHERE player = '" + name + "'";
                            rsPlayer = statement.executeQuery(queryPlayer);
                            if (rsPlayer.next()) {
                                firstplate = false;
                                String data = rsPlayer.getString("visited");
                                String[] visited = data.split(",");
                                if (Arrays.asList(visited).contains(id)) {
                                    discovered = true;
                                }
                                if (discovered == false) {
                                    queryDiscover = "UPDATE players SET visited = '" + data + "," + id + "' WHERE player = '" + name + "'";
                                }
                            }
                            if (discovered == false && firstplate == true) {
                                queryDiscover = "INSERT INTO players (player, visited) VALUES ('" + name + "','" + id + "')";
                            }
                            statement.executeUpdate(queryDiscover);
                            if (plugin.getConfig().getBoolean("xp_on_discover") && discovered == false) {
                                Location loc = p.getLocation();
                                loc.setX(loc.getBlockX() + 1);
                                World world = loc.getWorld();
                                ((ExperienceOrb) world.spawn(loc, ExperienceOrb.class)).setExperience(plugin.getConfig().getInt("xp_to_give"));
                                //p.giveExp(plugin.getConfig().getInt("xp_to_give"));
                            }
                            if (discovered == false) {
                                p.sendMessage(ChatColor.GOLD + "[" + plugin.getConfig().getString("localisation.plugin_name") + "] " + ChatColor.RESET + String.format(plugin.getConfig().getString("localisation.discovered"), warp));
                            }
                            rsPlayer.close();
                            rsPlate.close();
                            statement.close();
                        }
                    }
                } catch (SQLException e) {
                    plugin.debug("Could not update player's visited data, " + e);
                } finally {
                    if (rsPlayer != null) {
                        try {
                            rsPlayer.close();
                        } catch (Exception e) {
                        }
                    }
                    if (rsPlate != null) {
                        try {
                            rsPlate.close();
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
        }
    }
}
