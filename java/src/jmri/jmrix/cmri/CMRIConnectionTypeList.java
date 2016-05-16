// CMRIConnectionTypeList.java

package jmri.jmrix.cmri;


/**
 * Returns a list of valid CMRI Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision: 17977 $
 *
 */
public class CMRIConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
        return new String[] {
              "jmri.jmrix.cmri.serial.serialdriver.ConnectionConfig",
              "jmri.jmrix.cmri.serial.sim.ConnectionConfig"
        };
    }

}

