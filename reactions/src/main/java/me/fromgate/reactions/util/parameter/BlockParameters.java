package me.fromgate.reactions.util.parameter;

import org.bukkit.block.Block;

public class BlockParameters extends Parameters {
    private final Block block;

    public BlockParameters(String param, Block block) {
        super(param, Parameters.fromString(param).getMap());
        this.block = block;
    }

    public Block getBlock() {
        return this.block;
    }
}
