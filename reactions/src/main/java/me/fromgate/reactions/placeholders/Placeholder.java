package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.RaContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface Placeholder permits Placeholder.Keyed, Placeholder.Dynamic, Placeholder.Preprocess {
    @NotNull String getName();

    // TODO: boolean requiresPlayer?

    non-sealed interface Keyed extends Placeholder {
        /**
         * Process this placeholder
         * @param context Context of activation
         * @param key Key of placeholder(e.g. %var:test% - var) in lower case
         * @param params Text of placeholder(e.g. %var:test% - test)
         * @return Processed placeholder
         */
        @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String params);
    }

    non-sealed interface Dynamic extends Placeholder {
        /**
         * Process this placeholder
         * @param context Context of activation
         * @param phText Full placeholder(e.g. %temp_variable% - temp_variable)
         * @return Processed placeholder
         */
        @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String phText);
    }

    non-sealed interface Preprocess extends Placeholder {
        /**
         * Process this placeholder
         * @param context Context of activation
         * @param fullText Full message text
         * @return Processed text
         */
        @NotNull String processPlaceholder(@NotNull RaContext context, @NotNull String fullText);
    }
}
