package me.fromgate.reactions.events.listeners;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.events.PlayerAttacksEntityEvent;
import me.fromgate.reactions.events.PlayerPickupItemEvent;
import me.fromgate.reactions.externals.RaEconomics;
import me.fromgate.reactions.externals.RaVault;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.ItemStoragesManager;
import me.fromgate.reactions.module.basics.StoragesManager;
import me.fromgate.reactions.module.basics.activators.*;
import me.fromgate.reactions.module.basics.storages.*;
import me.fromgate.reactions.time.waiter.WaitingManager;
import me.fromgate.reactions.util.BlockUtils;
import me.fromgate.reactions.util.TemporaryOp;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.data.DataValue;
import me.fromgate.reactions.util.location.PlayerRespawner;
import me.fromgate.reactions.util.location.Teleporter;
import me.fromgate.reactions.util.math.Rng;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.message.RaDebug;
import me.fromgate.reactions.util.mob.EntityUtils;
import me.fromgate.reactions.util.mob.MobSpawn;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Map;

public class BukkitListener implements Listener {

    /*
    private static boolean triggerEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            Location loc = event.getEntity().getLocation();
            for (Player p : loc.getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(loc) > 0.7) continue;
                EntityDamageByEntityEvent ev = new EntityDamageByEntityEvent(event.getEntity(), p, EntityDamageEvent.DamageCause.FALLING_BLOCK, 0);
                Bukkit.getPluginManager().callEvent(ev);
                return ev.isCancelled();
            }
        }
        return false;
    }
    */

    @EventHandler(ignoreCancelled = true)
    public void onPickupEvent(EntityPickupItemEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;
        PlayerPickupItemEvent plEvent = new PlayerPickupItemEvent((Player) event.getEntity(), event.getItem());
        Bukkit.getPluginManager().callEvent(plEvent);
        event.setCancelled(plEvent.isCancelled());
    }

