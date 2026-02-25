package dev.kitplugin.gui;

import dev.kitplugin.KitPlugin;
import dev.kitplugin.kits.Kit;
import dev.kitplugin.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KitPreviewMenu {

    public static final String TITLE_PREFIX = "§8Preview: ";

    public static void open(KitPlugin plugin, Player player, Kit kit) {
        String title = TITLE_PREFIX + kit.getDisplayName();
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Dark background
        ItemStack dark = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) inv.setItem(i, dark);

        // Armor pieces (slots 20-23) — cloned with enchant lore injected
        if (kit.getHelmet()     != null) inv.setItem(20, withEnchantLore(kit.getHelmet()));
        if (kit.getChestplate() != null) inv.setItem(21, withEnchantLore(kit.getChestplate()));
        if (kit.getLeggings()   != null) inv.setItem(22, withEnchantLore(kit.getLeggings()));
        if (kit.getBoots()      != null) inv.setItem(23, withEnchantLore(kit.getBoots()));

        // Items row (slots 29-33) — cloned with enchant lore injected
        int[] itemSlots = {29, 30, 31, 32, 33};
        List<ItemStack> items = kit.getItems();
        for (int i = 0; i < items.size() && i < itemSlots.length; i++) {
            inv.setItem(itemSlots[i], withEnchantLore(items.get(i).clone()));
        }

        boolean hasPermission = player.hasPermission(kit.getPermission());

        // Claim button — green if permitted, red if not
        if (hasPermission) {
            inv.setItem(38, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                    .name("§a§lCLAIM KIT")
                    .lore("§7Click to claim the " + kit.getDisplayName() + " §7kit!")
                    .build());
        } else {
            inv.setItem(38, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                    .name("§c§lLOCKED")
                    .lore(
                        "§7You don't have the §f" + kit.getId().toUpperCase() + " §7rank.",
                        "§7Purchase it to claim this kit!"
                    ).build());
        }

        // Back button
        inv.setItem(42, new ItemBuilder(Material.ARROW)
                .name("§e§lBack to Kits")
                .lore("§7Return to the kit menu.")
                .build());

        player.openInventory(inv);
    }

    /**
     * Clones the item and injects a lore block listing all enchantments
     * so they're visible in the preview even when ItemFlags hide them.
     */
    private static ItemStack withEnchantLore(ItemStack original) {
        ItemStack item = original.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Map<Enchantment, Integer> enchants = meta.getEnchants();
        if (enchants.isEmpty()) {
            item.setItemMeta(meta);
            return item;
        }

        List<String> lore = new ArrayList<>();
        lore.add("§7Enchantments:");
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            String enchName = formatEnchantName(entry.getKey().getKey().getKey());
            String level    = toRoman(entry.getValue());
            lore.add("§b  " + enchName + " " + level);
        }

        // Keep any existing lore below
        List<String> existing = meta.getLore();
        if (existing != null && !existing.isEmpty()) {
            lore.add("");
            lore.addAll(existing);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static String formatEnchantName(String key) {
        String[] words = key.replace("_", " ").split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty())
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase()).append(" ");
        }
        return sb.toString().trim();
    }

    private static String toRoman(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }
}
