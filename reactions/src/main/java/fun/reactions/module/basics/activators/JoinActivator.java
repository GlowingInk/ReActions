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

package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.module.basics.contexts.JoinContext;
import fun.reactions.util.enums.TriBoolean;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class JoinActivator extends Activator {
    private final TriBoolean firstJoin;

    private JoinActivator(Logic base, TriBoolean firstJoin) {
        super(base);
        this.firstJoin = firstJoin;
    }

    public static JoinActivator create(Logic base, Parameters param) {
        return new JoinActivator(base, param.getTriBoolean("first-join"));
    }

    public static JoinActivator load(Logic base, ConfigurationSection cfg) {
        return new JoinActivator(base, TriBoolean.of(cfg.getString("first-join")));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        JoinContext ce = (JoinContext) context;
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
