package fun.reactions.util.item.aspects;

import fun.reactions.util.Utils;
import org.bukkit.Color;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ColorAspect implements MetaAspect {
    @Override
    public @NotNull String getName() {
        return "color";
    }

    @Override
    public @NotNull MetaAspect.Instance fromString(@NotNull String value) {
        Color color = Utils.getColor(value);
        return color != null
                ? new ColorInst(color)
                : ColorInst.EMPTY;
    }

    @Override
    public @Nullable MetaAspect.Instance fromItem(@NotNull ItemMeta meta) {
        Color color = null;
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            color = leatherMeta.getColor();
        } else if (meta instanceof PotionMeta potionMeta) {
            color = potionMeta.getColor();
        } else if (meta instanceof MapMeta mapMeta) {
            color = mapMeta.getColor();
        }
        if (color != null) {
            return new ColorInst(color);
        }
        return null;
    }

    private record ColorInst(@Nullable Color color) implements MetaAspect.Instance {
        public static final ColorInst EMPTY = new ColorInst(null);

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
