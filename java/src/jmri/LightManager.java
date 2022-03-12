package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Interface for obtaining Lights.
 * <p>
 * This doesn't have a "new" method, as Lights are separately implemented,
 * instead of being system-specific.
 * <p>
 * Based on SignalHeadManager.java
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Dave Duchamp Copyright (C) 2004
 */
public interface LightManager extends ProvidingManager<Light> {

    /**
     * Get the Light with the user name, then system name if needed; if that fails, create a
     * new Light. 
     * If the name is a valid system name, it will be used for the new Light.
     * Otherwise, the {@link Manager#makeSystemName} method will attempt to turn it
     * into a valid system name.
     * <p>This provides the same function as {@link ProvidingManager#provide}
     * which has a more generic form.
     *
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Never null under normal circumstances
     */
    @Nonnull
    public Light provideLight(@Nonnull String name) throws IllegalArgumentException;

    /** {@inheritDoc} */
    @Override
    @Nonnull
    default public Light provide(@Nonnull String name) throws IllegalArgumentException { return provideLight(name); }

    /** {@inheritDoc} */
    @Override
    public void dispose();

    /**
     * Get an existing Light or return null if it doesn't exist.
     * <p>
     * Locates via user name, then system name if needed. 
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Light, or null if no existing Light.
     */
    @CheckReturnValue
    @CheckForNull
    public Light getLight(@Nonnull String name);

    /**
     * Return a Light with the specified user or system name.
     * Lookup Light by UserName, then Provide New Light by SystemName.
     * Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Light object representing a given physical Light and therefore
     * only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the Light object created; a valid system name must be
     * provided
     * <li>If both names are provided, the system name defines the hardware
     * access of the desired sensor, and the user address is associated with it.
     * The system name must be valid.
     * </ul>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Lights when you should be looking them up.
     *
     * @param systemName the desired system name
     * @param userName   the desired user name
     * @return requested Light object (never null)
     * @throws IllegalArgumentException if cannot create the Light due to e.g.
     *                                  an illegal name or name that can't be
     *                                  parsed.
     */
    @Nonnull
    public Light newLight(@Nonnull String systemName, @CheckForNull String userName) throws IllegalArgumentException;

    /**
     * Locate a Light by its user name.
     *
     * @param s the user name
     * @return the light or null if not found
     */
    @CheckReturnValue
    @CheckForNull
    @Override
    public Light getByUserName(@Nonnull String s);

    /**
     * Locate a Light by its system name.
     *
     * @param s the system name
     * @return the light or null if not found
     */
    @CheckReturnValue
    @CheckForNull
    @Override
    public Light getBySystemName(@Nonnull String s);

    /**
     * Test if parameter is a valid system name for current configuration.
     *
     * @param systemName the system name
     * @return true if valid; false otherwise
     */
    @CheckReturnValue
    public default boolean validSystemNameConfig(@Nonnull String systemName){
        try {
            validateSystemNameFormat(systemName);
            return true;
        } catch (jmri.NamedBean.BadSystemNameException ex) {
            return false;
        }
    }

    /**
     * Convert the system name to a normalized alternate name.
     * <p>
     * This routine is to allow testing to ensure that two Lights with alternate
     * names that refer to the same output bit are not created.
     * <p>
     * This routine is implemented in AbstractLightManager to return "". If a
     * system implementation has alternate names, the system specific Light
     * Manager should override this routine and supply the alternate name.
     *
     * @param systemName the system name to convert
     * @return an alternate name
     */
    @CheckReturnValue
    @Nonnull
    public String convertSystemNameToAlternate(@Nonnull String systemName);

    /**
     * Activate the control mechanism for each Light controlled by this
     * LightManager. Note that some Lights don't require any activation. The
     * activateLight method in AbstractLight.java determines what needs to be
     * done for each Light.
     */
    public void activateAllLights();

    /**
     * Test if system in the given name can support a variable light.
     *
     * @param systemName the system name
     * @return true if variable lights are supported; false otherwise
     */
    @CheckReturnValue
    public boolean supportsVariableLights(@Nonnull String systemName);

    /**
     * Test if possible to generate multiple lights given a numerical range to
     * complete the system name.
     *
     * @param systemName the system name
     * @return true if multiple lights can be created at once; false otherwise
     */
    @CheckReturnValue
    public boolean allowMultipleAdditions(@Nonnull String systemName);
    
    /**
     * Get the Next valid hardware address.
     * Used by the Turnout / Sensor / Reporter / Light Manager classes.
     * <p>
     * System-specific methods may want to override getIncrement() rather than this one.
     * @param curAddress the starting hardware address to get the next valid from.
     * @param prefix system prefix, just system name, not type letter.
     * @param ignoreInitialExisting false to return the starting address if it 
     *                          does not exist, else true to force an increment.
     * @return the next valid system name, excluding both system name prefix and type letter.
     * @throws JmriException    if unable to get the current / next address, 
     *                          or more than 10 next addresses in use.
     */
    @Nonnull
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws JmriException;

    /**
     * Get a system name for a given hardware address and system prefix.
     *
     * @param curAddress desired hardware address
     * @param prefix     system prefix used in system name, excluding Bean type-letter.
     * @return the complete Light system name for the prefix and current
     *         address
     * @throws jmri.JmriException if unable to create a system name for the
     *                            given address, possibly due to invalid address
     *                            format
     */
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException;
    
}
