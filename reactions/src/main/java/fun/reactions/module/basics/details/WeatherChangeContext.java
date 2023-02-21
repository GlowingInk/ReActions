package fun.reactions.module.basics.details;

import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.logic.environment.Variable;
import fun.reactions.module.basics.activators.WeatherChangeActivator;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class WeatherChangeContext extends ActivationContext {
    private final String world;
    private final boolean raining;

    public WeatherChangeContext(String world, boolean raining) {
        super(null);
        this.world = world;
        this.raining = raining;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, Variable.property(false),
                "world", Variable.simple(world),
                "weather", Variable.simple(raining ? "RAINING" : "CLEAR")
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
