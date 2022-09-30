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


package me.fromgate.reactions.module.basics.storages;

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.data.StringValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.MessageActivator;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.collections.MapBuilder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MessageStorage extends Storage {
    public static final String MESSAGE = "message";
    private static final Pattern NOT_D = Pattern.compile("\\D+");

    private final String message;
    private final MessageActivator activator;


    public MessageStorage(Player player, MessageActivator activator, String message) {
        super(player);
        this.activator = activator;
        this.message = message;
    }

    public boolean isForActivator(MessageActivator messageActivator) {
        return this.activator.equals(messageActivator);
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return MessageActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("message", message);
        String[] args = message.split(" ");
        int countInt = 0;
        int countNum = 0;
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                tempVars.put("word" + (i + 1), args[i]);
                tempVars.put("wnum" + (i + 1), NOT_D.matcher(args[i]).replaceAll(""));
                if (NumberUtils.INT.matcher(args[i]).matches()) {
                    countInt++;
                    tempVars.put("int" + countInt, args[i]);
                }
                if (NumberUtils.FLOAT.matcher(args[i]).matches()) {
                    countNum++;
                    tempVars.put("num" + countNum, args[i]);
                }
            }
        }
        tempVars.put("word-count", Integer.toString(args.length));
        tempVars.put("int-count", Integer.toString(countInt));
        tempVars.put("num-count", Integer.toString(countNum));
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return new MapBuilder<String, DataValue>()
                .put(CANCEL_EVENT, new BooleanValue(false))
                .put(MESSAGE, new StringValue(message))
                .build();
    }

    public String getMessage() {return this.message;}

    public MessageActivator getActivator() {return this.activator;}
}
