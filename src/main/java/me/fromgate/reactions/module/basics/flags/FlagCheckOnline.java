package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.activity.flags.OldFlag;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.data.RaContext;

public class FlagCheckOnline implements OldFlag {
    @Override
    public boolean checkFlag(RaContext context, String param) {
        return Utils.getPlayerExact(param) != null;
    }
}
