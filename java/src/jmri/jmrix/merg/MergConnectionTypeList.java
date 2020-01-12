package jmri.jmrix.merg;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Return a list of valid connection types for MERG.
 *
 * @author Matthew Harris Copyright (c) 2011
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class MergConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String MERG = "MERG";

    @Override
    public String[] getAvailableProtocolClasses() {
        // set the connection types to have MERG at the front
        jmri.jmrix.can.ConfigurationManager.setMERG();

        return new String[]{
            "jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.net.MergConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.net.ConnectionConfig",
            "jmri.jmrix.can.adapters.lawicell.canusb.serialdriver.ConnectionConfig",
            "jmri.jmrix.can.adapters.loopback.ConnectionConfig",
            "jmri.jmrix.rfid.serialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{MERG};
    }

}
