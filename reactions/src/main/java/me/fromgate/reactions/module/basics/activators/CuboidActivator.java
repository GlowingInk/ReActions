package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Locatable;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.util.location.Cuboid;
import me.fromgate.reactions.util.location.VirtualLocation;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class CuboidActivator extends Activator implements Locatable {
    private final CuboidMode mode;
    private final Cuboid cuboid;
    private final Set<UUID> within;

    private CuboidActivator(ActivatorLogic base, Cuboid cuboid, CuboidMode mode) {
        super(base);
        this.cuboid = cuboid;
        this.mode = mode;
        this.within = new HashSet<>();
    }

    public static CuboidActivator create(ActivatorLogic base, Parameters param) {
        CuboidMode mode = CuboidMode.getByName(param.getString("mode", "ENTER"));
        String world = param.getString("world", Bukkit.getWorlds().get(0).getName());
        VirtualLocation loc1 = new VirtualLocation(world, param.getInteger("loc1.x", 0), param.getInteger("loc1.y", 0), param.getInteger("loc1.z", 0));
        VirtualLocation loc2 = new VirtualLocation(world, param.getInteger("loc2.x", 0), param.getInteger("loc2.y", 0), param.getInteger("loc2.z", 0));
        return new CuboidActivator(base, new Cuboid(loc1, loc2), mode);
    }

    public static CuboidActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        CuboidMode mode = CuboidMode.getByName(cfg.getString("mode", "ENTER"));
        String world = cfg.getString("world");
        VirtualLocation loc1 = new VirtualLocation(world, cfg.getInt("loc1.x"), cfg.getInt("loc1.y"), cfg.getInt("loc1.z"));
        VirtualLocation loc2 = new VirtualLocation(world, cfg.getInt("loc2.x"), cfg.getInt("loc2.y"), cfg.getInt("loc2.z"));
        return new CuboidActivator(base, new Cuboid(loc1, loc2), mode);
    }

    @Override
    public boolean checkStorage(Storage event) {
        Player player = event.getPlayer();
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
                return !inCuboid && within.remove(id);
        }
        return false;
    }

    public boolean isLocatedAt(Location loc) {
        return cuboid.isInside(loc, false);
    }

    @Override
    public boolean isLocatedAt(World world, int x, int y, int z) {
        return isLocatedAt(new Location(world, x, y, z));
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
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

    @Override
    public boolean isValid() {
        return true;
    }

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
}
