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

package me.fromgate.reactions.activators;


import me.fromgate.reactions.event.BlockClickEvent;
import me.fromgate.reactions.event.ButtonEvent;
import me.fromgate.reactions.event.CommandEvent;
import me.fromgate.reactions.event.DamageByBlockEvent;
import me.fromgate.reactions.event.DamageByMobEvent;
import me.fromgate.reactions.event.DamageEvent;
import me.fromgate.reactions.event.DoorEvent;
import me.fromgate.reactions.event.DropEvent;
import me.fromgate.reactions.event.EntityClickEvent;
import me.fromgate.reactions.event.ExecEvent;
import me.fromgate.reactions.event.FactionCreateEvent;
import me.fromgate.reactions.event.FactionDisbandEvent;
import me.fromgate.reactions.event.FactionEvent;
import me.fromgate.reactions.event.FactionRelationEvent;
import me.fromgate.reactions.event.FlightEvent;
import me.fromgate.reactions.event.GameModeEvent;
import me.fromgate.reactions.event.GodEvent;
import me.fromgate.reactions.event.ItemClickEvent;
import me.fromgate.reactions.event.ItemConsumeEvent;
import me.fromgate.reactions.event.ItemHeldEvent;
import me.fromgate.reactions.event.ItemHoldEvent;
import me.fromgate.reactions.event.ItemWearEvent;
import me.fromgate.reactions.event.JoinEvent;
import me.fromgate.reactions.event.LeverEvent;
import me.fromgate.reactions.event.MessageEvent;
import me.fromgate.reactions.event.MobClickEvent;
import me.fromgate.reactions.event.MobDamageEvent;
import me.fromgate.reactions.event.MobKillEvent;
import me.fromgate.reactions.event.PvpKillEvent;
import me.fromgate.reactions.event.PickupItemEvent;
import me.fromgate.reactions.event.PlateEvent;
import me.fromgate.reactions.event.PlayerBlockBreakEvent;
import me.fromgate.reactions.event.PlayerInventoryClickEvent;
import me.fromgate.reactions.event.PlayerRespawnedEvent;
import me.fromgate.reactions.event.PlayerWasKilledEvent;
import me.fromgate.reactions.event.QuitEvent;
import me.fromgate.reactions.event.RegionEnterEvent;
import me.fromgate.reactions.event.RegionEvent;
import me.fromgate.reactions.event.RegionLeaveEvent;
import me.fromgate.reactions.event.SignEvent;
import me.fromgate.reactions.event.SneakEvent;
import me.fromgate.reactions.event.VariableEvent;
import me.fromgate.reactions.event.WeChangeEvent;
import me.fromgate.reactions.event.WeSelectionRegionEvent;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public enum ActivatorType {
    // алиас, класс активатора, класс события
    BUTTON("b", ButtonActivator.class, ButtonEvent.class, true),
    PLATE("plt", PlateActivator.class, PlateEvent.class, true),
    REGION("rg", RegionActivator.class, RegionEvent.class),
    REGION_ENTER("rgenter", RegionEnterActivator.class, RegionEnterEvent.class),
    REGION_LEAVE("rgleave", RegionLeaveActivator.class, RegionLeaveEvent.class),
    EXEC("exe", ExecActivator.class, ExecEvent.class),
    COMMAND("cmd", CommandActivator.class, CommandEvent.class),
    MESSAGE("msg", MessageActivator.class, MessageEvent.class),
    PVP_KILL("pvpkill", PvpKillActivator.class, PvpKillEvent.class),
    PLAYER_DEATH("PVP_DEATH", PlayerDeathActivator.class, PlayerWasKilledEvent.class),
    PLAYER_RESPAWN("PVP_RESPAWN", PlayerRespawnActivator.class, PlayerRespawnedEvent.class),
    LEVER("lvr", LeverActivator.class, LeverEvent.class, true),
    DOOR("door", DoorActivator.class, DoorEvent.class, true),
    JOIN("join", JoinActivator.class, JoinEvent.class),
    QUIT("quit", QuitActivator.class, QuitEvent.class),
    MOB_CLICK("mobclick", MobClickActivator.class, MobClickEvent.class),
    MOB_KILL("mobkill", MobKillActivator.class, MobKillEvent.class),
    MOB_DAMAGE("mobdamage", MobDamageActivator.class, MobDamageEvent.class),
    ITEM_CLICK("itemclick", ItemClickActivator.class, ItemClickEvent.class),
    ITEM_CONSUME("consume", ItemConsumeActivator.class, ItemConsumeEvent.class),
    ITEM_HOLD("itemhold", ItemHoldActivator.class, ItemHoldEvent.class),
    ITEM_HELD("itemheld", ItemHeldActivator.class, ItemHeldEvent.class),
    ITEM_WEAR("itemwear", ItemWearActivator.class, ItemWearEvent.class),
    FCT_CHANGE("faction", FactionActivator.class, FactionEvent.class),
    FCT_RELATION("fctrelation", FactionRelationActivator.class, FactionRelationEvent.class),
    FCT_CREATE("fctcreate", FactionCreateActivator.class, FactionCreateEvent.class),
    FCT_DISBAND("fctdisband", FactionDisbandActivator.class, FactionDisbandEvent.class),
    SIGN("sign", SignActivator.class, SignEvent.class, true),
    BLOCK_CLICK("blockclick", BlockClickActivator.class, BlockClickEvent.class, true),
    INVENTORY_CLICK("inventoryclick", InventoryClickActivator.class, PlayerInventoryClickEvent.class),
    DROP("drop", DropActivator.class, DropEvent.class),
    PICKUP_ITEM("pickupitem", PickupItemActivator.class, PickupItemEvent.class),
    FLIGHT("flight", FlightActivator.class, FlightEvent.class),
    ENTITY_CLICK("entityclick", EntityClickActivator.class, EntityClickEvent.class),
    BLOCK_BREAK("blockbreak", BlockBreakActivator.class, PlayerBlockBreakEvent.class, true),
    SNEAK("sneak", SneakActivator.class, SneakEvent.class),
    DAMAGE("damage", DamageActivator.class, DamageEvent.class),
    DAMAGE_BY_MOB("damagebymob", DamageByMobActivator.class, DamageByMobEvent.class),
    DAMAGE_BY_BLOCK("damagebyblock", DamageByBlockActivator.class, DamageByBlockEvent.class, true),
    VARIABLE("var", VariableActivator.class, VariableEvent.class),
    WE_SELECTION_REGION("weselectionregion", WeSelectionRegionActivator.class, WeSelectionRegionEvent.class),
    WE_CHANGE("wechange", WeChangeActivator.class, WeChangeEvent.class),
    GAME_MODE("gamemode", GameModeActivator.class, GameModeEvent.class),
    GOD("god", GodActivator.class, GodEvent.class);

    private String alias;
    private Class<? extends Activator> aclass;
    private Class<? extends Event> eclass;
    private boolean needTargetBlock;


    ActivatorType(String alias, Class<? extends Activator> actclass, Class<? extends Event> evntclass, boolean needTargetBlock) {
        this.alias = alias;
        this.aclass = actclass;
        this.eclass = evntclass;
        this.needTargetBlock = needTargetBlock;
    }

    ActivatorType(String alias, Class<? extends Activator> actclass, Class<? extends Event> evntclass) {
        this(alias, actclass, evntclass, false);
    }

    public Class<? extends Activator> getActivatorClass() {
        return aclass;
    }

    public Class<? extends Event> getEventClass() {
        return eclass;
    }

    public Activator create(String name, String param) {
        return create(name, null, param);
    }

    public Activator create(String name, Block targetBlock, String param) {
        Constructor<? extends Activator> constructor;
        Activator activator = null;
        try {
            if (this.needTargetBlock) {
                constructor = aclass.getConstructor(String.class, Block.class, String.class);
                activator = constructor.newInstance(name, targetBlock, param);
            } else {
                constructor = aclass.getConstructor(String.class, String.class);
                activator = constructor.newInstance(name, param);
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        return activator;
    }


    public String getAlias() {
        return this.alias;
    }

    public static boolean isValid(String str) {
        for (ActivatorType at : ActivatorType.values())
            if (at.name().equalsIgnoreCase(str) || at.alias.equalsIgnoreCase(str)) return true;
        return false;
    }

    public boolean isValidEvent(Event event) {
        return eclass.isInstance(event);
    }

    public static ActivatorType getByName(String name) {
        for (ActivatorType at : ActivatorType.values())
            if (at.name().equalsIgnoreCase(name) || at.getAlias().equalsIgnoreCase(name)) return at;
        return null;
    }

    public static void listActivators(CommandSender sender, int pageNum) {
        List<String> activatorList = new ArrayList<>();
        for (ActivatorType activatorType : ActivatorType.values()) {
            String name = activatorType.name();
            String alias = activatorType.getAlias().equalsIgnoreCase(name) ? " " : " (" + activatorType.getAlias() + ") ";
            Msg activatorDesc = Msg.getByName("ACTIVATOR_" + name);
            if (activatorDesc == null) {
                Msg.LNG_MISSED_ACTIVATOR_DESC.log(name);
            } else {
                activatorList.add("&6" + name + "&e" + alias + "&3: &a" + activatorDesc.getText("NOCOLOR"));
            }
        }
        Util.printPage(sender, activatorList, Msg.MSG_ACTIVATORLISTTITLE, pageNum);
    }


}
