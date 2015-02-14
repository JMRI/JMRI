// ConnectionConfig.java

package jmri.jmrix.lenz.liusb;

import jmri.util.SystemType;


/**
 * Handle configuring an XPressNet layout connection
 * via a Lenz LIUSBadapter.
 * <P>
 * This uses the {@link LIUSBAdapter} class to do the actual
 * connection.
 *
 * @author      Paul Bender  Copyright (C) 2005
 * @version	$Revision$
 *
 * @see LIUSBAdapter
 */
public class ConnectionConfig  extends jmri.jmrix.lenz.AbstractXNetSerialConnectionConfig {

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
    
    @Override
    protected String[] getPortFriendlyNames() {
        if(SystemType.isWindows()){
            return new String[]{"LI-USB Serial Port","LI-USB"};
        }
        return new String[]{};
    }

    protected void setInstance() { if(adapter==null) adapter = new LIUSBAdapter(); }
}

