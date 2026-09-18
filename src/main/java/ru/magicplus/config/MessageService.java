package ru.magicplus.config;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.magicplus.MagicPlusPlugin;
import ru.magicplus.util.ColorUtil;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/** Загружает русские сообщения из отдельного messages.yml. */
public final class MessageService {
    private final MagicPlusPlugin plugin;
    private final File file;
    private YamlConfiguration messages;

    public MessageService(MagicPlusPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        reload();
    }

    public void reload() {
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String get(String key) {
        return get(key, Collections.emptyMap());
    }

    public String get(String key, Map<String, String> placeholders) {
        String text = messages.getString(key, "&cНе найдено сообщение: " + key);
        String prefix = messages.getString("prefix", "&8[&dMagic+&8] ");
        text = text.replace("{prefix}", prefix);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return ColorUtil.color(text);
    }

    public void send(CommandSender sender, String key) {
        sender.sendMessage(get(key));
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(get(key, placeholders));
    }
}
