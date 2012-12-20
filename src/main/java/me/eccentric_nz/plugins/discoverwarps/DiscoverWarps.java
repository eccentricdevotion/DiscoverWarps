package me.eccentric_nz.plugins.discoverwarps;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscoverWarps extends JavaPlugin {

    protected DiscoverWarps plugin;
    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();
    private DiscoverWarpsCommands commando;
    PluginManager pm = Bukkit.getServer().getPluginManager();
    DiscoverWarpsPlateListener plateListener = new DiscoverWarpsPlateListener(plugin);
    DiscoverWarpsProtectionListener protectionListener = new DiscoverWarpsProtectionListener(plugin);
    DiscoverWarpsExplodeListener explodeListener = new DiscoverWarpsExplodeListener(plugin);
    private Vault vault;
    public Economy economy;

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
        // add allow_buying to config if missing
        if (!getConfig().contains("allow_buying")) {
            getConfig().set("allow_buying", true);
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
        if (getConfig().getBoolean("allow_buying")) {
            if (!setupVault()) {
                pm.disablePlugin(this);
                return;
            }
            setupEconomy();
        }
    }

    private boolean setupVault() {
        Plugin x = pm.getPlugin("Vault");
        if (x != null && x instanceof Vault) {
            vault = (Vault) x;
            return true;
        } else {
            System.err.println("Vault is required for economy, but wasn't found!");
            System.err.println("Download it from http://dev.bukkit.org/server-mods/vault/");
            System.err.println("Disabling plugin.");
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
            System.out.println("[DiscoverWarps Debug] " + o);
        }
    }
}