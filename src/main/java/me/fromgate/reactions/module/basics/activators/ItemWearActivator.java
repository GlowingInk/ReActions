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
import me.fromgate.reactions.module.basics.storages.*;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;

public class ItemWearActivator extends Activator /*implements Manageable*/ {
    // TODO: Store VirtualItem
    private final String item;
    // private final WearSlot slot;

    private ItemWearActivator(ActivatorLogic base, String item/*, WearSlot slot*/) {
        super(base);
        this.item = item;
        // this.slot = slot;
    }

    public static ItemWearActivator create(ActivatorLogic base, Parameters param) {
        String item = param.getString("item", "origin-string");
        // WearSlot slot = WearSlot.getByName(param.getParam("slot", "any"));
        return new ItemWearActivator(base, item/*, slot*/);
    }

    public static ItemWearActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String item = cfg.getString("item");
        // WearSlot slot = WearSlot.getByName(cfg.getString("wear-slot", "any"));
        return new ItemWearActivator(base, item/*, slot*/);
    }

    @Override
    public boolean checkStorage(Storage event) {
        if (item.isEmpty() || (VirtualItem.fromString(item) == null)) {
            Msg.logOnce(logic.getName() + "activatorwearempty", "Failed to parse item of activator " + logic.getName());
            return false;
        }
        ItemWearStorage iw = (ItemWearStorage) event;
        return iw.isItemWeared(this.item);
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("item", item);
        // cfg.set("wear-slot", this.slot.name());
    }

    public String getItemStr() {
        return this.item;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" (").append(this.item).append(")");
        return sb.toString();
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(item);
    }

	/*
	public enum WearSlot {
		HEAD, CHEST, LEGS, FEET, ANY;

		public static WearSlot getByName(String name) {
			if(Util.isStringEmpty(name)) return ANY;
			switch(name.toLowerCase(Locale.ENGLISH)) {
				case "head": return HEAD;
				case "chest": return CHEST;
				case "legs": return LEGS;
				case "feet": return FEET;
				default: return ANY;
			}
		}
	}
	*/
}

