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

package me.fromgate.reactions.module.basics.activators;

import lombok.Getter;
import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.util.RaGenerator;
import me.fromgate.reactions.util.message.BukkitMessenger;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// TODO: Will be irrelevant because of modules(externals) system
@Deprecated
public enum OldActivatorType {
    /*
     TODO: More activators
     GlideActivator, MoneyTransactionActivator, PotionSplashActivator, ProjectileHitActivator
    */
    EXEC("exe", ExecActivator::create, ExecActivator::load),
    BUTTON("b", ButtonActivator::create, ButtonActivator::load, true, true),
    PLATE("plt", PlateActivator::create, PlateActivator::load, true, true),
    TELEPORT("tp", TeleportActivator::create, TeleportActivator::load, true),
    COMMAND("cmd", CommandActivator::create, CommandActivator::load),
    MESSAGE("msg", MessageActivator::create, MessageActivator::load),
    PVP_KILL("pvpkill", PvpKillActivator::create, PvpKillActivator::load),
    DEATH("player_death", DeathActivator::create, DeathActivator::load),
    RESPAWN("player_respawn", RespawnActivator::create, RespawnActivator::load),
    LEVER("lvr", LeverActivator::create, LeverActivator::load, true, true),
    DOOR("door", DoorActivator::create, DoorActivator::load, true, true),
    JOIN("join", JoinActivator::create, JoinActivator::load),
    QUIT("quit", QuitActivator::create, QuitActivator::load),
    MOB_CLICK("mobclick", MobClickActivator::create, MobClickActivator::load, true),
    MOB_KILL("mobkill", MobKillActivator::create, MobKillActivator::load),
    MOB_DAMAGE("mobdamage", MobDamageActivator::create, MobDamageActivator::load),
    ITEM_CLICK("itemclick", ItemClickActivator::create, ItemClickActivator::load),
    ITEM_CONSUME("consume", ItemConsumeActivator::create, ItemConsumeActivator::load),
    ITEM_HOLD("itemhold", ItemHoldActivator::create, ItemHoldActivator::load),
    ITEM_HELD("itemheld", ItemHeldActivator::create, ItemHeldActivator::load),
    ITEM_WEAR("itemwear", ItemWearActivator::create, ItemWearActivator::load),
    SIGN("sign", SignActivator::create, SignActivator::load, true, true),
    BLOCK_CLICK("blockclick", BlockClickActivator::create, BlockClickActivator::load, true, true),
    INVENTORY_CLICK("inventoryclick", InventoryClickActivator::create, InventoryClickActivator::load),
    DROP("drop", DropActivator::create, DropActivator::load),
    PICKUP_ITEM("pickupitem", PickupItemActivator::create, PickupItemActivator::load),
    FLIGHT("flight", FlightActivator::create, FlightActivator::load),
    ENTITY_CLICK("entityclick", EntityClickActivator::create, EntityClickActivator::load),
    BLOCK_BREAK("blockbreak", BlockBreakActivator::create, BlockBreakActivator::load, true, true),
    SNEAK("sneak", SneakActivator::create, SneakActivator::load),
    DAMAGE("damage", DamageActivator::create, DamageActivator::load),
    DAMAGE_BY_MOB("damagebymob", DamageByMobActivator::create, DamageByMobActivator::load),
    DAMAGE_BY_BLOCK("damagebyblock", DamageByBlockActivator::create, DamageByBlockActivator::load, true, true),
    VARIABLE("var", VariableActivator::create, VariableActivator::load),
    GAMEMODE("gamemode", GameModeActivator::create, GameModeActivator::load),
    GOD("god", GodActivator::create, GodActivator::load),
    CUBOID("cube", CuboidActivator::create, CuboidActivator::load, true),
    WEATHER_CHANGE("weather", WeatherChangeActivator::create, WeatherChangeActivator::load),
    //PROJECTILE_HIT("projhit", ProjectileHitActivator::create, ProjectileHitActivator::load),
    /* WorldGuard */
    REGION("rg", RegionActivator::create, RegionActivator::load, true),
    REGION_ENTER("rgenter", RegionEnterActivator::create, RegionEnterActivator::load, true),
    REGION_LEAVE("rgleave", RegionLeaveActivator::create, RegionLeaveActivator::load, true),
    /* WorldEdit */
    WE_SELECTION_REGION("weselectionregion", WeSelectionActivator::create, WeSelectionActivator::load),
    WE_CHANGE("wechange", WeChangeActivator::create, WeChangeActivator::load);

