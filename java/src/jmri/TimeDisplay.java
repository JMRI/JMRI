package jmri;

/**
 * Provide access to basic functions of a clock face, that displays time in some
 * particular way.
 * <P>
 * There's really not all that much here, and an abstract interface is perhaps
 * not yet needed.
 * <P>
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
 * @author	Bob Jacobsen Copyright (C) 2004
 * @version	$Revision$
 */
public interface TimeDisplay {

    public void setUpdateRate(int msec);

    public int getUpdateRate();

}
