package me.fromgate.reactions.util.item.resolvers;

import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MetaResolver {
    @NotNull String getName();

    @NotNull MetaResolver.Instance fromString(@NotNull String value);

    @Nullable MetaResolver.Instance fromItem(@NotNull ItemMeta meta);

    interface Instance {
        void apply(@NotNull ItemMeta meta);

        boolean isSimilar(@NotNull ItemMeta meta);

        @NotNull String asString();
    }
}
