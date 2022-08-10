package jmri.jmrix.pi.simulator;

import jmri.jmrix.pi.*;

/**
 * Handle configuring a Raspberry Pi layout connection.
 * <p>
 * This uses the {@link RaspberryPiAdapter} class to do the actual connection.
 *
 * @author Paul Bender       Copyright (C) 2015
 * @author Daniel Bergqvist  Copyright (C) 2022
 *
 * @see RaspberryPiAdapter
 */
public class RaspberryPiSimulatorConnectionConfig extends jmri.jmrix.AbstractConnectionConfig {

    private boolean disabled = false;
    private RaspberryPiAdapter adapter = null;

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p the pre-existing adapter
     */
    public RaspberryPiSimulatorConnectionConfig(RaspberryPiAdapter p) {
        super();
        adapter = p;
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public RaspberryPiSimulatorConnectionConfig() {
        super();
        adapter = new RaspberryPiAdapter(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkInitDone() {
    }

    @Override
    public void updateAdapter() {
    }

    @Override
    protected void showAdvancedItems() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(final javax.swing.JPanel details) {
        _details = details;
        setInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new RaspberryPiAdapter(true);
        }
    }

    @Override
    public RaspberryPiAdapter getAdapter() {
        return adapter;
    }

    @Override
    public String getInfo() {
        return Bundle.getMessage("none");
    }

    String manuf = RaspberryPiConnectionTypeList.PI;

    @Override
    public String getManufacturer() {
        return manuf;
    }

    @Override
    public void setManufacturer(String manufacturer) {
        manuf = manufacturer;
    }

    @Override
    public String name() {
        return getConnectionName();
    }

    @Override
    public String getConnectionName() {
        return "Raspberry Pi Simulator"; // NOI18N
    }

    @Override
    public boolean getDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disable) {
        this.disabled = disable;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RaspberryPiSimulatorConnectionConfig.class);

}
