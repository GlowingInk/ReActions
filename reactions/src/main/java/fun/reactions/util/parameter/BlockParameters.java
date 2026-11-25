package fun.reactions.util.parameter;

import ink.glowing.utils.hash.CaseInsensitive;
import org.bukkit.block.Block;


public final class BlockParameters extends Parameters {
    private final Block block;

    public BlockParameters(String param, Block block) {
        super(param, CaseInsensitive.newLinkedMap(fromString(param).originMap()));
        this.block = block;
    }

    public Block getBlock() {
        return this.block;
    }
}
