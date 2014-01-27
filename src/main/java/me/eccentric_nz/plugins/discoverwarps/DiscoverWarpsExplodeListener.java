package me.eccentric_nz.plugins.discoverwarps;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class DiscoverWarpsExplodeListener implements Listener {

    private final DiscoverWarps plugin;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();

    public DiscoverWarpsExplodeListener(DiscoverWarps plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlateExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        List<Block> blocks = event.blockList();
        Statement statement = null;
        ResultSet rsWarpBlocks = null;
        try {
            Connection connection = service.getConnection();
            statement = connection.createStatement();
            String queryWarpBlocks = "SELECT * FROM discoverwarps";
            rsWarpBlocks = statement.executeQuery(queryWarpBlocks);
            if (rsWarpBlocks.isBeforeFirst()) {
                while (rsWarpBlocks.next()) {
                    String foo = rsWarpBlocks.getString("world").trim();
                    World explosionWorld = Bukkit.getServer().getWorld(foo);
                    int x = rsWarpBlocks.getInt("x");
                    int y = rsWarpBlocks.getInt("y");
                    int z = rsWarpBlocks.getInt("z");
                    Block block = explosionWorld.getBlockAt(x, y, z);
                    Block under = block.getRelative(BlockFace.DOWN);
                    // if the block is a DiscoverPlate then remove it
                    if (blocks.contains(under)) {
                        blocks.remove(under);
                    }
                    if (blocks.contains(block)) {
                        blocks.remove(block);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.debug("Explosion Listener error, " + e);
        } finally {
            if (rsWarpBlocks != null) {
                try {
                    rsWarpBlocks.close();
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
