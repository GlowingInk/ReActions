package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.Locatable;
import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.variables.BlockVariable;
import fun.reactions.util.block.VirtualBlockData;
import fun.reactions.util.location.position.ImplicitPosition;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static fun.reactions.model.environment.Variable.value;

/**
 * @author MaxDikiy
 * @since 14/05/2017
 */
public class BlockBreakActivator extends Activator implements Locatable {

    private final VirtualBlockData blockData;
    private final ImplicitPosition pos;

    private BlockBreakActivator(Logic base, VirtualBlockData block, ImplicitPosition pos) {
        super(base);
        this.blockData = block;
        this.pos = pos;
    }

    public static BlockBreakActivator create(Logic base, Parameters param) {
        VirtualBlockData block = param.get("block", VirtualBlockData::fromString);
        ImplicitPosition pos;
        if (param.contains("location")) {
            pos = ImplicitPosition.byString(param.getString("location"));
        } else if (param.contains("loc")) {
            pos = ImplicitPosition.byString(param.getString("loc"));
        } else {
            pos = ImplicitPosition.fromParameters(param);
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
        if (this.blockData != null && !blockData.matches(block)) return false;
        return pos.isValidAt(block.getLocation());
    }

    @Override
    public boolean isLocatedAt(@NotNull World world, int x, int y, int z) {
        return pos.isValidAt(world.getName(), x, y, z);
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("block", blockData == null ? null : blockData.asString());
        pos.intoConfiguration(cfg);
    }

    /**
     * @author MaxDikiy
     * @since 14/05/2017
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
            Map<String, Variable> vars = new HashMap<>(BlockVariable.flatVars(block));
            vars.put(CANCEL_EVENT, value(false));
            vars.put(DO_DROP, value(dropItems));
            return vars;
        }
    }
}
