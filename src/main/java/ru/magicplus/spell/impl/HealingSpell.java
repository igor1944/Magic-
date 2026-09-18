package ru.magicplus.spell.impl;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.magicplus.MagicPlusPlugin;
import ru.magicplus.spell.AbstractSpell;
import ru.magicplus.spell.CastResult;
import ru.magicplus.util.ColorUtil;
import ru.magicplus.util.Targeting;

import java.util.Map;

public final class HealingSpell extends AbstractSpell {
    public HealingSpell(MagicPlusPlugin plugin) {
        super(plugin, "healing");
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

        double missing = Math.max(0.0, target.getMaxHealth() - target.getHealth());
        if (missing < 0.01) {
            return CastResult.failure("spell-health-full");
        }
        double restored = Math.min(Math.max(0.0, number("heal-amount", 8.0)), missing);
        target.setHealth(Math.min(target.getMaxHealth(), target.getHealth() + restored));
        target.getWorld().spawnParticle(Particle.HEART, target.getEyeLocation(), 8, 0.45, 0.45, 0.45, 0.05);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.65f, 1.6f);

        return CastResult.success("spell-healed", Map.of(
                "amount", ColorUtil.number(restored),
                "target", target.getName()
        ));
    }
}
