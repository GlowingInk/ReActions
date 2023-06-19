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

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Aliased.Names({"CONSUME", "EAT"})
public class ItemConsumeActivator extends Activator {
    private final VirtualItem item;
    // TODO: Hand option

    private ItemConsumeActivator(Logic base, String item) {
        super(base);
        this.item = VirtualItem.fromString(item);
    }

    public static ItemConsumeActivator create(Logic base, Parameters param) {
        String item = param.getString("item", param.origin());
        return new ItemConsumeActivator(base, item);
    }

    public static ItemConsumeActivator load(Logic base, ConfigurationSection cfg) {
        String item = cfg.getString("item", "");
        return new ItemConsumeActivator(base, item);
    }

    public boolean checkContext(@NotNull ActivationContext context) {
        Context ie = (Context) context;
        return item.isSimilar(ie.item);
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

    public static class Context extends ActivationContext {
        private final ItemStack item;
        private final boolean mainHand;

        public Context(Player p, ItemStack item, boolean mainHand) {
            super(p);
            this.item = item;
            this.mainHand = mainHand;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return ItemConsumeActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            Map<String, Variable> vars = new HashMap<>();
            vars.put(CANCEL_EVENT, Variable.property(false));
            vars.put("hand", Variable.simple(mainHand ? "MAIN" : "OFF"));
            if (item != null) {
                vars.put("item", Variable.lazy(() -> VirtualItem.asString(item)));
                vars.put("item-str", Variable.lazy(() -> ItemUtils.toDisplayString(item)));
            }
            return vars;
        }
    }
}
