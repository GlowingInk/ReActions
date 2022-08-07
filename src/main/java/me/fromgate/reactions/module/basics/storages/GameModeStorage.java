package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.GameModeActivator;
import me.fromgate.reactions.util.collections.MapBuilder;
import me.fromgate.reactions.util.data.BooleanValue;
import me.fromgate.reactions.util.data.DataValue;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-10-27.
 */
public class GameModeStorage extends Storage {

    private final GameMode gameMode;

    public GameModeStorage(Player player, GameMode gameMode) {
        super(player);
        this.gameMode = gameMode;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return GameModeActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("gamemode", gameMode.name());
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return MapBuilder.single(CANCEL_EVENT, new BooleanValue(false));
    }

    public GameMode getGameMode() {return this.gameMode;}
}
