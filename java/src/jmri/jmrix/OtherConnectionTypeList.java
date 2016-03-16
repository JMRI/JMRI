// ConnectionTypeList.java
package jmri.jmrix;

/**
 * Returns a list of valid Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class OtherConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.direct.serial.ConnectionConfig"
        };
    }

}
