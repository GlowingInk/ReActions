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

package fun.reactions.module.basics.context;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.SignActivator;
import fun.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

public class SignContext extends ActivationContext {

    private final boolean leftClick;
    private final Location location;
    private final String[] signLines;

    public SignContext(Player player, String[] signLines, Location loc, boolean leftClick) { // TODO Hand?
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
            vars.put("sign_line" + (i + 1), Variable.simple(signLines[i]));
        }
        vars.put("sign_loc", simple(LocationUtils.locationToString(location)));
        vars.put("click", Variable.simple(leftClick ? "left" : "right"));
        return vars;
    }

    public boolean isLeftClick() {
        return this.leftClick;
    }

    public String[] getSignLines() {
        return this.signLines;
    }
}
