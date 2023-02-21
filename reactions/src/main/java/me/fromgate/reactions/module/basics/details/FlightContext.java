package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.FlightActivator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.property;
import static me.fromgate.reactions.logic.context.Variable.simple;

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
                CANCEL_EVENT, property(false),
                "flight", simple(flying)
        );
    }

    public boolean isFlying() {return this.flying;}
}
