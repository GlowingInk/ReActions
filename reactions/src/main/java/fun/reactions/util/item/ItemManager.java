package fun.reactions.util.item;

import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.Parameters;
import ink.glowing.utils.hash.CaseInsensitive;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemManager {
    private static final Pattern SIMPLE_ITEM = Pattern.compile("([a-zA-Z\\d_]+)(?::(\\d{1,9}))?(?:\\*(\\d{1,9}))?");

    private final Map<String, MetaAspect> aspectByName;
    private final List<MetaAspect> aspects;

    public ItemManager() {
        aspectByName = CaseInsensitive.newMap();
        aspects = new ArrayList<>();
    }

    public void registerAspect(@NotNull MetaAspect aspect) {
        String name = aspect.getName();
        if (aspectByName.containsKey(name)) {
            throw new IllegalStateException("Item aspect '" + name + "' is already registered");
        }
        aspectByName.put(name, aspect);
        aspects.add(aspect);
        for (String alias : Aliased.getAliasesOf(aspect)) {
            aspectByName.putIfAbsent(alias, aspect);
        }
    }

    public @Nullable MetaAspect getAspect(@NotNull String name) {
        return aspectByName.get(name);
    }

    public @NotNull @UnmodifiableView List<MetaAspect> getAspects() {
        return Collections.unmodifiableList(aspects);
    }

    public @NotNull VirtualItem fromItemStack(@Nullable ItemStack item) {
        return fromItemStack(item, true);
    }

    private @NotNull VirtualItem fromItemStack(@Nullable ItemStack item, boolean clone) {
        if (item == null || item.isEmpty()) {
            return VirtualItem.AIR;
        }
        if (clone) {
            item = item.clone();
        }
        List<MetaAspect.Instance> itemAspects;
        if (!item.hasItemMeta()) {
            itemAspects = List.of();
        } else {
            itemAspects = new ArrayList<>();
            ItemMeta meta = item.getItemMeta();
            for (MetaAspect aspect : aspects) {
                MetaAspect.Instance aspectInst = aspect.fromItem(meta);
                if (aspectInst != null) itemAspects.add(aspectInst);
            }
            if (itemAspects.isEmpty()) {
                itemAspects = List.of();
            }
        }
        return new VirtualItem(
                item.getType(),
                item.getAmount(),
                itemAspects,
                item
        );
    }

    @Contract(pure = true)
    public @NotNull VirtualItem fromString(@Nullable String paramsStr) {
        return paramsStr == null
                ? VirtualItem.EVERYTHING
                : fromParameters(Parameters.fromString(paramsStr));
    }

    @Contract(pure = true)
    public @NotNull VirtualItem fromParameters(@Nullable Parameters params) {
        if (params == null) return VirtualItem.EVERYTHING;
        if (params.isEmpty()) return VirtualItem.ANY;
        List<MetaAspect.Instance> itemAspects = new ArrayList<>();
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
                case "everything": {
                    if (params.getBoolean(key)) {
                        return VirtualItem.EVERYTHING;
                    }
                    break;
                }
                case "nothing", "invalid": {
                    if (params.getBoolean(key)) {
                        return VirtualItem.NOTHING;
                    }
                    break;
                }
                case "item": {
                    Matcher matcher = SIMPLE_ITEM.matcher(params.getString(key));
                    if (!matcher.matches()) break;
                    type = ItemUtils.getMaterial(matcher.group(1));
                    if (type == null) return VirtualItem.NOTHING;
                    MetaAspect durability = getAspect("durability");
                    if (matcher.group(2) != null && durability != null) {
                        itemAspects.add(durability.fromString(matcher.group(2)));
                    }
                    if (matcher.group(3) != null) {
                        amount = NumberUtils.asInteger(matcher.group(3), -1);
                    }
                    break;
                }
                case "type": {
                    type = params.get(key, ItemUtils::getMaterial);
                    if (type == null) return VirtualItem.NOTHING;
                    break;
                }
                case "amount": {
                    amount = params.getInteger(key);
                    break;
                }
                case "name": case "lore":
                    if (regex) key += "-regex";
                default:
                    MetaAspect aspect = getAspect(key);
                    if (aspect == null) continue;
                    itemAspects.add(aspect.fromString(value));
                    break;
            }
        }
        return new VirtualItem(
                type,
                amount,
                itemAspects,
                params
        );
    }

    public @Nullable ItemStack asItemStack(@NotNull String itemStr) {
        return fromString(itemStr).asItemStack(false);
    }

    public @Nullable ItemStack asItemStack(@NotNull Parameters itemParams) {
        return fromParameters(itemParams).asItemStack(false);
    }

    public @NotNull String asString(@Nullable ItemStack item) {
        return fromItemStack(item, false).asString();
    }

    public boolean isSimilar(@NotNull String itemStr, @Nullable ItemStack compared) {
        return fromString(itemStr).isSimilar(compared);
    }
}
