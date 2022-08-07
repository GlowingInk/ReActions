package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.externals.placeholderapi.RaPlaceholderAPI;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderPAPI implements Placeholder {
    @Override
    public @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String text) {
        String backup = "%" + text + "%";
        String result = RaPlaceholderAPI.processPlaceholder(context.getPlayer(), backup);
        return backup.equals(result) ? null : result;
    }

    @Override
    public @NotNull String getBasicName() {
        return "placeholderapi";
    }
}
