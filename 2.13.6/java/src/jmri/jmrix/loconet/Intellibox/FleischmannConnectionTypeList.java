// FleischmannConnectionTypeList.java.java

package jmri.jmrix.loconet.Intellibox;


/**
 * Returns a list of valid Fleischmann Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class FleischmannConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

       public String[] getAvailableProtocolClasses() { 
        return new String[] {
            "jmri.jmrix.loconet.Intellibox.ConnectionConfig"
        };
    }
}

