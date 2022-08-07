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

package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.RespawnActivator;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.collections.MapBuilder;
import me.fromgate.reactions.util.data.DataValue;
import me.fromgate.reactions.util.data.LocationValue;
import me.fromgate.reactions.util.enums.DeathCause;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RespawnStorage extends Storage {
    public static final String RESPAWN_LOCATION = "respawn_loc";

    private final DeathCause deathCause;
    private final LivingEntity killer;
    private final Location respawnLoc;

    public RespawnStorage(Player player, LivingEntity killer, DeathCause cause, Location respawnLoc) {
        super(player);
        this.killer = killer;
        this.deathCause = cause;
        this.respawnLoc = respawnLoc;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return RespawnActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("cause", deathCause.name());
        if (killer != null) {
            tempVars.put("killer-type", killer.getType().name());
            if (killer.getType() == EntityType.PLAYER) {
                tempVars.put("targetplayer", killer.getName());
                tempVars.put("killer-name", killer.getName());
            } else {
                String mobName = killer.getCustomName();
                tempVars.put("killer-name", Utils.isStringEmpty(mobName) ? killer.getType().name() : mobName);
            }
        }
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return MapBuilder.single(RESPAWN_LOCATION, new LocationValue(respawnLoc));
    }

    public DeathCause getDeathCause() {return this.deathCause;}

    public LivingEntity getKiller() {return this.killer;}

    public Location getRespawnLoc() {return this.respawnLoc;}
}
