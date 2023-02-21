package fun.reactions.module.basics.context;

import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.logic.environment.Variable;
import fun.reactions.module.basics.activators.GodActivator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
                CANCEL_EVENT, Variable.property(false),
                "god", Variable.simple(god)
        );
    }

    public boolean isGod() {
        return this.god;
    }
}
