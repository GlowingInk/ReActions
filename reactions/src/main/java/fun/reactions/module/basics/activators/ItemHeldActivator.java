package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-11-11.
 */
public class ItemHeldActivator extends Activator {
    private final int previousSlot;
    private final int newSlot;
    // TODO: Store VirtualItem
    private final String itemNewStr;
    private final String itemPrevStr;

    private ItemHeldActivator(Logic base, String itemPrevStr, String itemNewStr, int previousSlot, int newSlot) {
        super(base);
        this.itemNewStr = itemNewStr;
        this.itemPrevStr = itemPrevStr;
        this.previousSlot = previousSlot;
        this.newSlot = newSlot;
    }

    public static ItemHeldActivator create(Logic base, Parameters param) {
        String itemNewStr = param.getString("itemnew", "");
        String itemPrevStr = param.getString("itemprev", "");
        int newSlot = param.getInteger("slotnew", 1);
        int previousSlot = param.getInteger("slotprev", 1);
        return new ItemHeldActivator(base, itemPrevStr, itemNewStr, --newSlot, --previousSlot);
    }

    public static ItemHeldActivator load(Logic base, ConfigurationSection cfg) {
        String itemNewStr = cfg.getString("item-new");
        String itemPrevStr = cfg.getString("item-prev");
        int newSlot = cfg.getInt("slot-new", 1);
        int previousSlot = cfg.getInt("slot-prev", 1);
        return new ItemHeldActivator(base, itemPrevStr, itemNewStr, --newSlot, --previousSlot);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context ihe = (Context) context;
        ItemStack itemNew = ihe.newItem;
        ItemStack itemPrev = ihe.previousItem;
        if (!this.itemNewStr.isEmpty() && (!VirtualItem.isSimilar(this.itemNewStr, itemNew)))
            return false;
        if (!this.itemPrevStr.isEmpty() && (!VirtualItem.isSimilar(this.itemPrevStr, itemPrev)))
            return false;
        if (newSlot > -1 && newSlot != ihe.newSlot) return false;
        return previousSlot <= -1 || previousSlot == ihe.previousSlot;
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("item-new", itemNewStr);
        cfg.set("item-prev", itemPrevStr);
        cfg.set("slot-new", newSlot + 1);
        cfg.set("slot-prev", previousSlot + 1);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "itemnew:" + (itemNewStr.isEmpty() ? "-" : itemNewStr) +
                " itemprev:" + (itemPrevStr.isEmpty() ? "-" : itemPrevStr) +
                " slotnew:" + (newSlot + 1) +
                " slotprev:" + (previousSlot + 1) +
                ")";
        return sb;
    }

    /**
     * Created by MaxDikiy on 2017-11-11.
     */
    public static class Context extends ActivationContext {
        private final int newSlot;
        private final int previousSlot;
        private final ItemStack newItem;
        private final ItemStack previousItem;

        public Context(Player player, int newSlot, int previousSlot) {
            super(player);
            this.newSlot = newSlot;
            this.previousSlot = previousSlot;
            this.newItem = this.player.getInventory().getItem(newSlot);
            this.previousItem = this.player.getInventory().getItem(previousSlot);
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return ItemHeldActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            Map<String, Variable> vars = new HashMap<>();
            vars.put(CANCEL_EVENT, Variable.property(false));
            vars.put("slotnew", Variable.simple(newSlot + 1));
            vars.put("slotprev", Variable.simple(previousSlot + 1));
            if (newItem != null) {
                vars.put("itemnew", Variable.lazy(() -> VirtualItem.asString(newItem)));
                vars.put("itemnew-str", Variable.lazy(() -> ItemUtils.toDisplayString(newItem)));
            }
            if (previousItem != null) {
                vars.put("itemprev", Variable.lazy(() -> VirtualItem.asString(previousItem)));
                vars.put("itemprev-str", Variable.lazy(() -> ItemUtils.toDisplayString(previousItem)));
            }
            return vars;
        }
    }
}
