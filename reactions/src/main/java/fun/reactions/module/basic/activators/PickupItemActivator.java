package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

/**
 * @author MaxDikiy
 * @since 04/09/2017
 */
public class PickupItemActivator extends Activator {
    private final VirtualItem item;

    private PickupItemActivator(Logic base, String item) {
        super(base);
        this.item = VirtualItem.fromString(item);
    }

    public static PickupItemActivator create(Logic base, Parameters param) {
        String item = param.getString("item", param.origin());
        return new PickupItemActivator(base, item);
    }

    public static PickupItemActivator load(Logic base, ConfigurationSection cfg) {
        String item = cfg.getString("item", "");
        return new PickupItemActivator(base, item);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context pie = (Context) context;
        return item.isSimilar(pie.item);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("item", item.toString());
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "item:" + item +
                ")";
        return sb;
    }

    /**
     * @author MaxDikiy
     * @since 04/09/2017
     */
    public static class Context extends ActivationContext {
        public static final String PICKUP_DELAY = "pickupdelay";
        public static final String ITEM = "item";

        private final Location dropLoc;
        private final ItemStack item;
        private final int pickupDelay;

        public Context(Player p, Item item, int pickupDelay) {
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
    }
}
