package me.fromgate.reactions.util.item;

import me.fromgate.reactions.util.Utils;
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


public final class ModernVirtualItem {
    private static final Map<String, MetaResolver> RESOLVERS_MAP = new HashMap<>(); // TODO Fill the map
    private static final List<MetaResolver> RESOLVERS = new ArrayList<>(); // TODO Fill the list

    private final Material type;
    private final int amount;
    private final List<MetaResolver.Instance> resolvers;

    private boolean itemGenerated;
    private ItemStack itemStack;

    private boolean strGenerated;
    private String strValue;

    public ModernVirtualItem(
            @Nullable Material type,
            int amount,
            @NotNull List<MetaResolver.Instance> resolvers
    ) {
        this.type = type;
        this.amount = amount;
        this.resolvers = resolvers;
    }

    public @Nullable ItemStack asItemStack() {
        if (!itemGenerated) {
            itemGenerated = true;
            if (type == null || !type.isItem() || type.isEmpty()) return null;
            itemStack = new ItemStack(type);
            itemStack.setAmount(Math.max(amount, 1));
            if (!resolvers.isEmpty()) {
                ItemMeta meta = itemStack.getItemMeta();
                resolvers.forEach(resolver -> resolver.apply(meta));
                itemStack.setItemMeta(meta);
            }
        }
        return itemStack;
    }

    public @NotNull String asString() {
        if (strGenerated) {
            return strValue;
        }
        strGenerated = true;
        if (type == null) {
            return strValue = "type:AIR";
        }
        StringBuilder builder = new StringBuilder("type:").append(type.name()).append(' ');
        for (MetaResolver.Instance resolver : resolvers) {
            builder.append(resolver.getName()).append(':');
            String value = resolver.asString();
            if (value.indexOf(' ') != -1 || value.isEmpty()) { // We don't expect String to String conversion, so it's better to check emptiness later
                builder.append('{').append(value).append('}');
            } else {
                builder.append(value);
            }
            builder.append(' ');
        }
        return strValue = Utils.cutBuilder(builder, 1);
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

    public static @NotNull ModernVirtualItem fromItem(@NotNull ItemStack item) {
        List<MetaResolver.Instance> resolvers;
        if (!item.hasItemMeta()) {
            resolvers = Collections.emptyList();
        } else {
            resolvers = new ArrayList<>();
            ItemMeta meta = item.getItemMeta();
            for (MetaResolver resolver : RESOLVERS) {
                MetaResolver.Instance resolverInst = resolver.fromItem(meta);
                resolvers.add(resolverInst);
            }
            if (resolvers.isEmpty()) {
                resolvers = Collections.emptyList();
            }
        }
        return new ModernVirtualItem(
                item.getType(),
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
        int amount = -1;
        boolean regex = params.getBoolean("regex", false);
        for (String key : params) {
            key = key.toLowerCase(Locale.ROOT);
            switch (key) {
                case "type" -> type = Material.getMaterial(params.getString(key));
                case "name", "lore" -> {
                    if (regex) {
                        MetaResolver resolver = RESOLVERS_MAP.get(key + "-regex");
                        if (resolver == null) continue; // Literally how
                        resolvers.add(resolver.fromString(params.getString(key)));
                    }
                }
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
                amount,
                resolvers
        );
    }
}
