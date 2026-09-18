package ru.magicplus.spell.impl;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.magicplus.MagicPlusPlugin;
import ru.magicplus.spell.AbstractSpell;
import ru.magicplus.spell.CastResult;
import ru.magicplus.util.Targeting;

import java.util.Map;

public final class DrainSpell extends AbstractSpell {
    public DrainSpell(MagicPlusPlugin plugin) {
        super(plugin, "drain");
    }

    @Override
    public CastResult cast(Player caster, String explicitTarget) {
        LivingEntity target = Targeting.find(plugin, new Targeting.MagicTargetOptions(
                caster, explicitTarget, Math.max(1.0, number("range", 12.0)), false));
        if (target == null) {
            return CastResult.failure("spell-no-target");
        }

        double damage = Math.max(0.0, number("damage", 6.0));
        double heal = Math.max(0.0, number("heal-amount", 5.0));
        target.damage(damage, caster);
        caster.setHealth(Math.min(caster.getMaxHealth(), caster.getHealth() + heal));

        target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getEyeLocation(), 10, 0.35, 0.45, 0.35, 0.08);
        caster.getWorld().spawnParticle(Particle.HEART, caster.getEyeLocation(), 5, 0.3, 0.4, 0.3, 0.03);
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 0.8f, 0.65f);
        return CastResult.success("spell-drained", Map.of("target", target.getName()));
    }
}
