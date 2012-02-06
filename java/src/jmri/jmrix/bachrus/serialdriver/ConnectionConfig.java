// ConnectionConfig.java

package jmri.jmrix.bachrus.serialdriver;


/**
 * Definition of objects to handle configuring a connection
 * via a Bachrus SerialDriverAdapter object.
 *
 * @author      Bob Jacobsen        Copyright (C) 2001, 2003
 * @author      Andrew Crosland     Copyright (C) 2010
 * @version	$Revision$
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractSerialConnectionConfig {

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

    public String name() { return "Speedo"; }

    @Override
    protected String[] getPortFriendlyNames() {
        if(System.getProperty("os.name").toLowerCase().contains("windows")){
            return new String[]{"Bachrus Speedo","Bachrus"};
        }
        return new String[]{};
    }
    
    protected void setInstance() {         
        if (adapter == null){
            adapter = new SerialDriverAdapter();
        } 
    }
}

