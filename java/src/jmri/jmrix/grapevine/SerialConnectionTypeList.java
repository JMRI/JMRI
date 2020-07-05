package jmri.jmrix.grapevine;

import jmri.jmrix.ConnectionTypeList;
import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Return a list of valid ProTrak (Grapevine) Connection Types.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
@API(status = EXPERIMENTAL)
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
