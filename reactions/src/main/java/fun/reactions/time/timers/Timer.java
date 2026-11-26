/*
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *   *
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

package fun.reactions.time.timers;

import fun.reactions.ReActions;
import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.Parameters;
import fun.reactions.util.time.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public abstract sealed class Timer permits Timer.Ingame, Timer.Server {
    private final Parameters params;
    private boolean paused;

    private Timer(@NotNull Parameters params) {
        this.paused = params.getBoolean("paused", false);
        this.params = params.with("paused", String.valueOf(this.paused));
    }

    public static @NotNull Timer of(@NotNull Parameters params) {
        return params.getString("timer-type", Ingame.TYPE).equalsIgnoreCase(Ingame.TYPE)
                ? new Ingame(params)
                : new Server(params);
    }

    public abstract @NotNull String getType();

    protected abstract boolean isTime();

    protected abstract @Nullable Duration untilNextTime();

    public boolean isTimeToRun() {
        return !paused && isTime();
    }

    public @Nullable Duration untilNextRun() {
        return paused ? null : untilNextTime();
    }

    @Override
    public String toString() {
        return params.getString("activator", "Undefined") + " : " + params.getString("time", "Undefined") + " (" + getType() + ")";
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public @NotNull Parameters getParams() {
        return params;
    }

    public static final class Ingame extends Timer {
        public static final String TYPE = "ingame";

        private static final long TICKS_PER_DAY = 24000;

        private final Set<String> times = new HashSet<>();
        private final @Nullable String world;
        private @Nullable String lastTime;

        private Ingame(@NotNull Parameters params) {
            super(params);
            String world = params.getString("world", "");
            this.world = world.isEmpty() ? null : world;
            for (String time : params.getString("time", "").split(",\\s*")) {
                String normalized = normalize(time);
                if (normalized != null) times.add(normalized);
            }
        }

        @Override
        public @NotNull String getType() {
            return TYPE;
        }

        public @Nullable String getWorld() {
            return world;
        }

        @Override
        protected boolean isTime() {
            World resolved = resolveWorld();
            if (resolved == null) return false;
            String time = TimeUtils.formatIngameTime(resolved);
            if (time.equals(lastTime)) return false;
            lastTime = time;
            return times.contains(time);
        }

        @Override
        protected @Nullable Duration untilNextTime() {
            World resolved = resolveWorld();
            if (resolved == null) return null;
            long now = resolved.getTime();
            long closest = -1;
            for (String time : times) {
                long delta = Math.floorMod(ticks(time) - now, TICKS_PER_DAY);
                if (closest == -1 || delta < closest) closest = delta;
            }
            return closest == -1 ? null : Duration.ofMillis(closest * TimeUtils.MS_PER_TICK);
        }

        private @Nullable World resolveWorld() {
            if (world == null) return Bukkit.getWorlds().getFirst();
            World resolved = Bukkit.getWorld(world);
            if (resolved == null) ReActions.warnOnce("timer_world:" + world, "Timer world '{}' doesn't exist", world);
            return resolved;
        }

        private static @Nullable String normalize(@NotNull String time) {
            int split = time.indexOf(':');
            if (split == -1) return null;
            int hours = NumberUtils.asInteger(time.substring(0, split).trim(), -1);
            int minutes = NumberUtils.asInteger(time.substring(split + 1).trim(), -1);
            if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) return null;
            return TimeUtils.formatClock(hours, minutes);
        }

        private static long ticks(@NotNull String normalized) {
            int split = normalized.indexOf(':');
            int hours = Integer.parseInt(normalized.substring(0, split));
            int minutes = Integer.parseInt(normalized.substring(split + 1));
            return Math.floorMod(hours - 6, 24) * 1000L + (minutes * 1000L + 59) / 60;
        }
    }

    public static final class Server extends Timer {
        public static final String TYPE = "server";

        private final @Nullable CronExpression cron;

        private Server(@NotNull Parameters params) {
            super(params);
            this.cron = parseCron(params.getString("time", "").replace("_", " "));
        }

        @Override
        public @NotNull String getType() {
            return TYPE;
        }

        @Override
        protected boolean isTime() {
            return cron != null && cron.isSatisfiedBy(new Date());
        }

        @Override
        protected @Nullable Duration untilNextTime() {
            if (cron == null) return null;
            Date now = new Date();
            Date next = cron.getTimeAfter(now);
            return next == null ? null : Duration.ofMillis(next.getTime() - now.getTime());
        }

        private static @Nullable CronExpression parseCron(@NotNull String time) {
            try {
                return new CronExpression(time);
            } catch (ParseException ex) {
                ReActions.warnOnce("timer_cron:" + time, "Failed to parse cron format: {}", time);
                return null;
            }
        }
    }
}
