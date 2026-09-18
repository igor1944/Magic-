package ru.magicplus.spell;

import java.util.Collections;
import java.util.Map;

/** Результат попытки применения заклинания. */
public record CastResult(boolean success, String messageKey, Map<String, String> placeholders) {
    public static CastResult success(String messageKey) {
        return new CastResult(true, messageKey, Collections.emptyMap());
    }

    public static CastResult success(String messageKey, Map<String, String> placeholders) {
        return new CastResult(true, messageKey, placeholders);
    }

    public static CastResult failure(String messageKey) {
        return new CastResult(false, messageKey, Collections.emptyMap());
    }

    public static CastResult failure(String messageKey, Map<String, String> placeholders) {
        return new CastResult(false, messageKey, placeholders);
    }
}
