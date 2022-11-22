package me.fromgate.reactions.util.item;

import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.item.aspects.*;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    public static final VirtualItem AIR = new VirtualItem(Material.AIR, -1, List.of(), Parameters.fromMap(Map.of("type", "AIR")));
    /**
     * A VirtualItem that accepts any ItemStacks but null or air
     */
    public static final VirtualItem EMPTY = new VirtualItem(null, -1, List.of(), Parameters.EMPTY);

    private static final Map<String, MetaAspect> ASPECTS_BY_NAME = new LinkedHashMap<>(); // TODO: Registry
    private static final List<MetaAspect> ASPECTS = new ArrayList<>();
    static {
        registerAspect(new BannerAspect());
        registerAspect(new BookAspect(BookAspect.Type.PAGES));
        registerAspect(new BookAspect(BookAspect.Type.TITLE));
        registerAspect(new BookAspect(BookAspect.Type.AUTHOR));
        registerAspect(new ColorAspect());
        registerAspect(new DurabilityAspect());
        registerAspect(new EnchantmentsAspect());
        registerAspect(new FireworkAspect(true));
        registerAspect(new FireworkAspect(false));
        registerAspect(new HeadAspect());
        registerAspect(new LoreAspect(true));
        registerAspect(new LoreAspect(false));
        registerAspect(new MapAspect(true));
        registerAspect(new MapAspect(false));
        registerAspect(new ModelAspect());
        registerAspect(new NameAspect(true));
        registerAspect(new NameAspect(false));
        registerAspect(new PotionAspect(true));
        registerAspect(new PotionAspect(false));
    }
    private static void registerAspect(@NotNull MetaAspect aspect) {
        ASPECTS_BY_NAME.put(aspect.getName().toLowerCase(Locale.ROOT), aspect);
        ASPECTS.add(aspect);
        for (String alias : Utils.getAliases(aspect)) {
            ASPECTS_BY_NAME.putIfAbsent(alias.toLowerCase(Locale.ROOT), aspect);
        }
    }

    private final Material type;
    private final int amount;
    private final List<MetaAspect.Instance> aspects;

    private boolean itemGenerated;
    private ItemStack itemValue;
    private Parameters paramsValue;

    private VirtualItem(
            @Nullable Material type,
            int amount,
            @NotNull List<MetaAspect.Instance> aspects,
            @NotNull Parameters params
    ) {
        this.type = type;
        this.amount = amount;
        this.aspects = aspects;
        this.paramsValue = params;
    }

    private VirtualItem(
            @Nullable Material type,
            int amount,
            @NotNull List<MetaAspect.Instance> aspects,
            @NotNull ItemStack item
    ) {
        this.type = type;
        this.amount = amount;
        this.aspects = aspects;
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

    private @Nullable ItemStack asItem(boolean initClone) {
        if (itemGenerated) {
            return itemValue == null
                    ? null
                    : itemValue.clone();
        } else {
            itemGenerated = true;
            if (type == null || !type.isItem()) {
                return null;
            } else {
                itemValue = new ItemStack(type);
                itemValue.setAmount(Math.max(amount, 1));
                if (!type.isEmpty() && !aspects.isEmpty()) {
                    ItemMeta meta = itemValue.getItemMeta();
                    aspects.forEach(aspect -> aspect.apply(meta));
                    itemValue.setItemMeta(meta);
                }
                return initClone ? itemValue.clone() : itemValue;
            }
        }
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
        for (MetaAspect.Instance aspect : aspects) {
            paramsMap.put(aspect.getName(), aspect.asString());
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
     * @return is compared item conforms this item's type and aspects
     */
    public boolean isSimilar(@Nullable ItemStack compared) {
        if (compared == null || compared.getType().isEmpty()) {
            return type != null && type.isEmpty();
        }
        if (type == null || compared.getType() == type) {
            if (!aspects.isEmpty()) {
                ItemMeta meta = compared.getItemMeta();
                for (MetaAspect.Instance aspect : aspects) {
                    if (!aspect.isSimilar(meta)) return false;
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
        List<MetaAspect.Instance> aspects;
        if (!item.hasItemMeta()) {
            aspects = List.of();
        } else {
            aspects = new ArrayList<>();
            ItemMeta meta = item.getItemMeta();
            for (MetaAspect aspect : ASPECTS) {
                MetaAspect.Instance aspectInst = aspect.fromItem(meta);
                if (aspectInst != null) aspects.add(aspectInst);
            }
            if (aspects.isEmpty()) {
                aspects = List.of();
            }
        }
        return new VirtualItem(
                item.getType(),
                item.getAmount(),
                aspects,
                item
        );
    }

    public static @NotNull VirtualItem fromString(@NotNull String paramsStr) {
        return fromParameters(Parameters.fromString(paramsStr));
    }

    public static @NotNull VirtualItem fromParameters(@NotNull Parameters params) {
        if (params.isEmpty()) return VirtualItem.EMPTY;
        List<MetaAspect.Instance> aspects = new ArrayList<>();
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
                    if (!Utils.isStringEmpty(matcher.group(2))) {
                        aspects.add(ASPECTS_BY_NAME.get("durability").fromString(matcher.group(1)));
                    }
                    if (!Utils.isStringEmpty(matcher.group(3))) {
                        amount = NumberUtils.asInt(matcher.group(3), -1);
                    }
                    break;
                }
                case "type": type = params.get(key, ItemUtils::getMaterial); break;
                case "amount": amount = params.getInteger(key); break;
                case "name": case "lore":
                    if (regex) key += "-regex";
                default:
                    MetaAspect aspect = ASPECTS_BY_NAME.get(key);
                    if (aspect == null) continue;
                    aspects.add(aspect.fromString(params.getString(key)));
                    break;
            }
        }
        return new VirtualItem(
                type,
                amount,
                aspects,
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
