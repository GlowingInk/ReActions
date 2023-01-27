package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.GodActivator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.plain;
import static me.fromgate.reactions.logic.context.Variable.property;

/**
 * Created by MaxDikiy on 2017-10-27.
 */
public class GodDetails extends Details {

    private final boolean god;

    public GodDetails(Player player, boolean god) {
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
                "god", plain(god)
        );
    }

    public boolean isGod() {
        return this.god;
    }
}
