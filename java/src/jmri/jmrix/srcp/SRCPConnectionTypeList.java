// SRCPConnectionTypeList.java
package jmri.jmrix.srcp;

/**
 * Returns a list of valid SRCP Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class SRCPConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String SRCP = "SRCP";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.srcp.networkdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{SRCP};
    }

}
