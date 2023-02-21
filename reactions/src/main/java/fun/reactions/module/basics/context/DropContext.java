package fun.reactions.module.basics.context;

import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.logic.environment.Variable;
import fun.reactions.module.basics.activators.DropActivator;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.location.LocationUtils;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.logic.environment.Variable.simple;

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
                CANCEL_EVENT, Variable.property(false),
                PICKUP_DELAY, Variable.property(pickupDelay),
                ITEM, Variable.lazy(() -> VirtualItem.asString(item)),
                "droplocation", simple(LocationUtils.locationToString(player.getLocation()))
        );
    }

    public ItemStack getItem() {
        return this.item;
    }
}
