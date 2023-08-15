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
import fun.reactions.model.activators.Locatable;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.BlockUtils;
import fun.reactions.util.enums.TriBoolean;
import fun.reactions.util.location.ImplicitPosition;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.BlockParameters;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DoorActivator extends Activator implements Locatable {
    private static final TriBoolean.Mapper STATE_MAPPER = new TriBoolean.Mapper.Builder()
            .trueMain("OPEN").addTrueVariants("OPENED")
            .falseMain("CLOSE").addFalseVariants("CLOSED")
            .build();

    private final TriBoolean state;
    private final ImplicitPosition lowerPos;

    private DoorActivator(Logic base, TriBoolean state, ImplicitPosition lowerPos) {
        super(base);
        this.state = state;
        this.lowerPos = lowerPos;
    }

    public static DoorActivator create(Logic base, Parameters params) {
        Block targetBlock = params instanceof BlockParameters blockParams ? blockParams.getBlock() : null;
        TriBoolean state = params.get(params.findKey(Parameters.ORIGIN, "state"), STATE_MAPPER::byString);
        if (targetBlock != null && targetBlock.getType() == Material.LEVER) {
            return new DoorActivator(base, state, ImplicitPosition.byLocation(targetBlock.getLocation()));
        } else {
            return new DoorActivator(base, state, params.getSafe("location", ImplicitPosition::byString));
        }
    }

    public static DoorActivator load(Logic base, ConfigurationSection cfg) {
        ImplicitPosition pos;
        if (cfg.isString("location")) {
            pos = ImplicitPosition.byString(cfg.getString("location"));
        } else {
            pos = ImplicitPosition.fromConfiguration(cfg);
        }
        TriBoolean state = STATE_MAPPER.byString(cfg.getString("state"));
        return new DoorActivator(base, state, pos);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context de = (Context) context;
        return state.isValidFor(de.isDoorOpened()) && lowerPos.isValidAt(de.getDoorLocation());
    }

    @Override
    public boolean isLocatedAt(@NotNull World world, int x, int y, int z) {
        return lowerPos.isValidAt(world.getName(), x, y, z);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        lowerPos.intoConfiguration(cfg);
        cfg.set("state", STATE_MAPPER.toString(state));
    }

    @Override
    public String toString() {
        return super.toString() + " (" +
                lowerPos + "; " +
                "state:" + STATE_MAPPER.toString(state) +
                ")";
    }

    public static class Context extends ActivationContext {
        public static final String DOOR_LOCATION = "door_loc";

        private final Block doorBlock;

        public Context(Player p, Block block) {
            super(p);
            this.doorBlock = block;
        }

        public boolean isDoorOpened() {
            return BlockUtils.isOpen(doorBlock);
        }

        public Location getDoorLocation() {
            return doorBlock.getLocation();
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return DoorActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of(
                    CANCEL_EVENT, Variable.property(false),
                    DOOR_LOCATION, Variable.simple(LocationUtils.locationToString(doorBlock))
            );
        }
    }
}
