package me.fromgate.reactions.module;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.ActivatorType;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.module.basics.actions.ActionBack;
import me.fromgate.reactions.module.basics.actions.ActionBlockFill;
import me.fromgate.reactions.module.basics.actions.ActionBlockSet;
import me.fromgate.reactions.module.basics.actions.ActionBroadcast;
import me.fromgate.reactions.module.basics.actions.ActionCancelEvent;
import me.fromgate.reactions.module.basics.actions.ActionChange;
import me.fromgate.reactions.module.basics.actions.ActionChatMessage;
import me.fromgate.reactions.module.basics.actions.ActionClearRadius;
import me.fromgate.reactions.module.basics.actions.ActionClearRegion;
import me.fromgate.reactions.module.basics.actions.ActionCommand;
import me.fromgate.reactions.module.basics.actions.ActionDamage;
import me.fromgate.reactions.module.basics.actions.ActionDelay;
import me.fromgate.reactions.module.basics.actions.ActionDelayed;
import me.fromgate.reactions.module.basics.actions.ActionExecStop;
import me.fromgate.reactions.module.basics.actions.ActionExecUnstop;
import me.fromgate.reactions.module.basics.actions.ActionExecute;
import me.fromgate.reactions.module.basics.actions.ActionFile;
import me.fromgate.reactions.module.basics.actions.ActionFly;
import me.fromgate.reactions.module.basics.actions.ActionFlySpeed;
import me.fromgate.reactions.module.basics.actions.ActionGlide;
import me.fromgate.reactions.module.basics.actions.ActionGroupAdd;
import me.fromgate.reactions.module.basics.actions.ActionGroupRemove;
import me.fromgate.reactions.module.basics.actions.ActionHeal;
import me.fromgate.reactions.module.basics.actions.ActionIfElse;
import me.fromgate.reactions.module.basics.actions.ActionItems;
import me.fromgate.reactions.module.basics.actions.ActionLog;
import me.fromgate.reactions.module.basics.actions.ActionMenuItem;
import me.fromgate.reactions.module.basics.actions.ActionMessage;
import me.fromgate.reactions.module.basics.actions.ActionMobSpawn;
import me.fromgate.reactions.module.basics.actions.ActionMoneyGive;
import me.fromgate.reactions.module.basics.actions.ActionMoneyPay;
import me.fromgate.reactions.module.basics.actions.ActionPlayerId;
import me.fromgate.reactions.module.basics.actions.ActionPotion;
import me.fromgate.reactions.module.basics.actions.ActionPotionRemove;
import me.fromgate.reactions.module.basics.actions.ActionPowerSet;
import me.fromgate.reactions.module.basics.actions.ActionRegex;
import me.fromgate.reactions.module.basics.actions.ActionResponse;
import me.fromgate.reactions.module.basics.actions.ActionShoot;
import me.fromgate.reactions.module.basics.actions.ActionSignSet;
import me.fromgate.reactions.module.basics.actions.ActionSound;
import me.fromgate.reactions.module.basics.actions.ActionSql;
import me.fromgate.reactions.module.basics.actions.ActionTimer;
import me.fromgate.reactions.module.basics.actions.ActionTp;
import me.fromgate.reactions.module.basics.actions.ActionVar;
import me.fromgate.reactions.module.basics.actions.ActionVelocity;
import me.fromgate.reactions.module.basics.actions.ActionVelocityJump;
import me.fromgate.reactions.module.basics.actions.ActionWait;
import me.fromgate.reactions.module.basics.actions.ActionWalkSpeed;
import me.fromgate.reactions.module.basics.actions.ActionWeSuperPickaxe;
import me.fromgate.reactions.module.basics.actions.ActionWeToolControl;
import me.fromgate.reactions.module.basics.activators.BlockBreakActivator;
import me.fromgate.reactions.module.basics.activators.BlockClickActivator;
import me.fromgate.reactions.module.basics.activators.ButtonActivator;
import me.fromgate.reactions.module.basics.activators.CommandActivator;
import me.fromgate.reactions.module.basics.activators.CuboidActivator;
import me.fromgate.reactions.module.basics.activators.DamageActivator;
import me.fromgate.reactions.module.basics.activators.DamageByBlockActivator;
import me.fromgate.reactions.module.basics.activators.DamageByMobActivator;
import me.fromgate.reactions.module.basics.activators.DeathActivator;
import me.fromgate.reactions.module.basics.activators.DoorActivator;
import me.fromgate.reactions.module.basics.activators.DropActivator;
import me.fromgate.reactions.module.basics.activators.EntityClickActivator;
import me.fromgate.reactions.module.basics.activators.ExecActivator;
import me.fromgate.reactions.module.basics.activators.FlightActivator;
import me.fromgate.reactions.module.basics.activators.GameModeActivator;
import me.fromgate.reactions.module.basics.activators.GodActivator;
import me.fromgate.reactions.module.basics.activators.InventoryClickActivator;
import me.fromgate.reactions.module.basics.activators.ItemClickActivator;
import me.fromgate.reactions.module.basics.activators.ItemConsumeActivator;
import me.fromgate.reactions.module.basics.activators.ItemHeldActivator;
import me.fromgate.reactions.module.basics.activators.ItemHoldActivator;
import me.fromgate.reactions.module.basics.activators.ItemWearActivator;
import me.fromgate.reactions.module.basics.activators.JoinActivator;
import me.fromgate.reactions.module.basics.activators.LeverActivator;
import me.fromgate.reactions.module.basics.activators.MessageActivator;
import me.fromgate.reactions.module.basics.activators.MobClickActivator;
import me.fromgate.reactions.module.basics.activators.MobDamageActivator;
import me.fromgate.reactions.module.basics.activators.MobKillActivator;
import me.fromgate.reactions.module.basics.activators.PickupItemActivator;
import me.fromgate.reactions.module.basics.activators.PlateActivator;
import me.fromgate.reactions.module.basics.activators.PvpKillActivator;
import me.fromgate.reactions.module.basics.activators.QuitActivator;
import me.fromgate.reactions.module.basics.activators.RegionActivator;
import me.fromgate.reactions.module.basics.activators.RegionEnterActivator;
import me.fromgate.reactions.module.basics.activators.RegionLeaveActivator;
import me.fromgate.reactions.module.basics.activators.RespawnActivator;
import me.fromgate.reactions.module.basics.activators.SignActivator;
import me.fromgate.reactions.module.basics.activators.SneakActivator;
import me.fromgate.reactions.module.basics.activators.TeleportActivator;
import me.fromgate.reactions.module.basics.activators.VariableActivator;
import me.fromgate.reactions.module.basics.activators.WEChangeActivator;
import me.fromgate.reactions.module.basics.activators.WESelectionActivator;
import me.fromgate.reactions.module.basics.activators.WeatherChangeActivator;
import me.fromgate.reactions.module.basics.flags.FlagBiome;
import me.fromgate.reactions.module.basics.flags.FlagBlock;
import me.fromgate.reactions.module.basics.flags.FlagChance;
import me.fromgate.reactions.module.basics.flags.FlagCheckOnline;
import me.fromgate.reactions.module.basics.flags.FlagCompare;
import me.fromgate.reactions.module.basics.flags.FlagDelay;
import me.fromgate.reactions.module.basics.flags.FlagDirection;
import me.fromgate.reactions.module.basics.flags.FlagExecuteStop;
import me.fromgate.reactions.module.basics.flags.FlagExperience;
import me.fromgate.reactions.module.basics.flags.FlagFlagSet;
import me.fromgate.reactions.module.basics.flags.FlagFlySpeed;
import me.fromgate.reactions.module.basics.flags.FlagFoodLevel;
import me.fromgate.reactions.module.basics.flags.FlagGamemode;
import me.fromgate.reactions.module.basics.flags.FlagGreaterLower;
import me.fromgate.reactions.module.basics.flags.FlagGroup;
import me.fromgate.reactions.module.basics.flags.FlagHealth;
import me.fromgate.reactions.module.basics.flags.FlagHeldSlot;
import me.fromgate.reactions.module.basics.flags.FlagItem;
import me.fromgate.reactions.module.basics.flags.FlagLevel;
import me.fromgate.reactions.module.basics.flags.FlagLightLevel;
import me.fromgate.reactions.module.basics.flags.FlagMoney;
import me.fromgate.reactions.module.basics.flags.FlagOnline;
import me.fromgate.reactions.module.basics.flags.FlagPerm;
import me.fromgate.reactions.module.basics.flags.FlagPowered;
import me.fromgate.reactions.module.basics.flags.FlagPvp;
import me.fromgate.reactions.module.basics.flags.FlagRegex;
import me.fromgate.reactions.module.basics.flags.FlagRegion;
import me.fromgate.reactions.module.basics.flags.FlagRegionInRadius;
import me.fromgate.reactions.module.basics.flags.FlagSQL;
import me.fromgate.reactions.module.basics.flags.FlagSelectionBlocks;
import me.fromgate.reactions.module.basics.flags.FlagState;
import me.fromgate.reactions.module.basics.flags.FlagSuperPickaxe;
import me.fromgate.reactions.module.basics.flags.FlagTime;
import me.fromgate.reactions.module.basics.flags.FlagTimerActive;
import me.fromgate.reactions.module.basics.flags.FlagToolControl;
import me.fromgate.reactions.module.basics.flags.FlagVar;
import me.fromgate.reactions.module.basics.flags.FlagWalkBlock;
import me.fromgate.reactions.module.basics.flags.FlagWalkSpeed;
import me.fromgate.reactions.module.basics.flags.FlagWeather;
import me.fromgate.reactions.module.basics.flags.FlagWorld;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.selectors.Selector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static me.fromgate.reactions.logic.activators.ActivatorsManager.typeOf;

