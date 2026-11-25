package fun.reactions.areas;

import fun.reactions.util.naming.Named;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AreaProvider extends Named {
    @Nullable AreaBounds getBounds(@NotNull String id);

    boolean contains(@NotNull String id, @NotNull Location location);
}
