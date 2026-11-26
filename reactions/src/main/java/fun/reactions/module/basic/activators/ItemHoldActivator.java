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
import fun.reactions.util.enums.HandType;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemHoldActivator extends ItemPossessionActivatorBase {
    private final HandType hand;

    private ItemHoldActivator(@NotNull Logic logic, @NotNull VirtualItem item, @NotNull HandType hand) {
        super(logic, item);
        this.hand = hand;
    }

    public static ItemHoldActivator create(Logic logic, Parameters params) {
        VirtualItem item = logic.getPlatform().getItems().fromString(params.getString("item", ""));
        HandType hand = HandType.getByName(params.getString("hand", "ANY"));
        return new ItemHoldActivator(logic, item, hand);
    }

    public @NotNull HandType getHand() {
        return hand;
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context hold = (Context) context;
        return hand.isValidFor(hold.hand) && item.isSimilar(hold.item);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        super.saveOptions(cfg);
        cfg.set("hand", hand.name());
    }

    public static final class Type extends ItemPossessionActivatorBase.Type<ItemHoldActivator, Context> {
        public Type(@NotNull ReActions.Platform platform) {
            super(platform, ItemHoldActivator.class, "ITEM_HOLD");
        }

        @Override
        public @NotNull Activator createActivator(@NotNull Logic logic, @NotNull Parameters params) {
            return create(logic, params);
        }

        @Override
        protected int recheck(@NotNull RaConfiguration config) {
            return config.reactionsCfg().itemHoldRecheck();
        }

        @Override
        protected @NotNull List<Context> contexts(@NotNull Player player, @NotNull VirtualItem item) {
            List<Context> contexts = new ArrayList<>(2);
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (item.isSimilar(mainHand)) contexts.add(new Context(player, mainHand, EquipmentSlot.HAND));
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (item.isSimilar(offHand)) contexts.add(new Context(player, offHand, EquipmentSlot.OFF_HAND));
            return contexts;
        }

        @Override
        protected boolean matches(@NotNull ItemHoldActivator activator, @NotNull Context context) {
            return activator.hand.isValidFor(context.hand);
        }
    }

    public static class Context extends ActivationContext {
        private final EquipmentSlot hand;
        private final ItemStack item;

        public Context(@NotNull Player player, @NotNull ItemStack item, @NotNull EquipmentSlot hand) {
            super(player);
            this.item = item;
            this.hand = hand;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return ItemHoldActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            Map<String, Variable> vars = new HashMap<>();
            vars.put("hand", Variable.value(hand == EquipmentSlot.HAND ? "MAIN" : "SECOND"));
            vars.put("item", new ItemVariable(item));
            vars.put("item-str", Variable.lazy(() -> ItemUtils.toDisplayString(item)));
            return vars;
        }
    }
}