public class BasicModule implements Module {
    @Override
    public @NotNull String getName() {
        return "reactions";
    }

    @Override
    public @NotNull String @NotNull [] getAuthors() {
        return new String[] {"fromgate", "MaxDikiy", "imDaniX"};
    }

    @Override
    public @NotNull Collection<ActivatorType> getActivatorTypes() {
        return List.of(
                typeOf(ExecActivator.class, "EXEC", ExecActivator::create, ExecActivator::load),
                typeOf(ButtonActivator.class, "BUTTON", ButtonActivator::create, ButtonActivator::load, true),
                typeOf(PlateActivator.class, "PLATE", PlateActivator::create, PlateActivator::load, true),
                typeOf(TeleportActivator.class, "TELEPORT", TeleportActivator::create, TeleportActivator::load),
                typeOf(CommandActivator.class, "COMMAND", CommandActivator::create, CommandActivator::load),
                typeOf(MessageActivator.class, "MESSAGE", MessageActivator::create, MessageActivator::load),
                typeOf(PvpKillActivator.class, "PVP_KILL", PvpKillActivator::create, PvpKillActivator::load),
                typeOf(DeathActivator.class, "DEATH", DeathActivator::create, DeathActivator::load),
                typeOf(RespawnActivator.class, "RESPAWN", RespawnActivator::create, RespawnActivator::load),
                typeOf(LeverActivator.class, "LEVER", LeverActivator::create, LeverActivator::load, true),
                typeOf(DoorActivator.class, "DOOR", DoorActivator::create, DoorActivator::load, true),
                typeOf(JoinActivator.class, "JOIN", JoinActivator::create, JoinActivator::load),
                typeOf(QuitActivator.class, "QUIT", QuitActivator::create, QuitActivator::load),
                typeOf(MobClickActivator.class, "MOB_CLICK", MobClickActivator::create, MobClickActivator::load),
                typeOf(MobKillActivator.class, "MOB_KILL", MobKillActivator::create, MobKillActivator::load),
                typeOf(MobDamageActivator.class, "MOB_DAMAGE", MobDamageActivator::create, MobDamageActivator::load),
                typeOf(ItemClickActivator.class, "ITEM_CLICK", ItemClickActivator::create, ItemClickActivator::load),
                typeOf(ItemConsumeActivator.class, "ITEM_CONSUME", ItemConsumeActivator::create, ItemConsumeActivator::load),
                typeOf(ItemHoldActivator.class, "ITEM_HOLD", ItemHoldActivator::create, ItemHoldActivator::load),
                typeOf(ItemHeldActivator.class, "ITEM_HELD", ItemHeldActivator::create, ItemHeldActivator::load),
                typeOf(ItemWearActivator.class, "ITEM_WEAR", ItemWearActivator::create, ItemWearActivator::load),
                typeOf(SignActivator.class, "SIGN", SignActivator::create, SignActivator::load, true),
                typeOf(BlockClickActivator.class, "BLOCK_CLICK", BlockClickActivator::create, BlockClickActivator::load, true),
                typeOf(InventoryClickActivator.class, "INVENTORY_CLICK", InventoryClickActivator::create, InventoryClickActivator::load),
                typeOf(DropActivator.class, "DROP", DropActivator::create, DropActivator::load),
                typeOf(PickupItemActivator.class, "PICKUP_ITEM", PickupItemActivator::create, PickupItemActivator::load),
                typeOf(FlightActivator.class, "FLIGHT", FlightActivator::create, FlightActivator::load),
                typeOf(EntityClickActivator.class, "ENTITY_CLICK", EntityClickActivator::create, EntityClickActivator::load),
                typeOf(BlockBreakActivator.class, "BLOCK_BREAK", BlockBreakActivator::create, BlockBreakActivator::load, true),
                typeOf(SneakActivator.class, "SNEAK", SneakActivator::create, SneakActivator::load),
                typeOf(DamageActivator.class, "DAMAGE", DamageActivator::create, DamageActivator::load),
                typeOf(DamageByMobActivator.class, "DAMAGE_BY_MOB", DamageByMobActivator::create, DamageByMobActivator::load),
                typeOf(DamageByBlockActivator.class, "DAMAGE_BY_BLOCK", DamageByBlockActivator::create, DamageByBlockActivator::load),
                typeOf(VariableActivator.class, "VARIABLE", VariableActivator::create, VariableActivator::load),
                typeOf(GameModeActivator.class, "GAMEMODE", GameModeActivator::create, GameModeActivator::load),
                typeOf(GodActivator.class, "GOD", GodActivator::create, GodActivator::load),
                typeOf(CuboidActivator.class, "CUBOID", CuboidActivator::create, CuboidActivator::load),
                typeOf(WeatherChangeActivator.class, "WEATHER_CHANGE", WeatherChangeActivator::create, WeatherChangeActivator::load),
                /* WorldGuard */
                typeOf(RegionActivator.class, "REGION", RegionActivator::create, RegionActivator::load),
                typeOf(RegionEnterActivator.class, "REGION_ENTER", RegionEnterActivator::create, RegionEnterActivator::load),
                typeOf(RegionLeaveActivator.class, "REGION_LEAVE", RegionLeaveActivator::create, RegionLeaveActivator::load),
                /* WorldEdit */
                typeOf(WESelectionActivator.class, "WE_SELECTION", WESelectionActivator::create, WESelectionActivator::load),
                typeOf(WEChangeActivator.class, "WE_CHANGE", WEChangeActivator::create, WEChangeActivator::load)
        );
    }

