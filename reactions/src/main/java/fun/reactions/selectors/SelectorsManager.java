package fun.reactions.selectors;

import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class SelectorsManager {
    private final Map<String, Selector> selectorByName;
    private final List<Selector> selectors;

    public SelectorsManager() {
        selectorByName = new HashMap<>();
        selectors = new ArrayList<>();
    }

    public void registerSelector(@NotNull Selector selector) {
        String selectorName = selector.getName().toLowerCase(Locale.ROOT);
        if (selectorByName.containsKey(selectorName)) {
            throw new IllegalStateException("Selector '" + selectorName + "' is already registered");
        }
        selectors.add(selector);
        selectorByName.put(selectorName, selector);
        for (String alias : Aliased.getAliasesOf(selector)) {
            selectorByName.putIfAbsent(alias.toLowerCase(Locale.ROOT), selector);
        }
    }

    @Deprecated
    public Set<Player> getPlayers(Parameters param) {
        Set<Player> players = new HashSet<>();
        for (Selector selector : selectors) {
            String selectorParam = param.getString(selector.getName());
            if (selectorParam.isEmpty()) continue;
            if (selector.getName().equalsIgnoreCase("loc") && param.contains("radius")) {
                selectorParam = "loc:" + selectorParam + " " + "radius:" + param.getString("radius", "1");
            }
            players.addAll(selector.getPlayers(selectorParam));
        }
        return players;
    }

    public void iteratePlayers(@NotNull Parameters params, @NotNull Consumer<@Nullable Player> run) {
        for (Selector selector : selectors) {
            String selectorParam = params.getString(selector.getName(), null);
            if (selectorParam != null) {
                selector.iteratePlayers(selectorParam, run);
            }
        }
    }

    public Set<String> getAllKeys() {
        return selectorByName.keySet();
    }
}
