package fun.reactions.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Named;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Placeholder extends Named {
    /**
     * Process this placeholder
     * @param env Context of activation
     * @param key Key of placeholder(e.g. %var:test% - var) in lower case
     * @param params Text of placeholder(e.g. %var:test% - test)
     * @return Processed placeholder
     */
    @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String params);

    interface Dynamic extends Placeholder {
        @Override
        default @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String params) {
            return resolve(env, params);
        }

        /**
         * Process this placeholder
         * @param env Context of activation
         * @param phText Full placeholder(e.g. %my_variable% - my_variable)
         * @return Processed placeholder
         */
        @Nullable String resolve(@NotNull Environment env, @NotNull String phText);
    }
}
