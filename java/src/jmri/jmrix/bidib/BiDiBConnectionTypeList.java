package jmri.jmrix.bidib;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid BiDiB Connection Types
 * <p>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2015
 * @author Eckart Meyer Copyright (C) 2019-2023
 *
 * Based on DCCppConnectionTypeList
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class BiDiBConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String BIDIB = "BiDiB";

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.bidib.serialdriver.ConnectionConfig",
            "jmri.jmrix.bidib.simulator.ConnectionConfig",
            "jmri.jmrix.bidib.bidibovertcp.ConnectionConfig",
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getManufacturers() {
        return new String[]{BIDIB, BIDIB};
    }

}
