package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.environment.Variable;
import me.fromgate.reactions.module.basics.activators.ItemHeldActivator;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static me.fromgate.reactions.logic.environment.Variable.*;

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
        vars.put(CANCEL_EVENT, property(false));
        vars.put("slotnew", simple(newSlot + 1));
        vars.put("slotprev", simple(previousSlot + 1));
        if (newItem != null) {
            vars.put("itemnew", lazy(() -> VirtualItem.asString(newItem)));
            vars.put("itemnew-str", lazy(() -> ItemUtils.toDisplayString(newItem)));
        }
        if (previousItem != null) {
            vars.put("itemprev", lazy(() -> VirtualItem.asString(previousItem)));
            vars.put("itemprev-str", lazy(() -> ItemUtils.toDisplayString(previousItem)));
        }
        return vars;
    }

    public int getNewSlot() {return this.newSlot;}

    public int getPreviousSlot() {return this.previousSlot;}

    public ItemStack getNewItem() {return this.newItem;}

    public ItemStack getPreviousItem() {return this.previousItem;}
}
