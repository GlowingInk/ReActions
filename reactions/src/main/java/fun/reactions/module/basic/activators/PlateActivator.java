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
import fun.reactions.util.BlockUtils;
import fun.reactions.util.location.position.ImplicitPosition;
import fun.reactions.util.parameter.BlockParameters;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// TODO Use TriBoolean (when pressing on disabled plate)
public class PlateActivator extends Activator implements Locatable {
    private final ImplicitPosition pos;

    private PlateActivator(Logic base, ImplicitPosition pos) {
        super(base);
        this.pos = pos;
    }

    public static PlateActivator create(Logic base, Parameters params) {
        Block targetBlock = params instanceof BlockParameters blockParams ? blockParams.getBlock() : null;
        if (targetBlock != null && BlockUtils.isPlate(targetBlock)) {
            return new PlateActivator(base, ImplicitPosition.byLocation(targetBlock.getLocation()));
        } else {
            return new PlateActivator(base, params.getSafe("location", ImplicitPosition::byString));
        }
    }

    public static PlateActivator load(Logic base, ConfigurationSection cfg) {
        ImplicitPosition pos;
        if (cfg.isString("location")) {
            pos = ImplicitPosition.byString(cfg.getString("location"));
        } else {
            pos = ImplicitPosition.fromConfiguration(cfg);
        }
        return new PlateActivator(base, pos);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        PlateContext be = (PlateContext) context;
        return pos.isValidAt(be.getLocation());
    }

    @Override
    public boolean isLocatedAt(@NotNull World world, int x, int y, int z) {
        return pos.isValidAt(world.getName(), x, y, z);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        pos.intoConfiguration(cfg);
    }

    @Override
    public String toString() {
        return super.toString() + " (" + pos + ")";
    }

    public static class PlateContext extends ActivationContext {
        private final Location location;

        public PlateContext(Player p, Location loc) {
            super(p);
            this.location = loc;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return PlateActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of(CANCEL_EVENT, Variable.property(false));
        }

        public Location getLocation() {
            return this.location;
        }
    }
}
