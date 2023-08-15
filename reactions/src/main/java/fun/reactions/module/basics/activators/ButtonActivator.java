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
import fun.reactions.util.location.ImplicitPosition;
import fun.reactions.util.parameter.BlockParameters;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// TODO Use TriBoolean (when clicking on disabled button)
public class ButtonActivator extends Activator implements Locatable {
    private final ImplicitPosition pos;

    private ButtonActivator(Logic base, ImplicitPosition pos) {
        super(base);
        this.pos = pos;
    }

    public static ButtonActivator create(Logic base, Parameters p) {
        if (!(p instanceof BlockParameters param)) return null;
        return new ButtonActivator(base, ImplicitPosition.byLocation(param.getBlock().getLocation()));
    }

    public static ButtonActivator load(Logic base, ConfigurationSection cfg) {
        return new ButtonActivator(base, ImplicitPosition.fromConfiguration(cfg));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context be = (Context) context;
        return pos.isValidAt(be.buttonLocation);
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

    public static class Context extends ActivationContext {
        private final Location buttonLocation;

        public Context(Player p, Location loc) {
            super(p);
            this.buttonLocation = loc;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return ButtonActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of(CANCEL_EVENT, Variable.property(false));
        }
    }
}
