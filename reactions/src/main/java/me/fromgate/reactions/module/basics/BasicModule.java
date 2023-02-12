package me.fromgate.reactions.module.basics;

import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.MineDownParser;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.ActivatorType;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.module.Module;
import me.fromgate.reactions.module.basics.actions.*;
import me.fromgate.reactions.module.basics.activators.*;
import me.fromgate.reactions.module.basics.flags.*;
import me.fromgate.reactions.module.basics.placeholders.*;
import me.fromgate.reactions.module.basics.selectors.GroupSelector;
import me.fromgate.reactions.module.basics.selectors.LocSelector;
import me.fromgate.reactions.module.basics.selectors.PermSelector;
import me.fromgate.reactions.module.basics.selectors.PlayerSelector;
import me.fromgate.reactions.module.basics.selectors.RegionSelector;
import me.fromgate.reactions.module.basics.selectors.WorldSelector;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.selectors.Selector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static me.fromgate.reactions.logic.activators.ActivatorTypesRegistry.typeOf;

public class BasicModule implements Module {
    @Override
    public boolean isPluginDepended() {
        return false; // TODO: Should return true, but needs splitting into plugin modules
    }

    @Override
    public @NotNull String getName() {
        return "reactions";
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return List.of("fromgate", "MaxDikiy", "imDaniX");
    }

