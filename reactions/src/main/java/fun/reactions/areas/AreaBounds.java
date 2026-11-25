package fun.reactions.areas;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public record AreaBounds(@NotNull Location min, @NotNull Location max) {
}
