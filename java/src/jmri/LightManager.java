package jmri;

import java.util.List;

/**
 * Interface for obtaining Lights.
 * <P>
 * This doesn't have a "new" method, as Lights are separately implemented,
 * instead of being system-specific.
 * <P>
 * Based on SignalHeadManager.java
 *
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
public interface LightManager extends Manager {

    /**
     * Locate via user name, then system name if needed. If that fails, create a
     * new Light: If the name is a valid system name, it will be used for the
     * new Light. Otherwise, the makeSystemName method will attempt to turn it
     * into a valid system name.
     *
     * @param name
     * @return Never null under normal circumstances
     */
    public Light provideLight(String name);

    // to free resources when no longer used
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
     *                                  e.g. an illegal name or name that can't
     *                                  be parsed.
     */
    public Light getLight(String name);

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
     * @return requested Light object (never null)
     * @throws IllegalArgumentException if cannot create the Light due to e.g.
     *                                  an illegal name or name that can't be
     *                                  parsed.
     */
    public Light newLight(String systemName, String userName);

    /**
     * Locate a Light by its user name
     */
    public Light getByUserName(String s);

    /**
     * Locate a Light by its system name
     */
    public Light getBySystemName(String s);

    /**
     * Validate system name format returns 'true' if system name has a valid
     * format, else returns 'false'
     */
    public boolean validSystemNameFormat(String systemName);

    /**
     * Validate system name for the current hardware configuration returns
     * 'true' if system name has a valid meaning in current configuration, else
     * returns 'false'
     */
    public boolean validSystemNameConfig(String systemName);

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
     */
    public String normalizeSystemName(String systemName);

    /**
     * Convert the system name to a normalized alternate name
     * <P>
     * This routine is to allow testing to ensure that two Lights with alternate
     * names that refer to the same output bit are not created.
     * <P>
     * This routine is implemented in AbstractLightManager to return "". If a
     * system implementation has alternate names, the system specific Light
     * Manager should override this routine and supply the alternate name.
     */
    public String convertSystemNameToAlternate(String systemName);

    /**
     * Get a list of all Light system names.
     */
    public List<String> getSystemNameList();

    /**
     * Activate the control mechanism for each Light controlled by this
     * LightManager. Note that some Lights don't require any activation. The
     * activateLight method in AbstractLight.java determines what needs to be
     * done for each Light.
     */
    public void activateAllLights();

    /**
     * Returns 'true' if the System can potentially support variable Lights
     */
    public boolean supportsVariableLights(String systemName);

    /**
     * A method that determines if it is possible to add a range of lights in
     * numerical order eg 11 thru 18, primarily used to show/not show the add
     * range box in the add Light window
     *
     */
    public boolean allowMultipleAdditions(String systemName);

}
