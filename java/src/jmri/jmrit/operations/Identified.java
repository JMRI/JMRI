package jmri.jmrit.operations;

import javax.annotation.Nonnull;

import jmri.beans.PropertyChangeProviderImpl;

/**
 * An operations object that has an identity and provides support for listening to property changes.
 * 
 * @author Randall Wood Copyright 2020
 */
public abstract class Identified extends PropertyChangeProviderImpl {

    /**
     * Get the identity of the object.
     *
     * @return the identity
     */
    @Nonnull
    public abstract String getId();
}
