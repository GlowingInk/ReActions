package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.Logic;
import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.module.basics.details.DropContext;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

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
        DropContext de = (DropContext) context;
        return item.isSimilar(de.getItem());
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("item", item.toString());
    }
}
