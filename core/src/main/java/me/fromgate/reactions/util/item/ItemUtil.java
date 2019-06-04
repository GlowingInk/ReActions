package me.fromgate.reactions.util.item;

import me.fromgate.reactions.util.BukkitCompatibilityFix;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class ItemUtil {

    private static Random random = new Random();

    private final static Pattern INT_GZ = Pattern.compile("[1-9]+[0-9]*");
    private final static Pattern D = Pattern.compile("\\d+");
    private final static Pattern ITEM_D = Pattern.compile("item\\d+|ITEM\\d+");
    private final static Pattern SET_D = Pattern.compile("set\\d+|SET\\d+");

    public static void giveItemOrDrop(Player player, ItemStack item) {
        for (ItemStack itemDrop : player.getInventory().addItem(item).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), itemDrop);
        }
    }

    public static VirtualItem itemFromString(String itemStr) {
        return VirtualItem.fromString(itemStr);
    }

    public static void giveItemOrDrop(Player player, String itemStr) {
        VirtualItem vi = itemFromString(itemStr);
        if (vi == null) return;
        giveItemOrDrop(player, vi);
    }

    public static boolean removeItemInHand(Player player, String itemStr) {
        ItemStack inHand = BukkitCompatibilityFix.getItemInHand(player);
        if (inHand == null || inHand.getType() == Material.AIR) return false;
        VirtualItem hand = VirtualItem.fromItemStack(inHand);
        VirtualItem vi = removeItemFromStack(hand, itemStr);
        if (vi == null) return false;
        BukkitCompatibilityFix.setItemInHand(player, vi.getType() == Material.AIR ? null : vi);
        return true;
    }

    public static boolean removeItemInOffHand(Player player, String itemStr) {
        ItemStack inHand = BukkitCompatibilityFix.getItemInOffHand(player);
        if (inHand == null || inHand.getType() == Material.AIR) return false;
        VirtualItem hand = VirtualItem.fromItemStack(inHand);
        VirtualItem vi = removeItemFromStack(hand, itemStr);
        if (vi == null) return false;
        BukkitCompatibilityFix.setItemInOffHand(player, vi.getType() == Material.AIR ? null : vi);
        return true;
    }


    public static boolean removeItemInInventory(Inventory inventory, String itemStr) {
        Map<String, String> itemParams = VirtualItem.parseParams(itemStr);
        return removeItemInInventory(inventory, itemParams);
    }

    private static boolean removeItemInInventory(Inventory inventory, Map<String, String> itemParams) {
        int amountToRemove = Integer.parseInt(VirtualItem.getParam(itemParams, "amount", "1"));
        //int countItems =  countItemsInventory (inventory, itemParams);
        //if (amountToRemove>countItems) return false;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) continue;
            VirtualItem vi = ItemUtil.itemFromItemStack(inventory.getItem(i));
            if (!vi.compare(itemParams, 1)) continue;
            if (vi.getAmount() <= amountToRemove) {
                amountToRemove -= vi.getAmount();
                inventory.setItem(i, null);
            } else {
                vi.setAmount(vi.getAmount() - amountToRemove);
                inventory.setItem(i, vi);
                amountToRemove = 0;
            }
            if (amountToRemove == 0) return true;
        }
        return false;
    }

    private static int countItemsInventory(Inventory inventory, Map<String, String> itemParams) {
        int count = 0;
        for (ItemStack slot : inventory) {
            if (slot == null || slot.getType() == Material.AIR) continue;
            VirtualItem vi = ItemUtil.itemFromItemStack(slot);
            if (!vi.compare(itemParams, 1)) continue;
            count += slot.getAmount();
        }
        return count;
    }

    /**
     * @param stack   - source item
     * @param itemStr - item description to remove
     * @return - item stack contained left items (if all items removed - remove
     */
    private static VirtualItem removeItemFromStack(VirtualItem stack, String itemStr) {
        if (!ItemUtil.compareItemStr(stack, itemStr)) return null;
        int amountToRemove = getAmount(itemStr);
        if (amountToRemove <= 0) return null;
        int leftAmount = stack.getAmount() - amountToRemove;
        if (leftAmount < 0) return null;
        VirtualItem result = VirtualItem.fromItemStack(stack);
        if (leftAmount == 0) result.setType(Material.AIR);
        else result.setAmount(leftAmount);
        return result;
    }

    private static int getAmount(String itemStr) {
        Map<String, String> itemMap = VirtualItem.parseParams(itemStr);
        String amountStr = VirtualItem.getParam(itemMap, "amount", "1");
        if (D.matcher(amountStr).matches()) return Integer.parseInt(amountStr);
        return 1;
    }


    public static boolean hasItemInInventory(Player player, String itemStr) {
        return hasItemInInventory(player.getInventory(), itemStr);
    }


    public static boolean hasItemInInventory(Inventory inventory, String itemStr) {
        int countAmount = countItemsInInventory(inventory, itemStr);
        Variables.setTempVar("item_amount", countAmount == 0 ? "0" : String.valueOf(countAmount));
        int amount = getAmount(itemStr);
        return countAmount >= amount;
    }

    public static int countItemsInInventory(Inventory inventory, String itemStr) {
        Map<String, String> itemMap = VirtualItem.parseParams(itemStr);
        return countItemsInventory(inventory, itemMap);
    }

    @SuppressWarnings("deprecation")
    public static VirtualItem itemFromBlock(Block block) {
        if (block == null) return itemFromString("AIR");
        return itemFromItemStack(new ItemStack(block.getType(), 1, block.getData()));
    }

    public static VirtualItem itemFromItemStack(ItemStack item) {
        return VirtualItem.fromItemStack(item);
    }

    public static ItemStack parseItemStack(String string) {
        return itemFromString(string);
    }

    @SuppressWarnings("deprecation")
    public static boolean compareItemStr(Block block, String itemStr) {
        if (block == null || block.getType() == Material.AIR) return false;
        ItemStack item = new ItemStack(block.getType(), 1, block.getData());
        return compareItemStr(item, itemStr);
    }

    public static boolean compareItemStr(ItemStack item, String itemStr) {
        if (item == null || item.getType() == Material.AIR) return false;
        return itemFromItemStack(item).compare(itemStr);
    }

    public static boolean compareItemStr(ItemStack item, String itemStr, boolean allowHand) {
        if (item != null && item.getType() != Material.AIR) return compareItemStr(item, itemStr);
        if (!allowHand) return false;
        return (itemStr.equalsIgnoreCase("HAND") || itemStr.equalsIgnoreCase("AIR"));
    }

    public static boolean removeItemInInventory(Player player, String itemStr) {
        return removeItemInInventory(player.getInventory(), itemStr);
    }

    public static ItemStack getRndItem(String str) {
        if (str.isEmpty()) return new ItemStack(Material.AIR);
        String[] ln = str.split(",");
        if (ln.length == 0) return new ItemStack(Material.AIR);

        ItemStack item = ItemUtil.parseItemStack(ln[tryChance(ln.length)]);

        if (item == null) return new ItemStack(Material.AIR);
        item.setAmount(1);
        return item;
    }


    /*
     *  <item>;<item>;<item>[%<chance>]/<item>;<item>;<item>[%<chance>]
     *
     */
    public static List<ItemStack> parseItemStacksOld(String items) {
        List<ItemStack> stacks = new ArrayList<>();
        String[] ln = items.split(";"); // ВОТ ЭТО ЛОМАЕТ К ЧЕРТЯМ НОВЫЙ ФОРМАТ!!!
        for (String item : ln) {
            VirtualItem vi = itemFromString(item);
            if (vi != null) stacks.add(vi);
        }
        return stacks;
    }


    public static String itemToString(ItemStack item) {
        VirtualItem vi = itemFromItemStack(item);
        return vi == null ? "" : vi.toString();
    }


    public static String toDisplayString(List<ItemStack> items) {
        StringBuilder sb = new StringBuilder();
        for (ItemStack i : items) {
            VirtualItem vi = VirtualItem.fromItemStack(i);
            if (sb.length() > 0) sb.append(", ");
            sb.append(vi.toDisplayString());
        }
        return sb.toString();
    }

    //item:{item1:{[...] chance:50} item2:{} item3:{}

    public static VirtualItem itemFromMap(Param params) {
        return VirtualItem.fromMap(params.getMap());

    }

    public static List<ItemStack> parseItemsSet(Param params) {
        List<ItemStack> items = new ArrayList<>();
        for (String key : params.keySet()) {
            if (ITEM_D.matcher(key).matches()) {
                String itemStr = params.getParam(key, "");
                VirtualItem vi = itemFromString(itemStr);
                if (vi != null) items.add(vi);
            }
        }
        if (items.isEmpty()) {
            VirtualItem item = itemFromMap(params);
            if (item != null) items.add(item);
        }
        return items;
    }

    /*
     * set1:{item1:{}  item2:{} item3:{} chance:50}  set2:{item1:{}  item2:{} item3:{} chance:50}
     *
     *
     */
    public static List<ItemStack> parseRandomItemsStr(String items) {
        Param params = new Param(items);
        if (params.matchAnyParam(SET_D)) {
            Map<List<ItemStack>, Integer> sets = new HashMap<>();
            int maxChance = 0;
            int nochcount = 0;
            for (String key : params.keySet()) {
                if (!SET_D.matcher(key).matches()) continue;
                Param itemParams = new Param(params.getParam(key));
                List<ItemStack> itemList = parseItemsSet(itemParams);
                if (itemList == null || itemList.isEmpty()) continue;
                int chance = itemParams.getParam("chance", -1);
                if (chance > 0) maxChance += chance;
                else nochcount++;
                sets.put(itemList, chance);
            }
            int eqperc = (nochcount * 100) / sets.size();
            maxChance = maxChance + eqperc * nochcount;
            int rnd = tryChance(maxChance);
            int curchance = 0;
            for (List<ItemStack> stack : sets.keySet()) {
                curchance = curchance + (sets.get(stack) < 0 ? eqperc : sets.get(stack));
                if (rnd <= curchance) return stack;
            }
        } else if (params.matchAnyParam("item\\d+|ITEM\\d+")) {
            return parseItemsSet(params);
        } else {
            VirtualItem vi = itemFromString(items);
            if (vi != null) {
                List<ItemStack> iList = new ArrayList<>();
                iList.add(vi);
                return iList;
            }

        }
        return null;
    }


    //id:data*amount@enchant:level,color;id:data*amount%chance/id:data*amount@enchant:level,color;id:data*amount%chance
    public static String parseRandomItemsStrOld(String items) {
        if (items.isEmpty()) return "";
        String[] loots = items.split("/");
        Map<String, Integer> drops = new HashMap<>();
        int maxchance = 0;
        int nochcount = 0;
        for (String loot : loots) {
            String[] ln = loot.split("%");
            if (ln.length > 0) {
                String stacks = ln[0];
                if (stacks.isEmpty()) continue;
                int chance = -1;
                if ((ln.length == 2) && (INT_GZ.matcher(ln[1]).matches())) {
                    chance = Integer.parseInt(ln[1]);
                    maxchance += chance;
                } else nochcount++;
                drops.put(stacks, chance);
            }
        }
        if (drops.isEmpty()) return "";
        int eqperc = (nochcount * 100) / drops.size();
        maxchance = maxchance + eqperc * nochcount;
        int rnd = tryChance(maxchance);
        int curchance = 0;
        for (String stack : drops.keySet()) {
            curchance = curchance + (drops.get(stack) < 0 ? eqperc : drops.get(stack));
            if (rnd <= curchance) return stack;
        }
        return "";
    }

    private static int tryChance(int chance) {
        return random.nextInt(chance);
    }

    public static String toDisplayString(String itemStr) {
        VirtualItem vi = itemFromString(itemStr);
        if (vi != null) return vi.toDisplayString();
        Map<String, String> itemMap = VirtualItem.parseParams(itemStr);
        String name = itemMap.containsKey("name") ? itemMap.get("name") : itemMap.getOrDefault("type", null);
        if (name == null) return itemStr;
        int amount = getAmount(itemStr);
        String data = VirtualItem.getParam(itemMap, "data", "0");
        StringBuilder sb = new StringBuilder(name);
        if (!itemMap.containsKey("name") && !data.equals("0")) sb.append(":").append(data);
        if (amount > 1) sb.append("*").append(amount);
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', sb.toString()));
    }

}
