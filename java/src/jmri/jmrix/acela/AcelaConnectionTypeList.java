package jmri.jmrix.acela;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid CTI Electronics Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class AcelaConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String CTI = "CTI Electronics";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.acela.serialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{CTI};
    }

}
