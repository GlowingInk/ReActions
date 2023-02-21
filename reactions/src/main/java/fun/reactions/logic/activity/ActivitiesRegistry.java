package fun.reactions.logic.activity;

import fun.reactions.logic.activity.actions.Action;
import fun.reactions.logic.activity.actions.StoredAction;
import fun.reactions.logic.activity.flags.Flag;
import fun.reactions.logic.activity.flags.StoredFlag;
import fun.reactions.util.naming.Aliased;
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

    public @Nullable StoredAction storedActionOf(@NotNull String str) {
        String[] split = str.split("=", 2);
        if (split.length != 2) return null;
        Action action = getAction(split[0]);
        if (action == null) return null;
        return new StoredAction(action, split[1]);
    }

    public @Nullable StoredFlag storedFlagOf(@NotNull String str) {
        String[] split = str.split("=", 2);
        if (split.length != 2) return null;
        boolean inverted = split[0].startsWith("!");
        Flag flag = getFlag(inverted ? split[0].substring(1) : split[0]);
        if (flag == null) return null;
        return new StoredFlag(flag, split[1], inverted);
    }
}
