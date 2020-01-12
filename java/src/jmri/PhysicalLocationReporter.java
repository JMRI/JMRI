package jmri;

import jmri.util.PhysicalLocation;

/**
 * Implements a common way that a Reporter that supports having a Physical Location
 * can report that location to querying objects.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author   Mark Underwood Copyright (C) 2012
 */
public interface PhysicalLocationReporter {

    static public enum Direction {

        UNKNOWN, ENTER, EXIT
    }

    public LocoAddress getLocoAddress(String s);

    public Direction getDirection(String s);

    public PhysicalLocation getPhysicalLocation();

    public PhysicalLocation getPhysicalLocation(String s);

}
