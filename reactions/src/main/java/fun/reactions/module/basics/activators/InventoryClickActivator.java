package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.module.basics.context.InventoryClickContext;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InventoryClickActivator extends Activator {
    // That's pretty freaky stuff
    private final String inventoryName;
    private final ClickType click;
    private final InventoryAction action;
    private final InventoryType inventory;
    private final SlotType slotType;
    private final String numberKey;
    private final String slotStr;
    private final VirtualItem item;

    private InventoryClickActivator(Logic base, String inventoryName, ClickType click, InventoryAction action,
                                    InventoryType inventory, SlotType slotType, String numberKey, String slotStr, String itemStr) {
        super(base);
        this.inventoryName = inventoryName;
        this.click = click;
        this.action = action;
        this.inventory = inventory;
        this.slotType = slotType;
        this.numberKey = numberKey;
        this.slotStr = slotStr;
        this.item = VirtualItem.fromString(itemStr);
    }

    private static String getNumberKeyByName(String keyStr) {
        if (keyStr.equalsIgnoreCase("ANY")) return "ANY";
        int key = Integer.parseInt(keyStr);
        if (key > 0) {
            for (int i = 1; i < 10; i++) {
                if (key == i) return String.valueOf(i);
            }
        }
        return "ANY";
    }

    private static String getSlotByName(String slotStr) {
        int slot = Integer.parseInt(slotStr);
        if (slot > -1) {
            for (int i = 0; i < 36; i++) {
                if (slot == i) return String.valueOf(i);
            }
        }
        return "ANY";
    }

    public static InventoryClickActivator create(Logic base, Parameters param) {
        String inventoryName = param.getString("name", "");
        ClickType click = ClickType.getByName(param.getString("click", "ANY"));
        InventoryAction action = InventoryAction.getByName(param.getString("action", "ANY"));
        InventoryType inventory = InventoryType.getByName(param.getString("inventory", "ANY"));
        SlotType slotType = SlotType.getByName(param.getString("slotType", "ANY"));
        String numberKey = getNumberKeyByName(param.getString("key", "ANY"));
        String slotStr = getSlotByName(param.getString("slot", "ANY"));
        String itemStr = param.getString("item");
        return new InventoryClickActivator(base, inventoryName, click, action, inventory, slotType, numberKey, slotStr, itemStr);
    }

    public static InventoryClickActivator load(Logic base, ConfigurationSection cfg) {
        String inventoryName = cfg.getString("name", "");
        ClickType click = ClickType.getByName(cfg.getString("click-type", "ANY"));
        InventoryAction action = InventoryAction.getByName(cfg.getString("action-type", "ANY"));
        InventoryType inventory = InventoryType.getByName(cfg.getString("inventory-type", "ANY"));
        SlotType slotType = SlotType.getByName(cfg.getString("slot-type", "ANY"));
        String numberKey = cfg.getString("key", "");
        String slotStr = cfg.getString("slot", "");
        String itemStr = cfg.getString("item", "");
        return new InventoryClickActivator(base, inventoryName, click, action, inventory, slotType, numberKey, slotStr, itemStr);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        InventoryClickContext pice = (InventoryClickContext) context;
        if (!inventoryName.isEmpty() && !pice.getInventoryName().equalsIgnoreCase(inventoryName)) return false;
        if (pice.getClickType() == null) return false;
        if (!clickCheck(pice.getClickType())) return false;
        if (!actionCheck(pice.getAction())) return false;
        if (!inventoryCheck(pice.getInventoryType())) return false;
        if (!slotTypeCheck(pice.getSlotType())) return false;
        int key = pice.getNumberKey();
        if (!checkItem(pice.getItem(), key, pice.getBottomInventory())) return false;
        if (!checkNumberKey(key)) return false;
        return checkSlot(pice.getSlot());
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("name", inventoryName);
        cfg.set("click-type", click.name());
        cfg.set("action-type", action.name());
        cfg.set("inventory-type", inventory.name());
        cfg.set("slot-type", slotType.name());
        cfg.set("key", numberKey);
        cfg.set("slot", slotStr);
        cfg.set("item", item.toString());
    }

    private boolean clickCheck(org.bukkit.event.inventory.ClickType ct) {
        if (click.name().equals("ANY")) return true;
        return ct.name().equals(click.name());
    }

    private boolean actionCheck(org.bukkit.event.inventory.InventoryAction act) {
        if (action.name().equals("ANY")) return true;
        return act.name().equals(action.name());
    }

    private boolean inventoryCheck(org.bukkit.event.inventory.InventoryType it) {
        if (inventory.name().equals("ANY")) return true;
        return it.name().equals(inventory.name());
    }

    private boolean slotTypeCheck(org.bukkit.event.inventory.InventoryType.SlotType sl) {
        if (slotType.name().equals("ANY")) return true;
        return sl.name().equals(slotType.name());
    }

    private boolean checkItem(ItemStack item, int key, Inventory bottomInventory) {
        boolean result = this.item.isSimilar(item);
        if (!result && key > -1) return this.item.isSimilar(bottomInventory.getItem(key));
        return result;
    }

    private boolean checkNumberKey(int key) {
        if (numberKey.isEmpty() || numberKey.equals("ANY") || Integer.parseInt(numberKey) <= 0) return true;
        return key == Integer.parseInt(numberKey) - 1;
    }

    private boolean checkSlot(int slot) {
        if (slotStr.isEmpty() || slotStr.equals("ANY") || Integer.parseInt(slotStr) <= 0) return true;
        return slot == Integer.parseInt(slotStr);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "name:" + this.inventoryName +
                "; click:" + this.click.name() +
                "; action:" + this.action.name() +
                "; inventory:" + this.inventory.name() +
                "; slotType:" + this.slotType.name() +
                "; key:" + this.numberKey +
                "; slot:" + this.slotStr +
                ")";
        return sb;
    }

    enum ClickType {
        ANY,
        CONTROL_DROP,
        CREATIVE,
        DROP,
        DOUBLE_CLICK,
        LEFT,
        MIDDLE,
        NUMBER_KEY,
        RIGHT,
        SHIFT_LEFT,
        SHIFT_RIGHT,
        UNKNOWN,
        WINDOW_BORDER_LEFT,
        WINDOW_BORDER_RIGHT;

        public static ClickType getByName(String clickStr) {
            if (clickStr != null) {
                for (ClickType clickType : values()) {
                    if (clickStr.equalsIgnoreCase(clickType.name())) {
                        return clickType;
                    }
                }
            }
            return ClickType.ANY;
        }
    }

    enum InventoryAction {
        ANY,
        CLONE_STACK,
        COLLECT_TO_CURSOR,
        DROP_ALL_CURSOR,
        DROP_ALL_SLOT,
        DROP_ONE_CURSOR,
        DROP_ONE_SLOT,
        HOTBAR_MOVE_AND_READD,
        HOTBAR_SWAP,
        MOVE_TO_OTHER_INVENTORY,
        NOTHING,
        PICKUP_ALL,
        PICKUP_HALF,
        PICKUP_ONE,
        PICKUP_SOME,
        PLACE_ALL,
        PLACE_ONE,
        PLACE_SOME,
        SWAP_WITH_CURSOR,
        UNKNOWN;

        public static InventoryAction getByName(String actionStr) {
            if (actionStr != null) {
                for (InventoryAction action : values()) {
                    if (actionStr.equalsIgnoreCase(action.name())) {
                        return action;
                    }
                }
            }
            return InventoryAction.ANY;
        }
    }

    enum InventoryType {
        ANY,
        ANVIL,
        BEACON,
        BREWING,
        CHEST,
        CRAFTING,
        CREATIVE,
        DISPENSER,
        DROPPER,
        ENCHANTING,
        ENDER_CHEST,
        HOPPER,
        MERCHANT,
        PLAYER,
        SHULKER_BOX,
        WORKBENCH;

        public static InventoryType getByName(String inventoryStr) {
            if (inventoryStr != null) {
                for (InventoryType inventoryType : values()) {
                    if (inventoryStr.equalsIgnoreCase(inventoryType.name())) {
                        return inventoryType;
                    }
                }
            }
            return InventoryType.ANY;
        }
    }

    enum SlotType {
        ANY,
        ARMOR,
        CONTAINER,
        CRAFTING,
        FUEL,
        OUTSIDE,
        QUICKBAR,
        RESULT;

        public static SlotType getByName(String slotStr) {
            if (slotStr != null) {
                for (SlotType slotType : values()) {
                    if (slotStr.equalsIgnoreCase(slotType.name())) {
                        return slotType;
                    }
                }
            }
            return SlotType.ANY;
        }
    }
}
