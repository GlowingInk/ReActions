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
import fun.reactions.model.environment.Variable;
import fun.reactions.util.Utils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class VariableActivator extends Activator {
    private final String id;
    private final boolean personal;

    private VariableActivator(Logic base, String id, boolean personal) {
        super(base);
        this.id = id;
        this.personal = personal;
    }

    public static VariableActivator create(Logic base, Parameters param) {
        String id = param.getString("id", "UnknownVariable");
        boolean personal = param.getBoolean("personal", false);
        return new VariableActivator(base, id, personal);
    }

    public static VariableActivator load(Logic base, ConfigurationSection cfg) {
        String id = cfg.getString("variable-id", "UnknownVariable");
        boolean personal = cfg.getBoolean("personal", false);
        return new VariableActivator(base, id, personal);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context ve = (Context) context;
        if (!this.id.equalsIgnoreCase(ve.variableId)) return false;
        return !personal || ve.getPlayer() == null;
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("variable-id", id);
        cfg.set("personal", personal);
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" (");
        sb.append("variable id:").append(this.id);
        if (this.personal) sb.append(" personal:true");
        sb.append(")");
        return sb.toString();
    }

    public static class Context extends ActivationContext {
        private final String variableId;
        private final String newValue;
        private final String oldValue;

        public Context(Player player, String var, String newValue, String prevValue) {
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
    }
}
