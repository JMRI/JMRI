// ConnectionConfig.java

package jmri.jmrix.lenz.liusb;


/**
 * Handle configuring an XPressNet layout connection
 * via a Lenz LIUSBadapter.
 * <P>
 * This uses the {@link LIUSBAdapter} class to do the actual
 * connection.
 *
 * @author      Paul Bender  Copyright (C) 2005
 * @version	$Revision: 1.0 $
 *
 * @see LIUSBAdapter
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        super(p);
    }
    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() { return "Lenz LIUSB"; }

    protected void setInstance() { adapter = LIUSBAdapter.instance(); }
}

