package ru.magicplus.mana;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.magicplus.MagicPlusPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/** Хранит, изменяет и сохраняет персональную ману игроков. */
public final class ManaManager {
    private final MagicPlusPlugin plugin;
    private final File playersFile;
    private final Map<UUID, ManaProfile> profiles = new HashMap<>();
    private YamlConfiguration storage;

    public ManaManager(MagicPlusPlugin plugin) {
        this.plugin = plugin;
        this.playersFile = new File(plugin.getDataFolder(), "players.yml");
        this.storage = YamlConfiguration.loadConfiguration(playersFile);
    }

    public ManaProfile load(Player player) {
        ManaProfile loaded = profiles.get(player.getUniqueId());
        if (loaded != null) {
            return loaded;
        }

        String path = "players." + player.getUniqueId();
        double defaultMax = Math.max(1.0, plugin.getConfig().getDouble("mana.default-max", 100.0));
        double max = storage.getDouble(path + ".max-mana", defaultMax);
        double initial = plugin.getConfig().getDouble("mana.initial", defaultMax);
        double mana = storage.contains(path + ".mana") ? storage.getDouble(path + ".mana") : initial;
        String selected = storage.getString(path + ".selected-spell", "healing");
        boolean wandReceived = storage.getBoolean(path + ".wand-received", false);

        ManaProfile profile = new ManaProfile(mana, max, selected, wandReceived);
        profiles.put(player.getUniqueId(), profile);
        return profile;
    }

    public ManaProfile get(Player player) {
        ManaProfile profile = profiles.get(player.getUniqueId());
        return profile == null ? load(player) : profile;
    }

    public boolean has(Player player, double amount) {
        return get(player).getMana() + 0.0001 >= amount;
    }

    public boolean consume(Player player, double amount) {
        if (!has(player, amount)) {
            return false;
        }
        ManaProfile profile = get(player);
        profile.setMana(profile.getMana() - Math.max(0.0, amount));
        return true;
    }

    public void add(Player player, double amount) {
        ManaProfile profile = get(player);
        profile.setMana(profile.getMana() + amount);
    }

    public void set(Player player, double amount) {
        get(player).setMana(amount);
    }

    public void setMax(Player player, double max) {
        get(player).setMaxMana(max);
    }

    public void save(Player player, boolean unload) {
        ManaProfile profile = profiles.get(player.getUniqueId());
        if (profile == null) {
            return;
        }
        String path = "players." + player.getUniqueId();
        storage.set(path + ".name", player.getName());
        storage.set(path + ".mana", profile.getMana());
        storage.set(path + ".max-mana", profile.getMaxMana());
        storage.set(path + ".selected-spell", profile.getSelectedSpell());
        storage.set(path + ".wand-received", profile.isWandReceived());
        if (unload) {
            profiles.remove(player.getUniqueId());
        }
        saveFile();
    }

    public void saveAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ManaProfile profile = profiles.get(player.getUniqueId());
            if (profile == null) {
                continue;
            }
            String path = "players." + player.getUniqueId();
            storage.set(path + ".name", player.getName());
            storage.set(path + ".mana", profile.getMana());
            storage.set(path + ".max-mana", profile.getMaxMana());
            storage.set(path + ".selected-spell", profile.getSelectedSpell());
            storage.set(path + ".wand-received", profile.isWandReceived());
        }
        saveFile();
    }

    private void saveFile() {
        try {
            storage.save(playersFile);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить players.yml", exception);
        }
    }
}
