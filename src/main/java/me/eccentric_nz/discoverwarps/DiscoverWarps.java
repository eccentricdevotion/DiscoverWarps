 /* Portions of this code are copyright (c) 2011, The Multiverse Team All rights reserved. */
package me.eccentric_nz.discoverwarps;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscoverWarps extends JavaPlugin {

    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();
    private DiscoverWarpsCommands commando;
    PluginManager pm = getServer().getPluginManager();
    private Vault vault;
    public Economy economy;
    ConsoleCommandSender console;
    String MY_PLUGIN_NAME = ChatColor.GOLD + "[DiscoverWarps] " + ChatColor.RESET;
    private Map<String, DiscoverWarpsSession> discoverWarpSessions;

    @Override
    public void onDisable() {
        this.saveConfig();
        try {
            service.connection.close();
        } catch (SQLException e) {
            debug("Could not close database connection: " + e);
        }
    }

    @Override
    public void onEnable() {
        console = getServer().getConsoleSender();
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                console.sendMessage(MY_PLUGIN_NAME + " Could not create directory!");
                console.sendMessage(MY_PLUGIN_NAME + " Requires you to manually make the DiscoverWarps/ directory!");
            }
            getDataFolder().setWritable(true);
            getDataFolder().setExecutable(true);
        }
        this.saveDefaultConfig();
        try {
            String path = getDataFolder() + File.separator + "DiscoverWarps.db";
            service.setConnection(path);
            service.createTables();
        } catch (Exception e) {
            console.sendMessage(MY_PLUGIN_NAME + " Connection and Tables Error: " + e);
        }
        // check config
        new DiscoverWarpsConfig(this).checkConfig();
        commando = new DiscoverWarpsCommands(this);
        getCommand("discoverwarps").setExecutor(commando);

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }

        registerListeners();

        if (getConfig().getBoolean("allow_buying")) {
            if (!setupVault()) {
                pm.disablePlugin(this);
                return;
            }
            setupEconomy();
        }
        this.discoverWarpSessions = new HashMap<String, DiscoverWarpsSession>();
    }

    private boolean setupVault() {
        Plugin x = pm.getPlugin("Vault");
        if (x != null && x instanceof Vault) {
            vault = (Vault) x;
            return true;
        } else {
            console.sendMessage("Vault is required for economy, but wasn't found!");
            console.sendMessage("Download it from http://dev.bukkit.org/server-mods/vault/");
            console.sendMessage("Disabling plugin.");
            return false;
        }
    }

    //Loading economy API from Vault
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public void debug(Object o) {
        if (getConfig().getBoolean("debug") == true) {
            console.sendMessage("[DiscoverWarps Debug] " + o);
        }
    }

    private void registerListeners() {
        if (pm.isPluginEnabled("WorldGuard")) {
            pm.registerEvents(new DiscoverWarpsMoveListener(this), this);
        }
        pm.registerEvents(new DiscoverWarpsPlateListener(this), this);
        pm.registerEvents(new DiscoverWarpsProtectionListener(this), this);
        pm.registerEvents(new DiscoverWarpsExplodeListener(this), this);
        pm.registerEvents(new DiscoverWarpsSignListener(this), this);
    }

    public DiscoverWarpsSession getDiscoverWarpsSession(Player p) {
        if (this.discoverWarpSessions.containsKey(p.getName())) {
            return this.discoverWarpSessions.get(p.getName());
        }
        DiscoverWarpsSession session = new DiscoverWarpsSession(p);
        this.discoverWarpSessions.put(p.getName(), session);
        return session;
    }
}
