/*
 *  Copyright 2014 eccentric_nz.
 */
package me.eccentric_nz.discoverwarps;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author eccentric_nz
 */
public class DiscoverWarpsGUIInventory {

    private final DiscoverWarps plugin;
    private final UUID uuid;
    private final ItemStack[] warps;

    public DiscoverWarpsGUIInventory(DiscoverWarps plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
        warps = getStacks();
    }

    private ItemStack[] getStacks() {
        ItemStack[] stack = new ItemStack[54];
        Statement statement = null;
        ResultSet rs = null;
        try {
            DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();
            Connection connection = service.getConnection();
            statement = connection.createStatement();
            List<String> visited = new ArrayList<>();
            // get players visited plates
            String queryVisited = "SELECT visited FROM players WHERE uuid = '" + uuid.toString() + "'";
            ResultSet rsVisited = statement.executeQuery(queryVisited);
            if (rsVisited.isBeforeFirst()) {
                visited = Arrays.asList(rsVisited.getString("visited").split(","));
            }
            String queryList = "SELECT id, name, auto, cost, icon FROM discoverwarps WHERE enabled = 1";
            rs = statement.executeQuery(queryList);
            // check name is valid
            if (rs.isBeforeFirst()) {
                int i = 0;
                while (rs.next()) {
                    if (visited.contains(rs.getString("id")) || rs.getBoolean("auto")) {
                        String warp = rs.getString("name");
                        Material material;
                        try {
                            material = Material.valueOf(rs.getString("icon"));
                        } catch (IllegalArgumentException e) {
                            material = Material.STONE_PRESSURE_PLATE;
                        }
                        ItemStack is = new ItemStack(material, 1);
                        ItemMeta im = is.getItemMeta();
                        im.setDisplayName(warp);
                        is.setItemMeta(im);
                        stack[i] = is;
                        if (i > 52) {
                            break;
                        }
                        i++;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.debug("Could not get visited discover plates for GUI, " + e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {
                }
            }
        }
        return stack;
    }

    public ItemStack[] getWarps() {
        return warps;
    }
}
