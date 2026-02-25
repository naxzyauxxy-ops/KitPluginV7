package dev.kitplugin.gui;

import dev.kitplugin.KitPlugin;
import dev.kitplugin.kits.Kit;
import dev.kitplugin.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitMenu {

    public static String getTitle(KitPlugin plugin) {
        return plugin.getConfig().getString("gui.title", "§7SERVER KITS");
    }

    public static void open(KitPlugin plugin, Player player) {
        int rows     = plugin.getConfig().getInt("gui.rows", 4);
        int size     = rows * 9;
        int bookSlot = plugin.getConfig().getInt("gui.book-slot", 31);
        String title = getTitle(plugin);

        Inventory inv = Bukkit.createInventory(null, size, title);

        for (Kit kit : plugin.getKitManager().getKits()) {
            int slot = kit.getSlot();
            if (slot >= 0 && slot < size) {
                inv.setItem(slot, buildKitItem(plugin, player, kit));
            }
        }

        if (bookSlot >= 0 && bookSlot < size) {
            inv.setItem(bookSlot, new ItemBuilder(Material.BOOK)
                    .name("§e§lKit Info")
                    .lore(
                        "§7Left-click  §8» §aClaim kit",
                        "§7Right-click §8» §ePreview kit",
                        "",
                        "§7§oAll kits can be previewed",
                        "§7§oby anyone, even without a rank!"
                    ).build());
        }

        player.openInventory(inv);
    }

    public static ItemStack buildKitItem(KitPlugin plugin, Player player, Kit kit) {
        boolean hasPermission = player.hasPermission(kit.getPermission());
        long remaining        = plugin.getKitManager().getRemainingCooldown(player, kit);

        Material icon = kit.getChestplate() != null
                ? kit.getChestplate().getType()
                : kit.getIcon().getType();

        List<String> lore = new ArrayList<>();

        if (!hasPermission) {
            // Locked — but still previewable
            lore.add("§cLocked §8— §7requires §f" + kit.getId().toUpperCase() + " §7rank");
            lore.add("");
            lore.add("§eRight-click §8» §7Preview contents");
            return new ItemBuilder(icon)
                    .name(kit.getDisplayName())
                    .lore(lore)
                    .hideFlags()
                    .build();
        }

        // Unlocked — show contents + cooldown
        for (String line : kit.getContentLore()) {
            lore.add("§8» " + line);
        }
        lore.add("");
        if (remaining > 0) {
            lore.add("§c§lCOOLDOWN §f" + plugin.getKitManager().formatTime(remaining));
        } else {
            lore.add("§a§lREADY TO CLAIM");
        }
        lore.add("");
        lore.add("§aLeft-click  §8» §7Claim");
        lore.add("§eRight-click §8» §7Preview");

        return new ItemBuilder(icon)
                .name(kit.getDisplayName())
                .lore(lore)
                .hideFlags()
                .build();
    }
}
