package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.*;
import me.fromgate.reactions.util.collections.MapBuilder;
import me.fromgate.reactions.util.data.BooleanValue;
import me.fromgate.reactions.util.data.DataValue;

import java.util.Map;

public class WeatherChangeStorage extends Storage {
    private final String world;
    private final boolean raining;

    public WeatherChangeStorage(String world, boolean raining) {
        super(null);
        this.world = world;
        this.raining = raining;
    }

    @Override
    protected Map<String, DataValue> prepareChangeables() {
        return MapBuilder.single(Storage.CANCEL_EVENT, new BooleanValue(false));
    }

    @Override
    public Class<? extends Activator> getType() {
        return WeatherChangeActivator.class;
    }

    @Override
    protected Map<String, String> prepareVariables() {
        return new MapBuilder<String, String>()
                .put("world", world)
                .put("weather", raining ? "RAINING" : "CLEAR")
                .build();
    }

    public String getWorld() {return this.world;}

    public boolean isRaining() {return this.raining;}
}
