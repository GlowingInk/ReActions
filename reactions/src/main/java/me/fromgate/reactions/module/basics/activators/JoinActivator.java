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

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.JoinStorage;
import me.fromgate.reactions.util.enums.TriBoolean;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class JoinActivator extends Activator {
    private final TriBoolean firstJoin;

    private JoinActivator(ActivatorLogic base, TriBoolean firstJoin) {
        super(base);
        this.firstJoin = firstJoin;
    }

    public static JoinActivator create(ActivatorLogic base, Parameters param) {
        return new JoinActivator(base, param.getTriBoolean("first-join"));
    }

    public static JoinActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        return new JoinActivator(base, TriBoolean.getByName(cfg.getString("first-join")));
    }

    @Override
    public boolean checkStorage(@NotNull Storage event) {
        JoinStorage ce = (JoinStorage) event;
        return firstJoin.isValidFor(ce.isFirstJoin());
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("join-state", firstJoin.name());
    }

    @Override
    public String toString() {
        return super.toString() + " (first-join:" + this.firstJoin + ")";
    }

}
