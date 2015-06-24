// RocoConnectionTypeList.java
package jmri.jmrix.roco;

/**
 * Returns a list of valid Roco Connection Types Note that most Roco options are
 * Lenz options (RocoNet is XPressNet).
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class RocoConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

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
            "jmri.jmrix.lenz.liusbethernet.ConnectionConfig", // experimental
            "jmri.jmrix.roco.z21.ConnectionConfig", // experimental
            "jmri.jmrix.roco.z21.simulator.ConnectionConfig" // experimental
        };
    }

}
