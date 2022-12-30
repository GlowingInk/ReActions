package me.fromgate.reactions.util.item.aspects;

import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.item.ItemUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToIntFunction;

import static me.fromgate.reactions.util.Utils.cutBuilder;

@Aliases({"enchants", "stored-enchantments", "stored-enchants"})
public class EnchantmentsAspect implements MetaAspect {
    @Override
    public @NotNull String getName() {
        return "enchantments";
    }

    @Override
    public @NotNull EnchantmentsAspect.EnchantmentsInst fromString(@NotNull String value) {
        if (value.isEmpty()) {
            return EnchantmentsInst.EMPTY;
        }
        String[] split = value.split(";");
        Map<Enchantment, Integer> enchantments = new HashMap<>(split.length);
        for (String enchValue : split) {
            enchValue = enchValue.trim();
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
            Enchantment enchantment = ItemUtils.searchByKey(enchKey, Enchantment::getByKey);
            if (enchantment == null) {
                enchantment = Enchantment.getByName(enchKey.toUpperCase(Locale.ROOT));
                if (enchantment == null) continue;
            }
            int level = NumberUtils.asInt(levelStr, 0);
            enchantments.put(enchantment, level > 0 ? level : null);
        }
        return new EnchantmentsInst(enchantments, value);
    }

    @Override
    public EnchantmentsInst fromItem(@NotNull ItemMeta meta) {
        Map<Enchantment, Integer> enchants =  meta instanceof EnchantmentStorageMeta enchantmentMeta
                ? enchantmentMeta.getStoredEnchants()
                : meta.getEnchants();
        if (!enchants.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                builder.append(entry.getKey().getKey().value());
                if (entry.getValue() != null) {
                    builder.append(':').append(entry.getValue());
                }
                builder.append(",");
            }
            return new EnchantmentsInst(enchants, cutBuilder(builder, 2));
        }
        return null;
    }

    private record EnchantmentsInst(@NotNull Map<Enchantment, Integer> enchantments, @NotNull String value) implements Instance {
        public static final EnchantmentsInst EMPTY = new EnchantmentsInst(Map.of(), "");

        @Override
        public void apply(@NotNull ItemMeta meta) {
            EnchantmentConsumer consumer = meta instanceof EnchantmentStorageMeta enchantmentMeta
                    ? (ench, level) -> enchantmentMeta.addStoredEnchant(ench, level, true)
                    : (ench, level) -> meta.addEnchant(ench, level, true);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                int level = entry.getValue() == null ? 1 : entry.getValue();
                consumer.accept(entry.getKey(), level);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (enchantments.isEmpty()) {
                return !meta.hasEnchants();
            } else {
                if (!meta.hasEnchants()) return false;
                ToIntFunction<Enchantment> levelSuppler = meta instanceof EnchantmentStorageMeta enchantmentMeta
                        ? enchantmentMeta::getStoredEnchantLevel
                        : meta::getEnchantLevel;
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    int metaLevel = levelSuppler.applyAsInt(entry.getKey());
                    Integer level = entry.getValue();
                    if (level == null ? metaLevel == 0 : metaLevel != level) {
                        return false;
                    }
                }
                return true;
            }
        }

        @Override
        public @NotNull String getName() {
            return "enchantments";
        }

        @Override
        public @NotNull String asString() {
            return value;
        }
    }

    @FunctionalInterface
    private interface EnchantmentConsumer {
        void accept(@NotNull Enchantment enchantment, int level);
    }
}