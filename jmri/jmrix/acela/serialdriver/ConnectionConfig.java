// ConnectionConfig.java

package jmri.jmrix.acela.serialdriver;


/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via an NCE SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.1 $
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008
 *              Based on Mrc example, modified to establish Acela support. 
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

    public String name() { return "Acela"; }

    protected void setInstance() { adapter = SerialDriverAdapter.instance(); }
}

/* @(#)ConnectionConfig.java */