package fun.reactions.util.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public final class CollectionUtils {
    private CollectionUtils() {}

    public static <T> @NotNull List<T> emptyOnNull(@Nullable T @Nullable [] arr) {
        return arr == null ? List.of() : Arrays.asList(arr);
    }
}
