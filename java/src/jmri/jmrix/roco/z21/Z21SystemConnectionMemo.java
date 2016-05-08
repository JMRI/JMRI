package jmri.jmrix.roco.z21;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.ProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Bob Jacobsen Copyright (C) 2010 copied from NCE into PowerLine for
 * multiple connections by
 * @author	Ken Cameron Copyright (C) 2011 copied from PowerLine into z21 by
 * @author	Paul Bender Copyright (C) 2013
 */
public class Z21SystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    private Z21XPressNetTunnel _xnettunnel = null;

    public Z21SystemConnectionMemo() {
        this("Z", "Z21");
    }

    public Z21SystemConnectionMemo(String prefix, String userName) {
        super(prefix, userName);
        register(); // registers general type
        InstanceManager.store(this, Z21SystemConnectionMemo.class); // also register as specific type
        init();
    }

    /*
     * Override the init function for any subtype specific 
     * registration into init.  init is called by the generic contstructor.
     */
    protected void init() {
        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.roco.z21.swing.Z21ComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

    }

    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    /**
     * Traffic Controller for this instance.
     */
    public void setTrafficController(Z21TrafficController newtc) {
        _tc = newtc;
    }

    public Z21TrafficController getTrafficController() {
        return _tc;
    }
    private Z21TrafficController _tc = null;

    public ProgrammerManager getProgrammerManager() {
        if (_xnettunnel!=null) {
            // deligate to the XPressnet tunnel.
            return _xnettunnel.getStreamPortController().getSystemConnectionMemo().getProgrammerManager();        
        }
        return null;
    }

    public void setProgrammerManager(ProgrammerManager p) {
    }

    /**
     * Tells which managers this provides by class
     */
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (_xnettunnel!=null) {
            // deligate to the XPressnet tunnel.
            return _xnettunnel.getStreamPortController().getSystemConnectionMemo().provides(type);        
        }
        return false; // nothing, by default
    }

    /**
     * Provide manager by class
     */
    @SuppressWarnings("unchecked")  // xpressnet code managed type for cast
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (_xnettunnel!=null) {
            // delegate to the XPressnet tunnel.
            return _xnettunnel.getStreamPortController().getSystemConnectionMemo().get(T);        
        }
        return null; // nothing, by default
    }

    /**
     * Configure the common managers for z21 connections. This puts the common
     * manager config in one place.
     */
    public void configureManagers() {
        log.debug("Called Configure Managers");

        // set the broadcast flags so we get messages we may want to hear
        _tc.sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(
                           0x00010001),null);  //right now,just the XPressNet flags.

        // add an XPressNet Tunnel.
        _xnettunnel = new Z21XPressNetTunnel(this);
 
   }

    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.roco.z21.z21ActionListBundle");
    }

    public void dispose() {
        InstanceManager.deregister(this, Z21SystemConnectionMemo.class);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(Z21SystemConnectionMemo.class.getName());

}
