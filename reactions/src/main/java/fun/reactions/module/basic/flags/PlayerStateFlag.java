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

package fun.reactions.module.basic.flags;

import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Aliased.Names("STATE")
public class PlayerStateFlag implements Flag, Activity.Personal {
    private static final Vector ZERO = new Vector();

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        State state = State.getByName(paramsStr);
        return state != null && state.check.test(player, paramsStr);
    }

    @Override
    public @NotNull String getName() {
        return "PLAYER_STATE";
    }

    private enum State {
        SNEAK(Player::isSneaking, true),
        SPRINT(Player::isSprinting, true),
        VEHICLE(Player::isInsideVehicle, true),
        VEHICLE_TYPED((player, paramsStr) -> {
            if (!player.isInsideVehicle()) return false;
            return player.getVehicle().getType().name().equalsIgnoreCase(paramsStr.substring(8));
        }, true),
        SLEEP(Player::isSleeping, true),
        FLY(Player::isFlying, true),
        OP(Player::isOp, false),
        SPECTATOR_TARGET(player -> player.getSpectatorTarget() != null, true),
        GLIDE(Player::isGliding, true),
        GLOW(Player::isGlowing, false),
        DEAD(Player::isDead, true),
        ALIVE(player -> !player.isDead(), true),
        SWIM(Player::isSwimming, true),
        CLIMB(Player::isClimbing, true),
        STAND((player, paramsStr) -> {
            if (!player.getVelocity().equals(ZERO)) return false;
            for (State state : State.values()) {
                if (state.changesPosture && state.check.test(player, paramsStr)) return false;
            }
            return true;
        }, false),
        SIMPLE((player, paramsStr) -> {
            for (State state : State.values()) {
                if (state.changesPosture && state.check.test(player, paramsStr)) return false;
            }
            return true;
        }, false);

        private final BiPredicate<Player, String> check;
        private final boolean changesPosture;

        private static final Map<String, State> BY_NAME = Stream.of(values()).collect(Collectors.toMap(Enum::name, d -> d));

        State(Predicate<Player> check, boolean changesPosture) {
            this((player, paramsStr) -> check.test(player), changesPosture);
        }

        State(BiPredicate<Player, String> check, boolean changesPosture) {
            this.check = check;
            this.changesPosture = changesPosture;
        }

        public static State getByName(@NotNull String name) {
            name = name.toUpperCase(Locale.ROOT);
            if (name.startsWith("VEHICLE_")) {
                return VEHICLE_TYPED;
            }
            return BY_NAME.get(name);
        }
    }

}
