package me.fromgate.reactions.util.parameter;

import me.fromgate.reactions.util.collections.Maps;
import org.bukkit.block.Block;

public final class BlockParameters extends Parameters {
    private final Block block;

    public BlockParameters(String param, Block block) {
        super(param, Maps.caseInsensitive(Parameters.fromString(param).originMap()));
        this.block = block;
    }

    public Block getBlock() {
        return this.block;
    }
}
