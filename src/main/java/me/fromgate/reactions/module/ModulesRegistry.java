package me.fromgate.reactions.module;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.ActivatorType;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.selectors.Selector;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModulesRegistry {
    private final ReActions.Platform platform;

    public ModulesRegistry(ReActions.Platform platform) {
        this.platform = platform;
    }

    public void registerModule(Module module) {
        platform.logger().info("Registering " + module.getName() + " module (by " + StringUtils.join(module.getAuthors(), ", ") + ")");
        register("activators", module.getActivatorTypes(platform), ActivatorType::getName, platform.getActivatorTypes()::registerType);
        register("actions", module.getActions(platform), Action::getName, platform.getActivities()::registerAction);
        register("flags", module.getFlags(platform), Flag::getName, platform.getActivities()::registerFlag);
        register("placeholders", module.getPlaceholders(platform), Placeholder::getName, platform.getPlaceholders()::registerPlaceholder);
        register("selectors", module.getSelectors(platform), Selector::getName, platform.getSelectors()::registerSelector);
    }

    private <T> void register(String what, Collection<T> values, Function<T, String> toString, Consumer<T> register) {
        if (values.isEmpty()) return;
        List<String> names = new ArrayList<>(values.size());
        List<String> failed = null;
        for (T type : values) {
            try {
                register.accept(type);
                names.add(toString.apply(type).toUpperCase(Locale.ROOT));
            } catch (IllegalStateException e) {
                if (failed == null) failed = new ArrayList<>();
                failed.add(e.getMessage());
            }
        }
        if (!names.isEmpty()) {
            platform.logger().info("Registered " + names.size() + " " + what + ": " + String.join(", ", names));
        }
        if (failed != null && !failed.isEmpty()) {
            failed.forEach(platform.logger()::warn);
        }
    }
}
