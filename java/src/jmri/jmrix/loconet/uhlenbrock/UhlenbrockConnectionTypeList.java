// FleischmannConnectionTypeList.java.java

package jmri.jmrix.loconet.Uhlenbrock;


/**
 * Returns a list of valid Fleischmann Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision: 17977 $
 *
 */
public class UhlenbrockConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

       public String[] getAvailableProtocolClasses() { 
        return new String[] {
            "jmri.jmrix.loconet.uhlenbrock.ConnectionConfig"
        };
    }
}

