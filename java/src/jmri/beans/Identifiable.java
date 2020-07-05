package jmri.beans;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * An Object that has an identity property that is not publicly mutable (it may be
 * indirectly mutated).
 * 
 * @author Randall Wood Copyright 2020
 * @see MutableIdentifiable
 */
@API(status = EXPERIMENTAL)
public interface Identifiable {

    /**
     * Get the identity of the object.
     *
     * @return the identity
     */
    @Nonnull
    public abstract String getId();
}
