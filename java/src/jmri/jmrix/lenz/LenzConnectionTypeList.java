// LenzConnectionTypeList.java
package jmri.jmrix.lenz;

/**
 * Returns a list of valid lenz XpressNet Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class LenzConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String ATLAS = "Atlas";
    public static final String LENZ = "Lenz";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.lenz.li100.ConnectionConfig",
            "jmri.jmrix.lenz.li100f.ConnectionConfig",
            "jmri.jmrix.lenz.li101.ConnectionConfig",
            "jmri.jmrix.lenz.liusb.ConnectionConfig",
            "jmri.jmrix.lenz.xntcp.ConnectionConfig",
            "jmri.jmrix.xpa.serialdriver.ConnectionConfig",
            "jmri.jmrix.lenz.xnetsimulator.ConnectionConfig",
            "jmri.jmrix.lenz.liusbserver.ConnectionConfig",
            "jmri.jmrix.lenz.liusbethernet.ConnectionConfig" // experimental
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{ATLAS, LENZ};
    }

}
