package jmri.jmrix.loconet;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid LocoNet Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class LnConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String DIGITRAX = "Digitrax"; // NOI18N

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.loconet.locobufferusb.ConnectionConfig", // NOI18N
            "jmri.jmrix.loconet.pr2.ConnectionConfig", // NOI18N
            "jmri.jmrix.loconet.pr3.ConnectionConfig", // NOI18N
            "jmri.jmrix.loconet.hexfile.ConnectionConfig", // NOI18N
            "jmri.jmrix.loconet.locormi.ConnectionConfig", // NOI18N
            "jmri.jmrix.loconet.loconetovertcp.ConnectionConfig", // NOI18N
            "jmri.jmrix.loconet.locobufferii.ConnectionConfig", // NOI18N
            "jmri.jmrix.loconet.locobuffer.ConnectionConfig", // NOI18N
            "jmri.jmrix.loconet.ms100.ConnectionConfig", // NOI18N
            "jmri.jmrix.loconet.bluetooth.ConnectionConfig" // NOI18N
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{DIGITRAX};
    }

}
