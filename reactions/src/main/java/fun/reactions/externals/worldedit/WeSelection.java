package fun.reactions.externals.worldedit;

import org.bukkit.Location;
import org.bukkit.World;

public record WeSelection(String selType, Location min, Location max, int area, World world, String region) {
    public boolean isValid() {
        return world != null && region != null && !region.isEmpty() && min != null && max != null && area != -1;
    }
}
