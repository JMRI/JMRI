package jmri.jmrix.wangrow;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid lenz XpressNet Connection Types.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class WangrowConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String WANGROW = "Wangrow";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.wangrow.serialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{WANGROW};
    }

}
