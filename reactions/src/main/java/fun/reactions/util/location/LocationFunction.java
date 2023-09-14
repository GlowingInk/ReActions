package fun.reactions.util.location;

import org.bukkit.World;

@FunctionalInterface
public interface LocationFunction<R> {
    R apply(World world, int x, int y, int z);
}
