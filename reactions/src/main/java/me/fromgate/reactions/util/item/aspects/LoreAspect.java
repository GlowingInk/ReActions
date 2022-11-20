package me.fromgate.reactions.util.item.aspects;

import me.fromgate.reactions.util.Utils;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.lang.String.join;
import static org.bukkit.ChatColor.COLOR_CHAR;
import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class LoreAspect implements MetaAspect {
    private final boolean regex;

    public LoreAspect(boolean regex) {
        this.regex = regex;
    }

    @Override
    public @NotNull String getName() {
        return regex
                ? "lore-regex"
                : "lore";
    }

    @Override
    public @NotNull MetaAspect.Instance fromString(@NotNull String value) {
        if (value.isEmpty()) return LoreInst.EMPTY;
        return new LoreInst(
                value,
                translateAlternateColorCodes('&', value),
                regex
        );
    }

    @Override
    public @Nullable MetaAspect.Instance fromItem(@NotNull ItemMeta meta) {
        if (!meta.hasLore() || regex) return null;
        String value = join("\\n", meta.getLore());
        return new LoreInst(
                value.replace(COLOR_CHAR, '&'),
                value,
                false
        );
    }

    private static final class LoreInst implements Instance {
        public static final LoreInst EMPTY = new LoreInst("", "", false);

        private final String plain;
        private final String colored;
        private final Pattern lorePattern;

        private LoreInst(@NotNull String plain, @NotNull String colored, boolean regex) {
            this.plain = plain;
            this.colored = colored;
            if (regex) {
                Pattern pattern;
                try {
                    pattern = Pattern.compile(colored, Pattern.UNICODE_CASE);
                } catch (PatternSyntaxException ex) {
                    // TODO: Log error
                    pattern = Pattern.compile(Pattern.quote(colored));
                }
                this.lorePattern = pattern;
            } else {
                this.lorePattern = null;
            }
        }

        @Override
        public void apply(@NotNull ItemMeta meta) {
            meta.setLore(Utils.literalSplit(colored, "\\n"));
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (lorePattern != null) {
                return meta.hasLore() && lorePattern.matcher(join("\\n", meta.getLore())).matches();
            }
            return colored.isEmpty()
                    ? !meta.hasLore()
                    : meta.hasLore() && colored.equals(join("\\n", meta.getLore()));
        }

        @Override
        public @NotNull String getName() {
            return lorePattern != null
                    ? "lore-regex"
                    : "lore";
        }

        @Override
        public @NotNull String asString() {
            return plain;
        }
    }
}
