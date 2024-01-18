package fun.reactions.module.basic.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.TimeUtils;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static fun.reactions.util.TimeUtils.formatIngameTime;
import static fun.reactions.util.TimeUtils.formatTime;

@Aliased.Names({"TIME_INGAME", "curtime", "TIME_SERVER", "servertime"})
public class TimePlaceholders implements Placeholder {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String param) {
        Player player = env.getPlayer();
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
