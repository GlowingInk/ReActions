package fun.reactions.module.basic.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

@Aliased.Names("enderchest")
public class EnderChestPlaceholder implements Placeholder {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String params) {
        Player player = env.getPlayer();
        if (player == null) return null;
        OptionalInt slotOpt = NumberUtils.parseInteger(params, Is.NON_NEGATIVE);
        if (slotOpt.isPresent()) {
            int slotNum = slotOpt.getAsInt();
            Inventory inv = player.getEnderChest();
            if (slotNum >= 0 && slotNum < inv.getSize()) {
                return VirtualItem.asString(inv.getItem(slotNum));
            }
        }
        return VirtualItem.AIR.asString();
    }

    @Override
    public @NotNull String getName() {
        return "ender_chest";
    }
}
