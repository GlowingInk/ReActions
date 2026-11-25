package fun.reactions.menu;

import fun.reactions.model.environment.Variables;
import fun.reactions.util.item.ItemManager;
import fun.reactions.util.parameter.Parameters;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class Menu {
    public static final String DEFAULT_TITLE = "&4Re&6Actions Menu";
    public static final int ROW_SIZE = 9;
    public static final int MAX_ROWS = 6;
    public static final int MAX_SIZE = ROW_SIZE * MAX_ROWS;

    private static final String CHEST_PREFIX = "generic_9x";

    private final String title;
    private final Key type;
    private final boolean chest;
    private final List<Slot> slots;
    private final boolean closeOnClick;

    public Menu(@NotNull String title, @NotNull Key type, @NotNull List<Slot> slots, boolean closeOnClick) {
        int rows = chestRows(type);
        int size = rows > 0 ? rows * ROW_SIZE : trimmedSize(slots);
        List<Slot> resized = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            resized.add(i < slots.size() ? slots.get(i) : Slot.EMPTY);
        }
        this.title = title;
        this.type = type;
        this.chest = rows > 0;
        this.slots = Collections.unmodifiableList(resized);
        this.closeOnClick = closeOnClick;
    }

    public static @NotNull Menu empty(int rows, @Nullable String title) {
        return new Menu(title == null ? DEFAULT_TITLE : title, chestType(rows), List.of(), true);
    }

    public static @NotNull Menu fromInventory(
            @NotNull Inventory inventory,
            @Nullable String title,
            @NotNull ItemManager items
    ) {
        List<Slot> slots = new ArrayList<>(inventory.getSize());
        for (ItemStack item : inventory.getContents()) {
            slots.add(item == null || item.getType().isAir()
                    ? Slot.EMPTY
                    : new Slot(items.asString(item), ""));
        }
        return new Menu(title == null ? DEFAULT_TITLE : title, chestTypeOfSize(slots.size()), slots, true);
    }

    public static @NotNull Menu fromConfig(@NotNull ConfigurationSection cfg) {
        String typeName = cfg.getString("type");
        Key type = typeName == null ? chestTypeOfSize(cfg.getInt("size", ROW_SIZE)) : parseType(typeName);
        List<Slot> slots = new ArrayList<>(MAX_SIZE);
        for (int i = 1; i <= MAX_SIZE; i++) {
            slots.add(new Slot(
                    cfg.getString("slot" + i + ".item", ""),
                    cfg.getString("slot" + i + ".activator", "")
            ));
        }
        return new Menu(cfg.getString("title", DEFAULT_TITLE), type, slots, cfg.getBoolean("close", true));
    }

    public static @NotNull Menu fromParameters(@NotNull Parameters params) {
        String typeName = params.getString("type", null);
        Key type = typeName == null ? chestTypeOfSize(params.getInteger("size", ROW_SIZE)) : parseType(typeName);
        List<Slot> slots = new ArrayList<>(MAX_SIZE);
        for (int i = 1; i <= MAX_SIZE; i++) {
            slots.add(new Slot(params.getString("slot" + i, ""), params.getString("exec" + i, "")));
        }
        return new Menu(params.getString("title", DEFAULT_TITLE), type, slots, params.getBoolean("close", true));
    }

    public void save(@NotNull ConfigurationSection cfg) {
        cfg.set("title", title);
        cfg.set("type", type.namespace().equals(Key.MINECRAFT_NAMESPACE) ? type.value() : type.asString());
        if (!closeOnClick) cfg.set("close", false);
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            if (!slot.item().isEmpty()) cfg.set("slot" + (i + 1) + ".item", slot.item());
            if (!slot.activator().isEmpty()) cfg.set("slot" + (i + 1) + ".activator", slot.activator());
        }
    }

    public @NotNull Menu withCloseOnClick(boolean closeOnClick) {
        return this.closeOnClick == closeOnClick ? this : new Menu(title, type, slots, closeOnClick);
    }

    public @NotNull String title() {
        return title;
    }

    public @NotNull Key type() {
        return type;
    }

    public boolean isChest() {
        return chest;
    }

    public int size() {
        return slots.size();
    }

    public @NotNull @Unmodifiable List<Slot> slots() {
        return slots;
    }

    public @NotNull Menu.Slot slot(int index) {
        return index >= 0 && index < slots.size() ? slots.get(index) : Slot.EMPTY;
    }

    public boolean closeOnClick() {
        return closeOnClick;
    }

    public static @NotNull Key chestType(int rows) {
        return Key.key(CHEST_PREFIX + Math.clamp(rows, 1, MAX_ROWS));
    }

    private static @NotNull Key chestTypeOfSize(int size) {
        return chestType((size + ROW_SIZE - 1) / ROW_SIZE);
    }

    private static int chestRows(@NotNull Key type) {
        if (!type.namespace().equals(Key.MINECRAFT_NAMESPACE) || !type.value().startsWith(CHEST_PREFIX)) return 0;
        String rows = type.value().substring(CHEST_PREFIX.length());
        if (rows.length() != 1) return 0;
        int count = rows.charAt(0) - '0';
        return count >= 1 && count <= MAX_ROWS ? count : 0;
    }

    private static @NotNull Key parseType(@NotNull String type) {
        return Key.key(type.toLowerCase(Locale.ROOT));
    }

    private static int trimmedSize(@NotNull List<Slot> slots) {
        int size = Math.min(slots.size(), MAX_SIZE);
        while (size > 0 && slots.get(size - 1).equals(Slot.EMPTY)) size--;
        return size;
    }

    static final class Holder implements InventoryHolder {
        private final Menu menu;
        private final Variables variables;
        private Inventory inventory;

        Holder(@NotNull Menu menu, @NotNull Variables variables) {
            this.menu = menu;
            this.variables = variables;
        }

        @NotNull Menu menu() {
            return menu;
        }

        @NotNull Variables variables() {
            return variables;
        }

        void setInventory(@NotNull Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }

    public record Slot(@NotNull String item, @NotNull String activator) {
        public static final Slot EMPTY = new Slot("", "");
    }
}
