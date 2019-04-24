/*
 *  Copyright 2014 eccentric_nz.
 */
package me.eccentric_nz.discoverwarps;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author eccentric_nz
 */
public class DiscoverWarpsGUIListener implements Listener {

    private final DiscoverWarps plugin;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();

    public DiscoverWarpsGUIListener(DiscoverWarps plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onControlMenuInteract(InventoryClickEvent event) {
        InventoryView view = event.getView();
        String name = view.getTitle();
        if (name.equals(ChatColor.RED + plugin.getConfig().getString("localisation.plugin_name"))) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            Player player = (Player) event.getWhoClicked();
            if (slot >= 0 && slot < 54) {
                ItemStack is = view.getItem(slot);
                if (is != null) {
                    // get the display name
                    ItemMeta im = is.getItemMeta();
                    String warp = im.getDisplayName();
                    // get the warp
                    Statement statement = null;
                    ResultSet rsName = null;
                    try {
                        Connection connection = service.getConnection();
                        statement = connection.createStatement();
                        String queryName = "SELECT * FROM discoverwarps WHERE name = '" + warp + "' COLLATE NOCASE";
                        rsName = statement.executeQuery(queryName);
                        // check name is valid
                        if (rsName.next()) {
                            close(player);
                            World w = plugin.getServer().getWorld(rsName.getString("world"));
                            int x = rsName.getInt("x");
                            int y = rsName.getInt("y");
                            int z = rsName.getInt("z");
                            World from = player.getLocation().getWorld();
                            Location l = new Location(w, x, y, z);
                            l.setPitch(player.getLocation().getPitch());
                            l.setYaw(player.getLocation().getYaw());
                            if (plugin.getConfig().getLong("cooldown") > 0) {
                                plugin.getDiscoverWarpCooldowns().put(player.getUniqueId(), System.currentTimeMillis());
                            }
                            new DiscoverWarpsMover(plugin).movePlayer(player, l, from);
                        } else {
                            player.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not find discover plate record, " + e);
                    } finally {
                        if (rsName != null) {
                            try {
                                rsName.close();
                            } catch (SQLException ex) {
                            }
                        }
                        if (statement != null) {
                            try {
                                statement.close();
                            } catch (SQLException ex) {
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Closes the inventory.
     *
     * @param p the player using the GUI
     */
    public void close(Player p) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            p.closeInventory();
        }, 1L);
    }
}
