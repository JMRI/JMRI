package jmri.jmrix.ecos.simulator;

import jmri.jmrix.AbstractSimulatorConnectionConfig;
import jmri.jmrix.NetworkPortAdapter;
import jmri.jmrix.ecos.EcosConnectionTypeList;

/**
 * Handle configuring an ECoS layout connection via an EcosSimulator adapter.
 * <p>
 * This uses the {@link EcosSimulatorAdapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
 * @author Mark Underwood Copyright (C) 2015
 * @author Randall Wood Copyright 2017
 */
public class EcosSimulatorConnectionConfig extends AbstractSimulatorConnectionConfig<NetworkPortAdapter> {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p the port adapter supporting this connection
     */
    public EcosSimulatorConnectionConfig(NetworkPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public EcosSimulatorConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "ECoS Simulator";
    }

    String manufacturerName = EcosConnectionTypeList.ESU;

    @Override
    public String getManufacturer() {
        return manufacturerName;
    }

    @Override
    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new EcosSimulatorAdapter();
        }
    }

}
