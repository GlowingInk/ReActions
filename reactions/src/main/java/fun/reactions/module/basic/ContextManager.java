/*
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *
 *  This file is part of ReActions.
 *
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReActions is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more context.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 *
 */

package fun.reactions.module.basic;

import fun.reactions.Cfg;
import fun.reactions.ReActions;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basic.activators.*;
import fun.reactions.module.worldguard.activators.RegionActivator;
import fun.reactions.module.worldguard.activators.RegionEnterActivator;
import fun.reactions.module.worldguard.activators.RegionLeaveActivator;
import fun.reactions.module.worldguard.external.RaWorldGuard;
import fun.reactions.util.BlockUtils;
import fun.reactions.util.enums.DeathCause;
import fun.reactions.util.message.Msg;
import fun.reactions.util.mob.EntityUtils;
import fun.reactions.util.parameter.Parameters;
import fun.reactions.util.time.TimeUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// TODO: Refactor to DetailsFactory
public final class ContextManager {

    private ContextManager() {}

    public static @NotNull Optional<Variables> triggerTeleport(Player player, TeleportCause cause, Location to) {
        TeleportActivator.Context context = new TeleportActivator.Context(player, cause, to);
        activate(context);
        return context.getVariables();
    }

    public static boolean triggerPrecommand(Player player, CommandSender sender, String fullCommand) {
        CommandActivator.Context context = new CommandActivator.Context(player, sender, fullCommand);
        return context.isCancelled();
    }

    public static boolean triggerMobClick(Player player, LivingEntity mob) {
        if (mob == null) return false;
        MobClickActivator.Context e = new MobClickActivator.Context(player, mob);
        activate(e);
        return e.isCancelled();
    }

    public static void triggerMobKill(Player player, LivingEntity mob) {
        if (mob == null) return;
        MobKillActivator.Context e = new MobKillActivator.Context(player, mob);
        activate(e);
    }

    public static void triggerJoin(Player player, boolean joinfirst) {
        JoinActivator.Context e = new JoinActivator.Context(player, joinfirst);
        activate(e);
    }

    public static boolean triggerDoor(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
        if (!BlockUtils.isOpenable(event.getClickedBlock()) || event.getHand() != EquipmentSlot.HAND) return false;
        DoorActivator.Context e = new DoorActivator.Context(event.getPlayer(), BlockUtils.getBottomDoor(event.getClickedBlock()));
        activate(e);
        return e.isCancelled();
    }

    public static boolean triggerItemConsume(PlayerItemConsumeEvent event) {
        ConsumeActivator.Context ce = new ConsumeActivator.Context(event.getPlayer(), event.getItem(), event.getHand());
        activate(ce);
        return ce.isCancelled();
    }

