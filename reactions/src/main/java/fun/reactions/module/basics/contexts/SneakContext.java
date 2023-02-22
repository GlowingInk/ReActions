package fun.reactions.module.basics.contexts;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.SneakActivator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by MaxDikiy on 2017-05-16.
 */
public class SneakContext extends ActivationContext {

    private final boolean sneaking;

    public SneakContext(Player player, boolean sneaking) {
        super(player);
        this.sneaking = sneaking;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return SneakActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of("sneak", Variable.simple(sneaking));
    }

    public boolean isSneaking() {
        return this.sneaking;
    }
}