package fun.reactions.model.activity;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.actions.InvalidAction;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.activity.flags.InvalidFlag;
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
            throw new IllegalStateException("Action '" + action.getName().toUpperCase(Locale.ROOT) + "' is already registered");
        }
        actionByName.put(action.getName().toUpperCase(Locale.ROOT), action);
        for (String alias : Aliased.getAliasesOf(action)) {
            actionByName.putIfAbsent(alias.toUpperCase(Locale.ROOT), action);
        }
    }

    public void registerFlag(@NotNull Flag flag) {
        if (flagByName.containsKey(flag.getName().toUpperCase(Locale.ROOT))) {
            throw new IllegalStateException("Flag '" + flag.getName().toUpperCase(Locale.ROOT) + "' is already registered");
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

    public @NotNull Action.Stored storedActionOf(@NotNull String str) {
        String[] split = str.split("=", 2);
        split[0] = split[0].trim();
        Action action = getAction(split[0]);
        if (action == null) {
            action = new InvalidAction(split[0]);
        }
        return new Action.Stored(action, split.length > 1 ? split[1] : "");
    }

    public @NotNull Flag.Stored storedFlagOf(@NotNull String str) {
        String[] split = str.split("=", 2);
        split[0] = split[0].trim();
        boolean inverted = split[0].startsWith("!");
        Flag flag = getFlag(inverted ? split[0].substring(1) : split[0]);
        if (flag == null) {
            flag = new InvalidFlag(split[0]);
        }
        return new Flag.Stored(flag, split.length > 1 ? split[1] : "", inverted);
    }
}