    public static boolean triggerItemClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemClickActivator.Context context = new ItemClickActivator.Context(
                player,
                event.getHand() == EquipmentSlot.HAND
                        ? player.getInventory().getItemInMainHand()
                        : player.getInventory().getItemInOffHand(),
                event.getHand());
        activate(context);
        return context.isCancelled();
    }

    public static boolean triggerItemClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
        ItemClickActivator.Context context = new ItemClickActivator.Context(event.getPlayer(), event.getItem(), event.getHand());
        activate(context);
        return context.isCancelled();
    }


    public static boolean triggerLever(PlayerInteractEvent event) {
        if (!((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.LEFT_CLICK_BLOCK)))
            return false;
        if (event.getHand() != EquipmentSlot.HAND) return false;
        if (event.getClickedBlock().getType() != Material.LEVER) return false;
        LeverActivator.Context e = new LeverActivator.Context(event.getPlayer(), event.getClickedBlock());
        activate(e);
        return e.isCancelled();
    }

    // PVP Kill Event
    public static void triggerPvpKill(PlayerDeathEvent event) {
        Player deadplayer = event.getEntity();
        Player killer = EntityUtils.getKillerPlayer(deadplayer.getLastDamageCause());
        if (killer == null) return;
        PvpKillActivator.PvpKillContext pe = new PvpKillActivator.PvpKillContext(killer, deadplayer);
        activate(pe);
    }

    // PVP Death Event
    public static void triggerPvpDeath(PlayerDeathEvent event) {
        Player deadplayer = event.getEntity();
        LivingEntity killer = EntityUtils.getKillerEntity(deadplayer.getLastDamageCause());
        DeathCause ds = (killer == null) ? DeathCause.OTHER : (killer instanceof Player) ? DeathCause.PVP : DeathCause.PVE;
        DeathActivator.Context pe = new DeathActivator.Context(killer, deadplayer, ds);
        activate(pe);
    }

    // Button Event
    public static boolean triggerButton(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return false;
        Block block = event.getClickedBlock();
        if (block == null || !Tag.BUTTONS.isTagged(block.getType())) return false;
        Switch button = (Switch) block.getBlockData();
        if (button.isPowered()) return false;
        ButtonActivator.Context be = new ButtonActivator.Context(event.getPlayer(), event.getClickedBlock().getLocation());
        activate(be);
        return be.isCancelled();
    }

    public static boolean triggerSign(Player player, String[] lines, Location loc, boolean leftClick) {
        for (Activator act : ReActions.getActivatorTypes().get(SignActivator.class).getActivators()) {
            SignActivator sign = (SignActivator) act;
            if (sign.checkMask(lines)) {
                SignActivator.SignContext se = new SignActivator.SignContext(player, lines, loc, leftClick);
                activate(se);
                return se.isCancelled();
            }
        }
        return false;
    }

    // TODO: I think all of it should be inside ActionExecute class

    @Deprecated
    public static boolean triggerFunction(CommandSender sender, String param) {
        if (param.isEmpty()) return false;
        return triggerFunction(sender, Parameters.fromString(param, "player"));
    }

    @Deprecated
    public static boolean triggerFunction(CommandSender sender, Parameters param) {
        return triggerFunction(sender, param, new Variables());
    }

    @Deprecated
    public static boolean triggerFunction(CommandSender sender, Parameters param, Variables vars) {
        if (param.isEmpty()) return false;
        final Player senderPlayer = (sender instanceof Player) ? (Player) sender : null;
        final String id = param.getString("activator", param.getString("exec"));
        if (id.isEmpty()) return false;
        Activator act = ReActions.getActivators().getActivator(id);
        if (act == null) {
            Msg.logOnce("wrongact_" + id, "Failed to run exec activator " + id + ". Activator not found.");
            return false;
        }
        if (act.getClass() != FunctionActivator.class) {
            Msg.logOnce("wrongactype_" + id, "Failed to run exec activator " + id + ". Wrong activator type.");
            return false;
        }

        int repeat = Math.min(param.getInteger("repeat", 1), 1);

        long delay = TimeUtils.timeToTicksSafe(param.getTime("delay", 50));

        final Set<Player> target = new HashSet<>();

        if (param.contains("player")) {
            target.addAll(ReActions.getSelectors().getPlayers(Parameters.fromString(param.getString("player"), "player")));
        }
        target.addAll(ReActions.getSelectors().getPlayers(param));   // Оставляем для совместимости со старым вариантом

        if (target.isEmpty() && !param.containsAny(ReActions.getSelectors().getAllKeys())) target.add(senderPlayer);

        for (int i = 0; i < repeat; i++) {
            Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
                for (Player player : target) {
                    // if (ReActions.getActivators().isStopped(player, id, true)) continue;
                    FunctionActivator.LegacyContext ce = new FunctionActivator.LegacyContext(player, vars);
                    // TODO Custom ActivatorType for Exec
                    ReActions.getActivators().activate(ce, id);
                }
            }, delay * repeat);
        }
        return true;
    }

    @Deprecated
    public static boolean triggerFunction(CommandSender sender, String id, Variables vars) {
        final Player player = (sender instanceof Player) ? (Player) sender : null;
        Activator act = ReActions.getActivators().getActivator(id);
        if (act == null) {
            Msg.logOnce("wrongact_" + id, "Failed to run exec activator " + id + ". Activator not found.");
            return false;
        }
        if (act.getClass() != FunctionActivator.class) {
            Msg.logOnce("wrongactype_" + id, "Failed to run exec activator " + id + ". Wrong activator type.");
            return false;
        }
        // TODO Custom ActivatorType to handle exec stopping
        // if (ReActions.getActivators().isStopped(player, id, true)) return true;
        FunctionActivator.LegacyContext ce = new FunctionActivator.LegacyContext(player, vars);
        ReActions.getActivators().activate(ce, id);
        return true;
    }

    public static boolean triggerPlate(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return false;
        // TODO EnumSet Plates?
        if (!(event.getClickedBlock().getType().name().endsWith("_PRESSURE_PLATE"))) return false;
        PlateActivator.PlateContext pe = new PlateActivator.PlateContext(event.getPlayer(), event.getClickedBlock().getLocation());
        activate(pe);
        return pe.isCancelled();
    }

    public static void triggerCuboid(final Player player) {
        ReActions.getActivators().activate(new CuboidActivator.Context(player));
    }

    public static void triggerAllRegions(final Player player, final Location to, final Location from) {
        if (!RaWorldGuard.isConnected()) return;
        Bukkit.getScheduler().runTaskLaterAsynchronously(ReActions.getPlugin(), () -> {

            final Set<String> regionsTo = RaWorldGuard.getRegions(to);
            final Set<String> regionsFrom = RaWorldGuard.getRegions(from);

            Bukkit.getScheduler().runTask(ReActions.getPlugin(), () -> {
                triggerRegion(player, regionsTo);
                triggerRgEnter(player, regionsTo, regionsFrom);
                triggerRgLeave(player, regionsTo, regionsFrom);
            });
        }, 1);
    }

    private static void triggerRgEnter(Player player, Set<String> regionTo, Set<String> regionFrom) {
        if (regionTo.isEmpty()) return;
        for (String rg : regionTo)
            if (!regionFrom.contains(rg)) {
                RegionEnterActivator.Context wge = new RegionEnterActivator.Context(player, rg);
                activate(wge);
            }
    }

    private static void triggerRgLeave(Player player, Set<String> regionTo, Set<String> regionFrom) {
        if (regionFrom.isEmpty()) return;
        for (String rg : regionFrom)
            if (!regionTo.contains(rg)) {
                RegionLeaveActivator.Context wge = new RegionLeaveActivator.Context(player, rg);
                activate(wge);
            }
    }

    private static void triggerRegion(Player player, Set<String> to) {
        if (to.isEmpty()) return;
        for (String region : to) {
            setFutureRegionCheck(player.getName(), region, false);
        }
    }

    private static void setFutureRegionCheck(final String playerName, final String region, boolean repeat) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return;
        if (!player.isOnline()) return;
        if (player.isDead()) return;
        if (!RaWorldGuard.isPlayerInRegion(player, region)) return;
        String rg = "rg-" + region;
        if (!isTimeToRaiseEvent(player, rg, Cfg.worldguardRecheck, repeat)) return;

        RegionActivator.Context wge = new RegionActivator.Context(player, region);
        activate(wge);

        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> setFutureRegionCheck(playerName, region, true), 20L * Cfg.worldguardRecheck);
    }

    public static boolean isTimeToRaiseEvent(Player p, String id, int seconds, boolean repeat) {
        long curTime = System.currentTimeMillis();
        long prevTime = p.hasMetadata("reactions-rchk-" + id) ? p.getMetadata("reactions-rchk-" + id).get(0).asLong() : 0;
        boolean needUpdate = repeat || ((curTime - prevTime) >= (1000L * seconds));
        if (needUpdate) p.setMetadata("reactions-rchk-" + id, new FixedMetadataValue(ReActions.getPlugin(), curTime));
        return needUpdate;
    }

    // FIXME: Refactor, should not be this way
    public static @NotNull Optional<Variables> triggerMessage(CommandSender sender, MessageActivator.Source source, String message) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        for (Activator act : ReActions.getActivatorTypes().get(MessageActivator.class).getActivators()) {
            MessageActivator a = (MessageActivator) act;
            if (a.filterMessage(source, message)) {
                MessageActivator.Context me = new MessageActivator.Context(player, a, message);
                activate(me);
                return me.getVariables();
            }
        }
        return Optional.empty();
    }

    public static void triggerVariable(String var, String playerName, String newValue, String prevValue) {
        if (newValue.equalsIgnoreCase(prevValue)) return;
        Player player = Bukkit.getPlayerExact(playerName);
        if (!playerName.isEmpty() && player == null) return;
        VariableActivator.Context ve = new VariableActivator.Context(player, var, newValue, prevValue);
        activate(ve);
    }

    public static @NotNull Optional<Variables> triggerMobDamage(Player damager, LivingEntity entity, double damage, double finalDamage, EntityDamageEvent.DamageCause cause) {
        MobDamageActivator.MobDamageContext mde = new MobDamageActivator.MobDamageContext(entity, damager, cause, damage, finalDamage);
        activate(mde);
        return mde.getVariables();
    }

    public static @NotNull Optional<Variables> triggerQuit(PlayerQuitEvent event) {
        QuitActivator.Context qu = new QuitActivator.Context(event.getPlayer(), event.getQuitMessage());
        activate(qu);
        return qu.getVariables();
    }

    public static boolean triggerBlockClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return false;
        Block block = event.getClickedBlock();
        if (block == null || block.isEmpty()) return false;
        boolean leftClick;
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                leftClick = false;
                break;
            case LEFT_CLICK_BLOCK:
                leftClick = true;
                break;
            default:
                return false;
        }
        BlockClickActivator.Context e = new BlockClickActivator.Context(event.getPlayer(), block, leftClick);
        activate(e);
        return e.isCancelled();
    }

    public static @NotNull Optional<Variables> triggerInventoryClick(InventoryClickEvent event) {
        InventoryClickActivator.Context e = new InventoryClickActivator.Context((Player) event.getWhoClicked(), event.getAction(),
                event.getClick(), event.getInventory(), event.getSlotType(),
                event.getCurrentItem(), event.getHotbarButton(),
                event.getView(), event.getSlot());
        activate(e);
        return e.getVariables();
    }

    public static @NotNull Optional<Variables> triggerDrop(Player player, Item item, int pickupDelay) {
        DropActivator.Context e = new DropActivator.Context(player, item, pickupDelay);
        activate(e);
        return e.getVariables();
    }

    public static boolean triggerFlight(Player player, boolean flying) {
        FlightActivator.Context e = new FlightActivator.Context(player, flying);
        activate(e);
        return e.isCancelled();
    }

    public static boolean triggerEntityClick(Player player, Entity rightClicked) {
        EntityClickActivator.Context e = new EntityClickActivator.Context(player, rightClicked);
        activate(e);
        return e.isCancelled();
    }

    public static @NotNull Optional<Variables> triggerBlockBreak(Player player, Block block, boolean dropItems) {
        BlockBreakActivator.Context e = new BlockBreakActivator.Context(player, block, dropItems);
        activate(e);
        return e.getVariables();
    }

    public static void triggerSneak(PlayerToggleSneakEvent event) {
        SneakActivator.Context e = new SneakActivator.Context(event.getPlayer(), event.isSneaking());
        activate(e);
    }

    public static @NotNull Optional<Variables> triggerDamageByMob(EntityDamageByEntityEvent event) {
        DamageByMobActivator.Context dm = new DamageByMobActivator.Context((Player) event.getEntity(), event.getDamager(), event.getCause(), event.getDamage(), event.getDamage());
        activate(dm);
        return dm.getVariables();
    }

    public static @NotNull Optional<Variables> triggerDamageByBlock(EntityDamageByBlockEvent event, Block blockDamager) {
        double damage = event.getDamage();
        DamageByBlockActivator.Context db = new DamageByBlockActivator.Context((Player) event.getEntity(), blockDamager, event.getCause(), damage, event.getFinalDamage());
        activate(db);
        return db.getVariables();
    }

    public static @NotNull Optional<Variables> triggerDamage(EntityDamageEvent event, String source) {
        DamageActivator.Context de = new DamageActivator.Context(
                (Player) event.getEntity(), 
                event.getCause(), 
                source, 
                event.getDamage(), 
                event.getFinalDamage()
        );
        activate(de);
        return de.getVariables();
    }

    public static @NotNull Optional<Variables> triggerPickupItem(Player player, Item item, int pickupDelay) {
        PickupItemActivator.Context e = new PickupItemActivator.Context(player, item, pickupDelay);
        activate(e);
        return e.getVariables();
    }

    public static boolean triggerGamemode(Player player, GameMode gameMode) {
        GameModeActivator.Context e = new GameModeActivator.Context(player, gameMode);
        activate(e);
        return e.isCancelled();
    }

    public static boolean triggerItemHeld(Player player, int newSlot, int previousSlot) {
        ItemHeldActivator.Context e = new ItemHeldActivator.Context(player, newSlot, previousSlot);
        activate(e);
        return e.isCancelled();
    }

    public static boolean triggerWeatherChange(String world, boolean raining) {
        WeatherChangeActivator.Context context = new WeatherChangeActivator.Context(world, raining);
        activate(context);
        return context.isCancelled();
    }
    
    private static boolean activate(ActivationContext context) {
        return ReActions.getActivators().activate(context);
    }
}
