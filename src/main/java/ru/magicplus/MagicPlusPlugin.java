package ru.magicplus;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.magicplus.command.MagicCommand;
import ru.magicplus.config.MessageService;
import ru.magicplus.listener.PlayerListener;
import ru.magicplus.listener.WandListener;
import ru.magicplus.mana.ManaManager;
import ru.magicplus.spell.SpellManager;
import ru.magicplus.task.ActionBarTask;
import ru.magicplus.task.ManaRegenerationTask;
import ru.magicplus.wand.WandService;

import java.util.ArrayList;
import java.util.List;

public final class MagicPlusPlugin extends JavaPlugin {
    private final List<BukkitTask> repeatingTasks = new ArrayList<>();
    private MessageService messages;
    private ManaManager manaManager;
    private SpellManager spellManager;
    private WandService wandService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        messages = new MessageService(this);
        manaManager = new ManaManager(this);
        spellManager = new SpellManager(this, manaManager, messages);
        wandService = new WandService(this, messages);

        MagicCommand magicCommand = new MagicCommand(
                this, manaManager, spellManager, wandService, messages);
        PluginCommand command = getCommand("magic");
        if (command == null) {
            getLogger().severe("Команда magic отсутствует в plugin.yml. Плагин будет выключен.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(magicCommand);
        command.setTabCompleter(magicCommand);

        getServer().getPluginManager().registerEvents(
                new PlayerListener(this, manaManager, spellManager, wandService), this);
        getServer().getPluginManager().registerEvents(
                new WandListener(wandService, spellManager, messages), this);

        for (Player player : getServer().getOnlinePlayers()) {
            manaManager.load(player);
            spellManager.ensureSelection(player);
        }
        startTasks();
        getLogger().info("Magic+ включён. Система маны и заклинаний готова!");
    }

    @Override
    public void onDisable() {
        cancelTasks();
        if (manaManager != null) {
            manaManager.saveAll();
        }
        getLogger().info("Magic+ выключен. Данные игроков сохранены.");
    }

    public void reloadPlugin() {
        reloadConfig();
        messages.reload();
        spellManager.reload();
        for (Player player : getServer().getOnlinePlayers()) {
            spellManager.ensureSelection(player);
        }
        cancelTasks();
        startTasks();
    }

    private void startTasks() {
        if (getConfig().getBoolean("mana.regeneration.enabled", true)) {
            long regenerationPeriod = Math.max(1L,
                    getConfig().getLong("mana.regeneration.interval-ticks", 20L));
            repeatingTasks.add(new ManaRegenerationTask(this, manaManager)
                    .runTaskTimer(this, regenerationPeriod, regenerationPeriod));
        }
        if (getConfig().getBoolean("mana.action-bar.enabled", true)) {
            long actionBarPeriod = Math.max(1L,
                    getConfig().getLong("mana.action-bar.update-interval-ticks", 10L));
            repeatingTasks.add(new ActionBarTask(this, manaManager, spellManager)
                    .runTaskTimer(this, 1L, actionBarPeriod));
        }
        repeatingTasks.add(getServer().getScheduler().runTaskTimer(
                this, manaManager::saveAll, 6000L, 6000L));
    }

    private void cancelTasks() {
        for (BukkitTask task : repeatingTasks) {
            task.cancel();
        }
        repeatingTasks.clear();
    }
}
