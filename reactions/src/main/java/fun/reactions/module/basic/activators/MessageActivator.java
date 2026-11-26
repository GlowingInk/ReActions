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
import fun.reactions.model.activators.ActivatorsManager;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.Variables;
import fun.reactions.util.Utils;
import fun.reactions.util.function.FunctionalUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
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
        Source source = Source.getByName(param.getString("source", "CHAT_INPUT"));
        String mask = param.getString("mask", "Message mask");
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
        Context message = (Context) context;
        return (source == Source.ALL || source == message.source) && filter(message.message);
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(mask);
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

    public static final class Type implements ActivatorType {
        private final Map<Source, List<MessageActivator>> activators = new EnumMap<>(Source.class);

        public Type() {
            for (Source source : Source.values()) {
                activators.put(source, new CopyOnWriteArrayList<>());
            }
        }

        @Override
        public @NotNull String getName() {
            return "MESSAGE";
        }

        @Override
        public @NotNull Class<MessageActivator> getActivatorClass() {
            return MessageActivator.class;
        }

        @Override
        public @NotNull Activator createActivator(@NotNull Logic logic, @NotNull Parameters params) {
            return create(logic, params);
        }

        @Override
        public boolean isNeedBlock() {
            return false;
        }

        @Override
        public void include(@NotNull Activator activator) {
            MessageActivator message = (MessageActivator) activator;
            activators.get(message.source).add(message);
        }

        @Override
        public void discard(@NotNull Activator activator) {
            MessageActivator message = (MessageActivator) activator;
            activators.get(message.source).remove(message);
        }

        public @NotNull Optional<Variables> trigger(
                @Nullable CommandSender sender,
                @NotNull Source source,
                @NotNull String message
        ) {
            MessageActivator activator = find(source, message);
            if (activator == null && source != Source.ALL) activator = find(Source.ALL, message);
            if (activator == null) return Optional.empty();
            Context context = new Context(sender instanceof Player player ? player : null, source, message);
            ActivatorsManager.executeLogic(context, List.of(activator));
            return context.getVariables();
        }

        private @Nullable MessageActivator find(@NotNull Source source, @NotNull String message) {
            for (MessageActivator activator : activators.get(source)) {
                if (activator.filter(message)) return activator;
            }
            return null;
        }
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
        CHAT_OUTPUT, // TODO Doesn't work; should probably just be removed
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

        private final Source source;
        private final String message;

        public Context(@Nullable Player player, @NotNull Source source, @NotNull String message) {
            super(player);
            this.source = source;
            this.message = message;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return MessageActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            Map<String, Variable> vars = new HashMap<>();
            vars.put(CANCEL_EVENT, Variable.value(false));
            vars.put(MESSAGE, Variable.value(message));
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
            vars.put("word-count", Variable.lazy(() -> Integer.toString(msgInfo.get().words.size())));
            vars.put("num-count", Variable.lazy(() -> Integer.toString(msgInfo.get().nums.size())));
            vars.put("int-count", Variable.lazy(() -> Integer.toString(msgInfo.get().ints.size())));
            return vars;
        }

        private static class MessageInfo {
            private final List<String> words;
            private final List<String> nums;
            private final List<String> ints;

            private MessageInfo(String[] args) {
                if (args.length > 0) {
                    this.words = Arrays.asList(args);
                    this.nums = new ArrayList<>(0);
                    this.ints = new ArrayList<>(0);
                    for (String arg : args) {
                        if (NumberUtils.IS_NUMBER.test(arg)) {
                            nums.add(arg);
                            if (NumberUtils.IS_INTEGER.test(arg)) {
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
