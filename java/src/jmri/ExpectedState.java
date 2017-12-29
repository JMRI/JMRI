package jmri;

import javax.annotation.Nonnull;

/**
 * Record an object and its expected state within a specific scenario. This
 * can be used in collections, for example as a route through a set of turnouts,
 * where the turnouts and their state when all turnouts are routed for that
 * route.
 *
 * @author Randall Wood Copyright 2017
 * @param <T> the type of object that this contains the expected state for
 */
public interface ExpectedState<T extends Object> {

    /**
     * Constant for the property name when {@link #setExpectedState(int)} fires
     * a {@link java.beans.PropertyChangeEvent}.
     */
    public final static String EXPECTED_STATE = "expectedState";

    /**
     * Get the expected state. This state should not change as the state of the
     * NamedBean changes.
     *
     * @return the expected state
     */
    public int getExpectedState();

    /**
     * Set the expected state.
     * <p>
     * If UnsupportedOperationException is not thrown, this must fire a
     * {@link java.beans.PropertyChangeEvent} with the name
     * {@value #EXPECTED_STATE}
     *
     * @param state the new expected state
     * @throws UnsupportedOperationException if the implementing class does not
     *                                       allow states to be set
     */
    public void setExpectedState(int state) throws UnsupportedOperationException;

    /**
     * Get the Object this records the expected state for.
     *
     * @return the associated object
     */
    @Nonnull
    public T getObject();
}
