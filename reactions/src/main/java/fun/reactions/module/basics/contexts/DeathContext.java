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

package fun.reactions.module.basics.contexts;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.DeathActivator;
import fun.reactions.util.enums.DeathCause;
import fun.reactions.util.mob.EntityUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

public class DeathContext extends ActivationContext {

    private final LivingEntity killer;
    private final DeathCause cause;

    public DeathContext(LivingEntity killer, Player player, DeathCause deathCause) {
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
            vars.put("killer-type", Variable.simple(killer.getType()));
            vars.put("killer-name", simple(EntityUtils.getEntityDisplayName(killer)));
        }
        return vars;
    }

    public DeathCause getCause() {
        return this.cause;
    }
}
