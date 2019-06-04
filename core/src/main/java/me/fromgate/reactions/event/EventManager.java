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

package me.fromgate.reactions.event;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.activators.Activator;
import me.fromgate.reactions.activators.ActivatorType;
import me.fromgate.reactions.activators.Activators;
import me.fromgate.reactions.activators.ItemHoldActivator;
import me.fromgate.reactions.activators.ItemWearActivator;
import me.fromgate.reactions.activators.MessageActivator;
import me.fromgate.reactions.activators.PlayerDeathActivator;
import me.fromgate.reactions.activators.SignActivator;
import me.fromgate.reactions.externals.RaWorldGuard;
import me.fromgate.reactions.util.BukkitCompatibilityFix;
import me.fromgate.reactions.util.Cfg;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.item.ItemUtil;
import me.fromgate.reactions.util.message.M;
import me.fromgate.reactions.util.playerselector.PlayerSelectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Button;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventManager {
    private static ReActions plg() {
        return ReActions.instance;
    }

    public static boolean raiseFactionEvent(Player p, String oldFaction, String newFaction) {
        FactionEvent e = new FactionEvent(p, oldFaction, newFaction);
        Bukkit.getServer().getPluginManager().callEvent(e);
        return true;
    }


    public static boolean raiseFactionCreateEvent(String factionName, Player player) {
        FactionCreateEvent e = new FactionCreateEvent(factionName, player);
        Bukkit.getServer().getPluginManager().callEvent(e);
        return true;
    }

    public static boolean raiseFactionDisbandEvent(String factionName, Player player) {
        FactionDisbandEvent e = new FactionDisbandEvent(factionName, player);
        Bukkit.getServer().getPluginManager().callEvent(e);
        return true;
    }


    public static boolean raiseFactionRelationEvent(String faction, String factionOther, String oldRelation, String newRelation) {
        FactionRelationEvent e = new FactionRelationEvent(faction, factionOther, oldRelation, newRelation);
        Bukkit.getServer().getPluginManager().callEvent(e);
        return true;
    }

    public static boolean raiseMobClickEvent(Player player, LivingEntity mob) {
        if (mob == null) return false;
        MobClickEvent e = new MobClickEvent(player, mob);
        Bukkit.getServer().getPluginManager().callEvent(e);
        return true;
    }

    public static boolean raiseMobKillEvent(Player player, LivingEntity mob) {
        if (mob == null) return false;
        MobKillEvent e = new MobKillEvent(player, mob);
        Bukkit.getServer().getPluginManager().callEvent(e);
        return true;
    }


    public static boolean raiseJoinEvent(Player player, boolean joinfirst) {
        JoinEvent e = new JoinEvent(player, joinfirst);
        Bukkit.getServer().getPluginManager().callEvent(e);
        return true;
    }

    public static boolean raiseDoorEvent(PlayerInteractEvent event) {
        if (!((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.LEFT_CLICK_BLOCK)))
            return false;
        if (!Util.isDoorBlock(event.getClickedBlock())) return false;
        if (!BukkitCompatibilityFix.isHandSlot(event)) {
            return false;
        }
        DoorEvent e = new DoorEvent(event.getPlayer(), Util.getDoorBottomBlock(event.getClickedBlock()));
        Bukkit.getServer().getPluginManager().callEvent(e);
        return e.isCancelled();
    }

    public static boolean raiseItemConsumeEvent(PlayerItemConsumeEvent event) {
        if (event.getItem() == null) return false;
        ItemConsumeEvent ce = new ItemConsumeEvent(event.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(ce);
        return ce.isCancelled();
    }

    public static boolean raiseItemClickEvent(PlayerInteractEntityEvent event) {
        ItemStack itemInHand = BukkitCompatibilityFix.getItemInHand(event.getPlayer());
        if (itemInHand == null || itemInHand.getType() == Material.AIR) return false;
        if (!BukkitCompatibilityFix.isHandSlot(event)) {
            return false;
        }
        ItemClickEvent ice = new ItemClickEvent(event.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(ice);
        return true;
    }

    public static boolean raiseItemClickEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return false;
        }
        if (!BukkitCompatibilityFix.isHandSlot(event)) {
            return false;
        }
        ItemStack itemInHand = BukkitCompatibilityFix.getItemInHand(event.getPlayer());
        if (itemInHand == null || itemInHand.getType() == Material.AIR) return false;
        ItemClickEvent ice = new ItemClickEvent(event.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(ice);
        itemInHand = BukkitCompatibilityFix.getItemInHand(event.getPlayer());
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            event.setCancelled(true);
        }
        return true;
    }


    public static boolean raiseLeverEvent(PlayerInteractEvent event) {
        if (!((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.LEFT_CLICK_BLOCK)))
            return false;
        if (!BukkitCompatibilityFix.isHandSlot(event)) {
            return false;
        }
        if (event.getClickedBlock().getType() != Material.LEVER) return false;
        LeverEvent e = new LeverEvent(event.getPlayer(), event.getClickedBlock());
        Bukkit.getServer().getPluginManager().callEvent(e);
        return e.isCancelled();
    }


    // PVP Kill Event
    public static void raisePvpKillEvent(PlayerDeathEvent event) {
        Player deadplayer = event.getEntity();
        Player killer = Util.getKiller(deadplayer.getLastDamageCause());
        if (killer == null) return;
        PvpKillEvent pe = new PvpKillEvent(killer, deadplayer);
        Bukkit.getServer().getPluginManager().callEvent(pe);
    }

    // PVP Death Event
    public static void raisePvpDeathEvent(PlayerDeathEvent event) {
        Player deadplayer = event.getEntity();
        LivingEntity killer = Util.getAnyKiller(deadplayer.getLastDamageCause());
        PlayerDeathActivator.DeathCause ds = (killer == null) ? PlayerDeathActivator.DeathCause.OTHER : (killer instanceof Player) ? PlayerDeathActivator.DeathCause.PVP : PlayerDeathActivator.DeathCause.PVE;
        PlayerWasKilledEvent pe = new PlayerWasKilledEvent(killer, deadplayer, ds);
        Bukkit.getServer().getPluginManager().callEvent(pe);
    }

    // Button Event
    public static boolean raiseButtonEvent(PlayerInteractEvent event) {
        if (!((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.LEFT_CLICK_BLOCK))) {
            return false;
        }
        if (!(event.getClickedBlock().getType().name().endsWith("_BUTTON"))) {
            return false;
        }
        if (!BukkitCompatibilityFix.isHandSlot(event)) {
            return false;
        }
        BlockState state = event.getClickedBlock().getState();
        if (state.getData() instanceof Button) {
            Button button = (Button) state.getData();
            if (button.isPowered()) return false;
        }
        ButtonEvent be = new ButtonEvent(event.getPlayer(), event.getClickedBlock().getLocation());
        Bukkit.getServer().getPluginManager().callEvent(be);
        return be.isCancelled();
    }

    public static boolean raiseSignEvent(Player player, String[] lines, Location loc, boolean leftClick) {
        for (Activator act : Activators.getActivators(ActivatorType.SIGN)) {
            SignActivator sign = (SignActivator) act;
            if (sign.checkMask(lines)) {
                SignEvent se = new SignEvent(player, lines, loc, leftClick);
                Bukkit.getServer().getPluginManager().callEvent(se);
                return true;
            }
        }
        return false;
    }

    public static boolean raiseCommandEvent(Player p, String command, boolean canceled) {
        if (command.isEmpty()) return false;
        String[] args = command.split(" ");
        CommandEvent ce = new CommandEvent(p, command, args, canceled);
        Bukkit.getServer().getPluginManager().callEvent(ce);
        return ce.isCancelled();
    }

    public static boolean raiseExecEvent(CommandSender sender, String param) {
        if (param.isEmpty()) return false;
        return raiseExecEvent(sender, new Param(param, "player"));
    }

    public static boolean raiseExecEvent(CommandSender sender, Param param) {
        return raiseExecEvent(sender, param, null);
    }

    public static boolean raiseExecEvent(CommandSender sender, Param param, final Param tempVars) {
        if (param.isEmpty()) return false;
        final Player senderPlayer = (sender instanceof Player) ? (Player) sender : null;
        final String id = param.getParam("activator", param.getParam("exec"));
        if (id.isEmpty()) return false;
        Activator act = Activators.get(id);
        if (act == null) {
            M.logOnce("wrongact_" + id, "Failed to run exec activator " + id + ". Activator not found.");
            return false;
        }
        if (act.getType() != ActivatorType.EXEC) {
            M.logOnce("wrongactype_" + id, "Failed to run exec activator " + id + ". Wrong activator type.");
            return false;
        }

        int repeat = Math.min(param.getParam("repeat", 1), 1);

        long delay = Util.timeToTicks(Util.parseTime(param.getParam("delay", "1t")));

        final Set<Player> target = new HashSet<>();

        if (param.isParamsExists("player")) {
            target.addAll(PlayerSelectors.getPlayerList(new Param(param.getParam("player"), "player")));
        }
        target.addAll(PlayerSelectors.getPlayerList(param));   // Оставляем для совместимости со старым вариантом

        if (target.isEmpty() && !param.hasAnyParam(PlayerSelectors.getAllKeys())) target.add(senderPlayer);

        for (int i = 0; i < repeat; i++) {
            Bukkit.getScheduler().runTaskLater(plg(), () -> {
                for (Player player : target) {
                    if (Activators.isStopped(player, id, true)) continue;
                    ExecEvent ce = new ExecEvent(senderPlayer, player, id, tempVars);
                    Bukkit.getServer().getPluginManager().callEvent(ce);
                }
            }, delay * repeat);
        }
        return true;
    }

    // Plate Event
    public static boolean raisePlateEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return false;
        if (!(event.getClickedBlock().getType().name().endsWith("_PRESSURE_PLATE"))) {
            return false;
        }
        final Player p = event.getPlayer();
        final Location l = event.getClickedBlock().getLocation();
        Bukkit.getScheduler().runTaskLater(plg(), () -> {
            PlateEvent pe = new PlateEvent(p, l);
            Bukkit.getServer().getPluginManager().callEvent(pe);
        }, 1);
        return false;
    }

    public static void raiseAllRegionEvents(final Player player, final Location to, final Location from) {
        if (!RaWorldGuard.isConnected()) return;
        Bukkit.getScheduler().runTaskLaterAsynchronously(ReActions.instance, () -> {

            final List<String> regionsTo = RaWorldGuard.getRegions(to);
            final List<String> regionsFrom = RaWorldGuard.getRegions(from);

            Bukkit.getScheduler().runTask(ReActions.instance, () -> {
                raiseRegionEvent(player, regionsTo);
                raiseRgEnterEvent(player, regionsTo, regionsFrom);
                raiseRgLeaveEvent(player, regionsTo, regionsFrom);
            });
        }, 1);
    }

    private static void raiseRgEnterEvent(Player player, List<String> regionTo, List<String> regionFrom) {
        if (regionTo.isEmpty()) return;
        for (String rg : regionTo)
            if (!regionFrom.contains(rg)) {
                RegionEnterEvent wge = new RegionEnterEvent(player, rg);
                Bukkit.getServer().getPluginManager().callEvent(wge);
            }
    }

    private static void raiseRgLeaveEvent(Player player, List<String> regionTo, List<String> regionFrom) {
        if (regionFrom.isEmpty()) return;
        for (String rg : regionFrom)
            if (!regionTo.contains(rg)) {
                RegionLeaveEvent wge = new RegionLeaveEvent(player, rg);
                Bukkit.getServer().getPluginManager().callEvent(wge);
            }
    }

    private static void raiseRegionEvent(Player player, List<String> to) {
        if (to.isEmpty()) return;
        for (String region : to) {
            setFutureRegionCheck(player.getName(), region, false);
        }
    }

    private static void setFutureRegionCheck(final String playerName, final String region, boolean repeat) {
        @SuppressWarnings("deprecation")
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return;
        if (!player.isOnline()) return;
        if (player.isDead()) return;
        if (!RaWorldGuard.isPlayerInRegion(player, region)) return;
        String rg = "rg-" + region;
        if (!isTimeToRaiseEvent(player, rg, Cfg.worlduardRecheck, repeat)) return;

        RegionEvent wge = new RegionEvent(player, region);
        Bukkit.getServer().getPluginManager().callEvent(wge);

        Bukkit.getScheduler().runTaskLater(plg(), () -> setFutureRegionCheck(playerName, region, true), 20 * Cfg.worlduardRecheck);
    }


    private static void setFutureItemWearCheck(final String playerName, final String itemStr, boolean repeat) {
        @SuppressWarnings("deprecation")
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return;
        if (!player.isOnline()) return;
        String rg = "iw-" + itemStr;
        if (!isTimeToRaiseEvent(player, rg, Cfg.itemWearRecheck, repeat)) return;
        ItemWearEvent iwe = new ItemWearEvent(player);
        if (!iwe.isItemWeared(itemStr)) return;
        Bukkit.getServer().getPluginManager().callEvent(iwe);
        Bukkit.getScheduler().runTaskLater(plg(), () -> setFutureItemWearCheck(playerName, itemStr, true), 20 * Cfg.itemWearRecheck);
    }


    public static void raiseItemWearEvent(Player player) {
        final String playerName = player.getName();
        Bukkit.getScheduler().runTaskLater(plg(), () -> {
            for (ItemWearActivator iw : Activators.getItemWearActivatos())
                setFutureItemWearCheck(playerName, iw.getItemStr(), false);
        }, 1);
    }

    public static void raiseItemHoldEvent(Player player) {
        final String playerName = player.getName();
        Bukkit.getScheduler().runTaskLater(plg(), () -> {
            for (ItemHoldActivator ih : Activators.getItemHoldActivatos())
                setFutureItemHoldCheck(playerName, ih.getItemStr(), false);
        }, 1);
    }


    private static boolean setFutureItemHoldCheck(final String playerName, final String itemStr, boolean repeat) {
        @SuppressWarnings("deprecation")
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null || !player.isOnline() || player.isDead()) return false;
        ItemStack itemInHand = BukkitCompatibilityFix.getItemInHand(player);
        if (itemInHand == null || itemInHand.getType() == Material.AIR) return false;
        String rg = "ih-" + itemStr;
        if (!isTimeToRaiseEvent(player, rg, Cfg.itemHoldRecheck, repeat)) return false;
        if (!ItemUtil.compareItemStr(itemInHand, itemStr)) return false;
        ItemHoldEvent ihe = new ItemHoldEvent(player);
        Bukkit.getServer().getPluginManager().callEvent(ihe);

        Bukkit.getScheduler().runTaskLater(plg(), () -> setFutureItemHoldCheck(playerName, itemStr, true), 20 * Cfg.itemHoldRecheck);
        return true;
    }

    public static boolean isTimeToRaiseEvent(Player p, String id, int seconds, boolean repeat) {
        Long curtime = System.currentTimeMillis();
        Long prevtime = p.hasMetadata("reactions-rchk-" + id) ? p.getMetadata("reactions-rchk-" + id).get(0).asLong() : 0;
        boolean needUpdate = repeat || ((curtime - prevtime) >= (1000 * seconds));
        if (needUpdate) p.setMetadata("reactions-rchk-" + id, new FixedMetadataValue(plg(), curtime));
        return needUpdate;
    }

    public static boolean raiseMessageEvent(CommandSender sender, MessageActivator.Source source, String message) {
        Player player = sender != null && (sender instanceof Player) ? (Player) sender : null;
        for (MessageActivator a : Activators.getMessageActivators()) {
            if (a.filterMessage(source, message)) {
                MessageEvent me = new MessageEvent(player, a, message);
                Bukkit.getServer().getPluginManager().callEvent(me);
                return me.isCancelled();
            }
        }
        return false;
    }

    public static void raiseVariableEvent(String var, String playerName, String newValue, String prevValue) {
        if (newValue.equalsIgnoreCase(prevValue)) return;
        @SuppressWarnings("deprecation")
        Player player = Bukkit.getPlayerExact(playerName);
        if (!playerName.isEmpty() && player == null) return;
        VariableEvent ve = new VariableEvent(player, var, newValue, prevValue);
        Bukkit.getServer().getPluginManager().callEvent(ve);
    }

    public static boolean raiseMobDamageEvent(EntityDamageEvent event, Player damager) {
        if (damager == null) return false;
        if (!(event.getEntity() instanceof LivingEntity)) return false;
        double damage = BukkitCompatibilityFix.getEventDamage(event);
        MobDamageEvent mde = new MobDamageEvent((LivingEntity) event.getEntity(), damager, damage, event.getCause());
        Bukkit.getServer().getPluginManager().callEvent(mde);
        BukkitCompatibilityFix.setEventDamage(event, mde.getDamage());
        return mde.isCancelled();
    }

    public static void raiseQuitEvent(PlayerQuitEvent event) {
        QuitEvent qu = new QuitEvent(event.getPlayer(), event.getQuitMessage());
        Bukkit.getServer().getPluginManager().callEvent(qu);
        event.setQuitMessage(qu.getQuitMessage() == null || qu.getQuitMessage().isEmpty() ? null : ChatColor.translateAlternateColorCodes('&', qu.getQuitMessage()));
    }

    public static boolean raiseBlockClickEvent(PlayerInteractEvent event) {
        Boolean leftClick;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) leftClick = false;
        else if (event.getAction() == Action.LEFT_CLICK_BLOCK) leftClick = true;
        else return false;
        if (!BukkitCompatibilityFix.isHandSlot(event)) {
            return false;
        }
        BlockClickEvent e = new BlockClickEvent(event.getPlayer(), event.getClickedBlock(), leftClick);
        Bukkit.getServer().getPluginManager().callEvent(e);
        return e.isCancelled();
    }

    @SuppressWarnings("deprecation")
    public static boolean raiseInventoryClickEvent(InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();
        ItemStack oldItem = event.getCurrentItem();
        PlayerInventoryClickEvent e = new PlayerInventoryClickEvent(p, event.getAction(), event.getClick(), event.getInventory(), event.getSlotType(), event.getCurrentItem(), event.getHotbarButton(), event.getView(), event.getSlot());
        Bukkit.getServer().getPluginManager().callEvent(e);
        ItemStack newItemStack = e.getItemStack();
        if (newItemStack != null) {
            if (newItemStack.getType() != Material.AIR && newItemStack.getAmount() <= 1 && oldItem != null) {
                newItemStack.setAmount(oldItem.getAmount());
            }
            if (!(event instanceof InventoryCreativeEvent)) event.setCurrentItem(newItemStack);
        }
        return e.isCancelled();
    }

    public static boolean raiseDropEvent(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        Player player = event.getPlayer();
        double pickupDelay = BukkitCompatibilityFix.getItemPickupDelay(item);
        DropEvent e = new DropEvent(player, event.getItemDrop(), pickupDelay);
        Bukkit.getServer().getPluginManager().callEvent(e);
        BukkitCompatibilityFix.setItemPickupDelay(item, e.getPickupDelay());
        ItemStack newItemStack = e.getItemStack();
        if (newItemStack != null && newItemStack.getType() == Material.AIR) {
            item.remove();
        } else if (newItemStack != null) {
            ItemStack itemStack = item.getItemStack();
            if (newItemStack.getAmount() > 1) {
                for (int i = 0; i < newItemStack.getAmount(); i++) {
                    item.setItemStack(new ItemStack(newItemStack.clone()));
                }
            } else {
                itemStack.setType(newItemStack.getType());
                if (newItemStack.getData() != null) itemStack.setData(newItemStack.getData());
                if (newItemStack.getItemMeta() != null) itemStack.setItemMeta(newItemStack.getItemMeta());
                itemStack.setDurability(newItemStack.getDurability());
            }
        }
        return e.isCancelled();
    }

    public static boolean raiseFlightEvent(PlayerToggleFlightEvent event) {
        FlightEvent e = new FlightEvent(event.getPlayer(), event.isFlying());
        Bukkit.getServer().getPluginManager().callEvent(e);
        return e.isCancelled();
    }

    public static boolean raiseEntityClickEvent(PlayerInteractEntityEvent event) {
        if (!BukkitCompatibilityFix.isHandSlot(event)) {
            return false;
        }
        EntityClickEvent e = new EntityClickEvent(event.getPlayer(), event.getRightClicked());
        Bukkit.getServer().getPluginManager().callEvent(e);
        return e.isCancelled();
    }

    public static boolean raiseBlockBreakEvent(BlockBreakEvent event) {
        boolean isDropItems = BukkitCompatibilityFix.isDropItems(event);
        PlayerBlockBreakEvent e = new PlayerBlockBreakEvent(event.getPlayer(), event.getBlock(), isDropItems);
        Bukkit.getServer().getPluginManager().callEvent(e);
        BukkitCompatibilityFix.setDropItems(event, e.isDropItems());
        return e.isCancelled();
    }

    public static boolean raiseSneakEvent(PlayerToggleSneakEvent event) {
        SneakEvent e = new SneakEvent(event.getPlayer(), event.isSneaking());
        Bukkit.getServer().getPluginManager().callEvent(e);
        return e.isCancelled();
    }

    public static boolean raisePlayerDamageByMobEvent(EntityDamageByEntityEvent event, LivingEntity damager, Entity entityDamager) {
        if (!(event.getEntity() instanceof LivingEntity)) return false;
        double damage = BukkitCompatibilityFix.getEventDamage(event);
        DamageByMobEvent dm = new DamageByMobEvent((Player) event.getEntity(), damager, entityDamager, damage, event.getCause());
        Bukkit.getServer().getPluginManager().callEvent(dm);
        BukkitCompatibilityFix.setEventDamage(event, dm.getDamage());
        return dm.isCancelled();
    }

    public static boolean raisePlayerDamageByBlockEvent(EntityDamageByBlockEvent event, Block blockDamager) {
        if (!(event.getEntity() instanceof LivingEntity)) return false;
        double damage = BukkitCompatibilityFix.getEventDamage(event);
        DamageByBlockEvent db = new DamageByBlockEvent((Player) event.getEntity(), blockDamager, damage, event.getCause());
        Bukkit.getServer().getPluginManager().callEvent(db);
        BukkitCompatibilityFix.setEventDamage(event, db.getDamage());
        return db.isCancelled();
    }

    public static boolean raisePlayerDamageEvent(EntityDamageEvent event, String source) {
        if (!(event.getEntity() instanceof LivingEntity)) return false;
        double damage = BukkitCompatibilityFix.getEventDamage(event);
        DamageEvent de = new DamageEvent((Player) event.getEntity(), damage, event.getCause(), source);
        Bukkit.getServer().getPluginManager().callEvent(de);
        BukkitCompatibilityFix.setEventDamage(event, de.getDamage());
        return de.isCancelled();
    }

    public static boolean raiseEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            FallingBlock fb = (FallingBlock) event.getEntity();
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Entity e : p.getNearbyEntities(0.5D, 1.0D, 0.5D)) {
                    if ((e instanceof FallingBlock) && fb == e) {
                        //noinspection deprecation
                        Bukkit.getPluginManager().callEvent(new EntityDamageByEntityEvent(e, p, EntityDamageEvent.DamageCause.FALLING_BLOCK, 0));
                    }
                }
            }
        }
        return false;
    }

    public static boolean raiseProjectileHitEvent(ProjectileHitEvent event) {
        Entity hitEntity = BukkitCompatibilityFix.getHitEntity(event);
        if (hitEntity == null || !(hitEntity instanceof Player)) return false;
        Player player = (Player) hitEntity;
        Entity entity = event.getEntity();
        if (entity == null) return false;
        // TODO PlayerProjectileHit activator
        return false;
    }

    public static boolean raisePlayerPickupItemEvent(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        Player player = event.getPlayer();
        double pickupDelay = BukkitCompatibilityFix.getItemPickupDelay(item);
        PickupItemEvent e = new PickupItemEvent(player, event.getItem(), pickupDelay);
        Bukkit.getServer().getPluginManager().callEvent(e);
        BukkitCompatibilityFix.setItemPickupDelay(item, e.getPickupDelay());
        ItemStack newItemStack = e.getItemStack();
        if (newItemStack != null && newItemStack.getType() == Material.AIR) {
            e.setCancelled(true);
            item.remove();
        } else if (newItemStack != null) {
            ItemStack itemStack = item.getItemStack();
            if (newItemStack.getAmount() > 1) {
                e.setCancelled(true);
                item.remove();
                ItemUtil.giveItemOrDrop(player, newItemStack);
            } else {
                itemStack.setType(newItemStack.getType());
                if (newItemStack.getData() != null) itemStack.setData(newItemStack.getData());
                if (newItemStack.getItemMeta() != null) itemStack.setItemMeta(newItemStack.getItemMeta());
                itemStack.setDurability(newItemStack.getDurability());
            }
        }
        return e.isCancelled();
    }

    public static boolean raisePlayerGameModeChangeEvent(PlayerGameModeChangeEvent event) {
        GameModeEvent e = new GameModeEvent(event.getPlayer(), event.getNewGameMode());
        Bukkit.getServer().getPluginManager().callEvent(e);
        return e.isCancelled();
    }

    public static boolean raisePlayerGodChangeEvent(Player player, boolean god) {
        GodEvent e = new GodEvent(player, god);
        Bukkit.getServer().getPluginManager().callEvent(e);
        return e.isCancelled();
    }

    public static boolean raiseItemHeldEvent(Player player, int newSlot, int previousSlot) {
        ItemHeldEvent e = new ItemHeldEvent(player, newSlot, previousSlot);
        Bukkit.getServer().getPluginManager().callEvent(e);
        return e.isCancelled();
    }
}