    @Override
    public @NotNull Collection<Action> getActions() {
        // TODO Split actions one-by-one? Or implement MultiAction?..
        return List.of(
                new ActionTp(),
                new ActionVelocity(),
                new ActionVelocityJump(),
                new ActionSound(),
                new ActionPotion(),
                new ActionPotionRemove(),
                new ActionGroupAdd(),
                new ActionGroupRemove(),
                new ActionMessage(),
                new ActionResponse(),
                new ActionChatMessage(),
                new ActionBroadcast(),
                new ActionDamage(),
                new ActionItems(ActionItems.Type.GIVE_ITEM),
                new ActionItems(ActionItems.Type.REMOVE_ITEM_HAND),
                new ActionItems(ActionItems.Type.REMOVE_ITEM_OFFHAND),
                new ActionItems(ActionItems.Type.REMOVE_ITEM_INVENTORY),
                new ActionItems(ActionItems.Type.DROP_ITEM),
                new ActionItems(ActionItems.Type.WEAR_ITEM),
                new ActionItems(ActionItems.Type.UNWEAR_ITEM),
                new ActionItems(ActionItems.Type.SET_INVENTORY),
                new ActionItems(ActionItems.Type.GET_INVENTORY),
                new ActionCommand(ActionCommand.Type.NORMAL),
                new ActionCommand(ActionCommand.Type.OP),
                new ActionCommand(ActionCommand.Type.CONSOLE),
                new ActionCommand(ActionCommand.Type.CHAT),
                new ActionMoneyPay(),
                new ActionMoneyGive(),
                new ActionDelay(true),
                new ActionDelay(false),
                new ActionBack(),
                new ActionMobSpawn(),
                new ActionExecute(),
                new ActionExecStop(),
                new ActionExecUnstop(),
                new ActionClearRegion(),
                new ActionHeal(),
                new ActionBlockSet(),
                new ActionBlockFill(),
                new ActionSignSet(),
                new ActionPowerSet(),
                new ActionShoot(),
                new ActionVar(ActionVar.Type.SET, false),
                new ActionVar(ActionVar.Type.CLEAR, false),
                new ActionVar(ActionVar.Type.INCREASE, false),
                new ActionVar(ActionVar.Type.DECREASE, false),
                new ActionVar(ActionVar.Type.SET, true),
                new ActionVar(ActionVar.Type.CLEAR, true),
                new ActionVar(ActionVar.Type.INCREASE, true),
                new ActionVar(ActionVar.Type.DECREASE, true),
                new ActionVar(ActionVar.Type.TEMPORARY_SET, false),
                new ActionChange(),
                new ActionTimer(true),
                new ActionTimer(false),
                new ActionCancelEvent(),
                new ActionSql(ActionSql.Type.SELECT),
                new ActionSql(ActionSql.Type.UPDATE),
                new ActionSql(ActionSql.Type.INSERT),
                new ActionSql(ActionSql.Type.DELETE),
                new ActionSql(ActionSql.Type.SET),
                new ActionRegex(),
                new ActionDelayed(),
                new ActionMenuItem(),
                new ActionWait(),
                new ActionLog(),
                new ActionPlayerId(),
                new ActionFile(),
                new ActionFly(),
                new ActionGlide(),
                new ActionWalkSpeed(),
                new ActionFlySpeed(),
                new ActionIfElse(),
                new ActionWeToolControl(),
                new ActionWeSuperPickaxe(),
                new ActionClearRadius()
        );
    }

