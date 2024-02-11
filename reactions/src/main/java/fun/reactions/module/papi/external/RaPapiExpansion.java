package fun.reactions.module.papi.external;

import fun.reactions.ReActions;
import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variables;
import fun.reactions.placeholders.PlaceholdersManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RaPapiExpansion extends PlaceholderExpansion {
    private final ReActions.Platform platform;
    private final PlaceholdersManager raPlaceholders;

    public RaPapiExpansion(@NotNull ReActions.Platform platform) {
        this.platform = platform;
        this.raPlaceholders = platform.getPlaceholders();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "reactions";
    }

    @Override
    public @NotNull String getAuthor() {
        return "imDaniX";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String param) {
        return raPlaceholders.resolvePlaceholder(new Environment(
                platform,
                "",
                new Variables(),
                player instanceof Player onlinePlayer ? onlinePlayer : null,
                true // We don't know if we're in async, so let's consider we are
        ), param);
    }
}
