package jmri.jmrix.ecos;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Maintains a list of valid ESU ECoS Connection Types. Note that
 * {@link jmri.jmrix.ecos.simulator.EcosSimulatorConnectionTypeList} also
 * provides a list of of valid ESU ECoS Connection Types. Using two different
 * lists of valid {@link #ESU} Connection Types demonstrates the capability to
 * add new Connection Types for an existing manufacturer using third-party JARs.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class EcosConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String ESU = "ESU";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.ecos.networkdriver.ConnectionConfig"};
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{ESU};
    }

}
