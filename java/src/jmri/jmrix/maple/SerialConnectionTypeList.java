package jmri.jmrix.maple;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid Maple Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class SerialConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String MAPLE = "Maple Systems";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.maple.serialdriver.ConnectionConfig",
            "jmri.jmrix.maple.simulator.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{MAPLE};
    }

}
