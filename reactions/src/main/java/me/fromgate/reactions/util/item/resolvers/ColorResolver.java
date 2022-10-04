package me.fromgate.reactions.util.item.resolvers;

import me.fromgate.reactions.util.Utils;
import org.bukkit.Color;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ColorResolver implements MetaResolver {
    @Override
    public @NotNull String getName() {
        return "color";
    }

    @Override
    public @NotNull MetaResolver.Instance fromString(@NotNull String value) {
        Color color = Utils.getColor(value);
        return color != null
                ? new ColorInst(color)
                : ColorInst.EMPTY;
    }

    @Override
    public @Nullable MetaResolver.Instance fromItem(@NotNull ItemMeta meta) {
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            return new ColorInst(leatherMeta.getColor());
        } else if (meta instanceof PotionMeta potionMeta) {
            return new ColorInst(potionMeta.getColor());
        } else if (meta instanceof MapMeta mapMeta) {
            return new ColorInst(mapMeta.getColor());
        }
        return null;
    }

    private record ColorInst(@Nullable Color color) implements MetaResolver.Instance {
        private static final ColorInst EMPTY = new ColorInst(null);

        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof LeatherArmorMeta leatherMeta) {
                leatherMeta.setColor(color);
            } else if (meta instanceof PotionMeta potionMeta) {
                potionMeta.setColor(color);
            } else if (meta instanceof MapMeta mapMeta) {
                mapMeta.setColor(color);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (meta instanceof LeatherArmorMeta leatherMeta) {
                return Objects.equals(leatherMeta.getColor(), color);
            } else if (meta instanceof PotionMeta potionMeta) {
                return Objects.equals(potionMeta.getColor(), color);
            } else if (meta instanceof MapMeta mapMeta) {
                return Objects.equals(mapMeta.getColor(), color);
            }
            return color == null;
        }

        @Override
        public @NotNull String getName() {
            return "color";
        }

        @Override
        public @NotNull String asString() {
            return color != null
                    ? color.getRed() + "," + color.getGreen() + "," + color.getBlue()
                    : "";
        }
    }
}
