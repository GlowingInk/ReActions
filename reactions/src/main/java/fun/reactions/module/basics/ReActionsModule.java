package fun.reactions.module.basics;

import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.MineDownParser;
import fun.reactions.ReActions;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.module.Module;
import fun.reactions.module.basics.actions.*;
import fun.reactions.module.basics.activators.*;
import fun.reactions.module.basics.external.RaProtocolLib;
import fun.reactions.module.basics.flags.*;
import fun.reactions.module.basics.placeholders.ActivatorNamePlaceholder;
import fun.reactions.module.basics.placeholders.CalcPlaceholder;
import fun.reactions.module.basics.placeholders.LocalVarPlaceholder;
import fun.reactions.module.basics.placeholders.PersistentVarPlaceholders;
import fun.reactions.module.basics.placeholders.PlayerInvPlaceholder;
import fun.reactions.module.basics.placeholders.PlayerPlaceholders;
import fun.reactions.module.basics.placeholders.RandomPlaceholder;
import fun.reactions.module.basics.placeholders.TimePlaceholders;
import fun.reactions.module.basics.placeholders.TimestampPlaceholder;
import fun.reactions.module.basics.selectors.LocSelector;
import fun.reactions.module.basics.selectors.PermSelector;
import fun.reactions.module.basics.selectors.PlayerSelector;
import fun.reactions.module.basics.selectors.WorldSelector;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.selectors.Selector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static fun.reactions.model.activators.type.ActivatorTypesRegistry.typeOf;

public class ReActionsModule implements Module {
    @Override
    public void preRegister(@NotNull ReActions.Platform platform) {
        RaProtocolLib.init();
    }

    @Override
    public @NotNull String getName() {
        return "ReActions";
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return List.of("fromgate", "MaxDikiy", "imDaniX");
    }

    @Override
    public @NotNull Collection<ActivatorType> getActivatorTypes() {
        return List.of(
                typeOf(FunctionActivator.class, "FUNCTION", FunctionActivator::create, FunctionActivator::create),
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
                typeOf(WeatherChangeActivator.class, "WEATHER_CHANGE", WeatherChangeActivator::create, WeatherChangeActivator::load)
        );
    }

    @Override
    public @NotNull Collection<Action> getActions() {
        RunFunctionAction functAction = new RunFunctionAction();
        return List.of(
                functAction,
                new ExecuteAction(functAction),
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
                new HealAction(),
                new BlockSetAction(),
                new SignSetAction(),
                new PowerSetAction(),
                new ShootAction(),
                new PersistentVarActions(PersistentVarActions.Type.SET, false),
                new PersistentVarActions(PersistentVarActions.Type.CLEAR, false),
                new PersistentVarActions(PersistentVarActions.Type.INCREASE, false),
                new PersistentVarActions(PersistentVarActions.Type.DECREASE, false),
                new PersistentVarActions(PersistentVarActions.Type.SET, true),
                new PersistentVarActions(PersistentVarActions.Type.CLEAR, true),
                new PersistentVarActions(PersistentVarActions.Type.INCREASE, true),
                new PersistentVarActions(PersistentVarActions.Type.DECREASE, true),
                new LocalVarAction(),
                new LocalVarBulkAction(),
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
                new ClearRadiusAction()
        );
    }

    @Override
    public @NotNull Collection<Flag> getFlags() {
        return List.of(
                new PermissionFlag(),
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
                new PersistentVarFlags(PersistentVarFlags.Type.EXIST, false),
                new PersistentVarFlags(PersistentVarFlags.Type.COMPARE, false),
                new PersistentVarFlags(PersistentVarFlags.Type.GREATER, false),
                new PersistentVarFlags(PersistentVarFlags.Type.LOWER, false),
                new PersistentVarFlags(PersistentVarFlags.Type.MATCH, false),
                new PersistentVarFlags(PersistentVarFlags.Type.EXIST, true),
                new PersistentVarFlags(PersistentVarFlags.Type.COMPARE, true),
                new PersistentVarFlags(PersistentVarFlags.Type.GREATER, true),
                new PersistentVarFlags(PersistentVarFlags.Type.LOWER, true),
                new PersistentVarFlags(PersistentVarFlags.Type.MATCH, true),
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
                new HeldSlotFlag()
        );
    }

    @Override
    public @NotNull Collection<Placeholder> getPlaceholders() {
        return List.of(
                new PlayerPlaceholders(),
                new PlayerInvPlaceholder(),
                new RandomPlaceholder(),
                new TimePlaceholders(),
                new CalcPlaceholder(),
                new ActivatorNamePlaceholder(),
                new PersistentVarPlaceholders(),
                new LocalVarPlaceholder(),
                new TimestampPlaceholder()
        );
    }

    @Override
    public @NotNull Collection<Selector> getSelectors() {
        return List.of(
                new PlayerSelector(),
                new WorldSelector(),
                new LocSelector(),
                new PermSelector()
        );
    }

    public static @NotNull MineDown getMineDown(@NotNull String text) {
        return new MineDown(text).disable(MineDownParser.Option.SIMPLE_FORMATTING);
    }
}
