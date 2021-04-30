package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;

/**
 * A reference to an object.
 * It's a faster replacement for AtomicReference when thread safety is not
 * needed.
 *
 * @param <E> the type of the reference
 *
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class Reference<E> {

    private E _ref;

    /**
     * Create an instance of Reference.
     */
    public Reference() {
    }

    /**
     * Create an instance of Reference.
     * @param ref the reference
     */
    public Reference(@CheckForNull E ref) {
        this._ref = ref;
    }

    /**
     * Set the reference.
     * @param ref the new reference
     */
    public void set(@CheckForNull E ref) {
        this._ref = ref;
    }

    /**
     * Return the reference.
     * @return the reference
     */
    @CheckReturnValue
    @CheckForNull
    public E get() {
        return _ref;
    }

}
