package jmri.beans;

import javax.annotation.Nonnull;

/**
 * A Bean that has a publicly mutable identity property and provides support for
 * listening to property changes.
 * 
 * @author Randall Wood Copyright 2020
 * @see IdentifiedBean
 */
public abstract class MutableIdentifiedBean extends IdentifiedBean {

    /**
     * Set the identity.
     * 
     * @param id the new identity
     */
    public abstract void setId(@Nonnull String id);
}
