package ru.magicplus.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import ru.magicplus.MagicPlusPlugin;

/** Поиск живой цели по нику или по направлению взгляда мага. */
public final class Targeting {
    private Targeting() {
    }

    public static LivingEntity find(MagicPlusPlugin plugin, MagicTargetOptions options) {
        Player caster = options.caster();
        String targetName = options.explicitTarget();
        if (targetName != null && !targetName.isBlank()) {
            Player explicit = Bukkit.getPlayerExact(targetName);
            if (explicit == null || explicit.isDead()) {
                return null;
            }
            if (!options.allowSelf() && explicit.getUniqueId().equals(caster.getUniqueId())) {
                return null;
            }
            if (!explicit.getWorld().equals(caster.getWorld())
                    || explicit.getLocation().distanceSquared(caster.getLocation()) > options.range() * options.range()) {
                return null;
            }
            return explicit;
        }

        Location eye = caster.getEyeLocation();
        double raySize = Math.max(0.1, plugin.getConfig().getDouble("targeting.ray-size", 0.55));
        RayTraceResult trace = caster.getWorld().rayTraceEntities(
                eye,
                eye.getDirection(),
                options.range(),
                raySize,
                entity -> isValid(entity, caster, options.allowSelf())
        );
        if (trace == null || !(trace.getHitEntity() instanceof LivingEntity living)) {
            return null;
        }
        return living;
    }

    private static boolean isValid(Entity entity, Player caster, boolean allowSelf) {
        return entity instanceof LivingEntity living
                && !living.isDead()
                && (allowSelf || !entity.getUniqueId().equals(caster.getUniqueId()));
    }

    public record MagicTargetOptions(Player caster, String explicitTarget, double range, boolean allowSelf) {
    }
}
