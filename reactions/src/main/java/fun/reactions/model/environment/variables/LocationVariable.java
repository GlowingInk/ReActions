package fun.reactions.model.environment.variables;

import fun.reactions.model.environment.Variable;
import fun.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class LocationVariable extends ComputedVariable {
    private static final List<String> CHILD_KEYS = List.of("x", "y", "z", "world", "yaw", "pitch");

    private final Location location;
    private final boolean hasDirection;

    public LocationVariable(@NotNull Location location) {
        this(location, location.getYaw() != 0 || location.getPitch() != 0);
    }

    public LocationVariable(@NotNull Block block) {
        this(block.getLocation(), false);
    }

    private LocationVariable(@NotNull Location location, boolean hasDirection) {
        this.location = location;
        this.hasDirection = hasDirection;
    }

    public @NotNull Location location() {
        return location;
    }

    @Override
    public @NotNull String get() {
        return hasDirection
                ? LocationUtils.locationToString(location)
                : location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    @Override
    protected @NotNull List<String> childKeys() {
        return CHILD_KEYS;
    }

    @Override
    protected @NotNull ComputedVariable copy() {
        return new LocationVariable(location, hasDirection);
    }

    @Override
    protected @Nullable Variable computeChild(@NotNull String key) {
        return switch (key) {
            case "x" -> Variable.value(Integer.toString(location.getBlockX()));
            case "y" -> Variable.value(Integer.toString(location.getBlockY()));
            case "z" -> Variable.value(Integer.toString(location.getBlockZ()));
            case "world" -> Variable.value(location.getWorld().getName());
            case "yaw" -> Variable.value(Float.toString(location.getYaw()));
            case "pitch" -> Variable.value(Float.toString(location.getPitch()));
            default -> null;
        };
    }
}
