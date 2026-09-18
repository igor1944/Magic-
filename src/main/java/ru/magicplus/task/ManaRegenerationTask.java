package ru.magicplus.task;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.magicplus.MagicPlusPlugin;
import ru.magicplus.mana.ManaManager;
import ru.magicplus.mana.ManaProfile;

/** Периодическое естественное восстановление маны. */
public final class ManaRegenerationTask extends BukkitRunnable {
    private final MagicPlusPlugin plugin;
    private final ManaManager manaManager;

    public ManaRegenerationTask(MagicPlusPlugin plugin, ManaManager manaManager) {
        this.plugin = plugin;
        this.manaManager = manaManager;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("mana.regeneration.enabled", true)) {
            return;
        }
        double amount = Math.max(0.0, plugin.getConfig().getDouble("mana.regeneration.amount", 2.0));
        if (amount <= 0.0) {
            return;
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ManaProfile profile = manaManager.get(player);
            if (profile.getMana() < profile.getMaxMana()) {
                manaManager.add(player, amount);
            }
        }
    }
}
