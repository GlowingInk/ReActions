package fun.reactions.util.item.aspects;


import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

@Aliased.Names({"custommodeldata", "model"})
public class ModelAspect implements MetaAspect {
    @Override
    public @NotNull String getName() {
        return "custom-model-data";
    }

    @Override
    public @NotNull MetaAspect.Instance fromString(@NotNull String value) {
        OptionalInt modelValue = NumberUtils.parseInteger(value, Is.NON_NEGATIVE);
        return modelValue.isPresent()
                ? new ModelInst(modelValue.getAsInt())
                : ModelInst.EMPTY;
    }

    @Override
    public @Nullable MetaAspect.Instance fromItem(@NotNull ItemMeta meta) {
        return meta.hasCustomModelData()
                ? new ModelInst(meta.getCustomModelData())
                : null;
    }

    private record ModelInst(Integer value) implements MetaAspect.Instance {
        private static final ModelInst EMPTY = new ModelInst(null);

        @Override
        public void apply(@NotNull ItemMeta meta) {
            meta.setCustomModelData(value);
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (value != null) {
                return meta.hasCustomModelData() && meta.getCustomModelData() == value;
            }
            return !meta.hasCustomModelData();
        }

        @Override
        public @NotNull String getName() {
            return "custom-model-data";
        }

        @Override
        public @NotNull String asString() {
            return value == null ? "" : value.toString();
        }
    }
}
