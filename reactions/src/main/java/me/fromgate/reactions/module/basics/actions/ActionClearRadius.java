package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.mob.EntityUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Created by MaxDikiy on 20/10/2017.
 */
@Aliases("CLEAR_RADIUS")
public class ActionClearRadius implements Action {
    // TODO: Too weird. Optimize, simplify

    @Override
    public boolean proceed(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        int radius = params.getInteger("radius", 0);
        String type = params.getString("type", "all");
        if (radius == 0) return false;
        List<Location> locs = LocationUtils.getMinMaxRadiusLocations(context.getPlayer(), radius);
        context.setVariable("loc1", LocationUtils.locationToString(locs.get(0)));
        context.setVariable("loc2", LocationUtils.locationToString(locs.get(1)));
        if (locs.size() != 2) return false;
        Collection<Entity> en = EntityUtils.getEntities(locs.get(0), locs.get(1));
        int count = 0;
        for (Entity e : en) {
            if (e.getType() == EntityType.PLAYER) continue;
            if (isEntityIsTypeOf(e, type)) {
                e.remove();
                count++;
            }
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "RADIUS_CLEAR";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
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
        return (Utils.isWordInList(e.getType().name().toLowerCase(Locale.ROOT), type.toLowerCase(Locale.ROOT)));
    }

}
