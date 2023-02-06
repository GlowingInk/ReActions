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

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.DeathActivator;
import me.fromgate.reactions.util.enums.DeathCause;
import me.fromgate.reactions.util.mob.EntityUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.simple;

public class DeathDetails extends Details {

    private final LivingEntity killer;
    private final DeathCause cause;

    public DeathDetails(LivingEntity killer, Player player, DeathCause deathCause) {
        super(player);
        this.killer = killer;
        this.cause = killer != null ? deathCause : DeathCause.OTHER;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return DeathActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        Map<String, Variable> vars = new HashMap<>();
        vars.put("cause", simple(cause.name()));
        if (killer != null) {
            vars.put("killer-type", simple(killer.getType()));
            vars.put("killer-name", simple(EntityUtils.getEntityDisplayName(killer)));
        }
        return vars;
    }

    public DeathCause getCause() {
        return this.cause;
    }
}
