package me.eccentric_nz.discoverwarps;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DiscoverWarpsTabComplete implements TabCompleter {

    private final DiscoverWarps plugin;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();
    private final List<String> CMD_SUBS = Arrays.asList("allow_buying", "auto", "buy", "cost", "delete", "disable", "enable", "icon", "list", "rename", "set", "sign", "tp", "xp_on_discover");
    private final List<String> MAT_SUBS = new ArrayList<>();

    public DiscoverWarpsTabComplete(DiscoverWarps plugin) {
        this.plugin = plugin;
        for (Material m : Material.values()) {
            if (!m.toString().startsWith("LEGACY")) {
                MAT_SUBS.add(m.toString());
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 1:
                return partial(args[0], CMD_SUBS);
            case 2:
                Statement statement = null;
                ResultSet rsNames = null;
                try {
                    List<String> names = new ArrayList<>();
                    Connection connection = service.getConnection();
                    statement = connection.createStatement();
                    String queryName = "SELECT name FROM discoverwarps";
                    rsNames = statement.executeQuery(queryName);
                    // check name is not in use
                    if (rsNames.isBeforeFirst()) {
                        while (rsNames.next()) {
                            names.add(rsNames.getString("name"));
                        }
                        return partial(args[1], names);
                    }
                } catch (SQLException ignored) {
                } finally {
                    if (rsNames != null) {
                        try {
                            rsNames.close();
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
                return null;
            case 3:
                if (args[0].equalsIgnoreCase("icon")) {
                    return partial(args[2], MAT_SUBS);
                }
                return null;
            default:
                return null;
        }
    }

    private List<String> partial(String token, Collection<String> from) {
        return StringUtil.copyPartialMatches(token, from, new ArrayList<>(from.size()));
    }
}
