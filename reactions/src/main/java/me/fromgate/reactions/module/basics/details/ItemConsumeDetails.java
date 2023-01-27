package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.ItemConsumeActivator;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.*;

public class ItemConsumeDetails extends Details {

    private final ItemStack item;
    private final boolean mainHand;

    public ItemConsumeDetails(Player p, ItemStack item, boolean mainHand) {
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
        vars.put(CANCEL_EVENT, property(false));
        vars.put("hand", plain(mainHand ? "MAIN" : "OFF"));
        if (item != null) {
            vars.put("item", lazy(() -> VirtualItem.asString(item)));
            vars.put("item-str", lazy(() -> ItemUtils.toDisplayString(item)));
        }
        return vars;
    }

    public ItemStack getItem() {
        return this.item;
    }
}
