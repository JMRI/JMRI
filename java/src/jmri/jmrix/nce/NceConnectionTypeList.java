package jmri.jmrix.nce;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Return a list of valid NCE Connection Types.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class NceConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String NCE = "NCE";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.nce.serialdriver.ConnectionConfig",
            "jmri.jmrix.nce.networkdriver.ConnectionConfig",
            "jmri.jmrix.nce.usbdriver.ConnectionConfig",
            "jmri.jmrix.nce.simulator.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{NCE};
    }

}
