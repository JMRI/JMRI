package jmri.beans;

import javax.annotation.Nonnull;

/**
 * A Bean that has an identity property that is not publicly mutable (it may be
 * indirectly mutated) and provides support for listening to property changes.
 * 
 * @author Randall Wood Copyright 2020
 */
public abstract class IdentifiedBean extends PropertyChangeProviderImpl {

    /**
     * Get the identity of the object.
     *
     * @return the identity
     */
    @Nonnull
    public abstract String getId();
}
