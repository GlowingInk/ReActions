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

package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.Utils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.mob.EntityUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

public class MobKillActivator extends Activator {
    // TODO: EntityType
    private final String mobType;
    private final String mobName;

    private MobKillActivator(Logic base, @Nullable String type, @Nullable String name) {
        super(base);
        this.mobType = type;
        this.mobName = name == null ? null : ChatColor.translateAlternateColorCodes('&', name);
    }

    public static MobKillActivator create(Logic base, Parameters param) {
        String type = param.originValue();
        String name = null;
        if (param.contains("type")) {
            type = param.getString("type");
            name = param.getString("name", null);
        } else if (param.originValue().contains("$")) {
            name = type.substring(0, type.indexOf('$'));
            type = type.substring(name.length() + 1);
        }
        return new MobKillActivator(base, type, name);
    }

    public static MobKillActivator load(Logic base, ConfigurationSection cfg) {
        String type = cfg.getString("mob-type");
        String name = cfg.getString("mob-name");
        return new MobKillActivator(base, type, name);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context me = (Context) context;
        if (mobType != null && !me.entity.getType().name().equals(mobType)) return false;
        if (mobName != null) {
            if (mobName.isEmpty()) return me.entity.getCustomName() == null;
            return mobName.equals(me.entity.getCustomName());
        }
        return true;
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("mob-type", mobType);
        cfg.set("mob-name", mobName);
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(mobType);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "type:" + (mobType == null ? "-" : mobType.toUpperCase(Locale.ROOT)) +
                " name:" + (mobName == null ? "-" : mobName) +
                ")";
        return sb;
    }

    public static class Context extends ActivationContext {
        private final LivingEntity entity;

        public Context(Player p, LivingEntity entity) {
            super(p);
            this.entity = entity;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return MobKillActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of(
                    CANCEL_EVENT, Variable.property(false),
                    "moblocation", simple(LocationUtils.locationToString(entity.getLocation())),
                    "mobkiller", Variable.simple(player == null ? "" : player.getName()),
                    "mobtype", Variable.simple(entity.getType()),
                    "mobname", simple(EntityUtils.getEntityDisplayName(entity))
            );
        }
    }
}
