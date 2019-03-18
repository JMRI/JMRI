package jmri;

import javax.annotation.Nonnull;

/**
 * Extends the {@link Manager} class to handle "provide" methods that
 * can create a {@link NamedBean} on request from just its system name.
 *
 *
 * @param <E> the type of NamedBean supported by this manager
 * @author Bob Jacobsen Copyright (C) 2003
 */
public interface ProvidingManager<E extends NamedBean> extends Manager<E> {

    /**
     * Get an existing instance via user name, then system name; if no matching instance is found, create a
     * new NameBean from the system name.
     * <p>
     * If the name is a valid system name, it will be used for the
     * new NamedBean. Otherwise, the {@link Manager#makeSystemName} method will attempt to turn it
     * into a valid system name which the manager will attempt to use. If that fails,
     * an exception is thrown.
     * <p>
     * This is similar to the specific methods found in certain
     * type-specific managers:
     * {@link TurnoutManager#provideTurnout},
     * {@link SensorManager#provideSensor},
     * et al.  Those might be more mnemonic; this one is more generic.  Neither
     * is preferred nor deprecated; use your choice.
     *
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Never null
     * @throws IllegalArgumentException if NamedBean doesn't already exist and the
     *                                  manager cannot create it due to
     *                                  an illegal name or name that can't
     *                                  be parsed.
     */
    @Nonnull
    public E provide(@Nonnull String name) throws IllegalArgumentException;

}
