package jmri;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Interface for controlling sensors.
 *
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
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface SensorManager extends ProvidingManager<Sensor> {

    /**
     * Get the Sensor with the user name, then system name if needed; if that fails, create a
     * new Sensor. 
     * If the name is a valid system name, it will be used for the new Sensor.
     * Otherwise, the {@link Manager#makeSystemName} method will attempt to turn it
     * into a valid system name.
     * <p>This provides the same function as {@link ProvidingManager#provide}
     * which has a more generic form.
     *
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Never null
     * @throws IllegalArgumentException if Sensor doesn't already exist and the
     *                                  manager cannot create the Sensor due to
     *                                  an illegal name or name that can't
     *                                  be parsed.
     */
    @Nonnull
    public Sensor provideSensor(@Nonnull String name) throws IllegalArgumentException;

    @Override
    /** {@inheritDoc} */
    default public Sensor provide(@Nonnull String name) throws IllegalArgumentException { return provideSensor(name); }

    /**
     * Get an existing Sensor or return null if it doesn't exist. 
     * 
     * Locates via user name, then system name if needed.
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    @CheckReturnValue
    @CheckForNull
    public Sensor getSensor(@Nonnull String name);

    // to free resources when no longer used
    @Override
    public void dispose();

    /**
     * Return a Sensor with the specified system and user names. 
     * Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Sensor object representing a given physical turnout and
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
     * Turnouts when you should be looking them up.
     *
     * @param systemName the desired system name
     * @param userName   the desired user name
     * @return requested Sensor object
     * @throws IllegalArgumentException if cannot create the Sensor due to e.g.
     *                                  an illegal name or name that can't be
     *                                  parsed.
     */
    @Nonnull
    public Sensor newSensor(@Nonnull String systemName, @CheckForNull String userName) throws IllegalArgumentException;

    /**
     * Get an existing Sensor or return null if it doesn't exist. 
     * 
     * Locates via user name.
     *
     * @param name User name to match
     * @return null if no match found
     */
    @CheckReturnValue
    @CheckForNull
    public Sensor getByUserName(@Nonnull String name);

    /**
     * Get an existing Sensor or return null if it doesn't exist. 
     * 
     * Locates via system name
     *
     * @param name System name to match
     * @return null if no match found
     */
    @CheckReturnValue
    @CheckForNull
    public Sensor getBySystemName(@Nonnull String name);

    /**
     * Requests status of all layout sensors under this Sensor Manager. This
     * method may be invoked whenever the status of sensors needs to be updated
     * from the layout, for example, when an XML configuration file is read in.
     * Note that there is a null implementation of this method in
     * AbstractSensorManager. This method only needs be implemented in
     * system-specific Sensor Managers where readout of sensor status from the
     * layout is possible.
     */
    public void updateAll();

    /**
     * Determines if it is possible to add a range of sensors in numerical
     * order.
     *
     * @param systemName the system name to check against; appears to be ignored
     *                   in all implementations
     * @return true if possible; false otherwise
     */
    @CheckReturnValue
    public boolean allowMultipleAdditions(@Nonnull String systemName);

    /**
     * Determine if the address supplied is valid and free, if not then it shall
     * return the next free valid address up to a maximum of 10 addresses away
     * from the initial address. Used when adding a range of Sensors.
     *
     * @param curAddress The hardware address of the sensor we wish to add
     * @param prefix     The System Prefix used to make up the systemName
     *                   check.
     * @return null if the system name made from prefix and curAddress is in
     *         use
     * @throws jmri.JmriException if problem calculating next address
     */
    @CheckReturnValue
    @CheckForNull
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException;

    /**
     * Get a system name for a given hardware address and system prefix.
     *
     * @param curAddress desired hardware address
     * @param prefix     system prefix used in system name
     * @return the complete sensor system name for the prefix and current
     *         address
     * @throws jmri.JmriException if unable to create a system name for the
     *                            given address, possibly due to invalid address
     *                            format
     */
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException;

    @CheckReturnValue
    public long getDefaultSensorDebounceGoingActive();

    @CheckReturnValue
    public long getDefaultSensorDebounceGoingInActive();

    public void setDefaultSensorDebounceGoingActive(long timer);

    public void setDefaultSensorDebounceGoingInActive(long timer);

    /**
     * Do the sensor objects provided by this manager support configuring
     * an internal pullup or pull down resistor?
     *
     * @return true if pull up/pull down configuration is supported.
     */
    public boolean isPullResistanceConfigurable();

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip();

}
