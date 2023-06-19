/*
  Created by MaxDikiy on 17/10/2017.
 */
package fun.reactions.module.worldedit.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.worldguard.external.RaWorldGuard;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

@Aliased.Names("WECHANGE")
public class WeChangeActivator extends Activator {

    private final Material blockType;
    private final String region;

    private WeChangeActivator(Logic base, Material blockType, String region) {
        super(base);
        this.blockType = blockType;
        this.region = region;
    }

    public static WeChangeActivator create(Logic base, Parameters param) {
        Material blockType = ItemUtils.getMaterial(param.getString("blocktype"));
        String region = param.getString("region", "");
        return new WeChangeActivator(base, blockType, region);
    }

    public static WeChangeActivator load(Logic base, ConfigurationSection cfg) {
        Material blockType = ItemUtils.getMaterial(cfg.getString("block-type", ""));
        String region = cfg.getString("region", "");
        return new WeChangeActivator(base, blockType, region);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context e = (Context) context;
        if (!checkBlockType(e.getBlockType())) return false;
        return region.isEmpty() || RaWorldGuard.isLocationInRegion(e.getLocation(), region);
    }

    private boolean checkBlockType(Material check) {
        return blockType == null || blockType == check;
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("block-type", blockType == null ? null : blockType.name());
        cfg.set("region", region);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "block-type:" + (blockType != null ? blockType : "ANY") +
                " region:" + (region.isEmpty() ? "-" : region.toUpperCase(Locale.ROOT)) +
                ")";
        return sb;
    }

    /**
     * Created by MaxDikiy on 17/10/2017.
     */
    public static class Context extends ActivationContext {

        private final Location location;
        private final Material blockType;

        public Context(Player player, Location location, Material blockType) {
            super(player);
            this.location = location;
            this.blockType = blockType;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return WeChangeActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of(
                    CANCEL_EVENT, Variable.property(false),
                    "blocktype", Variable.simple(blockType),
                    "blocklocation", simple(LocationUtils.locationToString(location))
            );
        }

        public Location getLocation() {
            return this.location;
        }

        public Material getBlockType() {
            return this.blockType;
        }
    }
}
