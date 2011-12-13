// RfidConnectionTypeList.java

package jmri.jmrix.rfid;


/**
 * Returns a list of valid RFID Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @author      Matthew Harris     Copyright (C) 2011
 * @version	$Revision$
 * @since       2.11.4
 */
public class RfidConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
        return new String[] {
              "jmri.jmrix.rfid.serialdriver.ConnectionConfig"
        };
    }

}

