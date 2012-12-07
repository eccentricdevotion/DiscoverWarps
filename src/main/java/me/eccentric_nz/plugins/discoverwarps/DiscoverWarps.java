package me.eccentric_nz.plugins.discoverwarps;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscoverWarps extends JavaPlugin {

    protected static DiscoverWarps plugin;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();
    private DiscoverWarpsCommands commando;
    PluginManager pm = Bukkit.getServer().getPluginManager();
    DiscoverWarpsPlateListener plateListener = new DiscoverWarpsPlateListener(plugin);
    DiscoverWarpsProtectionListener protectionListener = new DiscoverWarpsProtectionListener(plugin);
    DiscoverWarpsExplodeListener explodeListener = new DiscoverWarpsExplodeListener(plugin);

    @Override
    public void onDisable() {
        this.saveConfig();
        try {
            service.connection.close();
        } catch (Exception e) {
            debug("Could not close database connection: " + e);
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();

        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                System.err.println(DiscoverWarpsConstants.MY_PLUGIN_NAME + " Could not create directory!");
                System.out.println(DiscoverWarpsConstants.MY_PLUGIN_NAME + " Requires you to manually make the DiscoverWarps/ directory!");
            }
            getDataFolder().setWritable(true);
            getDataFolder().setExecutable(true);
        }

        try {
            String path = getDataFolder() + File.separator + "DiscoverWarps.db";
            service.setConnection(path);
            service.createTables();
        } catch (Exception e) {
            System.err.println(DiscoverWarpsConstants.MY_PLUGIN_NAME + " Connection and Tables Error: " + e);
        }
        pm.registerEvents(plateListener, plugin);
        pm.registerEvents(protectionListener, plugin);
        pm.registerEvents(explodeListener, plugin);
        commando = new DiscoverWarpsCommands(plugin);
        getCommand("discoverwarps").setExecutor(commando);

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
    }

    public void debug(Object o) {
        if (getConfig().getBoolean("debug") == true) {
            System.out.println("[DiscoverWarps Debug] " + o);
        }
    }
}