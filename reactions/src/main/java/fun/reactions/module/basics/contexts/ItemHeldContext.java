package fun.reactions.module.basics.contexts;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.ItemHeldActivator;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.item.VirtualItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-11-11.
 */
public class ItemHeldContext extends ActivationContext {

    private final int newSlot;
    private final int previousSlot;
    private final ItemStack newItem;
    private final ItemStack previousItem;

    public ItemHeldContext(Player player, int newSlot, int previousSlot) {
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

    public int getNewSlot() {return this.newSlot;}

    public int getPreviousSlot() {return this.previousSlot;}

    public ItemStack getNewItem() {return this.newItem;}

    public ItemStack getPreviousItem() {return this.previousItem;}
}