    private static final Map<String, OldActivatorType> BY_NAME;

    static {
        Map<String, OldActivatorType> byName = new HashMap<>();
        for (OldActivatorType act : OldActivatorType.values()) {
            byName.put(act.name(), act);
            byName.put(act.alias.toUpperCase(Locale.ENGLISH), act);
        }
        BY_NAME = Collections.unmodifiableMap(byName);
    }

    private final String alias;
    private final RaGenerator<Parameters> creator;
    private final RaGenerator<ConfigurationSection> loader;
    @Getter
    private final boolean needBlock;
    @Getter
    private final boolean locatable;

    OldActivatorType(String alias, RaGenerator<Parameters> creator, RaGenerator<ConfigurationSection> loader, boolean locatable, boolean needBlock) {
        this.alias = alias.toUpperCase(Locale.ENGLISH);
        this.creator = creator;
        this.loader = loader;
        this.needBlock = needBlock;
        this.locatable = locatable;
    }

    OldActivatorType(String alias, RaGenerator<Parameters> creator, RaGenerator<ConfigurationSection> loader, boolean locatable) {
        this(alias, creator, loader, locatable, false);
    }

    OldActivatorType(String alias, RaGenerator<Parameters> creator, RaGenerator<ConfigurationSection> loader) {
        this(alias, creator, loader, false);
    }

    @SuppressWarnings("unused")
    public static boolean isValid(String str) {
        return getByName(str) != null;
    }

    public static OldActivatorType getByName(String name) {
        return BY_NAME.get(name.toUpperCase(Locale.ENGLISH));
    }

    public static void listActivators(CommandSender sender, int pageNum) {
        List<String> activatorList = new ArrayList<>();
        for (OldActivatorType activatorType : OldActivatorType.values()) {
            String name = activatorType.name();
            String alias = activatorType.alias.equalsIgnoreCase(name) ? " " : " (" + activatorType.alias + ") ";
            Msg activatorDesc = Msg.getByName("ACTIVATOR_" + name);
            if (activatorDesc == null) {
                Msg.LNG_MISSED_ACTIVATOR_DESC.log(name);
            } else {
                activatorList.add("&6" + name + "&e" + alias + "&3: &a" + activatorDesc.getText("NOCOLOR"));
            }
        }
        BukkitMessenger.printPage(sender, activatorList, Msg.MSG_ACTIVATORLISTTITLE, pageNum);
    }

    public Activator create(String name, String group, Parameters param) {
        ActivatorLogic base = new ActivatorLogic(name, group);
        return creator.generate(base, param);
    }

    public Activator load(String name, String group, ConfigurationSection cfg) {
        ActivatorLogic base = new ActivatorLogic(name, group, cfg);
        return loader.generate(base, cfg);
    }

}

/*
public abstract class ActivatorType {
	@Getter private final String name;

	private static Map<String, ActivatorType> byName;

	public ActivatorType(String name) {
		this.name  name;
	}

	public static register(ActivatorType type, String alias) {
		byName.put(type.getame.toUpperCase(Locale.ENGLISH), this);
		byName.put(alias.toUpperCase(Locale.ENGLISH), this);
	}

	public abstract Activator create(ActivatorBase logic, Param params);

	public abstract Activator load(ActivatorBase logic, ConfigurationSection cfg);
}
*/