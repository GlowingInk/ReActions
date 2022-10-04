package me.fromgate.reactions.util.item.resolvers;

import me.fromgate.reactions.util.Utils;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookResolver implements MetaResolver {
    private final Type type;

    public BookResolver(@NotNull Type type) {
        this.type = type;
    }

    @Override
    public @NotNull String getName() {
        return switch (type) {
            case TITLE -> "book-title";
            case AUTHOR -> "book-author";
            case PAGES -> "book-pages";
        };
    }

    @Override
    public @NotNull MetaResolver.Instance fromString(@NotNull String value) {
        return switch (type) {
            case TITLE -> new TextInst(value, false);
            case AUTHOR -> new TextInst(value, true);
            case PAGES -> new PagesInst(value);
        };
    }

    @Override
    public @Nullable MetaResolver.Instance fromItem(@NotNull ItemMeta meta) {
        if (!(meta instanceof BookMeta bookMeta)) return null;
        return switch (type) {
            case TITLE -> bookMeta.hasTitle() ? new TextInst(bookMeta.getTitle(), false) : null;
            case AUTHOR -> bookMeta.hasAuthor() ? new TextInst(bookMeta.getAuthor(), true) : null;
            case PAGES -> bookMeta.hasPages() ? new PagesInst(bookMeta.getPages()) : null;
        };
    }

    private static final class PagesInst implements Instance {
        private final List<String> pages;
        private final String pagesStr;

        private PagesInst(@NotNull List<String> pages) {
            this.pages = pages;
            if (pages.isEmpty()) {
                pagesStr = "";
            } else {
                StringBuilder builder = new StringBuilder();
                for (String page : pages) {
                    builder.append(page.replace("\\n", "&z")).append("\\n");
                }
                pagesStr = Utils.cutBuilder(builder, 2);
            }
        }

        public PagesInst(@NotNull String pagesStr) {
            this.pagesStr = pagesStr;
            String[] split = pagesStr.split("\\\\n");
            this.pages = new ArrayList<>(split.length);
            for (String page : split) this.pages.add(page.replace("&z", "\n"));
        }

        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof BookMeta bookMeta) {
                bookMeta.setPages(pages);
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (meta instanceof BookMeta bookMeta) {
                return pages.equals(bookMeta.getPages());
            }
            return false;
        }

        @Override
        public @NotNull String getName() {
            return "book-pages";
        }

        @Override
        public @NotNull String asString() {
            return pagesStr;
        }
    }

    private record TextInst(String value, boolean author) implements Instance {
        @Override
        public void apply(@NotNull ItemMeta meta) {
            if (meta instanceof BookMeta bookMeta) {
                if (author) {
                    bookMeta.setAuthor(value);
                } else {
                    bookMeta.setTitle(value);
                }
            }
        }

        @Override
        public boolean isSimilar(@NotNull ItemMeta meta) {
            if (meta instanceof BookMeta bookMeta) {
                if (author) {
                    return Objects.equals(value, bookMeta.getAuthor());
                } else {
                    return Objects.equals(value, bookMeta.getTitle());
                }
            }
            return false;
        }

        @Override
        public @NotNull String getName() {
            return author
                    ? "book-author"
                    : "book-title";
        }

        @Override
        public @NotNull String asString() {
            return value;
        }
    }

    public enum Type {
        TITLE, AUTHOR, PAGES
    }
}
