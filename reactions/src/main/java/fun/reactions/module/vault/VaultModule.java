package fun.reactions.module.vault;

import fun.reactions.ReActions;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.module.Module;
import fun.reactions.module.vault.actions.GroupAddAction;
import fun.reactions.module.vault.actions.GroupRemoveAction;
import fun.reactions.module.vault.actions.MoneyGiveAction;
import fun.reactions.module.vault.actions.MoneyTakeAction;
import fun.reactions.module.vault.external.RaVault;
import fun.reactions.module.vault.flags.GroupFlag;
import fun.reactions.module.vault.placeholders.MoneyPlaceholder;
import fun.reactions.module.vault.selectors.GroupSelector;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.selectors.Selector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class VaultModule implements Module {
    @Override
    public @NotNull Collection<String> requiredPlugins() {
        return List.of("Vault");
    }

    @Override
    public void preRegister(@NotNull ReActions.Platform platform) {
        RaVault.init();
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return List.of("fromgate", "MaxDikiy", "imDaniX");
    }

    @Override
    public @NotNull Collection<Action> getActions() {
        return List.of(
                new MoneyTakeAction(),
                new MoneyGiveAction(),
                new GroupAddAction(),
                new GroupRemoveAction()
        );
    }

    @Override
    public @NotNull Collection<Flag> getFlags() {
        return List.of(new GroupFlag());
    }

    @Override
    public @NotNull Collection<Placeholder> getPlaceholders() {
        return List.of(new MoneyPlaceholder());
    }

    @Override
    public @NotNull Collection<Selector> getSelectors() {
        return List.of(new GroupSelector());
    }

    @Override
    public @NotNull String getName() {
        return "Vault";
    }
}
