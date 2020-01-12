package jmri.jmrix.sprog;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid Sprog Connection Types.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class SprogConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String SPROG = "SPROG DCC";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.sprog.sprog.ConnectionConfig",
            "jmri.jmrix.sprog.sprogCS.ConnectionConfig",
            "jmri.jmrix.sprog.sprognano.ConnectionConfig",
            "jmri.jmrix.sprog.pi.pisprogone.ConnectionConfig",
            "jmri.jmrix.sprog.pi.pisprogonecs.ConnectionConfig",
            "jmri.jmrix.sprog.pi.pisprognano.ConnectionConfig",
            "jmri.jmrix.sprog.simulator.ConnectionConfig",
            "jmri.jmrix.sprog.SprogCSStreamConnectionConfig",
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{SPROG};
    }

}
