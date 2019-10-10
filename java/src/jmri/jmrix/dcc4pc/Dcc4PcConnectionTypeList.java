package jmri.jmrix.dcc4pc;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid Dcc4Pc Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class Dcc4PcConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String DCC4PC = "DCC4PC";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.dcc4pc.serialdriver.ConnectionConfig",};
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{DCC4PC};
    }

}
