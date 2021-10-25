package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.data.RaContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Alias({"TIME_INGAME", "curtime", "TIME_SERVER", "servertime"})
public class PlaceholderTime implements Placeholder.Equal {
    @Override
    public @NotNull String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String param) {
        Player player = context.getPlayer();
        return switch (key) {
            case "time", "time_server", "servertime" -> TimeUtils.fullTimeToString(System.currentTimeMillis(), param.isEmpty() ? "dd-MM-YYYY HH:mm:ss" : param);
            case "TIME_INGAME", "curtime" -> TimeUtils.formattedIngameTime((player == null ? Bukkit.getWorlds().get(0).getTime() : player.getWorld().getTime()), false);
            default -> null;
        };
    }

    @Override
    public @NotNull String getId() {
        return "time";
    }
}
