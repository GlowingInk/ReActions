/*
  Created by MaxDikiy on 17/10/2017.
 */
package fun.reactions.module.basics.activators;

import fun.reactions.externals.worldguard.RaWorldGuard;
import fun.reactions.logic.Logic;
import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.module.basics.details.WeChangeContext;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Aliased.Names("WECHANGE")
public class WEChangeActivator extends Activator {

    private final Material blockType;
    private final String region;

    private WEChangeActivator(Logic base, Material blockType, String region) {
        super(base);
        this.blockType = blockType;
        this.region = region;
    }

    public static WEChangeActivator create(Logic base, Parameters param) {
        Material blockType = ItemUtils.getMaterial(param.getString("blocktype"));
        String region = param.getString("region", "");
        return new WEChangeActivator(base, blockType, region);
    }

    public static WEChangeActivator load(Logic base, ConfigurationSection cfg) {
        Material blockType = ItemUtils.getMaterial(cfg.getString("block-type", ""));
        String region = cfg.getString("region", "");
        return new WEChangeActivator(base, blockType, region);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        WeChangeContext e = (WeChangeContext) context;
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
}
