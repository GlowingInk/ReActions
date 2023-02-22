package fun.reactions.module.worldedit;

import fun.reactions.ReActions;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.module.Module;
import fun.reactions.module.worldedit.actions.WeSuperPickaxeAction;
import fun.reactions.module.worldedit.actions.WeToolControlAction;
import fun.reactions.module.worldedit.activators.WeChangeActivator;
import fun.reactions.module.worldedit.activators.WeSelectionActivator;
import fun.reactions.module.worldedit.external.RaWorldEdit;
import fun.reactions.module.worldedit.flags.WeSelectionFlag;
import fun.reactions.module.worldedit.flags.WeSuperPickaxeFlag;
import fun.reactions.module.worldedit.flags.WeToolControlFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static fun.reactions.model.activators.type.ActivatorTypesRegistry.typeOf;

public class WorldEditModule implements Module { // TODO: Actions with WorldEdit API - placing blocks, working with schematics
    @Override
    public @NotNull Collection<String> requiredPlugins() {
        return List.of("WorldEdit");
    }

    @Override
    public void preRegister(ReActions.@NotNull Platform platform) {
        RaWorldEdit.init();
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return List.of("fromgate", "MaxDikiy", "imDaniX");
    }

    @Override
    public @NotNull Collection<ActivatorType> getActivatorTypes() {
        return List.of(
                typeOf(WeSelectionActivator.class, "WE_SELECTION", WeSelectionActivator::create, WeSelectionActivator::load),
                typeOf(WeChangeActivator.class, "WE_CHANGE", WeChangeActivator::create, WeChangeActivator::load)
        );
    }

    @Override
    public @NotNull Collection<Action> getActions() {
        return List.of(
                new WeToolControlAction(),
                new WeSuperPickaxeAction()
        );
    }

    @Override
    public @NotNull Collection<Flag> getFlags() {
        return List.of(
                new WeSelectionFlag(),
                new WeSuperPickaxeFlag(),
                new WeToolControlFlag()
        );
    }

    @Override
    public @NotNull String getName() {
        return "WorldEdit";
    }
}
