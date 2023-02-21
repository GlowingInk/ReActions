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

package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.RespawnActivator;
import me.fromgate.reactions.util.enums.DeathCause;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.mob.EntityUtils;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.property;
import static me.fromgate.reactions.logic.context.Variable.simple;

public class RespawnContext extends ActivationContext {
    public static final String RESPAWN_LOCATION = "respawn_loc";

    private final DeathCause deathCause;
    private final LivingEntity killer;
    private final Location respawnLoc;

    public RespawnContext(Player player, LivingEntity killer, DeathCause cause, Location respawnLoc) {
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
    protected @NotNull Map<String, Variable> prepareVariables() {
        Map<String, Variable> vars = new HashMap<>();
        vars.put(RESPAWN_LOCATION, property(LocationUtils.locationToString(respawnLoc)));
        vars.put("cause", simple(deathCause));
        if (killer != null) {
            vars.put("killer-type", simple(killer.getType()));
            vars.put("killer-name", simple(EntityUtils.getEntityDisplayName(killer)));
        }
        return vars;
    }

    public DeathCause getDeathCause() {
        return this.deathCause;
    }
}
