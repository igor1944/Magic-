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

public final class WeaknessSpell extends AbstractSpell {
    public WeaknessSpell(MagicPlusPlugin plugin) {
        super(plugin, "weakness");
    }

    @Override
    public CastResult cast(Player caster, String explicitTarget) {
        LivingEntity target = Targeting.find(plugin, new Targeting.MagicTargetOptions(
                caster, explicitTarget, Math.max(1.0, number("range", 16.0)), false));
        if (target == null) {
            return CastResult.failure("spell-no-target");
        }

        int duration = Math.max(1, integer("duration-seconds", 8)) * 20;
        int weakness = Math.max(0, integer("weakness-amplifier", 1));
        int slowness = Math.max(0, integer("slowness-amplifier", 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, weakness, false, true, true), true);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, slowness, false, true, true), true);
        target.getWorld().spawnParticle(Particle.SMOKE_LARGE, target.getEyeLocation(), 22, 0.5, 0.7, 0.5, 0.03);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.55f, 1.35f);
        return CastResult.success("spell-weakened", Map.of("target", target.getName()));
    }
}
