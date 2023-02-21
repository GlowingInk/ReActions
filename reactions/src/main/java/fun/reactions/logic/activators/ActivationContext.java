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

package fun.reactions.logic.activators;

import fun.reactions.ReActions;
import fun.reactions.logic.environment.Environment;
import fun.reactions.logic.environment.Variable;
import fun.reactions.logic.environment.Variables;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * Details of activation
 */
public abstract class ActivationContext {
    public static final String CANCEL_EVENT = "cancel_event";

    protected final Player player;
    private final boolean async; // TODO
    private Variables variables;

    public ActivationContext() {
        this(null);
    }

    public ActivationContext(@Nullable Player player) {
        this(player, false);
    }

    public ActivationContext(@Nullable Player player, boolean async) {
        this.player = player;
        this.async = async;
    }

    public abstract @NotNull Class<? extends Activator> getType();

    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of();
    }

    @Contract(pure = true)
    public final @NotNull Environment createEnvironment(@NotNull ReActions.Platform platform, @NotNull String activatorName) {
        initialize();
        return new Environment(platform, activatorName, variables, player);
    }

    public final void initialize() {
        if (isInitialized()) return;
        var varsMap = prepareVariables();
        if (varsMap.isEmpty()) {
            variables = new Variables();
        } else {
            variables = new Variables(prepareVariables());
        }
    }

    public final boolean isInitialized() {
        return variables != null;
    }

    public final @NotNull Optional<Variables> getVariables() {
        return Optional.ofNullable(variables);
    }

    public final @Nullable Player getPlayer() {
        return this.player;
    }

    public final boolean isAsync() {
        return this.async;
    }

    public boolean isCancelled() {
        return getVariables()
                .map(vars -> vars.getChanged(CANCEL_EVENT, Boolean::valueOf).orElse(false))
                .orElse(false);
    }
}
