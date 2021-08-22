package me.fromgate.reactions.externals.placeholderapi;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@UtilityClass
@Deprecated
public class RaPlaceholderAPI {

    private boolean enabled = false;

    public void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            enabled = true;
            new RaPapiExpansion().register();
            Msg.logMessage("Connected to PlaceholderAPI");
        }
    }

    public String processPlaceholder(Player player, String text) {
        return enabled ? PlaceholderAPI.setPlaceholders(player, text) : text;
    }
}
