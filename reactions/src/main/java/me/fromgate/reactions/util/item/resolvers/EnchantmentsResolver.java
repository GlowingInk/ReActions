package me.fromgate.reactions.util.item.resolvers;

import me.fromgate.reactions.util.NumberUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static me.fromgate.reactions.util.Utils.cutBuilder;

public class EnchantmentsResolver implements MetaResolver {
    private static final Instance EMPTY = new Instance(Collections.emptyMap());

    @Override
    public @NotNull String getName() {
        return "enchantments";
    }

    @Override
    public @NotNull Instance fromString(@NotNull String key, @NotNull String value) {
        if (value.isEmpty()) {
            return EMPTY;
        }
        String[] split = value.split(";");
        Map<Enchantment, Integer> enchantments = new HashMap<>(split.length);
        for (String enchValue : split) {
            int index = enchValue.indexOf(':');
            String enchKey;
            String levelStr;
            if (index == -1) {
                enchKey = enchValue;
                levelStr = "";
            } else {
                enchKey = enchValue.substring(0, index);
                levelStr = enchValue.substring(index + 1);
            }
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchKey.toUpperCase(Locale.ROOT)));
            if (enchantment == null) continue;
            int level = NumberUtils.getInteger(levelStr, 0);
            enchantments.put(enchantment, level > 0 ? level : null);
        }
        return new Instance(enchantments);
    }

    @Override
    public Instance fromItem(@NotNull ItemMeta meta) {
        return new Instance(meta.getEnchants());
    }

    private record Instance(Map<Enchantment, Integer> enchantments) implements MetaResolver.Instance {
        @Override
        public void apply(@NotNull ItemMeta meta) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                int level = entry.getValue() == null ? 1 : entry.getValue();
                meta.addEnchant(entry.getKey(), level, true);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (enchantments.isEmpty()) {
                return !meta.hasEnchants();
            } else {
                if (!meta.hasEnchants()) return false;
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    int metaLevel = meta.getEnchantLevel(entry.getKey());
                    Integer level = entry.getValue();
                    if (level == null ? metaLevel == 0 : metaLevel != level) {
                        return false;
                    }
                }
                return true;
            }
        }

        @Override
        public @NotNull String asString() {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                builder.append(entry.getKey());
                if (entry.getValue() != null) {
                    builder.append(':').append(entry.getValue());
                }
                builder.append(';');
            }
            return cutBuilder(builder, 1);
        }
    }
}
