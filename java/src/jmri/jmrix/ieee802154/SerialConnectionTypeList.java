package jmri.jmrix.ieee802154;

import jmri.jmrix.ConnectionTypeList;
import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid IEEE 802.15.4 Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2013
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
@API(status = EXPERIMENTAL)
public class SerialConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String IEEE802154 = "IEEE 802.15.4";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.ieee802154.serialdriver.ConnectionConfig",
            "jmri.jmrix.ieee802154.xbee.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{IEEE802154};
    }

}
