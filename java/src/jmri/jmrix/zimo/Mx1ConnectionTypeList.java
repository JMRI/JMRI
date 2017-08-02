package jmri.jmrix.zimo;

import org.openide.util.lookup.ServiceProvider;
import jmri.jmrix.ConnectionTypeList;

/**
 * Returns a list of valid Zimo Connection Types
 * <P>
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
