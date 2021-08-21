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

package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.MessageStorage;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

@Alias("MSG")
public class MessageActivator extends Activator {

    private final CheckType type;
    private final Source source;
    private final String mask;

    private MessageActivator(ActivatorLogic base, CheckType type, Source source, String mask) {
        super(base);
        this.type = type;
        this.source = source;
        this.mask = mask;
    }

    public static MessageActivator create(ActivatorLogic base, Parameters param) {
        CheckType type = CheckType.getByName(param.getString("type", "EQUAL"));
        Source source = Source.getByName(param.getString("source", "CHAT_MESSAGE"));
        String mask = param.getString("mask", param.getString("message", "Message mask"));
        return new MessageActivator(base, type, source, mask);
    }

    public static MessageActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        CheckType type = CheckType.getByName(cfg.getString("type", "EQUAL"));
        Source source = Source.getByName(cfg.getString("source", "CHAT_INPUT"));
        String mask = cfg.getString("mask", "Message mask");
        return new MessageActivator(base, type, source, mask);
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("mask", mask);
        cfg.set("type", type.name());
        cfg.set("source", source.name());
    }

    @Override
    public boolean checkStorage(Storage event) {
        MessageStorage e = (MessageStorage) event;
        return e.isForActivator(this);
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(mask);
    }

    public boolean filterMessage(Source source, String message) {
        if (source != this.source && this.source != Source.ALL) return false;
        return filter(message);
    }

    private boolean filter(String message) {
        return switch (type) {
            case CONTAINS -> message.toLowerCase(Locale.ENGLISH).contains(this.mask.toLowerCase(Locale.ENGLISH));
            case END -> message.toLowerCase(Locale.ENGLISH).endsWith(this.mask.toLowerCase(Locale.ENGLISH));
            case EQUAL -> message.equalsIgnoreCase(this.mask);
            case REGEX -> message.matches(this.mask);
            case START -> message.toLowerCase(Locale.ENGLISH).startsWith(this.mask.toLowerCase(Locale.ENGLISH));
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" (");
        sb.append("type:").append(this.type.name());
        sb.append(" source:").append(this.source.name());
        sb.append(" mask:").append(this.mask);
        sb.append(")");
        return sb.toString();
    }

    public enum CheckType {
        REGEX,
        CONTAINS,
        EQUAL,
        START,
        END;

        public static CheckType getByName(String name) {
            if (name.equalsIgnoreCase("contain")) return CheckType.CONTAINS;
            if (name.equalsIgnoreCase("equals")) return CheckType.EQUAL;
            for (CheckType t : CheckType.values()) {
                if (t.name().equalsIgnoreCase(name)) return t;
            }
            return CheckType.EQUAL;
        }

        public static boolean isValid(String name) {
            for (CheckType t : CheckType.values()) {
                if (t.name().equalsIgnoreCase(name)) return true;
            }
            return false;
        }
    }

    public enum Source {
        ALL,
        CHAT_INPUT,
        CONSOLE_INPUT,
        CHAT_OUTPUT,
        LOG_OUTPUT;
        //ANSWER;

        public static Source getByName(String name) {
            for (Source source : Source.values()) {
                if (source.name().equalsIgnoreCase(name)) return source;
            }
            return Source.ALL;
        }

        public static boolean isValid(String name) {
            for (Source source : Source.values()) {
                if (source.name().equalsIgnoreCase(name)) return true;
            }
            return false;
        }
    }
}
