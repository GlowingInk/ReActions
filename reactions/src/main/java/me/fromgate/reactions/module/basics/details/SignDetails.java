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

package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.SignActivator;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.plain;

public class SignDetails extends Details {

    private final boolean leftClick;
    private final Location location;
    private final String[] signLines;

    public SignDetails(Player player, String[] signLines, Location loc, boolean leftClick) { // TODO Hand?
        super(player);
        this.signLines = signLines;
        this.location = loc;
        this.leftClick = leftClick;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return SignActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        Map<String, Variable> vars = new HashMap<>();
        for (int i = 0; i < signLines.length; i++) {
            vars.put("sign_line" + (i + 1), plain(signLines[i]));
        }
        vars.put("sign_loc", plain(LocationUtils.locationToString(location)));
        vars.put("click", plain(leftClick ? "left" : "right"));
        return vars;
    }

    public boolean isLeftClick() {
        return this.leftClick;
    }

    public String[] getSignLines() {
        return this.signLines;
    }
}
