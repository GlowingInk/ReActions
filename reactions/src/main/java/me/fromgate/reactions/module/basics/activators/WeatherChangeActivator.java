package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.details.WeatherChangeDetails;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Aliased.Names("WEATHER")
public class WeatherChangeActivator extends Activator {
    private final String world;
    private final WeatherState state;

    private WeatherChangeActivator(ActivatorLogic base, String world, WeatherState state) {
        super(base);
        this.world = world;
        this.state = state;
    }

    @Override
    public boolean checkStorage(@NotNull Details strg) {
        WeatherChangeDetails storage = (WeatherChangeDetails) strg;
        if (world != null && !storage.getWorld().equalsIgnoreCase(world)) return false;
        if (state == WeatherState.ANY) return true;
        return storage.isRaining() == (state == WeatherState.RAINING);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("world", world);
        cfg.set("weather", state.name());
    }

    public static WeatherChangeActivator create(ActivatorLogic base, Parameters params) {
        String world = params.getString("world");
        WeatherState state = WeatherState.getByName(params.getString("weather", "any"));
        return new WeatherChangeActivator(base, world, state);
    }

    public static WeatherChangeActivator load(ActivatorLogic base, ConfigurationSection cfg) {
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
