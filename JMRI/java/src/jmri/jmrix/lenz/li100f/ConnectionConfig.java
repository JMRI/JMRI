package jmri.jmrix.lenz.li100f;

/**
 * Handle configuring an XpressNet layout connection via a Lenz (LI100 or) LI100F
 * adapter.
 * <P>
 * This uses the {@link LI100fAdapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
  *
 * @see LI100fAdapter
 */
public class ConnectionConfig extends jmri.jmrix.lenz.AbstractXNetSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process.
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "Lenz LI100F"; // NOI18N
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new LI100fAdapter(); // version in jmri.jmrix.li100f
        }
    }

}
