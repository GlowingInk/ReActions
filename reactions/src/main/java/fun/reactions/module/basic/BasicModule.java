package fun.reactions.module.basic;

import fun.reactions.ReActions;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.module.Module;
import fun.reactions.module.basic.actions.*;
import fun.reactions.module.basic.activators.*;
import fun.reactions.module.basic.aspects.*;
import fun.reactions.module.basic.flags.*;
import fun.reactions.module.basic.placeholders.*;
import fun.reactions.module.basic.selectors.LocSelector;
import fun.reactions.module.basic.selectors.PermSelector;
import fun.reactions.module.basic.selectors.PlayerSelector;
import fun.reactions.module.basic.selectors.WorldSelector;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.selectors.Selector;
import fun.reactions.util.item.MetaAspect;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static fun.reactions.model.activators.type.ActivatorType.of;

public class BasicModule implements
        Module.WithActivatorTypes, Module.WithActions, Module.WithFlags,
        Module.WithPlaceholders, Module.WithSelectors, Module.WithItemAspects {
    private LogHandler logHandler;
    private ReActions.Platform platform;

    @Override
    public void onRegister(@NotNull ReActions.Platform platform) {
        this.platform = platform;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable(@NotNull ReActions.Platform platform) {
        if (platform.getActivatorTypes().get(MessageActivator.class) instanceof MessageActivator.Type messageType) {
            logHandler = new LogHandler(messageType);
            platform.getServer().getLogger().addHandler(logHandler);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onDisable(@NotNull ReActions.Platform platform) {
        if (logHandler != null) platform.getServer().getLogger().removeHandler(logHandler);
    }

    @Override
    public @NotNull String getName() {
        return "ReActions";
    }

    @Override
    public boolean isImportant() {
        return true;
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return List.of("fromgate", "MaxDikiy", "imDaniX");
    }

    @Override
    public @NotNull Collection<ActivatorType> getActivatorTypes() {
        return Arrays.asList(
                of(FunctionActivator.class, "FUNCTION", FunctionActivator::create),
                of(ButtonActivator.class, "BUTTON", ButtonActivator::create, true),
                of(PlateActivator.class, "PLATE", PlateActivator::create, true),
                of(TeleportActivator.class, "TELEPORT", TeleportActivator::create),
                of(CommandActivator.class, "COMMAND", CommandActivator::create),
                new MessageActivator.Type(),
                of(PvpKillActivator.class, "PVP_KILL", PvpKillActivator::create),
                of(DeathActivator.class, "DEATH", DeathActivator::create),
                of(RespawnActivator.class, "RESPAWN", RespawnActivator::create),
                of(LeverActivator.class, "LEVER", LeverActivator::create, true),
                of(DoorActivator.class, "DOOR", DoorActivator::create, true),
                of(JoinActivator.class, "JOIN", JoinActivator::create),
                of(QuitActivator.class, "QUIT", QuitActivator::create),
                of(MobClickActivator.class, "MOB_CLICK", MobClickActivator::create),
                of(MobKillActivator.class, "MOB_KILL", MobKillActivator::create),
                of(MobDamageActivator.class, "MOB_DAMAGE", MobDamageActivator::create),
                of(ItemClickActivator.class, "ITEM_CLICK", ItemClickActivator::create),
                of(ConsumeActivator.class, "CONSUME", ConsumeActivator::create),
                new ItemHoldActivator.Type(platform),
                of(ItemHeldActivator.class, "ITEM_HELD", ItemHeldActivator::create),
                new ItemWearActivator.Type(platform),
                new SignActivator.Type(),
                of(BlockClickActivator.class, "BLOCK_CLICK", BlockClickActivator::create, true),
                of(InventoryClickActivator.class, "INVENTORY_CLICK", InventoryClickActivator::create),
                of(DropActivator.class, "DROP", DropActivator::create),
                of(PickupItemActivator.class, "PICKUP_ITEM", PickupItemActivator::create),
                of(FlightActivator.class, "FLIGHT", FlightActivator::create),
                of(EntityClickActivator.class, "ENTITY_CLICK", EntityClickActivator::create),
                of(BlockBreakActivator.class, "BLOCK_BREAK", BlockBreakActivator::create, true),
                of(SneakActivator.class, "SNEAK", SneakActivator::create),
                of(DamageActivator.class, "DAMAGE", DamageActivator::create),
                of(DamageByMobActivator.class, "DAMAGE_BY_MOB", DamageByMobActivator::create),
                of(DamageByBlockActivator.class, "DAMAGE_BY_BLOCK", DamageByBlockActivator::create),
                of(VariableActivator.class, "VARIABLE", VariableActivator::create),
                of(GameModeActivator.class, "GAMEMODE", GameModeActivator::create),
                of(CuboidActivator.class, "CUBOID", CuboidActivator::create),
                of(WeatherChangeActivator.class, "WEATHER_CHANGE", WeatherChangeActivator::create)
        );
    }

    @Override
    public @NotNull Collection<Action> getActions() {
        RunFunctionAction functAction = new RunFunctionAction();
        return Arrays.asList(
                functAction,
                new ExecuteAction(functAction),
                new TeleportAction(),
                new VelocityAction(),
                new VelocityJumpAction(),
                new SoundAction(),
                new PotionEffectAction(),
                new PotionRemoveAction(),
                new MessageAction(),
                new InkyMessageAction(),
                new MiniMessageAction(),
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
                new SqlActions(platform.getSql(), SqlActions.Type.SELECT),
                new SqlActions(platform.getSql(), SqlActions.Type.UPDATE),
                new SqlActions(platform.getSql(), SqlActions.Type.INSERT),
                new SqlActions(platform.getSql(), SqlActions.Type.DELETE),
                new SqlActions(platform.getSql(), SqlActions.Type.SET),
                new RegexAction(),
                new DynamicActionAction(),
                new OpenMenuAction(),
                new WaitAction(),
                new LogAction(),
                new PlayerIdAction(),
                new FileAction(),
                new FlyAction(),
                new GlideAction(),
                new WalkSpeedAction(),
                new FlySpeedAction(),
                new JsConditionalAction(),
                new ClearRadiusAction()
        );
    }

    @Override
    public @NotNull Collection<Flag> getFlags() {
        return Arrays.asList(
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
                new ExpLevelFlag(),
                new HealthFlag(),
                new BlockPoweredFlag(),
                new WorldFlag(),
                new BiomeFlag(),
                new LightLevelFlag(),
                new WalkBlockFlag(),
                new DirectionFlag(),
                new EitherFlag(),
                new ExecuteStopFlag(),
                new PersistentVarFlags(PersistentVarFlags.Type.EXIST, false),
                new PersistentVarFlags(PersistentVarFlags.Type.EXIST, true),
                new PersistentVarFlags(PersistentVarFlags.Type.COMPARE, false),
                new PersistentVarFlags(PersistentVarFlags.Type.COMPARE, true),
                new PersistentVarFlags(PersistentVarFlags.Type.GREATER, false),
                new PersistentVarFlags(PersistentVarFlags.Type.GREATER, true),
                new PersistentVarFlags(PersistentVarFlags.Type.LOWER, false),
                new PersistentVarFlags(PersistentVarFlags.Type.LOWER, true),
                new PersistentVarFlags(PersistentVarFlags.Type.MATCH, false),
                new PersistentVarFlags(PersistentVarFlags.Type.MATCH, true),
                new EqualsFlag(),
                new NumCompareFlags(NumCompareFlags.Type.GREATER),
                new NumCompareFlags(NumCompareFlags.Type.LOWER),
                new WeatherFlag(),
                new TimerActiveFlag(),
                new SqlFlags(platform.getSql(), SqlFlags.Type.CHECK),
                new SqlFlags(platform.getSql(), SqlFlags.Type.RESULT),
                new FlySpeedFlag(),
                new WalkSpeedFlag(),
                new CheckOnlineFlag(),
                new RegexFlag(),
                new HeldSlotFlag(),
                new DistanceFlag(),
                new DynamicFlagFlag()
        );
    }

    @Override
    public @NotNull Collection<Placeholder> getPlaceholders() {
        return Arrays.asList(
                new PlayerPlaceholders(),
                new PlayerInvPlaceholder(),
                new RandomPlaceholder(),
                new TimePlaceholders(),
                new CalcPlaceholder(),
                new ActivatorNamePlaceholder(),
                new PersistentVarPlaceholders(),
                new LocalVarPlaceholder(),
                new EnderChestPlaceholder(),
                new FunctionPlaceholder(),
                new EscapePlaceholder()
        );
    }

    @Override
    public @NotNull Collection<Selector> getSelectors() {
        return Arrays.asList(
                new PlayerSelector(),
                new WorldSelector(),
                new LocSelector(),
                new PermSelector()
        );
    }

    @Override
    public @NotNull Collection<MetaAspect> getItemAspects() {
        return Arrays.asList(
                new NameAspect(true),
                new NameAspect(false),
                new LoreAspect(true),
                new LoreAspect(false),
                new DurabilityAspect(),
                new EnchantmentsAspect(),
                new ModelAspect(),
                new ColorAspect(),
                new BannerAspect(),
                new BookAspect(BookAspect.Type.PAGES),
                new BookAspect(BookAspect.Type.TITLE),
                new BookAspect(BookAspect.Type.AUTHOR),
                new FireworkAspect(true),
                new FireworkAspect(false),
                new HeadAspect(),
                new MapAspect(true),
                new MapAspect(false),
                new PotionAspect(true),
                new PotionAspect(false)
        );
    }
}
