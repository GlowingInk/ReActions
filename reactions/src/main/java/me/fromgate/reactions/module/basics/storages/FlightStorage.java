package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.FlightActivator;
import me.fromgate.reactions.util.collections.MapBuilder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 5/2/2017.
 */
public class FlightStorage extends Storage {

    private final boolean flying;

    public FlightStorage(Player p, boolean flying) {
        super(p);
        this.flying = flying;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return FlightActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("flight", Boolean.toString(flying));
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return MapBuilder.single(CANCEL_EVENT, new BooleanValue(false));
    }

    public boolean isFlying() {return this.flying;}
}
