package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.module.basics.contexts.GameModeContext;
import fun.reactions.util.Utils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-10-27.
 */
public class GameModeActivator extends Activator {
    private final GameMode gameMode;

    private GameModeActivator(Logic base, GameMode gameMode) {
        super(base);
        this.gameMode = gameMode;
    }

    public static GameModeActivator create(Logic base, Parameters param) {
        GameMode gameMode = Utils.getEnum(GameMode.class, param.getString("gamemode", "ANY"));
        return new GameModeActivator(base, gameMode);
    }

    public static GameModeActivator load(Logic base, ConfigurationSection cfg) {
        GameMode gameMode = Utils.getEnum(GameMode.class, cfg.getString("gamemode", "ANY"));
        return new GameModeActivator(base, gameMode);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        GameModeContext e = (GameModeContext) context;
        return gameModeCheck(e.getGameMode());
    }

    private boolean gameModeCheck(GameMode gm) {
        if (gameMode == null) return true;
        return gm == gameMode;
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("gamemode", gameMode == null ? "ANY" : gameMode.name());
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "gamemode:" + (gameMode == null ? "ANY" : gameMode.name()) +
                ")";
        return sb;
    }
}
