package me.fromgate.reactions.util.item.resolvers;

import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.alias.Aliases;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliases("data")
public class DurabilityResolver implements MetaResolver {
    @Override
    public @NotNull String getName() {
        return "durability";
    }

    @Override
    public @NotNull MetaResolver.Instance fromString(@NotNull String value) {
        return new Durability(NumberUtils.getInteger(value, 0));
    }

    @Override
    public @Nullable MetaResolver.Instance fromItem(@NotNull ItemMeta meta) {
        if (meta instanceof Damageable damageMeta && damageMeta.hasDamage()) {
            return new Durability(damageMeta.getDamage());
        }
        return null;
    }

    private record Durability(int value) implements Instance {
        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof Damageable damageMeta) {
                damageMeta.setDamage(value);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (meta instanceof Damageable damageMeta) {
                return value < 0 ? !damageMeta.hasDamage() : damageMeta.getDamage() >= value;
            }
            return false;
        }

        @Override
        public @NotNull String getName() {
            return "durability";
        }

        @Override
        public @NotNull String asString() {
            return value < 0 ? "" : Integer.toString(value);
        }
    }
}
