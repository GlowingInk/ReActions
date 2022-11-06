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

package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.MobDamageStorage;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import java.util.Locale;

public class MobDamageActivator extends Activator {
    private final String mobName;
    // TODO: EntityType
    private final String mobType;
    private final VirtualItem item;

    private MobDamageActivator(ActivatorLogic base, String type, String name, String item) {
        super(base);
        this.mobType = type;
        this.mobName = name;
        this.item = VirtualItem.fromString(item);
    }

    public static MobDamageActivator create(ActivatorLogic base, Parameters param) {
        String type = param.getOrigin();
        String name = "";
        String itemStr = "";
        if (param.contains("type")) {
            type = param.getString("type");
            name = param.getString("name");
            itemStr = param.getString("item");
        } else if (param.getOrigin().contains("$")) {
            name = type.substring(0, type.indexOf("$"));
            type = type.substring(name.length() + 1);
        }
        return new MobDamageActivator(base, type, name, itemStr);
    }

    public static MobDamageActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String type = cfg.getString("mob-type", "");
        String name = cfg.getString("mob-name", "");
        String itemStr = cfg.getString("item", "");
        return new MobDamageActivator(base, type, name, itemStr);
    }

    @Override
    public boolean checkStorage(Storage event) {
        MobDamageStorage me = (MobDamageStorage) event;
        if (mobType.isEmpty()) return false;
        if (me.getEntity() == null) return false;
        if (!isActivatorMob(me.getEntity())) return false;
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
    public void saveOptions(ConfigurationSection cfg) {
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
}
