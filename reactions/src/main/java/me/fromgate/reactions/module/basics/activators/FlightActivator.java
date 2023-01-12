package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.details.FlightDetails;
import me.fromgate.reactions.util.enums.TriBoolean;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 5/2/2017.
 */
public class FlightActivator extends Activator {
    private final TriBoolean flight;

    private FlightActivator(ActivatorLogic base, TriBoolean type) {
        super(base);
        this.flight = type;
    }

    public static FlightActivator create(ActivatorLogic base, Parameters param) {
        return new FlightActivator(base, param.getTriBoolean("flight"));
    }

    public static FlightActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        return new FlightActivator(base, TriBoolean.getByName(cfg.getString("flight")));
    }

    @Override
    public boolean checkDetails(@NotNull Details event) {
        FlightDetails fe = (FlightDetails) event;
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
