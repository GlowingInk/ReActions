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

package fun.reactions.module.worldguard.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.Locatable;
import fun.reactions.module.worldguard.external.RaWorldGuard;
import fun.reactions.module.worldguard.external.WGBridge;
import fun.reactions.util.Utils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"RGENTER", "RG_ENTER"})
public class RegionEnterActivator extends Activator implements Locatable {

    private final String region;

    private RegionEnterActivator(Logic base, String region) {
        super(base);
        this.region = region;
    }

    public String getRegion() {return this.region;}

    public static RegionEnterActivator create(Logic base, Parameters param) {
        String region = param.getString("region", param.originValue());
        return new RegionEnterActivator(base, region);
    }

    public static RegionEnterActivator load(Logic base, ConfigurationSection cfg) {
        String region = cfg.getString("region", "region");
        return new RegionEnterActivator(base, region);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context be = (Context) context;
        return be.getRegion().equalsIgnoreCase(WGBridge.getFullRegionName(this.region));
    }

    public boolean isLocatedAt(Location loc) {
        return RaWorldGuard.isLocationInRegion(loc, this.region);
    }

    @Override
    public boolean isLocatedAt(@NotNull World world, int x, int y, int z) {
        return isLocatedAt(new Location(world, x, y, z));
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("region", region);
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(region);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "region:" + this.region +
                ")";
        return sb;
    }

    public static class Context extends RegionActivator.Context {
        public Context(Player player, String region) {
            super(player, region);
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return RegionEnterActivator.class;
        }
    }
}
