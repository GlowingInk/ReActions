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
import me.fromgate.reactions.module.basics.activators.*;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MobKillStorage extends Storage {

    private final LivingEntity entity;

    public MobKillStorage(Player p, LivingEntity entity) {
        super(p);
        this.entity = entity;
    }

    @Override
    public Class<? extends Activator> getType() {
        return MobKillActivator.class;
    }

    @Override
    protected Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("moblocation", LocationUtils.locationToString(entity.getLocation()));
        tempVars.put("mobkiller", player == null ? "" : player.getName());
        tempVars.put("mobtype", entity.getType().name());
        String mobName = entity instanceof Player ? entity.getName() : entity.getCustomName();
        tempVars.put("mobname", Utils.isStringEmpty(mobName) ? entity.getType().name() : mobName);
        return tempVars;
    }

    public LivingEntity getEntity() {return this.entity;}
}
