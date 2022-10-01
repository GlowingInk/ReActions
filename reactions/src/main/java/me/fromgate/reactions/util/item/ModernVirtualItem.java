package me.fromgate.reactions.util.item;

import me.fromgate.reactions.util.item.resolvers.MetaResolver;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static me.fromgate.reactions.util.item.CheckingStrategy.getStrategy;

public record ModernVirtualItem(
        @Nullable Material type,
        @NotNull CheckingStrategy name,
        @NotNull CheckingStrategy lore,
        int amount, // TODO Make nullable to ignore amount
        @NotNull List<MetaResolver.Instance> resolvers
) {
    private static final Map<String, MetaResolver> RESOLVERS_MAP = new HashMap<>(); // TODO Fill the map
    private static final List<MetaResolver> RESOLVERS = new ArrayList<>(); // TODO Fill the list

    public static @NotNull ModernVirtualItem fromItem(@NotNull ItemStack item) {
        // TODO if (!item.hasItemMeta())
        List<MetaResolver.Instance> resolvers;
        String name = null;
        String lore = null;
        if (!item.hasItemMeta()) {
            resolvers = Collections.emptyList();
        } else {
            resolvers = new ArrayList<>();
            ItemMeta meta = item.getItemMeta();
            for (MetaResolver resolver : RESOLVERS) {
                MetaResolver.Instance resolverInst = resolver.fromItem(meta);
                resolvers.add(resolverInst);
            }
            name = meta.getDisplayName();
            lore = meta.hasLore() ? String.join("\\n", meta.getLore()) : null;
            if (resolvers.isEmpty()) {
                resolvers = Collections.emptyList();
            }
        }
        return new ModernVirtualItem(
                item.getType(),
                getStrategy(name, false),
                getStrategy(lore, false),
                item.getAmount(),
                resolvers
        );
    }

    public static @NotNull ModernVirtualItem fromString(@NotNull String paramsStr) {
        return fromParameters(Parameters.fromString(paramsStr));
    }

    public static @NotNull ModernVirtualItem fromParameters(@NotNull Parameters params) {
        List<MetaResolver.Instance> resolvers = new ArrayList<>();
        Material type = null;
        String name = null;
        String lore = null;
        boolean regexName = false;
        boolean regexLore = false;
        int amount = 1;
        for (String key : params) {
            key = key.toLowerCase(Locale.ROOT);
            switch (key) {
                case "type" -> type = Material.getMaterial(params.getString(key));
                case "name" -> name = params.getString(key);
                case "lore" -> lore = params.getString(key);
                case "regex" -> {
                    boolean value = params.getBoolean(key);
                    regexName = value;
                    regexLore = value;
                }
                case "regex-name" -> regexName = params.getBoolean(key);
                case "regex-lore" -> regexLore = params.getBoolean(key);
                case "amount" -> amount = params.getInteger(key);
                default -> {
                    MetaResolver resolver = RESOLVERS_MAP.get(key);
                    if (resolver == null) continue;
                    resolvers.add(resolver.fromString(params.getString(key)));
                }
            }
        }
        return new ModernVirtualItem(
                type,
                getStrategy(name, regexName),
                getStrategy(lore, regexLore),
                amount,
                resolvers
        );
    }
}
