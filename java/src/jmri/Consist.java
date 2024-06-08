package jmri;

import java.util.ArrayList;

/**
 * Interface for a Consist Object, describing one or more
 * cooperatively-operating locomotives.
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
 * @author Paul Bender Copyright (C) 2003-2008
 */
public interface Consist {

    // Constants for the ConsistType
    // For Advanced Consists
    int ADVANCED_CONSIST = 0;
    // For Command Station Consists
    // This is for a: Digitrax Universal Consist,
    // or Lenz Double Header,or NCE "old Style Consist",etc
    int CS_CONSIST = 1;
    // Position Constants
    // 0x00 represents the lead locomotive
    // 0xFF represents the trailing (or rear) locomotive in the consist
    // All other values in between are middle locomotives
    int POSITION_LEAD = 0x00;
    int POSITION_TRAIL = 0xFF;

    /**
     * A method for cleaning up the consist
     */
    void dispose();

    /**
     * Set the Consist Type.
     *
     * @param consistType the consist type
     */
    void setConsistType(int consistType);

    /**
     * Get the Consist Type.
     *
     * @return the consist type
     */
    int getConsistType();

    /**
     * Get the Consist Address
     *
     * @return the consist address
     */
    DccLocoAddress getConsistAddress();

    /**
     * Is the specific address allowed? (needed for system specific
     * restrictions)
     *
     * @param address the address
     * @return true if allowed; false otherwise
     */
    boolean isAddressAllowed(DccLocoAddress address);

    /**
     * Is there a size limit for this type of consist?
     *
     * @return -1 if no limit; 0 if the Consist Type is not supported; or the
     *         total number of usable spaces if the consist has a limit (do not
     *         subtract used spaces).
     */
    int sizeLimit();

    /**
     * Get a list of the locomotives in the consist.
     *
     * @return the list of addresses
     */
    ArrayList<DccLocoAddress> getConsistList();

    /**
     * Does the consist contain the specified locomotive address?
     *
     * @param address the address to check
     * @return true if in consist; false otherwise
     */
    boolean contains(DccLocoAddress address);

    /**
     * Get the relative direction setting for a specific locomotive in the
     * consist.
     *
     * @param address the address to check
     * @return true if locomotive is in consist in its normal direction of
     *         travel; false otherwise
     */
    boolean getLocoDirection(DccLocoAddress address);

    /**
     * Add a Locomotive to a Consist
     *
     * @param address         is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling the same
     *                        direction as the consist, or false otherwise.
     */
    void add(DccLocoAddress address, boolean directionNormal);

    /**
     * Restore a Locomotive to a Consist, but don't write to the command
     * station. This is used for restoring the consist from a file or adding a
     * consist read from the command station.
     *
     * @param address         is the Locomotive address to add to the consist
     * @param directionNormal is True if the locomotive is traveling the same
     *                        direction as the consist, or false otherwise.
     */
    void restore(DccLocoAddress address, boolean directionNormal);

    /**
     * Remove a Locomotive from this Consist
     *
     * @param address is the Locomotive address to add to the locomotive
     */
    void remove(DccLocoAddress address);

    /**
     * Set the position of a locomotive within the consist
     *
     * @param address  is the Locomotive address
     * @param position is a constant representing the position within the
     *                 consist.
     */
    void setPosition(DccLocoAddress address, int position);

    /**
     * Get the position of a locomotive within the consist
     *
     * @param address is the Locomotive address of interest
     * @return integer equal to jmri.Consist.POSITION_LEAD for the designated
     *         lead locomotive. equal to jmri.Consist.POSITION_TRAIL for the
     *         designated trailing locomotive. between 1 and 254 for other
     *         locomotives in the consist
     */
    int getPosition(DccLocoAddress address);

    /**
     * Set the roster entry of a locomotive within the consist
     *
     * @param address  is the Locomotive address
     * @param rosterId is the roster Identifier of the associated roster entry.
     */
    void setRosterId(DccLocoAddress address, String rosterId);

    /**
     * Get the rosterId of a locomotive within the consist
     *
     * @param address is the Locomotive address of interest
     * @return string roster Identifier associated with the given address 
     *         in the consist.  Returns null if no roster entry is associated
     *         with this entry.
     */
    String getRosterId(DccLocoAddress address);

    /**
     * Add a listener for consist events
     *
     * @param listener is a consistListener object
     */
    void addConsistListener(jmri.ConsistListener listener);

    /**
     * Remove a listener for consist events
     *
     * @param listener is a consistListener object
     */
    void removeConsistListener(jmri.ConsistListener listener);

    /**
     * Set the text ID associated with the consist
     *
     * @param id is a string identifier for the consist
     */
    void setConsistID(String id);

    /**
     * Get the text ID associated with the consist
     *
     * @return String identifier for the consist default value is the string
     *         Identifier for the consist address.
     */
    String getConsistID();

    /**
     * Reverse the order of the consist and the direction the locomotives are
     * traveling
     */
    void reverse();

    /**
     * Restore the consist to the command station.
     */
    void restore();

}
