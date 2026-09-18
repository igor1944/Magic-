package ru.magicplus.task;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.magicplus.MagicPlusPlugin;
import ru.magicplus.mana.ManaManager;
import ru.magicplus.mana.ManaProfile;
import ru.magicplus.spell.SpellManager;
import ru.magicplus.util.ColorUtil;

/** Персональная полоска маны в action bar каждого игрока. */
public final class ActionBarTask extends BukkitRunnable {
    private final MagicPlusPlugin plugin;
    private final ManaManager manaManager;
    private final SpellManager spellManager;

    public ActionBarTask(MagicPlusPlugin plugin, ManaManager manaManager, SpellManager spellManager) {
        this.plugin = plugin;
        this.manaManager = manaManager;
        this.spellManager = spellManager;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("mana.action-bar.enabled", true)) {
            return;
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ManaProfile profile = manaManager.get(player);
            player.sendActionBar(ColorUtil.component(format(player, profile)));
        }
    }

    private String format(Player player, ManaProfile profile) {
        int segments = Math.max(1, Math.min(100,
                plugin.getConfig().getInt("mana.action-bar.segments", 20)));
        double ratio = profile.getMaxMana() <= 0.0 ? 0.0 : profile.getMana() / profile.getMaxMana();
        int filled = Math.max(0, Math.min(segments, (int) Math.round(ratio * segments)));

        String fullSymbol = plugin.getConfig().getString("mana.action-bar.full-symbol", "|");
        String emptySymbol = plugin.getConfig().getString("mana.action-bar.empty-symbol", "|");
        String fullColor = plugin.getConfig().getString("mana.action-bar.full-color", "&d");
        String emptyColor = plugin.getConfig().getString("mana.action-bar.empty-color", "&7");
        String bar = fullColor + safe(fullSymbol).repeat(filled)
                + emptyColor + safe(emptySymbol).repeat(segments - filled);

        String format = plugin.getConfig().getString("mana.action-bar.format",
                "&5Мана &8[&d{bar}&8] &f{mana}&7/&f{max} &8| &b{spell}");
        return safe(format)
                .replace("{bar}", bar)
                .replace("{mana}", ColorUtil.number(profile.getMana()))
                .replace("{max}", ColorUtil.number(profile.getMaxMana()))
                .replace("{spell}", spellManager.getSelectedDisplayName(player));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
