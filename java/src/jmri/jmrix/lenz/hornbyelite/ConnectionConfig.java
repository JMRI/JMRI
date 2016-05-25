// ConnectionConfig.java
package jmri.jmrix.lenz.hornbyelite;

/**
 * Handle configuring an XPressNet layout connection via the built in USB port
 * on the Hornby Elite.
 * <P>
 * This uses the {@link EliteAdapter} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2008
 * @version	$Revision$
 *
 * @see EliteAdapter
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
        return "Hornby Elite USB port";
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new EliteAdapter();
        }
    }
}
