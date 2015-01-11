// RpsConnectionTypeList.java

package jmri.jmrix.rps;


/**
 * Returns a list of valid lenz XpressNet Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class RpsConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
        return new String[] {
              "jmri.jmrix.rps.serial.ConnectionConfig"
        };
    }

}

