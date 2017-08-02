package jmri.jmrix.bachrus;

import org.openide.util.lookup.ServiceProvider;
import jmri.jmrix.ConnectionTypeList;

/**
 * Returns a list of valid BAchrus Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Andrew Crosland Copyright (C) 2010
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class SpeedoConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String BACHRUS = "Bachrus";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.bachrus.serialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{BACHRUS};
    }

}
