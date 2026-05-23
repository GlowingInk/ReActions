package fun.reactions.util.bool;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public enum OptionalBoolean {
    TRUE(true), FALSE(false), EMPTY(false);
    
    private final boolean value;
    
    OptionalBoolean(boolean value) {
        this.value = value;
    }

    public static OptionalBoolean empty() {
        return EMPTY;
    }

    public static OptionalBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }

    public boolean getAsBoolean() {
        if (isPresent()) {
            return value;
        } else {
            throw new NoSuchElementException("No value present");
        }
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public void ifPresent(@NotNull BooleanConsumer action) {
        if (isPresent()) {
            action.accept(this.value);
        }

    }

    public void ifPresentOrElse(@NotNull BooleanConsumer action, @NotNull Runnable emptyAction) {
        if (isPresent()) {
            action.accept(this.value);
        } else {
            emptyAction.run();
        }
    }

    public boolean orElse(boolean other) {
        return isPresent() ? this.value : other;
    }

    public boolean orElseGet(@NotNull BooleanSupplier supplier) {
        return isPresent() ? this.value : supplier.getAsBoolean();
    }

    public boolean orElseThrow() {
        if (isPresent()) {
            return this.value;
        } else {
            throw new NoSuchElementException("No value present");
        }
    }

    public <X extends Throwable> boolean orElseThrow(@NotNull Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return this.value;
        } else {
            throw exceptionSupplier.get();
        }
    }
}
