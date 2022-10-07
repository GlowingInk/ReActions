package me.fromgate.reactions.util.item.resolvers;

import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
        return new NameInst(ChatColor.translateAlternateColorCodes('&', value), value, regex);
    }

    @Override
    public @Nullable MetaResolver.Instance fromItem(@NotNull ItemMeta meta) {
        if (regex || !meta.hasDisplayName()) return null;
        String name = meta.getDisplayName();
        return new NameInst(name, name.replace(ChatColor.COLOR_CHAR, '&'), false);
    }

    private static final class NameInst implements Instance {
        private final String value;
        private final String decolored;
        private final Pattern namePattern;

        private NameInst(@NotNull String value, @NotNull String decolored, boolean regex) {
            this.value = value;
            this.decolored = decolored;
            if (regex) {
                Pattern pattern;
                try {
                    pattern = Pattern.compile(value);
                    // TODO: Log error
                } catch (PatternSyntaxException ex) {
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
        public @NotNull String getName() {
            return namePattern != null
                    ? "name-regex"
                    : "name";
        }

        @Override
        public @NotNull String asString() {
            return decolored;
        }
    }
}
