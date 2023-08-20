package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.enums.TriBoolean;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author MaxDikiy
 * @since 05/02/2017
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
        return new FlightActivator(base, TriBoolean.byString(cfg.getString("flight")));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context fe = (Context) context;
        return flight.isValidFor(fe.flying);
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

    /**
     * @author MaxDikiy
     * @since 05/02/2017
     */
    public static class Context extends ActivationContext {
        private final boolean flying;

        public Context(Player p, boolean flying) {
            super(p);
            this.flying = flying;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return FlightActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of(
                    CANCEL_EVENT, Variable.property(false),
                    "flight", Variable.simple(flying)
            );
        }
    }
}
