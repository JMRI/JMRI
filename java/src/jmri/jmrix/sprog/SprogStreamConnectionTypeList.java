package jmri.jmrix.sprog;

import jmri.jmrix.ConnectionTypeList;
import jmri.jmrix.StreamConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid Sprog Stream Connection Types.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class SprogStreamConnectionTypeList implements jmri.jmrix.StreamConnectionTypeList {

    public static final String SPROG = "SPROG DCC";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.sprog.SprogCSStreamConnectionConfig",
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{SPROG};
    }

}
