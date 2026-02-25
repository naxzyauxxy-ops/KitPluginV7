package dev.kitplugin.kits;

import org.bukkit.inventory.ItemStack;
import java.util.List;

public class Kit {

    private final String id;
    private final String displayName;
    private final int slot;
    private final String permission;
    private final long cooldownMillis;
    private final ItemStack icon;
    private final List<String> contentLore;
    private final ItemStack helmet;
    private final ItemStack chestplate;
    private final ItemStack leggings;
    private final ItemStack boots;
    private final List<ItemStack> items;

    public Kit(String id, String displayName, int slot, String permission, long cooldownMillis,
               ItemStack icon, List<String> contentLore,
               ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots,
               List<ItemStack> items) {
        this.id = id;
        this.displayName = displayName;
        this.slot = slot;
        this.permission = permission;
        this.cooldownMillis = cooldownMillis;
        this.icon = icon;
        this.contentLore = contentLore;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.items = items;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getSlot() { return slot; }
    public String getPermission() { return permission; }
    public long getCooldownMillis() { return cooldownMillis; }
    public ItemStack getIcon() { return icon.clone(); }
    public List<String> getContentLore() { return contentLore; }
    public ItemStack getHelmet() { return helmet != null ? helmet.clone() : null; }
    public ItemStack getChestplate() { return chestplate != null ? chestplate.clone() : null; }
    public ItemStack getLeggings() { return leggings != null ? leggings.clone() : null; }
    public ItemStack getBoots() { return boots != null ? boots.clone() : null; }
    public List<ItemStack> getItems() { return items; }
}
