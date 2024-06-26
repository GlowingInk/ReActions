package fun.reactions.module.basic.placeholders;

import fun.reactions.holders.PlayerRespawner;
import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.naming.Aliased;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

// TODO: Split into different classes?
@Aliased.Names({
        "player_loc", "player_loc_eye",
        "player_loc_death", "deathpoint",
        "player_loc_view", "player_loc_view_solid",
        "player_name", "player_display", "dplayer",
        "player_item_hand", "itemplayer", "player_item_offhand", "offitemplayer",
        "player_held_slot", "slot",
        "player_health", "health",
        "player_id", "player_uuid", "uuid",
        "player_level", "level",
        "player_ip", "ip_address"
})
public class PlayerPlaceholders implements Placeholder {

    private static final Set<Material> NON_SOLID;
    static {
        Set<Material> nonSolid = EnumSet.noneOf(Material.class);
        for (Material mat : Material.values())
            if (mat.isBlock() && !mat.isCollidable()) nonSolid.add(mat);
        NON_SOLID = Set.copyOf(nonSolid);
    }

    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String param) {
        Player player = env.getPlayer();
        if (player == null) return null;
        return switch (key) {
            case "player", "player_name" -> player.getName();
            case "player_health", "health" -> Double.toString(player.getHealth());
            case "player_item_hand", "itemplayer" -> VirtualItem.asString(player.getInventory().getItemInMainHand());
            case "player_item_offhand", "offitemplayer" -> VirtualItem.asString(player.getInventory().getItemInOffHand());
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
