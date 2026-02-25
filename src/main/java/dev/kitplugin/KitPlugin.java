package dev.kitplugin;

import dev.kitplugin.commands.KitCommand;
import dev.kitplugin.commands.KitCooldownCommand;
import dev.kitplugin.commands.KitReloadCommand;
import dev.kitplugin.gui.KitMenuListener;
import dev.kitplugin.kits.KitManager;
import org.bukkit.plugin.java.JavaPlugin;

public class KitPlugin extends JavaPlugin {

    private KitManager kitManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        kitManager = new KitManager(this);
        getCommand("kit").setExecutor(new KitCommand(this));
        getCommand("kitcooldown").setExecutor(new KitCooldownCommand(this));
        getCommand("kitreload").setExecutor(new KitReloadCommand(this));
        getServer().getPluginManager().registerEvents(new KitMenuListener(this), this);
        getLogger().info("KitPlugin enabled! " + kitManager.getKits().size() + " kits loaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("KitPlugin disabled!");
    }

    public KitManager getKitManager() { return kitManager; }
}
