package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.Logic;
import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.module.basics.details.PickupItemContext;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-09-04.
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
        PickupItemContext pie = (PickupItemContext) context;
        return item.isSimilar(pie.getItem());
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
}
