package me.fromgate.reactions.util.item.resolvers;

import me.fromgate.reactions.util.alias.Aliases;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Aliases("skull-owner")
public class HeadResolver implements MetaResolver { // TODO: Support UUIDs and raw values
    @Override
    public @NotNull String getName() {
        return "head-owner";
    }

    @Override
    public @NotNull MetaResolver.Instance fromString(@NotNull String value) {
        return new HeadInst(value);
    }

    @Override
    public @Nullable MetaResolver.Instance fromItem(@NotNull ItemMeta meta) {
        if (meta instanceof SkullMeta skullMeta) {
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
