package jmri.jmrix.can;

/**
 * Return a list of valid lenz XpressNet Connection Types.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
  *
 */
public class CanConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    @Override
    public String[] getAvailableProtocolClasses() {

        // set the connection types to have MERG at the front
        jmri.jmrix.can.ConfigurationManager.setMERG();

        // return the list of connector values for a CAN/MERG connection
        return new String[]{
            "jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.net.ConnectionConfig",
            "jmri.jmrix.can.adapters.lawicell.canusb.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.loopback.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        // Not in DCCManufacturerList, and not in META-INF, so returning class name.
        return new String[]{CanConnectionTypeList.class.getCanonicalName()};
    }

}
