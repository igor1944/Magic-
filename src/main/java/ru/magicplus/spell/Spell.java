package ru.magicplus.spell;

import org.bukkit.entity.Player;

/** Контракт отдельного заклинания. Новые заклинания удобно добавлять отдельными классами. */
public interface Spell {
    String id();

    String displayName();

    String description();

    double manaCost();

    long cooldownMillis();

    CastResult cast(Player caster, String explicitTarget);
}
