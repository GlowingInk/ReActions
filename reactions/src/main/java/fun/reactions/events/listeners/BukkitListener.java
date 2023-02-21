package fun.reactions.events.listeners;

import fun.reactions.ReActions;
import fun.reactions.events.PlayerAttacksEntityEvent;
import fun.reactions.events.PlayerPickupItemEvent;
import fun.reactions.externals.RaVault;
import fun.reactions.holders.PlayerRespawner;
import fun.reactions.holders.Teleporter;
import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.logic.environment.Variables;
import fun.reactions.module.basics.DetailsManager;
import fun.reactions.module.basics.activators.MessageActivator;
import fun.reactions.module.basics.activators.SignActivator;
import fun.reactions.module.basics.details.BlockBreakContext;
import fun.reactions.module.basics.details.DamageContext;
import fun.reactions.module.basics.details.DropContext;
import fun.reactions.module.basics.details.InventoryClickContext;
import fun.reactions.module.basics.details.MessageContext;
import fun.reactions.module.basics.details.MobDamageContext;
import fun.reactions.module.basics.details.QuitContext;
import fun.reactions.module.basics.details.TeleportContext;
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

import static fun.reactions.module.basics.ItemDetailsManager.triggerItemHold;
import static fun.reactions.module.basics.ItemDetailsManager.triggerItemWear;

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
        Optional<Variables> optVars = DetailsManager.triggerTeleport(
                event.getPlayer(),
                event.getCause(),
                event.getTo());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
        vars.getChanged(TeleportContext.LOCATION_TO, LocationUtils::parseLocation).ifPresent(event::setTo);
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getRightClicked().getType() != EntityType.ARMOR_STAND) return;
        if (DetailsManager.triggerMobClick(event.getPlayer(), (LivingEntity) event.getRightClicked()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        // TODO: That's not really good solution
        try {
            Optional<Variables> optVars = DetailsManager.triggerMessage(event.getPlayer(),
                    MessageActivator.Source.CHAT_INPUT,
                    event.getMessage());
            if (optVars.isEmpty()) return;
            Variables vars = optVars.get();
            vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
            vars.getChanged(MessageContext.MESSAGE).ifPresent(event::setMessage);
            // TODO: setFormat
        } catch (IllegalStateException ignore) {
            Msg.logOnce("asyncchaterror", "Chat is in async thread. Because of that you should use " +
                    "additional EXEC activator in some cases, like teleportation, setting blocks etc.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        if (DetailsManager.triggerPrecommand(null, event.getSender(), event.getCommand()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (DetailsManager.triggerPrecommand(event.getPlayer(), event.getPlayer(), event.getMessage().substring(1)))
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
        if (DetailsManager.triggerItemHeld(event.getPlayer(), event.getNewSlot(), event.getPreviousSlot()))
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
        DetailsManager.triggerPvpKill(event);
        DetailsManager.triggerPvpDeath(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (DetailsManager.triggerItemConsume(event))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerClickMob(PlayerInteractEntityEvent event) {
        DetailsManager.triggerItemClick(event);
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        DetailsManager.triggerMobClick(event.getPlayer(), (LivingEntity) event.getRightClicked());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // TODO: Set respawn location
        PlayerRespawner.triggerPlayerRespawn(event.getPlayer(), event.getRespawnLocation());
        DetailsManager.triggerAllRegions(event.getPlayer(), event.getRespawnLocation(), event.getPlayer().getLocation());
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

        DetailsManager.triggerMobKill(killer, event.getEntity());
        if (event.getEntity().hasMetadata("ReActions-money") && RaVault.isEconomyConnected()) {
            int money = Rng.nextIntRanged(event.getEntity().getMetadata("ReActions-money").get(0).asString());
            RaVault.creditAccount(killer.getName(), "", Double.toString(money), "");
            Msg.MSG_MOBBOUNTY.print(killer, 'e', '6', RaVault.format(money, ""), event.getEntity().getType().name());
        }
        if (event.getEntity().hasMetadata("ReActions-activator")) {
            String exec = event.getEntity().getMetadata("ReActions-activator").get(0).asString();
            DetailsManager.triggerExec(killer, exec, new Variables());
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
        Optional<Variables> optVars = DetailsManager.triggerMobDamage(event.getPlayer(), event.getEntity(), event.getDamage(), event.getFinalDamage(), event.getCause());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
        vars.getChanged(MobDamageContext.DAMAGE, NumberUtils::asDouble).ifPresent(event::setDamage);
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
            Optional<Variables> optVars = DetailsManager.triggerDamageByMob(evdmg);;
            if (optVars.isPresent()) {
                Variables vars = optVars.get();
                vars.getChanged(DamageContext.DAMAGE, NumberUtils::asDouble).ifPresent(event::setDamage);
                vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
            }
        } else if (event instanceof EntityDamageByBlockEvent evdmg) {
            source = "BLOCK";
            Block blockDamager = evdmg.getDamager();
            if (blockDamager != null) {
                Optional<Variables> optVars = DetailsManager.triggerDamageByBlock(evdmg, blockDamager);
                if (optVars.isPresent()) {
                    Variables vars = optVars.get();
                    vars.getChanged(DamageContext.DAMAGE, NumberUtils::asDouble).ifPresent(event::setDamage);
                    vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
                }
            }
        } else {
            source = "OTHER";
        }

        Optional<Variables> optVars = DetailsManager.triggerDamage(event, source);
        if (optVars.isPresent()) {
            Variables vars = optVars.get();
            vars.getChanged(DamageContext.DAMAGE, NumberUtils::asDouble).ifPresent(event::setDamage);
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

        DetailsManager.triggerJoin(player, !player.hasPlayedBefore());
        DetailsManager.triggerAllRegions(player, player.getLocation(), null);
        DetailsManager.triggerCuboid(player);
        triggerItemHold(player);
        triggerItemWear(player);
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!BlockUtils.isSign(event.getClickedBlock())) return;
        Sign sign = (Sign) event.getClickedBlock().getState();
        if (DetailsManager.triggerSign(event.getPlayer(), sign.getLines(), event.getClickedBlock().getLocation(), event.getAction() == Action.LEFT_CLICK_BLOCK))
            event.setCancelled(true);
    }

    // TODO: Rework
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        DetailsManager.triggerItemClick(event);
        triggerItemWear(event.getPlayer());
        if (
                DetailsManager.triggerPlate(event) ||
                (DetailsManager.triggerBlockClick(event) |
                DetailsManager.triggerButton(event)) ||
                DetailsManager.triggerLever(event) ||
                DetailsManager.triggerDoor(event)
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Teleporter.startTeleport(event);
        DetailsManager.triggerCuboid(event.getPlayer());
        DetailsManager.triggerAllRegions(event.getPlayer(), event.getTo(), event.getFrom());
        Teleporter.stopTeleport(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Optional<Variables> optVars = DetailsManager.triggerInventoryClick(event);
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.getChanged(InventoryClickContext.ITEM, VirtualItem::asItemStack).ifPresent(event::setCurrentItem);
        vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Optional<Variables> optVars = DetailsManager.triggerDrop(event.getPlayer(), event.getItemDrop(), event.getItemDrop().getPickupDelay());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
        vars.getChanged(DropContext.PICKUP_DELAY, NumberUtils::asInteger).ifPresent((d) -> event.getItemDrop().setPickupDelay(d));
        vars.getChanged(DropContext.ITEM, VirtualItem::asItemStack).ifPresent((i) -> event.getItemDrop().setItemStack(i));
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlight(PlayerToggleFlightEvent event) {
        if (DetailsManager.triggerFlight(event.getPlayer(), event.isFlying())) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (DetailsManager.triggerEntityClick(event.getPlayer(), event.getRightClicked()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Optional<Variables> optVars = DetailsManager.triggerBlockBreak(event.getPlayer(), event.getBlock(), event.isDropItems());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.getChanged(BlockBreakContext.DO_DROP, Boolean::parseBoolean).ifPresent(event::setDropItems);
        vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::parseBoolean).ifPresent(event::setCancelled);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        DetailsManager.triggerSneak(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TemporaryOp.removeOp(event.getPlayer());
        Optional<Variables> optVars = DetailsManager.triggerQuit(event);
        if (optVars.isPresent()){
            optVars.get().getChanged(QuitContext.QUIT_MESSAGE).ifPresent(event::setQuitMessage);
        }
        MoveListener.removeLocation(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (DetailsManager.triggerGamemode(event.getPlayer(), event.getNewGameMode()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (DetailsManager.triggerWeatherChange(event.getWorld().getName(), event.toWeatherState()))
            event.setCancelled(true);
    }
}
