package dev.kitplugin.commands;

import dev.kitplugin.KitPlugin;
import dev.kitplugin.gui.KitMenu;
import dev.kitplugin.kits.Kit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class KitCommand implements CommandExecutor, TabCompleter {

    private final KitPlugin plugin;

    public KitCommand(KitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        if (!player.hasPermission("kit.use")) {
            player.sendMessage("§8[§cKits§8] §cYou don't have permission to use kits!");
            return true;
        }
        if (args.length == 0) {
            KitMenu.open(plugin, player);
            return true;
        }
        Kit kit = plugin.getKitManager().getKit(args[0]);
        if (kit == null) {
            player.sendMessage("§8[§cKits§8] §cThat kit doesn't exist. Use §7/kit §cto browse kits.");
            return true;
        }
        if (!player.hasPermission(kit.getPermission())) {
            player.sendMessage("§8[§cKits§8] §cYou don't have access to the §l" + kit.getId().toUpperCase() + " §ckit!");
            return true;
        }
        plugin.getKitManager().giveKit(player, kit);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            return plugin.getKitManager().getKits().stream()
                    .filter(k -> player.hasPermission(k.getPermission()))
                    .map(Kit::getId)
                    .filter(id -> id.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
