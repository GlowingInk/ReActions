package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.GodActivator;
import me.fromgate.reactions.util.collections.Maps;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by MaxDikiy on 2017-10-27.
 */
public class GodStorage extends Storage {

    private final boolean god;

    public GodStorage(Player player, boolean god) {
        super(player);
        this.god = god;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return GodActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        return Maps.Builder.single("god", Boolean.toString(god));
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return Maps.Builder.single(CANCEL_EVENT, new BooleanValue(false));
    }

    public boolean isGod() {return this.god;}
}
