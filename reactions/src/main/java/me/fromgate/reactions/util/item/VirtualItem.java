package me.fromgate.reactions.util.item;

import me.fromgate.reactions.util.NumberUtils;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VirtualItem {
    private static final Pattern SIMPLE_ITEM = Pattern.compile("([a-zA-Z_]+)(?::(\\d+))?(?:\\*(\\d+))?");

    /**
     * A VirtualItem that accepts only null or air ItemStacks
     */
    public static final VirtualItem AIR = new VirtualItem(Material.AIR, -1, Collections.emptyList(), Parameters.fromMap(Collections.singletonMap("type", "AIR")));
    /**
     * A VirtualItem that accepts any ItemStacks but null or air
     */
    public static final VirtualItem EMPTY = new VirtualItem(null, -1, Collections.emptyList(), Parameters.EMPTY);

    private static final Map<String, MetaResolver> RESOLVERS_MAP = new LinkedHashMap<>(); // TODO: Registry
    private static final List<MetaResolver> RESOLVERS = new ArrayList<>();
    static {
        registerResolver(new BannerResolver());
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
        registerResolver(new MapResolver(true));
        registerResolver(new MapResolver(false));
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

    private final @Nullable Material type;
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

    public @Nullable Material getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public @Nullable ItemStack asItem() {
        return asItem(true);
    }

    private @Nullable ItemStack asItem(boolean clone) {
        if (!itemGenerated) {
            itemGenerated = true;
            if (type == null || !type.isItem()) {
                itemValue = null;
            } else {
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
        }
        return itemValue == null
                ? null
                : clone ? itemValue.clone() : itemValue;
    }

    public @NotNull Parameters asParameters() {
        if (paramsValue != null) {
            return paramsValue;
        }
        Map<String, String> paramsMap = new LinkedHashMap<>();
        if (type != null) {
            paramsMap.put("type", type.name());
        }
        paramsMap.put("amount", Integer.toString(amount));
        for (MetaResolver.Instance resolver : resolvers) {
            paramsMap.put(resolver.getName(), resolver.asString());
        }
        return paramsValue = Parameters.fromMap(paramsMap);
    }

    public @NotNull String asString() {
        return asParameters().toString();
    }

    /**
     * Compare this item with real one.
     * If compared item is null or its type is AIR, expecting this item's type to be AIR.
     * If this item's type is specified, expecting compared item's type to be the same.
     * @param compared item to compare
     * @return is compared item conforms this item's type and resolvers
     */
    public boolean isSimilar(@Nullable ItemStack compared) {
        if (compared == null || compared.getType().isEmpty()) {
            return type != null && type.isEmpty();
        }
        if (type == null || compared.getType() == type) {
            if (!resolvers.isEmpty()) {
                ItemMeta meta = compared.getItemMeta();
                for (MetaResolver.Instance resolver : resolvers) {
                    if (!resolver.isSimilar(meta)) return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Generate a new VirtualItem from item.
     * Considers null as item type AIR.
     * @param item item to generate from
     * @return generated VirtualItem
     */
    public static @NotNull VirtualItem fromItem(@Nullable ItemStack item) {
        if (item == null || item.getType().isEmpty()) {
            return VirtualItem.AIR;
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
        return paramsStr.isEmpty()
                ? VirtualItem.EMPTY
                : fromParameters(Parameters.fromString(paramsStr));
    }

    public static @NotNull VirtualItem fromParameters(@NotNull Parameters params) {
        List<MetaResolver.Instance> resolvers = new ArrayList<>();
        Material type = null;
        int amount = -1;
        boolean regex = params.getBoolean("regex", false);
        for (String key : params) {
            key = key.toLowerCase(Locale.ROOT);
            switch (key) {
                case "item": {
                    Matcher matcher = SIMPLE_ITEM.matcher(params.getString(key));
                    if (!matcher.matches()) break;
                    type = ItemUtils.getMaterial(matcher.group(1));
                    if (!matcher.group(1).isEmpty()) {
                        resolvers.add(RESOLVERS_MAP.get("durability").fromString(matcher.group(1)));
                    }
                    if (!matcher.group(2).isEmpty()) {
                        amount = NumberUtils.getInteger(matcher.group(2), -1);
                    }
                    break;
                }
                case "type": type = params.get(key, ItemUtils::getMaterial, Material.AIR); break;
                case "amount": amount = params.getInteger(key); break;
                case "name": case "lore":
                    if (regex) key += "-regex";
                default:
                    MetaResolver resolver = RESOLVERS_MAP.get(key);
                    if (resolver == null) continue;
                    resolvers.add(resolver.fromString(params.getString(key)));
                    break;
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

    public static @NotNull Parameters asParameters(@Nullable ItemStack item) {
        return fromItem(item).asParameters();
    }

    public static boolean isSimilar(@NotNull String itemStr, @Nullable ItemStack compared) {
        return fromString(itemStr).isSimilar(compared);
    }

    public static boolean isSimilar(@NotNull Parameters itemParams, @Nullable ItemStack compared) {
        return fromParameters(itemParams).isSimilar(compared);
    }

    @Override
    public String toString() {
        return asString();
    }
}
