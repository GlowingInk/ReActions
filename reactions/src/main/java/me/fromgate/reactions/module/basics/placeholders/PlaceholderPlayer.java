package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.location.PlayerRespawner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

// TODO: Split to different classes
@Aliases({"player_loc", "player_loc_eye", "player_loc_view", "player_name",
                "player_display", "dplayer", "player_item_hand", "itemplayer",
                "health", "player_loc_death", "deathpoint", "player_id", "player_uuid", "uuid", "player_level", "level",
                "player_held_slot", "slot"})
public class PlaceholderPlayer implements Placeholder.Equal {

    private static final Set<Material> NON_SOLID;
    static {
        Set<Material> nonSolid = EnumSet.noneOf(Material.class);
        for (Material mat : Material.values())
            if (mat.isBlock() && !mat.isCollidable()) nonSolid.add(mat);
        NON_SOLID = Collections.unmodifiableSet(nonSolid);
    }

    @Override
    public @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String param) {
        Player player = context.getPlayer();
        if (player == null) return null;
        return switch (key) {
            case "player", "player_name" -> player.getName();
            case "health" -> Double.toString(player.getHealth());
            case "player_item_hand", "itemplayer" -> ItemUtils.getPlayerItemInHand(player, false);
            case "player_item_offhand", "offitemplayer" -> ItemUtils.getPlayerItemInHand(player, true);
            case "player_display", "dplayer" -> player.getDisplayName();
            case "player_loc" -> LocationUtils.locationToString(player.getLocation());
            case "player_loc_death", "deathpoint" -> LocationUtils.locationToString(Optional.of(PlayerRespawner.getLastDeathPoint(player)).orElse(player.getLocation()));
            case "player_loc_eye" -> LocationUtils.locationToString(player.getEyeLocation());
            case "player_loc_view" -> LocationUtils.locationToString(getViewLocation(player, false));
            case "player_loc_view_solid" -> LocationUtils.locationToString(getViewLocation(player, true));
            case "player_level", "level" -> Integer.toString(player.getLevel());
            case "player_uuid", "player_id", "uuid" -> player.getUniqueId().toString();
            case "player_ip", "ip_address" -> player.getAddress().getAddress().getHostAddress();
            case "player_held_slot", "slot" -> Integer.toString(player.getInventory().getHeldItemSlot());
            default -> null;
        };
    }

    /**
     * Get location that player is looking on
     *
     * @param p     Player to use
     * @param solid Search for only solid blocks or not
     * @return Location of block
     */
    private Location getViewLocation(Player p, boolean solid) {
        return p.getTargetBlock(solid ? NON_SOLID : null, 100).getLocation().add(0.5, 0.5, 0.5);
    }

    @Override
    public @NotNull String getName() {
        return "player";
    }
}
