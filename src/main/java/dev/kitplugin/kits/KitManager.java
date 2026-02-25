package dev.kitplugin.kits;

import dev.kitplugin.KitPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class KitManager {

    private final KitPlugin plugin;
    private final List<Kit> kits = new ArrayList<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public KitManager(KitPlugin plugin) {
        this.plugin = plugin;
        loadKits();
    }

    public void reload() {
        kits.clear();
        plugin.reloadConfig();
        loadKits();
    }

    private void loadKits() {
        ConfigurationSection kitsSection = plugin.getConfig().getConfigurationSection("kits");
        if (kitsSection == null) {
            plugin.getLogger().warning("No kits found in config.yml!");
            return;
        }

        for (String kitId : kitsSection.getKeys(false)) {
            ConfigurationSection sec = kitsSection.getConfigurationSection(kitId);
            if (sec == null) continue;
            try {
                int slot = sec.getInt("slot", 0);
                String displayName = ChatColor.translateAlternateColorCodes('&', sec.getString("display-name", kitId));
                String permission = sec.getString("permission", "kit." + kitId);
                long cooldownMs = sec.getLong("cooldown", 1800) * 1000L;

                Material iconMat = Material.matchMaterial(sec.getString("icon", "IRON_CHESTPLATE"));
                if (iconMat == null) iconMat = Material.IRON_CHESTPLATE;

                // Parse armor-enchants (shared across all pieces)
                Map<Enchantment, Integer> armorEnchants = new LinkedHashMap<>();
                ConfigurationSection enchSec = sec.getConfigurationSection("armor-enchants");
                if (enchSec != null) {
                    for (String enchName : enchSec.getKeys(false)) {
                        Enchantment e = Enchantment.getByName(enchName);
                        if (e != null) armorEnchants.put(e, enchSec.getInt(enchName));
                    }
                }

                // Parse armor pieces
                ConfigurationSection armorSec = sec.getConfigurationSection("armor");
                ItemStack helmet     = buildArmorPiece(armorSec, "helmet",     armorEnchants);
                ItemStack chestplate = buildArmorPiece(armorSec, "chestplate", armorEnchants);
                ItemStack leggings   = buildArmorPiece(armorSec, "leggings",   armorEnchants);
                ItemStack boots      = buildArmorPiece(armorSec, "boots",      armorEnchants);

                // Parse items using simple format: "MATERIAL ENCHANT:LEVEL xAMOUNT"
                List<ItemStack> items = new ArrayList<>();
                for (String line : sec.getStringList("items")) {
                    ItemStack item = parseItemLine(line);
                    if (item != null) items.add(item);
                }

                // Build content lore from items
                List<String> contentLore = new ArrayList<>();
                if (helmet != null) contentLore.add("§8Armor §7" + formatMat(helmet.getType()) + " | " + formatEnchants(armorEnchants));
                for (ItemStack item : items) {
                    contentLore.add("§8» §7" + formatMat(item.getType()) + " x" + item.getAmount()
                            + (item.getItemMeta().hasEnchants() ? " | " + formatEnchants(item.getItemMeta().getEnchants()) : ""));
                }

                ItemStack icon = new ItemStack(iconMat);
                ItemMeta iconMeta = icon.getItemMeta();
                iconMeta.setDisplayName(displayName);
                iconMeta.addItemFlags(ItemFlag.values());
                icon.setItemMeta(iconMeta);

                kits.add(new Kit(kitId, displayName, slot, permission, cooldownMs,
                        icon, contentLore, helmet, chestplate, leggings, boots, items));
                plugin.getLogger().info("Loaded kit: " + kitId + " (slot " + slot + ")");

            } catch (Exception e) {
                plugin.getLogger().warning("Error loading kit '" + kitId + "': " + e.getMessage());
            }
        }

        kits.sort(Comparator.comparingInt(Kit::getSlot));
        plugin.getLogger().info("Loaded " + kits.size() + " kit(s).");
    }

    // Builds an armor piece with shared enchants
    private ItemStack buildArmorPiece(ConfigurationSection armorSec, String key, Map<Enchantment, Integer> enchants) {
        if (armorSec == null) return null;
        String matStr = armorSec.getString(key);
        if (matStr == null) return null;
        Material mat = Material.matchMaterial(matStr);
        if (mat == null) return null;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        enchants.forEach((e, lvl) -> meta.addEnchant(e, lvl, true));
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }

    // Parses item lines like: "DIAMOND_SWORD SHARPNESS:5 UNBREAKING:3 x1"
    private ItemStack parseItemLine(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0) return null;

        Material mat = Material.matchMaterial(parts[0]);
        if (mat == null) {
            plugin.getLogger().warning("Unknown material: " + parts[0]);
            return null;
        }

        int amount = 1;
        Map<Enchantment, Integer> enchants = new LinkedHashMap<>();

        for (int i = 1; i < parts.length; i++) {
            String token = parts[i];
            if (token.toLowerCase().startsWith("x")) {
                // Amount token: x64
                try { amount = Integer.parseInt(token.substring(1)); } catch (NumberFormatException ignored) {}
            } else if (token.contains(":")) {
                // Enchant token: SHARPNESS:5
                String[] split = token.split(":", 2);
                Enchantment ench = Enchantment.getByName(split[0]);
                if (ench != null) {
                    try { enchants.put(ench, Integer.parseInt(split[1])); } catch (NumberFormatException ignored) {}
                } else {
                    plugin.getLogger().warning("Unknown enchantment: " + split[0]);
                }
            }
        }

        ItemStack item = new ItemStack(mat, amount);
        if (!enchants.isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            enchants.forEach((e, lvl) -> meta.addEnchant(e, lvl, true));
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatMat(Material mat) {
        String name = mat.name().replace("_", " ").toLowerCase();
        String[] words = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        return sb.toString().trim();
    }

    private String formatEnchants(Map<Enchantment, Integer> enchants) {
        StringBuilder sb = new StringBuilder();
        enchants.forEach((e, lvl) -> {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(e.getKey().getKey().replace("_", " ")).append(" ").append(lvl);
        });
        return sb.toString();
    }

    // ── COOLDOWNS ─────────────────────────────────────────────────

    public long getRemainingCooldown(Player player, Kit kit) {
        if (kit.getCooldownMillis() <= 0) return 0;
        Map<String, Long> pc = cooldowns.get(player.getUniqueId());
        if (pc == null) return 0;
        Long last = pc.get(kit.getId());
        if (last == null) return 0;
        return Math.max(0, kit.getCooldownMillis() - (System.currentTimeMillis() - last));
    }

    public void setCooldown(Player player, Kit kit) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(kit.getId(), System.currentTimeMillis());
    }

    public void clearCooldowns(UUID uuid) { cooldowns.remove(uuid); }

    public String formatTime(long millis) {
        long s = millis / 1000, m = s / 60, h = m / 60;
        m %= 60; s %= 60;
        if (h > 0) return h + "h " + m + "m " + s + "s";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    // ── GIVE KIT ──────────────────────────────────────────────────

    public boolean giveKit(Player player, Kit kit) {
        long remaining = getRemainingCooldown(player, kit);
        if (remaining > 0) {
            player.sendMessage("§8[§cKits§8] §cOn cooldown! §7(" + formatTime(remaining) + " remaining)");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return false;
        }
        if (kit.getHelmet() != null)     player.getInventory().setHelmet(kit.getHelmet());
        if (kit.getChestplate() != null) player.getInventory().setChestplate(kit.getChestplate());
        if (kit.getLeggings() != null)   player.getInventory().setLeggings(kit.getLeggings());
        if (kit.getBoots() != null)      player.getInventory().setBoots(kit.getBoots());
        for (ItemStack item : kit.getItems()) player.getInventory().addItem(item.clone());
        setCooldown(player, kit);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent("§aYou claimed the " + kit.getDisplayName() + " §akit!"));
        player.sendMessage("");
        player.sendMessage("§8[§aKits§8] " + kit.getDisplayName() + " §akit claimed!");
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        return true;
    }

    public List<Kit> getKits() { return Collections.unmodifiableList(kits); }
    public Kit getKit(String id) {
        return kits.stream().filter(k -> k.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }
}
