package me.fromgate.reactions.events.listeners;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.events.PlayerAttacksEntityEvent;
import me.fromgate.reactions.events.PlayerPickupItemEvent;
import me.fromgate.reactions.externals.RaVault;
import me.fromgate.reactions.holders.PlayerRespawner;
import me.fromgate.reactions.holders.Teleporter;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.activators.MessageActivator;
import me.fromgate.reactions.module.basics.activators.SignActivator;
import me.fromgate.reactions.module.basics.details.BlockBreakDetails;
import me.fromgate.reactions.module.basics.details.DamageDetails;
import me.fromgate.reactions.module.basics.details.DropDetails;
import me.fromgate.reactions.module.basics.details.InventoryClickDetails;
import me.fromgate.reactions.module.basics.details.MessageDetails;
import me.fromgate.reactions.module.basics.details.MobDamageDetails;
import me.fromgate.reactions.module.basics.details.TeleportDetails;
import me.fromgate.reactions.time.waiter.WaitingManager;
import me.fromgate.reactions.util.BlockUtils;
import me.fromgate.reactions.util.Rng;
import me.fromgate.reactions.util.TemporaryOp;
import me.fromgate.reactions.util.Utils;
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
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Map;

import static me.fromgate.reactions.module.basics.DetailsManager.*;
import static me.fromgate.reactions.module.basics.ItemDetailsManager.triggerItemHold;
import static me.fromgate.reactions.module.basics.ItemDetailsManager.triggerItemWear;

