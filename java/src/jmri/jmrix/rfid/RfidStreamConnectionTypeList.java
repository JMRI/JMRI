package jmri.jmrix.rfid;

import jmri.jmrix.ConnectionTypeList;
import jmri.jmrix.StreamConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid RFID Stream Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010, 2015
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class RfidStreamConnectionTypeList implements jmri.jmrix.StreamConnectionTypeList {

    public static final String RFID = "RFID";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.rfid.RfidStreamConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{RFID};
    }

}
