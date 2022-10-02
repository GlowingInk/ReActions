package me.fromgate.reactions.util.item.resolvers;

import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class NameResolver implements MetaResolver {
    private final boolean regex;

    public NameResolver(boolean regex) {
        this.regex = regex;
    }

    @Override
    public @NotNull String getName() {
        return regex
                ? "name-regex"
                : "name";
    }

    @Override
    public @NotNull MetaResolver.Instance fromString(@NotNull String value) {
        return new Name(value, regex);
    }

    @Override
    public @Nullable MetaResolver.Instance fromItem(@NotNull ItemMeta meta) {
        if (regex || !meta.hasDisplayName()) return null;
        return new Name(meta.getDisplayName(), false);
    }

    private static final class Name implements Instance {
        private final String value;
        private final Pattern namePattern;

        private Name(@NotNull String value, boolean regex) {
            this.value = value;
            if (regex) {
                Pattern pattern;
                try {
                    pattern = Pattern.compile(value, Pattern.UNICODE_CASE);
                } catch (Exception ex) {
                    pattern = Pattern.compile(Pattern.quote(value));
                }
                this.namePattern = pattern;
            } else {
                this.namePattern = null;
            }
        }

        @Override
        public void apply(@NotNull ItemMeta meta) {
            meta.setDisplayName(value);
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (namePattern != null) {
                String name = meta.hasDisplayName() ? meta.getDisplayName() : "";
                return namePattern.matcher(name).matches();
            }
            return value.isEmpty() ?
                    !meta.hasDisplayName() :
                    value.equals(meta.getDisplayName());
        }

        @Override
        public @NotNull String asString() {
            return value;
        }
    }
}
