package fun.reactions.module.basics.placeholders;

import fun.reactions.logic.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TimestampPlaceholder implements Placeholder.Keyed {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String params) {
        return Long.toString(System.currentTimeMillis());
    }

    @Override
    public @NotNull String getName() {
        return "timestamp";
    }
}
