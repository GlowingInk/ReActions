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

package me.fromgate.reactions.util.location;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.text.DecimalFormat;

public class TpLocation {

    private static final DecimalFormat FORMAT = new DecimalFormat("####0.##");

    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public TpLocation(Location loc) {
        this.world = loc.getWorld().getName();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }

    public TpLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(this.world);
        return world == null ? new Location(Bukkit.getWorlds().get(0), x, y, z, yaw, pitch) : new Location(world, x, y, z, yaw, pitch);
    }

    public boolean equalsToLoc(Location loc) {
        return (loc.getWorld().getName().equalsIgnoreCase(this.world) &&
                (Math.round(loc.getX()) == Math.round(x)) &&
                (Math.round(loc.getY()) == Math.round(y)) &&
                (Math.round(loc.getZ()) == Math.round(z)));
    }

    @Override
    public String toString() {
        return "[" + this.world + "] " + FORMAT.format(x) + ", " + FORMAT.format(y) + ", " + FORMAT.format(z);
    }


    public String getWorld() {return this.world;}

    public double getX() {return this.x;}

    public double getY() {return this.y;}

    public double getZ() {return this.z;}

    public float getYaw() {return this.yaw;}

    public float getPitch() {return this.pitch;}
}
