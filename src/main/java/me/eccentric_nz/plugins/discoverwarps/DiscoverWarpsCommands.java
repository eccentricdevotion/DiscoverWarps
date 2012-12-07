package me.eccentric_nz.plugins.discoverwarps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscoverWarpsCommands implements CommandExecutor {

    private DiscoverWarps plugin;
    List<String> admincmds;
    List<String> usercmds;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();

    public DiscoverWarpsCommands(DiscoverWarps plugin) {
        this.plugin = plugin;
        this.admincmds = new ArrayList<String>();
        admincmds.add("set");
        admincmds.add("delete");
        admincmds.add("enable");
        admincmds.add("disable");
        this.usercmds = new ArrayList<String>();
        usercmds.add("tp");
        usercmds.add("list");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("discoverwarps")) {
            if (args.length == 0) {
                sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "Help");
                sender.sendMessage("------------");
                sender.sendMessage(DiscoverWarpsConstants.HELP.split("\n"));
                return true;
            }
            if (admincmds.contains(args[0])) {
                if (!sender.hasPermission("discoverwarps.admin")) {
                    sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "You do not have permission to run that command!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "Not enough command arguments!");
                    return false;
                }
                if (args[0].equalsIgnoreCase("set")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        Location l = p.getLocation();
                        //l.setY(l.getY() - .2);
                        Block b = l.getBlock();
                        // check player is standing on pressure plate
                        Material m = b.getType();
                        if (!m.equals(Material.STONE_PLATE)) {
                            sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "You are not standing on a stone pressure plate");
                            return true;
                        }
                        try {
                            Connection connection = service.getConnection();
                            Statement statement = connection.createStatement();
                            String queryName = "SELECT name FROM discoverwarps WHERE name = '" + args[1] + "'";
                            ResultSet rsName = statement.executeQuery(queryName);
                            // check name is not in use
                            if (rsName.isBeforeFirst()) {
                                sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "That name is already in use!");
                                return true;
                            }
                            String w = b.getLocation().getWorld().getName();
                            int x = b.getLocation().getBlockX();
                            int y = b.getLocation().getBlockY();
                            int z = b.getLocation().getBlockZ();
                            PreparedStatement ps = connection.prepareStatement("INSERT INTO discoverwarps (name, world, x, y, z, enabled) VALUES (?, ?, ?, ?, ?, 1)");
                            ps.setString(1, args[1]);
                            ps.setString(2, w);
                            ps.setInt(3, x);
                            ps.setInt(4, y);
                            ps.setInt(5, z);
                            ps.executeUpdate();
                            sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "DiscoverPlate " + args[1] + " added!");
                        } catch (SQLException e) {
                            plugin.debug("Could not insert new discover plate, " + e);
                        }
                    } else {
                        sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "Only a player can use the 'set' command!");
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("delete")) {
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        String queryName = "SELECT * FROM discoverwarps WHERE name = '" + args[1] + "'";
                        ResultSet rsName = statement.executeQuery(queryName);
                        // check name is valid
                        if (rsName.next()) {
                            World w = plugin.getServer().getWorld(rsName.getString("world"));
                            int x = rsName.getInt("x");
                            int y = rsName.getInt("y");
                            int z = rsName.getInt("z");
                            String queryDel = "DELETE FROM discoverwarps WHERE name = '" + args[1] + "'";
                            statement.executeUpdate(queryDel);
                            Block b = w.getBlockAt(x, y, z);
                            b.setTypeId(0);
                            sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "DiscoverPlate " + args[1] + " deleted!");
                            return true;
                        } else {
                            sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "Couldn't find a DiscoverPlate with that name!");
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not delete discover plate, " + e);
                    }
                }
                if (args[0].equalsIgnoreCase("enable")) {
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        String queryName = "SELECT name FROM discoverwarps WHERE name = '" + args[1] + "'";
                        ResultSet rsName = statement.executeQuery(queryName);
                        // check name is valid
                        if (rsName.isBeforeFirst()) {
                            String queryDel = "UPDATE discoverwarps SET enabled = 1 WHERE name = '" + args[1] + "'";
                            statement.executeUpdate(queryDel);
                            sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "DiscoverPlate " + args[1] + " enabled!");
                            return true;
                        } else {
                            sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "Couldn't find a DiscoverPlate with that name!");
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not delete discover plate, " + e);
                    }
                }
                if (args[0].equalsIgnoreCase("disable")) {
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        String queryName = "SELECT name FROM discoverwarps WHERE name = '" + args[1] + "'";
                        ResultSet rsName = statement.executeQuery(queryName);
                        // check name is valid
                        if (rsName.isBeforeFirst()) {
                            String queryDel = "UPDATE discoverwarps SET enabled = 0 WHERE name = '" + args[1] + "'";
                            statement.executeUpdate(queryDel);
                            sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "DiscoverPlate " + args[1] + " enabled!");
                            return true;
                        } else {
                            sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "Couldn't find a DiscoverPlate with that name!");
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not delete discover plate, " + e);
                    }
                }
            }
            if (usercmds.contains(args[0])) {
                if (!sender.hasPermission("discoverwarps.use")) {
                    sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "You do not have permission to run that command!");
                    return true;
                }
                if (args[0].equalsIgnoreCase("list")) {
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        List<String> visited = new ArrayList<String>();
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            String p = player.getName();
                            // get players visited plates
                            String queryVisited = "SELECT visited FROM players WHERE player = '" + p + "'";
                            ResultSet rsVisited = statement.executeQuery(queryVisited);
                            if (rsVisited.isBeforeFirst()) {
                                visited = Arrays.asList(rsVisited.getString("visited").split(","));
                            }
                        }
                        String queryList = "SELECT id, name FROM discoverwarps WHERE enabled = 1";
                        ResultSet rsList = statement.executeQuery(queryList);
                        // check name is valid
                        if (rsList.isBeforeFirst()) {
                            sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "List");
                            sender.sendMessage("------------");
                            int i = 1;
                            String discovered;
                            while (rsList.next()) {
                                discovered = (visited.contains(rsList.getString("id"))) ? ChatColor.GREEN + "VISITED" : ChatColor.RED + "NOT VISITED";
                                String warp = rsList.getString("name");
                                sender.sendMessage(i + ". " + warp + " " + discovered);
                                i++;
                            }
                            sender.sendMessage("------------");
                            return true;
                        } else {
                            sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "There are no DiscoverPlates to find!");
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not list discover plates, " + e);
                    }
                }
                if (args[0].equalsIgnoreCase("tp")) {
                    Player player = null;
                    if (sender instanceof Player) {
                        player = (Player) sender;
                    } else {
                        sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "This command requires a player!");
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "You need to supply a warp name!");
                        return false;
                    }
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        String queryName = "SELECT * FROM discoverwarps WHERE name = '" + args[1] + "'";
                        ResultSet rsName = statement.executeQuery(queryName);
                        // check name is valid
                        if (rsName.next()) {
                            String id = rsName.getString("id");
                            String warp = rsName.getString("name");
                            World w = plugin.getServer().getWorld(rsName.getString("world"));
                            int x = rsName.getInt("x");
                            int y = rsName.getInt("y");
                            int z = rsName.getInt("z");
                            List<String> visited = new ArrayList<String>();
                            // can the player tp to here?
                            String p = player.getName();
                            // get players visited plates
                            String queryVisited = "SELECT visited FROM players WHERE player = '" + p + "'";
                            ResultSet rsVisited = statement.executeQuery(queryVisited);
                            if (rsVisited.isBeforeFirst()) {
                                visited = Arrays.asList(rsVisited.getString("visited").split(","));
                            }
                            if (!visited.contains(id)) {
                                sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "You need to discover '" + warp + "' before you can teleport to it!");
                                return true;
                            }
                            World from = player.getLocation().getWorld();
                            Location l = new Location(w, x, y, z);
                            l.setPitch(player.getLocation().getPitch());
                            l.setYaw(player.getLocation().getYaw());
                            movePlayer(player, l, from);
                            return true;
                        } else {
                            sender.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "Couldn't find a DiscoverPlate with that name!");
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not delete discover plate, " + e);
                    }
                }
            }
        }
        return false;
    }

    private void movePlayer(Player p, Location l, World from) {

        p.sendMessage(DiscoverWarpsConstants.MY_PLUGIN_NAME + "Teleporting...");

        final Player thePlayer = p;
        final Location theLocation = l;
        final World to = theLocation.getWorld();
        final boolean allowFlight = thePlayer.getAllowFlight();
        final boolean crossWorlds = from != to;

        // try loading chunk
        World world = l.getWorld();
        Chunk chunk = world.getChunkAt(l);
        if (!world.isChunkLoaded(chunk)) {
            world.loadChunk(chunk);
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                thePlayer.teleport(theLocation);
                thePlayer.getWorld().playSound(theLocation, Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
        }, 5L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                thePlayer.teleport(theLocation);
//                if (plugin.getConfig().getBoolean("no_damage")) {
//                    thePlayer.setNoDamageTicks(plugin.getConfig().getInt("no_damage_time") * 20);
//                }
                if (thePlayer.getGameMode() == GameMode.CREATIVE || (allowFlight && crossWorlds)) {
                    thePlayer.setAllowFlight(true);
                }
            }
        }, 10L);
    }
}
