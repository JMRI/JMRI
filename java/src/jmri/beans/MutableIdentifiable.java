package jmri.beans;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * An object that has a publicly mutable identity property.
 *
 * @author Randall Wood Copyright 2020
 * @see Identifiable
 */
@API(status = EXPERIMENTAL)
public interface MutableIdentifiable extends Identifiable {

    /**
     * Set the identity.
     *
     * @param id the new identity
     */
    public abstract void setId(@Nonnull String id);
}
