package fun.reactions.util.item;

import fun.reactions.util.item.aspects.*;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.Parameterizable;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class VirtualItem implements Parameterizable {
    private static final Pattern SIMPLE_ITEM = Pattern.compile("([a-zA-Z\\d_]+)(?::(\\d{1,9}))?(?:\\*(\\d{1,9}))?");

    /**
     * A VirtualItem that accepts only null or air ItemStacks
     */
    public static final VirtualItem AIR = new VirtualItem(Material.AIR, -1, List.of(), Parameters.singleton("type", "AIR"));
    /**
     * A VirtualItem that accepts any ItemStacks but null or air
     */
    public static final VirtualItem ANY = new VirtualItem(null, -1, List.of(), Parameters.EMPTY);
    /**
     * A VirtualItem that accepts nothing
     */
    public static final VirtualItem INVALID = new VirtualItem(null, -1, List.of(new MetaAspect.Instance() {
        @Override
        public void apply(@NotNull ItemMeta meta) {}

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            return false;
        }

        @Override
        public @NotNull String getName() {
            return "invalid";
        }

        @Override
        public @NotNull String asString() {
            return "true";
        }
    }), Parameters.singleton("invalid", "true"));

    private static final Map<String, MetaAspect> ASPECTS_BY_NAME = new LinkedHashMap<>(); // TODO: Registry
    private static final List<MetaAspect> ASPECTS = new ArrayList<>();
    static {
        registerAspect(new NameAspect(true));
        registerAspect(new NameAspect(false));
        registerAspect(new LoreAspect(true));
        registerAspect(new LoreAspect(false));
        registerAspect(new DurabilityAspect());
        registerAspect(new EnchantmentsAspect());
        registerAspect(new ModelAspect());
        registerAspect(new ColorAspect());
        registerAspect(new BannerAspect());
        registerAspect(new BookAspect(BookAspect.Type.PAGES));
        registerAspect(new BookAspect(BookAspect.Type.TITLE));
        registerAspect(new BookAspect(BookAspect.Type.AUTHOR));
        registerAspect(new FireworkAspect(true));
        registerAspect(new FireworkAspect(false));
        registerAspect(new HeadAspect());
        registerAspect(new MapAspect(true));
        registerAspect(new MapAspect(false));
        registerAspect(new PotionAspect(true));
        registerAspect(new PotionAspect(false));
    }
    private static void registerAspect(@NotNull MetaAspect aspect) {
        ASPECTS_BY_NAME.put(aspect.getName().toLowerCase(Locale.ROOT), aspect);
        ASPECTS.add(aspect);
        for (String alias : Aliased.getAliasesOf(aspect)) {
            ASPECTS_BY_NAME.putIfAbsent(alias.toLowerCase(Locale.ROOT), aspect);
        }
    }

    private final Material type;
    private final int amount;
    private final List<MetaAspect.Instance> aspects; // TODO Probably would be better to just have a Map<String, MA.Instance>

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

    public @Nullable String getAspect(@NotNull String aspectName) {
        return asParameters().getString(aspectName, null);
    }

    @Contract(pure = true)
    public @NotNull ItemStack affect(@NotNull ItemStack item) {
        return affect(item, true);
    }

    @Contract("_, true -> new")
    public @NotNull ItemStack affect(@NotNull ItemStack item, boolean clone) {
        if (clone) {
            item = item.clone();
        }
        if (type != null) {
            if (type.isEmpty()) {
                item.setType(Material.AIR);
                return item;
            } else if (type.isItem() && item.getType() != type) {
                item.setType(type);
            }
        }
        if (amount > 0) {
            item.setAmount(amount);
        }
        if (!aspects.isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                aspects.forEach(aspect -> aspect.apply(meta));
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    @Contract(pure = true)
    public @NotNull VirtualItem affect(@NotNull VirtualItem other) {
        var aspectsMap = other.aspects.stream().collect(Collectors.toMap(MetaAspect.Instance::getName, a -> a));
        this.aspects.forEach(a -> aspectsMap.put(a.getName(), a));
        return new VirtualItem(
                type == null ? other.getType() : type,
                amount < 0 ? other.getAmount() : amount,
                new ArrayList<>(aspectsMap.values()),
                other.asParameters().with(asParameters())
        );
    }

    public @Nullable ItemStack asItemStack() {
        return asItemStack(true);
    }

    private @Nullable ItemStack asItemStack(boolean initClone) {
        if (itemGenerated) {
            return itemValue == null
                    ? null
                    : itemValue.clone();
        } else {
            if (type == null || !type.isItem()) {
                return null;
            } else {
                ItemStack genItem = new ItemStack(type);
                if (!type.isEmpty()) {
                    genItem.setAmount(Math.max(amount, 1));
                    if (!aspects.isEmpty()) {
                        ItemMeta meta = genItem.getItemMeta();
                        aspects.forEach(aspect -> aspect.apply(meta));
                        genItem.setItemMeta(meta);
                    }
                }
                itemValue = genItem;
                itemGenerated = true;
                return initClone ? genItem.clone() : genItem;
            }
        }
    }

    @Override
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
     * Compare this item with a real one.
     * If compared item is null or its type is AIR, expecting this item's type to be AIR.
     * If this item's type is specified, expecting compared item's type to be the same.
     * Skips amount check.
     * @param compared item to compare
     * @return is compared item conforms this item's type and aspects
     * @see VirtualItem#isSimilar(ItemStack, AmountCheck)
     */
    public boolean isSimilar(@Nullable ItemStack compared) {
        return isSimilar(compared, AmountCheck.SKIP);
    }

    /**
     * Compare this item with real one.
     * If compared item is null or its type is AIR, expecting this item's type to be AIR.
     * If this item's type is specified, expecting compared item's type to be the same.
     * @param compared item to compare
     * @param amountCheck type of amount checking
     * @return is compared item conforms this item's type and aspects
     */
    public boolean isSimilar(@Nullable ItemStack compared, AmountCheck amountCheck) {
        if (compared == null || compared.getType().isEmpty()) {
            return amount == 0 || (type != null && type.isEmpty());
        }
        if (type != null && compared.getType() != type) {
            return false;
        }
        if (!aspects.isEmpty()) {
            ItemMeta meta = compared.getItemMeta();
            for (MetaAspect.Instance aspect : aspects) {
                if (!aspect.isSimilar(meta)) return false;
            }
        }
        return switch (amountCheck) {
            case SKIP -> true;
            case EQUAL -> amount == compared.getAmount();
            case SATISFIES -> amount <= compared.getAmount();
        };
    }

    /**
     * Generate a new VirtualItem from item.
     * Considers null as item type AIR.
     * @param item item to generate from
     * @return generated VirtualItem
     */
    public static @NotNull VirtualItem fromItemStack(@Nullable ItemStack item) {
        return fromItemStack(item, true);
    }

    private static @NotNull VirtualItem fromItemStack(@Nullable ItemStack item, boolean clone) {
        if (item == null || item.getType().isEmpty()) {
            return VirtualItem.AIR;
        }
        if (clone) {
            item = item.clone();
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

    @Contract(pure = true)
    public static @NotNull VirtualItem fromString(@NotNull String paramsStr) {
        return fromParameters(Parameters.fromString(paramsStr));
    }

    @Contract(pure = true)
    public static @NotNull VirtualItem fromParameters(@NotNull Parameters params) {
        if (params.isEmpty()) return VirtualItem.ANY;
        List<MetaAspect.Instance> aspects = new ArrayList<>();
        Material type = null;
        int amount = -1;
        boolean regex = params.getBoolean("regex", false);
        for (String key : params.keysFull()) {
            key = key.toLowerCase(Locale.ROOT);
            String value = params.getString(key);
            switch (key) {
                case Parameters.ORIGIN_KEY: {
                    continue;
                }
                case "invalid": {
                    if (params.getBoolean(key)) {
                        return INVALID;
                    }
                    break;
                }
                case "item": {
                    Matcher matcher = SIMPLE_ITEM.matcher(params.getString(key));
                    if (!matcher.matches()) break;
                    type = ItemUtils.getMaterial(matcher.group(1));
                    if (type == null) return VirtualItem.INVALID;
                    if (matcher.group(2) != null) {
                        aspects.add(ASPECTS_BY_NAME.get("durability").fromString(matcher.group(1)));
                    }
                    if (matcher.group(3) != null) {
                        amount = NumberUtils.asInteger(matcher.group(3), -1);
                    }
                    break;
                }
                case "type": {
                    type = params.get(key, ItemUtils::getMaterial);
                    if (type == null) return VirtualItem.INVALID;
                    break;
                }
                case "amount": {
                    amount = params.getInteger(key);
                    break;
                }
                case "name": case "lore":
                    if (regex) key += "-regex";
                default:
                    MetaAspect aspect = ASPECTS_BY_NAME.get(key);
                    if (aspect == null) continue;
                    aspects.add(aspect.fromString(value));
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

    @Contract("null -> null; !null -> !null")
    public static @Nullable ItemStack asCleanItemStack(@Nullable ItemStack item) {
        if (item == null) return null;
        VirtualItem virtual = fromItemStack(item, false);
        return new VirtualItem(
                virtual.type,
                virtual.amount,
                virtual.aspects,
                virtual.asParameters()
        ).asItemStack(false);
    }

    public static @Nullable ItemStack asItemStack(@NotNull String itemStr) {
        return fromString(itemStr).asItemStack(false);
    }

    public static @Nullable ItemStack asItemStack(@NotNull Parameters itemParams) {
        return fromParameters(itemParams).asItemStack(false);
    }

    public static @NotNull String asString(@Nullable ItemStack item) {
        return fromItemStack(item, false).asString();
    }

    public static @NotNull Parameters asParameters(@Nullable ItemStack item) {
        return fromItemStack(item, false).asParameters();
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

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof VirtualItem otherItem &&
                amount == otherItem.amount &&
                type == otherItem.type &&
                aspects.equals(otherItem.aspects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount, aspects);
    }

    public enum AmountCheck {
        /**
         * Skip amount check as a whole
         */
        SKIP,
        /**
         * Checks if amounts are equal
         */
        EQUAL,
        /**
         * Checks if amount of ItemStack satisfies (>=) amount of VirtualItem
         */
        SATISFIES
    }
}
