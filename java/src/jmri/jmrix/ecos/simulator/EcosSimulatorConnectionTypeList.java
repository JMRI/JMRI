package jmri.jmrix.ecos.simulator;

import jmri.jmrix.ConnectionTypeList;
import jmri.jmrix.ecos.EcosConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Maintains a list of valid ESU ECoS Connection Types. Note that
 * {@link EcosConnectionTypeList} also provides a list of valid ESU ECoS
 * Connection Types. Using two different lists of valid
 * {@link EcosConnectionTypeList#ESU} Connection Types demonstrates the
 * capability to add new Connection Types for an existing manufacturer using
 * third-party JARs.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class EcosSimulatorConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.ecos.simulator.EcosSimulatorConnectionConfig"};
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{EcosConnectionTypeList.ESU};
    }

}
