package ru.magicplus.spell;

import org.bukkit.entity.Player;
import ru.magicplus.MagicPlusPlugin;
import ru.magicplus.config.MessageService;
import ru.magicplus.mana.ManaManager;
import ru.magicplus.mana.ManaProfile;
import ru.magicplus.spell.impl.CleansingSpell;
import ru.magicplus.spell.impl.DrainSpell;
import ru.magicplus.spell.impl.FireballSpell;
import ru.magicplus.spell.impl.HealingSpell;
import ru.magicplus.spell.impl.PoisonSpell;
import ru.magicplus.spell.impl.WeaknessSpell;
import ru.magicplus.util.ColorUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Реестр заклинаний, проверка маны, прав и времени восстановления. */
public final class SpellManager {
    private final MagicPlusPlugin plugin;
    private final ManaManager manaManager;
    private final MessageService messages;
    private final Map<String, Spell> spells = new LinkedHashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public SpellManager(MagicPlusPlugin plugin, ManaManager manaManager, MessageService messages) {
        this.plugin = plugin;
        this.manaManager = manaManager;
        this.messages = messages;
        reload();
    }

    public void reload() {
        spells.clear();
        registerIfEnabled("healing", new HealingSpell(plugin));
        registerIfEnabled("cleansing", new CleansingSpell(plugin));
        registerIfEnabled("fireball", new FireballSpell(plugin));
        registerIfEnabled("poison", new PoisonSpell(plugin));
        registerIfEnabled("weakness", new WeaknessSpell(plugin));
        registerIfEnabled("drain", new DrainSpell(plugin));
    }

    private void registerIfEnabled(String id, Spell spell) {
        if (plugin.getConfig().getBoolean("spells." + id + ".enabled", true)) {
            spells.put(id, spell);
        }
    }

    public void castSelected(Player player) {
        ensureSelection(player);
        cast(player, manaManager.get(player).getSelectedSpell(), null);
    }

    public void cast(Player player, String requestedId, String explicitTarget) {
        String id = normalize(requestedId);
        Spell spell = spells.get(id);
        if (spell == null) {
            messages.send(player, "spell-not-found", Map.of("id", requestedId));
            return;
        }
        if (!player.hasPermission("magicplus.spell." + id)) {
            messages.send(player, "no-permission");
            return;
        }

        ManaProfile profile = manaManager.get(player);
        if (!manaManager.has(player, spell.manaCost())) {
            messages.send(player, "spell-no-mana", Map.of(
                    "cost", ColorUtil.number(spell.manaCost()),
                    "mana", ColorUtil.number(profile.getMana())
            ));
            return;
        }

        long remaining = getRemainingCooldown(player, spell);
        if (remaining > 0L) {
            messages.send(player, "spell-cooldown", Map.of(
                    "seconds", ColorUtil.number(remaining / 1000.0)
            ));
            return;
        }

        CastResult result = spell.cast(player, explicitTarget);
        if (!result.success()) {
            messages.send(player, result.messageKey(), result.placeholders());
            return;
        }

        manaManager.consume(player, spell.manaCost());
        cooldowns.computeIfAbsent(player.getUniqueId(), ignored -> new HashMap<>())
                .put(spell.id(), System.currentTimeMillis() + spell.cooldownMillis());
        messages.send(player, result.messageKey(), result.placeholders());
    }

    private long getRemainingCooldown(Player player, Spell spell) {
        long readyAt = cooldowns.getOrDefault(player.getUniqueId(), Collections.emptyMap())
                .getOrDefault(spell.id(), 0L);
        return Math.max(0L, readyAt - System.currentTimeMillis());
    }

    public boolean select(Player player, String requestedId) {
        String id = normalize(requestedId);
        Spell spell = spells.get(id);
        if (spell == null) {
            messages.send(player, "spell-not-found", Map.of("id", requestedId));
            return false;
        }
        if (!player.hasPermission("magicplus.spell." + id)) {
            messages.send(player, "no-permission");
            return false;
        }
        manaManager.get(player).setSelectedSpell(id);
        messages.send(player, "spell-selected", Map.of("spell", spell.displayName()));
        return true;
    }

    public void selectNext(Player player) {
        List<Spell> available = getAvailable(player);
        if (available.isEmpty()) {
            messages.send(player, "no-permission");
            return;
        }
        String selected = manaManager.get(player).getSelectedSpell();
        int currentIndex = -1;
        for (int i = 0; i < available.size(); i++) {
            if (available.get(i).id().equals(selected)) {
                currentIndex = i;
                break;
            }
        }
        Spell next = available.get((currentIndex + 1) % available.size());
        manaManager.get(player).setSelectedSpell(next.id());
        messages.send(player, "spell-selected", Map.of("spell", next.displayName()));
    }

    public void ensureSelection(Player player) {
        ManaProfile profile = manaManager.get(player);
        Spell selected = spells.get(profile.getSelectedSpell());
        if (selected != null && player.hasPermission("magicplus.spell." + selected.id())) {
            return;
        }
        List<Spell> available = getAvailable(player);
        profile.setSelectedSpell(available.isEmpty() ? "" : available.get(0).id());
    }

    public String getSelectedDisplayName(Player player) {
        ensureSelection(player);
        Spell spell = spells.get(manaManager.get(player).getSelectedSpell());
        return spell == null ? ColorUtil.color("&7Нет заклинания") : spell.displayName();
    }

    public List<Spell> getAvailable(Player player) {
        List<Spell> result = new ArrayList<>();
        for (Spell spell : spells.values()) {
            if (player.hasPermission("magicplus.spell." + spell.id())) {
                result.add(spell);
            }
        }
        return result;
    }

    public List<String> getSpellIds() {
        return new ArrayList<>(spells.keySet());
    }

    public Spell getSpell(String id) {
        return spells.get(normalize(id));
    }

    public void clearCooldowns(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    private String normalize(String id) {
        return id == null ? "" : id.toLowerCase(Locale.ROOT);
    }
}
