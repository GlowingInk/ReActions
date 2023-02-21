package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.Logic;
import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.module.basics.details.GameModeContext;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.parameter.Parameters;
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
