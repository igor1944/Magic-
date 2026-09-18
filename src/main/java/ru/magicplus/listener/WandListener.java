package ru.magicplus.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import ru.magicplus.config.MessageService;
import ru.magicplus.spell.SpellManager;
import ru.magicplus.wand.WandService;

public final class WandListener implements Listener {
    private final WandService wandService;
    private final SpellManager spellManager;
    private final MessageService messages;

    public WandListener(WandService wandService, SpellManager spellManager, MessageService messages) {
        this.wandService = wandService;
        this.spellManager = spellManager;
        this.messages = messages;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !wandService.isWand(event.getItem())) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK
                && action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        event.setCancelled(true);

        if (!canUse(event.getPlayer())) {
            return;
        }
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            spellManager.selectNext(event.getPlayer());
        } else {
            spellManager.castSelected(event.getPlayer());
        }
    }

    @EventHandler
    public void onUseEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND
                || !wandService.isWand(event.getPlayer().getInventory().getItemInMainHand())) {
            return;
        }
        event.setCancelled(true);
        if (canUse(event.getPlayer())) {
            spellManager.castSelected(event.getPlayer());
        }
    }

    private boolean canUse(Player player) {
        if (player.hasPermission("magicplus.wand")) {
            return true;
        }
        messages.send(player, "no-permission");
        return false;
    }
}
