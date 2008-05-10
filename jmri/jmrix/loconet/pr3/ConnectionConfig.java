// ConnectionConfig.java

package jmri.jmrix.loconet.pr3;


/**
 * Definition of objects to handle configuring a PR3 layout connection
 * via a PR2Adapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2008
 * @version	$Revision: 1.1 $
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

    public String name() { return "LocoNet PR3"; }

    protected void setInstance() { adapter = jmri.jmrix.loconet.pr3.PR3Adapter.instance(); }
}

