package me.fromgate.reactions.selectors;

import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SelectorsManager {
    private final Map<String, Selector> selectorByName;
    private final List<Selector> selectors;

    public SelectorsManager() {
        selectorByName = new HashMap<>();
        selectors = new ArrayList<>();
    }

    public void registerSelector(@NotNull Selector selector) {
        if (selectorByName.containsKey(selector.getName().toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException("Selector '" + selector.getName().toLowerCase(Locale.ROOT) + "' is already registered!");
        }
        selectors.add(selector);
        selectorByName.put(selector.getName().toLowerCase(Locale.ROOT), selector);
        for (String alias : Utils.getAliases(selector)) {
            selectorByName.putIfAbsent(alias.toLowerCase(Locale.ROOT), selector);
        }
    }

    public Set<Player> getPlayerList(Parameters param) {
        Set<Player> players = new HashSet<>();
        // TODO Optimize
        for (Selector selector : selectors) {
            String selectorParam = param.getString(selector.getName());
            if (selector.getName().equalsIgnoreCase("loc") && param.contains("radius"))
                selectorParam = "loc:" + selectorParam + " " + "radius:" + param.getString("radius", "1");
            players.addAll(selector.getPlayers(selectorParam));
        }
        return players;
    }

    public Set<String> getAllKeys() {
        return selectorByName.keySet();
    }
}