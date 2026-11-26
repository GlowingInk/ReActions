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

import fun.reactions.ReActions;
import fun.reactions.cfg.RaConfiguration;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.variables.ItemVariable;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemWearActivator extends ItemPossessionActivatorBase {
    private ItemWearActivator(@NotNull Logic logic, @NotNull VirtualItem item) {
        super(logic, item);
    }

    public static ItemWearActivator create(Logic logic, Parameters params) {
        return new ItemWearActivator(logic, logic.getPlatform().getItems().fromString(params.getString("item", "")));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        return item.isSimilar(((Context) context).item);
    }

    public static final class Type extends ItemPossessionActivatorBase.Type<ItemWearActivator, Context> {
        private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        );

        public Type(@NotNull ReActions.Platform platform) {
            super(platform, ItemWearActivator.class, "ITEM_WEAR");
        }

        @Override
        public @NotNull Activator createActivator(@NotNull Logic logic, @NotNull Parameters params) {
            return create(logic, params);
        }

        @Override
        protected int recheck(@NotNull RaConfiguration config) {
            return config.reactionsCfg().itemWearRecheck();
        }

        @Override
        protected @NotNull List<Context> contexts(@NotNull Player player, @NotNull VirtualItem item) {
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack armor = player.getInventory().getItem(slot);
                if (item.isSimilar(armor)) return List.of(new Context(player, armor, slot));
            }
            return List.of();
        }
    }

    public static class Context extends ActivationContext {
        private final ItemStack item;
        private final EquipmentSlot slot;

        public Context(@NotNull Player player, @NotNull ItemStack item, @NotNull EquipmentSlot slot) {
            super(player);
            this.item = item;
            this.slot = slot;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return ItemWearActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            Map<String, Variable> vars = new HashMap<>();
            vars.put("slot", Variable.value(slot.name()));
            vars.put("item", new ItemVariable(item));
            vars.put("item-str", Variable.lazy(() -> ItemUtils.toDisplayString(item)));
            return vars;
        }
    }
}
