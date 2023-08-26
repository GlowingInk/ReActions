package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

@Aliased.Names("WEATHER")
public class WeatherChangeActivator extends Activator {
    private final String world;
    private final WeatherState state;

    private WeatherChangeActivator(Logic base, String world, WeatherState state) {
        super(base);
        this.world = world;
        this.state = state;
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context storage = (Context) context;
        if (world != null && !storage.world.equalsIgnoreCase(world)) return false;
        if (state == WeatherState.ANY) return true;
        return storage.raining == (state == WeatherState.RAINING);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("world", world);
        cfg.set("weather", state.name());
    }

    public static WeatherChangeActivator create(Logic base, Parameters params) {
        String world = params.getString("world");
        WeatherState state = WeatherState.getByName(params.getString("weather", "any"));
        return new WeatherChangeActivator(base, world, state);
    }

    public static WeatherChangeActivator load(Logic base, ConfigurationSection cfg) {
        String world = cfg.getString("world");
        WeatherState state = WeatherState.getByName(cfg.getString("weather", "any"));
        return new WeatherChangeActivator(base, world, state);
    }

    private enum WeatherState {
        RAINING, CLEAR, ANY;

        public static WeatherState getByName(String name) {
            return switch (name.toUpperCase(Locale.ROOT)) {
                case "RAINING", "RAIN" -> RAINING;
                case "CLEAR", "SUN" -> CLEAR;
                default -> ANY;
            };
        }
    }

    public static class Context extends ActivationContext {
        private final String world;
        private final boolean raining;

        public Context(String world, boolean raining) {
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
    }
}
