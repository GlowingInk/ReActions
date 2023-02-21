package fun.reactions.util.parameter;

import fun.reactions.util.collections.Maps;
import org.bukkit.block.Block;

public final class BlockParameters extends Parameters {
    private final Block block;

    public BlockParameters(String param, Block block) {
        super(param, Maps.caseInsensitive(fromString(param).originMap()));
        this.block = block;
    }

    public Block getBlock() {
        return this.block;
    }
}
