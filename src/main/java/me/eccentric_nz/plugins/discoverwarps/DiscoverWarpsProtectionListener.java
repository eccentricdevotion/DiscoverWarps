package me.eccentric_nz.plugins.discoverwarps;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class DiscoverWarpsProtectionListener implements Listener {

    private DiscoverWarps plugin;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();

    public DiscoverWarpsProtectionListener(DiscoverWarps plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlateBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        Material m = b.getType();
        if (m.equals(Material.STONE_PLATE) || b.getRelative(BlockFace.UP).getType().equals(Material.STONE_PLATE)) {
            Location l = b.getLocation();
            String w = l.getWorld().getName();
            int x = l.getBlockX();
            int y = l.getBlockY();
            if (b.getRelative(BlockFace.UP).getType().equals(Material.STONE_PLATE)) {
                y += 1;
            }
            int z = l.getBlockZ();
            Statement statement = null;
            ResultSet rsPlate = null;
            try {
                Connection connection = service.getConnection();
                statement = connection.createStatement();
                String getQuery = "SELECT name FROM discoverwarps WHERE world = '" + w + "' AND x = " + x + " AND y = " + y + " AND z = " + z;
                rsPlate = statement.executeQuery(getQuery);
                if (rsPlate.isBeforeFirst()) {
                    Player p = event.getPlayer();
                    p.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "You cannot break this pressure plate, use " + ChatColor.GREEN + "/dw delete [name]" + ChatColor.RESET + " to remove it.");
                    event.setCancelled(true);
                }
            } catch (SQLException e) {
                plugin.debug("Could not find discover plate to protect, " + e);
            } finally {
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