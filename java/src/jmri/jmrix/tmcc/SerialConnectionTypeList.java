// SerialConnectionTypeList.java
package jmri.jmrix.tmcc;

/**
 * Returns a list of valid lenz XpressNet Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class SerialConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String LIONEL = "Lionel TMCC";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.tmcc.serialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{LIONEL};
    }

}
