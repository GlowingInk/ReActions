package me.fromgate.reactions.activators;

import me.fromgate.reactions.actions.Actions;
import me.fromgate.reactions.storage.FlightStorage;
import me.fromgate.reactions.storage.RAStorage;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Variables;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Created by MaxDikiy on 5/2/2017.
 */
public class FlightActivator extends Activator {
	private FlightType flight;

	public FlightActivator(String name, String param) {
		super(name, "activators");
		Param params = new Param(param);
		this.flight = FlightType.getByName(params.getParam("flight", "ANY"));
	}

	public FlightActivator(String name, String group, YamlConfiguration cfg) {
		super(name, group, cfg);
	}

	@Override
	public boolean activate(RAStorage event) {
		FlightStorage fe = (FlightStorage) event;
		if (!checkFlight(fe.isFlying())) return false;
		Variables.setTempVar("flight", fe.isFlying() ? "TRUE" : "FALSE");
		return Actions.executeActivator(fe.getPlayer(), this);
	}

	@Override
	public void save(ConfigurationSection cfg) {
		cfg.set("flight", flight.name());
	}

	@Override
	public void load(ConfigurationSection cfg) {
		flight = FlightType.getByName(cfg.getString("flight", "ANY"));
	}

	@Override
	public ActivatorType getType() {
		return ActivatorType.FLIGHT;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	private boolean checkFlight(Boolean isFlight) {
		switch (flight) {
			case ANY:
				return true;
			case TRUE:
				return isFlight;
			case FALSE:
				return !isFlight;
		}
		return false;
	}

	enum FlightType {
		TRUE,
		FALSE,
		ANY;

		public static FlightType getByName(String flightStr) {
			if (flightStr.equalsIgnoreCase("true")) return FlightType.TRUE;
			if (flightStr.equalsIgnoreCase("any")) return FlightType.ANY;
			return FlightType.FALSE;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name).append(" [").append(getType()).append("]");
		if (!getFlags().isEmpty()) sb.append(" F:").append(getFlags().size());
		if (!getActions().isEmpty()) sb.append(" A:").append(getActions().size());
		if (!getReactions().isEmpty()) sb.append(" R:").append(getReactions().size());
		sb.append(" (");
		sb.append("flight:").append(this.flight.name());
		sb.append(")");
		return sb.toString();
	}

}
