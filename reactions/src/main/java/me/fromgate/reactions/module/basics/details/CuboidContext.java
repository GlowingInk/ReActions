package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.module.basics.activators.CuboidActivator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CuboidContext extends ActivationContext {
    public CuboidContext(Player player) {
        super(player);
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return CuboidActivator.class;
    }
}
