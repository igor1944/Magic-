package ru.magicplus.spell.impl;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import ru.magicplus.MagicPlusPlugin;
import ru.magicplus.spell.AbstractSpell;
import ru.magicplus.spell.CastResult;

public final class FireballSpell extends AbstractSpell {
    public FireballSpell(MagicPlusPlugin plugin) {
        super(plugin, "fireball");
    }

    @Override
    public CastResult cast(Player caster, String explicitTarget) {
        double speed = Math.max(0.2, number("speed", 1.35));
        SmallFireball fireball = caster.launchProjectile(SmallFireball.class);
        fireball.setVelocity(caster.getEyeLocation().getDirection().normalize().multiply(speed));
        fireball.setIsIncendiary(bool("incendiary", true));
        fireball.setYield((float) Math.max(0.0, number("yield", 0.0)));
        fireball.setShooter(caster);

        caster.getWorld().spawnParticle(Particle.FLAME, caster.getEyeLocation().add(caster.getEyeLocation().getDirection()),
                12, 0.12, 0.12, 0.12, 0.02);
        caster.getWorld().playSound(caster.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.9f);
        return CastResult.success("spell-fireball");
    }
}
