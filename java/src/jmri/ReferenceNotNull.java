package jmri;


import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * A reference to an object where the object must not be null.
 * It's a faster replacement for AtomicReference when thread safety is not
 * needed.
 *
 * @param <E> the type of the reference
 *
 * @author Daniel Bergqvist Copyright (C) 2024
 */
public class ReferenceNotNull<E> {

    private E _ref;

    /**
     * Create an instance of Reference.
     */
    public ReferenceNotNull() {
    }

    /**
     * Create an instance of Reference.
     * @param ref the reference
     */
    public ReferenceNotNull(@Nonnull E ref) {
        this._ref = ref;
    }

    /**
     * Set the reference.
     * @param ref the new reference
     */
    public void set(@Nonnull E ref) {
        this._ref = ref;
    }

    /**
     * Return the reference.
     * @return the reference
     */
    @CheckReturnValue
    @Nonnull
    public E get() {
        return _ref;
    }

}
