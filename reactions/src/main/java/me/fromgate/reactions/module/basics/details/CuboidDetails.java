package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.activators.CuboidActivator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CuboidDetails extends Details {
    public CuboidDetails(Player player) {
        super(player);
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return CuboidActivator.class;
    }
}
