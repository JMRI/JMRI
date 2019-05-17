package jmri;

/**
 * Allow notification of changes to the cab signal list.
 * <p>
 * This allows a {@link CabSignalManager} object to return delayed status.
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
 * @author Paul Bender Copyright (C) 2012
 */
public interface CabSignalListListener extends java.util.EventListener {

    /**
     * Receive notification that the cab signal manager has changed its signal
     * list.
     */
    public void notifyCabSignalListChanged();
}
