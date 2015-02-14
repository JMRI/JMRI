// SerialConnectionTypeList.java

package jmri.jmrix.oaktree;


/**
 * Returns a list of valid Oaktree Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class SerialConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
        return new String[] {
              "jmri.jmrix.oaktree.serialdriver.ConnectionConfig"
        };
    }

}

