package me.fromgate.reactions.util.item.resolvers;

import me.fromgate.reactions.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LoreResolver implements MetaResolver {
    private final boolean regex;

    public LoreResolver(boolean regex) {
        this.regex = regex;
    }

    @Override
    public @NotNull String getName() {
        return regex
                ? "lore-regex"
                : "lore";
    }

    @Override
    public @NotNull MetaResolver.Instance fromString(@NotNull String value) {
        List<String> lore = value.isEmpty() || regex
                ? Collections.emptyList()
                : Utils.literalSplit(value, "\\n");
        return new LoreInst(ChatColor.translateAlternateColorCodes('&', value), lore, value, regex);
    }

    @Override
    public @Nullable MetaResolver.Instance fromItem(@NotNull ItemMeta meta) {
        if (!meta.hasLore() || regex) return null;
        List<String> lore = meta.getLore();
        String value = String.join("\\n", lore);
        return new LoreInst(value, lore, value.replace(ChatColor.COLOR_CHAR, '&'), false);
    }

    private static final class LoreInst implements Instance {
        private final List<String> lore;
        private final Pattern lorePattern;
        private final String decolored;

        private LoreInst(@NotNull String value, @NotNull List<String> lore, @NotNull String decolored, boolean regex) {
            this.decolored = decolored;
            if (regex) {
                this.lore = null;
                Pattern pattern;
                try {
                    pattern = Pattern.compile(value, Pattern.UNICODE_CASE);
                } catch (PatternSyntaxException ex) {
                    pattern = Pattern.compile(Pattern.quote(value));
                }
                this.lorePattern = pattern;
            } else {
                this.lore = lore;
                this.lorePattern = null;
            }
        }

        @Override
        public void apply(@NotNull ItemMeta meta) {
            meta.setLore(lore);
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (lorePattern != null) {
                String name = meta.hasLore() ? String.join("\\n", meta.getLore()) : "";
                return lorePattern.matcher(name).matches();
            }
            return lore.isEmpty() ?
                    !meta.hasLore() :
                    Objects.equals(lore, meta.getLore());
        }

        @Override
        public @NotNull String getName() {
            return lorePattern != null
                    ? "lore-regex"
                    : "lore";
        }

        @Override
        public @NotNull String asString() {
            return decolored;
        }
    }
}
