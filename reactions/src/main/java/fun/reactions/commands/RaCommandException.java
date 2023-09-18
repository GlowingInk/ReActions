package fun.reactions.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class RaCommandException extends RuntimeException {
    private static final Component ERROR = Component.text("ERROR").color(NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD);
    private final Component message;

    public RaCommandException() {
        super("ERROR");
        this.message = ERROR;
    }

    public RaCommandException(@NotNull String message) {
        super(message);
        this.message = Component.text(message);
    }

    public RaCommandException(@NotNull Component message) {
        super(PlainTextComponentSerializer.plainText().serialize(message));
        this.message = message;
    }

    public @NotNull Component message() {
        return message;
    }
}
