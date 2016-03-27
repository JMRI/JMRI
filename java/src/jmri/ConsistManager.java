package jmri;

import java.util.ArrayList;

/**
 * Interface for Consist Manager objects, which provide access to the existing
 * Consists and allows for creation and destruction.
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
 *
 * @author Paul Bender Copyright (C) 2003
 * @version $Revision$
 */
public interface ConsistManager {

    /**
     * Find a Consist with this consist address, and return it. If the Consist
     * doesn't exit, create it.
     */
    public Consist getConsist(DccLocoAddress address);

    /**
     * Remove an old Consist
     */
    public void delConsist(DccLocoAddress address);

    /**
     * Does this implementation support Command Station Consists?
     */
    public boolean isCommandStationConsistPossible();

    /**
     * Does a CS consist require a separate consist address?
     */
    public boolean csConsistNeedsSeperateAddress();

    /**
     * Get an ArrayList object containing the string representation of the
     * consist addresses we know about.
     */
    public ArrayList<DccLocoAddress> getConsistList();

    /**
     * Translate Error Codes relieved by a consistListener into Strings
     */
    public String decodeErrorCode(int errorCode);

    /**
     * Request an update from the layout, loading Consists from the command
     * station.
     */
    public void requestUpdateFromLayout();

    /**
     * Register a ConsistListListener object with this ConsistManager
     *
     * @param listener a Consist List Listener object.
     */
    public void addConsistListListener(ConsistListListener listener);

    /**
     * Remove a ConsistListListener object with this ConsistManager
     *
     * @param listener a Consist List Listener object.
     */
    public void removeConsistListListener(ConsistListListener listener);

    /**
     * Notify the registered ConsistListListener objects that the ConsistList
     * has changed.
     */
    public void notifyConsistListChanged();
}
