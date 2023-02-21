package fun.reactions.module.basics.context;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
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
