package jmri.jmrix.loconet;

import jmri.jmrix.ConnectionTypeList;
import jmri.jmrix.StreamConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Get a list of valid LocoNet Stream Connection Types.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class LnStreamConnectionTypeList implements jmri.jmrix.StreamConnectionTypeList {

    public static final String DIGITRAX = "Digitrax"; // NOI18N

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.loconet.streamport.LnStreamConnectionConfig" // NOI18N
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{DIGITRAX};
    }

}
