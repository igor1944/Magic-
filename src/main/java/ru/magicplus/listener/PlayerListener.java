package ru.magicplus.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.magicplus.MagicPlusPlugin;
import ru.magicplus.mana.ManaManager;
import ru.magicplus.mana.ManaProfile;
import ru.magicplus.spell.SpellManager;
import ru.magicplus.wand.WandService;

public final class PlayerListener implements Listener {
    private final MagicPlusPlugin plugin;
    private final ManaManager manaManager;
    private final SpellManager spellManager;
    private final WandService wandService;

    public PlayerListener(MagicPlusPlugin plugin, ManaManager manaManager,
                          SpellManager spellManager, WandService wandService) {
        this.plugin = plugin;
        this.manaManager = manaManager;
        this.spellManager = spellManager;
        this.wandService = wandService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ManaProfile profile = manaManager.load(event.getPlayer());
        spellManager.ensureSelection(event.getPlayer());

        if (plugin.getConfig().getBoolean("wand.give-on-first-join", true)
                && !profile.isWandReceived()
                && event.getPlayer().hasPermission("magicplus.wand")) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (event.getPlayer().isOnline()) {
                    manaManager.get(event.getPlayer()).setWandReceived(true);
                    wandService.give(event.getPlayer());
                }
            }, 10L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manaManager.save(event.getPlayer(), true);
        spellManager.clearCooldowns(event.getPlayer());
    }
}