    @EventHandler(ignoreCancelled = true)
    public void onAttackEvent(EntityDamageByEntityEvent event) {
        LivingEntity damager = EntityUtils.getDamagerEntity(event);
        if (damager == null || damager.getType() != EntityType.PLAYER) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        PlayerAttacksEntityEvent plEvent = new PlayerAttacksEntityEvent((Player) damager,
                (LivingEntity) event.getEntity(),
                event.getDamage(),
                event.getCause());
        Bukkit.getPluginManager().callEvent(plEvent);
        event.setDamage(plEvent.getDamage());
        event.setCancelled(plEvent.isCancelled());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Map<String, DataValue> changeables = StoragesManager.triggerTeleport(
                event.getPlayer(),
                event.getCause(),
                event.getTo());
        event.setTo(changeables.get(TeleportStorage.LOCATION_TO).asLocation());
        event.setCancelled(changeables.get(Storage.CANCEL_EVENT).asBoolean());
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getRightClicked().getType() != EntityType.ARMOR_STAND) return;
        if (StoragesManager.triggerMobClick(event.getPlayer(), (LivingEntity) event.getRightClicked()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        // TODO: That's not really good solution
		/*
		Bukkit.getScheduler().runTask(ReActions.getPlugin(), () -> {
			if (StorageManager.triggerMessage(event.getPlayer(), MessageActivator.Source.CHAT_INPUT, event.getMessage())) {
				event.setCancelled(true);
			}
		});*/
        try {
            Map<String, DataValue> changeables = StoragesManager.triggerMessage(event.getPlayer(),
                    MessageActivator.Source.CHAT_INPUT,
                    event.getMessage());
            if (changeables == null) return;
            event.setMessage(changeables.get(MessageStorage.MESSAGE).asString());
            event.setCancelled(changeables.get(Storage.CANCEL_EVENT).asBoolean());
        } catch (IllegalStateException ignore) {
            Msg.logOnce("asyncchaterror", "Chat is in async thread. Because of that you should use " +
                    "additional EXEC activator in some cases, like teleportation, setting blocks etc.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        if (StoragesManager.triggerPrecommand(null, event.getSender(), event.getCommand()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (StoragesManager.triggerPrecommand(event.getPlayer(), event.getPlayer(), event.getMessage().substring(1)))
            event.setCancelled(true);
    }

    // TODO: All the checks should be inside activator
    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        for (Activator activator : ReActions.getActivators().getType(SignActivator.class).getActivators()) {
            SignActivator signAct = (SignActivator) activator;
            if (!signAct.checkMask(event.getLines())) continue;
            Msg.MSG_SIGNFORBIDDEN.print(event.getPlayer(), '4', 'c', signAct.getLogic().getName());
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (StoragesManager.triggerItemHeld(event.getPlayer(), event.getNewSlot(), event.getPreviousSlot()))
            event.setCancelled(true);
        else {
            ItemStoragesManager.triggerItemHold(event.getPlayer());
            ItemStoragesManager.triggerItemWear(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryInteract(InventoryInteractEvent event) {
        ItemStoragesManager.triggerItemHold((Player) event.getWhoClicked());
        ItemStoragesManager.triggerItemWear((Player) event.getWhoClicked());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        ItemStoragesManager.triggerItemHold((Player) event.getPlayer());
        ItemStoragesManager.triggerItemWear((Player) event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerRespawner.addPlayerRespawn(event);
        StoragesManager.triggerPvpKill(event);
        StoragesManager.triggerPvpDeath(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (StoragesManager.triggerItemConsume(event))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerClickMob(PlayerInteractEntityEvent event) {
        StoragesManager.triggerItemClick(event);
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        StoragesManager.triggerMobClick(event.getPlayer(), (LivingEntity) event.getRightClicked());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // TODO: Set respawn location
        PlayerRespawner.triggerPlayerRespawn(event.getPlayer(), event.getRespawnLocation());
        StoragesManager.triggerAllRegions(event.getPlayer(), event.getRespawnLocation(), event.getPlayer().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        List<ItemStack> stacks = MobSpawn.getMobDrop(event.getEntity());
        if (stacks != null && !stacks.isEmpty()) {
            event.getDrops().clear();
            event.getDrops().addAll(stacks);
        }

        if (event.getEntity().hasMetadata("ReActions-xp")) {
            int xp = Rng.nextIntRanged(event.getEntity().getMetadata("ReActions-xp").get(0).asString());
            event.setDroppedExp(xp);
        }

        Player killer = EntityUtils.getKiller(event.getEntity().getLastDamageCause());
        if (killer == null) return;

        StoragesManager.triggerMobKill(killer, event.getEntity());
        if (event.getEntity().hasMetadata("ReActions-money") && RaVault.isEconomyConnected()) {
            int money = Rng.nextIntRanged(event.getEntity().getMetadata("ReActions-money").get(0).asString());
            RaEconomics.creditAccount(killer.getName(), "", Double.toString(money), "");
            Msg.MSG_MOBBOUNTY.print(killer, 'e', '6', RaEconomics.format(money, ""), event.getEntity().getType().name());
        }
        if (event.getEntity().hasMetadata("ReActions-activator")) {
            String exec = event.getEntity().getMetadata("ReActions-activator").get(0).asString();
            StoragesManager.triggerExec(killer, exec, null);
        }

    }

    /*
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (triggerEntityChangeBlock(event)) event.setCancelled(true);
    }
    */

	/*
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		StoragesManager.triggerProjectileHit(event);
	}
	*/

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCheckGodEvent(EntityDamageEvent event) {
        GodModeListener.cancelGodEvent(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobGrowl(PlayerAttacksEntityEvent event) {
        LivingEntity damager = event.getPlayer();
        if (!damager.hasMetadata("ReActions-growl")) return;
        String growl = damager.getMetadata("ReActions-growl").get(0).asString();
        if (Utils.isStringEmpty(growl)) return;
        Utils.soundPlay(damager.getLocation(), growl);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobDamageByPlayer(PlayerAttacksEntityEvent event) {
        Map<String, DataValue> changeables = StoragesManager.triggerMobDamage(event.getPlayer(), event.getEntity(), event.getDamage(), event.getCause());
        event.setDamage(changeables.get(MobDamageStorage.DAMAGE).asDouble());
        event.setCancelled(changeables.get(Storage.CANCEL_EVENT).asBoolean());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageByMob(EntityDamageByEntityEvent event) {
        LivingEntity damager = EntityUtils.getDamagerEntity(event);
        if (damager == null || !damager.hasMetadata("ReActions-dmg")) return;
        double dmg = damager.getMetadata("ReActions-dmg").get(0).asDouble();
        if (dmg < 0) return;
        event.setDamage(event.getDamage() * dmg);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        String source;
        if (event.getEntity().getType() != EntityType.PLAYER) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.CUSTOM && Math.round(event.getDamage()) == 0) return;
        if (event instanceof EntityDamageByEntityEvent evdmg) {
            source = "ENTITY";
            Map<String, DataValue> changeables = StoragesManager.triggerDamageByMob(evdmg);
            event.setDamage(changeables.get(DamageStorage.DAMAGE).asDouble());
            event.setCancelled(changeables.get(Storage.CANCEL_EVENT).asBoolean());
        } else if (event instanceof EntityDamageByBlockEvent evdmg) {
            source = "BLOCK";
            Block blockDamager = evdmg.getDamager();
            if (blockDamager != null) {
                Map<String, DataValue> changeables = StoragesManager.triggerDamageByBlock(evdmg, blockDamager);
                event.setDamage(changeables.get(DamageStorage.DAMAGE).asDouble());
                event.setCancelled(changeables.get(Storage.CANCEL_EVENT).asBoolean());
            }
        } else {
            source = "OTHER";
        }

        Map<String, DataValue> changeables = StoragesManager.triggerDamage(event, source);
        event.setDamage(changeables.get(DamageStorage.DAMAGE).asDouble());
        event.setCancelled(changeables.get(Storage.CANCEL_EVENT).asBoolean());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobCry(EntityDamageEvent event) {
        if ((event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) && (event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE))
            return;
        if (event.getEntityType() == EntityType.PLAYER) return;
        if (!(event.getEntity() instanceof LivingEntity le)) return;
        if (!le.hasMetadata("ReActions-cry")) return;
        String cry = le.getMetadata("ReActions-cry").get(0).asString();
        if (cry.isEmpty()) return;
        if (!(event instanceof EntityDamageByEntityEvent evdmg)) return;
        if (evdmg.getDamager() instanceof Projectile prj) {
            LivingEntity shooter = EntityUtils.getEntityFromProjectile(prj.getShooter());
            if (shooter == null) return;
            if (!(shooter instanceof Player)) return;
        } else if (evdmg.getDamager().getType() != EntityType.PLAYER) return;
        Utils.soundPlay(le.getLocation(), cry);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPvPDamage(PlayerAttacksEntityEvent event) {
        LivingEntity target = event.getEntity();
        if (target.getType() != EntityType.PLAYER) return;
        Player damager = event.getPlayer();
        long time = System.currentTimeMillis();
        damager.setMetadata("reactions-pvp-time", new FixedMetadataValue(ReActions.getPlugin(), time));
        target.setMetadata("reactions-pvp-time", new FixedMetadataValue(ReActions.getPlugin(), time));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        WaitingManager.refreshPlayer(player);
        TemporaryOp.removeOp(player);
        RaDebug.offPlayerDebug(player);
        MoveListener.initLocation(player);

        StoragesManager.triggerJoin(player, !player.hasPlayedBefore());
        StoragesManager.triggerAllRegions(player, player.getLocation(), null);
        StoragesManager.triggerCuboid(player);
        ItemStoragesManager.triggerItemHold(player);
        ItemStoragesManager.triggerItemWear(player);
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!BlockUtils.isSign(event.getClickedBlock())) return;
        Sign sign = (Sign) event.getClickedBlock().getState();
        if (StoragesManager.triggerSign(event.getPlayer(), sign.getLines(), event.getClickedBlock().getLocation(), event.getAction() == Action.LEFT_CLICK_BLOCK))
            event.setCancelled(true);
    }

    // TODO: Rework
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        StoragesManager.triggerItemClick(event);
        ItemStoragesManager.triggerItemWear(event.getPlayer());
        if (StoragesManager.triggerBlockClick(event)) event.setCancelled(true);
        if (StoragesManager.triggerButton(event)) event.setCancelled(true);
        if (StoragesManager.triggerPlate(event)) event.setCancelled(true);
        if (StoragesManager.triggerLever(event)) event.setCancelled(true);
        if (StoragesManager.triggerDoor(event)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Teleporter.startTeleport(event);
        StoragesManager.triggerCuboid(event.getPlayer());
        StoragesManager.triggerAllRegions(event.getPlayer(), event.getTo(), event.getFrom());
        Teleporter.stopTeleport(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Map<String, DataValue> changeables = StoragesManager.triggerInventoryClick(event);
        event.setCurrentItem(changeables.get(InventoryClickStorage.ITEM).asItemStack());
        event.setCancelled(changeables.get(Storage.CANCEL_EVENT).asBoolean());
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Map<String, DataValue> changeables = StoragesManager.triggerDrop(event.getPlayer(), event.getItemDrop(), event.getItemDrop().getPickupDelay());
        event.getItemDrop().setPickupDelay((int) changeables.get(DropStorage.PICKUP_DELAY).asDouble());
        event.getItemDrop().setItemStack(changeables.get(DropStorage.ITEM).asItemStack());
        event.setCancelled(changeables.get(Storage.CANCEL_EVENT).asBoolean());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlight(PlayerToggleFlightEvent event) {
        if (StoragesManager.triggerFlight(event.getPlayer(), event.isFlying())) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (StoragesManager.triggerEntityClick(event.getPlayer(), event.getRightClicked()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Map<String, DataValue> changeables = StoragesManager.triggerBlockBreak(event.getPlayer(), event.getBlock(), event.isDropItems());
        event.setDropItems(changeables.get(BlockBreakStorage.DO_DROP).asBoolean());
        event.setCancelled(changeables.get(Storage.CANCEL_EVENT).asBoolean());
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        StoragesManager.triggerSneak(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TemporaryOp.removeOp(event.getPlayer());
        event.setQuitMessage(StoragesManager.triggerQuit(event));
        MoveListener.removeLocation(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (StoragesManager.triggerGamemode(event.getPlayer(), event.getNewGameMode()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (StoragesManager.triggerWeatherChange(event.getWorld().getName(), event.toWeatherState()))
            event.setCancelled(true);
    }
}
