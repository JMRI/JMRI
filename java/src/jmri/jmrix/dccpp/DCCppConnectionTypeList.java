package jmri.jmrix.dccpp;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid lenz DCC++ Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2015
  *
 * Based on jmri.jmrix.lenz.LenzConnectionTypeList
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class DCCppConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String DCCPP = "DCC++";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.dccpp.serial.ConnectionConfig",
            "jmri.jmrix.dccpp.simulator.ConnectionConfig",
            "jmri.jmrix.dccpp.network.ConnectionConfig",
            "jmri.jmrix.dccpp.dccppovertcp.ConnectionConfig",
            "jmri.jmrix.dccpp.DCCppStreamConnectionConfig",
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{DCCPP};
    }

}
