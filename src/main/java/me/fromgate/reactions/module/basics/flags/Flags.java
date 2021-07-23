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

package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activity.flags.OldFlag;
import me.fromgate.reactions.logic.activity.flags.StoredFlag;
import me.fromgate.reactions.module.basics.flags.worldedit.FlagRegionInRadius;
import me.fromgate.reactions.module.basics.flags.worldedit.FlagSelectionBlocks;
import me.fromgate.reactions.module.basics.flags.worldedit.FlagSuperPickAxe;
import me.fromgate.reactions.module.basics.flags.worldedit.FlagToolControl;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.message.BukkitMessenger;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.message.RaDebug;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// TODO: Will be irrelevant because of modules(externals) system
@Deprecated
public enum Flags {
    /*
     TODO: More flags
     FlagCuboid, FlagString(for strings checking), FlagDynamic(for flags from placeholders)
     FlagStatistic(checking player's stats)
    */
    GROUP("group", new FlagGroup(), true),
    PERM("perm", new FlagPerm(), true),
    TIME("time", new FlagTime()),
    ITEM("item", new FlagItem(FlagItem.Type.HAND), true),
    ITEM_INVENTORY("invitem", new FlagItem(FlagItem.Type.INVENTORY), true),
    ITEM_WEAR("invwear", new FlagItem(FlagItem.Type.WEAR), true),
    ITEM_OFFHAND("itemoffhand", new FlagItem(FlagItem.Type.OFFHAND), true),
    BLOCK_CHECK("block", new FlagBlock()),
    MONEY("money", new FlagMoney()),
    CHANCE("chance", new FlagChance()),
    PVP("pvp", new FlagPvp(), true),
    ONLINE("online", new FlagOnline()),
    DELAY("delay", new FlagDelay(true)),
    DELAY_PLAYER("pdelay", new FlagDelay(false), true),
    STATE("pose", new FlagState(), true),
    REGION("region", new FlagRegion(FlagRegion.Type.REGION), true),
    REGION_PLAYERS("rgplayer", new FlagRegion(FlagRegion.Type.REGION_PLAYERS)),
    REGION_MEMBER("rgmember", new FlagRegion(FlagRegion.Type.REGION_MEMBER)),
    REGION_OWNER("rgowner", new FlagRegion(FlagRegion.Type.REGION_OWNER)),
    REGION_STATE("rgstate", new FlagRegion(FlagRegion.Type.REGION_STATE)),
    GAMEMODE("gm", new FlagGamemode(), true),
    FOODLEVEL("food", new FlagFoodLevel(), true),
    XP("xp", new FlagXP(), true),
    LEVEL("level", new FlagLevel(), true),
    HEALTH("hp", new FlagHealth(), true),
    POWER("powered", new FlagPowered()),
    WORLD("world", new FlagWorld(), true),
    BIOME("biome", new FlagBiome(), true),
    LIGHT_LEVEL("light", new FlagLightLevel(), true),
    WALK_BLOCK("walk", new FlagWalkBlock(), true),
    DIRECTION("dir", new FlagDirection(), true),
    FLAG_SET("flagset", new FlagFlagSet()),
    EXECUTE_STOP("stopped", new FlagExecStop()),
    VAR_EXIST("varexist", new FlagVar(FlagVar.Type.EXIST, false)),
    VAR_COMPARE("varcmp", new FlagVar(FlagVar.Type.COMPARE, false)),
    VAR_GREATER("vargrt", new FlagVar(FlagVar.Type.GREATER, false)),
    VAR_LOWER("varlwr", new FlagVar(FlagVar.Type.LOWER, false)),
    VAR_MATCH("varmatch", new FlagVar(FlagVar.Type.MATCH, false)),
    VAR_PLAYER_EXIST("varpexist", new FlagVar(FlagVar.Type.EXIST, true), true),
    VAR_PLAYER_COMPARE("varpcmp", new FlagVar(FlagVar.Type.COMPARE, true), true),
    VAR_PLAYER_GREATER("varpgrt", new FlagVar(FlagVar.Type.GREATER, true), true),
    VAR_PLAYER_LOWER("varplwr", new FlagVar(FlagVar.Type.LOWER, true), true),
    VAR_PLAYER_MATCH("varpmatch", new FlagVar(FlagVar.Type.MATCH, true), true),
    /*
    VAR_TEMP_EXIST
    VAR_TEMP_COMPARE
    VAR_TEMP_GREATER
    VAR_TEMP_LOWER
    VAR_TEMP_MATCH
    */
    COMPARE("cmp", new FlagCompare()),
    GREATER("greater", new FlagGreaterLower(true)),
    LOWER("lower", new FlagGreaterLower(false)),
    WEATHER("weather", new FlagWeather()),
    TIMER_ACTIVE("timeract", new FlagTimerActive()),
    SQL_CHECK("sqlcheck", new FlagSQL(true)),
    SQL_RESULT("sqlhasresult", new FlagSQL(false)),
    FLY_SPEED("flyspeed", new FlagFlySpeed(), true),
    WALK_SPEED("walkspeed", new FlagWalkSpeed(), true),
    WE_SEL_BLOCKS("selblocks", new FlagSelectionBlocks(), true),
    WE_SUPERPICKAXE("superpickaxe", new FlagSuperPickAxe(), true),
    WE_TOOLCONTROL("toolcontrol", new FlagToolControl(), true),
    REGION_IN_RADIUS("regioninradius", new FlagRegionInRadius(), true),
    CHECK_ONLINE("checkonline", new FlagCheckOnline()),
    REGEX("regex", new FlagRegex()),
    HELD_SLOT("slot", new FlagHeldSlot(), true);

