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
import me.fromgate.reactions.module.basics.storages.LeverStorage;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.parameter.BlockParameters;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

public class LeverActivator extends Activator implements Locatable {
    // TODO: Use enum
    private final String state; //on, off
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    private LeverActivator(ActivatorLogic base, String state, String world, int x, int y, int z) {
        super(base);
        this.state = state;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static LeverActivator create(ActivatorLogic base, Parameters p) {
        if (!(p instanceof BlockParameters param)) return null;
        Block targetBlock = param.getBlock();
        String line = param.toString();
        if (targetBlock != null && targetBlock.getType() == Material.LEVER) {
            String state = "ANY";
            if (line.equalsIgnoreCase("on")) state = "ON";
            if (line.equalsIgnoreCase("off")) state = "OFF";
            String world = targetBlock.getWorld().getName();
            int x = targetBlock.getX();
            int y = targetBlock.getY();
            int z = targetBlock.getZ();
            return new LeverActivator(base, state, world, x, y, z);
        } else return null;
    }

    public static LeverActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String world = cfg.getString("world");
        int x = cfg.getInt("x");
        int y = cfg.getInt("y");
        int z = cfg.getInt("z");
        String state = cfg.getString("lever-state", "ANY");
        if ((!state.equalsIgnoreCase("on")) && (!state.equalsIgnoreCase("off"))) state = "ANY";
        return new LeverActivator(base, state, world, x, y, z);
    }

    @Override
    public boolean checkStorage(Storage event) {
        LeverStorage le = (LeverStorage) event;
        if (le.getLever() == null) return false;
        if (!isLocatedAt(le.getLeverLocation())) return false;
        if (this.state.equalsIgnoreCase("on") && le.isLeverPowered()) return false;
        return !this.state.equalsIgnoreCase("off") || (le.isLeverPowered());
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
        cfg.set("lever-state", state);
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(world);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" + world + ", " + x + ", " + y + ", " + z +
                " state:" + this.state.toUpperCase(Locale.ROOT) + ")";
        return sb;
    }
}
