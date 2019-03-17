package jmri.jmrix.lenz;

import jmri.jmrix.ConnectionTypeList;
import jmri.jmrix.StreamConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid lenz XpressNet Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class LenzStreamConnectionTypeList implements jmri.jmrix.StreamConnectionTypeList {

    public static final String ATLAS = "Atlas";
    public static final String LENZ = "Lenz";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.lenz.XNetStreamConnectionConfig",
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{ATLAS, LENZ};
    }

}
