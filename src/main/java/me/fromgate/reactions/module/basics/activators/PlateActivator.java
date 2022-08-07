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

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Locatable;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.PlateStorage;
import me.fromgate.reactions.util.BlockUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.parameter.BlockParameters;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class PlateActivator extends Activator implements Locatable {
    // TODO: VirtualLocation
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    private PlateActivator(ActivatorLogic base, String world, int x, int y, int z) {
        super(base);
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static PlateActivator create(ActivatorLogic base, Parameters p) {
        if (!(p instanceof BlockParameters param)) return null;
        Block targetBlock = param.getBlock();
        if (targetBlock != null && BlockUtils.isPlate(targetBlock)) {
            String world = targetBlock.getWorld().getName();
            int x = targetBlock.getX();
            int y = targetBlock.getY();
            int z = targetBlock.getZ();
            return new PlateActivator(base, world, x, y, z);
        }
        return null;
    }

    public static PlateActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String world = cfg.getString("world");
        int x = cfg.getInt("x");
        int y = cfg.getInt("y");
        int z = cfg.getInt("z");
        return new PlateActivator(base, world, x, y, z);
    }

    @Override
    public boolean checkStorage(Storage event) {
        PlateStorage be = (PlateStorage) event;
        return isLocatedAt(be.getLocation());
    }

    public boolean isLocatedAt(Location l) {
        if (l == null) return false;
        if (!world.equals(l.getWorld().getName())) return false;
        if (x != l.getBlockX()) return false;
        if (y != l.getBlockY()) return false;
        return (z == l.getBlockZ());
    }

    @Override
    public boolean isLocatedAt(World world, int x, int y, int z) {
        return this.world.equals(world.getName()) &&
                this.x == x &&
                this.y == y &&
                this.z == z;
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("world", world);
        cfg.set("x", x);
        cfg.set("y", y);
        cfg.set("z", z);
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(world);
    }

    @Override
    public String toString() {
        return super.toString() + " (" + world + ", " + x + ", " + y + ", " + z + ")";
    }
}
