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

package me.fromgate.reactions.logic.activators;


import me.fromgate.reactions.logic.ActivatorType;
import me.fromgate.reactions.logic.storages.Storage;
import me.fromgate.reactions.logic.storages.VariableStorage;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;

public class VariableActivator extends Activator {
    private final String id;
    private final boolean personal;

    private VariableActivator(ActivatorBase base, String id, boolean personal) {
        super(base);
        this.id = id;
        this.personal = personal;
    }

    public static VariableActivator create(ActivatorBase base, Parameters param) {
        String id = param.getParam("id", "UnknownVariable");
        boolean personal = param.getParam("personal", false);
        return new VariableActivator(base, id, personal);
    }

    public static VariableActivator load(ActivatorBase base, ConfigurationSection cfg) {
        String id = cfg.getString("variable-id", "UnknownVariable");
        boolean personal = cfg.getBoolean("personal", false);
        return new VariableActivator(base, id, personal);
    }

    @Override
    public boolean activate(Storage event) {
        VariableStorage ve = (VariableStorage) event;
        if (!this.id.equalsIgnoreCase(ve.getVariableId())) return false;
        return !personal || ve.getPlayer() == null;
    }

    @Override
    public void save(ConfigurationSection cfg) {
        cfg.set("variable-id", id);
        cfg.set("personal", personal);
    }

    @Override
    public ActivatorType getType() {
        return ActivatorType.VARIABLE;
    }

    @Override
    public boolean isValid() {
        return !Util.isStringEmpty(id);
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
}
