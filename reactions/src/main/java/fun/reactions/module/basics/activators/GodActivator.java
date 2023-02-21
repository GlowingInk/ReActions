package fun.reactions.module.basics.activators;

import fun.reactions.logic.Logic;
import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.module.basics.context.GodContext;
import fun.reactions.util.enums.TriBoolean;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-10-28.
 */
public class GodActivator extends Activator {
    private final TriBoolean god;

    private GodActivator(Logic base, TriBoolean type) {
        super(base);
        this.god = type;
    }

    public static GodActivator create(Logic base, Parameters param) {
        return new GodActivator(base, param.getTriBoolean("god"));
    }

    public static GodActivator load(Logic base, ConfigurationSection cfg) {
        return new GodActivator(base, TriBoolean.of(cfg.getString("god")));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        GodContext e = (GodContext) context;
        return god.isValidFor(e.isGod());
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("god", god.name());
    }

    @Override
    public String toString() {
        return super.toString() + " (" +
                "god:" + this.god.name() +
                ")";
    }
}
