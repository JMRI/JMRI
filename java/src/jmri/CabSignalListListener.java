package jmri;

/**
 * Allow notification of changes to the cab signal list.
 * <P>
 * This allows a {@link CabSignalManager} object to return delayed status.
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
 * @author Paul Bender Copyright (C) 2012
 */
public interface CabSignalListListener extends java.util.EventListener {

    /**
     * Receive notification that the cab signal manager has changed it's signal 
     * list.
     */
    public void notifyCabSignalListChanged();
}
