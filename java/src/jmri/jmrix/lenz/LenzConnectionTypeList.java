package jmri.jmrix.lenz;

import jmri.jmrix.ConnectionTypeList;
import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid lenz XpressNet Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 *
 */
@ServiceProvider(service = ConnectionTypeList.class)
@API(status = EXPERIMENTAL)
public class LenzConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String ATLAS = "Atlas";
    public static final String LENZ = "Lenz";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.lenz.li100.ConnectionConfig",
            "jmri.jmrix.lenz.li100f.ConnectionConfig",
            "jmri.jmrix.lenz.li101.ConnectionConfig",
            "jmri.jmrix.lenz.liusb.ConnectionConfig",
            "jmri.jmrix.lenz.lzv200.ConnectionConfig",
            "jmri.jmrix.lenz.ztc640.ConnectionConfig",
            "jmri.jmrix.lenz.xntcp.ConnectionConfig",
            "jmri.jmrix.xpa.serialdriver.ConnectionConfig",
            "jmri.jmrix.lenz.xnetsimulator.ConnectionConfig",
            "jmri.jmrix.lenz.liusbserver.ConnectionConfig",
            "jmri.jmrix.lenz.liusbethernet.ConnectionConfig", // experimental
            "jmri.jmrix.lenz.XNetStreamConnectionConfig",
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{ATLAS, LENZ};
    }

}
