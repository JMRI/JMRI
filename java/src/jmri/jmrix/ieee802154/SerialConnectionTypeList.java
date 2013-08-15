// SerialConnectionTypeList.java

package jmri.jmrix.ieee802154;


/**
 * Returns a list of valid IEEE 802.15.4 Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Paul Bender    Copyright (C) 2013
 * @version	$Revision$
 *
 */
public class SerialConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
        return new String[] {
              "jmri.jmrix.ieee802154.serialdriver.ConnectionConfig",
              "jmri.jmrix.ieee802154.xbee.ConnectionConfig"
        };
    }

}

