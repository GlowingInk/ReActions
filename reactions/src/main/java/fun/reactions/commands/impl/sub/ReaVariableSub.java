package fun.reactions.commands.impl.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fun.reactions.ReActions;
import fun.reactions.commands.RaCommandBase;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ReaVariableSub extends RaCommandBase {
    public ReaVariableSub(@NotNull ReActions.Platform platform) {
        super(platform);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> asNode() {
        return literal("variable")
                .executes(ctx -> promptForName(ctx, "variable"))
                .then(argument("name", StringArgumentType.word())
                        .executes(this::help)
                        .then(literal("show").executes(this::show))
                        .then(literal("set")
                                .executes(ctx -> setVariable(ctx, ""))
                                .then(argument("value", StringArgumentType.greedyString())
                                        .executes(ctx -> setVariable(ctx, StringArgumentType.getString(ctx, "value")))))
                        .then(literal("delete") // TODO
                                /*.executes(ctx -> { delete(ctx); return Command.SINGLE_SUCCESS; })*/))
                .build();
    }

    private int help(@NotNull CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (platform.getPersistentVariables().getVariable(null, name) == null) {
            return suggestCreate(ctx, "variable " + name, "Variable", name, "set");
        }
        return sendHelp(ctx, "variable " + esc(name),
                "create", "&e[value]", "Create variable with optional&e value",
                "show", "", "Show a variable",
                "delete", "", "Delete a variable",
                "set", "[value]", "Set variable to a &especified value"
        );
    }

    private int setVariable(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String value) {
        String name = StringArgumentType.getString(ctx, "name");
        String[] varName = name.split(":");
        platform.getPersistentVariables().setVariable(
                varName.length > 1 ? varName[0] : null,
                varName.length > 1 ? varName[1] : varName[0],
                value
        );
        sendPrefixed(ctx, "Variable&a '&{name}'&r was created with the value:\n&{value}", Map.of(
                "name", name,
                "value", value
        ));
        return  Command.SINGLE_SUCCESS;
    }

    private int show(@NotNull CommandContext<CommandSourceStack> ctx) {
        String value = platform.getPersistentVariables().getVariable(null, StringArgumentType.getString(ctx, "name"));
        sendInky(ctx, value == null ? "" : esc(value)); // TODO Better message
        return Command.SINGLE_SUCCESS;
    }
}