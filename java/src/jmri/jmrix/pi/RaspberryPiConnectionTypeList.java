package jmri.jmrix.pi;

import org.openide.util.lookup.ServiceProvider;
import jmri.jmrix.ConnectionTypeList;

/**
 * Returns a list of valid Raspberry Pi Connection Types
 * <P>
 * @author Paul Bender Copyright (C) 2015
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class RaspberryPiConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String PI = "Raspberry Pi Foundation";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.pi.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{PI};
    }

}
