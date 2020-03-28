package jmri.beans;

import javax.annotation.Nonnull;

/**
 * An Object that has an identity property that is not publicly mutable (it may be
 * indirectly mutated).
 * 
 * @author Randall Wood Copyright 2020
 * @see MutableIdentifiable
 */
public interface Identifiable {

    /**
     * Get the identity of the object.
     *
     * @return the identity
     */
    @Nonnull
    public abstract String getId();
}
