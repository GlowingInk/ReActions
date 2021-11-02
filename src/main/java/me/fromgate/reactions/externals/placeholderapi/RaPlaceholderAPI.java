package me.fromgate.reactions.externals.placeholderapi;

import me.clip.placeholderapi.PlaceholderAPI;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Deprecated
public final class RaPlaceholderAPI {

    private static boolean enabled = false;

    private RaPlaceholderAPI() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            enabled = true;
            new RaPapiExpansion().register();
            Msg.logMessage("Connected to PlaceholderAPI");
        }
    }

    public static String processPlaceholder(Player player, String text) {
        return enabled ? PlaceholderAPI.setPlaceholders(player, text) : text;
    }
}
