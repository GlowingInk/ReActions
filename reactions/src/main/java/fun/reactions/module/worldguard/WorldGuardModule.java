package fun.reactions.module.worldguard;

import fun.reactions.ReActions;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.module.Module;
import fun.reactions.module.worldguard.actions.BlockFillAction;
import fun.reactions.module.worldguard.actions.ClearRegionAction;
import fun.reactions.module.worldguard.activators.RegionActivator;
import fun.reactions.module.worldguard.activators.RegionEnterActivator;
import fun.reactions.module.worldguard.activators.RegionLeaveActivator;
import fun.reactions.module.worldguard.external.RaWorldGuard;
import fun.reactions.module.worldguard.flags.RegionFlags;
import fun.reactions.module.worldguard.flags.RegionInRadiusFlag;
import fun.reactions.module.worldguard.selectors.RegionSelector;
import fun.reactions.selectors.Selector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static fun.reactions.model.activators.type.ActivatorTypesRegistry.typeOf;

public class WorldGuardModule implements Module {
    @Override
    public @NotNull Collection<String> requiredPlugins() {
        return List.of("WorldGuard");
    }

    @Override
    public void preRegister(@NotNull ReActions.Platform platform) {
        RaWorldGuard.init();
    }

    @Override
    public void postRegister(@NotNull ReActions.Platform platform) {
        platform.getServer().getScheduler().runTask(platform.getPlugin(), RaWorldGuard::updateRegionCache);
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return List.of("fromgate", "MaxDikiy", "imDaniX");
    }

    @Override
    public @NotNull Collection<ActivatorType> getActivatorTypes() {
        return List.of(
                typeOf(RegionActivator.class, "REGION", RegionActivator::create, RegionActivator::load),
                typeOf(RegionEnterActivator.class, "REGION_ENTER", RegionEnterActivator::create, RegionEnterActivator::load),
                typeOf(RegionLeaveActivator.class, "REGION_LEAVE", RegionLeaveActivator::create, RegionLeaveActivator::load)
        );
    }

    @Override
    public @NotNull Collection<Action> getActions() {
        return List.of(
                new ClearRegionAction(),
                new BlockFillAction()
        );
    }

    @Override
    public @NotNull Collection<Flag> getFlags() {
        return List.of(
                new RegionFlags(RegionFlags.Type.REGION),
                new RegionFlags(RegionFlags.Type.REGION_PLAYERS),
                new RegionFlags(RegionFlags.Type.REGION_MEMBER),
                new RegionFlags(RegionFlags.Type.REGION_OWNER),
                new RegionFlags(RegionFlags.Type.REGION_STATE),
                new RegionInRadiusFlag()
        );
    }

    @Override
    public @NotNull Collection<Selector> getSelectors() {
        return List.of(new RegionSelector());
    }

    @Override
    public @NotNull String getName() {
        return "worldguard";
    }
}
