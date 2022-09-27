package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.RaContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Placeholder {
    /**
     * Process this placeholder
     * @param context Context of activation
     * @param key Key of placeholder(e.g. %var:test% - var) in lower case
     * @param text Text of placeholder(e.g. %var:test% - test)
     * @return Processed placeholder
     */
    @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String text);

    @NotNull String getName();

    // TODO: boolean requiresPlayer

    interface Equal extends Placeholder {
    }

    interface Prefixed extends Placeholder {
    }
}
