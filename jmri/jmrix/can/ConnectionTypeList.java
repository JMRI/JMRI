// ConnectionTypeList.java

package jmri.jmrix.can;


/**
 * Returns a list of valid lenz XpressNet Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision: 1.2 $
 *
 */
public class ConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
        return new String[] {
              "jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver.ConnectionConfig",
              "jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig",
              "jmri.jmrix.can.adapters.gridconnect.net.ConnectionConfig",
              "jmri.jmrix.can.adapters.lawicell.canusb.serialdriver.ConnectionConfig",
              "jmri.jmrix.can.adapters.loopback.ConnectionConfig"
        };
    }

}

