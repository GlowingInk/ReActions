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
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 *
 */

package me.fromgate.reactions.module.basics;

import me.fromgate.reactions.Cfg;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.commands.custom.FakeCommander;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.externals.worldguard.RaWorldGuard;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.activators.ExecActivator;
import me.fromgate.reactions.module.basics.activators.MessageActivator;
import me.fromgate.reactions.module.basics.activators.SignActivator;
import me.fromgate.reactions.module.basics.details.*;
import me.fromgate.reactions.util.BlockUtils;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.enums.DeathCause;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.mob.EntityUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// TODO: Refactor to StorageFactory
public final class DetailsManager {

    private DetailsManager() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static @Nullable Map<String, DataValue> triggerTeleport(Player player, TeleportCause cause, Location to) {
        TeleportDetails storage = new TeleportDetails(player, cause, to);
        ReActions.getActivators().activate(storage);
        return storage.getChangeables();
    }

    public static boolean triggerPrecommand(Player player, CommandSender sender, String fullCommand) {
        CommandDetails storage = new CommandDetails(player, sender, fullCommand);
        boolean activated = ReActions.getActivators().activate(storage);
        return storage.getChangeables() != null && (
                storage.getChangeables().get(Details.CANCEL_EVENT).asBoolean() |
                FakeCommander.triggerRaCommand(storage, activated)
        );
    }

