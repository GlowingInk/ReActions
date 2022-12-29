package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.WeatherChangeActivator;
import me.fromgate.reactions.util.collections.Maps;
import org.jetbrains.annotations.NotNull;

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
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return Maps.Builder.single(Storage.CANCEL_EVENT, new BooleanValue(false));
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return WeatherChangeActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        return new Maps.Builder<String, String>()
                .put("world", world)
                .put("weather", raining ? "RAINING" : "CLEAR")
                .build();
    }

    public String getWorld() {return this.world;}

    public boolean isRaining() {return this.raining;}
}
