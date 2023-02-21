package fun.reactions.module.basics.context;

import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.module.basics.activators.CuboidActivator;
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
