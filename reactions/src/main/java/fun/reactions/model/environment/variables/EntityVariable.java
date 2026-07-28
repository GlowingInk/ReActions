package fun.reactions.model.environment.variables;

import fun.reactions.model.environment.Variable;
import fun.reactions.util.mob.EntityUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class EntityVariable extends ComputedVariable {
    private static final List<String> CHILD_KEYS = List.of("x", "y", "z", "world", "type", "name", "location");

    private final Entity entity;

    public EntityVariable(@NotNull Entity entity) {
        this.entity = entity;
    }

    public @NotNull Entity entity() {
        return entity;
    }

    @Override
    public @NotNull String get() {
        return entity.getType().name();
    }

    @Override
    protected @NotNull List<String> childKeys() {
        return CHILD_KEYS;
    }

    @Override
    protected @NotNull ComputedVariable copy() {
        return new EntityVariable(entity);
    }

    @Override
    protected @Nullable Variable computeChild(@NotNull String key) {
        return switch (key) {
            case "x" -> Variable.value(Integer.toString(entity.getLocation().getBlockX()));
            case "y" -> Variable.value(Integer.toString(entity.getLocation().getBlockY()));
            case "z" -> Variable.value(Integer.toString(entity.getLocation().getBlockZ()));
            case "world" -> Variable.value(entity.getWorld().getName());
            case "type" -> Variable.value(entity.getType());
            case "spawn_reason" -> Variable.value(entity.getEntitySpawnReason());
            case "name" -> Variable.value(EntityUtils.getEntityDisplayName(entity));
            case "origin" -> {
                Location origin = entity.getOrigin();
                yield origin == null ? null : new LocationVariable(origin);
            }
            case "location" -> new LocationVariable(entity.getLocation());
            case "uuid", "id" -> Variable.value(entity.getUniqueId());
            case "network_id" -> Variable.value(entity.getEntityId());
            default -> null;
        };
    }
}
