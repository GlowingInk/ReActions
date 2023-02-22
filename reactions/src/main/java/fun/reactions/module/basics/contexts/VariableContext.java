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

package fun.reactions.module.basics.contexts;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.VariableActivator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class VariableContext extends ActivationContext {

    private final String variableId;
    private final String newValue;
    private final String oldValue;

    public VariableContext(Player player, String var, String newValue, String prevValue) {
        super(player);
        this.variableId = var;
        this.newValue = newValue;
        this.oldValue = prevValue;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return VariableActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                "var-id", Variable.simple(variableId),
                "var-old", Variable.simple(oldValue),
                "var-new", Variable.simple(newValue)
        );
    }

    public String getVariableId() {
        return this.variableId;
    }
}
