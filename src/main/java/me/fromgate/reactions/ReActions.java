package me.fromgate.reactions;

import lombok.experimental.UtilityClass;
import me.fromgate.reactions.logic.activators.ActivatorsManager;
import me.fromgate.reactions.logic.activity.ActivitiesRegistry;
import me.fromgate.reactions.placeholders.PlaceholdersManager;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

@UtilityClass
// TODO: This is a temporary solution - makes everything to be in one place rather than one-by-one singletones
// Probably will eventually be replaced with Guise
public class ReActions {
    private Platform platform;

    public void setPlatform(Platform platform) {
        if (ReActions.platform != null) {
            throw new IllegalStateException("Platform is already assigned.");
        }
        Objects.requireNonNull(platform, "Platform cannot be null.");
        Objects.requireNonNull(platform.getActivators(), "ActivatorsManager cannot be null.");
        Objects.requireNonNull(platform.getPlaceholders(), "PlaceholdersManager cannot be null.");
        Objects.requireNonNull(platform.getVariables(), "VariablesManager cannot be null.");
        Objects.requireNonNull(platform.getPlugin(), "Plugin cannot be null.");
        ReActions.platform = platform;
    }

    public ActivatorsManager getActivators() {
        return platform.getActivators();
    }

    public ActivitiesRegistry getActivities() {
        return platform.getActivities();
    }

    public PlaceholdersManager getPlaceholders() {
        return platform.getPlaceholders();
    }

    public VariablesManager getVariables() {
        return platform.getVariables();
    }

    public Plugin getPlugin() {
        return platform.getPlugin();
    }

    public interface Platform {
        ActivatorsManager getActivators();
        ActivitiesRegistry getActivities();
        PlaceholdersManager getPlaceholders();
        VariablesManager getVariables();
        Plugin getPlugin();
        // TODO: Selectors
    }
}
