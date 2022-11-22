package me.fromgate.reactions.util.item.aspects;

import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

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
                    ? new Base(potionMeta.getBasePotionData())
                    : potionMeta.hasCustomEffects() ? new Effects(potionMeta.getCustomEffects()) : null;
        }
        return null;
    }

    private static final class Base implements MetaAspect.Instance {
        private final PotionData potionData;
        private final String potionDataStr;

        public Base(@NotNull PotionData potionData) {
            this.potionData = potionData;
            StringBuilder builder = new StringBuilder();
            builder.append("base-type:").append(potionData.getType().name());
            if (potionData.isExtended()) builder.append(" extended:true");
            if (potionData.isUpgraded()) builder.append(" upgraded:true");
            this.potionDataStr = builder.toString();
        }

        public Base(@NotNull String potionDataStr) {
            this.potionDataStr = potionDataStr;
            Parameters params = Parameters.fromString(potionDataStr);
            PotionType type = params.getEnum("base-type", PotionType.UNCRAFTABLE);
            boolean extended = type.isExtendable() && params.getBoolean("extended", false);
            boolean upgraded = type.isUpgradeable() && params.getBoolean("upgraded", false);
            this.potionData = new PotionData(type, extended, upgraded);
        }

        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.setBasePotionData(potionData);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (meta instanceof PotionMeta potionMeta) {
                return potionMeta.getBasePotionData().equals(potionData);
            }
            return false;
        }

        @Override
        public @NotNull String getName() {
            return "potion-base";
        }

        @Override
        public @NotNull String asString() {
            return potionDataStr;
        }
    }

    private static final class Effects implements MetaAspect.Instance { // TODO Particles, etc
        public static final Effects EMPTY = new Effects(List.of());

        private final List<PotionEffect> effects;
        private final String effectsStr;

        public Effects(@NotNull String value) {
            this.effectsStr = value;
            String[] split = value.split(";");
            this.effects = new ArrayList<>(split.length);
            for (String effectStr : split) {
                effectStr = effectStr.trim();
                String[] effectData = effectStr.split(":");
                if (effectData.length < 3) continue;
                PotionEffectType type = ItemUtils.searchByKey(effectData[0], PotionEffectType::getByKey);
                if (type == null) {
                    type = PotionEffectType.getByName(effectData[0].toUpperCase(Locale.ROOT));
                    if (type == null) continue;
                }
                int level = Math.max(NumberUtils.asInt(effectData[1], 0), 0);
                long duration = TimeUtils.parseTime(effectData[2]) / 50L;
                this.effects.add(new PotionEffect(type, NumberUtils.safeLongToInt(duration), level));
            }
        }

        public Effects(@NotNull List<PotionEffect> effects) {
            this.effects = effects;
            if (effects.isEmpty()) {
                this.effectsStr = "";
            } else {
                StringBuilder builder = new StringBuilder();
                for (PotionEffect effect : effects) {
                    builder.append(effect.getType().getKey().getKey())
                            .append(':').append(effect.getAmplifier())
                            .append(':').append(effect.getDuration()).append("t; ");
                }
                this.effectsStr = Utils.cutBuilder(builder, 2);
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
                return new HashSet<>(potionMeta.getCustomEffects()).containsAll(effects);
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
}
