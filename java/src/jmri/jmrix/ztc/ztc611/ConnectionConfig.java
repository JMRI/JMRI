package jmri.jmrix.ztc.ztc611;

/**
 * Handle configuring an XpressNet layout connection via a ZTC Controls ZTC611
 * command station.
 * <p>
 * This uses the {@link ZTC611Adapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 *
 * @see ZTC611Adapter
 */
public class ConnectionConfig extends jmri.jmrix.lenz.AbstractXNetSerialConnectionConfig {

    /**
     * Create a connection configuration with an existing adapter.
     *
     * @param p the associated adapter
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Create a connection configuration without an existing adapter.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "ZTC Controls ZTC611";
    }

    String manufacturerName = "ZTC";

    @Override
    public String getManufacturer() {
        return manufacturerName;
    }

    @Override
    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new ZTC611Adapter();
        }
    }

}
