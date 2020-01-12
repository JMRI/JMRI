package jmri.jmrix.zimo;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid Zimo Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class Mx1ConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String ZIMO = "Zimo";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.zimo.mx1.ConnectionConfig",
            "jmri.jmrix.zimo.mxulf.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{ZIMO};
    }

}
