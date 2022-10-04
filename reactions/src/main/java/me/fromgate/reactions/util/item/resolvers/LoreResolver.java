package me.fromgate.reactions.util.item.resolvers;

import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

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
        return new LoreInst(value, regex);
    }

    @Override
    public @Nullable MetaResolver.Instance fromItem(@NotNull ItemMeta meta) {
        if (regex || !meta.hasLore()) return null;
        return new LoreInst(meta.getLore(), false);
    }

    private static final class LoreInst implements Instance {
        private final List<String> lore;
        private final String value;
        private final Pattern lorePattern;

        private LoreInst(@NotNull String value, boolean regex) {
            this(
                    value,
                    value.isEmpty()
                            ? Collections.emptyList()
                            : List.of(value.split("\\\\n")),
                    regex
            );
        }

        private LoreInst(@NotNull List<String> lore, boolean regex) {
            this(
                    String.join("\\n", lore),
                    lore,
                    regex
            );
        }

        private LoreInst(@NotNull String value, @NotNull List<String> lore, boolean regex) {
            this.lore = lore;
            this.value = value;
            if (regex) {
                Pattern pattern;
                try {
                    pattern = Pattern.compile(value, Pattern.UNICODE_CASE);
                } catch (Exception ex) {
                    pattern = Pattern.compile(Pattern.quote(value));
                }
                this.lorePattern = pattern;
            } else {
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
                String name = meta.hasDisplayName() ? meta.getDisplayName() : "";
                return lorePattern.matcher(name).matches();
            }
            return lore.isEmpty() ?
                    !meta.hasDisplayName() :
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
            return value;
        }
    }
}
