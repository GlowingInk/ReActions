package me.fromgate.reactions.menu;

import lombok.Getter;
import lombok.Setter;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VirtualInventory {

    private int size;
    private String title;
    private List<String> slots;
    private List<String> activators;

    public VirtualInventory(int size, String title) {
        this.size = (size % 9 == 0) ? size : ((size / 9) + 1) * 9;
        this.title = title;
        this.slots = Utils.getEmptyList(this.size);
        this.activators = Utils.getEmptyList(this.size);
    }

    public VirtualInventory(YamlConfiguration cfg, String root) {
        this(9, "&4Re&6Actions menu");
        load(cfg, root);
    }

    @SuppressWarnings("unused")
    public VirtualInventory(Parameters params) {
        title = params.getString("title", "&4Re&6Actions &eMenu");
        size = params.getInteger("size", 9);
        size = (size % 9 == 0) ? size : ((size / 9) + 1) * 9;
        slots = new ArrayList<>();
        activators = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            activators.add(params.getString("exec" + i, ""));
            slots.add(params.getString("exec" + i, ""));
        }
    }

    public void save(YamlConfiguration cfg, String root) {
        cfg.set(root + ".title", title);
        cfg.set(root + ".size", size);
        for (int i = 0; i < size; i++) {
            if (!slots.get(i).isEmpty())
                cfg.set(root + ".slot" + (i + 1) + ".item", slots.get(i));
            if (!activators.get(i).isEmpty())
                cfg.set(root + ".slot" + (i + 1) + ".activator", activators.get(i));
        }
    }

    public void load(YamlConfiguration cfg, String root) {
        this.title = cfg.getString(root + ".title", "&4Re&6Actions menu");
        this.size = cfg.getInt(root + ".size", 9);
        size = (size % 9 == 0) ? size : ((size / 9) + 1) * 9;
        this.slots = Utils.getEmptyList(this.size);
        this.activators = Utils.getEmptyList(this.size);
        for (int i = 1; i <= size; i++) {
            this.slots.set(i - 1, cfg.getString(root + ".slot" + i + ".item", ""));
            this.activators.set(i - 1, cfg.getString(root + ".slot" + i + ".activator", ""));
        }
    }

    public Inventory getInventory() {
        RaInventoryHolder holder = new RaInventoryHolder(activators);
        Inventory inv = Bukkit.createInventory(holder, (size % 9 == 0) ? size : ((size / 9) + 1) * 9, ChatColor.translateAlternateColorCodes('&', title));
        holder.setInventory(inv);
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).isEmpty()) continue;
            ItemStack item = VirtualItem.fromString(slots.get(i));
            if (item == null) continue;
            inv.setItem(i, item);
        }
        return inv;
    }
}
