// LnConnectionTypeList.java
package jmri.jmrix.loconet;

/**
 * Returns a list of valid lenz XpressNet Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class LnConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String DIGITRAX = "Digitrax";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.loconet.locobufferusb.ConnectionConfig",
            "jmri.jmrix.loconet.pr2.ConnectionConfig",
            "jmri.jmrix.loconet.pr3.ConnectionConfig",
            "jmri.jmrix.loconet.hexfile.ConnectionConfig",
            "jmri.jmrix.loconet.locormi.ConnectionConfig",
            "jmri.jmrix.loconet.loconetovertcp.ConnectionConfig",
            "jmri.jmrix.loconet.locobufferii.ConnectionConfig",
            "jmri.jmrix.loconet.locobuffer.ConnectionConfig",
            "jmri.jmrix.loconet.ms100.ConnectionConfig",
            "jmri.jmrix.loconet.bluetooth.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{DIGITRAX};
    }

}
