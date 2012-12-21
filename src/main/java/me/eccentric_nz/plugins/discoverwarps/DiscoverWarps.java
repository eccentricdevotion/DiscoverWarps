package me.eccentric_nz.plugins.discoverwarps;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscoverWarps extends JavaPlugin {

    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();
    private DiscoverWarpsCommands commando;
    PluginManager pm = Bukkit.getServer().getPluginManager();
    DiscoverWarpsPlateListener plateListener = new DiscoverWarpsPlateListener(this);
    DiscoverWarpsProtectionListener protectionListener = new DiscoverWarpsProtectionListener(this);
    DiscoverWarpsExplodeListener explodeListener = new DiscoverWarpsExplodeListener(this);
    private Vault vault;
    public Economy economy;
    private FileConfiguration config = null;

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
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                System.err.println(DiscoverWarpsConstants.MY_PLUGIN_NAME + " Could not create directory!");
                System.out.println(DiscoverWarpsConstants.MY_PLUGIN_NAME + " Requires you to manually make the DiscoverWarps/ directory!");
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
            System.err.println(DiscoverWarpsConstants.MY_PLUGIN_NAME + " Connection and Tables Error: " + e);
        }
        pm.registerEvents(plateListener, this);
        pm.registerEvents(protectionListener, this);
        pm.registerEvents(explodeListener, this);
        commando = new DiscoverWarpsCommands(this);
        getCommand("discoverwarps").setExecutor(commando);

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        // add allow_buying etc to config if missing
        File myconfigfile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(myconfigfile);
        if (!config.contains("allow_set_spawn")) {
            this.getConfig().set("allow_buying", false);
            this.getConfig().set("xp_on_discover", false);
            this.getConfig().set("xp_to_give", 3);
            this.saveConfig();
            System.out.println("[DiscoverWarps] Added new config options");
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