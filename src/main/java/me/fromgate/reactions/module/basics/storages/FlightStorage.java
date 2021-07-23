package me.fromgate.reactions.module.basics.storages;

import lombok.Getter;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.FlightActivator;
import me.fromgate.reactions.util.collections.MapBuilder;
import me.fromgate.reactions.util.data.BooleanValue;
import me.fromgate.reactions.util.data.DataValue;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MaxDikiy on 5/2/2017.
 */
@Getter
public class FlightStorage extends Storage {

    private final boolean flying;

    public FlightStorage(Player p, boolean flying) {
        super(p);
        this.flying = flying;
    }

    @Override
    public Class<? extends Activator> getType() {
        return FlightActivator.class;
    }

    @Override
    protected Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("flight", Boolean.toString(flying));
        return tempVars;
    }

    @Override
    protected Map<String, DataValue> prepareChangeables() {
        return MapBuilder.single(CANCEL_EVENT, new BooleanValue(false));
    }
}
