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

package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.events.listeners.GodModeListener;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("PLAYER_STATE")
public class FlagState implements Flag {

    @Override
    public boolean proceed(@NotNull RaContext context, @NotNull String params) {
        Player player = context.getPlayer();
        Posture pt = Posture.getByName(params);
        if (pt == null) return false;
        switch (pt) {
            case SNEAK:
                return player.isSneaking();
            case FLY:
                return player.isFlying();
            case SPRINT:
                return player.isSprinting();
            case VEHICLE:
                return player.isInsideVehicle();
            case SLEEP:
                return player.isSleeping();
            case STAND:
                if (player.isSleeping()) return false;
                if (player.isSneaking()) return false;
                if (player.isSprinting()) return false;
                if (player.isFlying()) return false;
                if (player.isGliding()) return false;
                return !player.isInsideVehicle();
            case OP:
                return player.isOp();
            case VEHICLE_TYPED:
                if (!player.isInsideVehicle()) return false;
                return player.getVehicle().getType().name().equalsIgnoreCase(params.substring(8));
            case SPECTATOR_TARGET:
                return player.getSpectatorTarget() != null;
            case GLIDE:
                return player.isGliding();
            case GOD:
                GodModeListener.setCheckGod(player);
                if (GodModeListener.isGod(player)) return true;
        }
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "STATE";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    private enum Posture {
        SNEAK,
        SPRINT,
        STAND,
        VEHICLE,
        VEHICLE_TYPED,
        SLEEP,
        FLY,
        OP,
        SPECTATOR_TARGET,
        GLIDE,
        GOD;

        public static Posture getByName(String name) {
            if (name.startsWith("VEHICLE_"))
                return VEHICLE_TYPED;
            for (Posture pt : Posture.values())
                if (pt.name().equalsIgnoreCase(name)) return pt;
            return null;
        }
    }

}
