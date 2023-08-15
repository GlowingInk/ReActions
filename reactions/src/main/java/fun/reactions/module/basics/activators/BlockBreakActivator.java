package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.Locatable;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.location.ImplicitPosition;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.model.environment.Variable.property;
import static fun.reactions.model.environment.Variable.simple;

/**
 * Created by MaxDikiy on 2017-05-14.
 */
public class BlockBreakActivator extends Activator implements Locatable {

    private final Material blockType;
    private final ImplicitPosition pos;

    private BlockBreakActivator(Logic base, Material block, ImplicitPosition pos) {
        super(base);
        this.blockType = block;
        this.pos = pos;
    }

    public static BlockBreakActivator create(Logic base, Parameters param) {
        Material block = param.get("block", ItemUtils::getMaterial);
        ImplicitPosition pos = param.get("loc", ImplicitPosition::byString);
        return new BlockBreakActivator(base, block, pos);
    }

    public static BlockBreakActivator load(Logic base, ConfigurationSection cfg) {
        Material block = ItemUtils.getMaterial(cfg.getString("block", ""));
        ImplicitPosition pos;
        if (cfg.isString("location")) {
            pos = ImplicitPosition.byString(cfg.getString("location"));
        } else if (cfg.isString("loc")) {
            pos = ImplicitPosition.byString(cfg.getString("loc"));
        } else {
            pos = ImplicitPosition.fromConfiguration(cfg);
        }
        return new BlockBreakActivator(base, block, pos);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context bbe = (Context) context;
        Block brokenBlock = bbe.block;
        if (brokenBlock == null) return false;
        return isActivatorBlock(brokenBlock);
    }

    private boolean isActivatorBlock(Block block) {
        if (this.blockType != null && blockType != block.getType()) return false;
        return pos.isValidAt(block.getLocation());
    }

    @Override
    public boolean isLocatedAt(@NotNull World world, int x, int y, int z) {
        return pos.isValidAt(world.getName(), x, y, z);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("block", blockType == null ? null : blockType.name());
        pos.intoConfiguration(cfg);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "block:" + (blockType == null ? "-" : blockType) +
                "; loc:" + (pos == ImplicitPosition.EVERYWHERE ? "-" : pos) +
                ")";
        return sb;
    }

    /**
     * Created by MaxDikiy on 2017-05-14.
     */
    public static class Context extends ActivationContext {
        public static final String DO_DROP = "is_drop";

        private final Block block;
        private final boolean dropItems;

        public Context(Player p, Block block, boolean dropItems) {
            super(p);
            this.block = block;
            this.dropItems = dropItems;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return BlockBreakActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return Map.of(
                    CANCEL_EVENT, property(false),
                    DO_DROP, property(dropItems),
                    "blocklocation", simple(LocationUtils.locationToString(block.getLocation())),
                    "blocktype", simple(block.getType()),
                    "block", simple(block.getType()) // FIXME Why there is a copy?
            );
        }
    }
}
