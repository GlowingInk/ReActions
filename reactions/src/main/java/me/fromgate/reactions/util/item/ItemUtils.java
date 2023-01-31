package me.fromgate.reactions.util.item;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.fromgate.reactions.util.Rng;
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
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public final class ItemUtils {
    private ItemUtils() {}

    public static <T> @Nullable T searchByKey(@NotNull String key, @NotNull Function<NamespacedKey, T> search) {
        try {
            return search.apply(NamespacedKey.minecraft(key.toLowerCase(Locale.ROOT)));
        } catch (Exception ignored) {
            return null;
        }
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

    public static @NotNull String toDisplayString(@NotNull ItemStack item) {
        if (!item.hasItemMeta()) return item.getType().name();
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName()
                ? meta.getDisplayName()
                : item.getType().name();
    }

    public static @NotNull String toDisplayString(@NotNull String itemStr) {
        return toDisplayString(Parameters.fromString(itemStr));
    }

    public static @NotNull String toDisplayString(@NotNull Parameters itemParams) {
        return itemParams.getStringSafe("name", () -> itemParams.getString("type", "AIR"));
    }

    /**
     * Get list of items from random set
     *
     * @param items Set of items, e.g set1:{item1:{}  item2:{} item3:{} chance:50}  set2:{item1:{}  item2:{} item3:{} chance:50}
     * @return List of items
     */
    public static @NotNull List<ItemStack> parseRandomItemsStr(@NotNull String items) { // TODO: Should be refactored
        Parameters params = Parameters.fromString(items);
        List<String> keys;
        if (!(keys = params.getKeyList("set")).isEmpty()) {
            Object2IntMap<List<ItemStack>> sets = new Object2IntOpenHashMap<>();
            int maxChance = 0;
            int nochcount = 0;
            for (String key : keys) {
                Parameters itemParams = Parameters.fromString(params.getString(key));
                List<ItemStack> itemList = parseItemsSet(itemParams, itemParams.getKeyList("item"));
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
                curchance = curchance + (sets.getInt(stack) < 0 ? eqperc : sets.getInt(stack));
                if (rnd <= curchance) return stack;
            }
        } else if (!(keys = params.getKeyList("item")).isEmpty()) {
            return parseItemsSet(params, keys);
        } else {
            ItemStack vi = VirtualItem.asItemStack(items);
            if (vi != null) {
                return List.of(vi);
            }

        }
        return List.of();
    }

    private static @NotNull List<ItemStack> parseItemsSet(@NotNull Parameters params, @NotNull List<String> keys) {
        List<ItemStack> items = new ArrayList<>();
        for (String key : keys) {
            ItemStack item = params.get(key, VirtualItem::asItemStack);
            if (item != null) items.add(item);
        }
        if (items.isEmpty()) {
            ItemStack item = VirtualItem.asItemStack(params);
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
        Material type = getMaterial(name);
        return type == null ? def : type;
    }

    /**
     * Is item actually exist
     *
     * @param item Item to check
     * @return Is item not null and not air
     */
    @Contract("null -> false")
    public static boolean isExist(@Nullable ItemStack item) {
        return item != null && !item.getType().isEmpty();
    }

    /**
     * Get item in hand
     *
     * @param player Player to use
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
