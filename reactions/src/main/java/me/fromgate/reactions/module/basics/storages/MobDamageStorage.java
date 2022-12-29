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

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.data.DoubleValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.MobDamageActivator;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.collections.Maps;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MobDamageStorage extends Storage {
    public static final String DAMAGE = "damage";

    private final LivingEntity entity;
    private final DamageCause cause;
    private final double damage;

    public MobDamageStorage(LivingEntity entity, Player damager, double damage, DamageCause cause) {
        super(damager);
        this.entity = entity;
        this.damage = damage;
        this.cause = cause;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return MobDamageActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("moblocation", LocationUtils.locationToString(entity.getLocation()));
        tempVars.put("mobdamager", player == null ? "" : player.getName());
        tempVars.put("mobtype", entity.getType().name());
        String mobName = entity instanceof Player ? entity.getName() : entity.getCustomName();
        tempVars.put("mobname", Utils.isStringEmpty(mobName) ? entity.getType().name() : mobName);
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return new Maps.Builder<String, DataValue>()
                .put(CANCEL_EVENT, new BooleanValue(false))
                .put(DAMAGE, new DoubleValue(damage))
                .build();
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public LivingEntity getEntity() {return this.entity;}

    public DamageCause getCause() {return this.cause;}

    public double getDamage() {return this.damage;}
}
