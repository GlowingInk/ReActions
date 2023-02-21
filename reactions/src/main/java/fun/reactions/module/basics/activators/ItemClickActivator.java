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
import fun.reactions.module.basics.details.ItemClickContext;
import fun.reactions.util.enums.HandType;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class ItemClickActivator extends Activator {
    private final VirtualItem item;
    private final HandType hand;

    private ItemClickActivator(Logic base, String item, HandType hand) {
        super(base);
        this.item = VirtualItem.fromString(item);
        this.hand = hand;
    }

    public static ItemClickActivator create(Logic base, Parameters param) {
        String item = param.getString("item", param.origin());
        HandType hand = HandType.getByName(param.getString("hand", "ANY"));
        return new ItemClickActivator(base, item, hand);
    }

    public static ItemClickActivator load(Logic base, ConfigurationSection cfg) {
        String item = cfg.getString("item", "");
        HandType hand = HandType.getByName(cfg.getString("hand", "ANY"));
        return new ItemClickActivator(base, item, hand);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        ItemClickContext ie = (ItemClickContext) context;
        return hand.isValidFor(ie.getHand()) && item.isSimilar(ie.getItem());
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("item", item.toString());
        cfg.set("hand", hand.name());
    }

    @Override
    public String toString() {
        return super.toString() + " (" +
                this.item +
                "; hand:" + hand +
                ")";
    }
}
