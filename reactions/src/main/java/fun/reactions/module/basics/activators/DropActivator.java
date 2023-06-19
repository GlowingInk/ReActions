package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

/**
 * Created by MaxDikiy on 2017-05-01.
 */
public class DropActivator extends Activator {

    private final VirtualItem item;

    private DropActivator(Logic base, String itemStr) {
        super(base);
        this.item = VirtualItem.fromString(itemStr);
    }

    public static DropActivator create(Logic base, Parameters param) {
        String itemStr = param.getString("item", param.origin());
        return new DropActivator(base, itemStr);
    }

    public static DropActivator load(Logic base, ConfigurationSection cfg) {
        String itemStr = cfg.getString("item", "");
        return new DropActivator(base, itemStr);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context de = (Context) context;
        return item.isSimilar(de.item);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("item", item.toString());
    }

    /**
     * Created by MaxDikiy on 2017-05-01.
     */
    public static class Context extends ActivationContext {
        public static final String PICKUP_DELAY = "pickupdelay";
        public static final String ITEM = "item";

        private final ItemStack item;
        private final int pickupDelay;

        public Context(Player p, Item item, int pickupDelay) {
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
    }
}
