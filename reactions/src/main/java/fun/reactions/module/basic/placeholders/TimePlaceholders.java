package fun.reactions.module.basic.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static fun.reactions.util.TimeUtils.formatIngameTime;
import static fun.reactions.util.TimeUtils.formatTime;

@Aliased.Names({"time_ingame", "curtime", "time_server", "servertime", "timestamp", "stamp"})
public class TimePlaceholders implements Placeholder { // TODO World selection
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String param) {
        Player player = env.getPlayer();
        return switch (key) {
            case "timestamp", "stamp" -> Long.toString(System.currentTimeMillis());
            case "time", "time_server", "servertime" -> param.isEmpty()
                    ? formatTime(System.currentTimeMillis())
                    : formatTime(System.currentTimeMillis(), param);
            case "time_ingame", "curtime" -> player == null
                    ? formatIngameTime()
                    : formatIngameTime(player.getWorld().getTime(), false);
            default -> null;
        };
    }

    @Override
    public @NotNull String getName() {
        return "time";
    }
}
