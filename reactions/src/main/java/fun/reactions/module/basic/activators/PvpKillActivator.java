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
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PvpKillActivator extends Activator {
    private PvpKillActivator(Logic base) {
        super(base);
    }

    public static PvpKillActivator create(Logic base, Parameters ignore) {
        return new PvpKillActivator(base);
    }

    public static PvpKillActivator load(Logic base, ConfigurationSection ignore) {
        return new PvpKillActivator(base);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        return true;
    }

    public static class PvpKillContext extends ActivationContext {
        private final Player killedPlayer;

        public PvpKillContext(Player player, Player killedPlayer) {
            super(player);
            this.killedPlayer = killedPlayer;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return PvpKillActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of("targetplayer", Variable.simple(killedPlayer.getName()));
        }
    }
}
