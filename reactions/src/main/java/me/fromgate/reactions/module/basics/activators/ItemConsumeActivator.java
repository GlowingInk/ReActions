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
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.details.ItemConsumeDetails;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"CONSUME", "EAT"})
public class ItemConsumeActivator extends Activator {
    private final VirtualItem item;
    // TODO: Hand option

    private ItemConsumeActivator(ActivatorLogic base, String item) {
        super(base);
        this.item = VirtualItem.fromString(item);
    }

    public static ItemConsumeActivator create(ActivatorLogic base, Parameters param) {
        String item = param.getString("item", param.origin());
        return new ItemConsumeActivator(base, item);
    }

    public static ItemConsumeActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String item = cfg.getString("item", "");
        return new ItemConsumeActivator(base, item);
    }

    public boolean checkDetails(@NotNull Details event) {
        ItemConsumeDetails ie = (ItemConsumeDetails) event;
        return item.isSimilar(ie.getItem());
    }

    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("item", item.toString());
    }

    public String toString() {
        String sb = super.toString() + " (" +
                this.item +
                ")";
        return sb;
    }
}
