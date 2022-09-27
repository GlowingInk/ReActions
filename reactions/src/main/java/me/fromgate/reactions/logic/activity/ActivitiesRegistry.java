package me.fromgate.reactions.logic.activity;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ActivitiesRegistry {

    private final Map<String, Action> actionByName;
    private final Map<String, Flag> flagByName;

    public ActivitiesRegistry() {
        actionByName = new HashMap<>();
        flagByName = new HashMap<>();
    }

    public void registerAction(@NotNull Action action) {
        if (actionByName.containsKey(action.getName().toUpperCase(Locale.ENGLISH))) {
            throw new IllegalStateException("Action '" + action.getName().toUpperCase(Locale.ENGLISH) + "' is already registered!");
        }
        actionByName.put(action.getName().toUpperCase(Locale.ENGLISH), action);
        for (String alias : Utils.getAliases(action)) {
            actionByName.putIfAbsent(alias.toUpperCase(Locale.ENGLISH), action);
        }
    }

    public void registerFlag(@NotNull Flag flag) {
        if (flagByName.containsKey(flag.getName().toUpperCase(Locale.ENGLISH))) {
            throw new IllegalStateException("Flag '" + flag.getName().toUpperCase(Locale.ENGLISH) + "' is already registered!");
        }
        flagByName.put(flag.getName().toUpperCase(Locale.ENGLISH), flag);
        for (String alias : Utils.getAliases(flag)) {
            flagByName.putIfAbsent(alias.toUpperCase(Locale.ENGLISH), flag);
        }
    }

    public @Nullable Action getAction(@NotNull String name) {
        return actionByName.get(name.toUpperCase(Locale.ENGLISH));
    }

    public @Nullable Flag getFlag(@NotNull String name) {
        return flagByName.get(name.toUpperCase(Locale.ENGLISH));
    }
}
