// ConnectionConfig.java

package jmri.jmrix.wangrow.serialdriver;

import jmri.jmrix.nce.serialdriver.SerialDriverAdapter;

/**
 * Definition of objects to handle configuring a layout connection
 * via an NCE SerialDriverAdapter object.
 * <P>
 * Note that this is not referencing Wangrow code, but is
 * using NCE code in place.  This should eventually be changed!
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
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

    public String name() { return "Wangrow"; }

    protected void setInstance() { adapter = SerialDriverAdapter.instance(); }
}

