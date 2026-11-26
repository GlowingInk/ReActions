package fun.reactions.events.listeners;

import fun.reactions.ReActions;
import fun.reactions.holders.PlayerRespawner;
import fun.reactions.holders.Teleporter;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basic.activators.*;
import fun.reactions.module.vault.external.RaVault;
import fun.reactions.util.Rng;
import fun.reactions.util.TemporaryOp;
import fun.reactions.util.Utils;
import fun.reactions.util.block.BlockUtils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.mob.EntityUtils;
import fun.reactions.util.mob.MobSpawn;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static fun.reactions.module.basic.ContextManager.*;
import static fun.reactions.module.basic.ItemContextManager.triggerItemHold;
import static fun.reactions.module.basic.ItemContextManager.triggerItemWear;

public class BukkitListener implements Listener {
    private final ReActions.Platform platform;
    private final MoveListener moveListener;

    public BukkitListener(@NotNull ReActions.Platform platform, @NotNull MoveListener moveListener) {
        this.platform = platform;
        this.moveListener = moveListener;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Item item = event.getItem();
        triggerPickupItem(player, item, item.getPickupDelay()).ifPresent(vars -> applyItemChanges(vars, item, event));
        if (event.isCancelled()) return;
        triggerItemHold(player);
        triggerItemWear(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(EntityUtils.getDamagerEntity(event) instanceof Player damager)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        playGrowl(damager);
        if (target.getType() == EntityType.PLAYER) {
            long time = System.currentTimeMillis();
            damager.setMetadata("reactions-pvp-time", new FixedMetadataValue(platform.getPlugin(), time));
            target.setMetadata("reactions-pvp-time", new FixedMetadataValue(platform.getPlugin(), time));
        }
        var optVars = triggerMobDamage(damager, target, event.getDamage(), event.getFinalDamage(), event.getCause());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.changedBoolean(ActivationContext.CANCEL_EVENT).ifPresent(event::setCancelled);
        vars.changedDouble(MobDamageActivator.MobDamageContext.DAMAGE).ifPresent(event::setDamage);
    }

    private static void playGrowl(LivingEntity entity) {
        if (!entity.hasMetadata("ReActions-growl")) return;
        String growl = entity.getMetadata("ReActions-growl").getFirst().asString();
        if (Utils.isStringEmpty(growl)) return;
        Utils.soundPlay(entity.getLocation(), growl);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        var optVars = triggerTeleport(
                event.getPlayer(),
                event.getCause(),
                event.getTo());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.changedBoolean(ActivationContext.CANCEL_EVENT).ifPresent(event::setCancelled);
        vars.changed(TeleportActivator.Context.LOCATION_TO, LocationUtils::parseLocation).ifPresent(event::setTo);
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
            var optVars = triggerMessage(event.getPlayer(),
                    MessageActivator.Source.CHAT_INPUT,
                    event.getMessage());
            if (optVars.isEmpty()) return;
            Variables vars = optVars.get();
            vars.changedBoolean(ActivationContext.CANCEL_EVENT).ifPresent(event::setCancelled);
            vars.changedString(MessageActivator.Context.MESSAGE).ifPresent(event::setMessage);
            // TODO: setFormat
        } catch (IllegalStateException ignore) {
            platform.warnOnce(
                    "async_chat",
                    "Chat is in async thread. Because of that you should use " +
                    "additional EXEC activator in some cases, like teleportation, setting blocks etc."
            );
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

    @EventHandler(ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (triggerItemHeld(event.getPlayer(), event.getNewSlot(), event.getPreviousSlot())) {
            event.setCancelled(true);
        } else {
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
        if (triggerItemClick(event)) {
            event.setCancelled(true);
        }
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        triggerMobClick(event.getPlayer(), (LivingEntity) event.getRightClicked());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // TODO: Set respawn location
        PlayerRespawner.triggerPlayerRespawn(event.getPlayer(), event.getRespawnLocation());
        platform.getMovement().onMove(event.getPlayer(), event.getPlayer().getLocation(), event.getRespawnLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        List<ItemStack> stacks = MobSpawn.getMobDrop(event.getEntity());
        if (stacks != null && !stacks.isEmpty()) {
            event.getDrops().clear();
            event.getDrops().addAll(stacks);
        }

        if (event.getEntity().hasMetadata("ReActions-xp")) {
            int xp = Rng.nextIntRanged(event.getEntity().getMetadata("ReActions-xp").getFirst().asString());
            event.setDroppedExp(xp);
        }

        Player killer = EntityUtils.getKillerPlayer(event.getEntity().getLastDamageCause());
        if (killer == null) return;

        triggerMobKill(killer, event.getEntity());
        if (event.getEntity().hasMetadata("ReActions-money") && RaVault.isEconomyConnected()) {
            int money = Rng.nextIntRanged(event.getEntity().getMetadata("ReActions-money").getFirst().asString());
            RaVault.creditAccount(killer.getName(), "", Double.toString(money), "");
        }
        if (event.getEntity().hasMetadata("ReActions-activator")) {
            String exec = event.getEntity().getMetadata("ReActions-activator").getFirst().asString();
            triggerFunction(killer, exec, new Variables());
        }

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageByMob(EntityDamageByEntityEvent event) {
        LivingEntity damager = EntityUtils.getDamagerEntity(event);
        if (damager == null || !damager.hasMetadata("ReActions-dmg")) return;
        double dmg = damager.getMetadata("ReActions-dmg").getFirst().asDouble();
        if (dmg < 0) return;
        event.setDamage(event.getDamage() * dmg);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.CUSTOM && Math.round(event.getDamage()) == 0) return; // FIXME Why?
        String source;
        if (event instanceof EntityDamageByEntityEvent evdmg) {
            source = "ENTITY";
            var optVars = triggerDamageByMob(evdmg);
            if (optVars.isPresent()) {
                Variables vars = optVars.get();
                vars.changedDouble(DamageActivator.Context.DAMAGE).ifPresent(event::setDamage);
                vars.changedBoolean(ActivationContext.CANCEL_EVENT).ifPresent(event::setCancelled);
            }
        } else if (event instanceof EntityDamageByBlockEvent evdmg) {
            source = "BLOCK";
            Block blockDamager = evdmg.getDamager();
            if (blockDamager != null) {
                var optVars = triggerDamageByBlock(evdmg, blockDamager);
                if (optVars.isPresent()) {
                    Variables vars = optVars.get();
                    vars.changedDouble(DamageActivator.Context.DAMAGE).ifPresent(event::setDamage);
                    vars.changedBoolean(ActivationContext.CANCEL_EVENT).ifPresent(event::setCancelled);
                }
            }
        } else {
            source = "OTHER";
        }

        var optVars = triggerDamage(event, source);
        if (optVars.isPresent()) {
            Variables vars = optVars.get();
            vars.changedDouble(DamageActivator.Context.DAMAGE).ifPresent(event::setDamage);
            vars.changedBoolean(ActivationContext.CANCEL_EVENT).ifPresent(event::setCancelled);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobCry(EntityDamageEvent event) {
        if ((event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) && (event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE))
            return;
        if (event.getEntityType() == EntityType.PLAYER) return;
        if (!(event.getEntity() instanceof LivingEntity le)) return;
        if (!le.hasMetadata("ReActions-cry")) return;
        String cry = le.getMetadata("ReActions-cry").getFirst().asString();
        if (cry.isEmpty()) return;
        if (!(event instanceof EntityDamageByEntityEvent evdmg)) return;
        if (evdmg.getDamager() instanceof Projectile prj) {
            LivingEntity shooter = EntityUtils.getEntityFromProjectile(prj.getShooter());
            if (shooter == null) return;
            if (!(shooter instanceof Player)) return;
        } else if (evdmg.getDamager().getType() != EntityType.PLAYER) return;
        Utils.soundPlay(le.getLocation(), cry);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        TemporaryOp.removeOp(player);
        moveListener.initLocation(player);

        triggerJoin(player, !player.hasPlayedBefore());
        platform.getMovement().onMove(player, null, player.getLocation());
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
        if (triggerItemClick(event)) {
            event.setCancelled(true);
        }
        triggerItemWear(event.getPlayer());
        if (
                triggerPlate(event) ||
                (triggerBlockClick(event) |
                triggerButton(event)) ||
                triggerLever(event) ||
                triggerDoor(event)
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Teleporter.startTeleport(event);
        triggerCuboid(event.getPlayer());
        platform.getMovement().onMove(event.getPlayer(), event.getFrom(), event.getTo());
        Teleporter.stopTeleport(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        var optVars = triggerInventoryClick(event);
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.changed(InventoryClickActivator.Context.ITEM, platform.getItems()::asItemStack).ifPresent(event::setCurrentItem);
        vars.changedBoolean(ActivationContext.CANCEL_EVENT).ifPresent(event::setCancelled);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        triggerDrop(event.getPlayer(), item, item.getPickupDelay()).ifPresent(vars -> applyItemChanges(vars, item, event));
    }

    private void applyItemChanges(Variables vars, Item item, Cancellable event) {
        vars.changedBoolean(ActivationContext.CANCEL_EVENT).ifPresent(event::setCancelled);
        vars.changedInt(DropActivator.Context.PICKUP_DELAY).ifPresent(item::setPickupDelay);
        vars.changed(DropActivator.Context.ITEM, platform.getItems()::asItemStack).ifPresent(item::setItemStack);
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
        var optVars = triggerBlockBreak(event.getPlayer(), event.getBlock(), event.isDropItems());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.changedBoolean(BlockBreakActivator.Context.DO_DROP).ifPresent(event::setDropItems);
        vars.changedBoolean(ActivationContext.CANCEL_EVENT).ifPresent(event::setCancelled);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        triggerSneak(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TemporaryOp.removeOp(event.getPlayer());
        var optVars = triggerQuit(event);
        optVars.flatMap(variables -> variables.changedString(QuitActivator.Context.QUIT_MESSAGE)).ifPresent(event::setQuitMessage);
        moveListener.removeLocation(event.getPlayer());
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
