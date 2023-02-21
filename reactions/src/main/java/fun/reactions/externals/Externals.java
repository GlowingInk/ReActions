package fun.reactions.externals;

import fun.reactions.externals.placeholderapi.RaPlaceholderAPI;
import fun.reactions.externals.worldedit.RaWorldEdit;
import fun.reactions.externals.worldguard.RaWorldGuard;
import org.bukkit.Bukkit;

@Deprecated
public class Externals {
    public static void init() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                RaPlaceholderAPI.init();
            } catch (Throwable ignore) {
            }
        }


        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            try {
                RaProtocolLib.connectProtocolLib();
            } catch (Throwable ignore) {
            }
        }

        // TODO: Actions with WorldEdit API - placing blocks, working with schematics
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            try {
                RaWorldEdit.init();
            } catch (Throwable ignore) {
            }
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                RaWorldGuard.init();
            } catch (Throwable ignore) {
            }
        }

        // TODO: Essentials support (homes, warps, native god check)
    }
}
