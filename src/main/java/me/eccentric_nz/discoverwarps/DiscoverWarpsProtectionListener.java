package me.eccentric_nz.discoverwarps;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DiscoverWarpsProtectionListener implements Listener {

    private final DiscoverWarps plugin;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();
    List<Material> validBlocks = new ArrayList<>();

    public DiscoverWarpsProtectionListener(DiscoverWarps plugin) {
        this.plugin = plugin;
        validBlocks.add(Material.ACACIA_PRESSURE_PLATE);
        validBlocks.add(Material.BIRCH_PRESSURE_PLATE);
        validBlocks.add(Material.DARK_OAK_PRESSURE_PLATE);
        validBlocks.add(Material.JUNGLE_PRESSURE_PLATE);
        validBlocks.add(Material.OAK_PRESSURE_PLATE);
        validBlocks.add(Material.SPRUCE_PRESSURE_PLATE);
        validBlocks.add(Material.STONE_PRESSURE_PLATE);
        validBlocks.add(Material.WARPED_PRESSURE_PLATE);
        validBlocks.add(Material.CRIMSON_PRESSURE_PLATE);
        validBlocks.add(Material.MANGROVE_PRESSURE_PLATE);
        validBlocks.add(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
    }

    @EventHandler
    public void onPlateBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        Material m = b.getType();
        if (validBlocks.contains(m) || validBlocks.contains(b.getRelative(BlockFace.UP).getType())) {
            Location l = b.getLocation();
            String w = l.getWorld().getName();
            int x = l.getBlockX();
            int y = l.getBlockY();
            if (validBlocks.contains(b.getRelative(BlockFace.UP).getType())) {
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
                    p.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.no_break"), ChatColor.GREEN + "/dw delete [name]" + ChatColor.RESET));
                    event.setCancelled(true);
                }
            } catch (SQLException e) {
                plugin.debug("Could not find discover plate to protect, " + e);
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
        }
    }
}
