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

package fun.reactions.module.basic;

import fun.reactions.module.basic.activators.MessageActivator;

import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class LogHandler extends StreamHandler {
    @Override
    public void publish(LogRecord record) {
        try {
            ContextManager.triggerMessage(null, MessageActivator.Source.LOG_OUTPUT, record.getMessage());
        } catch (Exception ignored) { }
        super.publish(record);
    }
}
