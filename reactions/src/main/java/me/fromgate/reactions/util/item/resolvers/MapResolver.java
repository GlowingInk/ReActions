package me.fromgate.reactions.util.item.resolvers;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MapResolver implements MetaResolver { // TODO: MapView handling
    private final boolean scale;

    public MapResolver(boolean scale) {
        this.scale = scale;
    }

    @Override
    public @NotNull String getName() {
        return scale
                ? "map-scale"
                : "map-name";
    }

    @Override
    public @NotNull MetaResolver.Instance fromString(@NotNull String value) {
        if (scale) {
            return value.equalsIgnoreCase("true")
                    ? ScaleInst.TRUE
                    : ScaleInst.FALSE;
        } else {
            return new NameInst(value.isEmpty() ? null : value);
        }
    }

    @Override
    public @Nullable MetaResolver.Instance fromItem(@NotNull ItemMeta meta) {
        if (meta instanceof MapMeta mapMeta) {
            if (scale) {
                return mapMeta.isScaling()
                        ? ScaleInst.TRUE
                        : ScaleInst.FALSE;
            } else if (mapMeta.getLocationName() != null) {
                return new NameInst(mapMeta.getLocationName());
            }
        }
        return null;
    }

    private record ScaleInst(boolean scale) implements Instance {
        public static final ScaleInst TRUE = new ScaleInst(true);
        public static final ScaleInst FALSE = new ScaleInst(false);

        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof MapMeta mapMeta) {
                mapMeta.setScaling(scale);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            return meta instanceof MapMeta mapMeta && mapMeta.isScaling() == scale;
        }

        @Override
        public @NotNull String getName() {
            return "map-scale";
        }

        @Override
        public @NotNull String asString() {
            return Boolean.toString(scale);
        }
    }

    private record NameInst(@Nullable String name) implements Instance {
        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof MapMeta mapMeta) {
                mapMeta.setLocationName(name);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            return meta instanceof MapMeta mapMeta && Objects.equals(mapMeta.getLocationName(), name);
        }

        @Override
        public @NotNull String getName() {
            return "map-name";
        }

        @Override
        public @NotNull String asString() {
            return name == null ? "" : name;
        }
    }
}
