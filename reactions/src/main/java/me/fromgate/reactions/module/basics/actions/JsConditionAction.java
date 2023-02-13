package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.logic.context.Variables;
import me.fromgate.reactions.module.basics.DetailsManager;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
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
import java.util.Locale;
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
        DetailsManager.triggerExec(p, param, new Variables(vars));
        return true;
    }

    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
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
                    context.getVariables().set("ifelseresult" + suffix, (result) ? then_ : else_);
            } catch (Exception e) {
                context.getVariables().set("ifelsedebug", e.getMessage());
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

    private boolean executeActions(Environment context, String paramStr) {
        List<StoredAction> actions = new ArrayList<>();
        Parameters params = Parameters.fromString(paramStr);
        if (!params.contains("run")) return false;
        params = Parameters.fromString(params.getString("run"));
        if (params.isEmpty() || !params.contains("actions")) return false;
        params = Parameters.fromString(params.getString("actions"));

        if (!params.contains("action1")) return false;
        for (String actionKey : params.keys()) {
            if (!((actionKey.toLowerCase(Locale.ROOT)).startsWith("action"))) continue;
            if (params.isEmpty() || !params.origin().contains("=")) continue;
            String actionStr = params.getString(actionKey);

            String name = actionStr.substring(0, actionStr.indexOf("="));
            String param = actionStr.substring(actionStr.indexOf("=") + 1);
            Action action = ReActions.getActivities().getAction(name);
            if (action == null) continue;
            actions.add(new StoredAction(action, param));
        }
        if (!actions.isEmpty())
            actions.forEach(action -> action.getActivity().proceed(context, action.getParameters()));
        return true;
    }
}
