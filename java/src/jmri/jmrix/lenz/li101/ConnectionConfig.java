// ConnectionConfig.java
package jmri.jmrix.lenz.li101;

/**
 * Handle configuring an XPressNet layout connection via a Lenz LI101 adapter.
 * <P>
 * This uses the {@link LI101Adapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @version	$Revision$
 *
 * @see LI101Adapter
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
        return "Lenz LI101F";
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new LI101Adapter();
        }
    }
}
