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
import fun.reactions.util.bool.TriBoolean;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
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
        return new JoinActivator(base, TriBoolean.byString(cfg.getString("first-join")));
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context ce = (Context) context;
        return firstJoin.isValidFor(ce.firstJoin);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("join-state", firstJoin.name());
    }

    @Override
    public String toString() {
        return super.toString() + " (first-join:" + this.firstJoin + ")";
    }

    public static class Context extends ActivationContext {
        private final boolean firstJoin;

        public Context(Player p, boolean firstJoin) {
            super(p);
            this.firstJoin = firstJoin;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return JoinActivator.class;
        }
        // TODO: Message
    }
}
