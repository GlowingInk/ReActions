package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.Locatable;
import fun.reactions.util.location.position.ImplicitPosition;
import fun.reactions.util.location.space.Cuboid;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class CuboidActivator extends Activator implements Locatable {
    private final CuboidMode mode;
    private final Cuboid cuboid;
    private final Set<UUID> within;

    private CuboidActivator(Logic base, Cuboid cuboid, CuboidMode mode) {
        super(base);
        this.cuboid = cuboid;
        this.mode = mode;
        this.within = new HashSet<>();
    }

    public static CuboidActivator create(Logic base, Parameters param) {
        CuboidMode mode = CuboidMode.getByName(param.getString("mode", "ENTER"));
        String world = param.getString("world", Bukkit.getWorlds().getFirst().getName());
        Parameters loc1P = param.getParameters("loc1");
        Parameters loc2P = param.getParameters("loc2");
        ImplicitPosition loc1 = ImplicitPosition.of(world, loc1P.getInteger("x"), loc1P.getInteger("y"), loc1P.getInteger("z"));
        ImplicitPosition loc2 = ImplicitPosition.of(world, loc2P.getInteger("x"), loc2P.getInteger("y"), loc2P.getInteger("z"));
        return new CuboidActivator(base, new Cuboid(loc1, loc2), mode);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Player player = context.getPlayer();
        UUID id = player.getUniqueId();
        boolean inCuboid = cuboid.isInside(player.getLocation(), true);
        return switch (mode) {
            case CHECK -> inCuboid;
            case ENTER -> {
                if (inCuboid) {
                    if (within.contains(id)) yield false;
                    within.add(id);
                    yield true;
                }
                yield false;
            }
            case LEAVE -> {
                if (!inCuboid && within.contains(id)) {
                    within.remove(id);
                    yield true;
                }
                yield false;
            }
        };
    }

    public boolean isLocatedAt(Location loc) {
        return cuboid.isInside(loc, false);
    }

    @Override
    public boolean isLocatedAt(@NotNull World world, int x, int y, int z) {
        return isLocatedAt(new Location(world, x, y, z));
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("mode", mode.name());
        cfg.set("world", cuboid.world());
        cfg.set("loc1.x", cuboid.xMin());
        cfg.set("loc2.x", cuboid.xMax());
        cfg.set("loc1.y", cuboid.yMin());
        cfg.set("loc2.y", cuboid.yMax());
        cfg.set("loc1.z", cuboid.zMin());
        cfg.set("loc2.z", cuboid.zMax());
    }

    // TODO: toString method


    private enum CuboidMode {
        CHECK, ENTER, LEAVE;

        static CuboidMode getByName(String name) {
            return switch (name.toUpperCase(Locale.ROOT)) {
                case "CHECK" -> CHECK;
                case "LEAVE" -> LEAVE;
                default -> ENTER;
            };
        }
    }

    public static class Context extends ActivationContext {
        public Context(Player player) {
            super(player);
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return CuboidActivator.class;
        }
    }
}