    private static final Map<String, Flags> BY_NAME;

    static {
        Map<String, Flags> byName = new HashMap<>();
        for (Flags flg : Flags.values()) {
            byName.put(flg.name(), flg);
            byName.put(flg.alias.toUpperCase(Locale.ENGLISH), flg);
        }
        BY_NAME = Collections.unmodifiableMap(byName);
    }

    private final String alias;
    private final boolean requirePlayer;
    private final OldFlag flag;

    Flags(String alias, OldFlag flag, boolean requirePlayer) {
        this.alias = alias;
        this.requirePlayer = requirePlayer;
        this.flag = flag;
    }

    Flags(String alias, OldFlag flag) {
        this(alias, flag, false);
    }

    public static Flags getByName(String name) {
        return BY_NAME.get(name.toUpperCase(Locale.ENGLISH));
    }

    public static boolean isValid(String name) {
        return getByName(name) != null;
    }

    public static boolean checkFlag(RaContext context, String flag, String param, boolean not) {
        return checkFlag(context, getByName(flag), param, not);
    }

    public static boolean checkFlag(RaContext context, Flags flag, String param, boolean not) {
        if (flag == null || Utils.isStringEmpty(param)) return false;
        context.setVariable((flag + "_flag").toUpperCase(Locale.ENGLISH), param);
        boolean check = flag.check(context, param);
        if (not) return !check;
        context.setVariable((flag + "_flag_val").toUpperCase(Locale.ENGLISH), String.valueOf(check));
        return check;
    }

    public static boolean checkFlags(RaContext context, ActivatorLogic c) {
        return RaDebug.checkFlagAndDebug(context.getPlayer(), checkAllFlags(context, c));
    }

    public static boolean checkAllFlags(RaContext context, ActivatorLogic c) {
        if (c.getFlags().size() > 0)
            for (int i = 0; i < c.getFlags().size(); i++) {
                StoredFlag f = c.getFlags().get(i);
                context.setVariable((f.getFlagName() + "_flag").toUpperCase(Locale.ENGLISH), f.getParameters());
                if (!checkFlag(context, f.getFlag(),
                        ReActions.getPlaceholders().parsePlaceholders(context, f.getParameters()), f.isInverted()))
                    return false;
            }
        return true;
    }

    @SuppressWarnings("unused")
    public static String getFtypes() {
        String str = "";
        for (Flags f : Flags.values()) {
            str = (str.isEmpty() ? f.name() : str + "," + f.name());
            str = (str.isEmpty() ? f.alias : str + "," + f.alias);
        }
        return str;
    }

    @SuppressWarnings("unused")
    public static String getValidName(String flag) {
        Flags flg = getByName(flag);
        if (flg != null) return flg.name();
        return flag;
    }

    public static void listFlags(CommandSender sender, int pageNum) {
        List<String> flagList = new ArrayList<>();
        for (Flags flagType : Flags.values()) {
            String flagName = flagType.name();
            String alias = flagType.alias.equalsIgnoreCase(flagName) ? " " : " (" + flagType.alias + ") ";

            Msg msg = Msg.getByName("flag_" + flagName);
            if (msg == null) {
                Msg.LNG_FAIL_FLAG_DESC.log(flagName);
            } else {
                flagList.add("&6" + flagName + "&e" + alias + "&3: &a" + msg.getText("NOCOLOR"));
            }
        }
        BukkitMessenger.printPage(sender, flagList, Msg.MSG_FLAGLISTTITLE, pageNum);
    }

    public boolean check(RaContext context, String param) {
        if (this.requirePlayer && (context.getPlayer() == null)) return false;
        return flag.checkFlag(context, param);
    }


}
