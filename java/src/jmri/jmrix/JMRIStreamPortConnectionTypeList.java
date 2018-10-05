package jmri.jmrix;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid StreamPort Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2014
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class JMRIStreamPortConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String JMRISTREAM = "JMRI (Streams)";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.lenz.XNetStreamConnectionConfig",
            "jmri.jmrix.sprog.SprogCSStreamConnectionConfig",
            "jmri.jmrix.rfid.RfidStreamConnectionConfig",
            "jmri.jmrix.loconet.streamport.LnStreamConnectionConfig",
            "jmri.jmrix.dccpp.DCCppStreamConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{JMRISTREAM};
    }

}
