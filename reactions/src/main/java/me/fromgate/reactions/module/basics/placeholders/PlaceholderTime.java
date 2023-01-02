package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.alias.Aliases;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.fromgate.reactions.util.TimeUtils.formatIngameTime;
import static me.fromgate.reactions.util.TimeUtils.formatTime;

@Aliases({"TIME_INGAME", "curtime", "TIME_SERVER", "servertime"})
public class PlaceholderTime implements Placeholder.Keyed {
    @Override
    public @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String param) {
        Player player = context.getPlayer();
        return switch (key) {
            case "time", "time_server", "servertime" -> param.isEmpty() ? formatTime(System.currentTimeMillis()) : TimeUtils.formatTime(System.currentTimeMillis(), param);
            case "TIME_INGAME", "curtime" -> player == null ? formatIngameTime() : TimeUtils.formatIngameTime(player.getWorld().getTime(), false);
            default -> null;
        };
    }

    @Override
    public @NotNull String getName() {
        return "time";
    }
}
