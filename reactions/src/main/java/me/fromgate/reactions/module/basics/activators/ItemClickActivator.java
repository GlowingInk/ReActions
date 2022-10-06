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
import me.fromgate.reactions.module.basics.storages.ItemClickStorage;
import me.fromgate.reactions.util.enums.HandType;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;

public class ItemClickActivator extends Activator {
    private final VirtualItem item;
    private final HandType hand;

    private ItemClickActivator(ActivatorLogic base, String item, HandType hand) {
        super(base);
        this.item = VirtualItem.fromString(item);
        this.hand = hand;
    }

    public static ItemClickActivator create(ActivatorLogic base, Parameters param) {
        String item = param.getString("item", param.toString());
        HandType hand = HandType.getByName(param.getString("hand", "ANY"));
        return new ItemClickActivator(base, item, hand);
    }

    public static ItemClickActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String item = cfg.getString("item", "");
        HandType hand = HandType.getByName(cfg.getString("hand", "ANY"));
        return new ItemClickActivator(base, item, hand);
    }

    @Override
    public boolean checkStorage(Storage event) {
        ItemClickStorage ie = (ItemClickStorage) event;
        if (hand.checkOff(ie.isMainHand())) return false;
        return item.isSimilar(ie.getItem());
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("item", item.toString());
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
}
