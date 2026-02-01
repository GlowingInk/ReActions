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
        switch (meta) {
            case LeatherArmorMeta leatherMeta -> color = leatherMeta.getColor();
            case PotionMeta potionMeta -> color = potionMeta.getColor();
            case MapMeta mapMeta -> color = mapMeta.getColor();
            default -> {
            }
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
            switch (meta) {
                case LeatherArmorMeta leatherMeta -> leatherMeta.setColor(color);
                case PotionMeta potionMeta -> potionMeta.setColor(color);
                case MapMeta mapMeta -> mapMeta.setColor(color);
                default -> {
                }
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            return switch (meta) {
                case LeatherArmorMeta leatherMeta -> Objects.equals(leatherMeta.getColor(), color);
                case PotionMeta potionMeta -> Objects.equals(potionMeta.getColor(), color);
                case MapMeta mapMeta -> Objects.equals(mapMeta.getColor(), color);
                default -> color == null;
            };
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
