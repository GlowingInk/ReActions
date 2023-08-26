package fun.reactions.module.basic.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.Utils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.mob.EntityUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * @author MaxDikiy
 * @since 20/10/2017
 */
@Aliased.Names("CLEAR_RADIUS")
public class ClearRadiusAction implements Action {
    // TODO: Too weird. Optimize, simplify

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        int radius = params.getInteger("radius");
        String type = params.getString("type", "all");
        if (radius == 0) return false;

        List<Location> locs = getMinMaxRadiusLocations(
                params.getOr("center", LocationUtils::parseLocation, () -> env.hasPlayer() ? env.getPlayer().getLocation() : null),
                radius
        );
        if (locs.isEmpty()) return false;
        env.getVariables().set("loc1", LocationUtils.locationToString(locs.get(0)));
        env.getVariables().set("loc2", LocationUtils.locationToString(locs.get(1)));
        Collection<Entity> en = EntityUtils.getEntities(locs.get(0), locs.get(1));
        for (Entity e : en) {
            if (e.getType() == EntityType.PLAYER) continue;
            if (isEntityIsTypeOf(e, type)) {
                e.remove();
            }
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "RADIUS_CLEAR";
    }

    private boolean isEntityIsTypeOf(Entity e, String type) {
        if (e == null) return false;
        if (type.isEmpty()) return true;
        if (type.equalsIgnoreCase("all")) return true;
        if (e instanceof LivingEntity) {
            if (type.equalsIgnoreCase("mob") || type.equalsIgnoreCase("mobs")) return true;
        } else {
            if (type.equalsIgnoreCase("item") || type.equalsIgnoreCase("items")) return true;
        }
        return (Utils.containsWord(e.getType().name().toLowerCase(Locale.ROOT), type.toLowerCase(Locale.ROOT)));
    }

    public static List<Location> getMinMaxRadiusLocations(Location loc, int radius) {
        if (loc == null) return List.of();
        List<Location> locs = new ArrayList<>();
        World world = loc.getWorld();
        locs.add(new Location(world, loc.getBlockX() + radius, loc.getBlockY() + radius, loc.getBlockZ() + radius));
        locs.add(new Location(world, loc.getBlockX() - radius, loc.getBlockY() - radius, loc.getBlockZ() - radius));
        return locs;
    }
}
