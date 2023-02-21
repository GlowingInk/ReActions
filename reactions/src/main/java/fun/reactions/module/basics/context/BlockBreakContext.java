package fun.reactions.module.basics.context;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.BlockBreakActivator;
import fun.reactions.util.location.LocationUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.model.environment.Variable.property;
import static fun.reactions.model.environment.Variable.simple;

/**
 * Created by MaxDikiy on 2017-05-14.
 */
public class BlockBreakContext extends ActivationContext {
    public static final String DO_DROP = "is_drop";

    private final Block block;
    private final boolean dropItems;

    public BlockBreakContext(Player p, Block block, boolean dropItems) {
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

    public Block getBlock() {
        return this.block;
    }
}
