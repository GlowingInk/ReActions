package fun.reactions.module.basics.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static fun.reactions.util.NumberUtils.Is.NATURAL;

@Aliased.Names("invplayer")
public class PlayerInvPlaceholder implements Placeholder.Keyed {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String text) {
        return getPlayerInventory(env.getPlayer(), text);
    }

    private static String getPlayerInventory(Player player, String value) {
        ItemStack item = null;
        if (NumberUtils.isNumber(value, NATURAL)) {
            int slotNum = Integer.parseInt(value);
            if (slotNum < 0 || slotNum >= player.getInventory().getSize()) return "";
            item = player.getInventory().getItem(slotNum);
        } else switch (value.toLowerCase(Locale.ROOT)) {
            case "mainhand":
            case "hand":
                return ItemUtils.getItemInHand(player, false);
            case "offhand":
                return ItemUtils.getItemInHand(player, true);
            case "head":
            case "helm":
            case "helmet":
                item = player.getInventory().getHelmet();
                break;
            case "chestplate":
            case "chest":
                item = player.getInventory().getChestplate();
                break;
            case "leggings":
            case "legs":
                item = player.getInventory().getLeggings();
                break;
            case "boots":
            case "boot":
                item = player.getInventory().getBoots();
                break;
        }
        return item == null ? "" : VirtualItem.asString(item);
    }

    @Override
    public @NotNull String getName() {
        return "player_inv";
    }
}
