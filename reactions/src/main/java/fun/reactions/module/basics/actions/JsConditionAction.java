package fun.reactions.module.basics.actions;

import fun.reactions.ReActions;
import fun.reactions.logic.activity.actions.Action;
import fun.reactions.logic.activity.actions.StoredAction;
import fun.reactions.logic.environment.Environment;
import fun.reactions.logic.environment.Variable;
import fun.reactions.logic.environment.Variables;
import fun.reactions.module.basics.ContextManager;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MaxDikiy on 2017-05-17.
 */
@Aliased.Names("IF_ELSE")
@Deprecated
public class JsConditionAction implements Action {
    private static ScriptEngine engine = null;
    private static boolean checked = false;

    private static final String[] POSSIBLE_ENGINES = {"graal.js", "rhino", "nashorn", "js", "javascript"};

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
        param = param.getParameters("run");
        if (!param.containsAny("activator", "exec")) return false;
        param = param.with("player", p == null ? "~null" : p.getName());
        Map<String, Variable> vars = new HashMap<>();
        vars.put("condition", Variable.simple(condition));
        ContextManager.triggerExec(p, param, new Variables(vars));
        return true;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        if (!engineCheck()) return false;
        Parameters params = Parameters.fromString(paramsStr);
        Player player = env.getPlayer();
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
                        && !executeActions(env, (result) ? then_ : else_))
                    env.getVariables().set("ifelseresult" + suffix, (result) ? then_ : else_);
            } catch (Exception e) {
                env.getVariables().set("ifelsedebug", e.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "JS_CONDITION";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    private boolean executeActions(Environment env, String paramStr) {
        Parameters params = Parameters.fromString(paramStr);
        if (!params.contains("run")) return false;
        params = params.getParameters("run");
        if (params.isEmpty() || !params.contains("actions")) return false;
        params = params.getParameters("actions");

        List<StoredAction> toExecute = new ArrayList<>();
        params.keyedListIterate("action", (actionKey, actions) -> {
            if (actions.isEmpty()) return;
            String actionStr = actions.getString(actionKey);

            int index = actionStr.indexOf('=');
            if (index == -1) return;
            String name = actionStr.substring(0, index);
            String param = actionStr.substring(index + 1);
            Action action = ReActions.getActivities().getAction(name);
            if (action == null) return;
            toExecute.add(new StoredAction(action, param));
        });
        if (!toExecute.isEmpty()) {
            toExecute.forEach(action -> action.getActivity().proceed(env, action.getParameters()));
        }
        return true;
    }
}
