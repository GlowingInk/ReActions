package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.module.basics.contexts.FlightContext;
import fun.reactions.util.enums.TriBoolean;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 5/2/2017.
 */
public class FlightActivator extends Activator {
    private final TriBoolean flight;

    private FlightActivator(Logic base, TriBoolean type) {
        super(base);
        this.flight = type;
    }

    public static FlightActivator create(Logic base, Parameters param) {
        return new FlightActivator(base, param.getTriBoolean("flight"));
    }

    public static FlightActivator load(Logic base, ConfigurationSection cfg) {
        return new FlightActivator(base, TriBoolean.of(cfg.getString("flight")));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        FlightContext fe = (FlightContext) context;
        return flight.isValidFor(fe.isFlying());
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("flight", flight.name());
    }

    @Override
    public String toString() {
        return super.toString() + " (" +
                "flight:" + this.flight.name() +
                ")";
    }
}
