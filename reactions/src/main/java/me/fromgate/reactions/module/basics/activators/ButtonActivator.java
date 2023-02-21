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

import me.fromgate.reactions.logic.Logic;
import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Locatable;
import me.fromgate.reactions.module.basics.details.ButtonContext;
import me.fromgate.reactions.util.location.ImplicitPosition;
import me.fromgate.reactions.util.parameter.BlockParameters;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

// TODO Use TriBoolean (when clicking on disabled button)
public class ButtonActivator extends Activator implements Locatable {
    private final ImplicitPosition pos;

    private ButtonActivator(Logic base, ImplicitPosition pos) {
        super(base);
        this.pos = pos;
    }

    public static ButtonActivator create(Logic base, Parameters p) {
        if (!(p instanceof BlockParameters param)) return null;
        return new ButtonActivator(base, ImplicitPosition.of(param.getBlock().getLocation()));
    }

    public static ButtonActivator load(Logic base, ConfigurationSection cfg) {
        return new ButtonActivator(base, ImplicitPosition.fromConfiguration(cfg));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        ButtonContext be = (ButtonContext) context;
        return pos.isValidAt(be.getButtonLocation());
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
}
