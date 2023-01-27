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

package me.fromgate.reactions.logic;

import me.fromgate.reactions.Cfg;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.ActivitiesRegistry;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.Stopper;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.logic.activity.flags.StoredFlag;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

public final class ActivatorLogic {
    private String group; // TODO Should not be there
    private final String name;
    private final String type;
    private final List<StoredFlag> flags;
    private final List<StoredAction> actions;
    private final List<StoredAction> reactions;

    public ActivatorLogic(@NotNull String type, @NotNull String name, @Nullable String group) {
        this.type = type;
        this.name = name;
        this.group = Utils.isStringEmpty(group) ? "activators" : group;
        this.flags = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.reactions = new ArrayList<>();
    }

    public ActivatorLogic(@NotNull String type, @NotNull String name, @Nullable String group, @NotNull ConfigurationSection cfg, @NotNull ActivitiesRegistry activity) {
        this(type, name, group);
        loadData(cfg.getStringList("flags"), (s, v) -> storeFlag(s, v, flags, activity));
        loadData(cfg.getStringList("actions"), (s, v) -> storeAction(s, v, actions, activity));
        loadData(cfg.getStringList("reactions"), (s, v) -> storeAction(s, v, reactions, activity));
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

    private static void loadData(List<String> data, BiConsumer<String, String> loader) {
        for (String str : data) {
            String param = str;
            String value = "";
            int index = str.indexOf("=");
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

    public void executeLogic(@NotNull Environment context) {
        boolean noPlayer = context.getPlayer() == null;
        for (StoredFlag flag : flags) {
            if (flag.getFlag().requiresPlayer() && noPlayer) {
                executeActions(context, reactions, false);
                return;
            }
            String params = flag.hasPlaceholders() ?
                            ReActions.getPlaceholders().parsePlaceholders(context, flag.getParameters()) : // TODO Placeholders DI
                            flag.getParameters();
            if (!flag.getFlag().proceed(context, params)) {
                executeActions(context, reactions, !noPlayer);
                return;
            }
        }
        executeActions(context, actions, !noPlayer);
    }

    private static void executeActions(Environment context, List<StoredAction> actions, boolean hasPlayer) {
        for (int i = 0; i < actions.size(); i++) {
            StoredAction action = actions.get(i);
            // TODO: Microoptimization - check if hasPlayer and separate iteration
            if (hasPlayer || !action.getAction().requiresPlayer()) {
                String params = action.hasPlaceholders() ?
                                ReActions.getPlaceholders().parsePlaceholders(context, action.getParameters()) : // TODO Placeholders DI
                                action.getParameters();
                if (action.getAction().proceed(context, params) && action.getAction() instanceof Stopper stopAction) {
                    stopAction.stop(context, action.getParameters(), new ArrayList<>(actions.subList(i, actions.size())));
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
        return this == obj || (obj instanceof ActivatorLogic logic && logic.name.equalsIgnoreCase(name));
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
