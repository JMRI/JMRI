// SRCPSystemConnectionMemo.java
package jmri.jmrix.srcp;

import java.util.ResourceBundle;
import jmri.InstanceManager;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @author	Paul Bender Copyright (C) 2015-2016
 */
public class SRCPSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public SRCPSystemConnectionMemo(String prefix, String name, SRCPTrafficController et) {
        super(prefix, name);
        this.et = et;
        this.et.setSystemConnectionMemo(this);
        register();
        /*InstanceManager.store(cf = new jmri.jmrix.srcp.swing.ComponentFactory(this), 
         jmri.jmrix.swing.ComponentFactory.class);*/
    }

    public SRCPSystemConnectionMemo(SRCPTrafficController et) {
        super("D", "SRCP");
        this.et = et;
        this.et.setSystemConnectionMemo(this);
        register();
        /*InstanceManager.store(cf = new jmri.jmrix.srcp.swing.ComponentFactory(this), 
         jmri.jmrix.swing.ComponentFactory.class);*/
    }

    public SRCPSystemConnectionMemo() {
        super("D", "SRCP");
        register(); // registers general type
        InstanceManager.store(this, SRCPSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        /*InstanceManager.store(cf = new jmri.jmrix.srcp.swing.ComponentFactory(this), 
         jmri.jmrix.swing.ComponentFactory.class);*/
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public SRCPTrafficController getTrafficController() {
        return et;
    }

    public void setTrafficController(SRCPTrafficController et) {
        this.et = et;
        this.et.setSystemConnectionMemo(this);
    }
    private SRCPTrafficController et;

    /**
     * Configure the common managers for Internal connections. This puts the
     * common manager config in one place. This method is static so that it can
     * be referenced from classes that don't inherit, including
     * hexfile.HexFileFrame and locormi.LnMessageClient
     */
    public void configureManagers() {

    }

    /**
     * Configure the programming manager and "command station" objects
     */
    public void configureCommandStation() {
        // start the connection
        et.sendSRCPMessage(new SRCPMessage("SET PROTOCOL SRCP 0.8.3\n"), null);
        et.sendSRCPMessage(new SRCPMessage("SET CONNECTIONMODE SRCP COMMAND\n"), null);
        et.sendSRCPMessage(new SRCPMessage("GO\n"), null);
        // for now, limit to 10 busses.
        for (int i = 1; i < 11; i++) {
            et.sendSRCPMessage(new SRCPMessage("GET " + i + " DESCRIPTION\n"), null);
        }
    }

    // keep track of the current mode.
    private int mode = SRCPTrafficController.HANDSHAKEMODE;

    public void setMode(int m) {
        mode = m;
    }

    public int getMode() {
        return mode;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        return null; // nothing, by default
    }

    /**
     * Tells which managers this provides by class
     */
    @Override
    public boolean provides(Class<?> type) {
        return false; // nothing, by default
    }

    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.srcp.SrcpActionListBundle");
    }

    public void dispose() {
        et = null;
        InstanceManager.deregister(this, SRCPSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    // private list of busMemos, so the parser visitor can pass information
    // to the bus representation.
    private java.util.ArrayList<SRCPBusConnectionMemo> busMemos = null;

    public SRCPBusConnectionMemo getMemo(int i) {
        if (busMemos == null) {
            busMemos = new java.util.ArrayList<SRCPBusConnectionMemo>();
            // there is always a bus 0, so add it now.
            busMemos.add(0, new SRCPBusConnectionMemo(getTrafficController(), getSystemPrefix(), 0));
        }
        try {
            return busMemos.get(i);
        } catch (java.lang.IndexOutOfBoundsException ie) {
            // this memo must not exist in the list, add it and return it.
            busMemos.add(i, new SRCPBusConnectionMemo(getTrafficController(), getSystemPrefix(), i));
            return busMemos.get(i);
        }
    }

}


/* @(#)InternalSystemConnectionMemo.java */
