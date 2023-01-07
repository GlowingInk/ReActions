package me.fromgate.reactions.logic.activity;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.naming.Aliased;
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
        if (actionByName.containsKey(action.getName().toUpperCase(Locale.ROOT))) {
            throw new IllegalStateException("Action '" + action.getName().toUpperCase(Locale.ROOT) + "' is already registered!");
        }
        actionByName.put(action.getName().toUpperCase(Locale.ROOT), action);
        for (String alias : Aliased.getAliasesOf(action)) {
            actionByName.putIfAbsent(alias.toUpperCase(Locale.ROOT), action);
        }
    }

    public void registerFlag(@NotNull Flag flag) {
        if (flagByName.containsKey(flag.getName().toUpperCase(Locale.ROOT))) {
            throw new IllegalStateException("Flag '" + flag.getName().toUpperCase(Locale.ROOT) + "' is already registered!");
        }
        flagByName.put(flag.getName().toUpperCase(Locale.ROOT), flag);
        for (String alias : Aliased.getAliasesOf(flag)) {
            flagByName.putIfAbsent(alias.toUpperCase(Locale.ROOT), flag);
        }
    }

    public @Nullable Action getAction(@NotNull String name) {
        return actionByName.get(name.toUpperCase(Locale.ROOT));
    }

    public @Nullable Flag getFlag(@NotNull String name) {
        return flagByName.get(name.toUpperCase(Locale.ROOT));
    }
}
