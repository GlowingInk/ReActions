package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.module.basics.StoragesManager;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-05-17.
 */
public class ActionIfElse implements Action {
    // TODO: Maybe use some custom evaluator instead of freaking JS engine?
    private static ScriptEngine engine = null;
    private static boolean checked = false;

    private static final String[] POSSIBLE_ENGINES = {"graal.js", "js", "javascript", "rhino", "nashorn"};

    private static boolean engineCheck() {
        if (checked) return engine != null;
        checked = true;
        ScriptEngine search = searchForEngine(new ScriptEngineManager());
        if (search == null) {
            RegisteredServiceProvider<ScriptEngineManager> registered = Bukkit.getServicesManager().getRegistration(ScriptEngineManager.class);
            if (registered == null || (search = searchForEngine(registered.getProvider())) == null) {
                ReActions.getLogger().warn("Couldn't find JS engine for IF_ELSE action.");
            }
        }
        return (engine = search) != null;
    }

    private static ScriptEngine searchForEngine(ScriptEngineManager scriptsManager) {
        for (String engineName : POSSIBLE_ENGINES) {
            ScriptEngine engine = scriptsManager.getEngineByName(engineName);
            if (engine != null) return engine;
        }
        return null;
    }

    private static boolean executeActivator(Player p, String condition, String paramStr) {
        Parameters param = Parameters.fromString(paramStr);
        if (!param.contains("run")) return false;
        param = Parameters.fromString(param.getString("run"));
        if (param.isEmpty() || !param.containsAny("activator", "exec")) return false;
        param.put("player", p == null ? "~null" : p.getName());
        Map<String, String> tempVars = new HashMap<>();
        tempVars.put("condition", condition);
        StoragesManager.triggerExec(p, param, tempVars);
        return true;
    }

    @Override
    public boolean proceed(@NotNull RaContext context, @NotNull String paramsStr) {
        if (!engineCheck()) return false;
        Parameters params = Parameters.fromString(paramsStr);
        Player player = context.getPlayer();
        if (params.contains("if") && params.containsAny("then", "else")) {
            final ScriptContext scriptContext = new SimpleScriptContext();
            scriptContext.setBindings(new SimpleBindings(), ScriptContext.ENGINE_SCOPE);

            String condition = params.getString("if");
            String then_ = params.getString("then");
            String else_ = params.getString("else");
            String suffix = params.getString("suffix");

            try {
                boolean result = (boolean) engine.eval(condition, scriptContext);
                if (!executeActivator(player, condition, (result) ? then_ : else_)
                        && !executeActions(context, (result) ? then_ : else_))
                    context.setVariable("ifelseresult" + suffix, (result) ? then_ : else_);
            } catch (ScriptException e) {
                context.setVariable("ifelsedebug", e.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "IF_ELSE";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    private boolean executeActions(RaContext context, String paramStr) {
        List<StoredAction> actions = new ArrayList<>();
        Parameters params = Parameters.fromString(paramStr);
        if (!params.contains("run")) return false;
        params = Parameters.fromString(params.getString("run"));
        if (params.isEmpty() || !params.contains("actions")) return false;
        params = Parameters.fromString(params.getString("actions"));

        if (!params.contains("action1")) return false;
        for (String actionKey : params.keySet()) {
            if (!((actionKey.toLowerCase(Locale.ROOT)).startsWith("action"))) continue;
            if (params.isEmpty() || !params.toString().contains("=")) continue;
            String actionStr = params.getString(actionKey);

            String name = actionStr.substring(0, actionStr.indexOf("="));
            String param = actionStr.substring(actionStr.indexOf("=") + 1);
            // TODO
            Action action = ReActions.getActivities().getAction(name);
            if (action == null) continue;
            actions.add(new StoredAction(action, param));
        }
        if (!actions.isEmpty())
            actions.forEach(action -> action.getAction().proceed(context, action.getParameters()));
        return true;
    }

	/*
	private enum ConditionType {
		EQUAL("="), MORE(">"), MORE_OR_EQUAL(">="), LESS("<"), LESS_OR_EQUAL("<="),
		BOOLEAN(false, "check"), S_EQUALS(false, "equals"), IGNORE_CASE(false, "ignorecase"), REGEX(false, "regular");

		@Getter private final boolean numeric;
		private final String alias;
		private static final Map<String, ConditionType> BY_NAME;
		static {
			Map<String, ConditionType> byName = new HashMap<>();
			for(ConditionType cnd : ConditionType.values()) {
				byName.put(cnd.name(), cnd);
				byName.put(cnd.alias.toUpperCase(Locale.ROOT), cnd);
			}
			BY_NAME = Collections.unmodifiableMap(byName);
		}
		ConditionType(boolean num, String alias) {
			this.alias = alias;
			this.numeric = num;
		}
		ConditionType(String alias) {
			this.alias = alias;
			this.numeric = true;
		}

		public static ConditionType getByName(String name) {
			return BY_NAME.get(name);
		}
	}
	*/
}
