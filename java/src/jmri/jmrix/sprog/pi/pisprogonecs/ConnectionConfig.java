// ConnectionConfig.java
package jmri.jmrix.sprog.pi.pisprogonecs;

import jmri.util.SystemType;

/**
 * Definition of objects to handle configuring a layout connection via an SPROG
 * SerialDriverAdapter object.
 *
 * @author Andrew Crosland Copyright (C) 2016
 * @version	$Revision$
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

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
        return "Pi-SPROG One Command Station";
    }

    public String getManufacturer() {
        return adapter.getManufacturer();
    }

    public void setManufacturer(String manu) {
        adapter.setManufacturer(manu);
    }
    /*@Override
     protected Vector<String> getPortFriendlyNames() {
     System.out.println("Port names called");
     Vector<String> portNameVector = new Vector<String>();
     if(System.getProperty("os.name").toLowerCase().contains("windows")){
     portNameVector.add("SPROG");
     }
     System.out.println("Port names called" + portNameVector);
     return portNameVector;
     }*/

    @Override
    protected String[] getPortFriendlyNames() {
        if (SystemType.isWindows()) {
            return new String[]{"SPROG"};
        }
        return new String[]{};
    }

    protected void setInstance() {
        adapter = PiSprogOneCSSerialDriverAdapter.instance();
    }
}

/* @(#)ConnectionConfig.java */
