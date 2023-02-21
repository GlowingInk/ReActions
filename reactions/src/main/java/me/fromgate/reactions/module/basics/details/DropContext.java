package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.DropActivator;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.*;

/**
 * Created by MaxDikiy on 2017-05-01.
 */
public class DropContext extends ActivationContext {
    public static final String PICKUP_DELAY = "pickupdelay";
    public static final String ITEM = "item";

    private final ItemStack item;
    private final int pickupDelay;

    public DropContext(Player p, Item item, int pickupDelay) {
        super(p);
        this.item = item.getItemStack();
        this.pickupDelay = pickupDelay;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return DropActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, property(false),
                PICKUP_DELAY, property(pickupDelay),
                ITEM, lazy(() -> VirtualItem.asString(item)),
                "droplocation", simple(LocationUtils.locationToString(player.getLocation()))
        );
    }

    public ItemStack getItem() {
        return this.item;
    }
}
