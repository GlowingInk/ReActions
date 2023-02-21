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

package fun.reactions.module.basics.context;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.MobDamageActivator;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.mob.EntityUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

public class MobDamageContext extends ActivationContext {
    public static final String DAMAGE = "damage";

    private final LivingEntity entity;
    private final DamageCause cause;

    private final double damage;
    private final double finalDamage;

    public MobDamageContext(LivingEntity entity, Player damager, DamageCause cause, double damage, double finalDamage) {
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
                CANCEL_EVENT, Variable.property(false),
                DAMAGE, Variable.property(damage),
                "final_damage", Variable.simple(finalDamage),
                "moblocation", simple(LocationUtils.locationToString(entity.getLocation())),
                "mobdamager", Variable.simple(player == null ? "" : player.getName()),
                "mobtype", Variable.simple(entity.getType()),
                "mobname", simple(EntityUtils.getEntityDisplayName(entity)),
                "cause", Variable.simple(cause)
        );
    }

    public LivingEntity getEntity() {
        return this.entity;
    }
}
