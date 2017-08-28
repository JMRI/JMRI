package jmri.jmrix.oaktree;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid Oaktree Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class SerialConnectionTypeList implements jmri.jmrix.ConnectionTypeList {
    public static final String OAK = "Oak Tree Systems";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.oaktree.serialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{OAK};
    }

}
