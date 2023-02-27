package fun.reactions;

import fun.reactions.commands.user.UserCommandsManager;
import fun.reactions.model.activators.ActivatorsManager;
import fun.reactions.model.activators.type.ActivatorTypesRegistry;
import fun.reactions.model.activity.ActivitiesRegistry;
import fun.reactions.module.ModulesRegistry;
import fun.reactions.placeholders.PlaceholdersManager;
import fun.reactions.selectors.SelectorsManager;
import fun.reactions.time.wait.WaitingManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

// TODO: This is a temporary solution, has to be removed
public final class ReActions {
    private static Platform platform;

    private ReActions() {}

    public static void setPlatform(@NotNull Platform platform) {
        if (ReActions.platform != null) {
            throw new IllegalStateException("Platform is already assigned.");
        }
        ReActions.platform = platform;
    }

    public static Platform getPlatform() {
        return platform;
    }

    public static ActivatorTypesRegistry getActivatorTypes() {
        return platform.getActivatorTypes();
    }

    public static ActivatorsManager getActivators() {
        return platform.getActivators();
    }

    public static ActivitiesRegistry getActivities() {
        return platform.getActivities();
    }

    public static UserCommandsManager getCommands() {
        return platform.getCommands();
    }

    public static PlaceholdersManager getPlaceholders() {
        return platform.getPlaceholders();
    }

    public static PersistentVariablesManager getVariables() {
        return platform.getVariables();
    }

    public static SelectorsManager getSelectors() {
        return platform.getSelectors();
    }

    public static WaitingManager getWaiter() {
        return platform.getWaiter();
    }

    public static ComponentLogger getLogger() {
        return platform.logger();
    }

    public static Plugin getPlugin() {
        return platform.getPlugin();
    }

    public interface Platform {
        @NotNull ActivatorTypesRegistry getActivatorTypes();
        @NotNull ActivatorsManager getActivators();
        @NotNull ActivitiesRegistry getActivities();
        @NotNull UserCommandsManager getCommands();
        @NotNull PlaceholdersManager getPlaceholders();
        @NotNull PersistentVariablesManager getVariables();
        @NotNull SelectorsManager getSelectors();
        @NotNull WaitingManager getWaiter();
        @NotNull ModulesRegistry getModules();
        @NotNull ComponentLogger logger();
        @NotNull Plugin getPlugin();
        @NotNull File getDataFolder();
        @NotNull Server getServer();
    }
}
