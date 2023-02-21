package fun.reactions.module.basics.details;

import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.logic.environment.Variable;
import fun.reactions.module.basics.activators.PickupItemActivator;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.logic.environment.Variable.simple;

/**
 * Created by MaxDikiy on 2017-09-04.
 */
public class PickupItemContext extends ActivationContext {
    public static final String PICKUP_DELAY = "pickupdelay";
    public static final String ITEM = "item";

    private final Location dropLoc;
    private final ItemStack item;
    private final int pickupDelay;

    public PickupItemContext(Player p, Item item, int pickupDelay) {
        super(p);
        this.item = item.getItemStack();
        this.pickupDelay = pickupDelay;
        this.dropLoc = item.getLocation();
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return PickupItemActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, Variable.property(false),
                PICKUP_DELAY, Variable.property(pickupDelay),
                ITEM, Variable.lazy(() -> VirtualItem.asString(item)),
                "droplocation", simple(LocationUtils.locationToString(dropLoc))
        );
    }

    public ItemStack getItem() {
        return this.item;
    }
}
