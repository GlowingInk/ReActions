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

package fun.reactions.variables;

import fun.reactions.ReActions;
import fun.reactions.module.basic.ContextManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static fun.reactions.variables.VariableKeys.normalize;
import static fun.reactions.variables.VariableKeys.ownerOf;

public final class PersistentVariablesManager {
    private volatile Map<String, Map<String, String>> vars = new ConcurrentHashMap<>();
    private final ReActions.Platform rea;
    private final H2VariableStorage storage;

    public PersistentVariablesManager(@NotNull ReActions.Platform rea) {
        this.rea = rea;
        this.storage = new H2VariableStorage(rea, new File(rea.getDataFolder(), "variables"));
    }

    public @Nullable String getVariable(@Nullable String player, @NotNull String var) {
        Map<String, String> ownerVars = vars.get(normalize(ownerOf(player)));
        return ownerVars == null ? null : ownerVars.get(normalize(var));
    }

    public void setVariable(@Nullable String player, @NotNull String var, @NotNull String value) {
        String owner = ownerOf(player);
        String ownerKey = normalize(owner);
        String varKey = normalize(var);
        String[] prev = new String[1];
        vars.compute(ownerKey, (_, ownerVars) -> {
            if (ownerVars == null) ownerVars = new ConcurrentHashMap<>();
            prev[0] = ownerVars.put(varKey, value);
            storage.saveVariable(ownerKey, varKey, value);
            return ownerVars;
        });
        ContextManager.triggerVariable(var, owner, value, prev[0] == null ? "" : prev[0]);
    }

    public boolean removeVariable(@Nullable String player, @NotNull String var) {
        String owner = ownerOf(player);
        String ownerKey = normalize(owner);
        String varKey = normalize(var);
        String[] prev = new String[1];
        vars.computeIfPresent(ownerKey, (_, ownerVars) -> {
            prev[0] = ownerVars.remove(varKey);
            if (prev[0] != null) storage.removeVariable(ownerKey, varKey);
            return ownerVars.isEmpty() ? null : ownerVars;
        });
        if (prev[0] == null) return false;
        ContextManager.triggerVariable(var, owner, "", prev[0]);
        return true;
    }

    public int size() {
        int total = 0;
        for (Map<String, String> ownerVars : vars.values()) total += ownerVars.size();
        return total;
    }

    public void init() {
        if (load() && vars.isEmpty()) migrateLegacy();
    }

    public boolean load() {
        Map<String, Map<String, String>> loaded = new ConcurrentHashMap<>();
        boolean ok = storage.load(loaded);
        vars = loaded;
        return ok;
    }

    public void shutdown() {
        storage.close();
    }

    private void migrateLegacy() {
        Map<String, Map<String, String>> migrated = new HashMap<>();
        if (!LegacyVariableImport.importInto(rea, migrated)) return;

        if (!storage.saveAll(migrated)) return;
        LegacyVariableImport.backup(rea);
        migrated.forEach((owner, ownerVars) -> vars.put(owner, new ConcurrentHashMap<>(ownerVars)));

        int total = 0;
        for (Map<String, String> ownerVars : migrated.values()) total += ownerVars.size();
        rea.logger().info("Migrated {} legacy variable(s) into H2", total);
    }
}
