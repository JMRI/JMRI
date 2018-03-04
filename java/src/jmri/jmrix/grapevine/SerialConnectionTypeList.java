package jmri.jmrix.grapevine;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Return a list of valid ProTrak (Grapevine) Connection Types.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class SerialConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String PROTRAK = "Protrak";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
                "jmri.jmrix.grapevine.serialdriver.ConnectionConfig",
                "jmri.jmrix.grapevine.simulator.ConnectionConfig",
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{PROTRAK};
    }

}