    public static boolean triggerMobClick(Player player, LivingEntity mob) {
        if (mob == null) return false;
        MobClickDetails e = new MobClickDetails(player, mob);
        ReActions.getActivators().activate(e);
        return e.getChangeables() != null && e.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    public static void triggerMobKill(Player player, LivingEntity mob) {
        if (mob == null) return;
        MobKillDetails e = new MobKillDetails(player, mob);
        ReActions.getActivators().activate(e);
    }

    public static void triggerJoin(Player player, boolean joinfirst) {
        JoinDetails e = new JoinDetails(player, joinfirst);
        ReActions.getActivators().activate(e);
    }

    public static boolean triggerDoor(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
        if (!BlockUtils.isOpenable(event.getClickedBlock()) || event.getHand() != EquipmentSlot.HAND) return false;
        DoorDetails e = new DoorDetails(event.getPlayer(), BlockUtils.getBottomDoor(event.getClickedBlock()));
        ReActions.getActivators().activate(e);
        return e.getChangeables() != null && e.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    public static boolean triggerItemConsume(PlayerItemConsumeEvent event) {
        ItemConsumeDetails ce = new ItemConsumeDetails(event.getPlayer(), event.getItem(), event.getPlayer().getInventory().getItemInMainHand().isSimilar(event.getItem()));
        ReActions.getActivators().activate(ce);
        return ce.getChangeables() != null && ce.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    public static boolean triggerItemClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemClickDetails storage = new ItemClickDetails(
                player,
                event.getHand() == EquipmentSlot.HAND
                        ? player.getInventory().getItemInMainHand()
                        : player.getInventory().getItemInOffHand(),
                event.getHand());
        ReActions.getActivators().activate(storage);
        return storage.getChangeables() != null && storage.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    public static boolean triggerItemClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
        ItemClickDetails storage = new ItemClickDetails(event.getPlayer(), event.getItem(), event.getHand());
        ReActions.getActivators().activate(storage);
        return storage.getChangeables() != null && storage.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }


    public static boolean triggerLever(PlayerInteractEvent event) {
        if (!((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.LEFT_CLICK_BLOCK)))
            return false;
        if (event.getHand() != EquipmentSlot.HAND) return false;
        if (event.getClickedBlock().getType() != Material.LEVER) return false;
        LeverDetails e = new LeverDetails(event.getPlayer(), event.getClickedBlock());
        ReActions.getActivators().activate(e);
        return e.getChangeables() != null && e.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    // PVP Kill Event
    public static void triggerPvpKill(PlayerDeathEvent event) {
        Player deadplayer = event.getEntity();
        Player killer = EntityUtils.getKillerPlayer(deadplayer.getLastDamageCause());
        if (killer == null) return;
        PvpKillDetails pe = new PvpKillDetails(killer, deadplayer);
        ReActions.getActivators().activate(pe);
    }

    // PVP Death Event
    public static void triggerPvpDeath(PlayerDeathEvent event) {
        Player deadplayer = event.getEntity();
        LivingEntity killer = EntityUtils.getKillerEntity(deadplayer.getLastDamageCause());
        DeathCause ds = (killer == null) ? DeathCause.OTHER : (killer instanceof Player) ? DeathCause.PVP : DeathCause.PVE;
        DeathDetails pe = new DeathDetails(killer, deadplayer, ds);
        ReActions.getActivators().activate(pe);
    }

    // Button Event
    public static boolean triggerButton(PlayerInteractEvent event) {
        if (!((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.LEFT_CLICK_BLOCK)))
            return false;
        if (!Tag.BUTTONS.isTagged(event.getClickedBlock().getType())) return false;
        if (event.getHand() != EquipmentSlot.HAND) return false;
        Switch button = (Switch) event.getClickedBlock().getBlockData();
        if (button.isPowered()) return false;
        ButtonDetails be = new ButtonDetails(event.getPlayer(), event.getClickedBlock().getLocation());
        ReActions.getActivators().activate(be);
        return be.getChangeables() != null && be.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    public static boolean triggerSign(Player player, String[] lines, Location loc, boolean leftClick) {
        for (Activator act : ReActions.getActivatorTypes().get(SignActivator.class).getActivators()) {
            SignActivator sign = (SignActivator) act;
            if (sign.checkMask(lines)) {
                SignDetails se = new SignDetails(player, lines, loc, leftClick);
                ReActions.getActivators().activate(se);
                return se.getChangeables() != null && se.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
            }
        }
        return false;
    }

    // TODO: I think all of it should be inside ActionExecute class

    public static boolean triggerExec(CommandSender sender, String param) {
        if (param.isEmpty()) return false;
        return triggerExec(sender, Parameters.fromString(param, "player"));
    }

    public static boolean triggerExec(CommandSender sender, Parameters param) {
        return triggerExec(sender, param, null);
    }

    public static boolean triggerExec(CommandSender sender, Parameters param, Map<String, String> tempVars) {
        if (param.isEmpty()) return false;
        final Player senderPlayer = (sender instanceof Player) ? (Player) sender : null;
        final String id = param.getString("activator", param.getString("exec"));
        if (id.isEmpty()) return false;
        Activator act = ReActions.getActivators().getActivator(id);
        if (act == null) {
            Msg.logOnce("wrongact_" + id, "Failed to run exec activator " + id + ". Activator not found.");
            return false;
        }
        if (act.getClass() != ExecActivator.class) {
            Msg.logOnce("wrongactype_" + id, "Failed to run exec activator " + id + ". Wrong activator type.");
            return false;
        }

        int repeat = Math.min(param.getInteger("repeat", 1), 1);

        long delay = TimeUtils.timeToTicks(TimeUtils.parseTime(param.getString("delay", "1t")));

        final Set<Player> target = new HashSet<>();

        if (param.contains("player")) {
            target.addAll(ReActions.getSelectors().getPlayerList(Parameters.fromString(param.getString("player"), "player")));
        }
        target.addAll(ReActions.getSelectors().getPlayerList(param));   // Оставляем для совместимости со старым вариантом

        if (target.isEmpty() && !param.containsAny(ReActions.getSelectors().getAllKeys())) target.add(senderPlayer);

        for (int i = 0; i < repeat; i++) {
            Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
                for (Player player : target) {
                    // if (ReActions.getActivators().isStopped(player, id, true)) continue;
                    ExecDetails ce = new ExecDetails(player, tempVars);
                    // TODO Custom ActivatorType for Exec
                    ReActions.getActivators().activate(ce, id);
                }
            }, delay * repeat);
        }
        return true;
    }

    public static boolean triggerExec(CommandSender sender, String id, Map<String, String> tempVars) {
        final Player player = (sender instanceof Player) ? (Player) sender : null;
        Activator act = ReActions.getActivators().getActivator(id);
        if (act == null) {
            Msg.logOnce("wrongact_" + id, "Failed to run exec activator " + id + ". Activator not found.");
            return false;
        }
        if (act.getClass() != ExecActivator.class) {
            Msg.logOnce("wrongactype_" + id, "Failed to run exec activator " + id + ". Wrong activator type.");
            return false;
        }
        // TODO Custom ActivatorType to handle exec stopping
        // if (ReActions.getActivators().isStopped(player, id, true)) return true;
        ExecDetails ce = new ExecDetails(player, tempVars);
        ReActions.getActivators().activate(ce, id);
        return true;
    }

    public static boolean triggerPlate(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return false;
        // TODO EnumSet Plates?
        if (!(event.getClickedBlock().getType().name().endsWith("_PRESSURE_PLATE"))) return false;
        PlateDetails pe = new PlateDetails(event.getPlayer(), event.getClickedBlock().getLocation());
        ReActions.getActivators().activate(pe);
        return pe.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    public static void triggerCuboid(final Player player) {
        ReActions.getActivators().activate(new CuboidDetails(player));
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
                RegionEnterDetails wge = new RegionEnterDetails(player, rg);
                ReActions.getActivators().activate(wge);
            }
    }

    private static void triggerRgLeave(Player player, Set<String> regionTo, Set<String> regionFrom) {
        if (regionFrom.isEmpty()) return;
        for (String rg : regionFrom)
            if (!regionTo.contains(rg)) {
                RegionLeaveDetails wge = new RegionLeaveDetails(player, rg);
                ReActions.getActivators().activate(wge);
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

        RegionDetails wge = new RegionDetails(player, region);
        ReActions.getActivators().activate(wge);

        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> setFutureRegionCheck(playerName, region, true), 20L * Cfg.worldguardRecheck);
    }

    public static boolean isTimeToRaiseEvent(Player p, String id, int seconds, boolean repeat) {
        long curTime = System.currentTimeMillis();
        long prevTime = p.hasMetadata("reactions-rchk-" + id) ? p.getMetadata("reactions-rchk-" + id).get(0).asLong() : 0;
        boolean needUpdate = repeat || ((curTime - prevTime) >= (1000L * seconds));
        if (needUpdate) p.setMetadata("reactions-rchk-" + id, new FixedMetadataValue(ReActions.getPlugin(), curTime));
        return needUpdate;
    }

    // TODO: Redesign
    public static @Nullable Map<String, DataValue> triggerMessage(CommandSender sender, MessageActivator.Source source, String message) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        for (Activator act : ReActions.getActivatorTypes().get(MessageActivator.class).getActivators()) {
            MessageActivator a = (MessageActivator) act;
            if (a.filterMessage(source, message)) {
                MessageDetails me = new MessageDetails(player, a, message);
                ReActions.getActivators().activate(me);
                return me.getChangeables();
            }
        }
        return null;
    }

    public static void triggerVariable(String var, String playerName, String newValue, String prevValue) {
        if (newValue.equalsIgnoreCase(prevValue)) return;
        Player player = Bukkit.getPlayerExact(playerName);
        if (!playerName.isEmpty() && player == null) return;
        VariableDetails ve = new VariableDetails(player, var, newValue, prevValue);
        ReActions.getActivators().activate(ve);
    }

    public static @Nullable Map<String, DataValue> triggerMobDamage(Player damager, LivingEntity entity, double damage, EntityDamageEvent.DamageCause cause) {
        MobDamageDetails mde = new MobDamageDetails(entity, damager, damage, cause);
        ReActions.getActivators().activate(mde);
        return mde.getChangeables();
    }

    public static String triggerQuit(PlayerQuitEvent event) {
        QuitDetails qu = new QuitDetails(event.getPlayer(), event.getQuitMessage());
        ReActions.getActivators().activate(qu);
        return qu.getChangeables() == null
                ? qu.getQuitMessage()
                : qu.getChangeables().get(QuitDetails.QUIT_MESSAGE).asString();
    }

    public static boolean triggerBlockClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND ||
                event.getClickedBlock() == null ||
                event.getClickedBlock().getType().isAir()) return false;
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
        BlockClickDetails e = new BlockClickDetails(event.getPlayer(), event.getClickedBlock(), leftClick);
        ReActions.getActivators().activate(e);
        return e.getChangeables() != null && e.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    public static @Nullable Map<String, DataValue> triggerInventoryClick(InventoryClickEvent event) {
        InventoryClickDetails e = new InventoryClickDetails((Player) event.getWhoClicked(), event.getAction(),
                event.getClick(), event.getInventory(), event.getSlotType(),
                event.getCurrentItem(), event.getHotbarButton(),
                event.getView(), event.getSlot());
        ReActions.getActivators().activate(e);
        return e.getChangeables();
    }

