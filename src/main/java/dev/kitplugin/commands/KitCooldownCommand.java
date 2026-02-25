package dev.kitplugin.commands;

import dev.kitplugin.KitPlugin;
import dev.kitplugin.kits.Kit;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class KitCooldownCommand implements CommandExecutor, TabCompleter {

    private final KitPlugin plugin;

    public KitCooldownCommand(KitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("kit.admin")) {
            sender.sendMessage("§8[§cKits§8] §cNo permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§8[§aKits Admin§8] §6Usage:");
            sender.sendMessage("  §7/kitcooldown remove <player> §8— Clear all cooldowns");
            sender.sendMessage("  §7/kitcooldown check <player> §8— View cooldown status");
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§8[§cKits§8] §cPlayer not found.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "remove" -> {
                plugin.getKitManager().clearCooldowns(target.getUniqueId());
                sender.sendMessage("§8[§aKits§8] §aCleared all kit cooldowns for §e" + target.getName() + "§a.");
            }
            case "check" -> {
                sender.sendMessage("§8[§aKits§8] §6Cooldowns for §e" + target.getName() + "§6:");
                if (target.isOnline()) {
                    for (Kit kit : plugin.getKitManager().getKits()) {
                        long rem = plugin.getKitManager().getRemainingCooldown(target.getPlayer(), kit);
                        String status = rem > 0 ? "§c" + plugin.getKitManager().formatTime(rem) : "§aReady";
                        sender.sendMessage("  §8» §7" + kit.getId() + ": " + status);
                    }
                } else {
                    sender.sendMessage("  §7Player is offline — in-memory cooldowns not available.");
                }
            }
            default -> sender.sendMessage("§cUnknown subcommand. Use remove or check.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("remove", "check");
        return List.of();
    }
}
