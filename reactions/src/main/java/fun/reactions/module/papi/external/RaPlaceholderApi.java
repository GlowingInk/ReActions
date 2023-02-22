package fun.reactions.module.papi.external;

import fun.reactions.util.message.Msg;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Deprecated
public final class RaPlaceholderApi {

    private static boolean enabled = false;

    private RaPlaceholderApi() {}

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
