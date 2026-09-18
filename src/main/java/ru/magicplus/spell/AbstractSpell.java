package ru.magicplus.spell;

import org.bukkit.configuration.ConfigurationSection;
import ru.magicplus.MagicPlusPlugin;
import ru.magicplus.util.ColorUtil;

/** Базовый класс, читающий общие параметры заклинания из config.yml. */
public abstract class AbstractSpell implements Spell {
    protected final MagicPlusPlugin plugin;
    private final String id;
    private final ConfigurationSection settings;

    protected AbstractSpell(MagicPlusPlugin plugin, String id) {
        this.plugin = plugin;
        this.id = id;
        this.settings = plugin.getConfig().getConfigurationSection("spells." + id);
        if (settings == null) {
            throw new IllegalStateException("Отсутствует секция spells." + id + " в config.yml");
        }
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public String displayName() {
        return ColorUtil.color(settings.getString("display-name", id));
    }

    @Override
    public String description() {
        return ColorUtil.color(settings.getString("description", ""));
    }

    @Override
    public double manaCost() {
        return Math.max(0.0, settings.getDouble("mana-cost", 0.0));
    }

    @Override
    public long cooldownMillis() {
        return Math.max(0L, Math.round(settings.getDouble("cooldown-seconds", 0.0) * 1000.0));
    }

    protected double number(String key, double defaultValue) {
        return settings.getDouble(key, defaultValue);
    }

    protected int integer(String key, int defaultValue) {
        return settings.getInt(key, defaultValue);
    }

    protected boolean bool(String key, boolean defaultValue) {
        return settings.getBoolean(key, defaultValue);
    }
}
