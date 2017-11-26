package jmri.jmrix.cmri;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid C/MRI Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class CMRIConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String CMRI = "C/MRI";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.cmri.serial.serialdriver.ConnectionConfig",
            "jmri.jmrix.cmri.serial.networkdriver.ConnectionConfig",
            "jmri.jmrix.cmri.serial.sim.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{CMRI};
    }

}
