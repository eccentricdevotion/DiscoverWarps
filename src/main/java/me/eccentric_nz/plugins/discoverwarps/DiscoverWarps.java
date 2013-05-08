package me.eccentric_nz.plugins.discoverwarps;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscoverWarps extends JavaPlugin {

    DiscoverWarpsDatabase service = DiscoverWarpsDatabase.getInstance();
    private DiscoverWarpsCommands commando;
    PluginManager pm = Bukkit.getServer().getPluginManager();
    DiscoverWarpsPlateListener plateListener;
    DiscoverWarpsProtectionListener protectionListener;
    DiscoverWarpsExplodeListener explodeListener;
    DiscoverWarpsSignListener signListener;
    private Vault vault;
    public Economy economy;
    private FileConfiguration config = null;
    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    String MY_PLUGIN_NAME = ChatColor.GOLD + "[DiscoverWarps] " + ChatColor.RESET;

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
        registerListeners();
        commando = new DiscoverWarpsCommands(this);
        getCommand("discoverwarps").setExecutor(commando);

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        // check config
        new DiscoverWarpsConfig(this).checkConfig();

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
        plateListener = new DiscoverWarpsPlateListener(this);
        protectionListener = new DiscoverWarpsProtectionListener(this);
        explodeListener = new DiscoverWarpsExplodeListener(this);
        signListener = new DiscoverWarpsSignListener(this);
        pm.registerEvents(plateListener, this);
        pm.registerEvents(protectionListener, this);
        pm.registerEvents(explodeListener, this);
        pm.registerEvents(signListener, this);

    }
}
