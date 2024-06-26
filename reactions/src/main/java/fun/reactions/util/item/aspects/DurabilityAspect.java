package fun.reactions.util.item.aspects;

import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.NumberUtils;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names("data")
public class DurabilityAspect implements MetaAspect {
    @Override
    public @NotNull String getName() {
        return "durability";
    }

    @Override
    public @NotNull MetaAspect.Instance fromString(@NotNull String value) {
        return new DurabilityInst(NumberUtils.asInteger(value, 0));
    }

    @Override
    public @Nullable MetaAspect.Instance fromItem(@NotNull ItemMeta meta) {
        if (meta instanceof Damageable damageMeta && damageMeta.hasDamage()) {
            return new DurabilityInst(damageMeta.getDamage());
        }
        return null;
    }

    private record DurabilityInst(int value) implements Instance {
        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof Damageable damageMeta) {
                damageMeta.setDamage(value);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (meta instanceof Damageable damageMeta) {
                return value >= 0 ? damageMeta.getDamage() >= value : !damageMeta.hasDamage();
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
