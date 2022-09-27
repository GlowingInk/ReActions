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
    Collection<? extends Placeholder> getPlaceholders();

    /**
     * Parse and process placeholder
     * @param text Text to parse
     * @param context RaContext of activation
     * @return Resolved placeholder or null if not found
     */
    static String process(String text, RaContext context) {
        for (Parser parser : Internal.values()) {
            String replacement = parser.parse(text, context);
            if (replacement == null) continue;
            return replacement;
        }
        return null;
    }

    enum Internal implements Parser {
        /**
         * For {@link Placeholder.Equal} placeholders
         * Checks whole placeholder
         */
        EQUAL {
            private final Map<String, Placeholder.Equal> placeholders = new HashMap<>();

            @Override
            public boolean put(Placeholder ph) {
                if (ph instanceof Placeholder.Equal equalPh) {
                    String id = ph.getName().toLowerCase(Locale.ROOT);
                    if (placeholders.containsKey(id)) return false;
                    placeholders.put(id, equalPh);
                    for (String alias : Utils.getAliases(ph))
                        placeholders.putIfAbsent(alias.toLowerCase(Locale.ROOT), equalPh);
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
            public Collection<Placeholder.Equal> getPlaceholders() {
                return placeholders.values();
            }
        },

        /**
         * For {@link Placeholder.Prefixed} placeholders
         * Checks placeholder if it has "%prefix:text%" format
         */
        PREFIXED {
            private final Map<String, Placeholder.Prefixed> placeholders = new HashMap<>();

            @Override
            public boolean put(Placeholder ph) {
                if (ph instanceof Placeholder.Prefixed prefixedPh) {
                    String prefix = ph.getName().toLowerCase(Locale.ROOT);
                    if (placeholders.containsKey(prefix)) return false;
                    placeholders.put(prefix, prefixedPh);
                    for (String alias : Utils.getAliases(ph))
                        placeholders.putIfAbsent(alias.toLowerCase(Locale.ROOT), prefixedPh);
                    return true;
                }
                return false;
            }

            @Override
            public String parse(String text, RaContext context) {
                String[] split = text.split(":", 2);
                if (split.length == 1 || split[1].isEmpty()) return null;
                String prefix = split[0].toLowerCase(Locale.ROOT);
                Placeholder ph = placeholders.get(prefix);
                if (ph == null) return null;
                return ph.processPlaceholder(context, prefix, split[1]);
            }

            @Override
            public Collection<Placeholder.Prefixed> getPlaceholders() {
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
            public Collection<Placeholder> getPlaceholders() {
                return placeholders;
            }
        }

    }
}
