package jmri.jmrix.roco.z21;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.lenz.XNetProgrammerManager;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010 copied from NCE into PowerLine for
 * multiple connections by
 * @author Ken Cameron Copyright (C) 2011 copied from PowerLine into z21 by
 * @author Paul Bender Copyright (C) 2013,2019,2020
 */
public class Z21SystemConnectionMemo extends jmri.jmrix.DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    private Z21XPressNetTunnel _xnettunnel = null;
    private Z21LocoNetTunnel _loconettunnel = null;

    public Z21SystemConnectionMemo() {
        this("Z", "Z21");
    }

    public Z21SystemConnectionMemo(String prefix, String userName) {
        super(prefix, userName);
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
     * @param newtc Z21 traffic controller.
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
     * @param rm reporter manager.
     */
    public void setReporterManager(Z21ReporterManager rm){
        store(rm, ReporterManager.class);
    }

    public Z21ReporterManager getReporterManager() {
        return (Z21ReporterManager) classObjectMap.computeIfAbsent(ReporterManager.class, (Class c) -> { return new Z21ReporterManager(this); });
    }

    /**
     * SensorManager for this instance.
     * @param sm sensor manager.
     */
    public void setSensorManager(Z21SensorManager sm){
        store(sm,SensorManager.class);
    }

    public Z21SensorManager getSensorManager() {
        return (Z21SensorManager) classObjectMap.computeIfAbsent(SensorManager.class, (Class c) -> { return new Z21SensorManager(this); });
    }

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
        if (type.equals(jmri.SensorManager.class)){
           return true;
        }
        if (_xnettunnel!=null) {
            // delegate to the XPressNet tunnel.
            if(_xnettunnel.getStreamPortController().getSystemConnectionMemo().provides(type)) {
               return true;
            } // don't return false here, let the following code run 
        }
        if (_loconettunnel!=null) {
            // delegate to the LocoNet tunnel.
            if(_loconettunnel.getStreamPortController().getSystemConnectionMemo().provides(type)) {
               return true;
            } // don't return false here, let the following code run
            
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
            return super.get(T);
        }
        if(T.equals(jmri.MultiMeter.class)){
            return super.get(T);
        }
        if(T.equals(jmri.SensorManager.class)){
            return super.get(T);
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

        RocoZ21CommandStation z21CommandStation = getRocoZ21CommandStation();

        // set the broadcast flags so we get messages we may want to hear
        z21CommandStation.setXPressNetMessagesFlag(true);
        z21CommandStation.setXPressNetLocomotiveMessagesFlag(true);
        z21CommandStation.setLocoNetMessagesFlag(true);
        z21CommandStation.setLocoNetLocomotiveMessagesFlag(true);
        z21CommandStation.setLocoNetTurnoutMessagesFlag(true);

        // and forward the flags to the command station
        _tc.sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(
                           z21CommandStation.getZ21BroadcastFlags()),null);

        // add an LocoNet Tunnel
        _loconettunnel = (Z21LocoNetTunnel) classObjectMap.computeIfAbsent(Z21LocoNetTunnel.class, (Class c) -> new Z21LocoNetTunnel(this));

        // add an XpressNet Tunnel
        _xnettunnel = (Z21XPressNetTunnel) classObjectMap.computeIfAbsent(Z21XPressNetTunnel.class, (Class c) -> new Z21XPressNetTunnel(this));

        // set up the Reporter Manager
        jmri.InstanceManager.setReporterManager(getReporterManager());

        // set up the SensorManager
        jmri.InstanceManager.setSensorManager(getSensorManager());

        // but make sure the LocoNet memo is set (for one feedback message).
        XNetProgrammerManager xpm = _xnettunnel.getStreamPortController().getSystemConnectionMemo().getProgrammerManager();
        if ( xpm instanceof Z21XNetProgrammerManager) {
            ((Z21XNetProgrammerManager) xpm).setLocoNetMemo(_loconettunnel.getStreamPortController().getSystemConnectionMemo());
        }
        // setup the MultiMeter
        getMultiMeter();

        // setup the HeartBeat
        getHeartBeat();

        register();
   }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.roco.z21.z21ActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    /**
     * Provide access to the Command Station for this particular connection.
     * <p>
     * NOTE: Command Station defaults to NULL
     * @return command station, may be null.
     */
    public CommandStation getCommandStation() {
        return get(CommandStation.class);
    }

    public void setCommandStation(CommandStation c) {
        store(c,CommandStation.class);
    }

    /**
     * Provide access to the Roco Z21 Command Station for this particular
     * connection.
     * <p>
     * NOTE: Command Station defaults to NULL
     * @return Roco Z21 Command Station, may be null.
     */
    public RocoZ21CommandStation getRocoZ21CommandStation() {
        return (RocoZ21CommandStation) classObjectMap.computeIfAbsent(RocoZ21CommandStation.class, (Class c) -> new RocoZ21CommandStation());
    }

    public void setRocoZ21CommandStation(RocoZ21CommandStation c) {
        store(c,RocoZ21CommandStation.class);
    }

    /**
     * Provide access to the Roco Z21 MultiMeter for this particular
     * connection.
     * <p>
     * NOTE: MultiMeter defaults to NULL
     * @return MultiMeter, creates new if null.
     */
    public MultiMeter getMultiMeter() {
        return (MultiMeter) classObjectMap.computeIfAbsent(MultiMeter.class, (Class c) -> { return new Z21MultiMeter(this); });
    }

    /**
     * Provide access to the Z21HeartBeat instance for this connection.
     * <p>
     * NOTE: HeartBeat defaults to NULL
     * @return the HeartBeat, creates new if null.
     */
    public Z21HeartBeat getHeartBeat() {
        if(heartBeat == null){
           heartBeat = new Z21HeartBeat(this);
        }
        return heartBeat;
    }
    
    private Z21HeartBeat heartBeat = null;

    void shutdownTunnel(){
        if (_xnettunnel!=null) {
            _xnettunnel.dispose();
            _xnettunnel=null;
        }
    }

    @Override
    public void dispose() {
        if(heartBeat!=null) {
           heartBeat.dispose();
        }
        shutdownTunnel();
        InstanceManager.deregister(this, Z21SystemConnectionMemo.class);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(Z21SystemConnectionMemo.class);

}
