package me.eccentric_nz.discoverwarps;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class DiscoverWarpsMoveListener implements Listener {

    DiscoverWarps plugin;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();
    HashMap<UUID, List<String>> regionPlayers = new HashMap<>();
    WorldGuardPlugin wg;

    public DiscoverWarpsMoveListener(DiscoverWarps plugin) {
        this.plugin = plugin;
        wg = (WorldGuardPlugin) plugin.pm.getPlugin("WorldGuard");
        setupRegionPlayers();
    }

    /**
     * Gets the innermost region of a set of WorldGuard regions.
     *
     * @param ars The WorldGuard ApplicableRegionSet to search
     * @return the region name
     */
    public static String getRegion(ApplicableRegionSet ars) {
        LinkedList<String> parentNames = new LinkedList<>();
        LinkedList<String> regions = new LinkedList<>();
        for (ProtectedRegion pr : ars) {
            String id = pr.getId();
            regions.add(id);
            ProtectedRegion parent = pr.getParent();
            while (parent != null) {
                parentNames.add(parent.getId());
                parent = parent.getParent();
            }
        }
        parentNames.forEach((name) -> {
            regions.remove(name);
        });
        return regions.getFirst();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        if (p.hasPermission("discoverwarps.use")) {
            Location l = event.getTo();
            Location loc = p.getLocation(); // Grab Location

            /**
             * Copyright (c) 2011, The Multiverse Team All rights reserved.
             * Check the Player has actually moved a block to prevent unneeded
             * calculations... This is to prevent huge performance drops on high
             * player count servers.
             */
            DiscoverWarpsSession dws = plugin.getDiscoverWarpsSession(p);
            dws.setStaleLocation(loc);

            // If the location is stale, ie: the player isn't actually moving xyz coords, they're looking around
            if (dws.isStaleLocation()) {
                return;
            }
            RegionManager rm = wg.getRegionManager(l.getWorld());
            ApplicableRegionSet ars = rm.getApplicableRegions(l);
            if (ars.size() > 0) {
                // get the region
                String region = getRegion(ars);
                String w = l.getWorld().getName();
                boolean discovered = false;
                boolean firstplate = true;
                Statement statement = null;
                ResultSet rsPlate = null;
                ResultSet rsPlayer = null;
                try {
                    Connection connection = service.getConnection();
                    statement = connection.createStatement();
                    // get their current gamemode inventory from database
                    String getQuery = "SELECT * FROM discoverwarps WHERE world = '" + w + "' AND region = '" + region + "'";
                    rsPlate = statement.executeQuery(getQuery);
                    if (rsPlate.next() && (!regionPlayers.containsKey(uuid) || !regionPlayers.get(uuid).contains(region))) {
                        // add region to the player's list
                        List<String> theList;
                        if (regionPlayers.containsKey(uuid)) {
                            theList = regionPlayers.get(uuid);
                        } else {
                            theList = new ArrayList<>();
                        }
                        theList.add(region);
                        regionPlayers.put(uuid, theList);
                        // found a discoverplate
                        boolean enabled = rsPlate.getBoolean("enabled");
                        if (enabled) {
                            String id = rsPlate.getString("id");
                            String warp = rsPlate.getString("name");
                            String queryDiscover = "";
                            // check whether they have visited this plate before
                            String queryPlayer = "SELECT * FROM players WHERE uuid = '" + uuid.toString() + "'";
                            rsPlayer = statement.executeQuery(queryPlayer);
                            if (rsPlayer.next()) {
                                firstplate = false;
                                String data = rsPlayer.getString("visited");
                                String[] visited = data.split(",");
                                if (Arrays.asList(visited).contains(id)) {
                                    discovered = true;
                                }
                                if (discovered == false) {
                                    queryDiscover = "UPDATE players SET visited = '" + data + "," + id + "', regions = '" + rsPlayer.getString("regions") + "," + region + "' WHERE uuid = '" + uuid + "'";
                                }
                            }
                            if (discovered == false && firstplate == true) {
                                queryDiscover = "INSERT INTO players (uuid, visited, regions) VALUES ('" + uuid + "','" + id + "','" + region + "')";
                            }
                            statement.executeUpdate(queryDiscover);
                            if (plugin.getConfig().getBoolean("xp_on_discover") && discovered == false) {
                                loc.setX(loc.getBlockX() + 1);
                                World world = loc.getWorld();
                                world.spawn(loc, ExperienceOrb.class).setExperience(plugin.getConfig().getInt("xp_to_give"));
                            }
                            if (discovered == false) {
                                p.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.discovered"), warp));
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

    private void setupRegionPlayers() {
        // get regions players have visited
        ResultSet rs = null;
        Statement statement = null;
        try {
            Connection connection = service.getConnection();
            statement = connection.createStatement();
            String query = "SELECT uuid, regions FROM players";
            rs = statement.executeQuery(query);
            if (rs != null && rs.isBeforeFirst()) {
                while (rs.next()) {
                    String r = rs.getString("regions");
                    if (!rs.wasNull()) {
                        List<String> regions = new ArrayList<>(Arrays.asList(r.split(",")));
                        regionPlayers.put(UUID.fromString(rs.getString("uuid")), regions);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.debug("Could not get region lists!");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
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
