package jmri;

/**
 * Allow notification of changes to the consist list.
 * <P>
 * This allows a {@link ConsistManager} object to return delayed status.
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
 * @author	Paul Bender Copyright (C) 2012
 */
public interface ConsistListListener extends java.util.EventListener {

    /**
     * Receive notification that the consist manager has changed it's consist
     * list.
     */
    public void notifyConsistListChanged();
}
