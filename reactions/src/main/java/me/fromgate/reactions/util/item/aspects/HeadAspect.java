package me.fromgate.reactions.util.item.aspects;

import me.fromgate.reactions.util.naming.Aliased;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Aliased.Names("skull-owner")
public class HeadAspect implements MetaAspect { // TODO: Support UUIDs and raw values
    @Override
    public @NotNull String getName() {
        return "head-owner";
    }

    @Override
    public @NotNull MetaAspect.Instance fromString(@NotNull String value) {
        return new HeadInst(value.isEmpty() ? null : value);
    }

    @Override
    public @Nullable MetaAspect.Instance fromItem(@NotNull ItemMeta meta) {
        if (meta instanceof SkullMeta skullMeta && skullMeta.hasOwner()) {
            return new HeadInst(skullMeta.getOwner());
        }
        return null;
    }

    private record HeadInst(@Nullable String ownerName) implements Instance {
        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof SkullMeta skullMeta) {
                skullMeta.setOwner(ownerName);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (meta instanceof SkullMeta skullMeta) {
                return Objects.equals(ownerName, skullMeta.getOwner());
            }
            return false;
        }

        @Override
        public @NotNull String getName() {
            return "head-owner";
        }

        @Override
        public @NotNull String asString() {
            return ownerName == null ? "" : ownerName;
        }
    }
}
