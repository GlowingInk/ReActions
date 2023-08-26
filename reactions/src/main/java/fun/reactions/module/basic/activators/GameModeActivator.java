package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.Utils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author MaxDikiy
 * @since 27/10/2017
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
        Context e = (Context) context;
        return gameModeCheck(e.gameMode);
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

    /**
     * @author MaxDikiy
     * @since 27/10/2017
     */
    public static class Context extends ActivationContext {
        private final GameMode gameMode;

        public Context(Player player, GameMode gameMode) {
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
    }
}
