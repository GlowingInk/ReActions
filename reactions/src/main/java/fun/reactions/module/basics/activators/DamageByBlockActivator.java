package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.Locatable;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.Utils;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

/**
 * @author MaxDikiy
 * @since 23/07/2017
 */
// TODO: Assemble to one activator
public class DamageByBlockActivator extends Activator implements Locatable {

    private final Material blockType;
    private final String blockLocation;
    private final String damageCause;

    private DamageByBlockActivator(Logic base, String block, String location, String cause) {
        super(base);
        this.blockType = ItemUtils.getMaterial(block.startsWith("type:") ? block.substring(5) : block);
        this.blockLocation = location;
        this.damageCause = cause;
    }

    public static DamageByBlockActivator create(Logic base, Parameters param) {
        String block = param.getString("block", "");
        String location = param.getString("loc", "");
        String cause = param.getString("cause", "ANY");
        return new DamageByBlockActivator(base, block, location, cause);
    }

    public static DamageByBlockActivator load(Logic base, ConfigurationSection cfg) {
        String block = cfg.getString("block", "");
        String location = cfg.getString("loc", "");
        String cause = cfg.getString("cause", "ANY");
        return new DamageByBlockActivator(base, block, location, cause);

    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context db = (Context) context;
        Block damagerBlock = db.blockDamager;
        if (damagerBlock == null) return false;
        if (!isActivatorBlock(damagerBlock)) return false;
        return damageCauseCheck(db.cause);
    }

    private boolean checkLocations(Block block) {
        if (this.blockLocation.isEmpty()) return true;
        return this.isLocatedAt(block.getLocation());
    }

    private boolean isActivatorBlock(Block block) {
        if (blockType != null && block.getType() != blockType) return false;
        return checkLocations(block);
    }

    public boolean isLocatedAt(Location l) {
        if (this.blockLocation.isEmpty()) return false;
        Location loc = LocationUtils.parseLocation(this.blockLocation, null);
        if (loc == null) return false;
        return l.getWorld().equals(loc.getWorld()) &&
                l.getBlockX() == loc.getBlockX() &&
                l.getBlockY() == loc.getBlockY() &&
                l.getBlockZ() == loc.getBlockZ();
    }

    @Override
    public boolean isLocatedAt(@NotNull World world, int x, int y, int z) {
        return isLocatedAt(new Location(world, x, y, z));
    }

    private boolean damageCauseCheck(EntityDamageEvent.DamageCause dc) {
        if (damageCause.equals("ANY")) return true;
        return dc.name().equals(damageCause);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("block", blockType.name());
        cfg.set("location", Utils.isStringEmpty(blockLocation) ? null : this.blockLocation);
        cfg.set("cause", this.damageCause);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "block:" + (blockType == null ? "-" : blockType.name()) +
                "; loc:" + (blockLocation.isEmpty() ? "-" : blockLocation) +
                "; cause:" + damageCause +
                ")";
        return sb;
    }

    /**
     * @author MaxDikiy
     * @since 23/07/2017
     */
    public static class Context extends DamageActivator.Context {

        private final Block blockDamager;

        public Context(Player player, Block blockDamager, EntityDamageEvent.DamageCause cause, double damage, double finalDamage) {
            super(player, cause, "BLOCK", damage, finalDamage);
            this.blockDamager = blockDamager;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return DamageByBlockActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            Map<String, Variable> vars = super.prepareVariables();
            vars.put("blocklocation", simple(LocationUtils.locationToString(blockDamager.getLocation())));
            vars.put("blocktype", Variable.simple(blockDamager.getType()));
            vars.put("block", Variable.simple(blockDamager.getType())); // FIXME Why there is a copy?
            return vars;
        }
    }
}
