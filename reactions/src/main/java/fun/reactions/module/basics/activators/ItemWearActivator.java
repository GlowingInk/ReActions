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

package fun.reactions.module.basics.activators;

import fun.reactions.logic.Logic;
import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.module.basics.context.ItemWearContext;
import fun.reactions.util.Utils;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.message.Msg;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class ItemWearActivator extends Activator /*implements Manageable*/ {
    // TODO: Store VirtualItem
    private final String item;
    // private final WearSlot slot;

    private ItemWearActivator(Logic base, String item/*, WearSlot slot*/) {
        super(base);
        this.item = item;
        // this.slot = slot;
    }

    public static ItemWearActivator create(Logic base, Parameters param) {
        String item = param.getString("item", Parameters.ORIGIN);
        // WearSlot slot = WearSlot.getByName(param.getParam("slot", "any"));
        return new ItemWearActivator(base, item/*, slot*/);
    }

    public static ItemWearActivator load(Logic base, ConfigurationSection cfg) {
        String item = cfg.getString("item");
        // WearSlot slot = WearSlot.getByName(cfg.getString("wear-slot", "any"));
        return new ItemWearActivator(base, item/*, slot*/);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        if (item.isEmpty() || (VirtualItem.asItemStack(item) == null)) {
            Msg.logOnce(logic.getName() + "activatorwearempty", "Failed to parse item of activator " + logic.getName());
            return false;
        }
        ItemWearContext iw = (ItemWearContext) context;
        return iw.isItemWeared(this.item);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("item", item);
        // cfg.set("wear-slot", this.slot.name());
    }

    public String getItemStr() {
        return this.item;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + this.item + ")";
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(item);
    }
}

