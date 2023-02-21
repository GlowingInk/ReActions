package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.module.basics.context.WeatherChangeContext;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

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
        WeatherChangeContext storage = (WeatherChangeContext) context;
        if (world != null && !storage.getWorld().equalsIgnoreCase(world)) return false;
        if (state == WeatherState.ANY) return true;
        return storage.isRaining() == (state == WeatherState.RAINING);
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
}
