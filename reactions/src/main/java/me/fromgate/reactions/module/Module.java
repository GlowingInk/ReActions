package me.fromgate.reactions.module;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.ActivatorType;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.selectors.Selector;
import me.fromgate.reactions.util.naming.Named;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface Module extends Named {

    boolean isPluginDepended();

    default boolean init(@NotNull ReActions.Platform platform) {
        return true;
    }

    @NotNull Collection<String> getAuthors();

    default @NotNull Collection<ActivatorType> getActivatorTypes(@NotNull ReActions.Platform platform) {
        return List.of();
    }

    default @NotNull Collection<Action> getActions(@NotNull ReActions.Platform platform) {
        return List.of();
    }

    default @NotNull Collection<Flag> getFlags(@NotNull ReActions.Platform platform) {
        return List.of();
    }

    default @NotNull Collection<Placeholder> getPlaceholders(@NotNull ReActions.Platform platform) {
        return List.of();
    }

    default @NotNull Collection<Selector> getSelectors(@NotNull ReActions.Platform platform) {
        return List.of();
    }
}
