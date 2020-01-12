package jmri;

/**
 * Tags objects that remember whether they've been modified and need to be
 * (optionally) stored, for example before the program terminates or when a
 * window closes.
 * <p>
 * The default state of an object is undefined.
 * <p>
 * The modified flag is not a bound parameter
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
 * @author Bob Jacobsen Copyright (C) 2008
 * @since 2.1.6
 */
public interface ModifiedFlag {

    /**
     * Set the modified flag to a specific value
     *
     * @param flag true if the object has been modified
     */
    public void setModifiedFlag(boolean flag);

    /**
     * Get the current value of the modified flag.
     *
     * @return true if the object has been modified and needs to be stored
     */
    public boolean getModifiedFlag();
}
