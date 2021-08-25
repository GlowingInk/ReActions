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

import lombok.Getter;
import me.fromgate.reactions.util.data.DataValue;
import me.fromgate.reactions.util.data.RaContext;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

/**
 * Storages are used to transfer some data to activators
 */
@Getter
public abstract class Storage {
    public static final String CANCEL_EVENT = "cancel_event";

    protected final Player player;
    // TODO
    private final boolean async;

    // Default temporary placeholders
    private Map<String, String> variables;
    private Map<String, DataValue> changeables;

    public Storage() {
        this(null);
    }

    public Storage(Player player) {
        this(player, false);
    }

    public Storage(Player player, boolean async) {
        this.player = player;
        this.async = async;
    }

    public final void init() {
        variables = prepareVariables();
        changeables = prepareChangeables();
    }

    public abstract Class<? extends Activator> getType();

    // TODO: dynamicVariables Supplier<String> for expensive calculations? E.g. CommandStorage
    protected Map<String, String> prepareVariables() {
        return Collections.emptyMap();
    }

    protected Map<String, DataValue> prepareChangeables() {
        return Collections.emptyMap();
    }

    public final RaContext generateContext(String activator) {
        return new RaContext(activator, variables, changeables, player);
    }

}
