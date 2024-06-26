package fun.reactions.module.basic.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basic.ContextManager;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.script.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author MaxDikiy
 * @since 17/05/2017
 */
@Aliased.Names({"IF_ELSE", "JS_CONDITION"})
@Deprecated
public class JsConditionalAction implements Action {
    private static final List<String> POSSIBLE_ENGINES = List.of("graal.js", "js", "javascript", "rhino", "nashorn");

    private ScriptEngine engine = null;
    private boolean checked = false;

    @Override
    public @NotNull String getName() {
        return "JS_CONDITIONAL";
    }

    private boolean engineCheck(@NotNull Environment env) {
        if (checked) return engine != null;
        ScriptEngine search = searchForEngine(new ScriptEngineManager());
        if (search == null) {
            var registered = env.getPlatform().getServer().getServicesManager().getRegistration(ScriptEngineManager.class);
            if (registered != null) {
                search = searchForEngine(registered.getProvider());
            }
        }
        engine = search;
        checked = true;
        return engine != null;
    }

    private static ScriptEngine searchForEngine(ScriptEngineManager scriptsManager) {
        for (String engineName : POSSIBLE_ENGINES) {
            ScriptEngine engine = scriptsManager.getEngineByName(engineName);
            if (engine != null) return engine;
        }
        return null;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        if (!engineCheck(env)) {
            env.warn(
                    "Couldn't find JS engine for JS_CONDITIONAL action. " +
                    "The usage of JS_CONDITIONAL action is discouraged - when used " +
                    "carelessly, can lead to server machine being hacked."
            );
            return false;
        }
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

    private static boolean executeActivator(Player p, String condition, String paramStr) {
        Parameters param = Parameters.fromString(paramStr);
        if (!param.contains("run")) return false;
        param = param.getParameters("run");
        if (!param.containsAny("activator", "exec")) return false;
        param = param.with("player", p == null ? "~null" : p.getName());
        Map<String, Variable> vars = new HashMap<>();
        vars.put("condition", Variable.simple(condition));
        ContextManager.triggerFunction(p, param, new Variables(vars));
        return true;
    }

    private boolean executeActions(Environment env, String paramStr) {
        Parameters params = Parameters.fromString(paramStr);
        if (!params.contains("run")) return false;
        params = params.getParameters("run");
        if (params.isEmpty() || !params.contains("actions")) return false;
        params = params.getParameters("actions");

        List<Stored> toExecute = new ArrayList<>();
        params.keyedListIterate("action", (actionKey, actions) -> {
            if (actions.isEmpty()) return;
            String actionStr = actions.getString(actionKey);

            int index = actionStr.indexOf('=');
            if (index == -1) return;
            String name = actionStr.substring(0, index);
            String param = actionStr.substring(index + 1);
            Action action = env.getPlatform().getActivities().getAction(name);
            if (action == null) return;
            toExecute.add(new Stored(action, param));
        });
        if (!toExecute.isEmpty()) {
            toExecute.forEach(action -> action.getActivity().proceed(env, action.getContent()));
        }
        return true;
    }
}
