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
import fun.reactions.util.NumberUtils;
import fun.reactions.util.Utils;
import fun.reactions.util.function.FunctionalUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Aliased.Names("MSG")
public class MessageActivator extends Activator {

    private final CheckType type;
    private final Source source;
    private final String mask;

    private MessageActivator(Logic base, CheckType type, Source source, String mask) {
        super(base);
        this.type = type;
        this.source = source;
        this.mask = mask;
    }

    public static MessageActivator create(Logic base, Parameters param) {
        CheckType type = CheckType.getByName(param.getString("type", "EQUAL"));
        Source source = Source.getByName(param.getString("source", "CHAT_MESSAGE"));
        String mask = param.getString("mask", param.getString("message", "Message mask"));
        return new MessageActivator(base, type, source, mask);
    }

    public static MessageActivator load(Logic base, ConfigurationSection cfg) {
        CheckType type = CheckType.getByName(cfg.getString("type", "EQUAL"));
        Source source = Source.getByName(cfg.getString("source", "CHAT_INPUT"));
        String mask = cfg.getString("mask", "Message mask");
        return new MessageActivator(base, type, source, mask);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("mask", mask);
        cfg.set("type", type.name());
        cfg.set("source", source.name());
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context e = (Context) context;
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
            case CONTAINS -> message.toLowerCase(Locale.ROOT).contains(this.mask.toLowerCase(Locale.ROOT));
            case END -> message.toLowerCase(Locale.ROOT).endsWith(this.mask.toLowerCase(Locale.ROOT));
            case EQUAL -> message.equalsIgnoreCase(this.mask);
            case REGEX -> message.matches(this.mask);
            case START -> message.toLowerCase(Locale.ROOT).startsWith(this.mask.toLowerCase(Locale.ROOT));
        };
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "type:" + this.type.name() +
                " source:" + this.source.name() +
                " mask:" + this.mask +
                ")";
        return sb;
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

    public static class Context extends ActivationContext {
        public static final String MESSAGE = "message";
        private static final Pattern NOT_D = Pattern.compile("\\D+");

        private final String message;
        private final MessageActivator activator;

        public Context(Player player, MessageActivator activator, String message) {
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
        protected @NotNull Map<String, Variable> prepareVariables() {
            Map<String, Variable> vars = new HashMap<>();
            vars.put(CANCEL_EVENT, Variable.property(false));
            vars.put(MESSAGE, Variable.property(message));
            String[] words = message.split(" ");
            Supplier<MessageInfo> msgInfo = FunctionalUtils.asSafeCaching(() -> new MessageInfo(words));
            for (int i = 0; i < words.length; i++) {
                int index = i;
                int j = i + 1;
                vars.put("word" + j, Variable.lazy(() -> msgInfo.get().words.get(index)));
                vars.put("wnum" + j, Variable.lazy(() -> NOT_D.matcher(msgInfo.get().words.get(index)).replaceAll("")));
                vars.put("num" + j, Variable.lazy(() -> msgInfo.get().nums.size() > index ? msgInfo.get().nums.get(index) : ""));
                vars.put("int" + j, Variable.lazy(() -> msgInfo.get().ints.size() > index ? msgInfo.get().ints.get(index) : ""));
            }
            vars.put("word-count", Variable.lazy(() -> String.valueOf(msgInfo.get().words.size())));
            vars.put("num-count", Variable.lazy(() -> String.valueOf(msgInfo.get().nums.size())));
            vars.put("int-count", Variable.lazy(() -> String.valueOf(msgInfo.get().ints.size())));
            return vars;
        }

        private static class MessageInfo {
            private final List<String> words;
            private final List<String> nums;
            private final List<String> ints;

            public MessageInfo(String[] args) {
                if (args.length > 0) {
                    this.words = Arrays.asList(args);
                    this.nums = new ArrayList<>(0);
                    this.ints = new ArrayList<>(0);
                    for (String arg : args) {
                        if (NumberUtils.isNumber(arg)) {
                            nums.add(arg);
                            if (NumberUtils.isNumber(arg, NumberUtils.Is.INTEGER)) {
                                ints.add(arg);
                            }
                        }
                    }
                } else {
                    this.words = List.of();
                    this.nums = List.of();
                    this.ints = List.of();
                }
            }
        }
    }
}
