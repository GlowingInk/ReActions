package fun.reactions.util.item.aspects;

import fun.reactions.util.parameter.Parameterizable;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MetaAspect {
    @NotNull String getName();

    @NotNull MetaAspect.Instance fromString(@NotNull String value);

    @Nullable MetaAspect.Instance fromItem(@NotNull ItemMeta meta);

    interface Instance extends Parameterizable {
        void apply(@NotNull ItemMeta meta);

        boolean isSimilar(@NotNull ItemMeta meta);

        @NotNull String getName(); // TODO: I don't like the fact it's in both aspect and instance

        @NotNull String asString();

        default @NotNull Parameters asParameters() {
            return Parameters.singleton(getName(), asString());
        }
    }
}