public class BukkitListener implements Listener {
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
        Map<String, DataValue> changeables = triggerTeleport(
                event.getPlayer(),
                event.getCause(),
                event.getTo());
        if (changeables == null) return;
        event.setTo(changeables.get(TeleportDetails.LOCATION_TO).asLocation());
        event.setCancelled(changeables.get(Details.CANCEL_EVENT).asBoolean());
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getRightClicked().getType() != EntityType.ARMOR_STAND) return;
        if (triggerMobClick(event.getPlayer(), (LivingEntity) event.getRightClicked()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        // TODO: That's not really good solution
        try {
            Map<String, DataValue> changeables = triggerMessage(event.getPlayer(),
                    MessageActivator.Source.CHAT_INPUT,
                    event.getMessage());
            if (changeables == null) return;
            event.setMessage(changeables.get(MessageDetails.MESSAGE).asString());
            event.setCancelled(changeables.get(Details.CANCEL_EVENT).asBoolean());
        } catch (IllegalStateException ignore) {
            Msg.logOnce("asyncchaterror", "Chat is in async thread. Because of that you should use " +
                    "additional EXEC activator in some cases, like teleportation, setting blocks etc.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        if (triggerPrecommand(null, event.getSender(), event.getCommand()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (triggerPrecommand(event.getPlayer(), event.getPlayer(), event.getMessage().substring(1)))
            event.setCancelled(true);
    }

    // TODO: All the checks should be inside activator
    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        for (Activator activator : ReActions.getActivatorTypes().get(SignActivator.class).getActivators()) {
            SignActivator signAct = (SignActivator) activator;
            if (!signAct.checkMask(event.getLines())) continue;
            Msg.MSG_SIGNFORBIDDEN.print(event.getPlayer(), '4', 'c', signAct.getLogic().getName());
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (triggerItemHeld(event.getPlayer(), event.getNewSlot(), event.getPreviousSlot()))
            event.setCancelled(true);
        else {
            triggerItemHold(event.getPlayer());
            triggerItemWear(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryInteract(InventoryInteractEvent event) {
        triggerItemHold((Player) event.getWhoClicked());
        triggerItemWear((Player) event.getWhoClicked());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        triggerItemHold((Player) event.getPlayer());
        triggerItemWear((Player) event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerRespawner.addPlayerRespawn(event);
        triggerPvpKill(event);
        triggerPvpDeath(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (triggerItemConsume(event))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerClickMob(PlayerInteractEntityEvent event) {
        triggerItemClick(event);
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        triggerMobClick(event.getPlayer(), (LivingEntity) event.getRightClicked());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // TODO: Set respawn location
        PlayerRespawner.triggerPlayerRespawn(event.getPlayer(), event.getRespawnLocation());
        triggerAllRegions(event.getPlayer(), event.getRespawnLocation(), event.getPlayer().getLocation());
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

        Player killer = EntityUtils.getKillerPlayer(event.getEntity().getLastDamageCause());
        if (killer == null) return;

        triggerMobKill(killer, event.getEntity());
        if (event.getEntity().hasMetadata("ReActions-money") && RaVault.isEconomyConnected()) {
            int money = Rng.nextIntRanged(event.getEntity().getMetadata("ReActions-money").get(0).asString());
            RaVault.creditAccount(killer.getName(), "", Double.toString(money), "");
            Msg.MSG_MOBBOUNTY.print(killer, 'e', '6', RaVault.format(money, ""), event.getEntity().getType().name());
        }
        if (event.getEntity().hasMetadata("ReActions-activator")) {
            String exec = event.getEntity().getMetadata("ReActions-activator").get(0).asString();
            triggerExec(killer, exec, null);
        }

    }

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
        Map<String, DataValue> changeables = triggerMobDamage(event.getPlayer(), event.getEntity(), event.getDamage(), event.getCause());
        if (changeables == null) return;
        event.setDamage(changeables.get(MobDamageDetails.DAMAGE).asDouble());
        event.setCancelled(changeables.get(Details.CANCEL_EVENT).asBoolean());
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
            Map<String, DataValue> changeables = triggerDamageByMob(evdmg);
            if (changeables != null) {
                event.setDamage(changeables.get(DamageDetails.DAMAGE).asDouble());
                event.setCancelled(changeables.get(Details.CANCEL_EVENT).asBoolean());
            }
        } else if (event instanceof EntityDamageByBlockEvent evdmg) {
            source = "BLOCK";
            Block blockDamager = evdmg.getDamager();
            if (blockDamager != null) {
                Map<String, DataValue> changeables = triggerDamageByBlock(evdmg, blockDamager);
                if (changeables != null) {
                    event.setDamage(changeables.get(DamageDetails.DAMAGE).asDouble());
                    event.setCancelled(changeables.get(Details.CANCEL_EVENT).asBoolean());
                }
            }
        } else {
            source = "OTHER";
        }

        Map<String, DataValue> changeables = triggerDamage(event, source);
        if (changeables == null) return;
        event.setDamage(changeables.get(DamageDetails.DAMAGE).asDouble());
        event.setCancelled(changeables.get(Details.CANCEL_EVENT).asBoolean());
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

        triggerJoin(player, !player.hasPlayedBefore());
        triggerAllRegions(player, player.getLocation(), null);
        triggerCuboid(player);
        triggerItemHold(player);
        triggerItemWear(player);
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!BlockUtils.isSign(event.getClickedBlock())) return;
        Sign sign = (Sign) event.getClickedBlock().getState();
        if (triggerSign(event.getPlayer(), sign.getLines(), event.getClickedBlock().getLocation(), event.getAction() == Action.LEFT_CLICK_BLOCK))
            event.setCancelled(true);
    }

    // TODO: Rework
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        triggerItemClick(event);
        triggerItemWear(event.getPlayer());
        if (triggerBlockClick(event)) event.setCancelled(true);
        if (triggerButton(event)) event.setCancelled(true);
        if (triggerPlate(event)) event.setCancelled(true);
        if (triggerLever(event)) event.setCancelled(true);
        if (triggerDoor(event)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Teleporter.startTeleport(event);
        triggerCuboid(event.getPlayer());
        triggerAllRegions(event.getPlayer(), event.getTo(), event.getFrom());
        Teleporter.stopTeleport(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Map<String, DataValue> changeables = triggerInventoryClick(event);
        if (changeables == null) return;
        event.setCurrentItem(changeables.get(InventoryClickDetails.ITEM).asItemStack());
        event.setCancelled(changeables.get(Details.CANCEL_EVENT).asBoolean());
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Map<String, DataValue> changeables = triggerDrop(event.getPlayer(), event.getItemDrop(), event.getItemDrop().getPickupDelay());
        if (changeables == null) return;
        event.getItemDrop().setPickupDelay((int) changeables.get(DropDetails.PICKUP_DELAY).asDouble());
        event.getItemDrop().setItemStack(changeables.get(DropDetails.ITEM).asItemStack());
        event.setCancelled(changeables.get(Details.CANCEL_EVENT).asBoolean());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlight(PlayerToggleFlightEvent event) {
        if (triggerFlight(event.getPlayer(), event.isFlying())) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (triggerEntityClick(event.getPlayer(), event.getRightClicked()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Map<String, DataValue> changeables = triggerBlockBreak(event.getPlayer(), event.getBlock(), event.isDropItems());
        if (changeables == null) return;
        event.setDropItems(changeables.get(BlockBreakDetails.DO_DROP).asBoolean());
        event.setCancelled(changeables.get(Details.CANCEL_EVENT).asBoolean());
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        triggerSneak(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TemporaryOp.removeOp(event.getPlayer());
        event.setQuitMessage(triggerQuit(event));
        MoveListener.removeLocation(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (triggerGamemode(event.getPlayer(), event.getNewGameMode()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (triggerWeatherChange(event.getWorld().getName(), event.toWeatherState()))
            event.setCancelled(true);
    }
}
