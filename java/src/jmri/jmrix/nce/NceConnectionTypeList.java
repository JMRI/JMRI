// NceConnectionTypeList.java

package jmri.jmrix.nce;


/**
 * Returns a list of valid nce Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class NceConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
        return new String[] {
              "jmri.jmrix.nce.serialdriver.ConnectionConfig",
              "jmri.jmrix.nce.networkdriver.ConnectionConfig",
              "jmri.jmrix.nce.usbdriver.ConnectionConfig",
              "jmri.jmrix.nce.simulator.ConnectionConfig"
        };
    }

}

