package me.fromgate.reactions.menu;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.context.Variables;
import me.fromgate.reactions.module.basics.DetailsManager;
import me.fromgate.reactions.util.FileUtils;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InventoryMenu implements Listener { // TODO Requires major refactoring

    private static final Map<String, VirtualInventory> menu = new HashMap<>();
    private static Variables vars;

    public static void init(ReActions.Platform platform) {
        load();
        save();
        Plugin plugin = platform.getPlugin();
        plugin.getServer().getPluginManager().registerEvents(new InventoryMenu(), plugin);
    }

    public static void load() {
        menu.clear();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "menu.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        if (FileUtils.loadCfg(cfg, f, "Failed to load menu configuration file"))
            for (String key : cfg.getKeys(false)) {
                VirtualInventory vi = new VirtualInventory(cfg, key);
                putMenu(key, vi);
            }
    }

    public static void save() {
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "menu.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        for (String key : menu.keySet()) {
            getMenu(key).save(cfg, key);
        }
        FileUtils.saveCfg(cfg, f, "Failed to save menu configuration file");
    }

    public static boolean add(String id, int size, String title) {
        if (containsMenu(id)) return false;
        putMenu(id, new VirtualInventory(size, title));
        save();
        return true;
    }

    public static boolean set(String id, Parameters params) {
        if (!containsMenu(id)) return false;
        VirtualInventory vi = getMenu(id);
        String title = params.getString("title", vi.getTitle());
        int size = params.getInteger("size", vi.getSize());
        size = (size % 9 == 0) ? size : ((size / 9) + 1) * 9;

        List<String> activators = vi.getActivators();
        if (activators.size() < size)
            for (int i = activators.size(); i < size; i++) activators.add("");
        List<String> slots = vi.getSlots();
        if (slots.size() < size)
            for (int i = slots.size(); i < size; i++) slots.add("");
        for (int i = 1; i <= size; i++) {
            if (params.contains("activator" + i))
                activators.set(i - 1, params.getString("activator" + i, ""));
            if (params.contains("item" + i))
                slots.set(i - 1, params.getString("item" + i, ""));
        }
        vi.setTitle(title);
        vi.setSize(size);
        // vi.setSlots(slots);
        // vi.setActivators(activators);
        putMenu(id, vi);
        save();
        return true;
    }

    public static boolean remove(String id) {
        return menu.remove(id) != null;
    }

    private static List<String> getActivators(Parameters param) {
        if (param.contains("menu")) {
            String id = param.getString("menu", "");
            if (containsMenu(id)) return getMenu(id).getActivators();
        } else {
            int size = param.getInteger("size", 9);
            if (size > 0) {
                List<String> activators = new ArrayList<>();
                for (int i = 1; i <= size; i++)
                    activators.add(param.getString("exec" + i, ""));
                return activators;
            }
        }
        return new ArrayList<>();
    }

    public static Inventory getInventory(Parameters param) {
        RaInventoryHolder holder = new RaInventoryHolder(getActivators(param));
        Inventory inv = null;
        if (param.contains("menu")) {
            String id = param.getString("menu", "");
            if (containsMenu(id)) inv = getMenu(id).getInventory();
        } else {
            String title = param.getString("title", "ReActions Menu");
            int size = param.getInteger("size", 9);
            if (size <= 0) return null;
            inv = Bukkit.createInventory(holder, size, title);
            for (int i = 1; i <= size; i++) {
                String slotStr = "slot" + i;
                if (!param.contains(slotStr)) continue;
                ItemStack slotItem = VirtualItem.asItemStack(param.getString(slotStr));
                if (slotItem == null) continue;
                inv.setItem(i - 1, slotItem);
            }
        }
        holder.setInventory(inv);
        return inv;
    }

    public static boolean createAndOpenInventory(Player player, Parameters params, Variables vars) {
        InventoryMenu.vars = vars;
        Inventory inv = getInventory(params);
        if (inv == null) return false;
        openInventory(player, inv);
        return true;
    }

    private static void openInventory(final Player player, final Inventory inv) {
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
            if (player.isOnline()) {
                player.closeInventory();
                player.openInventory(inv);
            }
        }, 1);
    }

    private static boolean isMenu(Inventory inventory) {
        return inventory.getHolder() instanceof RaInventoryHolder;
    }

    private static List<String> getActivators(Inventory inventory) {
        if (isMenu(inventory)) return ((RaInventoryHolder) inventory.getHolder()).getActivators();
        return new ArrayList<>();
    }

    private static VirtualInventory getMenu(String id) {
        return menu.get(id.toLowerCase(Locale.ROOT));
    }

    private static void putMenu(String id, VirtualInventory inventory) {
        menu.put(id.toLowerCase(Locale.ROOT), inventory);
    }

    private static boolean containsMenu(String id) {
        return menu.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public static void printMenu(CommandSender sender, String id) {
        if (containsMenu(id)) {
            VirtualInventory vi = getMenu(id);
            Msg.printMSG(sender, "msg_menuinfotitle", 'e', '6', id, vi.getSize(), vi.getTitle());
            for (int i = 0; i < vi.getSize(); i++) {
                String exec = vi.getActivators().get(i);
                String slot = vi.getSlots().get(i);
                if (exec.isEmpty() && slot.isEmpty()) continue;
                slot = itemToString(slot);
                Msg.printMSG(sender, "msg_menuinfoslot", i + 1, exec.isEmpty() ? "N/A" : exec, slot.isEmpty() ? "AIR" : slot);
            }
        } else Msg.printMSG(sender, "msg_menuidfail", id);
    }

    public static void printMenuList(CommandSender sender, int pageNum, String mask) {
        int linesPerPage = (sender instanceof Player) ? 15 : 10000;
        List<String> menuList = new ArrayList<>();
        for (String id : menu.keySet()) {
            if (mask.isEmpty() || id.toLowerCase(Locale.ROOT).contains(mask.toLowerCase(Locale.ROOT))) {
                menuList.add(id + " : " + getMenu(id).getTitle());
            }
        }
        Msg.printPage(sender, menuList, Msg.MSG_MENULIST, pageNum, linesPerPage);
    }

    private static String itemToString(String itemStr) {
        if (itemStr.isEmpty()) return "AIR";
        ItemStack item = VirtualItem.asItemStack(itemStr);
        if (item == null || item.getType() == Material.AIR) return "AIR";
        String returnStr = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : "";
        String itemTypeData = item.getType().name() + (ItemUtils.getDurability(item) == 0 ? "" : ":" + ItemUtils.getDurability(item)) + (item.getAmount() == 1 ? "" : "*" + item.getAmount());
        return ChatColor.stripColor(returnStr.isEmpty() ? itemTypeData : returnStr + "[" + itemTypeData + "]");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isMenu(event.getInventory())) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int clickedSlot = event.getRawSlot();
        if (clickedSlot < 0 || clickedSlot >= event.getInventory().getSize()) return;
        List<String> activators = getActivators(event.getInventory());
        if (activators.size() > clickedSlot) {
            String activator = activators.get(clickedSlot);
            if (!activator.isEmpty()) {
                DetailsManager.triggerExec(player, Parameters.fromString(activator, "activator"), vars);
            }
        }
        // TODO: Do not close menu option?
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryMove(InventoryDragEvent event) {
        if (isMenu(event.getInventory())) event.setCancelled(true);
    }
}
