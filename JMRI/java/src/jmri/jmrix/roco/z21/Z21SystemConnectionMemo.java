package jmri.jmrix.roco.z21;

import java.util.ResourceBundle;
import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.jmrix.lenz.XNetProgrammerManager;
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
    private Z21LocoNetTunnel _loconettunnel = null;

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

    /**
     * Reporter Manager for this instance.
     */
    public void setReporterManager(Z21ReporterManager rm){
        _rm = rm;
    }

    public Z21ReporterManager getReporterManager() {
        if(_rm==null){
           setReporterManager(new Z21ReporterManager(this));
        }
        return _rm;
    }

    private Z21ReporterManager _rm = null;

    public XNetProgrammerManager getProgrammerManager() {
        if (_xnettunnel!=null) {
            // delegate to the XPressNet tunnel.
            return _xnettunnel.getStreamPortController().getSystemConnectionMemo().getProgrammerManager();
        }
        return null;
    }

    public void setProgrammerManager(XNetProgrammerManager p) {
    }

    /**
     * Tells which managers this class provides.
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.ReporterManager.class)){
           return true;
        }
        if (type.equals(jmri.MultiMeter.class)){
           return true;
        }
        if (_xnettunnel!=null) {
            // delegate to the XPressNet tunnel.
            return _xnettunnel.getStreamPortController().getSystemConnectionMemo().provides(type);
        }
        if (_loconettunnel!=null) {
            // delegate to the LocoNet tunnel.
            return _loconettunnel.getStreamPortController().getSystemConnectionMemo().provides(type);
        }
        return super.provides(type); // nothing, by default
    }

    /**
     * Provide manager by class.
     */
    @SuppressWarnings("unchecked")  // xpressnet code managed type for cast
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if(T.equals(jmri.ReporterManager.class)){
            return (T) getReporterManager();
        }
        if(T.equals(jmri.MultiMeter.class)){
            return (T) getMultiMeter();
        }
        if (_xnettunnel!=null && _xnettunnel.getStreamPortController().getSystemConnectionMemo().provides(T) ) {
            // delegate to the XPressNet tunnel.
            return _xnettunnel.getStreamPortController().getSystemConnectionMemo().get(T);
        }
        if (_loconettunnel!=null && _loconettunnel.getStreamPortController().getSystemConnectionMemo().provides(T) ) {
            // delegate to the LocoNet tunnel.
            return _loconettunnel.getStreamPortController().getSystemConnectionMemo().get(T);
        }
        return null; // nothing, by default
    }

    /**
     * Configure the common managers for z21 connections. This puts the common
     * manager config in one place.
     */
    public void configureManagers() {
        log.debug("Called Configure Managers");

        if(z21CommandStation == null){
           setRocoZ21CommandStation(new RocoZ21CommandStation());
        }

        // set the broadcast flags so we get messages we may want to hear
        z21CommandStation.setXPressNetMessagesFlag(true);
        z21CommandStation.setXPressNetLocomotiveMessagesFlag(true);
        z21CommandStation.setLocoNetMessagesFlag(true);
        z21CommandStation.setLocoNetLocomotiveMessagesFlag(true);
        z21CommandStation.setLocoNetTurnoutMessagesFlag(true);
        z21CommandStation.setLocoNetOccupancyMessagesFlag(true);

        // and forward the flags to the command station.
        _tc.sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(
                           z21CommandStation.getZ21BroadcastFlags()),null);

        // add an XpressNet Tunnel.
        _xnettunnel = new Z21XPressNetTunnel(this);

        // add an LocoNet Tunnel.
        _loconettunnel = new Z21LocoNetTunnel(this);

        // set up the Reporter Manager
        jmri.InstanceManager.store(getReporterManager(),jmri.ReporterManager.class);

        // setup the MultiMeter
        getMultiMeter();

   }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.roco.z21.z21ActionListBundle");
    }

    /**
     * Provide access to the Command Station for this particular connection.
     * <p>
     * NOTE: Command Station defaults to NULL
     */
    public CommandStation getCommandStation() {
        return commandStation;
    }

    public void setCommandStation(CommandStation c) {
        commandStation = c;
    }

    private CommandStation commandStation = null;

    /**
     * Provide access to the Roco Z21 Command Station for this particular
     * connection.
     * <p>
     * NOTE: Command Station defaults to NULL
     */
    public RocoZ21CommandStation getRocoZ21CommandStation() {
        return z21CommandStation;
    }

    public void setRocoZ21CommandStation(RocoZ21CommandStation c) {
        z21CommandStation = c;
    }

    private RocoZ21CommandStation z21CommandStation = null;

    /**
     * Provide access to the Roco Z21 MultiMeter for this particular
     * connection.
     * <p>
     * NOTE: MultiMeter defaults to NULL
     */
    public jmri.MultiMeter getMultiMeter() {
        if(meter == null){
           meter = new Z21MultiMeter(this);
           jmri.InstanceManager.store(meter,jmri.MultiMeter.class);
        }
        return meter;
    }

    private Z21MultiMeter meter = null;


    void shutdownTunnel(){
        if (_xnettunnel!=null) {
            _xnettunnel.dispose();
            _xnettunnel=null;
        }
    }

    @Override
    public void dispose() {
        shutdownTunnel();
        InstanceManager.deregister(this, Z21SystemConnectionMemo.class);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(Z21SystemConnectionMemo.class);

}
