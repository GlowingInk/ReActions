package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.data.RaContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.fromgate.reactions.util.TimeUtils.formattedIngameTime;
import static me.fromgate.reactions.util.TimeUtils.fullTimeToString;

@Aliases({"TIME_INGAME", "curtime", "TIME_SERVER", "servertime"})
public class PlaceholderTime implements Placeholder.Equal {
    @Override
    public @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String param) {
        Player player = context.getPlayer();
        return switch (key) {
            case "time", "time_server", "servertime" -> param.isEmpty() ? fullTimeToString(System.currentTimeMillis()) : fullTimeToString(System.currentTimeMillis(), param);
            case "TIME_INGAME", "curtime" -> player == null ? formattedIngameTime() : formattedIngameTime(player.getWorld().getTime(), false);
            default -> null;
        };
    }

    @Override
    public @NotNull String getId() {
        return "time";
    }
}
