package fun.reactions.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class RegistryUtils {
    public static <T extends Keyed> @NotNull Registry<T> getRegistry(@NotNull RegistryKey<T> key) {
        return RegistryAccess.registryAccess().getRegistry(key);
    }

    public static <T extends Keyed> @Nullable T searchRegistry(@NotNull String keyStr, @NotNull Registry<T> registry) {
        NamespacedKey key = NamespacedKey.fromString(keyStr.toLowerCase(Locale.ROOT));
        return key == null ? null : registry.get(key);
    }
}
