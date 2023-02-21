package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.environment.Variable;
import me.fromgate.reactions.module.basics.activators.GodActivator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.environment.Variable.property;
import static me.fromgate.reactions.logic.environment.Variable.simple;

/**
 * Created by MaxDikiy on 2017-10-27.
 */
public class GodContext extends ActivationContext {

    private final boolean god;

    public GodContext(Player player, boolean god) {
        super(player);
        this.god = god;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return GodActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, property(false),
                "god", simple(god)
        );
    }

    public boolean isGod() {
        return this.god;
    }
}
