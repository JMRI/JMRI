// ConnectionTypeList.java

package jmri.jmrix.sprog;


/**
 * Returns a list of valid nce Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision: 1.1 $
 *
 */
public class ConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
        return new String[] {
              "jmri.jmrix.sprog.sprog.ConnectionConfig",
              "jmri.jmrix.sprog.sprogCS.ConnectionConfig"
        };
    }

}

