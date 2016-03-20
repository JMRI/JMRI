// OlcbConnectionTypeList.java
package jmri.jmrix.openlcb;

/**
 * Returns a list of valid Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class OlcbConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String OPENLCB = "OpenLCB";

    @Override
    public String[] getAvailableProtocolClasses() {

        // set the connection types to have OPENLCB at the front
        jmri.jmrix.can.ConfigurationManager.setOpenLCB();

        // return the list of connector values for a CAN/MERG connection
        return new String[]{
            "jmri.jmrix.can.adapters.gridconnect.can2usbino.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.net.ConnectionConfig",
            "jmri.jmrix.can.adapters.lawicell.canusb.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.loopback.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{OPENLCB};
    }

}
