package dev.kitplugin.gui;

import dev.kitplugin.KitPlugin;
import dev.kitplugin.kits.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KitMenuListener implements Listener {

    private final KitPlugin plugin;

    public KitMenuListener(KitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        boolean isMainMenu = KitMenu.getTitle(plugin).equals(title);
        boolean isPreview  = title.startsWith(KitPreviewMenu.TITLE_PREFIX);

        if (!isMainMenu && !isPreview) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        String name = meta.getDisplayName();

        // ── MAIN MENU ──────────────────────────────────────────────
        if (isMainMenu) {
            for (Kit kit : plugin.getKitManager().getKits()) {
                if (!kit.getDisplayName().equals(name)) continue;

                if (event.getClick() == ClickType.RIGHT) {
                    // Anyone can preview — no permission check here
                    KitPreviewMenu.open(plugin, player, kit);
                } else {
                    // Left-click: require permission to claim
                    if (!player.hasPermission(kit.getPermission())) {
                        player.sendMessage("§8[§cKits§8] §cYou need the §l"
                                + kit.getId().toUpperCase() + " §crank to claim this kit!");
                        player.sendMessage("§8[§cKits§8] §7Right-click to preview it first.");
                        return;
                    }
                    player.closeInventory();
                    plugin.getKitManager().giveKit(player, kit);
                }
                return;
            }
        }

        // ── PREVIEW MENU ───────────────────────────────────────────
        if (isPreview) {
            if ("§e§lBack to Kits".equals(name)) {
                KitMenu.open(plugin, player);
                return;
            }
            if ("§a§lCLAIM KIT".equals(name)) {
                String kitDisplayName = title.substring(KitPreviewMenu.TITLE_PREFIX.length());
                for (Kit kit : plugin.getKitManager().getKits()) {
                    if (kit.getDisplayName().equals(kitDisplayName)) {
                        if (!player.hasPermission(kit.getPermission())) {
                            player.sendMessage("§8[§cKits§8] §cYou need the §l"
                                    + kit.getId().toUpperCase() + " §crank to claim this kit!");
                            return;
                        }
                        player.closeInventory();
                        plugin.getKitManager().giveKit(player, kit);
                        return;
                    }
                }
            }
        }
    }
}
