package fun.reactions.model.environment.variables;

import fun.reactions.model.environment.Variable;
import fun.reactions.util.location.LocationUtils;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class BlockVariable extends ComputedVariable {
    private static final List<String> CHILD_KEYS = List.of("x", "y", "z", "world", "type", "location");

    private final Block block;

    public static @NotNull Map<String, Variable> flatVars(@NotNull Block block) {
        return Map.of(
                "blocklocation", Variable.lazy(() -> LocationUtils.locationToString(block)),
                "block_x", Variable.lazy(() -> Integer.toString(block.getX())),
                "block_y", Variable.lazy(() -> Integer.toString(block.getY())),
                "block_z", Variable.lazy(() -> Integer.toString(block.getZ())),
                "block_world", Variable.value(block.getWorld().getName()),
                "blocktype", Variable.value(block.getType()),
                "block", new BlockVariable(block)
        );
    }

    public BlockVariable(@NotNull Block block) {
        this.block = block;
    }

    public @NotNull Block block() {
        return block;
    }

    @Override
    public @NotNull String get() {
        return block.getType().name();
    }

    @Override
    protected @NotNull List<String> childKeys() {
        return CHILD_KEYS;
    }

    @Override
    protected @NotNull ComputedVariable copy() {
        return new BlockVariable(block);
    }

    @Override
    protected @Nullable Variable computeChild(@NotNull String key) {
        return switch (key) {
            case "x" -> Variable.value(Integer.toString(block.getX()));
            case "y" -> Variable.value(Integer.toString(block.getY()));
            case "z" -> Variable.value(Integer.toString(block.getZ()));
            case "world" -> Variable.value(block.getWorld().getName());
            case "type" -> Variable.value(block.getType());
            case "location" -> new LocationVariable(block);
            default -> null;
        };
    }
}
