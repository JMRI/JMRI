package jmri.jmrix.direct;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Return a list of valid Direct Connection Types.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class DirectConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String DIRECT = "Others"; // NOI18N

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.direct.serial.ConnectionConfig",
            "jmri.jmrix.direct.simulator.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{DIRECT};
    }

}
