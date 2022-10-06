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
import me.fromgate.reactions.module.basics.storages.ItemHoldStorage;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.enums.HandType;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;

public class ItemHoldActivator extends Activator {
    // TODO: Store VirtualItem
    private final String item;
    private final HandType hand;

    private ItemHoldActivator(ActivatorLogic base, String item, HandType hand) {
        super(base);
        this.item = item;
        this.hand = hand;
    }

    public String getItem() {return this.item;}

    public HandType getHand() {return this.hand;}

    public static ItemHoldActivator create(ActivatorLogic base, Parameters param) {
        String item = param.getString("item", "");
        HandType hand = HandType.getByName(param.getString("hand", "ANY"));
        return new ItemHoldActivator(base, item, hand);
    }

    public static ItemHoldActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String item = cfg.getString("item", "");
        HandType hand = HandType.getByName(cfg.getString("hand", "ANY"));
        return new ItemHoldActivator(base, item, hand);
    }

    @Override
    public boolean checkStorage(Storage event) {
        if (item.isEmpty() || (VirtualItem.asItem(item) == null)) {
            Msg.logOnce(logic.getName() + "activatorholdempty", "Failed to parse itemStr of activator " + logic.getName());
            return false;
        }
        ItemHoldStorage ie = (ItemHoldStorage) event;

        if (!hand.checkMain(ie.isMainHand())) return false;

        return VirtualItem.isSimilar(this.item, ie.getItem());
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("item", item);
        cfg.set("hand", hand.name());
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                this.item +
                "; hand:" + hand +
                ")";
        return sb;
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(item);
    }
}

