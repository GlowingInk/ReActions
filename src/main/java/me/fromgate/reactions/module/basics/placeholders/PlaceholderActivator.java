package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;

@Alias("activatorname")
public class PlaceholderActivator implements Placeholder.Equal {
    @Override
    public String processPlaceholder(RaContext context, String key, String param) {
        return context.getActivatorName();
    }

    @Override
    public String getId() {
        return "activator_name";
    }
}