    public static @Nullable Map<String, DataValue> triggerDrop(Player player, Item item, int pickupDelay) {
        DropDetails e = new DropDetails(player, item, pickupDelay);
        ReActions.getActivators().activate(e);
        return e.getChangeables();
    }

    public static boolean triggerFlight(Player player, boolean flying) {
        FlightDetails e = new FlightDetails(player, flying);
        ReActions.getActivators().activate(e);
        return e.getChangeables() != null && e.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    public static boolean triggerEntityClick(Player player, Entity rightClicked) {
        EntityClickDetails e = new EntityClickDetails(player, rightClicked);
        ReActions.getActivators().activate(e);
        return e.getChangeables() != null && e.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    public static @Nullable Map<String, DataValue> triggerBlockBreak(Player player, Block block, boolean dropItems) {
        BlockBreakDetails e = new BlockBreakDetails(player, block, dropItems);
        ReActions.getActivators().activate(e);
        return e.getChangeables();
    }

    public static void triggerSneak(PlayerToggleSneakEvent event) {
        SneakDetails e = new SneakDetails(event.getPlayer(), event.isSneaking());
        ReActions.getActivators().activate(e);
    }

    public static @Nullable Map<String, DataValue> triggerDamageByMob(EntityDamageByEntityEvent event) {
        DamageByMobDetails dm = new DamageByMobDetails((Player) event.getEntity(), event.getDamager(), event.getDamage(), event.getCause());
        ReActions.getActivators().activate(dm);
        return dm.getChangeables();
    }

    public static @Nullable Map<String, DataValue> triggerDamageByBlock(EntityDamageByBlockEvent event, Block blockDamager) {
        double damage = event.getDamage();
        DamageByBlockDetails db = new DamageByBlockDetails((Player) event.getEntity(), blockDamager, damage, event.getCause());
        ReActions.getActivators().activate(db);
        return db.getChangeables();
    }

    public static @Nullable Map<String, DataValue> triggerDamage(EntityDamageEvent event, String source) {
        double damage = event.getDamage();
        DamageDetails de = new DamageDetails((Player) event.getEntity(), damage, event.getCause(), source);
        ReActions.getActivators().activate(de);
        return de.getChangeables();
    }

    public static @Nullable Map<String, DataValue> triggerPickupItem(Player player, Item item, int pickupDelay) {
        PickupItemDetails e = new PickupItemDetails(player, item, pickupDelay);
        ReActions.getActivators().activate(e);
        return e.getChangeables();
    }

    public static boolean triggerGamemode(Player player, GameMode gameMode) {
        GameModeDetails e = new GameModeDetails(player, gameMode);
        ReActions.getActivators().activate(e);
        return e.getChangeables() != null && e.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    public static boolean triggerGod(Player player, boolean god) {
        GodDetails e = new GodDetails(player, god);
        ReActions.getActivators().activate(e);
        return e.getChangeables() != null && e.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    public static boolean triggerItemHeld(Player player, int newSlot, int previousSlot) {
        ItemHeldDetails e = new ItemHeldDetails(player, newSlot, previousSlot);
        ReActions.getActivators().activate(e);
        return e.getChangeables() != null && e.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }

    public static boolean triggerWeatherChange(String world, boolean raining) {
        WeatherChangeDetails storage = new WeatherChangeDetails(world, raining);
        ReActions.getActivators().activate(storage);
        return storage.getChangeables() != null && storage.getChangeables().get(Details.CANCEL_EVENT).asBoolean();
    }
}
