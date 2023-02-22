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
import fun.reactions.module.basics.contexts.RespawnContext;
import fun.reactions.util.enums.DeathCause;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class RespawnActivator extends Activator {

    private final DeathCause deathCause;

    private RespawnActivator(Logic base, DeathCause cause) {
        super(base);
        this.deathCause = cause;
    }

    public static RespawnActivator create(Logic base, Parameters param) {
        DeathCause cause = DeathCause.getByName(param.getString("cause", param.origin()));
        return new RespawnActivator(base, cause);
    }

    public static RespawnActivator load(Logic base, ConfigurationSection cfg) {
        DeathCause cause = DeathCause.getByName(cfg.getString("death-cause", "ANY"));
        return new RespawnActivator(base, cause);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        RespawnContext pe = (RespawnContext) context;
        return this.deathCause == DeathCause.ANY || pe.getDeathCause() == this.deathCause;
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("death-cause", deathCause.name());
    }

    @Override
    public String toString() {
        return super.toString() + "(" + this.deathCause.name() + ")";
    }
}