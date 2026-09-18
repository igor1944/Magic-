package ru.magicplus.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;

/** Общие методы для цветов и отображения чисел. */
public final class ColorUtil {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.#");

    private ColorUtil() {
    }

    public static String color(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static Component component(String text) {
        return LEGACY.deserialize(color(text));
    }

    public static String number(double value) {
        synchronized (NUMBER_FORMAT) {
            return NUMBER_FORMAT.format(value);
        }
    }
}
