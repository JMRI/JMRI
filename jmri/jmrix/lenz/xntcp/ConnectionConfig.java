// ConnectionConfig.java

package jmri.jmrix.lenz.xntcp;


/**
 * Handle configuring an XPressNet layout connection
 * via a Lenz XnTcp or LI101 adapter.
 * <P>
 * This uses the {@link XnTcpAdapter} class to do the actual
 * connection.
 *
 * @author	Giorgio Terdina Copyright (C) 2008, based on LI100 Action by Bob Jacobsen, Copyright (C) 2003
 * @version	$Revision: 1.1 $
 *
 * @see XnTcpAdapter
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

    public String name() { return "XnTcp"; }

    protected void setInstance() { adapter = XnTcpAdapter.instance(); }
}

