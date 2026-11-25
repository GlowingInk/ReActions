package fun.reactions.util.item;

import fun.reactions.util.parameter.Parameterizable;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class VirtualItem implements Parameterizable {
    /**
     * A VirtualItem that accepts only null or air ItemStacks
     */
    public static final VirtualItem AIR = new VirtualItem(Material.AIR, -1, List.of(), Parameters.singleton("type", "AIR"));

    /**
     * A VirtualItem that accepts any ItemStacks but null or air
     */
    public static final VirtualItem ANY = new VirtualItem(null, -1, List.of(), Parameters.EMPTY);

    // TODO Special MetaAspects for NOTHING and EVERYTHING
    /**
     * A VirtualItem that accepts everything
     */
    public static final VirtualItem EVERYTHING = new VirtualItem(null, -1, List.of(new MetaAspect.Instance() {
        @Override
        public void apply(@NotNull ItemMeta meta) { }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            return true;
        }

        @Override
        public @NotNull String getName() {
            return "everything";
        }

        @Override
        public @NotNull String asString() {
            return "true";
        }
    }), Parameters.singleton("everything", "true")) {
        @Override
        public boolean isSimilar(@Nullable ItemStack compared, @NotNull AmountCheck amountCheck) {
            return true;
        }
    };

    /**
     * A VirtualItem that accepts nothing
     */
    public static final VirtualItem NOTHING = new VirtualItem(null, -1, List.of(new MetaAspect.Instance() {
        @Override
        public void apply(@NotNull ItemMeta meta) { }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            return false;
        }

        @Override
        public @NotNull String getName() {
            return "nothing";
        }

        @Override
        public @NotNull String asString() {
            return "true";
        }
    }), Parameters.singleton("nothing", "true")) {
        @Override
        public boolean isSimilar(@Nullable ItemStack compared) {
            return false;
        }
    };

    private final Material type;
    private final int amount;
    private final List<MetaAspect.Instance> aspects; // TODO Probably would be better to just have a Map<String, MA.Instance>

    private boolean itemGenerated;
    private ItemStack itemValue;
    private Parameters paramsValue;

    VirtualItem(
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

    VirtualItem(
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

    @Contract(value = "_ -> new", pure = true)
    public @NotNull ItemStack affect(@NotNull ItemStack item) {
        if (type != null) {
            if (type.isAir()) {
                item = item.withType(Material.AIR);
                return item;
            } else if (type.isItem() && item.getType() != type) {
                item = item.withType(type);
            } else {
                item = item.clone();
            }
        } else {
            item = item.clone();
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

    @Nullable ItemStack asItemStack(boolean initClone) {
        if (itemGenerated) {
            return itemValue == null
                    ? null
                    : itemValue.clone();
        } else {
            if (type == null || !type.isItem()) {
                return null;
            } else {
                ItemStack genItem = new ItemStack(type);
                if (!type.isAir()) {
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
    public boolean isSimilar(@Nullable ItemStack compared, @NotNull AmountCheck amountCheck) {
        if (compared == null || compared.isEmpty()) {
            return amount == 0 || (type != null && type.isAir());
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
            case EQUAL -> amount == -1 || amount == compared.getAmount();
            case SATISFIES -> amount == -1 || amount <= compared.getAmount();
        };
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
