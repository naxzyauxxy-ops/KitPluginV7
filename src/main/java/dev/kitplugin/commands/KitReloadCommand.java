package dev.kitplugin.commands;

import dev.kitplugin.KitPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class KitReloadCommand implements CommandExecutor {

    private final KitPlugin plugin;

    public KitReloadCommand(KitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("kit.admin")) {
            sender.sendMessage("§8[§cKits§8] §cNo permission.");
            return true;
        }
        plugin.getKitManager().reload();
        sender.sendMessage("§8[§aKits§8] §aConfig reloaded! Loaded §e"
                + plugin.getKitManager().getKits().size() + " §akits.");
        return true;
    }
}
