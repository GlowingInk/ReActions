package fun.reactions.module.basics.activators;

import fun.reactions.logic.Logic;
import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.module.basics.details.SneakContext;
import fun.reactions.util.enums.TriBoolean;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-05-16.
 */
public class SneakActivator extends Activator {
    private final TriBoolean sneak;

    private SneakActivator(Logic base, TriBoolean sneak) {
        super(base);
        this.sneak = sneak;
    }

    public static SneakActivator create(Logic base, Parameters param) {
        return new SneakActivator(base, param.getTriBoolean("sneak"));
    }

    public static SneakActivator load(Logic base, ConfigurationSection cfg) {
        return new SneakActivator(base, TriBoolean.of(cfg.getString("sneak")));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        SneakContext se = (SneakContext) context;
        return sneak.isValidFor(se.isSneaking());
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("sneak", sneak.name());
    }

    @Override
    public String toString() {
        return super.toString() + " (" +
                "sneak:" + this.sneak.name() +
                ")";
    }
}
