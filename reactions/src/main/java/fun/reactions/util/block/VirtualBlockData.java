package fun.reactions.util.block;

import fun.reactions.ReActions;
import fun.reactions.util.Utils;
import fun.reactions.util.item.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A block matcher, either a plain {@link Material} type check or a full {@link BlockData} state match.
 * Type-only matching is considerably cheaper than {@link BlockData#matches(BlockData)}, so a config
 * string with no block-state properties (no "[...]") is kept as a {@link Type} instead of being
 * parsed into full {@link BlockData}.
 */
public sealed interface VirtualBlockData {
    boolean matches(@NotNull Block block);

    @NotNull String asString();

    record Type(@NotNull Material material) implements VirtualBlockData {
        @Override
        public boolean matches(@NotNull Block block) {
            return block.getType() == material;
        }

        @Override
        public @NotNull String asString() {
            return material.name();
        }
    }

    record Data(@NotNull BlockData data) implements VirtualBlockData {
        @Override
        public boolean matches(@NotNull Block block) {
            return block.getBlockData().matches(data);
        }

        @Override
        public @NotNull String asString() {
            return data.getAsString(true);
        }
    }

    /**
     * Parse a block matcher from a string, e.g. "STONE" or "minecraft:oak_stairs[facing=north]".
     * Strings without state properties are parsed as a cheap {@link Type} check. Strings with state
     * properties are parsed as full {@link BlockData}; if that fails, the error is logged and parsing
     * falls back to a {@link Type} check on the material name alone.
     * @param str Block matcher string
     * @return Parsed matcher, or null if the string is empty or names no valid block/material
     */
    static @Nullable VirtualBlockData fromString(@Nullable String str) {
        if (Utils.isStringEmpty(str)) return null;
        int stateStart = str.indexOf('[');
        if (stateStart < 0) {
            return typeOf(str);
        }
        try {
            return new Data(Bukkit.createBlockData(str));
        } catch (IllegalArgumentException ex) {
            ReActions.getLogger().warn("Invalid block data '{}', falling back to type match", str, ex);
            return typeOf(str.substring(0, stateStart));
        }
    }

    private static @Nullable VirtualBlockData typeOf(@NotNull String str) {
        String name = str.trim();
        int namespaceEnd = name.indexOf(':');
        if (namespaceEnd >= 0) name = name.substring(namespaceEnd + 1);
        Material material = ItemUtils.getMaterial(name);
        return material == null ? null : new Type(material);
    }
}
