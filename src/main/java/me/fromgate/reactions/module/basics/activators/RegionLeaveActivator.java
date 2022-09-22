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

package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.externals.worldguard.RaWorldGuard;
import me.fromgate.reactions.externals.worldguard.WGBridge;
import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Locatable;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.RegionLeaveStorage;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

@Aliases({"RGLEAVE", "RG_LREAVE"})
public class RegionLeaveActivator extends Activator implements Locatable {

    private final String region;

    private RegionLeaveActivator(ActivatorLogic base, String region) {
        super(base);
        this.region = region;
    }

    public String getRegion() {return this.region;}

    public static RegionLeaveActivator create(ActivatorLogic base, Parameters param) {
        String region = param.getString("region", param.toString());
        RaWorldGuard.updateRegionCache();
        return new RegionLeaveActivator(base, region);
    }

    public static RegionLeaveActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String region = cfg.getString("region", "region");
        return new RegionLeaveActivator(base, region);
    }

    @Override
    public boolean checkStorage(Storage event) {
        RegionLeaveStorage be = (RegionLeaveStorage) event;
        return be.getRegion().equalsIgnoreCase(WGBridge.getFullRegionName(this.region));
    }

    public boolean isLocatedAt(Location loc) {
        if (!RaWorldGuard.isConnected()) return false;
        return RaWorldGuard.isLocationInRegion(loc, this.region);
    }

    @Override
    public boolean isLocatedAt(World world, int x, int y, int z) {
        return isLocatedAt(new Location(world, x, y, z));
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
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

}
