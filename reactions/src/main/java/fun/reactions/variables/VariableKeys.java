package fun.reactions.variables;

import fun.reactions.ReActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static fun.reactions.util.Utils.isStringEmpty;

final class VariableKeys {
    static final String GENERAL = "";

    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private VariableKeys() { }

    static void backupFile(@NotNull ReActions.Platform rea, @NotNull File file) {
        if (!file.exists()) return;
        File backup = new File(file.getParentFile(), file.getName() + ".bak-" + backupSuffix());
        if (!file.renameTo(backup)) {
            rea.logger().error("Failed to back up {} to {}", file, backup);
        }
    }

    private static @NotNull String backupSuffix() {
        return BACKUP_TIMESTAMP.format(LocalDateTime.now());
    }

    static @NotNull String ownerOf(@Nullable String player) {
        return isStringEmpty(player) ? GENERAL : player;
    }

    static @NotNull String normalize(@NotNull String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}
