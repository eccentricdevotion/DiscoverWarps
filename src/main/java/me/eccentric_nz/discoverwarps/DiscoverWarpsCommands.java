package me.eccentric_nz.discoverwarps;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscoverWarpsCommands implements CommandExecutor {

    private final DiscoverWarps plugin;
    List<String> admincmds;
    List<String> usercmds;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();

    public DiscoverWarpsCommands(DiscoverWarps plugin) {
        this.plugin = plugin;
        admincmds = new ArrayList<>();
        admincmds.add("set");
        admincmds.add("delete");
        admincmds.add("enable");
        admincmds.add("disable");
        admincmds.add("auto");
        admincmds.add("cost");
        admincmds.add("clear");
        admincmds.add("sign");
        admincmds.add("allow_buying");
        admincmds.add("xp_on_discover");
        admincmds.add("rename");
        admincmds.add("icon");
        usercmds = new ArrayList<>();
        usercmds.add("tp");
        usercmds.add("undiscover");
        usercmds.add("list");
        usercmds.add("buy");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("discoverwarps")) {
            if (args.length == 0) {
                String HELP = plugin.getConfig().getString("localisation.help.set") + ":\n"
                        + ChatColor.GREEN + "/dw set [name]" + ChatColor.RESET + "\n"
                        + plugin.getConfig().getString("localisation.help.delete") + ":\n"
                        + ChatColor.GREEN + "/dw delete [name]" + ChatColor.RESET + "\n"
                        + plugin.getConfig().getString("localisation.help.disable") + ":\n"
                        + ChatColor.GREEN + "/dw disable [name]" + ChatColor.RESET + "\n"
                        + plugin.getConfig().getString("localisation.help.enable") + ":\n"
                        + ChatColor.GREEN + "/dw enable [name]" + ChatColor.RESET + "\n"
                        + plugin.getConfig().getString("localisation.help.auto") + ":\n"
                        + ChatColor.GREEN + "/dw auto [name]" + ChatColor.RESET + "\n"
                        + plugin.getConfig().getString("localisation.help.cost") + ":\n"
                        + ChatColor.GREEN + "/dw cost [name] [amount]" + ChatColor.RESET + "\n"
                        + plugin.getConfig().getString("localisation.help.rename") + ":\n"
                        + ChatColor.GREEN + "/dw rename [old_name] [new_name]" + ChatColor.RESET + "\n"
                        + plugin.getConfig().getString("localisation.help.list") + ":\n"
                        + ChatColor.GREEN + "/dw list" + ChatColor.RESET + "\n"
                        + plugin.getConfig().getString("localisation.help.warp") + ":\n"
                        + ChatColor.GREEN + "/dw tp [name]" + ChatColor.RESET + "\n"
                        + plugin.getConfig().getString("localisation.help.buy") + ":\n"
                        + ChatColor.GREEN + "/dw buy [name]" + ChatColor.RESET + "\n"
                        + plugin.getConfig().getString("localisation.help.undiscover") + ":\n"
                        + ChatColor.GREEN + "/dw undiscover [name]" + ChatColor.RESET + "\n"
                        + plugin.getConfig().getString("localisation.help.clear") + ":\n"
                        + ChatColor.GREEN + "/dw clear [player|all]" + ChatColor.RESET + "\n"
                        + plugin.getConfig().getString("localisation.help.config") + ":\n"
                        + ChatColor.GREEN + "/dw [config setting name]" + ChatColor.RESET + " e.g. /dw allow_buying";
                sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.help"));
                sender.sendMessage("------------");
                sender.sendMessage(HELP.split("\n"));
                return true;
            }
            if (admincmds.contains(args[0])) {
                if (!sender.hasPermission("discoverwarps.admin")) {
                    sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.permission"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("allow_buying")) {
                    boolean bool = !plugin.getConfig().getBoolean("allow_buying");
                    plugin.getConfig().set("allow_buying", bool);
                    String str_bool = (bool) ? plugin.getConfig().getString("localisation.commands.str_true") : plugin.getConfig().getString("localisation.commands.str_false");
                    sender.sendMessage(plugin.getLocalisedName() + "allow_buying " + String.format(plugin.getConfig().getString("localisation.config"), str_bool));
                    if (bool) {
                        sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.commands.restart"), plugin.getLocalisedName()));
                    }
                    plugin.saveConfig();
                    return true;
                }
                if (args[0].equalsIgnoreCase("xp_on_discover")) {
                    boolean bool = !plugin.getConfig().getBoolean("xp_on_discover");
                    plugin.getConfig().set("xp_on_discover", bool);
                    String str_bool = (bool) ? plugin.getConfig().getString("localisation.commands.str_true") : plugin.getConfig().getString("localisation.commands.str_false");
                    sender.sendMessage(plugin.getLocalisedName() + "xp_on_discover " + String.format(plugin.getConfig().getString("localisation.config"), str_bool));
                    plugin.saveConfig();
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.arguments"));
                    return false;
                }
                if (args[0].equalsIgnoreCase("sign")) {
                    plugin.getConfig().set("sign", args[1]);
                    sender.sendMessage(plugin.getLocalisedName() + "sign " + String.format(plugin.getConfig().getString("localisation.config"), args[1]));
                    plugin.saveConfig();
                    return true;
                }
                if (args[0].equalsIgnoreCase("set")) {
                    if (sender instanceof Player p) {
                        Location l = p.getLocation();
                        //l.setY(l.getY() - .2);
                        Block b = l.getBlock();
                        // check player is standing on pressure plate
                        Material m = b.getType();
                        if (!Tag.PRESSURE_PLATES.isTagged(m)) {
                            sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.not_plate"));
                            return true;
                        }
                        String region_name = "";
                        // check if the plate is inside a WorldGuard region
                        if (plugin.getConfig().getBoolean("worldguard_regions") && plugin.pm.isPluginEnabled("WorldGuard")) {
                            region_name = DiscoverWarpsWorldGuardUtilities.isPlateInRegion(plugin, l, sender);
                        }
                        Statement statement = null;
                        ResultSet rsName = null;
                        try {
                            Connection connection = service.getConnection();
                            statement = connection.createStatement();
                            String queryName = "SELECT name FROM discoverwarps WHERE name = '" + args[1] + "'";
                            rsName = statement.executeQuery(queryName);
                            // check name is not in use
                            if (rsName.isBeforeFirst()) {
                                sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.name_in_use"));
                                return true;
                            }
                            String w = b.getLocation().getWorld().getName();
                            int x = b.getLocation().getBlockX();
                            int y = b.getLocation().getBlockY();
                            int z = b.getLocation().getBlockZ();
                            PreparedStatement ps = connection.prepareStatement("INSERT INTO discoverwarps (name, world, x, y, z, enabled, region) VALUES (?, ?, ?, ?, ?, 1, ?)");
                            ps.setString(1, args[1]);
                            ps.setString(2, w);
                            ps.setInt(3, x);
                            ps.setInt(4, y);
                            ps.setInt(5, z);
                            ps.setString(6, region_name);
                            ps.executeUpdate();
                            sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.commands.added"), args[1]));
                        } catch (SQLException e) {
                            plugin.debug("Could not insert new discover plate, " + e.getMessage());
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
                    } else {
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.only_player"));
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("delete")) {
                    Statement statement = null;
                    ResultSet rsName = null;
                    try {
                        Connection connection = service.getConnection();
                        statement = connection.createStatement();
                        String queryName = "SELECT * FROM discoverwarps WHERE name = '" + args[1] + "'";
                        rsName = statement.executeQuery(queryName);
                        // check name is valid
                        if (rsName.next()) {
                            World w = plugin.getServer().getWorld(rsName.getString("world"));
                            int x = rsName.getInt("x");
                            int y = rsName.getInt("y");
                            int z = rsName.getInt("z");
                            String queryDel = "DELETE FROM discoverwarps WHERE name = '" + args[1] + "'";
                            statement.executeUpdate(queryDel);
                            Block b = w.getBlockAt(x, y, z);
                            b.setType(Material.AIR);
                            sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.commands.deleted"), args[1]));
                        } else {
                            sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                        }
                        return true;
                    } catch (SQLException e) {
                        plugin.debug("Could not delete discover plate, " + e.getMessage());
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
                if (args[0].equalsIgnoreCase("enable")) {
                    Statement statement = null;
                    ResultSet rsName = null;
                    try {
                        Connection connection = service.getConnection();
                        statement = connection.createStatement();
                        String queryName = "SELECT name FROM discoverwarps WHERE name = '" + args[1] + "'";
                        rsName = statement.executeQuery(queryName);
                        // check name is valid
                        if (rsName.isBeforeFirst()) {
                            String queryDel = "UPDATE discoverwarps SET enabled = 1 WHERE name = '" + args[1] + "'";
                            statement.executeUpdate(queryDel);
                            sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.commands.enabled"), args[1]));
                        } else {
                            sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                        }
                        return true;
                    } catch (SQLException e) {
                        plugin.debug("Could not enable discover plate, " + e.getMessage());
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
                if (args[0].equalsIgnoreCase("disable")) {
                    Statement statement = null;
                    ResultSet rsName = null;
                    try {
                        Connection connection = service.getConnection();
                        statement = connection.createStatement();
                        String queryName = "SELECT name FROM discoverwarps WHERE name = '" + args[1] + "'";
                        rsName = statement.executeQuery(queryName);
                        // check name is valid
                        if (rsName.isBeforeFirst()) {
                            String queryDel = "UPDATE discoverwarps SET enabled = 0 WHERE name = '" + args[1] + "'";
                            statement.executeUpdate(queryDel);
                            sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.commands.disabled"), args[1]));
                        } else {
                            sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                        }
                        return true;
                    } catch (SQLException e) {
                        plugin.debug("Could not disable discover plate, " + e.getMessage());
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
                if (args[0].equalsIgnoreCase("auto")) {
                    Statement statement = null;
                    ResultSet rsAuto = null;
                    try {
                        Connection connection = service.getConnection();
                        statement = connection.createStatement();
                        String queryAuto = "SELECT name, auto FROM discoverwarps WHERE name = '" + args[1] + "'";
                        rsAuto = statement.executeQuery(queryAuto);
                        // check name is valid
                        if (rsAuto.next()) {
                            int auto = (rsAuto.getInt("auto") == 1) ? 0 : 1;
                            String bool = (auto == 1) ? plugin.getConfig().getString("localisation.commands.str_true") : plugin.getConfig().getString("localisation.commands.str_false");
                            String queryDel = "UPDATE discoverwarps SET auto = " + auto + " WHERE name = '" + args[1] + "'";
                            statement.executeUpdate(queryDel);
                            sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.commands.auto_discover"), args[1]) + " " + bool + "!");
                        } else {
                            sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                        }
                        return true;
                    } catch (SQLException e) {
                        plugin.debug("Could not set auto discover plate option, " + e.getMessage());
                    } finally {
                        if (rsAuto != null) {
                            try {
                                rsAuto.close();
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
                if (args[0].equalsIgnoreCase("cost")) {
                    Statement statement = null;
                    ResultSet rsCost = null;
                    try {
                        Connection connection = service.getConnection();
                        statement = connection.createStatement();
                        String queryCost = "SELECT name FROM discoverwarps WHERE name = '" + args[1] + "'";
                        rsCost = statement.executeQuery(queryCost);
                        // check name is valid
                        if (rsCost.isBeforeFirst()) {
                            int cost;
                            try {
                                cost = Integer.parseInt(args[2]);
                            } catch (NumberFormatException nfe) {
                                sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.cost"));
                                return true;
                            }
                            String queryDel = "UPDATE discoverwarps SET cost = " + cost + " WHERE name = '" + args[1] + "'";
                            statement.executeUpdate(queryDel);
                            sender.sendMessage(plugin.getLocalisedName() + "DiscoverPlate " + args[1] + " now costs " + cost + " to buy!");
                        } else {
                            sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                        }
                        return true;
                    } catch (SQLException e) {
                        plugin.debug("Could not set discover plate cost, " + e.getMessage());
                    } finally {
                        if (rsCost != null) {
                            try {
                                rsCost.close();
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
                if (args[0].equalsIgnoreCase("delete")) {
                    Statement statement = null;
                    ResultSet rsName = null;
                    try {
                        Connection connection = service.getConnection();
                        statement = connection.createStatement();
                        String queryName = "SELECT * FROM discoverwarps WHERE name = '" + args[1] + "'";
                        rsName = statement.executeQuery(queryName);
                        // check name is valid
                        if (rsName.next()) {
                            World w = plugin.getServer().getWorld(rsName.getString("world"));
                            int x = rsName.getInt("x");
                            int y = rsName.getInt("y");
                            int z = rsName.getInt("z");
                            String queryDel = "DELETE FROM discoverwarps WHERE name = '" + args[1] + "'";
                            statement.executeUpdate(queryDel);
                            Block b = w.getBlockAt(x, y, z);
                            b.setType(Material.AIR);
                            sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.commands.deleted"), args[1]));
                        } else {
                            sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                        }
                        return true;
                    } catch (SQLException e) {
                        plugin.debug("Could not delete discover plate, " + e.getMessage());
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
                if (args[0].equalsIgnoreCase("rename")) {
                    if (args.length < 3) {
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.arguments"));
                        return true;
                    }
                    Statement statement = null;
                    PreparedStatement ps = null;
                    ResultSet rsName = null;
                    try {
                        Connection connection = service.getConnection();
                        statement = connection.createStatement();
                        String queryName = "SELECT * FROM discoverwarps WHERE name = '" + args[1] + "'";
                        rsName = statement.executeQuery(queryName);
                        // check name is valid
                        if (rsName.next()) {
                            String queryRename = "UPDATE discoverwarps SET name = ? WHERE id = ?";
                            ps = connection.prepareStatement(queryRename);
                            ps.setString(1, args[2]);
                            ps.setInt(2, rsName.getInt("id"));
                            ps.executeUpdate();
                            sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.commands.renamed"), args[1], args[2]));
                        } else {
                            sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                        }
                        return true;
                    } catch (SQLException e) {
                        plugin.debug("Could not rename discover plate, " + e.getMessage());
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
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (SQLException ex) {
                            }
                        }
                    }
                }
                if (args[0].equalsIgnoreCase("icon")) {
                    if (args.length < 3) {
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.arguments"));
                        return true;
                    }
                    // check material name
                    Material material;
                    try {
                        material = Material.valueOf(args[2].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_material"));
                        return true;
                    }
                    Statement statement = null;
                    PreparedStatement ps = null;
                    ResultSet rsName = null;
                    try {
                        Connection connection = service.getConnection();
                        statement = connection.createStatement();
                        String queryName = "SELECT * FROM discoverwarps WHERE name = '" + args[1] + "'";
                        rsName = statement.executeQuery(queryName);
                        // check name is valid
                        if (rsName.next()) {
                            String queryRename = "UPDATE discoverwarps SET icon = ? WHERE id = ?";
                            ps = connection.prepareStatement(queryRename);
                            ps.setString(1, material.toString());
                            ps.setInt(2, rsName.getInt("id"));
                            ps.executeUpdate();
                            sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.icon"));
                        } else {
                            sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                        }
                        return true;
                    } catch (SQLException e) {
                        plugin.debug("Could not rename discover plate, " + e.getMessage());
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
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (SQLException ex) {
                            }
                        }
                    }
                }
                if (args[0].equalsIgnoreCase("clear")) {
                    if (args.length < 2) {
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.arguments"));
                        return false;
                    }
                    String clear = "DELETE from players";
                    if (!args[1].equalsIgnoreCase("all")) {
                        // get player uuid
                        Player player = plugin.getServer().getPlayer(args[1]);
                        if (player != null) {
                            clear += " WHERE uuid = '" + player.getUniqueId() + "'";
                        } else {
                            sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_player"));
                            return true;
                        }
                    }
                    Statement statement = null;
                    try {
                        Connection connection = service.getConnection();
                        statement = connection.createStatement();
                        statement.executeUpdate(clear);
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.cleared"));
                        return true;
                    } catch (SQLException e) {
                        plugin.debug("Could not clear discover plate, " + e.getMessage());
                    } finally {
                        if (statement != null) {
                            try {
                                statement.close();
                            } catch (SQLException ex) {
                            }
                        }
                    }
                    return true;
                }
            }
        }
        if (usercmds.contains(args[0])) {
            if (!sender.hasPermission("discoverwarps.use")) {
                sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.permission"));
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                Statement statement = null;
                ResultSet rsList = null;
                try {
                    Connection connection = service.getConnection();
                    statement = connection.createStatement();
                    List<String> visited = new ArrayList<>();
                    if (sender instanceof Player player) {
                        String uuid = player.getUniqueId().toString();
                        // get players visited plates
                        String queryVisited = "SELECT visited FROM players WHERE uuid = '" + uuid + "'";
                        ResultSet rsVisited = statement.executeQuery(queryVisited);
                        if (rsVisited.isBeforeFirst()) {
                            visited = Arrays.asList(rsVisited.getString("visited").split(","));
                        }
                    }
                    String queryList = "SELECT id, name, auto, cost FROM discoverwarps WHERE enabled = 1";
                    rsList = statement.executeQuery(queryList);
                    // check name is valid
                    if (rsList.isBeforeFirst()) {
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.list"));
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
                    } else {
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.none_set"));
                    }
                    return true;
                } catch (SQLException e) {
                    plugin.debug("Could not list discover plates, " + e);
                } finally {
                    if (rsList != null) {
                        try {
                            rsList.close();
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
            if (args[0].equalsIgnoreCase("tp")) {
                Player player;
                boolean must_discover = true;
                if (sender instanceof Player) {
                    player = (Player) sender;
                } else {
                    // tp specified player to specified warp
                    if (args.length < 3) {
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.arguments"));
                        return true;
                    }
                    // check the player
                    player = plugin.getServer().getPlayer(args[2]);
                    if (player == null || !player.isOnline()) {
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_player"));
                        return true;
                    }
                    if (args.length == 4 && args[3].equalsIgnoreCase("true")) {
                        must_discover = false;
                    }
                }
                long cd = plugin.getConfig().getLong("cooldown") * 1000;
                if (cd > 0) {
                    if (plugin.getDiscoverWarpCooldowns().containsKey(player.getUniqueId())) {
                        long expire = plugin.getDiscoverWarpCooldowns().get(player.getUniqueId()) + cd;
                        long now = System.currentTimeMillis();
                        if (expire > now) {
                            sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.commands.no_warp_cooldown"), (expire - now) / 1000));
                            return true;
                        }
                    }
                }
                if (args.length == 1) {
                    // open GUI
                    ItemStack[] warps = new DiscoverWarpsGUIInventory(plugin, player.getUniqueId()).getWarps();
                    Inventory gui = plugin.getServer().createInventory(player, 54, ChatColor.RED + plugin.getConfig().getString("localisation.plugin_name"));
                    gui.setContents(warps);
                    player.openInventory(gui);
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_warp_name"));
                    return false;
                }
                Statement statement = null;
                ResultSet rsName = null;
                try {
                    Connection connection = service.getConnection();
                    statement = connection.createStatement();
                    String queryName = "SELECT * FROM discoverwarps WHERE name = '" + args[1] + "' COLLATE NOCASE";
                    rsName = statement.executeQuery(queryName);
                    // check name is valid
                    if (rsName.next()) {
                        String id = rsName.getString("id");
                        String warp = rsName.getString("name");
                        World w = plugin.getServer().getWorld(rsName.getString("world"));
                        int x = rsName.getInt("x");
                        int y = rsName.getInt("y");
                        int z = rsName.getInt("z");
                        boolean auto = rsName.getBoolean("auto");
                        if (must_discover) {
                            List<String> visited = new ArrayList<>();
                            // can the player tp to here?
                            String uuid = player.getUniqueId().toString();
                            // get players visited plates
                            String queryVisited = "SELECT visited FROM players WHERE uuid = '" + uuid + "'";
                            ResultSet rsVisited = statement.executeQuery(queryVisited);
                            if (rsVisited.isBeforeFirst()) {
                                visited = Arrays.asList(rsVisited.getString("visited").split(","));
                            }
                            if (!visited.contains(id) && !auto) {
                                sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.commands.needs_discover"), warp));
                                return true;
                            }
                        }
                        World from = player.getLocation().getWorld();
                        Location l = new Location(w, x, y, z);
                        l.setPitch(player.getLocation().getPitch());
                        l.setYaw(player.getLocation().getYaw());
                        if (cd > 0) {
                            plugin.getDiscoverWarpCooldowns().put(player.getUniqueId(), System.currentTimeMillis());
                        }
                        new DiscoverWarpsMover(plugin).movePlayer(player, l, from);
                    } else {
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                    }
                    return true;
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
            if (args[0].equalsIgnoreCase("buy")) {
                if (!plugin.getConfig().getBoolean("allow_buying")) {
                    sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.buying.no_buying"));
                    return true;
                }
                Player player;
                if (sender instanceof Player) {
                    player = (Player) sender;
                } else {
                    sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.only_player"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_warp_name"));
                    return false;
                }
                Statement statement = null;
                ResultSet rsBuy = null;
                try {
                    Connection connection = service.getConnection();
                    statement = connection.createStatement();
                    String queryBuy = "SELECT * FROM discoverwarps WHERE name = '" + args[1] + "'";
                    rsBuy = statement.executeQuery(queryBuy);
                    // check name is valid
                    if (rsBuy.next()) {
                        boolean firstplate = true;
                        double cost = rsBuy.getDouble("cost");
                        if (cost <= 0) {
                            sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.buying.cannot_buy"), args[1]));
                            return true;
                        }
                        String uuid = player.getUniqueId().toString();
                        // check they have sufficient balance
                        double bal = plugin.economy.getBalance(player);
                        if (cost > bal) {
                            player.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.buying.no_money"));
                            return true;
                        }
                        String id = rsBuy.getString("id");
                        String queryDiscover = "";
                        // check whether they have visited this plate before
                        String queryPlayer = "SELECT * FROM players WHERE uuid = '" + uuid + "'";
                        ResultSet rsPlayer = statement.executeQuery(queryPlayer);
                        if (rsPlayer.next()) {
                            firstplate = false;
                            String data = rsPlayer.getString("visited");
                            String[] visited = data.split(",");
                            if (Arrays.asList(visited).contains(id)) {
                                sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.buying.no_need"), args[1]));
                                return true;
                            }
                            queryDiscover = "UPDATE players SET visited = '" + data + "," + id + "' WHERE uuid = '" + uuid + "'";
                        }
                        if (firstplate) {
                            queryDiscover = "INSERT INTO players (uuid, visited) VALUES ('" + uuid + "','" + id + "')";
                        }
                        statement.executeUpdate(queryDiscover);
                        plugin.economy.withdrawPlayer(player, cost);
                        player.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.buying.bought"), args[1]) + " " + cost);
                    } else {
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                    }
                    return true;
                } catch (SQLException e) {
                    plugin.debug("Could not buy discover plate, " + e);
                } finally {
                    if (rsBuy != null) {
                        try {
                            rsBuy.close();
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
            if (args[0].equalsIgnoreCase("undiscover")) {
                Player player;
                if (sender instanceof Player) {
                    player = (Player) sender;
                } else {
                    sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.only_player"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_warp_name"));
                    return false;
                }
                Statement statement = null;
                ResultSet rsForget = null;
                try {
                    Connection connection = service.getConnection();
                    statement = connection.createStatement();
                    String queryForget = "SELECT * FROM discoverwarps WHERE name = '" + args[1] + "'";
                    rsForget = statement.executeQuery(queryForget);
                    // check name is valid
                    if (rsForget.next()) {
                        String uuid = player.getUniqueId().toString();
                        String id = rsForget.getString("id");
                        // check whether they have visited this plate before
                        String queryPlayer = "SELECT * FROM players WHERE uuid = '" + uuid + "'";
                        ResultSet rsPlayer = statement.executeQuery(queryPlayer);
                        if (rsPlayer.next()) {
                            String data = rsPlayer.getString("visited");
                            List<String> visited = Arrays.asList(data.split(","));
                            StringBuilder forgotten = new StringBuilder("");
                            String queryUpdate;
                            if (visited.contains(id)) {
                                if (visited.size() > 1) {
                                    // forget
                                    for (String f : visited) {
                                        if (!f.equalsIgnoreCase(id)) {
                                            forgotten.append(f).append(",");
                                        }
                                    }
                                    queryUpdate = "UPDATE players SET visited = '" + forgotten.substring(0, forgotten.length() - 1) + "' WHERE uuid = '" + uuid + "'";
                                } else {
                                    queryUpdate = "UPDATE players SET visited = '' WHERE uuid = '" + uuid + "'";
                                }
                                statement.executeUpdate(queryUpdate);
                                player.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.commands.forgotten"), args[1]));
                            } else {
                                sender.sendMessage(plugin.getLocalisedName() + String.format(plugin.getConfig().getString("localisation.commands.needs_undiscover", args[1])));
                            }
                            return true;
                        }
                    } else {
                        sender.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.commands.no_plate_name"));
                    }
                    return true;
                } catch (SQLException e) {
                    plugin.debug("Could not buy discover plate, " + e);
                } finally {
                    if (rsForget != null) {
                        try {
                            rsForget.close();
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
        return false;
    }
}

