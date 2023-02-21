package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.SneakActivator;
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