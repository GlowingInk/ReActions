package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.GameModeActivator;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.property;
import static me.fromgate.reactions.logic.context.Variable.simple;

/**
 * Created by MaxDikiy on 2017-10-27.
 */
public class GameModeContext extends ActivationContext {

    private final GameMode gameMode;

    public GameModeContext(Player player, GameMode gameMode) {
        super(player);
        this.gameMode = gameMode;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return GameModeActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, property(false),
                "gamemode", simple(gameMode)
        );
    }

    public GameMode getGameMode() {return this.gameMode;}
}
