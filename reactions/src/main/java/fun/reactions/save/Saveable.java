package fun.reactions.save;

public interface Saveable {
    default void save() {
        saveSync();
    }

    void saveSync();
}
