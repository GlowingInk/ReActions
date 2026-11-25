package fun.reactions.menu;

import fun.reactions.ReActions;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basic.ContextManager;
import fun.reactions.util.ConfigUtils;
import fun.reactions.util.parameter.Parameters;
import ink.glowing.utils.hash.CaseInsensitive;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static ink.glowing.text.InkyMessage.inkyMessage;

public final class MenuManager implements Listener {
    private static final Set<InventoryType> UNSUPPORTED_TYPES = EnumSet.of(
            InventoryType.PLAYER,
            InventoryType.ENCHANTING,
            InventoryType.LOOM,
            InventoryType.CARTOGRAPHY,
            InventoryType.GRINDSTONE,
            InventoryType.CRAFTER
    );

    private final ReActions.Platform platform;
    private final File file;
    private final Map<String, Menu> menus;

    public MenuManager(@NotNull ReActions.Platform platform) {
        this.platform = platform;
        this.file = new File(platform.getDataFolder(), "menu.yml");
        this.menus = CaseInsensitive.newLinkedMap();
    }

    public void load() {
        menus.clear();
        YamlConfiguration cfg = new YamlConfiguration();
        if (!ConfigUtils.loadConfig(cfg, file, "Failed to load menu configuration file")) return;
        for (String name : cfg.getKeys(false)) {
            ConfigurationSection section = cfg.getConfigurationSection(name);
            if (section == null) continue;
            Menu menu;
            try {
                menu = Menu.fromConfig(section);
            } catch (InvalidKeyException ex) {
                platform.logger().warn("Menu '{}' has invalid type: {}", name, ex.getMessage());
                continue;
            }
            if (!menu.isChest() && getInventoryType(menu.type()) == null) {
                platform.logger().warn("Menu '{}' has unsupported type '{}'", name, menu.type());
                continue;
            }
            menus.put(name, menu);
        }
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<String, Menu> entry : menus.entrySet()) {
            entry.getValue().save(cfg.createSection(entry.getKey()));
        }
        ConfigUtils.saveConfig(cfg, file, "Failed to save menu configuration file");
    }

    public boolean add(@NotNull String name, @NotNull Menu menu) {
        if (menus.putIfAbsent(name, menu) != null) return false;
        save();
        return true;
    }

    public boolean remove(@NotNull String name) {
        if (menus.remove(name) == null) return false;
        save();
        return true;
    }

    public @Nullable Menu getMenu(@NotNull String name) {
        return menus.get(name);
    }

    public boolean contains(@NotNull String name) {
        return menus.containsKey(name);
    }

    public @NotNull @UnmodifiableView Set<String> getNames() {
        return Collections.unmodifiableSet(menus.keySet());
    }

    public boolean open(@NotNull Player player, @NotNull Menu menu, @NotNull Variables variables) {
        Inventory inventory = createInventory(menu, variables);
        if (inventory == null) return false;
        Bukkit.getScheduler().runTask(platform.getPlugin(), () -> {
            if (player.isOnline()) player.openInventory(inventory);
        });
        return true;
    }

    private @Nullable Inventory createInventory(@NotNull Menu menu, @NotNull Variables variables) {
        Menu.Holder holder = new Menu.Holder(menu, variables);
        Component title = inkyMessage().deserialize(menu.title());
        Inventory inventory;
        if (menu.isChest()) {
            inventory = Bukkit.createInventory(holder, menu.size(), title);
        } else {
            InventoryType type = getInventoryType(menu.type());
            if (type == null) return null;
            inventory = Bukkit.createInventory(holder, type, title);
        }
        holder.setInventory(inventory);
        int size = Math.min(menu.size(), inventory.getSize());
        for (int i = 0; i < size; i++) {
            String item = menu.slot(i).item();
            if (item.isEmpty()) continue;
            ItemStack stack = platform.getItems().asItemStack(item);
            if (stack != null) inventory.setItem(i, stack);
        }
        return inventory;
    }

    private static @Nullable InventoryType getInventoryType(@NotNull Key menuType) {
        for (InventoryType type : InventoryType.values()) {
            if (!type.isCreatable() || UNSUPPORTED_TYPES.contains(type)) continue;
            MenuType candidate = type.getMenuType();
            if (candidate != null && candidate.key().equals(menuType)) return type;
        }
        return null;
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder(false) instanceof Menu.Holder holder)) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inventory.getSize()) return;
        Player player = (Player) event.getWhoClicked();
        String activator = holder.menu().slot(slot).activator();
        if (!activator.isEmpty()) {
            ContextManager.triggerFunction(player, Parameters.fromString(activator, "activator"), holder.variables());
        }
        if (holder.menu().closeOnClick()) {
            Bukkit.getScheduler().runTask(platform.getPlugin(), () -> {
                if (player.getOpenInventory().getTopInventory() == inventory) player.closeInventory();
            });
        }
    }

    @EventHandler
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (event.getInventory().getHolder(false) instanceof Menu.Holder) event.setCancelled(true);
    }
}
