package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.parameter.Parameters;
import ink.glowing.utils.bool.TriState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author MaxDikiy
 * @since 05/02/2017
 */
public class FlightActivator extends Activator {
    private final TriState flight;

    private FlightActivator(Logic base, TriState type) {
        super(base);
        this.flight = type;
    }

    public static FlightActivator create(Logic base, Parameters param) {
        return new FlightActivator(base, param.getTriState("flight"));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context fe = (Context) context;
        return flight.isValidFor(fe.flying);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("flight", Parameters.TRI.name(flight));
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
                    CANCEL_EVENT, Variable.value(false),
                    "flight", Variable.value(flying)
            );
        }
    }
}
