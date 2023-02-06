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
import me.fromgate.reactions.module.basics.activators.MobDamageActivator;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.mob.EntityUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.property;
import static me.fromgate.reactions.logic.context.Variable.simple;

public class MobDamageDetails extends Details {
    public static final String DAMAGE = "damage";

    private final LivingEntity entity;
    private final DamageCause cause;

    private final double damage;
    private final double finalDamage;

    public MobDamageDetails(LivingEntity entity, Player damager, DamageCause cause, double damage, double finalDamage) {
        super(damager);
        this.entity = entity;
        this.cause = cause;
        this.damage = damage;
        this.finalDamage = finalDamage;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return MobDamageActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, property(false),
                DAMAGE, property(damage),
                "final_damage", simple(finalDamage),
                "moblocation", simple(LocationUtils.locationToString(entity.getLocation())),
                "mobdamager", simple(player == null ? "" : player.getName()),
                "mobtype", simple(entity.getType()),
                "mobname", simple(EntityUtils.getEntityDisplayName(entity)),
                "cause", simple(cause)
        );
    }

    public LivingEntity getEntity() {
        return this.entity;
    }
}
