package jmri.jmrit.operations;

import javax.annotation.Nonnull;

/**
 * An object that has an identity.
 *
 * @author Randall Wood Copyright 2020
 */
public interface Identified {

    /**
     * Get the identity of the object.
     *
     * @return the identity
     */
    @Nonnull
    public String getId();
}