    @Override
    public @NotNull Collection<ActivatorType> getActivatorTypes(@NotNull ReActions.Platform platform) {
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
                // WorldGuard
                typeOf(RegionActivator.class, "REGION", RegionActivator::create, RegionActivator::load),
                typeOf(RegionEnterActivator.class, "REGION_ENTER", RegionEnterActivator::create, RegionEnterActivator::load),
                typeOf(RegionLeaveActivator.class, "REGION_LEAVE", RegionLeaveActivator::create, RegionLeaveActivator::load),
                // WorldEdit
                typeOf(WESelectionActivator.class, "WE_SELECTION", WESelectionActivator::create, WESelectionActivator::load),
                typeOf(WEChangeActivator.class, "WE_CHANGE", WEChangeActivator::create, WEChangeActivator::load)
        );
    }

    @Override
    public @NotNull Collection<Action> getActions(@NotNull ReActions.Platform platform) {
        return List.of(
                new TeleportAction(),
                new VelocityAction(),
                new VelocityJumpAction(),
                new SoundAction(),
                new PotionAction(),
                new PotionRemoveAction(),
                new MessageAction(),
                new ResponseAction(),
                new SendChatAction(),
                new BroadcastAction(),
                new DamageAction(),
                new ItemActions(ItemActions.Type.GIVE_ITEM),
                new ItemActions(ItemActions.Type.REMOVE_ITEM_HAND),
                new ItemActions(ItemActions.Type.REMOVE_ITEM_OFFHAND),
                new ItemActions(ItemActions.Type.REMOVE_ITEM_INVENTORY),
                new ItemActions(ItemActions.Type.DROP_ITEM),
                new ItemActions(ItemActions.Type.WEAR_ITEM),
                new ItemActions(ItemActions.Type.UNWEAR_ITEM),
                new ItemActions(ItemActions.Type.SET_INVENTORY),
                new ItemActions(ItemActions.Type.GET_INVENTORY),
                new CommandActions(CommandActions.Type.NORMAL),
                new CommandActions(CommandActions.Type.OP),
                new CommandActions(CommandActions.Type.CONSOLE),
                new CommandActions(CommandActions.Type.CHAT),
                new CooldownActions(true),
                new CooldownActions(false),
                new BackAction(),
                new MobSpawnAction(),
                new ExecuteAction(platform),
                new HealAction(),
                new BlockSetAction(),
                new SignSetAction(),
                new PowerSetAction(),
                new ShootAction(),
                new VariableActions(VariableActions.Type.SET, false),
                new VariableActions(VariableActions.Type.CLEAR, false),
                new VariableActions(VariableActions.Type.INCREASE, false),
                new VariableActions(VariableActions.Type.DECREASE, false),
                new VariableActions(VariableActions.Type.SET, true),
                new VariableActions(VariableActions.Type.CLEAR, true),
                new VariableActions(VariableActions.Type.INCREASE, true),
                new VariableActions(VariableActions.Type.DECREASE, true),
                new LocalVariableAction(),
                new TimerActions(true),
                new TimerActions(false),
                new CancelEventAction(),
                new SqlActions(SqlActions.Type.SELECT),
                new SqlActions(SqlActions.Type.UPDATE),
                new SqlActions(SqlActions.Type.INSERT),
                new SqlActions(SqlActions.Type.DELETE),
                new SqlActions(SqlActions.Type.SET),
                new RegexAction(),
                new RunActionAction(),
                new OpenMenuAction(),
                new WaitAction(),
                new LogAction(),
                new PlayerIdAction(),
                new FileAction(),
                new FlyAction(),
                new GlideAction(),
                new WalkSpeedAction(),
                new FlySpeedAction(),
                new JsConditionAction(),
                new ClearRadiusAction(),
                // Vault
                new MoneyTakeAction(),
                new MoneyGiveAction(),
                new GroupAddAction(),
                new GroupRemoveAction(),
                // WorldGuard
                new ClearRegionAction(),
                new BlockFillAction(),
                // WorldEdit
                new WeToolControlAction(),
                new WeSuperPickaxeAction()
        );
    }

    @Override
    public @NotNull Collection<Flag> getFlags(@NotNull ReActions.Platform platform) {
        return List.of(
                new WorldTimeFlag(),
                new ItemFlags(ItemFlags.Type.HAND),
                new ItemFlags(ItemFlags.Type.INVENTORY),
                new ItemFlags(ItemFlags.Type.WEAR),
                new ItemFlags(ItemFlags.Type.OFFHAND),
                new BlockFlag(),
                new MoneyFlag(),
                new ChanceFlag(),
                new PvpFlag(),
                new OnlineCountFlag(),
                new CooldownFlags(true),
                new CooldownFlags(false),
                new PlayerStateFlag(),
                new RegionFlags(RegionFlags.Type.REGION),
                new RegionFlags(RegionFlags.Type.REGION_PLAYERS),
                new RegionFlags(RegionFlags.Type.REGION_MEMBER),
                new RegionFlags(RegionFlags.Type.REGION_OWNER),
                new RegionFlags(RegionFlags.Type.REGION_STATE),
                new GamemodeFlag(),
                new FoodLevelFlag(),
                new ExperienceFlag(),
                new ExperienceLevelFlag(),
                new HealthFlag(),
                new BlockPoweredFlag(),
                new WorldFlag(),
                new BiomeFlag(),
                new LightLevelFlag(),
                new WalkBlockFlag(),
                new DirectionFlag(),
                new EitherFlag(ReActions.getActivities()),
                new ExecuteStopFlag(),
                new VariableFlags(VariableFlags.Type.EXIST, false),
                new VariableFlags(VariableFlags.Type.COMPARE, false),
                new VariableFlags(VariableFlags.Type.GREATER, false),
                new VariableFlags(VariableFlags.Type.LOWER, false),
                new VariableFlags(VariableFlags.Type.MATCH, false),
                new VariableFlags(VariableFlags.Type.EXIST, true),
                new VariableFlags(VariableFlags.Type.COMPARE, true),
                new VariableFlags(VariableFlags.Type.GREATER, true),
                new VariableFlags(VariableFlags.Type.LOWER, true),
                new VariableFlags(VariableFlags.Type.MATCH, true),
                new EqualsFlag(),
                new NumCompareFlags(true),
                new NumCompareFlags(false),
                new WeatherFlag(),
                new TimerActiveFlag(),
                new SqlFlags(true),
                new SqlFlags(false),
                new FlySpeedFlag(),
                new WalkSpeedFlag(),
                new CheckOnlineFlag(),
                new RegexFlag(),
                new HeldSlotFlag(),
                // Vault
                new GroupFlag(),
                new PermissionFlag(),
                // WorldGuard
                new RegionInRadiusFlag(),
                // WorldEdit
                new WeSelectionFlag(),
                new WeSuperPickaxeFlag(),
                new WeToolControlFlag()
        );
    }

    @Override
    public @NotNull Collection<Placeholder> getPlaceholders(@NotNull ReActions.Platform platform) {
        return List.of(
                new PlayerPlaceholders(),
                new PlayerInvPlaceholder(),
                new RandomPlaceholder(),
                new TimePlaceholders(),
                new CalcPlaceholder(),
                new ActivatorNamePlaceholder(),
                new VariablePlaceholders(),
                new LocalVariablePlaceholder(),
                // Vault
                new MoneyPlaceholder(),
                // PAPI
                new PapiPlaceholder()
        );
    }

    @Override
    public @NotNull Collection<Selector> getSelectors(@NotNull ReActions.Platform platform) {
        return List.of(
                new PlayerSelector(),
                new WorldSelector(),
                new LocSelector(),
                new GroupSelector(),
                new PermSelector(),
                new RegionSelector()
        );
    }

    public static @NotNull MineDown getMineDown(@NotNull String text) {
        return new MineDown(text).disable(MineDownParser.Option.SIMPLE_FORMATTING);
    }
}
