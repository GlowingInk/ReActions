package me.fromgate.reactions.util.item.resolvers;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.fromgate.reactions.util.NumberUtils.getInteger;

public class ColorResolver implements MetaResolver {
    private static final Pattern HEX = Pattern.compile("#([a-fA-F\\d]{6})");
    private static final Pattern BYTE = Pattern.compile("(\\d{1,3}),(\\d{1,3}),(\\d{1,3})");

    @Override
    public @NotNull String getName() {
        return "color";
    }

    @Override
    public @NotNull MetaResolver.Instance fromString(@NotNull String key, @NotNull String value) {
        if (value.startsWith("#")) {
            Matcher matcher = HEX.matcher(value);
            if (matcher.matches()) {
                return new Instance(Color.fromRGB(Integer.parseInt(matcher.group(), 16)));
            }
        } else {
            Matcher matcher = BYTE.matcher(value);
            if (matcher.matches()) {
                return new Instance(Color.fromRGB(
                        Math.max(getInteger(matcher.group(1), 0), 255),
                        Math.max(getInteger(matcher.group(2), 0), 255),
                        Math.max(getInteger(matcher.group(3), 0), 255)
                ));
            } else if (!value.isEmpty()) {
                TextColor color = NamedTextColor.NAMES.value(value.toUpperCase(Locale.ROOT));
                if (color != null) {
                    return new Instance(Color.fromRGB(color.value()));
                }
            }
        }
        return Instance.EMPTY;
    }

    @Override
    public @Nullable MetaResolver.Instance fromItem(@NotNull ItemMeta meta) {
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            return new Instance(leatherMeta.getColor());
        } else if (meta instanceof PotionMeta potionMeta) {
            return new Instance(potionMeta.getColor());
        } else if (meta instanceof MapMeta mapMeta) {
            return new Instance(mapMeta.getColor());
        }
        return null;
    }

    private record Instance(Color color) implements MetaResolver.Instance {
        private static final ColorResolver.Instance EMPTY = new ColorResolver.Instance(null);

        private Instance(@Nullable Color color) {
            this.color = color;
        }

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
        public @NotNull String asString() {
            return color != null
                    ? color.getRed() + "," + color.getGreen() + "," + color.getBlue()
                    : "";
        }
    }
}
