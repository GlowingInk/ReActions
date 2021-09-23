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

import lombok.experimental.UtilityClass;
import me.fromgate.reactions.Cfg;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.commands.custom.FakeCommander;
import me.fromgate.reactions.externals.worldguard.RaWorldGuard;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.*;
import me.fromgate.reactions.module.basics.storages.*;
import me.fromgate.reactions.util.BlockUtils;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.data.DataValue;
import me.fromgate.reactions.util.enums.DeathCause;
import me.fromgate.reactions.util.item.ItemUtils;
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
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class StoragesManager {

    public Map<String, DataValue> triggerTeleport(Player player, TeleportCause cause, Location to) {
        TeleportStorage storage = new TeleportStorage(player, cause, to);
        ReActions.getActivators().activate(storage);
        return storage.getChangeables();
    }

    public boolean triggerPrecommand(Player player, CommandSender sender, String fullCommand) {
        CommandStorage storage = new CommandStorage(player, sender, fullCommand);
        boolean activated = ReActions.getActivators().activate(storage);
        return storage.getChangeables().get(Storage.CANCEL_EVENT).asBoolean() | FakeCommander.raiseRaCommand(storage, activated);
    }

    public boolean triggerMobClick(Player player, LivingEntity mob) {
        if (mob == null) return false;
        MobClickStorage e = new MobClickStorage(player, mob);
        ReActions.getActivators().activate(e);
        return e.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    public void triggerMobKill(Player player, LivingEntity mob) {
        if (mob == null) return;
        MobKillStorage e = new MobKillStorage(player, mob);
        ReActions.getActivators().activate(e);
    }

    public void triggerJoin(Player player, boolean joinfirst) {
        JoinStorage e = new JoinStorage(player, joinfirst);
        ReActions.getActivators().activate(e);
    }

    public boolean triggerDoor(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
        if (!BlockUtils.isOpenable(event.getClickedBlock()) || event.getHand() != EquipmentSlot.HAND) return false;
        DoorStorage e = new DoorStorage(event.getPlayer(), BlockUtils.getBottomDoor(event.getClickedBlock()));
        ReActions.getActivators().activate(e);
        return e.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    public boolean triggerItemConsume(PlayerItemConsumeEvent event) {
        ItemConsumeStorage ce = new ItemConsumeStorage(event.getPlayer(), event.getItem(), event.getPlayer().getInventory().getItemInMainHand().isSimilar(event.getItem()));
        ReActions.getActivators().activate(ce);
        return ce.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    public boolean triggerItemClick(PlayerInteractEntityEvent event) {
        ItemClickStorage ice;
        boolean mainHand = event.getHand() == EquipmentSlot.HAND;
        ItemStack item = mainHand ? event.getPlayer().getInventory().getItemInMainHand() : event.getPlayer().getInventory().getItemInOffHand();
        if (item.getType() == Material.AIR) return false;
        ice = new ItemClickStorage(event.getPlayer(), item, mainHand);
        ReActions.getActivators().activate(ice);
        return ice.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    public boolean triggerItemClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
        ItemClickStorage ice;
        boolean mainHand = event.getHand() == EquipmentSlot.HAND;
        ItemStack item = mainHand ? event.getPlayer().getInventory().getItemInMainHand() : event.getPlayer().getInventory().getItemInOffHand();
        if (!ItemUtils.isExist(item)) return false;
        ice = new ItemClickStorage(event.getPlayer(), item, mainHand);
        ReActions.getActivators().activate(ice);
        return ice.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }


    public boolean triggerLever(PlayerInteractEvent event) {
        if (!((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.LEFT_CLICK_BLOCK)))
            return false;
        if (event.getHand() != EquipmentSlot.HAND) return false;
        if (event.getClickedBlock().getType() != Material.LEVER) return false;
        LeverStorage e = new LeverStorage(event.getPlayer(), event.getClickedBlock());
        ReActions.getActivators().activate(e);
        return e.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    // PVP Kill Event
    public void triggerPvpKill(PlayerDeathEvent event) {
        Player deadplayer = event.getEntity();
        Player killer = EntityUtils.getKiller(deadplayer.getLastDamageCause());
        if (killer == null) return;
        PvpKillStorage pe = new PvpKillStorage(killer, deadplayer);
        ReActions.getActivators().activate(pe);
    }

    // PVP Death Event
    public void triggerPvpDeath(PlayerDeathEvent event) {
        Player deadplayer = event.getEntity();
        LivingEntity killer = EntityUtils.getAnyKiller(deadplayer.getLastDamageCause());
        DeathCause ds = (killer == null) ? DeathCause.OTHER : (killer instanceof Player) ? DeathCause.PVP : DeathCause.PVE;
        DeathStorage pe = new DeathStorage(killer, deadplayer, ds);
        ReActions.getActivators().activate(pe);
    }

    // Button Event
    public boolean triggerButton(PlayerInteractEvent event) {
        if (!((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.LEFT_CLICK_BLOCK)))
            return false;
        if (!Tag.BUTTONS.isTagged(event.getClickedBlock().getType())) return false;
        if (event.getHand() != EquipmentSlot.HAND) return false;
        Switch button = (Switch) event.getClickedBlock().getBlockData();
        if (button.isPowered()) return false;
        ButtonStorage be = new ButtonStorage(event.getPlayer(), event.getClickedBlock().getLocation());
        ReActions.getActivators().activate(be);
        return be.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    public boolean triggerSign(Player player, String[] lines, Location loc, boolean leftClick) {
        for (Activator act : ReActions.getActivators().getType(SignActivator.class).getActivators()) {
            SignActivator sign = (SignActivator) act;
            if (sign.checkMask(lines)) {
                SignStorage se = new SignStorage(player, lines, loc, leftClick);
                ReActions.getActivators().activate(se);
                return se.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
            }
        }
        return false;
    }

    // TODO: I think all of it should be inside ActionExecute class

    public boolean triggerExec(CommandSender sender, String param) {
        if (param.isEmpty()) return false;
        return triggerExec(sender, Parameters.fromString(param, "player"));
    }

    public boolean triggerExec(CommandSender sender, Parameters param) {
        return triggerExec(sender, param, null);
    }

    public boolean triggerExec(CommandSender sender, Parameters param, Map<String, String> tempVars) {
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
                    ExecStorage ce = new ExecStorage(player, tempVars);
                    // TODO Custom ActivatorType for Exec
                    ReActions.getActivators().activate(ce, id);
                }
            }, delay * repeat);
        }
        return true;
    }

    public boolean triggerExec(CommandSender sender, String id, Map<String, String> tempVars) {
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
        ExecStorage ce = new ExecStorage(player, tempVars);
        ReActions.getActivators().activate(ce, id);
        return true;
    }

    public boolean triggerPlate(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return false;
        // TODO EnumSet Plates?
        if (!(event.getClickedBlock().getType().name().endsWith("_PRESSURE_PLATE"))) return false;
        PlateStorage pe = new PlateStorage(event.getPlayer(), event.getClickedBlock().getLocation());
        ReActions.getActivators().activate(pe);
        return pe.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    public void triggerCuboid(final Player player) {
        ReActions.getActivators().activate(new CuboidStorage(player));
    }

    public void triggerAllRegions(final Player player, final Location to, final Location from) {
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

    private void triggerRgEnter(Player player, Set<String> regionTo, Set<String> regionFrom) {
        if (regionTo.isEmpty()) return;
        for (String rg : regionTo)
            if (!regionFrom.contains(rg)) {
                RegionEnterStorage wge = new RegionEnterStorage(player, rg);
                ReActions.getActivators().activate(wge);
            }
    }

    private void triggerRgLeave(Player player, Set<String> regionTo, Set<String> regionFrom) {
        if (regionFrom.isEmpty()) return;
        for (String rg : regionFrom)
            if (!regionTo.contains(rg)) {
                RegionLeaveStorage wge = new RegionLeaveStorage(player, rg);
                ReActions.getActivators().activate(wge);
            }
    }

    private void triggerRegion(Player player, Set<String> to) {
        if (to.isEmpty()) return;
        for (String region : to) {
            setFutureRegionCheck(player.getName(), region, false);
        }
    }

    private void setFutureRegionCheck(final String playerName, final String region, boolean repeat) {
        Player player = Utils.getPlayerExact(playerName);
        if (player == null) return;
        if (!player.isOnline()) return;
        if (player.isDead()) return;
        if (!RaWorldGuard.isPlayerInRegion(player, region)) return;
        String rg = "rg-" + region;
        if (!isTimeToRaiseEvent(player, rg, Cfg.worldguardRecheck, repeat)) return;

        RegionStorage wge = new RegionStorage(player, region);
        ReActions.getActivators().activate(wge);

        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> setFutureRegionCheck(playerName, region, true), 20L * Cfg.worldguardRecheck);
    }

    public boolean isTimeToRaiseEvent(Player p, String id, int seconds, boolean repeat) {
        long curTime = System.currentTimeMillis();
        long prevTime = p.hasMetadata("reactions-rchk-" + id) ? p.getMetadata("reactions-rchk-" + id).get(0).asLong() : 0;
        boolean needUpdate = repeat || ((curTime - prevTime) >= (1000L * seconds));
        if (needUpdate) p.setMetadata("reactions-rchk-" + id, new FixedMetadataValue(ReActions.getPlugin(), curTime));
        return needUpdate;
    }

    // TODO: Redesign
    public Map<String, DataValue> triggerMessage(CommandSender sender, MessageActivator.Source source, String message) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        for (Activator act : ReActions.getActivators().getType(MessageActivator.class).getActivators()) {
            MessageActivator a = (MessageActivator) act;
            if (a.filterMessage(source, message)) {
                MessageStorage me = new MessageStorage(player, a, message);
                ReActions.getActivators().activate(me);
                return me.getChangeables();
            }
        }
        return null;
    }

    public void triggerVariable(String var, String playerName, String newValue, String prevValue) {
        if (newValue.equalsIgnoreCase(prevValue)) return;
        Player player = Utils.getPlayerExact(playerName);
        if (!playerName.isEmpty() && player == null) return;
        VariableStorage ve = new VariableStorage(player, var, newValue, prevValue);
        ReActions.getActivators().activate(ve);
    }

    public Map<String, DataValue> triggerMobDamage(Player damager, LivingEntity entity, double damage, EntityDamageEvent.DamageCause cause) {
        MobDamageStorage mde = new MobDamageStorage(entity, damager, damage, cause);
        ReActions.getActivators().activate(mde);
        return mde.getChangeables();
    }

    public String triggerQuit(PlayerQuitEvent event) {
        QuitStorage qu = new QuitStorage(event.getPlayer(), event.getQuitMessage());
        ReActions.getActivators().activate(qu);
        return qu.getChangeables().get(QuitStorage.QUIT_MESSAGE).asString();
    }

    public boolean triggerBlockClick(PlayerInteractEvent event) {
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
        BlockClickStorage e = new BlockClickStorage(event.getPlayer(), event.getClickedBlock(), leftClick);
        ReActions.getActivators().activate(e);
        return e.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    public Map<String, DataValue> triggerInventoryClick(InventoryClickEvent event) {
        InventoryClickStorage e = new InventoryClickStorage((Player) event.getWhoClicked(), event.getAction(),
                event.getClick(), event.getInventory(), event.getSlotType(),
                event.getCurrentItem(), event.getHotbarButton(),
                event.getView(), event.getSlot());
        ReActions.getActivators().activate(e);
        return e.getChangeables();
    }

    public Map<String, DataValue> triggerDrop(Player player, Item item, int pickupDelay) {
        DropStorage e = new DropStorage(player, item, pickupDelay);
        ReActions.getActivators().activate(e);
        return e.getChangeables();
    }

    public boolean triggerFlight(Player player, boolean flying) {
        FlightStorage e = new FlightStorage(player, flying);
        ReActions.getActivators().activate(e);
        return e.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    public boolean triggerEntityClick(Player player, Entity rightClicked) {
        EntityClickStorage e = new EntityClickStorage(player, rightClicked);
        ReActions.getActivators().activate(e);
        return e.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    public Map<String, DataValue> triggerBlockBreak(Player player, Block block, boolean dropItems) {
        BlockBreakStorage e = new BlockBreakStorage(player, block, dropItems);
        ReActions.getActivators().activate(e);
        return e.getChangeables();
    }

    public void triggerSneak(PlayerToggleSneakEvent event) {
        SneakStorage e = new SneakStorage(event.getPlayer(), event.isSneaking());
        ReActions.getActivators().activate(e);
    }

    public Map<String, DataValue> triggerDamageByMob(EntityDamageByEntityEvent event) {
        DamageByMobStorage dm = new DamageByMobStorage((Player) event.getEntity(), event.getDamager(), event.getDamage(), event.getCause());
        ReActions.getActivators().activate(dm);
        return dm.getChangeables();
    }

    public Map<String, DataValue> triggerDamageByBlock(EntityDamageByBlockEvent event, Block blockDamager) {
        double damage = event.getDamage();
        DamageByBlockStorage db = new DamageByBlockStorage((Player) event.getEntity(), blockDamager, damage, event.getCause());
        ReActions.getActivators().activate(db);
        return db.getChangeables();
    }

    public Map<String, DataValue> triggerDamage(EntityDamageEvent event, String source) {
        double damage = event.getDamage();
        DamageStorage de = new DamageStorage((Player) event.getEntity(), damage, event.getCause(), source);
        ReActions.getActivators().activate(de);
        return de.getChangeables();
    }

    // TODO
    public void triggerProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        ProjectileHitStorage ph = new ProjectileHitStorage((Player) event.getEntity().getShooter(),
                event.getEntityType(),
                event.getHitBlock(), event.getHitBlockFace(),
                event.getHitEntity());
        ReActions.getActivators().activate(ph);
    }

    public Map<String, DataValue> triggerPickupItem(Player player, Item item, int pickupDelay) {
        PickupItemStorage e = new PickupItemStorage(player, item, pickupDelay);
        ReActions.getActivators().activate(e);
        return e.getChangeables();
    }

    public boolean triggerGamemode(Player player, GameMode gameMode) {
        GameModeStorage e = new GameModeStorage(player, gameMode);
        ReActions.getActivators().activate(e);
        return e.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    public boolean triggerGod(Player player, boolean god) {
        GodStorage e = new GodStorage(player, god);
        ReActions.getActivators().activate(e);
        return e.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    public boolean triggerItemHeld(Player player, int newSlot, int previousSlot) {
        ItemHeldStorage e = new ItemHeldStorage(player, newSlot, previousSlot);
        ReActions.getActivators().activate(e);
        return e.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }

    public boolean triggerWeatherChange(String world, boolean raining) {
        WeatherChangeStorage storage = new WeatherChangeStorage(world, raining);
        ReActions.getActivators().activate(storage);
        return storage.getChangeables().get(Storage.CANCEL_EVENT).asBoolean();
    }
}
