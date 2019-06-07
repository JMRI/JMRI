package jmri.jmrix.tams;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid ESU Tams Connection Types
 *
 * @author Kevin Dickerson Copyright (C) 2012
 *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class TamsConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String TAMS = "Tams";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.tams.serialdriver.ConnectionConfig",
            "jmri.jmrix.tams.simulator.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{TAMS};
    }

}
