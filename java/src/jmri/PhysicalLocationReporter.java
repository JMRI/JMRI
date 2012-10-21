package jmri;

/* <hr>
 * PhysicalLocationReporter Interface
 * </hr>
 * Implements a common way that a Reporter that supports having a Physical Location
 * can report that location to querying objects.
/*

/*
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Mark Underwood Copyright (C) 2012
 * @version			$Revision: 18722 $
 */

public interface PhysicalLocationReporter {

    static public enum Direction { UNKNOWN, ENTER, EXIT }

    public LocoAddress getLocoAddress(String s);

    public Direction getDirection(String s);
}

