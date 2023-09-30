package fun.reactions.model.activity;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.actions.InvalidAction;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.activity.flags.InvalidFlag;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ActivitiesRegistry {
    private final Map<String, Action> actionByName;
    private final Map<String, Flag> flagByName;
    private final Set<String> flagNames;

    public ActivitiesRegistry() {
        actionByName = new HashMap<>();
        flagByName = new HashMap<>();
        flagNames = new HashSet<>();
    }

    public void registerAction(@NotNull Action action) {
        String upperAction = action.getName().toUpperCase(Locale.ROOT);
        if (actionByName.containsKey(upperAction)) {
            throw new IllegalStateException("Action '" + upperAction + "' is already registered!");
        }
        actionByName.put(upperAction, action);
        for (String alias : Aliased.getAliasesOf(action)) {
            actionByName.putIfAbsent(alias.toUpperCase(Locale.ROOT), action);
        }
    }

    public void registerFlag(@NotNull Flag flag) {
        String upperFlag = flag.getName().toUpperCase(Locale.ROOT);
        if (flagByName.containsKey(upperFlag)) {
            throw new IllegalStateException("Flag '" + upperFlag + "' is already registered!");
        }
        flagByName.put(upperFlag, flag);
        flagNames.add(upperFlag);
        flagNames.add("!" + upperFlag);
        for (String alias : Aliased.getAliasesOf(flag)) {
            String upperAlias = alias.toUpperCase(Locale.ROOT);
            flagByName.putIfAbsent(upperAlias, flag);
            flagNames.add(upperAlias);
            flagNames.add("!" + upperAlias);
        }
    }

    public @Nullable Action getAction(@NotNull String name) {
        return actionByName.get(name.toUpperCase(Locale.ROOT));
    }

    public @Nullable Flag getFlag(@NotNull String name) {
        return flagByName.get(name.toUpperCase(Locale.ROOT));
    }

    public @NotNull @UnmodifiableView Collection<@NotNull String> getActionsTypesNames() {
        return Collections.unmodifiableCollection(actionByName.keySet());
    }

    public @NotNull @UnmodifiableView Collection<@NotNull String> getFlagsTypesNames() {
        return Collections.unmodifiableCollection(flagNames);
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
