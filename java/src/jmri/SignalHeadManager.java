package jmri;

import java.util.List;

/**
 * Interface for obtaining signal heads.
 * <P>
 * This doesn't have a "new" method, as SignalHeads are separately implemented,
 * instead of being system-specific.
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
 * @author Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public interface SignalHeadManager extends Manager {

    // to free resources when no longer used
    public void dispose();

    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name
     * @return null if no match found
     */
    public SignalHead getSignalHead(String name);

    public SignalHead getByUserName(String s);

    public SignalHead getBySystemName(String s);

    /**
     * Get a list of all SignalHead system names.
     */
    public List<String> getSystemNameList();

}
