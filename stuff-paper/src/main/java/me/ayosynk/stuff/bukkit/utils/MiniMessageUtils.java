package me.ayosynk.stuff.bukkit.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class MiniMessageUtils {

    private MiniMessageUtils() {}

    /**
     * Parses a MiniMessage-formatted string (including hex color tags) into an Adventure Component.
     */
    public static Component parse(String input) {
        if (input == null) {
            return Component.empty();
        }
        return MiniMessage.miniMessage().deserialize(input);
    }
}
