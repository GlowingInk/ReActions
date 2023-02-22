package fun.reactions.module.basics.contexts;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.ItemConsumeActivator;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.item.VirtualItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ItemConsumeContext extends ActivationContext {

    private final ItemStack item;
    private final boolean mainHand;

    public ItemConsumeContext(Player p, ItemStack item, boolean mainHand) {
        super(p);
        this.item = item;
        this.mainHand = mainHand;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return ItemConsumeActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        Map<String, Variable> vars = new HashMap<>();
        vars.put(CANCEL_EVENT, Variable.property(false));
        vars.put("hand", Variable.simple(mainHand ? "MAIN" : "OFF"));
        if (item != null) {
            vars.put("item", Variable.lazy(() -> VirtualItem.asString(item)));
            vars.put("item-str", Variable.lazy(() -> ItemUtils.toDisplayString(item)));
        }
        return vars;
    }

    public ItemStack getItem() {
        return this.item;
    }
}
