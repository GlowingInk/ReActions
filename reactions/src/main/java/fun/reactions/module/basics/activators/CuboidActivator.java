package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.Locatable;
import fun.reactions.util.location.Cuboid;
import fun.reactions.util.location.ImplicitPosition;
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
        String world = param.getString("world", Bukkit.getWorlds().get(0).getName());
        ImplicitPosition loc1 = ImplicitPosition.of(world, param.getInteger("loc1.x"), param.getInteger("loc1.y"), param.getInteger("loc1.z"));
        ImplicitPosition loc2 = ImplicitPosition.of(world, param.getInteger("loc2.x"), param.getInteger("loc2.y"), param.getInteger("loc2.z"));
        return new CuboidActivator(base, new Cuboid(loc1, loc2), mode);
    }

    public static CuboidActivator load(Logic base, ConfigurationSection cfg) {
        CuboidMode mode = CuboidMode.getByName(cfg.getString("mode", "ENTER"));
        String world = cfg.getString("world");
        ImplicitPosition loc1 = ImplicitPosition.of(world, cfg.getInt("loc1.x"), cfg.getInt("loc1.y"), cfg.getInt("loc1.z"));
        ImplicitPosition loc2 = ImplicitPosition.of(world, cfg.getInt("loc2.x"), cfg.getInt("loc2.y"), cfg.getInt("loc2.z"));
        return new CuboidActivator(base, new Cuboid(loc1, loc2), mode);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Player player = context.getPlayer();
        UUID id = player.getUniqueId();
        boolean inCuboid = cuboid.isInside(player.getLocation(), true);
        switch (mode) {
            case CHECK:
                return inCuboid;
            case ENTER:
                if (inCuboid) {
                    if (within.contains(id)) return false;
                    within.add(id);
                    return true;
                }
                return false;
            case LEAVE:
                if (!inCuboid && within.contains(id)) {
                    within.remove(id);
                    return true;
                }
                return false;
        }
        return false;
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
        cfg.set("world", cuboid.getWorld());
        cfg.set("loc1.x", cuboid.getXMin());
        cfg.set("loc2.x", cuboid.getXMax());
        cfg.set("loc1.y", cuboid.getYMin());
        cfg.set("loc2.y", cuboid.getYMax());
        cfg.set("loc1.z", cuboid.getZMin());
        cfg.set("loc2.z", cuboid.getZMax());
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
