// SerialConnectionTypeList.java

package jmri.jmrix.maple;


/**
 * Returns a list of valid Maple Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class SerialConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
        return new String[] {
              "jmri.jmrix.maple.serialdriver.ConnectionConfig"
        };
    }

}

