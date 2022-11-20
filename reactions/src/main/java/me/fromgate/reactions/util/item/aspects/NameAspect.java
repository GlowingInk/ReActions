package me.fromgate.reactions.util.item.aspects;

import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class NameAspect implements MetaAspect {
    private final boolean regex;

    public NameAspect(boolean regex) {
        this.regex = regex;
    }

    @Override
    public @NotNull String getName() {
        return regex
                ? "name-regex"
                : "name";
    }

    @Override
    public @NotNull MetaAspect.Instance fromString(@NotNull String value) {
        if (value.isEmpty()) return NameInst.EMPTY;
        return new NameInst(ChatColor.translateAlternateColorCodes('&', value), value, regex);
    }

    @Override
    public @Nullable MetaAspect.Instance fromItem(@NotNull ItemMeta meta) {
        if (regex || !meta.hasDisplayName()) return null;
        String name = meta.getDisplayName();
        return new NameInst(name, name.replace(ChatColor.COLOR_CHAR, '&'), false);
    }

    private static final class NameInst implements Instance {
        public static NameInst EMPTY = new NameInst("", "", false);

        private final String colored;
        private final String plain;
        private final Pattern namePattern;

        private NameInst(@NotNull String colored, @NotNull String plain, boolean regex) {
            this.colored = colored;
            this.plain = plain;
            if (regex) {
                Pattern pattern;
                try {
                    pattern = Pattern.compile(colored, Pattern.UNICODE_CASE);
                } catch (PatternSyntaxException ex) {
                    // TODO: Log error
                    pattern = Pattern.compile(Pattern.quote(colored));
                }
                this.namePattern = pattern;
            } else {
                this.namePattern = null;
            }
        }

        @Override
        public void apply(@NotNull ItemMeta meta) {
            meta.setDisplayName(colored);
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (namePattern != null) {
                String name = meta.hasDisplayName() ? meta.getDisplayName() : "";
                return namePattern.matcher(name).matches();
            }
            return colored.isEmpty()
                    ? !meta.hasDisplayName()
                    : colored.equals(meta.getDisplayName());
        }

        @Override
        public @NotNull String getName() {
            return namePattern != null
                    ? "name-regex"
                    : "name";
        }

        @Override
        public @NotNull String asString() {
            return plain;
        }
    }
}
