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
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.mob.EntityUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

public class MobDamageActivator extends Activator {
    private final String mobName;
    // TODO: EntityType
    private final String mobType;
    private final VirtualItem item;

    private MobDamageActivator(Logic base, String type, String name, String item) {
        super(base);
        this.mobType = type;
        this.mobName = name;
        this.item = VirtualItem.fromString(item);
    }

    public static MobDamageActivator create(Logic base, Parameters param) {
        String type = param.originValue();
        String name = "";
        String itemStr = "";
        if (param.contains("type")) {
            type = param.getString("type");
            name = param.getString("name");
            itemStr = param.getString("item");
        } else if (param.originValue().contains("$")) {
            name = type.substring(0, type.indexOf('$'));
            type = type.substring(name.length() + 1);
        }
        return new MobDamageActivator(base, type, name, itemStr);
    }

    public static MobDamageActivator load(Logic base, ConfigurationSection cfg) {
        String type = cfg.getString("mob-type", "");
        String name = cfg.getString("mob-name", "");
        String itemStr = cfg.getString("item", "");
        return new MobDamageActivator(base, type, name, itemStr);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        MobDamageContext me = (MobDamageContext) context;
        if (mobType.isEmpty()) return false;
        if (me.entity == null) return false;
        if (!isActivatorMob(me.entity)) return false;
        return item.isSimilar(me.getPlayer().getInventory().getItemInMainHand());
    }

    private boolean isActivatorMob(LivingEntity mob) {
        if (!mobName.isEmpty()) {
            if (!ChatColor.translateAlternateColorCodes('&', mobName.replace("_", " ")).equals(getMobName(mob)))
                return false;
        } else if (!getMobName(mob).isEmpty()) return false;
        return mob.getType().name().equalsIgnoreCase(this.mobType);
    }

    private String getMobName(LivingEntity mob) {
        if (mob.getCustomName() == null) return "";
        return mob.getCustomName();
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("mob-type", mobType);
        cfg.set("mob-name", mobName);
        cfg.set("item", item.toString());
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(mobType);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "type:" + (mobType.isEmpty() ? "-" : mobType.toUpperCase(Locale.ROOT)) +
                " name:" + (mobName.isEmpty() ? "-" : mobName) +
                ")";
        return sb;
    }

    public static class MobDamageContext extends ActivationContext {
        public static final String DAMAGE = "damage";

        private final LivingEntity entity;
        private final EntityDamageEvent.DamageCause cause;

        private final double damage;
        private final double finalDamage;

        public MobDamageContext(LivingEntity entity, Player damager, EntityDamageEvent.DamageCause cause, double damage, double finalDamage) {
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
    }
}
