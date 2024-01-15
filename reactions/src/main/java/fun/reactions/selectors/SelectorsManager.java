package fun.reactions.selectors;

import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SelectorsManager {
    private final Map<String, Selector> selectorByName;
    private final List<Selector> selectors;

    public SelectorsManager() {
        selectorByName = new HashMap<>();
        selectors = new ArrayList<>();
    }

    public void registerSelector(@NotNull Selector selector) {
        if (selectorByName.containsKey(selector.getName().toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException("Selector '" + selector.getName().toLowerCase(Locale.ROOT) + "' is already registered");
        }
        selectors.add(selector);
        selectorByName.put(selector.getName().toLowerCase(Locale.ROOT), selector);
        for (String alias : Aliased.getAliasesOf(selector)) {
            selectorByName.putIfAbsent(alias.toLowerCase(Locale.ROOT), selector);
        }
    }

    public Set<Player> getPlayerList(Parameters param) { // TODO: Honestly, I'm not even sure what's going on here. Rework from scratch
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

    public Set<String> getAllKeys() {
        return selectorByName.keySet();
    }
}
