package jmri;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;

/**
 * Interface for obtaining StringIOs.
 * 
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public interface StringIOManager extends ProvidingManager<StringIO>, NameIncrementingManager {

    @Nonnull
    StringIO provide(@Nonnull String name) throws IllegalArgumentException;

    @Nonnull
    StringIO provideStringIO(@Nonnull String name) throws IllegalArgumentException;

    /**
     * Return a StringIO with the specified user or system name.
     * Return StringIO by UserName else provide by SystemName.
     * <p>
     * Note that
     * two calls with the same arguments will get the same instance; there is
     * only one StringIO object representing a given physical turnout and
     * therefore only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the Sensor object created; a valid system name must be
     * provided
     * <li>If both names are provided, the system name defines the hardware
     * access of the desired sensor, and the user address is associated with it.
     * The system name must be valid.
     * </ul>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * StringIOs when you should be looking them up.
     *
     * @param systemName the desired system name
     * @param userName   the desired user name
     * @return requested StringIO object
     * @throws IllegalArgumentException if cannot create the StringIO due to e.g.
     *                                  an illegal name or name that can't be
     *                                  parsed.
     */
    @Nonnull
    StringIO newStringIO(@Nonnull String systemName, @CheckForNull String userName) throws IllegalArgumentException;

}
