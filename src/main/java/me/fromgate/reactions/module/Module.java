package me.fromgate.reactions.module;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.ActivatorType;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.selectors.Selector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public interface Module {
    @NotNull String getName();

    @NotNull String @NotNull [] getAuthors();

    default @NotNull Collection<ActivatorType> getActivatorTypes(ReActions.Platform platform) {
        return Collections.emptyList();
    }

    default @NotNull Collection<Action> getActions(ReActions.Platform platform) {
        return Collections.emptyList();
    }

    default @NotNull Collection<Flag> getFlags(ReActions.Platform platform) {
        return Collections.emptyList();
    }

    default @NotNull Collection<Placeholder> getPlaceholders(ReActions.Platform platform) {
        return Collections.emptyList();
    }

    default @NotNull Collection<Selector> getSelectors(ReActions.Platform platform) {
        return Collections.emptyList();
    }
}
