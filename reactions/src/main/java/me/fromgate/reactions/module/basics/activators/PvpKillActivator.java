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
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class PvpKillActivator extends Activator {
    private PvpKillActivator(Logic base) {
        super(base);
    }

    public static PvpKillActivator create(Logic base, Parameters ignore) {
        return new PvpKillActivator(base);
    }

    public static PvpKillActivator load(Logic base, ConfigurationSection ignore) {
        return new PvpKillActivator(base);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        return true;
    }

}
