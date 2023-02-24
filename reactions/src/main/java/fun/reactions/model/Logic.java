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
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.actions.Stopper;
import fun.reactions.model.activity.actions.StoredAction;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.activity.flags.StoredFlag;
import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.PlaceholdersManager;
import fun.reactions.util.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

public final class Logic {
    private final ReActions.Platform platform;
    private final String name;
    private final String type;
    private final List<StoredFlag> flags;
    private final List<StoredAction> actions;
    private final List<StoredAction> reactions;

    private String group; // TODO Should not be here?

    public Logic(@NotNull ReActions.Platform platform, @NotNull String type, @NotNull String name, @Nullable String group) {
        this.platform = platform;
        this.type = type;
        this.name = name;
        this.group = Utils.isStringEmpty(group) ? "activators" : group;
        this.flags = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.reactions = new ArrayList<>();
    }

    public Logic(@NotNull ReActions.Platform platform, @NotNull String type, @NotNull String name, @Nullable String group, @NotNull ConfigurationSection cfg) {
        this(platform, type, name, group);
        ActivitiesRegistry activities = platform.getActivities();
        loadData(cfg.getStringList("flags"), (s, v) -> storeFlag(s, v, flags, activities));
        loadData(cfg.getStringList("actions"), (s, v) -> storeAction(s, v, actions, activities));
        loadData(cfg.getStringList("reactions"), (s, v) -> storeAction(s, v, reactions, activities));
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

    public @NotNull List<StoredFlag> getFlags() {
        return this.flags;
    }

    public @NotNull List<StoredAction> getActions() {
        return this.actions;
    }

    public @NotNull List<StoredAction> getReactions() {
        return this.reactions;
    }

    public @NotNull ReActions.Platform getPlatform() {
        return platform;
    }

    private static void loadData(List<String> data, BiConsumer<String, String> loader) {
        for (String str : data) {
            String param = str;
            String value = "";
            int index = str.indexOf('=');
            if (index != -1) {
                param = str.substring(0, index).trim();
                value = str.substring(index + 1);
            }
            loader.accept(param, value);
        }
    }

    private static void storeFlag(String flagStr, String value, List<StoredFlag> flags, ActivitiesRegistry activity) {
        boolean inverted = flagStr.startsWith("!");
        Flag flag = activity.getFlag(inverted ? flagStr.substring(1) : flagStr);
        if (flag == null) {
            // TODO Error
            return;
        }
        flags.add(new StoredFlag(flag, value, inverted));
    }

    private static void storeAction(String actionStr, String value, List<StoredAction> actions, ActivitiesRegistry activity) {
        Action action = activity.getAction(actionStr);
        if (action == null) {
            // TODO Error
            return;
        }
        actions.add(new StoredAction(action, value));
    }

    public void setGroup(@NotNull String group) {
        this.group = group;
    }

    public void execute(@NotNull Environment env) {
        boolean noPlayer = env.getPlayer() == null;
        PlaceholdersManager placeholders = env.getPlatform().getPlaceholders();
        for (StoredFlag flag : flags) {
            if (flag.getActivity().requiresPlayer() && noPlayer) {
                executeActions(env, reactions, false);
                return;
            }
            String params = flag.hasPlaceholders()
                    ? placeholders.parse(env, flag.getParameters())
                    : flag.getParameters();
            if (!flag.getActivity().proceed(env, params)) {
                executeActions(env, reactions, !noPlayer);
                return;
            }
        }
        executeActions(env, actions, !noPlayer);
    }

    public static void executeActions(Environment env, List<StoredAction> actions, boolean hasPlayer) {
        PlaceholdersManager placeholders = env.getPlatform().getPlaceholders();
        for (int i = 0; i < actions.size(); i++) {
            StoredAction action = actions.get(i);
            // TODO: Microoptimization - check if hasPlayer and separate iteration
            if (hasPlayer || !action.getActivity().requiresPlayer()) {
                String params = action.hasPlaceholders()
                        ? placeholders.parse(env, action.getParameters())
                        : action.getParameters();
                if (action.getActivity().proceed(env, params) && action.getActivity() instanceof Stopper stopAction) {
                    stopAction.stop(env, action.getParameters(), new ArrayList<>(actions.subList(i + 1, actions.size())));
                    break;
                }
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
        flags.add(new StoredFlag(flag, param, inverted));
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
        actions.add(new StoredAction(action, param));
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
        reactions.add(new StoredAction(action, param));
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

    /**
     * Save activator to config
     *
     * @param cfg Config for activator
     */
    public void save(@NotNull ConfigurationSection cfg) {
        List<String> flg = new ArrayList<>();
        for (StoredFlag f : flags) flg.add(f.toString());
        cfg.set("flags", flg.isEmpty() && !Cfg.saveEmptySections ? null : flg);
        flg = new ArrayList<>();
        for (StoredAction a : actions) flg.add(a.toString());
        cfg.set("actions", flg.isEmpty() && !Cfg.saveEmptySections ? null : flg);
        flg = new ArrayList<>();
        for (StoredAction a : reactions) flg.add(a.toString());
        cfg.set("reactions", flg.isEmpty() && !Cfg.saveEmptySections ? null : flg);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Logic logic && logic.name.equalsIgnoreCase(name));
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
