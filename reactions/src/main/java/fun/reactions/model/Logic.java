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

package fun.reactions.model;

import fun.reactions.Cfg;
import fun.reactions.ReActions;
import fun.reactions.model.activity.ActivitiesRegistry;
import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.actions.Interrupting;
import fun.reactions.model.activity.actions.InvalidAction;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.activity.flags.InvalidFlag;
import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.PlaceholdersManager;
import fun.reactions.util.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Logic {
    private final ReActions.Platform platform;
    private final String name;
    private final String type;
    private final List<Flag.Stored> flags;
    private final List<Action.Stored> actions;
    private final List<Action.Stored> reactions;

    private String group; // TODO Should not be here?

    public Logic(@NotNull ReActions.Platform platform, @NotNull String type, @NotNull String name) {
        this.platform = platform;
        this.type = type;
        this.name = name;
        this.group = "activators";
        this.flags = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.reactions = new ArrayList<>();
    }

    public @NotNull String getType() {
        return type;
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @NotNull String getGroup() {
        return this.group;
    }

    public @NotNull List<Flag.Stored> getFlags() {
        return this.flags;
    }

    public @NotNull List<Action.Stored> getActions() {
        return this.actions;
    }

    public @NotNull List<Action.Stored> getReactions() {
        return this.reactions;
    }

    public @NotNull ReActions.Platform getPlatform() {
        return platform;
    }

    public void setGroup(@NotNull String group) {
        this.group = Utils.isStringEmpty(group) ? "activators" : group;
    }

    public void execute(@NotNull Environment env) {
        PlaceholdersManager placeholders = env.getPlatform().getPlaceholders();
        for (Flag.Stored flag : flags) {
            String params = flag.hasPlaceholders()
                    ? placeholders.parse(env, flag.getContent())
                    : flag.getContent();
            if (!flag.getActivity().proceed(env, params)) {
                executeActions(env, reactions);
                return;
            }
        }
        executeActions(env, actions);
    }

    public static void executeActions(Environment env, List<Action.Stored> actions) {
        PlaceholdersManager placeholders = env.getPlatform().getPlaceholders();
        for (int i = 0; i < actions.size(); i++) {
            Action.Stored action = actions.get(i);
            String params = action.hasPlaceholders()
                    ? placeholders.parse(env, action.getContent())
                    : action.getContent();
            if (action.getActivity().proceed(env, params) && action.getActivity() instanceof Interrupting stopAction) {
                stopAction.stop(env, action.getContent(), new ArrayList<>(actions.subList(i + 1, actions.size())));
                break;
            }
        }
    }

    /**
     * Add flag to activator
     *
     * @param flag Flag to add
     * @param param Parameters of flag
     * @param inverted Is indentation needed
     */
    public void addFlag(@NotNull Flag flag, @NotNull String param, boolean inverted) {
        flags.add(new Flag.Stored(flag, param, inverted));
    }

    /**
     * Remove flag from activator
     *
     * @param index Index of flag
     * @return Is there flag with this index
     */
    public boolean removeFlag(int index) {
        if (flags.size() <= index) return false;
        flags.remove(index);
        return true;
    }

    /**
     * Add action to activator
     *
     * @param action Action to add
     * @param param  Parameters of action
     */
    public void addAction(@NotNull Action action, @NotNull String param) {
        actions.add(new Action.Stored(action, param));
    }

    /**
     * Remove action from activator
     *
     * @param index Index of action
     * @return Is there action with this index
     */
    public boolean removeAction(int index) {
        if (actions.size() <= index) return false;
        actions.remove(index);
        return true;
    }

    /**
     * Add reaction to activator
     *
     * @param action Action to add
     * @param param  Parameters of action
     */
    public void addReaction(@NotNull Action action, @NotNull String param) {
        reactions.add(new Action.Stored(action, param));
    }

    /**
     * Remove reaction from activator
     *
     * @param index Index of action
     * @return Is there action with this index
     */
    public boolean removeReaction(int index) {
        if (reactions.size() <= index) return false;
        reactions.remove(index);
        return true;
    }

    /**
     * Clear flags of activator
     */
    public void clearFlags() {
        flags.clear();
    }

    /**
     * Clear actions of activator
     */
    public void clearActions() {
        actions.clear();
    }

    /**
     * Clear reactions of activator
     */
    public void clearReactions() {
        reactions.clear();
    }

    public void load(@NotNull ConfigurationSection cfg) {
        ActivitiesRegistry activities = platform.getActivities();
        for (String flagStr : cfg.getStringList("flags")) {
            loadActivity(flagStr, activities::storedFlagOf, f -> f instanceof InvalidFlag, flags);
        }
        for (String actionStr : cfg.getStringList("actions")) {
            loadActivity(actionStr, activities::storedActionOf, a -> a instanceof InvalidAction, actions);
        }
        for (String reactionsStr : cfg.getStringList("reactions")) {
            loadActivity(reactionsStr, activities::storedActionOf, a -> a instanceof InvalidAction, reactions);
        }
    }

    private <A extends Activity, S extends Activity.Stored<A>> void loadActivity(
            String value, Function<String, S> reader, Predicate<A> isDummy, List<S> list
    ) {
        S stored = reader.apply(value);
        if (isDummy.test(stored.getActivity())) {
            platform.logger().warn("Activator '" + name + "' loaded unknown activity '" + stored.getActivity().getName() + "'.");
        }
        list.add(stored);
    }

    /**
     * Save activator to config
     *
     * @param cfg Config for activator
     */
    public void save(@NotNull ConfigurationSection cfg) {
        List<String> flg = new ArrayList<>();
        for (Flag.Stored f : flags) flg.add(f.toString());
        cfg.set("flags", flg.isEmpty() && !Cfg.saveEmptySections ? null : flg);
        flg = new ArrayList<>();
        for (Action.Stored a : actions) flg.add(a.toString());
        cfg.set("actions", flg.isEmpty() && !Cfg.saveEmptySections ? null : flg);
        flg = new ArrayList<>();
        for (Action.Stored a : reactions) flg.add(a.toString());
        cfg.set("reactions", flg.isEmpty() && !Cfg.saveEmptySections ? null : flg);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Logic other && other.name.equalsIgnoreCase(name));
    }

    @Override
    public int hashCode() {
        return name.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder();
        if (!getFlags().isEmpty()) sb.append(" F:").append(getFlags().size());
        if (!getActions().isEmpty()) sb.append(" A:").append(getActions().size());
        if (!getReactions().isEmpty()) sb.append(" R:").append(getReactions().size());
        return sb.toString();
    }
}
