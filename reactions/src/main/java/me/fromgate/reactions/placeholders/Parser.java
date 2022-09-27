package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

interface Parser {
    boolean put(Placeholder ph);
    String parse(String text, RaContext context);
    boolean isEmpty();
    Collection<Placeholder> getPlaceholders();

    /**
     * Parse and process placeholder
     * @param text Text to parse
     * @param context RaContext of activation
     * @return Placeholder or %{@param text}% if not found
     */
    static String process(String text, final RaContext context) {
        for (Parser parser : Internal.values()) {
            String replacement = parser.parse(text, context);
            if (replacement == null) continue;
            return replacement;
        }
        return "%" + text + "%";
    }

    enum Internal implements Parser {
        /**
         * For {@link Placeholder.Equal} placeholders
         * Checks whole placeholder
         */
        EQUAL {
            private final Map<String, Placeholder> placeholders = new HashMap<>();

            @Override
            public boolean put(Placeholder ph) {
                if (ph instanceof Placeholder.Equal) {
                    String id = ((Placeholder.Equal)ph).getId().toLowerCase(Locale.ROOT);
                    if (placeholders.containsKey(id)) return false;
                    placeholders.put(id, ph);
                    for (String alias : Utils.getAliases(ph))
                        placeholders.putIfAbsent(alias.toLowerCase(Locale.ROOT), ph);
                    return true;
                }
                return false;
            }

            @Override
            public String parse(String text, RaContext context) {
                String key = text.toLowerCase(Locale.ROOT);
                Placeholder ph = placeholders.get(key);
                if (ph == null) return null;
                return ph.processPlaceholder(context, key, text);
            }

            @Override
            public boolean isEmpty() {
                return placeholders.isEmpty();
            }

            @Override
            public Collection<Placeholder> getPlaceholders() {
                return placeholders.values();
            }
        },

        /**
         * For {@link Placeholder.Prefixed} placeholders
         * Checks placeholder if it has "%prefix:text%" format
         */
        PREFIXED {
            private final Map<String, Placeholder> placeholders = new HashMap<>();

            @Override
            public boolean put(Placeholder ph) {
                if (ph instanceof Placeholder.Prefixed) {
                    String prefix = ((Placeholder.Prefixed)ph).getPrefix();
                    if (placeholders.containsKey(prefix)) return false;
                    placeholders.put(prefix, ph);
                    for (String alias : Utils.getAliases(ph))
                        placeholders.putIfAbsent(alias.toLowerCase(Locale.ROOT), ph);
                    return true;
                }
                return false;
            }

            @Override
            public String parse(String text, RaContext context) {
                String[] split = text.split(":", 2);
                String prefix = split[0].toLowerCase(Locale.ROOT);
                Placeholder ph = placeholders.get(prefix.toLowerCase(Locale.ROOT));
                if (ph == null) return null;
                return ph.processPlaceholder(context, prefix, split.length > 1 ? split[1] : "");
            }

            @Override
            public boolean isEmpty() {
                return placeholders.isEmpty();
            }

            @Override
            public Collection<Placeholder> getPlaceholders() {
                return placeholders.values();
            }
        },

        /**
         * For {@link Placeholder} placeholders without any other default interfaces
         * Just tries to check all stored placeholders - all the logic is inside of placeholder itself
         */
        SIMPLE {
            private final List<Placeholder> placeholders = new ArrayList<>();

            @Override
            public boolean put(Placeholder ph) {
                placeholders.add(ph);
                return true;
            }

            @Override
            public String parse(String text, RaContext context) {
                for (Placeholder ph : placeholders) {
                    String result = ph.processPlaceholder(context, text, text);
                    if (result != null) return result;
                }
                return null;
            }

            @Override
            public boolean isEmpty() {
                return placeholders.isEmpty();
            }

            @Override
            public Collection<Placeholder> getPlaceholders() {
                return placeholders;
            }
        }

    }
}
