package ru.magicplus.spell.impl;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.magicplus.MagicPlusPlugin;
import ru.magicplus.spell.AbstractSpell;
import ru.magicplus.spell.CastResult;
import ru.magicplus.util.Targeting;

import java.util.Map;

public final class PoisonSpell extends AbstractSpell {
    public PoisonSpell(MagicPlusPlugin plugin) {
        super(plugin, "poison");
    }

    @Override
    public CastResult cast(Player caster, String explicitTarget) {
        LivingEntity target = Targeting.find(plugin, new Targeting.MagicTargetOptions(
                caster, explicitTarget, Math.max(1.0, number("range", 16.0)), false));
        if (target == null) {
            return CastResult.failure("spell-no-target");
        }

        int duration = Math.max(1, integer("duration-seconds", 7)) * 20;
        int amplifier = Math.max(0, integer("amplifier", 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, amplifier, false, true, true), true);
        target.getWorld().spawnParticle(Particle.SPELL_WITCH, target.getEyeLocation(), 24, 0.45, 0.65, 0.45, 0.08);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 0.8f, 0.7f);
        return CastResult.success("spell-poisoned", Map.of("target", target.getName()));
    }
}
