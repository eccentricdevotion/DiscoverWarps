package me.eccentric_nz.plugins.discoverwarps;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class DiscoverWarpsConfig {

    private final DiscoverWarps plugin;
    private FileConfiguration config = null;
    private File configFile = null;
    HashMap<String, String> strOptions = new HashMap<String, String>();
    HashMap<String, Integer> intOptions = new HashMap<String, Integer>();
    HashMap<String, Boolean> boolOptions = new HashMap<String, Boolean>();

    public DiscoverWarpsConfig(DiscoverWarps plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
        // boolean
        boolOptions.put("no_damage", false);
        boolOptions.put("allow_buying", false);
        boolOptions.put("xp_on_discover", false);
        boolOptions.put("debug", false);
        boolOptions.put("localisation.commands.str_true", true);
        boolOptions.put("localisation.commands.str_false", false);
        // integer
        intOptions.put("no_damage_time", 10);
        intOptions.put("xp_to_give", 3);
        // string
        strOptions.put("sign", "discoverwarp");
        strOptions.put("localisation.plugin_name", "DiscoverWarps");
        strOptions.put("localisation.config", "was set to %s");
        strOptions.put("localisation.commands.help", "Help");
        strOptions.put("localisation.commands.permission", "You do not have permission to run that command!");
        strOptions.put("localisation.commands.arguments", "Not enough command arguments!");
        strOptions.put("localisation.commands.not_plate", "You are not standing on a stone pressure plate");
        strOptions.put("localisation.commands.name_in_use", "That name is already in use!");
        strOptions.put("localisation.commands.no_plate_name", "Couldn't find a DiscoverPlate with that name!");
        strOptions.put("localisation.commands.only_player", "This command requires a player!");
        strOptions.put("localisation.commands.cost", "The last argument must be a number!");
        strOptions.put("localisation.commands.none_set", "There are no DiscoverPlates to find!");
        strOptions.put("localisation.commands.no_warp_name", "You need to supply a warp name!");
        strOptions.put("localisation.commands.needs_discover", "You need to discover %s before you can teleport to it!");
        strOptions.put("localisation.commands.auto_discover", "DiscoverPlate %s auto-discovery is");
        strOptions.put("localisation.commands.added", "DiscoverPlate %s added!");
        strOptions.put("localisation.commands.enabled", "DiscoverPlate %s enabled!");
        strOptions.put("localisation.commands.disabled", "DiscoverPlate %s disabled!");
        strOptions.put("localisation.commands.deleted", "DiscoverPlate %s deleted!");
        strOptions.put("localisation.commands.restart", "A server restart will be needed in order to hook %s into your economy plugin");
        strOptions.put("localisation.buying.no_buying", "You are not allowed to buy DiscoverWarps on this server!");
        strOptions.put("localisation.buying.cannot_buy", "You cannot buy the location of DiscoverPlate %s!");
        strOptions.put("localisation.buying.no_money", "You don't have enough money to use this sign!");
        strOptions.put("localisation.buying.no_need", "You have already discovered %s!");
        strOptions.put("localisation.buying.bought", "You bought the DiscoverPlate location %s for");
        strOptions.put("localisation.discovered", "You have discovered %s");
        strOptions.put("localisation.teleport", "Teleporting");
        strOptions.put("localisation.list", "List");
        strOptions.put("localisation.visited", "VISITED");
        strOptions.put("localisation.not_visited", "NOT VISITED");
        strOptions.put("localisation.auto", "AUTO");
        strOptions.put("localisation.no_break", "You cannot break this pressure plate, use %s to remove it.");
        strOptions.put("localisation.help.set", "To set a stone pressure plate as a DiscoverPlate, stand on it and then type");
        strOptions.put("localisation.help.delete", "To delete a DiscoverPlate type");
        strOptions.put("localisation.help.disable", "To disable a DiscoverPlate type");
        strOptions.put("localisation.help.enable", "To enable a DiscoverPlate type");
        strOptions.put("localisation.help.auto", "To make a DiscoverPlate auto-discovered type");
        strOptions.put("localisation.help.cost", "To set a cost to buy a DiscoverPlate location type");
        strOptions.put("localisation.help.list", "To list DiscoverPlates type");
        strOptions.put("localisation.help.warp", "To warp to a DiscoverPlate type");
        strOptions.put("localisation.help.buy", "To buy a DiscoverPlate location type");
        strOptions.put("localisation.help.config", "To toggle DiscoverWarps config settings type");
        strOptions.put("localisation.signs.no_money", "You don't have enough maney to use this sign!");
        strOptions.put("localisation.signs.needs_discover", "You have not discovered %s yet!");
        strOptions.put("localisation.signs.sign_made", "Sign set successfully!");
    }

    public void checkConfig() {
        int i = 0;
        // int values
        for (Map.Entry<String, Integer> entry : intOptions.entrySet()) {
            if (!config.contains(entry.getKey())) {
                plugin.getConfig().set(entry.getKey(), entry.getValue());
                i++;
            }
        }
        // string values
        for (Map.Entry<String, String> entry : strOptions.entrySet()) {
            if (!config.contains(entry.getKey())) {
                plugin.getConfig().set(entry.getKey(), entry.getValue());
                i++;
            }
        }
        // boolean values
        for (Map.Entry<String, Boolean> entry : boolOptions.entrySet()) {
            if (!config.contains(entry.getKey())) {
                plugin.getConfig().set(entry.getKey(), entry.getValue());
                i++;
            }
        }
        plugin.saveConfig();
        if (i > 0) {
            System.out.println("[DiscoverWarps] Added " + i + " new items to config");
        }
    }
}
