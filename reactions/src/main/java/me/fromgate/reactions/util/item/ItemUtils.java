package me.fromgate.reactions.util.item;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.fromgate.reactions.util.Rng;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class ItemUtils {
    private static final Pattern ITEM_D = Pattern.compile("item\\d+|ITEM\\d+");
    private static final Pattern SET_D = Pattern.compile("set\\d+|SET\\d+");

    private ItemUtils() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static <T> @Nullable T searchByKey(@NotNull String key, @NotNull Function<NamespacedKey, T> search) {
        NamespacedKey namespaced;
        try {
            namespaced = NamespacedKey.minecraft(key.toLowerCase(Locale.ROOT));
        } catch (Exception ignored) {
            return null;
        }
        return search.apply(namespaced);
    }

    public static int getDurability(@NotNull ItemStack item) {
        return item.hasItemMeta() && item.getItemMeta() instanceof Damageable damageMeta
                ? damageMeta.getDamage()
                : 0;
    }

    public static void giveItemOrDrop(@NotNull Player player, @NotNull ItemStack item) {
        Location loc = player.getLocation();
        for (ItemStack itemDrop : player.getInventory().addItem(item).values()) {
            player.getWorld().dropItemNaturally(loc, itemDrop);
        }
    }

    // TODO: Too many "toDisplayString" utils?

    public static String toDisplayString(@NotNull List<ItemStack> items) {
        StringBuilder builder = new StringBuilder();
        for (ItemStack item : items) {
            builder.append(toDisplayString(item)).append(", ");
        }
        return Utils.cutBuilder(builder, 2);
    }

    public static String toDisplayString(@NotNull ItemStack item) {
        if (!item.hasItemMeta()) return item.getType().name();
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName()
                ? meta.getDisplayName()
                : item.getType().name();
    }

    public static String toDisplayString(@NotNull String itemStr) {
        return toDisplayString(Parameters.fromString(itemStr));
    }

    public static String toDisplayString(@NotNull Parameters itemParams) {
        return itemParams.getStringSafe("name", () -> itemParams.getString("type", "AIR"));
    }

    /**
     * Get list of items from random set
     *
     * @param items Set of items, e.g set1:{item1:{}  item2:{} item3:{} chance:50}  set2:{item1:{}  item2:{} item3:{} chance:50}
     * @return List of items
     */
    public static @NotNull List<ItemStack> parseRandomItemsStr(String items) { // TODO: Should be refactored
        Parameters params = Parameters.fromString(items);
        if (params.matchesAny(SET_D)) {
            Object2IntMap<List<ItemStack>> sets = new Object2IntOpenHashMap<>();
            int maxChance = 0;
            int nochcount = 0;
            for (String key : params.keySet()) {
                if (!SET_D.matcher(key).matches()) continue;
                Parameters itemParams = Parameters.fromString(params.getString(key));
                List<ItemStack> itemList = parseItemsSet(itemParams);
                if (itemList.isEmpty()) continue;
                int chance = itemParams.getInteger("chance", -1);
                if (chance > 0) maxChance += chance;
                else nochcount++;
                sets.put(itemList, chance);
            }
            int eqperc = (nochcount * 100) / sets.size();
            maxChance = maxChance + eqperc * nochcount;
            int rnd = Rng.nextInt(maxChance);
            int curchance = 0;
            for (List<ItemStack> stack : sets.keySet()) {
                curchance = curchance + (sets.get(stack) < 0 ? eqperc : sets.get(stack));
                if (rnd <= curchance) return stack;
            }
        } else if (params.matchesAny(ITEM_D)) {
            return parseItemsSet(params);
        } else {
            ItemStack vi = VirtualItem.asItem(items);
            if (vi != null) {
                return Collections.singletonList(vi);
            }

        }
        return Collections.emptyList();
    }

    private static List<ItemStack> parseItemsSet(Parameters params) {
        List<ItemStack> items = new ArrayList<>();
        for (String key : params.keySet()) {
            if (ITEM_D.matcher(key).matches()) {
                String itemStr = params.getString(key, "");
                ItemStack vi = VirtualItem.asItem(itemStr);
                if (vi != null) items.add(vi);
            }
        }
        if (items.isEmpty()) {
            ItemStack item = VirtualItem.asItem(params);
            if (item != null) items.add(item);
        }
        return items;
    }

    /**
     * Get material from name
     *
     * @param name Name of material
     * @return Material
     */
    public static @Nullable Material getMaterial(@NotNull String name) {
        return Material.getMaterial(name.toUpperCase(Locale.ROOT));
    }

    @Contract("_, !null -> !null")
    public static @Nullable Material getMaterial(@NotNull String name, @Nullable Material def) {
        Material type = Material.getMaterial(name.toUpperCase(Locale.ROOT));
        return type == null ? def : type;
    }

    /**
     * Is item actually exist
     *
     * @param item Item to check
     * @return Is item not null and not air
     */
    public static boolean isExist(@Nullable ItemStack item) {
        return item != null && !item.getType().isEmpty();
    }

    /**
     * Get item in hand
     *
     * @param player  Player to use
     * @param offhand Check offhand or not
     * @return Item string
     */
    public static @NotNull String getItemInHand(@NotNull Player player, boolean offhand) {
        return VirtualItem.asString(offhand
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand()
        );
    }
}
