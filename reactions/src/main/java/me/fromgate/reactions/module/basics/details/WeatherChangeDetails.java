package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.WeatherChangeActivator;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.property;
import static me.fromgate.reactions.logic.context.Variable.simple;

public class WeatherChangeDetails extends Details {
    private final String world;
    private final boolean raining;

    public WeatherChangeDetails(String world, boolean raining) {
        super(null);
        this.world = world;
        this.raining = raining;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, property(false),
                "world", simple(world),
                "weather", simple(raining ? "RAINING" : "CLEAR")
        );
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return WeatherChangeActivator.class;
    }

    public String getWorld() {
        return this.world;
    }

    public boolean isRaining() {
        return this.raining;
    }
}
