package fun.reactions.model.environment.variables;

import fun.reactions.model.environment.Variable;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.parameter.Parameterizable;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ItemVariable implements Variable, Parameterizable {
    private final ItemStack item;
    private @Nullable StructVariable struct;

    public ItemVariable(@NotNull ItemStack item) {
        this.item = item;
    }

    public @NotNull ItemStack item() {
        return item;
    }

    @Override
    public @NotNull String get() {
        return struct().get();
    }

    @Override
    public @Nullable Variable child(@NotNull String key) {
        if (struct == null) {
            Variable fastPath = switch (key.toLowerCase(Locale.ROOT)) {
                case "type" -> Variable.value(item.getType());
                case "amount" -> Variable.value(Integer.toString(item.getAmount()));
                default -> null;
            };
            if (fastPath != null) return fastPath;
        }
        return struct().child(key);
    }

    @Override
    public @NotNull Map<String, Variable> children() {
        return struct().children();
    }

    @Override
    public boolean putChild(@NotNull String key, @NotNull Variable value) {
        return struct().putChild(key, value);
    }

    @Override
    public void markChanged() {
        struct().markChanged();
    }

    @Override
    public @NotNull Optional<String> changed() {
        return struct().changed();
    }

    @Override
    public @NotNull Variable set(@NotNull String value) {
        struct().set(value);
        return this;
    }

    @Override
    public @NotNull Variable fork() {
        ItemVariable forked = new ItemVariable(item);
        if (struct != null) {
            forked.struct = (StructVariable) struct.fork();
        }
        return forked;
    }

    @Override
    public @NotNull Parameters asParameters() {
        return Variable.super.asParameters();
    }

    private @NotNull StructVariable struct() {
        if (struct == null) {
            VirtualItem virtual = VirtualItem.fromItemStack(item);
            struct = StructVariable.ofFlat(virtual.asString(), virtual.asParameters());
        }
        return struct;
    }
}
