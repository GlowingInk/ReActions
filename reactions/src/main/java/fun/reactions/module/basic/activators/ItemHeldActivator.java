package fun.reactions.module.basic.activators;

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
 * @author MaxDikiy
 * @since 11/11/2017
 */
public class ItemHeldActivator extends Activator {
    private final int slotPrev;
    private final int slotNew;
    private final VirtualItem virtualItemNew;
    private final VirtualItem virtualItemPrev;

    private ItemHeldActivator(Logic base, VirtualItem virtualItemPrev, VirtualItem virtualItemNew, int slotPrev, int slotNew) {
        super(base);
        this.virtualItemNew = virtualItemNew;
        this.virtualItemPrev = virtualItemPrev;
        this.slotPrev = slotPrev;
        this.slotNew = slotNew;
    }

    public static ItemHeldActivator create(Logic base, Parameters param) {
        return new ItemHeldActivator(base,
                param.getSafe("itemprev", VirtualItem::fromString),
                param.getSafe("itemnew", VirtualItem::fromString),
                param.getInteger("slotprev", 1) - 1,
                param.getInteger("slotnew", 1) - 1
        );
    }

    public static ItemHeldActivator load(Logic base, ConfigurationSection cfg) {
        return new ItemHeldActivator(base,
                VirtualItem.fromString(cfg.getString("item-prev", "")),
                VirtualItem.fromString(cfg.getString("item-new", "")),
                cfg.getInt("slot-prev", 1) - 1,
                cfg.getInt("slot-new", 1) - 1
        );
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context ihe = (Context) context;
        ItemStack itemNew = ihe.newItem;
        ItemStack itemPrev = ihe.previousItem;
        if (!virtualItemNew.isSimilar(itemNew) || !virtualItemPrev.isSimilar(itemPrev)) {
            return false;
        }
        return (slotNew < 0 || slotNew == ihe.newSlot) &&
                (slotPrev < 0 || slotPrev == ihe.previousSlot);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("item-new", virtualItemNew.asString());
        cfg.set("item-prev", virtualItemPrev.asString());
        cfg.set("slot-new", slotNew + 1);
        cfg.set("slot-prev", slotPrev + 1);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "itemnew:" + (virtualItemNew == VirtualItem.ANY ? "-" : virtualItemNew) +
                " itemprev:" + (virtualItemPrev == VirtualItem.ANY ? "-" : virtualItemPrev) +
                " slotnew:" + (slotNew + 1) +
                " slotprev:" + (slotPrev + 1) +
                ")";
        return sb;
    }

    /**
     * @author MaxDikiy
     * @since 11/11/2017
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
