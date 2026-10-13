package fun.reactions.variables;

import fun.reactions.ReActions;
import fun.reactions.util.collections.CollectionUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

final class LegacyVariableImport {
    private static final String LEGACY_GENERAL = "general";

    private LegacyVariableImport() { }

    static boolean importInto(@NotNull ReActions.Platform rea, @NotNull Map<String, Map<String, String>> out) {
        File flatFile = flatFile(rea);
        File perPlayerDir = perPlayerDir(rea);
        if (flatFile.exists()) readFile(rea, flatFile, out);
        if (perPlayerDir.isDirectory()) {
            readFile(rea, new File(perPlayerDir, "general.yml"), out);
            for (File file : CollectionUtils.emptyOnNull(perPlayerDir.listFiles())) {
                if (file.isDirectory() || !file.getName().endsWith(".yml") || file.getName().equals("general.yml")) continue;
                readFile(rea, file, out);
            }
        }
        return !out.isEmpty();
    }

    static void backup(@NotNull ReActions.Platform rea) {
        VariableKeys.backupFile(rea, flatFile(rea));
        VariableKeys.backupFile(rea, perPlayerDir(rea));
    }

    private static @NotNull File flatFile(@NotNull ReActions.Platform rea) {
        return new File(rea.getDataFolder(), "variables.yml");
    }

    private static @NotNull File perPlayerDir(@NotNull ReActions.Platform rea) {
        return new File(rea.getDataFolder(), "variables");
    }

    private static void readFile(@NotNull ReActions.Platform rea, @NotNull File file, @NotNull Map<String, Map<String, String>> out) {
        if (!file.exists()) return;
        try {
            YamlConfiguration cfg = new YamlConfiguration();
            cfg.load(file);
            for (String key : cfg.getKeys(true)) {
                if (cfg.isString(key)) putRemapped(out, key, cfg.getString(key, ""));
            }
        } catch (Exception ex) {
            rea.logger().error("Failed to read legacy variables file {}", file, ex);
        }
    }

    private static void putRemapped(@NotNull Map<String, Map<String, String>> out, @NotNull String key, @NotNull String value) {
        int dot = key.indexOf('.');
        String owner = dot == -1 ? key : key.substring(0, dot);
        String var = dot == -1 ? "" : key.substring(dot + 1);
        String newOwner = owner.equals(LEGACY_GENERAL) ? VariableKeys.GENERAL : owner;
        out.computeIfAbsent(VariableKeys.normalize(newOwner), o -> new HashMap<>()).put(VariableKeys.normalize(var), value);
    }
}
