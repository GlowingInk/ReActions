package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.FlightStorage;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by MaxDikiy on 5/2/2017.
 */
public class FlightActivator extends Activator {
    private final FlightType flight;

    private FlightActivator(ActivatorLogic base, FlightType type) {
        super(base);
        this.flight = type;
    }

    public static FlightActivator create(ActivatorLogic base, Parameters param) {
        FlightType type = FlightType.getByName(param.getString("flight", "ANY"));
        return new FlightActivator(base, type);
    }

    public static FlightActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        FlightType type = FlightType.getByName(cfg.getString("flight", "ANY"));
        return new FlightActivator(base, type);
    }

    @Override
    public boolean checkStorage(Storage event) {
        FlightStorage fe = (FlightStorage) event;
        return checkFlight(fe.isFlying());
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("flight", flight.name());
    }

    private boolean checkFlight(boolean isFlight) {
        return switch (flight) {
            case ANY -> true;
            case TRUE -> isFlight;
            case FALSE -> !isFlight;
        };
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "flight:" + this.flight.name() +
                ")";
        return sb;
    }

    private enum FlightType {
        TRUE,
        FALSE,
        ANY;

        public static FlightType getByName(String flightStr) {
            if (flightStr.equalsIgnoreCase("true")) return FlightType.TRUE;
            if (flightStr.equalsIgnoreCase("any")) return FlightType.ANY;
            return FlightType.FALSE;
        }
    }

}
