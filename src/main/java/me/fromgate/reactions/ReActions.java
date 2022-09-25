package me.fromgate.reactions;

import me.fromgate.reactions.logic.activators.ActivatorTypesRegistry;
import me.fromgate.reactions.logic.activators.ActivatorsManager;
import me.fromgate.reactions.logic.activity.ActivitiesRegistry;
import me.fromgate.reactions.module.ModulesRegistry;
import me.fromgate.reactions.placeholders.PlaceholdersManager;
import me.fromgate.reactions.selectors.SelectorsManager;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import java.util.Objects;

// TODO: This is a temporary solution, will be removed after 0.14.0
public final class ReActions {
    private static Platform platform;

    private ReActions() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static void setPlatform(Platform platform) {
        if (ReActions.platform != null) {
            throw new IllegalStateException("Platform is already assigned.");
        }
        Objects.requireNonNull(platform, "Platform cannot be null.");
        Objects.requireNonNull(platform.getActivators(), "ActivatorsManager cannot be null.");
        Objects.requireNonNull(platform.getPlaceholders(), "PlaceholdersManager cannot be null.");
        Objects.requireNonNull(platform.getVariables(), "VariablesManager cannot be null.");
        Objects.requireNonNull(platform.getSelectors(), "SelectorsManager cannot be null.");
        Objects.requireNonNull(platform.getModules(), "ModulesManager cannot be null.");
        Objects.requireNonNull(platform.getPlugin(), "Plugin cannot be null.");
        ReActions.platform = platform;
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

    public static PlaceholdersManager getPlaceholders() {
        return platform.getPlaceholders();
    }

    public static VariablesManager getVariables() {
        return platform.getVariables();
    }

    public static SelectorsManager getSelectors() {
        return platform.getSelectors();
    }

    public static ModulesRegistry getModules() {
        return platform.getModules();
    }

    public static Logger getLogger() {
        return platform.logger();
    }

    public static Plugin getPlugin() {
        return platform.getPlugin();
    }

    public interface Platform {
        ActivatorTypesRegistry getActivatorTypes();
        ActivatorsManager getActivators();
        ActivitiesRegistry getActivities();
        PlaceholdersManager getPlaceholders();
        VariablesManager getVariables();
        SelectorsManager getSelectors();
        ModulesRegistry getModules();
        Logger logger();
        Plugin getPlugin();
    }
}
