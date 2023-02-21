package fun.reactions.module.basics.details;

import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.logic.environment.Variable;
import fun.reactions.module.basics.activators.GameModeActivator;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
                CANCEL_EVENT, Variable.property(false),
                "gamemode", Variable.simple(gameMode)
        );
    }

    public GameMode getGameMode() {return this.gameMode;}
}
