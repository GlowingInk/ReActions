package me.fromgate.reactions.util.item;

import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.item.resolvers.*;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class VirtualItem {
    private static final Parameters AIR_PARAMS = Parameters.fromMap(Collections.singletonMap("type", "AIR"));
    public static final VirtualItem AIR = new VirtualItem(null, -1, Collections.emptyList(), AIR_PARAMS);

    private static final Map<String, MetaResolver> RESOLVERS_MAP = new LinkedHashMap<>(); // TODO: Registry
    private static final List<MetaResolver> RESOLVERS = new ArrayList<>();
    static {
        registerResolver(new BookResolver(BookResolver.Type.PAGES));
        registerResolver(new BookResolver(BookResolver.Type.TITLE));
        registerResolver(new BookResolver(BookResolver.Type.AUTHOR));
        registerResolver(new ColorResolver());
        registerResolver(new DurabilityResolver());
        registerResolver(new EnchantmentsResolver());
        registerResolver(new FireworkResolver(true));
        registerResolver(new FireworkResolver(false));
        registerResolver(new HeadResolver());
        registerResolver(new LoreResolver(true));
        registerResolver(new LoreResolver(false));
        registerResolver(new ModelResolver());
        registerResolver(new NameResolver(true));
        registerResolver(new NameResolver(false));
        registerResolver(new PotionResolver(true));
        registerResolver(new PotionResolver(false));
    }
    private static void registerResolver(@NotNull MetaResolver resolver) {
        RESOLVERS_MAP.put(resolver.getName().toLowerCase(Locale.ROOT), resolver);
        RESOLVERS.add(resolver);
        for (String alias : Utils.getAliases(resolver)) {
            RESOLVERS_MAP.putIfAbsent(alias.toLowerCase(Locale.ROOT), resolver);
        }
    }

    private final Material type;
    private final int amount;
    private final @NotNull List<MetaResolver.Instance> resolvers;

    private boolean itemGenerated;
    private ItemStack itemValue;
    private Parameters paramsValue;

    private VirtualItem(
            @Nullable Material type,
            int amount,
            @NotNull List<MetaResolver.Instance> resolvers,
            @NotNull Parameters params
    ) {
        this.type = type;
        this.amount = amount;
        this.resolvers = resolvers;
        this.paramsValue = params;
    }

    private VirtualItem(
            @Nullable Material type,
            int amount,
            @NotNull List<MetaResolver.Instance> resolvers,
            @NotNull ItemStack item
    ) {
        this.type = type;
        this.amount = amount;
        this.resolvers = resolvers;
        this.itemGenerated = true;
        this.itemValue = item;
    }

    public @Nullable ItemStack asItem() {
        return asItem(true);
    }

    private @Nullable ItemStack asItem(boolean clone) {
        if (!itemGenerated) {
            itemGenerated = true;
            if (type == null || !type.isItem()) return null;
            itemValue = new ItemStack(type);
            if (!type.isEmpty()) {
                itemValue.setAmount(Math.max(amount, 1));
                if (!resolvers.isEmpty()) {
                    ItemMeta meta = itemValue.getItemMeta();
                    resolvers.forEach(resolver -> resolver.apply(meta));
                    itemValue.setItemMeta(meta);
                }
            }
        }
        return itemValue == null
                ? null
                : clone ? itemValue.clone() : itemValue;
    }

    public @NotNull Parameters asParams() {
        if (paramsValue != null) {
            return paramsValue;
        }
        if (type == null) {
            return paramsValue = AIR_PARAMS;
        }
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("type", type.name());
        paramsMap.put("amount", Integer.toString(amount));
        for (MetaResolver.Instance resolver : resolvers) {
            paramsMap.put(resolver.getName(), resolver.asString());
        }
        return paramsValue = Parameters.fromMap(paramsMap);
    }

    public @NotNull String asString() {
        return asParams().toString();
    }

    public boolean isSimilar(@Nullable ItemStack compared) {
        if (type == null || type.isEmpty()) {
            return !ItemUtils.isExist(compared);
        } else if (!ItemUtils.isExist(compared)) {
            return false;
        }
        if (type != compared.getType() || amount > compared.getAmount()) {
            return false;
        }
        if (!resolvers.isEmpty()) {
            if (!compared.hasItemMeta()) return false;
            ItemMeta meta = compared.getItemMeta();
            for (MetaResolver.Instance resolver : resolvers) {
                if (!resolver.isSimilar(meta)) return false;
            }
        }
        return true;
    }

    public static @NotNull VirtualItem fromItem(@Nullable ItemStack item) {
        if (item == null || item.getType().isEmpty()) {
            return AIR;
        }
        List<MetaResolver.Instance> resolvers;
        if (!item.hasItemMeta()) {
            resolvers = Collections.emptyList();
        } else {
            resolvers = new ArrayList<>();
            ItemMeta meta = item.getItemMeta();
            for (MetaResolver resolver : RESOLVERS) {
                MetaResolver.Instance resolverInst = resolver.fromItem(meta);
                if (resolverInst != null) resolvers.add(resolverInst);
            }
            if (resolvers.isEmpty()) {
                resolvers = Collections.emptyList();
            }
        }
        return new VirtualItem(
                item.getType(),
                item.getAmount(),
                resolvers,
                item
        );
    }

    public static @NotNull VirtualItem fromString(@NotNull String paramsStr) {
        return fromParameters(Parameters.fromString(paramsStr));
    }

    public static @NotNull VirtualItem fromParameters(@NotNull Parameters params) {
        List<MetaResolver.Instance> resolvers = new ArrayList<>();
        Material type = null;
        int amount = -1;
        boolean regex = params.getBoolean("regex", false);
        for (String key : params) {
            key = key.toLowerCase(Locale.ROOT);
            switch (key) {
                case "type" -> type = Material.getMaterial(params.getString(key));
                case "name", "lore" -> {
                    MetaResolver resolver = RESOLVERS_MAP.get(regex ? key + "-regex" : key);
                    if (resolver == null) continue; // Literally how
                    resolvers.add(resolver.fromString(params.getString(key)));
                }
                case "amount" -> amount = params.getInteger(key);
                default -> {
                    MetaResolver resolver = RESOLVERS_MAP.get(key);
                    if (resolver == null) continue;
                    resolvers.add(resolver.fromString(params.getString(key)));
                }
            }
        }
        return new VirtualItem(
                type,
                amount,
                resolvers,
                params
        );
    }

    public static @Nullable ItemStack asItem(@NotNull String itemStr) {
        return fromString(itemStr).asItem(false);
    }

    public static @Nullable ItemStack asItem(@NotNull Parameters itemParams) {
        return fromParameters(itemParams).asItem(false);
    }

    public static @NotNull String asString(@Nullable ItemStack item) {
        return fromItem(item).asString();
    }

    public static @NotNull Parameters asParams(@Nullable ItemStack item) {
        return fromItem(item).asParams();
    }

    @Override
    public String toString() {
        return asString();
    }
}
