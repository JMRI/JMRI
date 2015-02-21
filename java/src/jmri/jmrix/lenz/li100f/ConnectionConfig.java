// ConnectionConfig.java
package jmri.jmrix.lenz.li100f;

/**
 * Handle configuring an XPressNet layout connection via a Lenz LI100 or LI100F
 * adapter.
 * <P>
 * This uses the {@link LI100Adapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @version	$Revision$
 *
 * @see LI100Adapter
 */
public class ConnectionConfig extends jmri.jmrix.lenz.AbstractXNetSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() {
        return "Lenz LI100F";
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new LI100Adapter();
        }
    }
}
