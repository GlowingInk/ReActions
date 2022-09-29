package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.RaContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Placeholder {
    /**
     * Process this placeholder
     * @param context Context of activation
     * @param key Key of placeholder(e.g. %var:test% - var) in lower case
     * @param params Text of placeholder(e.g. %var:test% - test)
     * @return Processed placeholder
     */
    @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String params);

    @NotNull String getName();

    // TODO: boolean requiresPlayer?

    interface Dynamic extends Placeholder {
        /**
         * Process this placeholder
         * @param context Context of activation
         * @param key Always {@link Placeholder#getName()}
         * @param phText Full placeholder(e.g. %temp_variable% - temp_variable)
         * @return Processed placeholder
         */
        @Override
        @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String phText);
    }

    interface Postprocess extends Placeholder {
        /**
         * Process this placeholder
         * @param context Context of activation
         * @param key Always {@link Placeholder#getName()}
         * @param fullText Full message text
         * @return Processed text
         */
        @Override
        @NotNull String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String fullText);
    }
}
