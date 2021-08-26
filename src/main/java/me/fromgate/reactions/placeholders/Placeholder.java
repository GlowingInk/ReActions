package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.util.data.RaContext;

@FunctionalInterface
public interface Placeholder {
    /**
     * Process this placeholder
     * @param context Context of activation
     * @param key Key of placeholder(e.g. %var:test% - var) in lower case
     * @param text Text of placeholder(e.g. %var:test% - test)
     * @return Processed placeholder
     */
    String processPlaceholder(RaContext context, String key, String text);

    // TODO: boolean requiresPlayer

    interface Equal extends Placeholder {
        /**
         * Get all the ids for this placeholder
         * @return Ids of placeholder
         */
        String getId();
    }

    interface Prefixed extends Placeholder {
        /**
         * Get all the prefixes for this placeholder
         * @return Prefixes of placeholder
         */
        String getPrefix();
    }
}
