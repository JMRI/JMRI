// MergConnectionTypeList.java

package jmri.jmrix.merg;

/**
 * Returns a list of valid connection types for MERG
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
 * @author      Matthew Harris  Copyright (c) 2011
 * @version     $Revision$
 */
public class MergConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() {
        // set the connection types to have MERG at the front
        jmri.jmrix.can.ConfigurationManager.setMERG();

        return new String[] {
            "jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.net.MergConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.net.ConnectionConfig",
            "jmri.jmrix.can.adapters.lawicell.canusb.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.loopback.ConnectionConfig",
            "jmri.jmrix.rfid.serialdriver.ConnectionConfig"
        };
    }
}

/* @(#)MergConnectionTypeList.java */