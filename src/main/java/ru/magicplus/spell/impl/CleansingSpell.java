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
import java.util.Set;

public final class CleansingSpell extends AbstractSpell {
    private static final Set<PotionEffectType> NEGATIVE_EFFECTS = Set.of(
            PotionEffectType.BLINDNESS,
            PotionEffectType.CONFUSION,
            PotionEffectType.HUNGER,
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.UNLUCK,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER,
            PotionEffectType.BAD_OMEN,
            PotionEffectType.LEVITATION
    );

    public CleansingSpell(MagicPlusPlugin plugin) {
        super(plugin, "cleansing");
    }

    @Override
    public CastResult cast(Player caster, String explicitTarget) {
        double range = Math.max(1.0, number("range", 12.0));
        LivingEntity target = Targeting.find(plugin, new Targeting.MagicTargetOptions(caster, explicitTarget, range, true));
        if (target == null && explicitTarget != null && !explicitTarget.isBlank()) {
            return CastResult.failure("spell-no-target");
        }
        if (target == null) {
            target = caster;
        }

        int removed = 0;
        for (PotionEffect effect : target.getActivePotionEffects()) {
            if (NEGATIVE_EFFECTS.contains(effect.getType())) {
                target.removePotionEffect(effect.getType());
                removed++;
            }
        }
        if (removed == 0) {
            return CastResult.failure("spell-no-negative-effects");
        }

        target.getWorld().spawnParticle(Particle.END_ROD, target.getEyeLocation(), 22, 0.55, 0.75, 0.55, 0.05);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.5f);
        return CastResult.success("spell-cleansed", Map.of(
                "target", target.getName(),
                "amount", Integer.toString(removed)
        ));
    }
}
