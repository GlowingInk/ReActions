package me.fromgate.reactions.actions;

import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Param;

public class ActionChange extends Action {
	@Override
	public boolean execute(RaContext context, Param params) {
		// TODO: Error message
		return context.setChangeable(params.getParam("key", params.getParam("id")).toLowerCase(),
										params.getParam("value", ""));
	}
}