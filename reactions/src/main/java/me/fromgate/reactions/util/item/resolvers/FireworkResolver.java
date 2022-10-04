package me.fromgate.reactions.util.item.resolvers;

import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FireworkResolver implements MetaResolver {
    private final boolean effects;

    public FireworkResolver(boolean effects) {
        this.effects = effects;
    }

    @Override
    public @NotNull String getName() {
        return effects
                ? "firework-effects"
                : "firework-power";
    }

    @Override
    public @NotNull MetaResolver.Instance fromString(@NotNull String value) {
        return effects
                ? new EffectsInst(value)
                : new PowerInst(NumberUtils.getInteger(value, 1));
    }

    @Override
    public @Nullable MetaResolver.Instance fromItem(@NotNull ItemMeta meta) {
        if (meta instanceof FireworkMeta fireworkMeta) {
            return effects
                    ? fireworkMeta.hasEffects() ? new EffectsInst(fireworkMeta.getEffects()) : null
                    : new PowerInst(fireworkMeta.getPower());
        }
        return null;
    }

    private record PowerInst(int power) implements Instance {
        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof FireworkMeta fireworkMeta) {
                fireworkMeta.setPower(power);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (meta instanceof FireworkMeta fireworkMeta) {
                return power < 0 || fireworkMeta.getPower() >= power;
            }
            return false;
        }

        @Override
        public @NotNull String getName() {
            return "firework-power";
        }

        @Override
        public @NotNull String asString() {
            return Integer.toString(power);
        }
    }

    private static final class EffectsInst implements Instance {
        private final String effectsStr;
        private final List<FireworkEffect> effects;

        public EffectsInst(@NotNull String effectsStr) {
            this.effectsStr = effectsStr;
            String[] split = effectsStr.split(";");
            this.effects = new ArrayList<>(split.length);
            for (String effectStr : split) {
                Parameters params = Parameters.fromString(effectStr);
                FireworkEffect.Builder builder = FireworkEffect.builder();
                FireworkEffect.Type type = Utils.getEnum(FireworkEffect.Type.class, params.getString("type"), FireworkEffect.Type.BALL); // TODO: Allow search by key
                builder.with(type);
                if (params.getBoolean("flicker")) builder.withFlicker();
                if (params.getBoolean("trail")) builder.withTrail();
                boolean addColor = true;
                if (params.contains("colors")) {
                    for (String colorStr : params.getString("colors").split(" ")) {
                        Color color = Utils.getColor(colorStr);
                        if (color != null) {
                            builder.withColor(color);
                            addColor = false;
                        }
                    }
                }
                if (addColor) {
                    builder.withColor(Color.WHITE);
                }
                if (params.contains("fade-colors")) {
                    for (String colorStr : params.getString("fade-colors").split(" ")) {
                        Color color = Utils.getColor(colorStr);
                        if (color != null) builder.withFade(color);
                    }
                }
                effects.add(builder.build());
            }
        }

        public EffectsInst(@NotNull List<FireworkEffect> effects) {
            this.effects = effects;
            if (effects.isEmpty()) {
                effectsStr = "";
            } else {
                StringBuilder builder = new StringBuilder();
                for (FireworkEffect effect : effects) {
                    builder.append("type:").append(effect.getType().name());
                    if (effect.hasFlicker()) builder.append(" flicker:true");
                    if (effect.hasTrail()) builder.append(" trail:true");
                    if (!effect.getColors().isEmpty()) {
                        builder.append(" colors:{");
                        for (Color color : effect.getColors()) {
                            builder.append(color.getRed()).append(',').append(color.getGreen()).append(',').append(color.getBlue()).append(' ');
                        }
                        builder.deleteCharAt(builder.length() - 1).append('}');
                    }
                    if (!effect.getFadeColors().isEmpty()) {
                        builder.append(" fade-colors:{");
                        for (Color color : effect.getFadeColors()) {
                            builder.append(color.getRed()).append(',').append(color.getGreen()).append(',').append(color.getBlue()).append(' ');
                        }
                        builder.deleteCharAt(builder.length() - 1).append('}');
                    }
                    builder.append("; ");
                }
                effectsStr = Utils.cutBuilder(builder, 2);
            }
        }

        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof FireworkMeta fireworkMeta) {
                fireworkMeta.addEffects(effects);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (meta instanceof FireworkMeta fireworkMeta) {
                return fireworkMeta.getEffects().equals(effects);
            }
            return false;
        }

        @Override
        public @NotNull String getName() {
            return "firework-effects";
        }

        @Override
        public @NotNull String asString() {
            return effectsStr;
        }
    }
}
