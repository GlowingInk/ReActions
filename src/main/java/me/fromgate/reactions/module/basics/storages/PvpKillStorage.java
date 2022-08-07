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

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.PvpKillActivator;
import me.fromgate.reactions.util.collections.MapBuilder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PvpKillStorage extends Storage {

    private final Player killedPlayer;

    public PvpKillStorage(Player player, Player killedPlayer) {
        super(player);
        this.killedPlayer = killedPlayer;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return PvpKillActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        return MapBuilder.single("targetplayer", killedPlayer.getName());
    }

    public Player getKilledPlayer() {return this.killedPlayer;}
}
