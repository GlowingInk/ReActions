package fun.reactions.events.listeners;

import fun.reactions.ReActions;
import fun.reactions.events.PlayerAttacksEntityEvent;
import fun.reactions.events.PlayerPickupItemEvent;
import fun.reactions.holders.PlayerRespawner;
import fun.reactions.holders.Teleporter;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basic.activators.BlockBreakActivator;
import fun.reactions.module.basic.activators.DamageActivator;
import fun.reactions.module.basic.activators.DropActivator;
import fun.reactions.module.basic.activators.InventoryClickActivator;
import fun.reactions.module.basic.activators.MessageActivator;
import fun.reactions.module.basic.activators.MobDamageActivator;
import fun.reactions.module.basic.activators.QuitActivator;
import fun.reactions.module.basic.activators.SignActivator;
import fun.reactions.module.basic.activators.TeleportActivator;
import fun.reactions.module.vault.external.RaVault;
import fun.reactions.util.BlockUtils;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.Rng;
import fun.reactions.util.TemporaryOp;
import fun.reactions.util.Utils;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.message.Msg;
import fun.reactions.util.message.RaDebug;
import fun.reactions.util.mob.EntityUtils;
import fun.reactions.util.mob.MobSpawn;
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
import java.util.Optional;

import static fun.reactions.module.basic.ContextManager.*;
import static fun.reactions.module.basic.ItemContextManager.triggerItemHold;
import static fun.reactions.module.basic.ItemContextManager.triggerItemWear;

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
                event.getFinalDamage(),
                event.getCause());
        Bukkit.getPluginManager().callEvent(plEvent);
        event.setDamage(plEvent.getDamage());
        event.setCancelled(plEvent.isCancelled());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Optional<Variables> optVars = triggerTeleport(
                event.getPlayer(),
                event.getCause(),
                event.getTo());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
        vars.getChanged(TeleportActivator.Context.LOCATION_TO, LocationUtils::parseLocation).ifPresent(event::setTo);
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
            Optional<Variables> optVars = triggerMessage(event.getPlayer(),
                    MessageActivator.Source.CHAT_INPUT,
                    event.getMessage());
            if (optVars.isEmpty()) return;
            Variables vars = optVars.get();
            vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
            vars.getChanged(MessageActivator.Context.MESSAGE).ifPresent(event::setMessage);
            // TODO: setFormat
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
            triggerFunction(killer, exec, new Variables());
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
        Optional<Variables> optVars = triggerMobDamage(event.getPlayer(), event.getEntity(), event.getDamage(), event.getFinalDamage(), event.getCause());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
        vars.getChanged(MobDamageActivator.MobDamageContext.DAMAGE, NumberUtils::asDouble).ifPresent(event::setDamage);
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
        if (event.getEntity().getType() != EntityType.PLAYER) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.CUSTOM && Math.round(event.getDamage()) == 0) return; // FIXME Why?
        String source;
        if (event instanceof EntityDamageByEntityEvent evdmg) {
            source = "ENTITY";
            Optional<Variables> optVars = triggerDamageByMob(evdmg);;
            if (optVars.isPresent()) {
                Variables vars = optVars.get();
                vars.getChanged(DamageActivator.Context.DAMAGE, NumberUtils::asDouble).ifPresent(event::setDamage);
                vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
            }
        } else if (event instanceof EntityDamageByBlockEvent evdmg) {
            source = "BLOCK";
            Block blockDamager = evdmg.getDamager();
            if (blockDamager != null) {
                Optional<Variables> optVars = triggerDamageByBlock(evdmg, blockDamager);
                if (optVars.isPresent()) {
                    Variables vars = optVars.get();
                    vars.getChanged(DamageActivator.Context.DAMAGE, NumberUtils::asDouble).ifPresent(event::setDamage);
                    vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
                }
            }
        } else {
            source = "OTHER";
        }

        Optional<Variables> optVars = triggerDamage(event, source);
        if (optVars.isPresent()) {
            Variables vars = optVars.get();
            vars.getChanged(DamageActivator.Context.DAMAGE, NumberUtils::asDouble).ifPresent(event::setDamage);
            vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
        }
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
        triggerAllRegions(event.getPlayer(), event.getTo(), event.getFrom());
        Teleporter.stopTeleport(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Optional<Variables> optVars = triggerInventoryClick(event);
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.getChanged(InventoryClickActivator.Context.ITEM, VirtualItem::asItemStack).ifPresent(event::setCurrentItem);
        vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Optional<Variables> optVars = triggerDrop(event.getPlayer(), event.getItemDrop(), event.getItemDrop().getPickupDelay());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
        vars.getChanged(DropActivator.Context.PICKUP_DELAY, NumberUtils::asInteger).ifPresent((d) -> event.getItemDrop().setPickupDelay(d));
        vars.getChanged(DropActivator.Context.ITEM, VirtualItem::asItemStack).ifPresent((i) -> event.getItemDrop().setItemStack(i));
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
        Optional<Variables> optVars = triggerBlockBreak(event.getPlayer(), event.getBlock(), event.isDropItems());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.getChanged(BlockBreakActivator.Context.DO_DROP, Boolean::parseBoolean).ifPresent(event::setDropItems);
        vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::parseBoolean).ifPresent(event::setCancelled);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        triggerSneak(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TemporaryOp.removeOp(event.getPlayer());
        Optional<Variables> optVars = triggerQuit(event);
        optVars.flatMap(variables -> variables.getChanged(QuitActivator.Context.QUIT_MESSAGE)).ifPresent(event::setQuitMessage);
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
