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

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.Locatable;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.enums.TriBoolean;
import fun.reactions.util.location.position.ImplicitPosition;
import fun.reactions.util.parameter.BlockParameters;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class LeverActivator extends Activator implements Locatable {
    private final TriBoolean state;
    private final ImplicitPosition pos;

    private LeverActivator(Logic base, TriBoolean state, ImplicitPosition pos) {
        super(base);
        this.state = state;
        this.pos = pos;
    }

    public static LeverActivator create(Logic base, Parameters params) {
        Block targetBlock = params instanceof BlockParameters blockParams ? blockParams.getBlock() : null;
        TriBoolean state = params.getTriBoolean(params.findKey(Parameters.ORIGIN, "lever-state"));
        if (targetBlock != null && targetBlock.getType() == Material.LEVER) {
            return new LeverActivator(base, state, ImplicitPosition.byLocation(targetBlock.getLocation()));
        } else {
            return new LeverActivator(base, state, params.getSafe("location", ImplicitPosition::byString));
        }
    }

    public static LeverActivator load(Logic base, ConfigurationSection cfg) {
        ImplicitPosition pos;
        if (cfg.isString("location")) {
            pos = ImplicitPosition.byString(cfg.getString("location"));
        } else {
            pos = ImplicitPosition.fromConfiguration(cfg);
        }
        TriBoolean state = TriBoolean.byString(cfg.getString("lever-state", "ANY"));
        return new LeverActivator(base, state, pos);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context le = (Context) context;
        if (!pos.isValidAt(le.leverBlock.getLocation())) return false;
        if (!(le.leverBlock.getBlockData() instanceof Switch dataSwitch)) return false;
        return state.isValidFor(dataSwitch.isPowered());
    }

    @Override
    public boolean isLocatedAt(@NotNull World world, int x, int y, int z) {
        return pos.isValidAt(world.getName(), x, y, z);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        pos.intoConfiguration(cfg);
        cfg.set("lever-state", state.name());
    }

    @Override
    public String toString() {
        return super.toString() + " (" +
                pos + "," +
                " state:" + state.name() +
                ")";
    }

    public static class Context extends ActivationContext {
        private final Block leverBlock;

        public Context(Player p, Block block) {
            super(p);
            this.leverBlock = block;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return LeverActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of(CANCEL_EVENT, Variable.property(false));
        }
    }
}
