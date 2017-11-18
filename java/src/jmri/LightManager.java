package jmri;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Interface for obtaining Lights.
 * <P>
 * This doesn't have a "new" method, as Lights are separately implemented,
 * instead of being system-specific.
 * <P>
 * Based on SignalHeadManager.java
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author Dave Duchamp Copyright (C) 2004
 */
public interface LightManager extends Manager<Light> {

    /**
     * Locate via user name, then system name if needed. If that fails, create a
     * new Light: If the name is a valid system name, it will be used for the
     * new Light. Otherwise, the makeSystemName method will attempt to turn it
     * into a valid system name.
     *
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Never null under normal circumstances
     */
    @Nonnull
    public Light provideLight(@Nonnull String name);

    // to free resources when no longer used
    @Override
    public void dispose();

    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Never null
     * @throws IllegalArgumentException if Light doesn't already exist and the
     *                                  manager cannot create the Light due to
     *                                  an illegal name or name that can't be
     *                                  parsed.
     */
    @CheckReturnValue
    @CheckForNull
    public Light getLight(@Nonnull String name);

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Light object representing a given physical Light and therefore
     * only one with a specific system or user name.
     * <P>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <UL>
     * <LI>If a null reference is given for user name, no user name will be
     * associated with the Light object created; a valid system name must be
     * provided
     * <LI>If both names are provided, the system name defines the hardware
     * access of the desired sensor, and the user address is associated with it.
     * The system name must be valid.
     * </UL>
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
    public Light newLight(@Nonnull String systemName, @CheckForNull String userName);

    /**
     * Locate a Light by its user name.
     *
     * @param s the user name
     * @return the light or null if not found
     */
    @CheckReturnValue
    @CheckForNull
    public Light getByUserName(@Nonnull String s);

    /**
     * Locate a Light by its system name.
     *
     * @param s the system name
     * @return the light or null if not found
     */
    @CheckReturnValue
    @CheckForNull
    public Light getBySystemName(@Nonnull String s);

    /**
     * Test if parameter is a valid system name for current configuration.
     *
     * @param systemName the system name
     * @return true if valid; false otherwise
     */
    @CheckReturnValue
    public boolean validSystemNameConfig(@Nonnull String systemName);

    /**
     * Normalize the system name
     * <P>
     * This routine is used to ensure that each system name is uniquely linked
     * to one C/MRI bit, by removing extra zeros inserted by the user.
     * <P>
     * This routine is implemented in AbstractLightManager to return the same
     * name. If a system implementation has names that could be normalized, the
     * system-specific Light Manager should override this routine and supply a
     * normalized system name.
     *
     * @param systemName the system name to normalize
     * @return the normalized system name
     */
    @CheckReturnValue
    @Nonnull
    public String normalizeSystemName(@Nonnull String systemName);

    /**
     * Convert the system name to a normalized alternate name
     * <P>
     * This routine is to allow testing to ensure that two Lights with alternate
     * names that refer to the same output bit are not created.
     * <P>
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
     * Get a list of all Light system names.
     *
     * @return a list of all system names
     */
    @CheckReturnValue
    @Nonnull
    @Override
    public List<String> getSystemNameList();

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

}
