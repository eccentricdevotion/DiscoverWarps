package me.eccentric_nz.plugins.discoverwarps;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
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
import multiworld.MultiWorldPlugin;
import multiworld.api.MultiWorldAPI;
import multiworld.api.MultiWorldWorldData;
import multiworld.api.flag.FlagName;

public class DiscoverWarpsCommands implements CommandExecutor {

    private final DiscoverWarps plugin;
    List<String> admincmds;
    List<String> usercmds;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();
    String plugin_name;
    List<Material> validBlocks = new ArrayList<Material>();

    public DiscoverWarpsCommands(DiscoverWarps plugin) {
        this.plugin = plugin;
        this.admincmds = new ArrayList<String>();
        admincmds.add("set");
        admincmds.add("delete");
        admincmds.add("enable");
        admincmds.add("disable");
        admincmds.add("auto");
        admincmds.add("cost");
        admincmds.add("sign");
        admincmds.add("allow_buying");
        admincmds.add("xp_on_discover");
        this.usercmds = new ArrayList<String>();
        usercmds.add("tp");
        usercmds.add("list");
        usercmds.add("buy");
        plugin_name = ChatColor.GOLD + "[" + this.plugin.getConfig().getString("localisation.plugin_name") + "] " + ChatColor.RESET;
        validBlocks.add(Material.WOOD_PLATE);
        validBlocks.add(Material.STONE_PLATE);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("discoverwarps")) {
            if (args.length == 0) {
                String HELP
                        = plugin.getConfig().getString("localisation.help.set") + ":\n" + ChatColor.GREEN + "/dw set [name]" + ChatColor.RESET
                        + "\n" + plugin.getConfig().getString("localisation.help.delete") + ":\n" + ChatColor.GREEN + "/dw delete [name]" + ChatColor.RESET
                        + "\n" + plugin.getConfig().getString("localisation.help.disable") + ":\n" + ChatColor.GREEN + "/dw disable [name]" + ChatColor.RESET
                        + "\n" + plugin.getConfig().getString("localisation.help.enable") + ":\n" + ChatColor.GREEN + "/dw enable [name]" + ChatColor.RESET
                        + "\n" + plugin.getConfig().getString("localisation.help.auto") + ":\n" + ChatColor.GREEN + "/dw auto [name]" + ChatColor.RESET
                        + "\n" + plugin.getConfig().getString("localisation.help.cost") + ":\n" + ChatColor.GREEN + "/dw cost [name] [amount]" + ChatColor.RESET
                        + "\n" + plugin.getConfig().getString("localisation.help.list") + ":\n" + ChatColor.GREEN + "/dw list" + ChatColor.RESET
                        + "\n" + plugin.getConfig().getString("localisation.help.warp") + ":\n" + ChatColor.GREEN + "/dw tp [name]" + ChatColor.RESET
                        + "\n" + plugin.getConfig().getString("localisation.help.buy") + ":\n" + ChatColor.GREEN + "/dw buy [name]" + ChatColor.RESET
                        + "\n" + plugin.getConfig().getString("localisation.help.config") + ":\n" + ChatColor.GREEN + "/dw [config setting name]" + ChatColor.RESET + " e.g. /dw allow_buying";
                sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.help"));
                sender.sendMessage("------------");
                sender.sendMessage(HELP.split("\n"));
                return true;
            }
            if (admincmds.contains(args[0])) {
                if (!sender.hasPermission("discoverwarps.admin")) {
                    sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.permission"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("allow_buying")) {
                    boolean bool = !plugin.getConfig().getBoolean("allow_buying");
                    plugin.getConfig().set("allow_buying", bool);
                    String str_bool = (bool) ? plugin.getConfig().getString("localisation.commands.str_true") : plugin.getConfig().getString("localisation.commands.str_false");
                    sender.sendMessage(plugin_name + "allow_buying " + String.format(plugin.getConfig().getString("localisation.config"), str_bool));
                    if (bool) {
                        sender.sendMessage(plugin_name + String.format(plugin.getConfig().getString("localisation.commands.restart"), plugin_name));
                    }
                    plugin.saveConfig();
                    return true;
                }
                if (args[0].equalsIgnoreCase("xp_on_discover")) {
                    boolean bool = !plugin.getConfig().getBoolean("xp_on_discover");
                    plugin.getConfig().set("xp_on_discover", bool);
                    String str_bool = (bool) ? plugin.getConfig().getString("localisation.commands.str_true") : plugin.getConfig().getString("localisation.commands.str_false");
                    sender.sendMessage(plugin_name + "xp_on_discover " + String.format(plugin.getConfig().getString("localisation.config"), str_bool));
                    plugin.saveConfig();
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.arguments"));
                    return false;
                }
                if (args[0].equalsIgnoreCase("sign")) {
                    plugin.getConfig().set("sign", args[1]);
                    sender.sendMessage(plugin_name + "sign " + String.format(plugin.getConfig().getString("localisation.config"), args[1]));
                    plugin.saveConfig();
                    return true;
                }
                if (args[0].equalsIgnoreCase("set")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        Location l = p.getLocation();
                        //l.setY(l.getY() - .2);
                        Block b = l.getBlock();
                        // check player is standing on pressure plate
                        Material m = b.getType();
                        if (!validBlocks.contains(m)) {
                            sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.not_plate"));
                            return true;
                        }
                        try {
                            Connection connection = service.getConnection();
                            Statement statement = connection.createStatement();
                            String queryName = "SELECT name FROM discoverwarps WHERE name = '" + args[1] + "'";
                            ResultSet rsName = statement.executeQuery(queryName);
                            // check name is not in use
                            if (rsName.isBeforeFirst()) {
                                sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.name_in_use"));
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
                            sender.sendMessage(plugin_name + String.format(plugin.getConfig().getString("localisation.commands.added"), args[1]));
                        } catch (SQLException e) {
                            plugin.debug("Could not insert new discover plate, " + e);
                        }
                    } else {
                        sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.only_player"));
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
                            sender.sendMessage(plugin_name + String.format(plugin.getConfig().getString("localisation.commands.deleted"), args[1]));
                            return true;
                        } else {
                            sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.no_plate_name"));
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
                            sender.sendMessage(plugin_name + String.format(plugin.getConfig().getString("localisation.commands.enabled"), args[1]));
                            return true;
                        } else {
                            sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not enable discover plate, " + e);
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
                            sender.sendMessage(plugin_name + String.format(plugin.getConfig().getString("localisation.commands.disabled"), args[1]));
                            return true;
                        } else {
                            sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not disable discover plate, " + e);
                    }
                }
                if (args[0].equalsIgnoreCase("auto")) {
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        String queryAuto = "SELECT name, auto FROM discoverwarps WHERE name = '" + args[1] + "'";
                        ResultSet rsAuto = statement.executeQuery(queryAuto);
                        // check name is valid
                        if (rsAuto.next()) {
                            int auto = (rsAuto.getInt("auto") == 1) ? 0 : 1;
                            String bool = (auto == 1) ? plugin.getConfig().getString("localisation.commands.str_true") : plugin.getConfig().getString("localisation.commands.str_false");
                            String queryDel = "UPDATE discoverwarps SET auto = " + auto + " WHERE name = '" + args[1] + "'";
                            statement.executeUpdate(queryDel);
                            sender.sendMessage(plugin_name + String.format(plugin.getConfig().getString("localisation.commands.auto_discover"), args[1]) + " " + bool + "!");
                            return true;
                        } else {
                            sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not set auto discover plate option, " + e);
                    }
                }
                if (args[0].equalsIgnoreCase("cost")) {
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        String queryCost = "SELECT name FROM discoverwarps WHERE name = '" + args[1] + "'";
                        ResultSet rsCost = statement.executeQuery(queryCost);
                        // check name is valid
                        if (rsCost.isBeforeFirst()) {
                            int cost;
                            try {
                                cost = Integer.parseInt(args[2]);
                            } catch (NumberFormatException nfe) {
                                sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.cost"));
                                return true;
                            }
                            String queryDel = "UPDATE discoverwarps SET cost = " + cost + " WHERE name = '" + args[1] + "'";
                            statement.executeUpdate(queryDel);
                            sender.sendMessage(plugin_name + "DiscoverPlate " + args[1] + " now costs " + cost + " to buy!");
                            return true;
                        } else {
                            sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not set discover plate cost, " + e);
                    }
                }
            }
            if (usercmds.contains(args[0])) {
                if (!sender.hasPermission("discoverwarps.use")) {
                    sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.permission"));
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
                        String queryList = "SELECT id, name, auto, cost FROM discoverwarps WHERE enabled = 1";
                        ResultSet rsList = statement.executeQuery(queryList);
                        // check name is valid
                        if (rsList.isBeforeFirst()) {
                            sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.list"));
                            sender.sendMessage("------------");
                            int i = 1;
                            String discovered;
                            while (rsList.next()) {
                                discovered = (visited.contains(rsList.getString("id"))) ? ChatColor.GREEN + plugin.getConfig().getString("localisation.visited") : ChatColor.RED + plugin.getConfig().getString("localisation.not_visited");
                                String status = (rsList.getBoolean("auto")) ? ChatColor.BLUE + plugin.getConfig().getString("localisation.auto") : discovered;
                                String warp = rsList.getString("name");
                                String cost = "";
                                if (plugin.getConfig().getBoolean("allow_buying") && !visited.contains(rsList.getString("id"))) {
                                    int amount = rsList.getInt("cost");
                                    if (amount > 0) {
                                        cost = ChatColor.RESET + " [" + plugin.economy.format(amount) + "]";
                                    }
                                }
                                sender.sendMessage(i + ". " + warp + " " + status + cost);
                                i++;
                            }
                            sender.sendMessage("------------");
                            return true;
                        } else {
                            sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.none_set"));
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not list discover plates, " + e);
                    }
                }
                if (args[0].equalsIgnoreCase("tp")) {
                    Player player;
                    if (sender instanceof Player) {
                        player = (Player) sender;
                    } else {
                        sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.only_player"));
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.no_warp_name"));
                        return false;
                    }
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        String queryName = "SELECT * FROM discoverwarps WHERE name = '" + args[1] + "' COLLATE NOCASE";
                        ResultSet rsName = statement.executeQuery(queryName);
                        // check name is valid
                        if (rsName.next()) {
                            String id = rsName.getString("id");
                            String warp = rsName.getString("name");
                            World w = plugin.getServer().getWorld(rsName.getString("world"));
                            int x = rsName.getInt("x");
                            int y = rsName.getInt("y");
                            int z = rsName.getInt("z");
                            boolean auto = rsName.getBoolean("auto");
                            List<String> visited = new ArrayList<String>();
                            // can the player tp to here?
                            String p = player.getName();
                            // get players visited plates
                            String queryVisited = "SELECT visited FROM players WHERE player = '" + p + "'";
                            ResultSet rsVisited = statement.executeQuery(queryVisited);
                            if (rsVisited.isBeforeFirst()) {
                                visited = Arrays.asList(rsVisited.getString("visited").split(","));
                            }
                            if (!visited.contains(id) && !auto) {
                                sender.sendMessage(plugin_name + String.format(plugin.getConfig().getString("localisation.commands.needs_discover"), warp));
                                return true;
                            }
                            World from = player.getLocation().getWorld();
                            Location l = new Location(w, x, y, z);
                            l.setPitch(player.getLocation().getPitch());
                            l.setYaw(player.getLocation().getYaw());
                            movePlayer(player, l, from);
                            return true;
                        } else {
                            sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not find discover plate record, " + e);
                    }
                }
                if (args[0].equalsIgnoreCase("buy")) {
                    if (!plugin.getConfig().getBoolean("allow_buying")) {
                        sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.buying.no_buying"));
                        return true;
                    }
                    Player player;
                    if (sender instanceof Player) {
                        player = (Player) sender;
                    } else {
                        sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.only_player"));
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.no_warp_name"));
                        return false;
                    }
                    try {
                        Connection connection = service.getConnection();
                        Statement statement = connection.createStatement();
                        String queryBuy = "SELECT * FROM discoverwarps WHERE name = '" + args[1] + "'";
                        ResultSet rsBuy = statement.executeQuery(queryBuy);
                        // check name is valid
                        if (rsBuy.next()) {
                            boolean firstplate = true;
                            double cost = rsBuy.getDouble("cost");
                            if (cost <= 0) {
                                sender.sendMessage(plugin_name + String.format(plugin.getConfig().getString("localisation.buying.cannot_buy"), args[1]));
                                return true;
                            }
                            String p = player.getName();
                            // check they have sufficient balance
                            double bal = plugin.economy.getBalance(p);
                            if (cost > bal) {
                                player.sendMessage(plugin_name + plugin.getConfig().getString("localisation.buying.no_money"));
                                return true;
                            }
                            String id = rsBuy.getString("id");
                            String queryDiscover = "";
                            // check whether they have visited this plate before
                            String queryPlayer = "SELECT * FROM players WHERE player = '" + p + "'";
                            ResultSet rsPlayer = statement.executeQuery(queryPlayer);
                            if (rsPlayer.next()) {
                                firstplate = false;
                                String data = rsPlayer.getString("visited");
                                String[] visited = data.split(",");
                                if (Arrays.asList(visited).contains(id)) {
                                    sender.sendMessage(plugin_name + String.format(plugin.getConfig().getString("localisation.buying.no_need"), args[1]));
                                    return true;
                                }
                                queryDiscover = "UPDATE players SET visited = '" + data + "," + id + "' WHERE player = '" + p + "'";
                            }
                            if (firstplate == true) {
                                queryDiscover = "INSERT INTO players (player, visited) VALUES ('" + p + "','" + id + "')";
                            }
                            statement.executeUpdate(queryDiscover);
                            plugin.economy.withdrawPlayer(p, cost);
                            player.sendMessage(plugin_name + String.format(plugin.getConfig().getString("localisation.buying.bought"), args[1]) + " " + cost);
                            return true;
                        } else {
                            sender.sendMessage(plugin_name + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                            return true;
                        }
                    } catch (SQLException e) {
                        plugin.debug("Could not buy discover plate, " + e);
                    }
                }
            }
        }
        return false;
    }

