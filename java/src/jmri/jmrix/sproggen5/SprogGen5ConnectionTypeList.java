package jmri.jmrix.sproggen5;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Return a list of valid connection types for SPROG Generation 5.
 *
 * @author Matthew Harris Copyright (c) 2011
 * @author Andrew Crosland 2019
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class SprogGen5ConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String SPROG = "SPROG DCC Generation 5";

    @Override
    public String[] getAvailableProtocolClasses() {
        // set the connection types to have MERG at the front
        jmri.jmrix.can.ConfigurationManager.setSPROG();

        return new String[]{
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.CanisbConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.Sprog3PlusConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3PlusConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3v2ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3ConnectionConfig",
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{SPROG};
    }

}
