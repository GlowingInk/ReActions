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


package fun.reactions.module.basics.context;

import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.logic.environment.Variable;
import fun.reactions.module.basics.activators.MessageActivator;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.NumberUtils.Is;
import fun.reactions.util.function.FunctionalUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class MessageContext extends ActivationContext {
    public static final String MESSAGE = "message";
    private static final Pattern NOT_D = Pattern.compile("\\D+");

    private final String message;
    private final MessageActivator activator;

    public MessageContext(Player player, MessageActivator activator, String message) {
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
                        if (NumberUtils.isNumber(arg, Is.INTEGER)) {
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

    public String getMessage() {
        return this.message;
    }
}
