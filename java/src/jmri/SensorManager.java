package jmri;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.CheckReturnValue;

/**
 * Interface for controlling sensors.
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
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public interface SensorManager extends Manager {

    /**
     * Locate via user name, then system name if needed. If that fails, create a
     * new sensor: If the name is a valid system name, it will be used for the
     * new sensor. Otherwise, the makeSystemName method will attempt to turn it
     * into a valid system name.
     *
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Never null
     * @throws IllegalArgumentException if Sensor doesn't already exist and the
     *                                  manager cannot create the Sensor due to
     *                                  e.g. an illegal name or name that can't
     *                                  be parsed.
     */
    public @Nonnull Sensor provideSensor(@Nonnull String name) throws IllegalArgumentException;

    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    @CheckReturnValue
    public@CheckForNull Sensor getSensor(@Nonnull String name);

    // to free resources when no longer used
    public void dispose();

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Sensor object representing a given physical turnout and
     * therefore only one with a specific system or user name.
     * <P>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <UL>
     * <LI>If a null reference is given for user name, no user name will be
     * associated with the Sensor object created; a valid system name must be
     * provided
     * <LI>If both names are provided, the system name defines the hardware
     * access of the desired sensor, and the user address is associated with it.
     * The system name must be valid.
     * </UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Turnouts when you should be looking them up.
     *
     * @return requested Sensor object (never null)
     * @throws IllegalArgumentException if cannot create the Sensor due to e.g.
     *                                  an illegal name or name that can't be
     *                                  parsed.
     */
    public @Nonnull Sensor newSensor(@Nonnull String systemName, @CheckForNull String userName) throws IllegalArgumentException;

    @CheckReturnValue
    public @CheckForNull Sensor getByUserName(@Nonnull String s);

    @CheckReturnValue
    public @CheckForNull Sensor getBySystemName(@Nonnull String s);

    @CheckReturnValue
    public @Nonnull List<String> getSystemNameList();

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
     * A method that determines if it is possible to add a range of sensors in
     * numerical order eg 10 to 30 will return true. where as if the address
     * format is 1b23 this will return false.
     *
     */
    @CheckReturnValue
    public boolean allowMultipleAdditions(@Nonnull String systemName);

    /**
     * Determine if the address supplied is valid and free, if not then it shall
     * return the next free valid address up to a maximum of 10 address away
     * from the initial address.
     *
     * @param curAddress - The hardware address of the sensor we wish to add
     * @param prefix     - The System Prefix used to make up the systemName
     *                   check.
     * @return           - null if the system name made from prefix and curAddress is in use
     * @throws jmri.JmriException if problem calculating next address
     */
    @CheckReturnValue
    public @CheckForNull String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException;

    public @Nonnull String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException;

    @CheckReturnValue
    public long getDefaultSensorDebounceGoingActive();

    @CheckReturnValue
    public long getDefaultSensorDebounceGoingInActive();

    public void setDefaultSensorDebounceGoingActive(long timer);

    public void setDefaultSensorDebounceGoingInActive(long timer);
}
