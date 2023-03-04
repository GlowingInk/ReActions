package fun.reactions.util;

import fun.reactions.util.message.Msg;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public final class FileUtils {
    private FileUtils() {}

    public static boolean loadCfg(YamlConfiguration cfg, File f, String error) {
        if (cfg == null) return false;
        try {
            if (!createFile(f, error)) return false;
            cfg.load(f);
            return true;
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException e) {
            Msg.logMessage(error);
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveCfg(YamlConfiguration cfg, File f, String error) {
        if (cfg == null) return false;
        try {
            if (recreateFile(f, error)) {
                cfg.save(f);
                return true;
            }
            return false;
        } catch (IOException | IllegalArgumentException e) {
            Msg.logMessage(error);
            e.printStackTrace();
            return false;
        }
    }

    private static boolean createFile(File f, String error) {
        if (f == null) return false;
        try {
            return f.exists() || f.createNewFile();
        } catch (IOException e) {
            Msg.logMessage(error);
            e.printStackTrace();
            return false;
        }
    }

    private static boolean recreateFile(File f, String error) {
        if (f == null) return false;
        try {
            return (!f.exists() || f.delete()) && f.createNewFile();
        } catch (IOException e) {
            Msg.logMessage(error);
            e.printStackTrace();
            return false;
        }
    }
}