    @Override
    public @NotNull Collection<Flag> getFlags() {
        // TODO
        return List.of(
                new FlagGroup(),
                new FlagPerm(),
                new FlagTime(),
                new FlagItem(FlagItem.Type.HAND),
                new FlagItem(FlagItem.Type.INVENTORY),
                new FlagItem(FlagItem.Type.WEAR),
                new FlagItem(FlagItem.Type.OFFHAND),
                new FlagBlock(),
                new FlagMoney(),
                new FlagChance(),
                new FlagPvp(),
                new FlagOnline(),
                new FlagDelay(true),
                new FlagDelay(false),
                new FlagState(),
                new FlagRegion(FlagRegion.Type.REGION),
                new FlagRegion(FlagRegion.Type.REGION_PLAYERS),
                new FlagRegion(FlagRegion.Type.REGION_MEMBER),
                new FlagRegion(FlagRegion.Type.REGION_OWNER),
                new FlagRegion(FlagRegion.Type.REGION_STATE),
                new FlagGamemode(),
                new FlagFoodLevel(),
                new FlagExperience(),
                new FlagLevel(),
                new FlagHealth(),
                new FlagPowered(),
                new FlagWorld(),
                new FlagBiome(),
                new FlagLightLevel(),
                new FlagWalkBlock(),
                new FlagDirection(),
                new FlagFlagSet(ReActions.getActivities()),
                new FlagExecuteStop(),
                new FlagVar(FlagVar.Type.EXIST, false),
                new FlagVar(FlagVar.Type.COMPARE, false),
                new FlagVar(FlagVar.Type.GREATER, false),
                new FlagVar(FlagVar.Type.LOWER, false),
                new FlagVar(FlagVar.Type.MATCH, false),
                new FlagVar(FlagVar.Type.EXIST, true),
                new FlagVar(FlagVar.Type.COMPARE, true),
                new FlagVar(FlagVar.Type.GREATER, true),
                new FlagVar(FlagVar.Type.LOWER, true),
                new FlagVar(FlagVar.Type.MATCH, true),
                new FlagCompare(),
                new FlagGreaterLower(true),
                new FlagGreaterLower(false),
                new FlagWeather(),
                new FlagTimerActive(),
                new FlagSQL(true),
                new FlagSQL(false),
                new FlagFlySpeed(),
                new FlagWalkSpeed(),
                new FlagSelectionBlocks(),
                new FlagSuperPickaxe(),
                new FlagToolControl(),
                new FlagRegionInRadius(),
                new FlagCheckOnline(),
                new FlagRegex(),
                new FlagHeldSlot()
        );
    }

    @Override
    public @NotNull Collection<Placeholder> getPlaceholders() {
        // TODO
        return Module.super.getPlaceholders();
    }

    @Override
    public @NotNull Collection<Selector> getSelectors() {
        // TODO
        return Module.super.getSelectors();
    }
}
