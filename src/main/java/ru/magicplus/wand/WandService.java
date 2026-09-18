package ru.magicplus.wand;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.magicplus.MagicPlusPlugin;
import ru.magicplus.config.MessageService;
import ru.magicplus.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Создание и надёжное распознавание волшебного жезла через PersistentDataContainer. */
public final class WandService {
    private final MagicPlusPlugin plugin;
    private final MessageService messages;
    private final NamespacedKey wandKey;

    public WandService(MagicPlusPlugin plugin, MessageService messages) {
        this.plugin = plugin;
        this.messages = messages;
        this.wandKey = new NamespacedKey(plugin, "magic_wand");
    }

    public ItemStack createWand() {
        String configuredMaterial = plugin.getConfig().getString("wand.material", "BLAZE_ROD");
        Material material = Material.matchMaterial(configuredMaterial == null ? "BLAZE_ROD" : configuredMaterial);
        if (material == null || !material.isItem()) {
            plugin.getLogger().warning("Неизвестный материал жезла '" + configuredMaterial + "', используется BLAZE_ROD.");
            material = Material.BLAZE_ROD;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ColorUtil.component(plugin.getConfig().getString("wand.name", "&dВолшебный жезл")));

        List<Component> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("wand.lore")) {
            lore.add(ColorUtil.component(line));
        }
        meta.lore(lore);
        int customModelData = plugin.getConfig().getInt("wand.custom-model-data", 0);
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isWand(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        Byte marker = item.getItemMeta().getPersistentDataContainer().get(wandKey, PersistentDataType.BYTE);
        return marker != null && marker == (byte) 1;
    }

    public void give(Player player) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(createWand());
        if (leftovers.isEmpty()) {
            messages.send(player, "wand-received");
            return;
        }
        for (ItemStack item : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        messages.send(player, "wand-inventory-full");
    }
}
