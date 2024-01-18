package fun.reactions.module.basic.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TimestampPlaceholder implements Placeholder {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String params) {
        return Long.toString(System.currentTimeMillis());
    }

    @Override
    public @NotNull String getName() {
        return "timestamp";
    }
}
