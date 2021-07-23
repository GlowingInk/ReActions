package me.fromgate.reactions.module;

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
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.selectors.Selector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

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
        // TODO
        return Module.super.getActivatorTypes();
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
        return Module.super.getFlags();
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
