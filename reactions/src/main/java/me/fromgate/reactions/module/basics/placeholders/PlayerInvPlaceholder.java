package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.logic.environment.Environment;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static me.fromgate.reactions.util.NumberUtils.Is.NATURAL;

@Aliased.Names("invplayer")
public class PlayerInvPlaceholder implements Placeholder.Keyed {
    @Override
    public @Nullable String processPlaceholder(@NotNull Environment env, @NotNull String key, @NotNull String text) {
        return getPlayerInventory(env.getPlayer(), text);
    }

    private static String getPlayerInventory(Player player, String value) {
        VirtualItem vi = null;
        if (NumberUtils.isNumber(value, NATURAL)) {
            int slotNum = Integer.parseInt(value);
            if (slotNum < 0 || slotNum >= player.getInventory().getSize()) return "";
            vi = VirtualItem.fromItemStack(player.getInventory().getItem(slotNum));
        } else switch (value.toLowerCase(Locale.ROOT)) {
            case "mainhand":
            case "hand":
                return ItemUtils.getItemInHand(player, false);
            case "offhand":
                return ItemUtils.getItemInHand(player, true);
            case "head":
            case "helm":
            case "helmet":
                vi = VirtualItem.fromItemStack(player.getInventory().getHelmet());
                break;
            case "chestplate":
            case "chest":
                vi = VirtualItem.fromItemStack(player.getInventory().getChestplate());
                break;
            case "leggings":
            case "legs":
                vi = VirtualItem.fromItemStack(player.getInventory().getLeggings());
                break;
            case "boots":
            case "boot":
                vi = VirtualItem.fromItemStack(player.getInventory().getBoots());
                break;
        }
        return vi == null ? "" : vi.toString();
    }

    @Override
    public @NotNull String getName() {
        return "player_inv";
    }
}
