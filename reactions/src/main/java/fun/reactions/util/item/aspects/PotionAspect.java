package fun.reactions.util.item.aspects;

import fun.reactions.util.Utils;
import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.Parameters;
import fun.reactions.util.time.TimeUtils;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PotionAspect implements MetaAspect {
    private final boolean base;

    public PotionAspect(boolean base) {
        this.base = base;
    }

    @Override
    public @NotNull String getName() {
        return base
                ? "potion-base"
                : "potion-effects";
    }

    @Override
    public @NotNull MetaAspect.Instance fromString(@NotNull String value) {
        return base
                ? new Base(value)
                : value.isEmpty() ? Effects.EMPTY : new Effects(value);
    }

    @Override
    public @Nullable MetaAspect.Instance fromItem(@NotNull ItemMeta meta) {
        if (meta instanceof PotionMeta potionMeta) {
            return base
                    ? potionMeta.hasBasePotionType() ? new Base(potionMeta.getBasePotionType()) : null
                    : potionMeta.hasCustomEffects() ? new Effects(potionMeta.getCustomEffects()) : null;
        }
        return null;
    }

    private static final class Effects implements MetaAspect.Instance { // TODO Particles, etc
        public static final Effects EMPTY = new Effects(List.of());

        private final Set<PotionEffect> effects;
        private final String effectsStr;

        public Effects(@NotNull String value) {
            this.effectsStr = value;
            String[] split = value.split(";");
            this.effects = new HashSet<>(split.length);
            for (String effectStr : split) {
                effectStr = effectStr.trim();
                String[] effectData = effectStr.split(":");
                if (effectData.length < 3) continue;
                PotionEffectType type = Utils.searchRegistry(effectData[0], Registry.POTION_EFFECT_TYPE);
                if (type == null) continue;
                int level = Math.max(NumberUtils.asInteger(effectData[1], 0), 0);
                long duration = TimeUtils.parseTime(effectData[2]) / 50L;
                this.effects.add(new PotionEffect(type, NumberUtils.compactLong(duration), level));
            }
        }

        public Effects(@NotNull List<PotionEffect> effects) {
            if (effects.isEmpty()) {
                this.effects = Set.of();
                this.effectsStr = "";
            } else {
                this.effects = new HashSet<>(effects);
                StringBuilder builder = new StringBuilder();
                for (PotionEffect effect : effects) {
                    builder.append(effect.getType().getKey().getKey())
                            .append(':').append(effect.getAmplifier())
                            .append(':').append(effect.getDuration()).append("t; ");
                }
                this.effectsStr = Utils.cutLast(builder, 2);
            }
        }

        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof PotionMeta potionMeta) {
                for (PotionEffect effect : effects) {
                    potionMeta.addCustomEffect(effect, true);
                }
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (meta instanceof PotionMeta potionMeta) {
                if (effects.isEmpty()) return !potionMeta.hasCustomEffects();
                List<PotionEffect> itemEffects = potionMeta.getCustomEffects();
                return effects.size() == itemEffects.size() && effects.containsAll(itemEffects);
            }
            return false;
        }

        @Override
        public @NotNull String getName() {
            return "potion-effects";
        }

        @Override
        public @NotNull String asString() {
            return effectsStr;
        }
    }

    private static final class Base implements MetaAspect.Instance {
        private final PotionType potionType;
        private final String potionTypeStr;

        public Base(@NotNull PotionType potionType) {
            this.potionType = potionType;
            this.potionTypeStr = potionType.name();
        }

        public Base(@NotNull String potionTypeStr) {
            this.potionTypeStr = potionTypeStr;
            if (potionTypeStr.isEmpty()) {
                this.potionType = null;
            } else if (potionTypeStr.startsWith("base-type:")) {
                this.potionType = Parameters.fromString(potionTypeStr).getEnum("base-type", PotionType.class);
            } else {
                this.potionType = Utils.getEnum(PotionType.class, potionTypeStr);
            }
        }

        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.setBasePotionType(potionType);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (meta instanceof PotionMeta potionMeta) {
                return Objects.equals(potionMeta.getBasePotionType(), potionType);
            }
            return false;
        }

        @Override
        public @NotNull String getName() {
            return "potion-base";
        }

        @Override
        public @NotNull String asString() {
            return potionTypeStr;
        }
    }
}
