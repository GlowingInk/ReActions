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

package me.fromgate.reactions.time.timers;

import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Timer {

    private boolean paused;

    private final boolean ingameTimer;
    private final Parameters params;
    private Set<String> timesIngame;

    private CronExpression timeServer;

    public Timer(Parameters params) {
        this.timesIngame = new HashSet<>();
        this.ingameTimer = params.getString("timer-type", "ingame").equalsIgnoreCase("ingame");
        this.paused = params.getBoolean("paused", false);
        this.params = params.with("paused", String.valueOf(this.paused));
        this.parseTime();

    }

    public void parseTime() {
        if (this.ingameTimer) {
            this.timesIngame = new HashSet<>();
            this.timesIngame.addAll(Arrays.asList(params.getString("time", "").split(",\\S*")));
        } else {
            String time = params.getString("time", "").replace("_", " ");
            try {
                this.timeServer = new CronExpression(time);
            } catch (ParseException e) {
                Msg.logOnce(time, "Failed to parse cron format: " + time);
                this.timeServer = null;
                e.printStackTrace();
            }
        }
    }


    public boolean isTimeToRun() {
        if (isPaused()) return false;
        return this.ingameTimer ? isIngameTimeToRun() : isServerTimeToRun();
    }

    private boolean isServerTimeToRun() {
        if (this.ingameTimer) return false;
        if (this.timeServer == null) return false;
        return (this.timeServer.isSatisfiedBy(new Date()));
    }

    private boolean isIngameTimeToRun() {
        return ingameTimer && timesIngame.contains(TimeUtils.formatIngameTime());
    }

    @Override
    public String toString() {
        return params.getString("activator", "Undefined") + " : " + params.getString("time", "Undefined") + (this.isIngameTimer() ? " (ingame)" : " (server)");
    }

    public boolean isPaused() {return this.paused;}

    public boolean isIngameTimer() {return this.ingameTimer;}

    public Parameters getParams() {return this.params;}

    public Set<String> getTimesIngame() {return this.timesIngame;}

    public CronExpression getTimeServer() {return this.timeServer;}

    public void setPaused(boolean paused) {this.paused = paused; }
}
