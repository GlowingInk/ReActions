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

package me.fromgate.reactions.module.basics.storages;

import lombok.Getter;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.ExecActivator;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ExecStorage extends Storage {

    private final Map<String, String> tempVars;

    public ExecStorage(Player player) {
        super(player);
        this.tempVars = null;
    }

    @Override
    public Class<? extends Activator> getType() {
        return ExecActivator.class;
    }

    public ExecStorage(Player player, Parameters tempVars) {
        super(player);
        this.tempVars = tempVars.getMap();
    }

    public ExecStorage(Player player, Map<String, String> tempVars) {
        super(player);
        this.tempVars = tempVars;
    }

    @Override
    protected Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        if (this.tempVars != null && !this.tempVars.isEmpty())
            tempVars.putAll(this.tempVars);
        return tempVars;
    }
}