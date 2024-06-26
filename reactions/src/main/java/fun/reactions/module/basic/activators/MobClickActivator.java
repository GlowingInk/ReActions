/*
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *
 *  This file is part of ReActions.
 *
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReActions is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 *
 */

package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.Locatable;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.Utils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.mob.EntityUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

public class MobClickActivator extends Activator implements Locatable {
    // TODO: EntityType
    private final String mobType;
    private final String mobName;
    private final String mobLocation;

    private MobClickActivator(Logic base, String type, String name, String location) {
        super(base);
        this.mobType = type;
        this.mobName = name;
        this.mobLocation = location;
    }

    public static MobClickActivator create(Logic base, Parameters param) {
        String type = param.originValue();
        String name = "";
        String location = "";
        if (param.contains("type")) {
            type = param.getString("type");
            name = param.getString("name");
            location = param.getString("loc");
        } else if (param.originValue().contains("$")) {
            name = type.substring(0, type.indexOf('$'));
            type = type.substring(name.length() + 1);
        }
        return new MobClickActivator(base, type, name, location);
    }

    public static MobClickActivator load(Logic base, ConfigurationSection cfg) {
        String type = cfg.getString("mob-type", "");
        String name = cfg.getString("mob-name", "");
        String location = cfg.getString("location", "");
        return new MobClickActivator(base, type, name, location);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context me = (Context) context;
        if (mobType.isEmpty()) return false;
        if (me.entity == null) return false;
        return isActivatorMob(me.entity);
    }

    private boolean checkLocations(LivingEntity mob) {
        if (this.mobLocation.isEmpty()) return true;
        return this.isLocatedAt(mob.getLocation());
    }

    private boolean isActivatorMob(LivingEntity mob) {
        if (!mob.getType().name().equalsIgnoreCase(this.mobType)) return false;
        if (!mobName.isEmpty()) {
            if (!ChatColor.translateAlternateColorCodes('&', mobName).equals(EntityUtils.getMobName(mob)))
                return false;
        } else if (!EntityUtils.getMobName(mob).isEmpty()) return false;
        return checkLocations(mob);
    }

    public boolean isLocatedAt(Location l) {
        if (this.mobLocation.isEmpty()) return false;
        Location loc = LocationUtils.parseCoordinates(this.mobLocation);
        if (loc == null) return false;
        return l.getWorld().equals(loc.getWorld()) &&
                l.getBlockX() == loc.getBlockX() &&
                l.getBlockY() == loc.getBlockY() &&
                l.getBlockZ() == loc.getBlockZ();
    }

    @Override
    public boolean isLocatedAt(@NotNull World world, int x, int y, int z) {
        return isLocatedAt(new Location(world, x, y, z));
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("mob-type", mobType);
        cfg.set("mob-name", mobName.isEmpty() ? null : mobName);
        cfg.set("location", mobLocation.isEmpty() ? null : mobLocation);
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(mobType);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "type:" + (mobType.isEmpty() ? "-" : mobType.toUpperCase(Locale.ROOT)) +
                " name:" + (mobName.isEmpty() ? "-" : mobName) +
                " loc:" + (mobLocation.isEmpty() ? "-" : mobLocation) +
                ")";
        return sb;
    }

    public static class Context extends ActivationContext {
        private final LivingEntity entity;

        public Context(Player p, LivingEntity entity) {
            super(p);
            this.entity = entity;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return MobClickActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of(
                    CANCEL_EVENT, Variable.property(false),
                    "moblocation", simple(LocationUtils.locationToString(entity.getLocation())),
                    "mobtype", Variable.simple(entity.getType()),
                    "mobname", simple(EntityUtils.getEntityDisplayName(entity))
            );
        }
    }
}
