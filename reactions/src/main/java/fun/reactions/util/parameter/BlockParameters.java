package fun.reactions.util.parameter;

import org.bukkit.block.Block;

import static fun.reactions.util.collections.CollectionUtils.caseInsensitiveMap;

public final class BlockParameters extends Parameters {
    private final Block block;

    public BlockParameters(String param, Block block) {
        super(param, caseInsensitiveMap(fromString(param).originMap()));
        this.block = block;
    }

    public Block getBlock() {
        return this.block;
    }
}
