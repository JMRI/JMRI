// LenzConnectionTypeList.java
package jmri.jmrix.dccpp;

/**
 * Returns a list of valid lenz XpressNet Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 *
 * Based on jmri.jmrix.lenz.LenzConnectionTypeList
 */
public class DCCppConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String DCCPP = "DCC++";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.dccpp.serial.ConnectionConfig",
            "jmri.jmrix.dccpp.simulator.ConnectionConfig",
            "jmri.jmrix.dccpp.network.ConnectionConfig",
            "jmri.jmrix.dccpp.dccppovertcp.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{DCCPP};
    }

}
