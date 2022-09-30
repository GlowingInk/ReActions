package me.fromgate.reactions.util.item;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.Rng;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class ItemUtils {

    private static final Pattern BYTES_RGB = Pattern.compile("^\\d{1,3},\\d{1,3},\\d{1,3}$");

    private static final Pattern ITEM_D = Pattern.compile("item\\d+|ITEM\\d+");
    private static final Pattern SET_D = Pattern.compile("set\\d+|SET\\d+");

    private ItemUtils() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static void removeItemAmount(ItemStack item, int amount) {
        if (!isExist(item)) return;
        int itemAmount = item.getAmount();
        if (amount >= itemAmount)
            item.setType(Material.AIR);
        else
            item.setAmount(itemAmount - amount);
    }

    public static Enchantment getEnchantmentByName(String name) {
        if (!Utils.isStringEmpty(name))
            try {
                return Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignore) {
            }
        return null;
    }

    public static int getDurability(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable)
            return ((Damageable) meta).getDamage();
        return 0;
    }

    public static void setDurability(ItemStack item, int durability) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable dmg) {
            dmg.setDamage(durability);
            item.setItemMeta(meta);
        }
    }

    public static void giveItemOrDrop(Player player, ItemStack item) {
        for (ItemStack itemDrop : player.getInventory().addItem(item).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), itemDrop);
        }
    }

    public static int countItemsInInventory(Inventory inventory, String itemStr) {
        Map<String, String> itemMap = Parameters.parametersMap(itemStr);
        return countItemsInventory(inventory, itemMap);
    }

    private static int countItemsInventory(Inventory inventory, Map<String, String> itemParams) {
        int count = 0;
        for (ItemStack slot : inventory) {
            if (slot == null || slot.getType() == Material.AIR) continue;
            VirtualItem vi = VirtualItem.fromItemStack(slot);
            if (!vi.compare(itemParams, 1)) continue;
            count += slot.getAmount();
        }
        return count;
    }

    public static int getAmount(String itemStr) {
        Map<String, String> itemMap = Parameters.parametersMap(itemStr);
        String amountStr = itemMap.getOrDefault("amount", "1");
        if (NumberUtils.INT_NONZERO_POSITIVE.matcher(amountStr).matches()) return Integer.parseInt(amountStr);
        return 1;
    }

    public static VirtualItem itemFromBlock(Block block) {
        return new VirtualItem(block == null ? Material.STONE : block.getType());
    }

    public static boolean compareItemStr(Block block, String itemStr) {
        if (block == null || block.getType() == Material.AIR) return false;
        ItemStack item = new ItemStack(block.getType(), 1);
        return compareItemStr(item, itemStr);
    }

    public static boolean compareItemStr(ItemStack item, String itemStr) {
        if (!isExist(item)) return false;
        return VirtualItem.fromItemStack(item).compare(itemStr);
    }

    public static boolean compareItemStr(ItemStack item, String itemStr, boolean allowHand) {
        if (allowHand && (itemStr.equalsIgnoreCase("HAND") || itemStr.equalsIgnoreCase("AIR"))) {
            return !isExist(item);
        }
        return compareItemStr(item, itemStr);
    }

    public static ItemStack getRndItem(String str) {
        if (str.isEmpty()) return new ItemStack(Material.AIR);
        String[] ln = str.split(",");
        if (ln.length == 0) return new ItemStack(Material.AIR);

        ItemStack item = VirtualItem.fromString(ln[Rng.nextInt(ln.length)]);

        if (item == null) return new ItemStack(Material.AIR);
        item.setAmount(1);
        return item;
    }

    public static String itemToString(ItemStack item) {
        VirtualItem vi = VirtualItem.fromItemStack(item);
        return vi == null ? "" : vi.toString();
    }

    public static String toDisplayString(List<ItemStack> items) {
        StringBuilder sb = new StringBuilder();
        for (ItemStack i : items) {
            VirtualItem vi = VirtualItem.fromItemStack(i);
            sb.append(vi.toDisplayString()).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    public static List<ItemStack> parseItemsSet(Parameters params) {
        List<ItemStack> items = new ArrayList<>();
        for (String key : params.keySet()) {
            if (ITEM_D.matcher(key).matches()) {
                String itemStr = params.getString(key, "");
                VirtualItem vi = VirtualItem.fromString(itemStr);
                if (vi != null) items.add(vi);
            }
        }
        if (items.isEmpty()) {
            VirtualItem item = VirtualItem.fromMap(params.getMap());
            if (item != null) items.add(item);
        }
        return items;
    }

    /**
     * Get list of items from random set
     *
     * @param items Set of items, e.g set1:{item1:{}  item2:{} item3:{} chance:50}  set2:{item1:{}  item2:{} item3:{} chance:50}
     * @return List of items
     */
    public static List<ItemStack> parseRandomItemsStr(String items) {
        Parameters params = Parameters.fromString(items);
        if (params.matchesAny(SET_D)) {
            Object2IntMap<List<ItemStack>> sets = new Object2IntOpenHashMap<>();
            int maxChance = 0;
            int nochcount = 0;
            for (String key : params.keySet()) {
                if (!SET_D.matcher(key).matches()) continue;
                Parameters itemParams = Parameters.fromString(params.getString(key));
                List<ItemStack> itemList = parseItemsSet(itemParams);
                if (itemList == null || itemList.isEmpty()) continue;
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
        } else if (params.matchesAny("item\\d+|ITEM\\d+")) {
            return parseItemsSet(params);
        } else {
            VirtualItem vi = VirtualItem.fromString(items);
            if (vi != null) {
                List<ItemStack> iList = new ArrayList<>();
                iList.add(vi);
                return iList;
            }

        }
        return null;
    }

    public static String toDisplayString(String itemStr) {
        VirtualItem vi = VirtualItem.fromString(itemStr);
        if (vi != null) return vi.toDisplayString();
        Map<String, String> itemMap = Parameters.parametersMap(itemStr);
        String name = itemMap.containsKey("name") ? itemMap.get("name") : itemMap.getOrDefault("type", null);
        if (name == null) return itemStr;
        int amount = getAmount(itemStr);
        String data = itemMap.getOrDefault("data", "0");
        StringBuilder sb = new StringBuilder(name);
        if (!itemMap.containsKey("name") && !data.equals("0")) sb.append(":").append(data);
        if (amount > 1) sb.append("*").append(amount);
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', sb.toString()));
    }

    public static String toDisplayString(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        StringBuilder sb = new StringBuilder(meta.hasDisplayName() ? meta.getDisplayName() : item.getType().name());
        int data = getDurability(item);
        if (data != 0) sb.append(":").append(data);
        if (item.getAmount() > 1) sb.append("*").append(item.getAmount());
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', sb.toString()));
    }

    /*
     * Функция проверяет входит есть ли item (блок) с заданным id и data в списке,
     * представленным в виде строки вида id1:data1,id2:data2,MATERIAL_NAME:data
     * При этом если data может быть опущена
     */
    public static boolean isItemInList(Material type, int durability, String str) {
        String[] ln = str.split(",");
        if (ln.length > 0)
            for (String itemInList : ln) {
                if (compareItemIdDataStr(type, durability, itemInList)) return true;
            }
        return false;
    }

    public static boolean compareItemIdDataStr(Material type, int durability, String itemStr) {
        ItemStack item = VirtualItem.fromString(itemStr);
        if (item == null) return false;
        if (item.getType() != type) return false;
        if (durability < 0) return true;
        return durability == getDurability(item);
    }

    /**
     * Get material from name
     *
     * @param name Name of material
     * @return Material (may be legacy)
     */
    public static Material getMaterial(String name) {
        if (Utils.isStringEmpty(name)) return null;
        name = name.toUpperCase(Locale.ROOT);
        Material material = Material.getMaterial(name, false);
        return material == null ? Material.getMaterial(name, true) : material;
    }

    /**
     * Is item actually exist
     *
     * @param item Item to check
     * @return Is item not null and not air
     */
    public static boolean isExist(ItemStack item) {
        return item != null && !item.getType().isAir();
    }

    public static String fireworksToString(FireworkEffect fe) {
        StringBuilder sb = new StringBuilder();
        sb.append("type:").append(fe.getType().name());
        sb.append(" flicker:").append(fe.hasFlicker());
        sb.append(" trail:").append(fe.hasTrail());
        if (!fe.getColors().isEmpty()) {
            sb.append(" colors:");
            for (int i = 0; i < fe.getColors().size(); i++) {
                Color c = fe.getColors().get(i);
                if (i > 0)
                    sb.append(";");
                sb.append(colorToString(c, true));
            }
        }
        if (!fe.getFadeColors().isEmpty()) {
            sb.append(" fade-colors:");
            for (int i = 0; i < fe.getFadeColors().size(); i++) {
                Color c = fe.getColors().get(i);
                if (i > 0) sb.append(";");
                sb.append(colorToString(c, true));
            }
        }
        return sb.toString();
    }

    static List<Color> parseColors(String colorStr) {
        List<Color> colors = new ArrayList<>();
        String[] clrs = colorStr.split(";");
        for (String cStr : clrs) {
            Color c = parseColor(cStr.trim());
            if (c == null)
                continue;
            colors.add(c);
        }
        return colors;
    }

    /**
     * Parse bukkit colors. Name and RGB values supported
     *
     * @param colorStr - Color name, or RGB values (Example: 10,15,20)
     * @return - Color
     */
    public static Color parseColor(String colorStr) {
        if (BYTES_RGB.matcher(colorStr).matches()) {
            String[] rgb = colorStr.split(",");
            int red = Integer.parseInt(rgb[0]);
            int green = Integer.parseInt(rgb[1]);
            int blue = Integer.parseInt(rgb[2]);
            return Color.fromRGB(red, green, blue);
        } else if (NumberUtils.BYTE.matcher(colorStr).matches()) {
            int num = Integer.parseInt(colorStr);
            if (num > 15)
                num = 15;
            @SuppressWarnings("deprecation")
            DyeColor c = DyeColor.getByDyeData((byte) num);
            return c == null ? null : c.getColor();
        } else {
			/*
			for (DyeColor dc : DyeColor.values())
				if (dc.name().equalsIgnoreCase(colorStr))
					return dc.getColor();
			 */
        }
        return null;
    }

    private static double getColorDistance(Color c1, Color c2) {
        double rmean = (c1.getRed() + c2.getRed()) / 2.0;
        double r = c1.getRed() - c2.getRed();
        double g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        double weightR = 2 + rmean / 256.0;
        double weightG = 4.0;
        double weightB = 2 + (255 - rmean) / 256.0;
        return weightR * r * r + weightG * g * g + weightB * b * b;
    }

    public static DyeColor getClosestColor(Color color) {
        int index = 0;
        double best = -1;
        for (int i = 0; i < DyeColor.values().length; i++) {
            double distance = getColorDistance(color,
                    DyeColor.values()[i].getColor());
            if (distance < best || best == -1) {
                best = distance;
                index = i;
            }
        }
        return DyeColor.values()[index];
    }

    public static String colorToString(Color c, boolean useRGB) {
        for (DyeColor dc : DyeColor.values())
            if (dc.getColor().equals(c))
                return dc.name();
        if (!useRGB)
            getClosestColor(c).name();
        return c.getRed() + "," +
                c.getGreen() + "," +
                c.getBlue();
    }

    public static Object2IntMap<Enchantment> parseEnchantmentsString(String enchStr) {
        Object2IntMap<Enchantment> ench = new Object2IntOpenHashMap<>();
        if (enchStr == null || enchStr.isEmpty()) return ench;
        String[] ln = enchStr.split(";");
        for (String e : ln) {
            String eType = e;
            int power = 0;
            if (eType.contains(":")) {
                String powerStr = eType.substring(eType.indexOf(":") + 1);
                eType = eType.substring(0, eType.indexOf(":"));
                power = Rng.nextIntRanged(powerStr);
            }
            Enchantment enchantment = getEnchantmentByName(eType);
            if (enchantment == null)
                continue;
            ench.put(enchantment, power);
        }
        return ench;

    }

    /**
     * Old format algorithm. Implemented for compatibility.
     *
     * @param itemStr - old item format
     * @return - ItemStack
     */
    protected static ItemStack parseOldItemStack(String itemStr) {
        if (Utils.isStringEmpty(itemStr))
            return null;
        String iStr = itemStr;
        String enchant = "";
        String name = "";
        String loreStr = "";
        if (iStr.contains("$")) {
            name = iStr.substring(0, iStr.indexOf("$"));
            iStr = iStr.substring(name.length() + 1);
            if (name.contains("@")) {
                loreStr = name.substring(name.indexOf("@") + 1);
                name = name.substring(0, name.indexOf("@"));
            }

        }
        if (iStr.contains("@")) {
            enchant = iStr.substring(iStr.indexOf("@") + 1);
            iStr = iStr.substring(0, iStr.indexOf("@"));
        }
        Material id;
        int amount = 1;
        short data = 0;
        String[] si = iStr.split("\\*");
        if (si.length > 0) {
            if (si.length == 2)
                amount = Math.max(Rng.nextIntRanged(si[1]), 1);
            String[] ti = si[0].split(":");
            if (ti.length > 0) {
                Material m = Material.getMaterial(ti[0].toUpperCase(Locale.ROOT));
                if (m == null)
                    return null;
                id = m;
                if ((ti.length == 2) && (NumberUtils.INT_POSITIVE.matcher(ti[1]).matches()))
                    data = Short.parseShort(ti[1]);
                ItemStack item = new ItemStack(id, amount);
                setDurability(item, data);
                if (!enchant.isEmpty()) {

                    String[] ln = enchant.split(",");
                    for (String ec : ln) {
                        if (ec.isEmpty())
                            continue;

                        Color clr = parseColor(ec);
                        if (clr != null) {
                            if (item.hasItemMeta()
                                    && (item.getItemMeta() instanceof LeatherArmorMeta meta)) {
                                meta.setColor(clr);
                                item.setItemMeta(meta);
                            }
                        } else {
                            String ench = ec;
                            int level = 1;
                            if (ec.contains(":")) {
                                ench = ec.substring(0, ec.indexOf(":"));
                                level = Math.max(1, Rng.nextIntRanged(ec.substring(ench
                                        .length() + 1)));
                            }
                            Enchantment e = ItemUtils.getEnchantmentByName(ench);
                            if (e == null)
                                continue;
                            item.addUnsafeEnchantment(e, level);
                        }
                    }
                }
                if (!name.isEmpty()) {
                    ItemMeta im = item.getItemMeta();
                    im.setDisplayName(ChatColor.translateAlternateColorCodes(
                            '&', name.replace("_", " ")));
                    item.setItemMeta(im);
                }
                if (!loreStr.isEmpty()) {
                    ItemMeta im = item.getItemMeta();
                    String[] ln = loreStr.split("@");
                    List<String> lore = new ArrayList<>();
                    for (String loreLine : ln)
                        lore.add(loreLine.replace("_", " "));
                    im.setLore(lore);
                    item.setItemMeta(im);
                }
                return item;
            }
        }
        return null;
    }

    /**
     * Get item in hand
     *
     * @param player  Player to use
     * @param offhand Check offhand or not
     * @return Item string
     */
    public static String getPlayerItemInHand(Player player, boolean offhand) {
        VirtualItem vi = VirtualItem.fromItemStack(offhand ? player.getInventory().getItemInOffHand() : player.getInventory().getItemInMainHand());
        if (vi == null) return "";
        return vi.toString();
    }
}
