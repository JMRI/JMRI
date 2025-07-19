package jmri.jmrix.nce.ph5driver;

/**
 * Definition of objects to handle configuring a layout connection via an NCE
 * Ph5DriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author kcameron Copyright (C) 2010, 2023
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    public final static String NAME = Bundle.getMessage("TypePh5USB");

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p SerialPortAdapter for existing adapter
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no existing adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new Ph5DriverAdapter();
        }
    }

}