    public void movePlayer(Player p, Location l, World from) {

        p.sendMessage(plugin_name + plugin.getConfig().getString("localisation.teleport") + "...");

        final Player thePlayer = p;
        final Location theLocation = l;
        final World to = theLocation.getWorld();
        final boolean allowFlight = thePlayer.getAllowFlight();
        final boolean crossWorlds = (from != to);
        final boolean isSurvival = checkSurvival(to);

        // adjust location to centre of plate
        theLocation.setX(l.getX() + 0.5);
        theLocation.setZ(l.getZ() + 0.5);

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
                if (plugin.getConfig().getBoolean("no_damage")) {
                    thePlayer.setNoDamageTicks(plugin.getConfig().getInt("no_damage_time") * 20);
                }
                if (thePlayer.getGameMode() == GameMode.CREATIVE || (allowFlight && crossWorlds && !isSurvival)) {
                    thePlayer.setAllowFlight(true);
                }
            }
        }, 10L);
    }

    /**
     * Checks if the world the player is teleporting to is a SURVIVAL world.
     *
     * @param w the world to check
     * @return true if the world is a SURVIVAL world, otherwise false
     */
    private boolean checkSurvival(World w) {
        boolean bool = false;
        if (plugin.pm.isPluginEnabled("Multiverse-Core")) {
            MultiverseCore mv = (MultiverseCore) plugin.pm.getPlugin("Multiverse-Core");
            MultiverseWorld mvw = mv.getCore().getMVWorldManager().getMVWorld(w);
            GameMode gm = mvw.getGameMode();
            if (gm.equals(GameMode.SURVIVAL)) {
                bool = true;
            }
        }
        if (plugin.pm.isPluginEnabled("MultiWorld")) {
            MultiWorldAPI mw = ((MultiWorldPlugin) plugin.pm.getPlugin("MultiWorld")).getApi();
            MultiWorldWorldData mww = mw.getWorld(w.getName());
            if (!mww.isOptionSet(FlagName.CREATIVEWORLD)) {
                bool = true;
            }
        }
        return bool;
    }

}
