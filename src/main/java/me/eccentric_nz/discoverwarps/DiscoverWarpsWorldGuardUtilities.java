package me.eccentric_nz.discoverwarps;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class DiscoverWarpsWorldGuardUtilities {

    public static String isPlateInRegion(DiscoverWarps plugin, Location l, CommandSender sender) {
        // check if the plate is inside a WorldGuard region
        String region_name = "";
        WorldGuardPlatform wg = WorldGuard.getInstance().getPlatform();
        RegionManager rm = wg.getRegionContainer().get(new BukkitWorld(l.getWorld()));
        BlockVector3 vector = BlockVector3.at(l.getX(), l.getY(), l.getZ());
        ApplicableRegionSet ars = rm.getApplicableRegions(vector);
        if (ars.size() > 0) {
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
            parentNames.forEach(regions::remove);
            region_name = regions.getFirst();
            sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.region_found"), region_name));
        }
        return region_name;
    }
}
