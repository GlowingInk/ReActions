package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.Utils;
import fun.reactions.util.enums.SafeEnum;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class InventoryClickActivator extends Activator {
    // That's pretty freaky stuff
    private final String inventoryName;
    private final SafeEnum<ClickType> click;
    private final SafeEnum<InventoryAction> action;
    private final SafeEnum<InventoryType> inventory;
    private final SafeEnum<SlotType> slotType;
    private final String numberKey;
    private final String slotStr;
    private final VirtualItem item;

    private InventoryClickActivator(Logic base, String inventoryName, ClickType click, InventoryAction action,
                                    InventoryType inventory, SlotType slotType, String numberKey, String slotStr, String itemStr) {
        super(base);
        this.inventoryName = inventoryName;
        this.click = new SafeEnum<>(click);
        this.action = new SafeEnum<>(action);
        this.inventory = new SafeEnum<>(inventory);
        this.slotType = new SafeEnum<>(slotType);
        this.numberKey = numberKey;
        this.slotStr = slotStr;
        this.item = VirtualItem.fromString(itemStr);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context pice = (Context) context;
        return (inventoryName.isEmpty() || pice.inventoryName.equalsIgnoreCase(inventoryName)) &&
                click.isValidFor(pice.clickType) &&
                action.isValidFor(pice.action) &&
                inventory.isValidFor(pice.inventoryType) &&
                slotType.isValidFor(pice.slotType) &&
                checkItem(pice.item, pice.numberKey, pice.getBottomInventory()) &&
                checkNumberKey(pice.numberKey) &&
                checkSlot(pice.slot);
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
        ClickType click = param.getEnum("click", ClickType.class);
        InventoryAction action = param.getEnum("action", InventoryAction.class);
        InventoryType inventory = param.getEnum("inventory", InventoryType.class);
        SlotType slotType = param.getEnum("slotType", SlotType.class);
        String numberKey = getNumberKeyByName(param.getString("key", "ANY"));
        String slotStr = getSlotByName(param.getString("slot", "ANY"));
        String itemStr = param.getString("item");
        return new InventoryClickActivator(base, inventoryName, click, action, inventory, slotType, numberKey, slotStr, itemStr);
    }

    public static InventoryClickActivator load(Logic base, ConfigurationSection cfg) {
        String inventoryName = cfg.getString("name", "");
        ClickType click = Utils.getEnum(ClickType.class, cfg.getString("click-type", ""));
        InventoryAction action = Utils.getEnum(InventoryAction.class, cfg.getString("action-type", ""));
        InventoryType inventory = Utils.getEnum(InventoryType.class, cfg.getString("inventory-type", ""));
        SlotType slotType = Utils.getEnum(SlotType.class, cfg.getString("slot-type", ""));
        String numberKey = cfg.getString("key", "");
        String slotStr = cfg.getString("slot", "");
        String itemStr = cfg.getString("item", "");
        return new InventoryClickActivator(base, inventoryName, click, action, inventory, slotType, numberKey, slotStr, itemStr);
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

    public static class Context extends ActivationContext {
        public static final String ITEM = "item";

        private final ItemStack item;
        private final InventoryAction action;
        private final ClickType clickType;
        private final SlotType slotType;
        private final InventoryType inventoryType;
        private final int numberKey;
        private final int slot;
        private final String inventoryName;
        private final InventoryView inventoryView;

        public Context(Player p, InventoryAction action, ClickType clickType, Inventory inventory, SlotType slotType,
                       ItemStack item, int numberKey, InventoryView inventoryView, int slot) {
            super(p);
            this.inventoryName = inventoryView.getTitle();
            this.action = action;
            this.clickType = clickType;
            this.inventoryType = inventory.getType();
            this.slotType = slotType;
            this.item = item;
            this.numberKey = numberKey;
            this.slot = slot;
            this.inventoryView = inventoryView;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return InventoryClickActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of(
                    CANCEL_EVENT, Variable.property(false),
                    ITEM, Variable.lazy(() -> VirtualItem.asString(item)),
                    "name", Variable.simple(inventoryName),
                    "click", Variable.simple(clickType),
                    "action", Variable.simple(action),
                    "slottype", Variable.simple(slotType),
                    "inventory", Variable.simple(inventoryType),
                    "key", Variable.simple(numberKey + 1),
                    "itemkey", numberKey > -1 ? Variable.lazy(() -> VirtualItem.asString(getBottomInventory().getItem(numberKey))) : Variable.simple(""),
                    "slot", Variable.simple(slot)
            );
        }

        public Inventory getBottomInventory() {
            return this.inventoryView.getBottomInventory();
        }
    }
}
