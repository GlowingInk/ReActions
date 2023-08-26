package fun.reactions.events.listeners;

import fun.reactions.Cfg;
import fun.reactions.ReActions;
import fun.reactions.module.basic.ContextManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Set;

public class GodModeListener implements Listener {

    private static final Set<EntityDamageEvent> godCheckEvents = new HashSet<>();

    public static void init() {
        if (Cfg.godActivatorEnable) {
            Bukkit.getPluginManager().registerEvents(new GodModeListener(), ReActions.getPlugin());
            Bukkit.getScheduler().runTaskTimer(ReActions.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(GodModeListener::setEventGod), 30, Cfg.godActivatorCheckTicks);
        }
    }

    public static boolean isGod(Player player) {
        return player.hasMetadata("reactions-god") && player.getMetadata("reactions-god").get(0).asBoolean();
    }

    public static boolean setGod(Player player) {
        if (!isGod(player)) {
            player.setMetadata("reactions-god", new FixedMetadataValue(ReActions.getPlugin(), true));
            return true;
        }
        return false;
    }

    public static boolean removeGod(Player player) {
        if (isGod(player)) {
            player.removeMetadata("reactions-god", ReActions.getPlugin());
            return true;
        }
        return false;
    }

    public static void setCheckGod(Player player) {
        player.setMetadata("reactions-god-check", new FixedMetadataValue(ReActions.getPlugin(), true));
    }

    public static boolean checkGod(Player player) {
        boolean result = false;
        if (player.hasMetadata("reactions-god-check")) {
            result = player.getMetadata("reactions-god-check").get(0).asBoolean();
            player.removeMetadata("reactions-god-check", ReActions.getPlugin());
        }
        return result;
    }

    public static void setEventGod(Player player) {
        EntityDamageEvent event = new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, 0);
        godCheckEvents.add(event);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void cancelGodEvent(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent && godCheckEvents.contains(event)) {
            godCheckEvents.remove(event);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCheckGod(EntityDamageEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            if (event.isCancelled()) {
                if (GodModeListener.checkGod(player) && GodModeListener.setGod(player)) return;
                if (GodModeListener.setGod(player) && ContextManager.triggerGod(player, true)) {
                    GodModeListener.removeGod(player);
                }
            } else if (GodModeListener.removeGod(player) && ContextManager.triggerGod(player, false)) {
                GodModeListener.setGod(player);
                event.setCancelled(true);
            }
        }
    }
}
