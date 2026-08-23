package fun.reactions.commands.plugin.impl.sub;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.plugin.RaCommandBase;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReaVariableSub extends RaCommandBase {
    public ReaVariableSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("variable")
                .requires(permission("reactions.variable"))
                .executes(ctx -> promptForName(ctx, "variable"))
                .then(argument("name", StringArgumentType.word())
                        .executes(this::help)
                        .then(literal("show")
                                .requires(permission("reactions.variable.view"))
                                .executes(this::show))
                        .then(literal("set")
                                .requires(permission("reactions.variable.edit"))
                                .executes(ctx -> set(ctx, ""))
                                .then(argument("value", StringArgumentType.greedyString())
                                        .executes(ctx -> set(ctx, StringArgumentType.getString(ctx, "value")))))
                        .then(literal("delete")
                                .requires(permission("reactions.variable.edit"))
                                .executes(this::delete)))
                .build();
    }

    private int help(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        boolean exists = platform.getPersistentVariables().getVariable(playerOf(name), varNameOf(name)) != null;
        return sendHelp(ctx, "variable", name, exists, "set",
                "show", "", "Show a variable",
                "set", "[value]", "Set variable to a&e specified value",
                "delete", "", "Delete a variable"
        );
    }

    private int set(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String value) {
        String name = StringArgumentType.getString(ctx, "name");
        platform.getPersistentVariables().setVariable(playerOf(name), varNameOf(name), value);
        sendPrefixed(ctx, "Variable&a '&{name}'&r was created with the value:\n&{value}", Map.of(
                "name", name,
                "value", value
        ));
        return SINGLE_SUCCESS;
    }

    private int show(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        String value = platform.getPersistentVariables().getVariable(playerOf(name), varNameOf(name));
        if (value == null) {
            sendNotFound(ctx, "Variable", name);
            return SINGLE_SUCCESS;
        }
        sendPrefixed(ctx, "Variable &a'&{name}'&r&7:\n&{value}", Map.of(
                "name", name,
                "value", value
        ));
        return SINGLE_SUCCESS;
    }

    private int delete(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (!platform.getPersistentVariables().removeVariable(playerOf(name), varNameOf(name))) {
            sendNotFound(ctx, "Variable", name);
            return SINGLE_SUCCESS;
        }
        sendPrefixed(ctx, "Variable &a'" + esc(name) + "'&r was deleted.");
        return SINGLE_SUCCESS;
    }

    private static @Nullable String playerOf(@NotNull String name) {
        int idx = name.indexOf(':');
        return idx == -1 ? null : name.substring(0, idx);
    }

    private static @NotNull String varNameOf(@NotNull String name) {
        int idx = name.indexOf(':');
        return idx == -1 ? name : name.substring(idx + 1);
    }
}