package fun.reactions.util.item.aspects;

import fun.reactions.util.RegistryUtils;
import fun.reactions.util.Utils;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static fun.reactions.util.RegistryUtils.getRegistry;

public class BannerAspect implements MetaAspect {
    @Override
    public @NotNull String getName() {
        return "banner-pattern";
    }

    @Override
    public @NotNull MetaAspect.Instance fromString(@NotNull String value) {
        if (value.isEmpty()) {
            return PatternInst.EMPTY;
        } else {
            String[] split = value.split(";");
            List<Pattern> patterns = new ArrayList<>();
            for (String patternStr : split) {
                patternStr = patternStr.trim();
                int index = patternStr.indexOf(':');
                if (index == -1) continue;
                PatternType type = RegistryUtils.searchRegistry(patternStr.substring(0, index), getRegistry(RegistryKey.BANNER_PATTERN));
                if (type == null) continue;
                DyeColor color = Utils.getEnum(DyeColor.class, patternStr.substring(index + 1));
                if (color == null) continue;
                patterns.add(new Pattern(color, type));
            }
            return new PatternInst(value, patterns);
        }
    }

    @Override
    public @Nullable MetaAspect.Instance fromItem(@NotNull ItemMeta meta) {
        if (meta instanceof BannerMeta bannerMeta) {
            if (bannerMeta.numberOfPatterns() == 0) {
                return PatternInst.EMPTY;
            } else {
                StringBuilder builder = new StringBuilder();
                List<Pattern> patterns = bannerMeta.getPatterns();
                for (Pattern pattern : patterns) {
                    builder.append(pattern.getPattern()).append(':').append(pattern.getColor()).append(",");
                }
                return new PatternInst(Utils.cutLast(builder, 1), patterns);
            }
        }
        return null;
    }

    private record PatternInst(@NotNull String value, @NotNull List<Pattern> patterns) implements Instance {
        public static final PatternInst EMPTY = new PatternInst("", List.of());

        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof BannerMeta bannerMeta) {
                patterns.forEach(bannerMeta::addPattern);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (meta instanceof BannerMeta bannerMeta) {
                if (patterns.isEmpty()) return bannerMeta.numberOfPatterns() == 0;
                if (patterns.size() > bannerMeta.numberOfPatterns()) return false;
                for (int i = 0; i < patterns.size(); i++) {
                    if (!patterns.get(i).equals(bannerMeta.getPattern(i))) return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public @NotNull String getName() {
            return "banner-pattern";
        }

        @Override
        public @NotNull String asString() {
            return value;
        }
    }
}
