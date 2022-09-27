/*
  Created by MaxDikiy on 17/10/2017.
 */
package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.externals.worldguard.RaWorldGuard;
import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.WeChangeStorage;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

@Aliases("WECHANGE")
public class WEChangeActivator extends Activator {

    private final Material blockType;
    private final String region;

    private WEChangeActivator(ActivatorLogic base, Material blockType, String region) {
        super(base);
        this.blockType = blockType;
        this.region = region;
    }

    public static WEChangeActivator create(ActivatorLogic base, Parameters param) {
        Material blockType = ItemUtils.getMaterial(param.getString("blocktype"));
        String region = param.getString("region", "");
        return new WEChangeActivator(base, blockType, region);
    }

    public static WEChangeActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        Material blockType = ItemUtils.getMaterial(cfg.getString("block-type"));
        String region = cfg.getString("region", "");
        return new WEChangeActivator(base, blockType, region);
    }

    @Override
    public boolean checkStorage(Storage event) {
        WeChangeStorage e = (WeChangeStorage) event;
        if (!checkBlockType(e.getBlockType())) return false;
        return region.isEmpty() || RaWorldGuard.isLocationInRegion(e.getLocation(), region);
    }

    private boolean checkBlockType(Material check) {
        return blockType == null || blockType == check;
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("block-type", blockType == null ? null : blockType.name());
        cfg.set("region", region);
    }

    @Override
    public boolean isValid() {
        return true;
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
