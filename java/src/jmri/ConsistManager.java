package jmri;

import java.util.ArrayList;

/**
 * Interface for Consist Manager objects, which provide access to the existing
 * Consists and allows for creation and destruction.
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
 * @author Paul Bender Copyright (C) 2003
 */
public interface ConsistManager {

    /**
     * Find a Consist with this consist address, and return it. If the Consist
     * doesn't exist, create it.
     *
     * @param address the consist address
     * @return an existing or new consist
     */
    Consist getConsist(LocoAddress address);

    /**
     * Remove an old Consist.
     *
     * @param address the consist address
     */
    void delConsist(LocoAddress address);

    /**
     * Does this implementation support Command Station Consists?
     *
     * @return true if command station consists are supported; false otherwise
     */
    boolean isCommandStationConsistPossible();

    /**
     * Does a command station consist require a separate consist address from
     * locomotives in consist?
     *
     * @return true is command station consist requires separate address; false
     *         otherwise
     */
    boolean csConsistNeedsSeperateAddress();

    /**
     * Get a list of known consist addresses.
     *
     * @return list of addresses
     */
    ArrayList<LocoAddress> getConsistList();

    /**
     * Translate Error Codes relieved by a consistListener into Strings
     *
     * @param errorCode the code
     * @return the description
     */
    String decodeErrorCode(int errorCode);

    /**
     * Request an update from the layout, loading Consists from the command
     * station.
     */
    void requestUpdateFromLayout();

    /**
     * Does this ConsistManager allow advanced consisting?
     * @return true if this manager's protocols support DCC advanced consisting
     */
     default boolean isAdvancedConsistPossible() {
        return true;
     }
     
    /**
     * Does this ConsistManager require that all locomotives in a consist
     * use the same protocol?
     * @return true if this manager requires that all locomotives in a consist 
     *              use the same protocol
     */
     default boolean isSingleFormConsistRequired() {
        return false;
     }
     
    /**
     * Register a ConsistListListener object with this ConsistManager
     *
     * @param listener a Consist List Listener object.
     */
    void addConsistListListener(ConsistListListener listener);

    /**
     * Remove a ConsistListListener object with this ConsistManager
     *
     * @param listener a Consist List Listener object.
     */
    void removeConsistListListener(ConsistListListener listener);

    /**
     * Notify the registered ConsistListListener objects that the ConsistList
     * has changed.
     */
    void notifyConsistListChanged();

    /**
     * Can this consist manager be disabled?
     * @return true if the manager can be disabled, false otherwise
     */
    default boolean canBeDisabled() {
        return false;
    }

    /**
     * Register a listener that is called if this manager is enabled or disabled.
     * @param listener the listener
     */
    default void registerEnableListener(EnableListener listener) {
        // Do nothing
    }

    /**
     * Unregister a listener that is called if this manager is enabled or disabled.
     * @param listener the listener
     */
    default void unregisterEnableListener(EnableListener listener) {
        // Do nothing
    }
    
    /**
     * Check if this manager is enabled
     * @return true if enabled
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * A listener that listens to whether the manager is enabled or disabled.
     */
    interface EnableListener {

        void setEnabled(boolean value);
    }

}
