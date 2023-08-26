package fun.reactions.module.basic.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static fun.reactions.util.NumberUtils.Is.NATURAL;

@Aliased.Names("invplayer")
public class PlayerInvPlaceholder implements Placeholder.Keyed {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String text) {
        Player player = env.getPlayer();
        if (player == null) {
            return null;
        }
        if (NumberUtils.isNumber(text, NATURAL)) {
            int slotNum = Integer.parseInt(text);
            if (slotNum < 0 || slotNum >= player.getInventory().getSize()) return VirtualItem.AIR.asString();
            return VirtualItem.asString(player.getInventory().getItem(slotNum));
        } else {
            return VirtualItem.asString(switch (text.toLowerCase(Locale.ROOT)) {
                case "mainhand", "main-hand", "main_hand", "hand" ->
                        player.getInventory().getItemInMainHand();
                case "offhand", "off-hand", "off_hand", "secondhand", "second-hand", "second_hand" ->
                        player.getInventory().getItemInOffHand();
                case "helmet", "helm", "head" ->
                        player.getInventory().getHelmet();
                case "chestplate", "chest", "body" ->
                        player.getInventory().getChestplate();
                case "leggings", "legs", "pants" ->
                        player.getInventory().getLeggings();
                case "boots", "boot", "foot", "feet" ->
                        player.getInventory().getBoots();
                default -> null;
            });
        }
    }

    @Override
    public @NotNull String getName() {
        return "player_inv";
    }
}
