package fun.reactions.areas;

import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class AreasRegistry {
    private final Map<String, AreaProvider> areaByName;

    public AreasRegistry() {
        areaByName = new HashMap<>();
    }

    public void registerArea(@NotNull AreaProvider area) {
        String lowerArea = area.getName().toLowerCase(Locale.ROOT);
        if (areaByName.containsKey(lowerArea)) {
            throw new IllegalStateException("Area '" + lowerArea + "' is already registered");
        }
        areaByName.put(lowerArea, area);
        for (String alias : Aliased.getAliasesOf(area)) {
            areaByName.putIfAbsent(alias.toLowerCase(Locale.ROOT), area);
        }
    }

    public @Nullable AreaProvider getArea(@NotNull String name) {
        return areaByName.get(name.toLowerCase(Locale.ROOT));
    }

    public @NotNull @UnmodifiableView Collection<@NotNull String> getAreasNames() {
        return Collections.unmodifiableCollection(areaByName.keySet());
    }
}
