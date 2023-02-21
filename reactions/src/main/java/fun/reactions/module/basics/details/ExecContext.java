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

package fun.reactions.module.basics.details;

import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.logic.activators.FunctionActivator;
import fun.reactions.logic.environment.Variable;
import fun.reactions.logic.environment.Variables;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Deprecated
public class ExecContext extends ActivationContext {
    private final Variables vars;

    public ExecContext(Player player, Variables vars) {
        super(player);
        this.vars = vars;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return FunctionActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return vars.forkMap();
    }
}