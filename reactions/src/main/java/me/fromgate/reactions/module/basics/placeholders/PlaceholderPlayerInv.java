package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@Aliases("invplayer")
public class PlaceholderPlayerInv implements Placeholder.Keyed {
    @Override
    public @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String text) {
        return getPlayerInventory(context.getPlayer(), text);
    }

    private static String getPlayerInventory(Player player, String value) {
        VirtualItem vi = null;
        if (NumberUtils.isPositiveInt(value)) {
            int slotNum = Integer.parseInt(value);
            if (slotNum < 0 || slotNum >= player.getInventory().getSize()) return "";
            vi = VirtualItem.fromItem(player.getInventory().getItem(slotNum));
        } else switch (value.toLowerCase(Locale.ROOT)) {
            case "mainhand":
            case "hand":
                return ItemUtils.getItemInHand(player, false);
            case "offhand":
                return ItemUtils.getItemInHand(player, true);
            case "head":
            case "helm":
            case "helmet":
                vi = VirtualItem.fromItem(player.getInventory().getHelmet());
                break;
            case "chestplate":
            case "chest":
                vi = VirtualItem.fromItem(player.getInventory().getChestplate());
                break;
            case "leggings":
            case "legs":
                vi = VirtualItem.fromItem(player.getInventory().getLeggings());
                break;
            case "boots":
            case "boot":
                vi = VirtualItem.fromItem(player.getInventory().getBoots());
                break;
        }
        return vi == null ? "" : vi.toString();
    }

    @Override
    public @NotNull String getName() {
        return "player_inv";
    }
}
