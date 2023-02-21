package fun.reactions.module.basics.context;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.FlightActivator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by MaxDikiy on 5/2/2017.
 */
public class FlightContext extends ActivationContext {

    private final boolean flying;

    public FlightContext(Player p, boolean flying) {
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

    public boolean isFlying() {return this.flying;}
}
