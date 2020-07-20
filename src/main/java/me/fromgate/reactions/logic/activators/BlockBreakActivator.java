package me.fromgate.reactions.logic.activators;

import me.fromgate.reactions.logic.storages.BlockBreakStorage;
import me.fromgate.reactions.logic.storages.Storage;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.item.ItemUtil;
import me.fromgate.reactions.util.location.LocationUtil;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by MaxDikiy on 2017-05-14.
 */
public class BlockBreakActivator extends Activator implements Locatable {
    private final Material blockType;
    // TODO: VirtualLocation
    private final String blockLocation;

    private BlockBreakActivator(ActivatorBase base, Material block, String location) {
        super(base);
        this.blockType = block;
        this.blockLocation = location;
    }

    public static BlockBreakActivator create(ActivatorBase base, Parameters param) {
        Material block = ItemUtil.getMaterial(param.getParam("block"));
        String loc = param.getParam("loc");
        return new BlockBreakActivator(base, block, loc);
    }

    public static BlockBreakActivator load(ActivatorBase base, ConfigurationSection cfg) {
        Material block = ItemUtil.getMaterial(cfg.getString("block"));
        String loc = cfg.getString("loc");
        return new BlockBreakActivator(base, block, loc);
    }

    @Override
    public boolean activate(Storage event) {
        BlockBreakStorage bbe = (BlockBreakStorage) event;
        Block brokenBlock = bbe.getBlock();
        if (brokenBlock == null) return false;
        return isActivatorBlock(brokenBlock);
    }

    private boolean isActivatorBlock(Block block) {
        if (this.blockType != null && blockType != block.getType()) return false;
        if (Util.isStringEmpty(blockLocation)) return true;
        return this.isLocatedAt(block.getLocation());
    }

    @Override
    public boolean isLocatedAt(Location l) {
        if (Util.isStringEmpty(blockLocation)) return false;
        Location loc = LocationUtil.parseLocation(this.blockLocation, null);
        if (loc == null) return false;
        return l.getWorld().equals(loc.getWorld()) &&
                l.getBlockX() == loc.getBlockX() &&
                l.getBlockY() == loc.getBlockY() &&
                l.getBlockZ() == loc.getBlockZ();
    }

    @Override
    public boolean isLocatedAt(World world, int x, int y, int z) {
        return isLocatedAt(new Location(world, x, y, z));
    }

    @Override
    public void save(ConfigurationSection cfg) {
        if (blockType != null) cfg.set("block", this.blockType.name());
        cfg.set("location", Util.isStringEmpty(blockLocation) ? null : this.blockLocation);
    }

    @Override
    public ActivatorType getType() {
        return ActivatorType.BLOCK_BREAK;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" (");
        sb.append("block:").append(blockType == null ? "-" : blockType);
        sb.append("; loc:").append(blockLocation.isEmpty() ? "-" : blockLocation);
        sb.append(")");
        return sb.toString();
    }
}