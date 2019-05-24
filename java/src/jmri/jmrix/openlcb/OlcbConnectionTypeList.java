package jmri.jmrix.openlcb;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class OlcbConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String OPENLCB = "OpenLCB";
    public static final String LCC = "LCC";

    @Override
    public String[] getAvailableProtocolClasses() {

        // set the connection types to have OPENLCB at the front
        jmri.jmrix.can.ConfigurationManager.setOpenLCB();

        // return the list of connector values for a CAN/MERG connection
        return new String[]{
            "jmri.jmrix.can.adapters.gridconnect.can2usbino.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.lccbuffer.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.net.ConnectionConfig",
            "jmri.jmrix.can.adapters.lawicell.canusb.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.loopback.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{OPENLCB, LCC};
    }

}
