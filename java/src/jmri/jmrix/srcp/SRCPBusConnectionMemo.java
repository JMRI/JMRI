package jmri.jmrix.srcp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.ClockControl;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.PowerManager;
import jmri.SensorManager;
import jmri.ThrottleManager;
import jmri.TurnoutManager;
import jmri.jmrix.srcp.parser.ASTinfo;
import jmri.jmrix.srcp.parser.SimpleNode;
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
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class SRCPBusConnectionMemo extends jmri.jmrix.SystemConnectionMemo implements SRCPListener {

    private int _bus = 0;
    private boolean configured = false;

    public SRCPBusConnectionMemo(SRCPTrafficController et, String Prefix, int bus) {
        super(Prefix + bus, "SRCP:" + bus);
        this.et = et;
        //this.et.setSystemConnectionMemo(this);
        _bus = bus;
        register();
        log.debug("Created SRCPBusConnectionMemo for bus " + bus);
        et.addSRCPListener(this);
        et.sendSRCPMessage(new SRCPMessage("GET " + bus + " DESCRIPTION\n"), null);
        configured = false;
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
    }

    private SRCPTrafficController et;

    /**
     * Configure the common managers for Internal connections. This puts the
     * common manager config in one place.
     */
    @SuppressFBWarnings(value = "UW_UNCOND_WAIT", justification="false postive, guarded by while statement")
    public void configureManagers() {
        while(!configured){
           // wait for the managers to be configured.
           synchronized(this){
              try {
                  this.wait();
              } catch(java.lang.InterruptedException ie){
                // just catch the error and re-check our condition.
              }
           }
        }
        log.debug("Manager configuration complete for bus " + _bus );
    }

    /**
     * package protected function to get the bus associated with this memo.
     *
     * @return integer bus number.
     */
    int getBus() {
        return _bus;
    }

    /**
     * Provides access to the Programmer for this particular connection. NOTE:
     * Programmer defaults to null
     */
    public SRCPProgrammerManager getProgrammerManager() {
        return programmerManager;
    }

    public void setProgrammerManager(SRCPProgrammerManager p) {
        programmerManager = p;
    }

    private SRCPProgrammerManager programmerManager = null;

    /*
     * Provides access to the Throttle Manager for this particular connection.
     * NOTE: Throttle Manager defaults to null
     */
    public ThrottleManager getThrottleManager() {
        return throttleManager;

    }

    public void setThrottleManager(ThrottleManager t) {
        throttleManager = t;
    }

    private ThrottleManager throttleManager;

    /*
     * Provides access to the Clock Control for this particular connection.
     * NOTE: May return null if the Clock Control has not been set.
     */
    public ClockControl getClockControl() {
        return clockControl;

    }

    public void setClockControl(ClockControl t) {
        clockControl = t;
        InstanceManager.store(clockControl, ClockControl.class);
        InstanceManager.setDefault(ClockControl.class, clockControl);
    }

    private ClockControl clockControl = null;

    /*
     * Provides access to the PowerManager for this particular connection.
     */
    public PowerManager getPowerManager() {
        return powerManager;
    }

    public void setPowerManager(PowerManager p) {
        powerManager = p;
    }

    private PowerManager powerManager;

    /*
     * Provides access to the SensorManager for this particular connection.
     */
    public SensorManager getSensorManager() {
        return sensorManager;

    }

    public void setSensorManager(SensorManager s) {
        sensorManager = s;
    }

    private SensorManager sensorManager = null;

    /*
     * Provides access to the TurnoutManager for this particular connection.
     * NOTE: TurnoutManager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return turnoutManager;

    }

    public void setTurnoutManager(TurnoutManager t) {
        turnoutManager = t;
    }

    private TurnoutManager turnoutManager = null;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        }
        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(jmri.ClockControl.class)) {
            return (T) getClockControl();
        }
        return super.get(T);
    }

    /**
     * Tells which managers this class provides.
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return (null != programmerManager);
        }
        if (type.equals(jmri.ThrottleManager.class)) {
            return (null != throttleManager);
        }
        if (type.equals(jmri.PowerManager.class)) {
            return (null != powerManager);
        }
        if (type.equals(jmri.SensorManager.class)) {
            return (null != sensorManager);
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return (null != turnoutManager);
        }
        if (type.equals(jmri.ClockControl.class)) {
            return (null != clockControl);
        }
        return super.provides(type); 
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.srcp.SrcpActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        et = null;
        InstanceManager.deregister(this, SRCPBusConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    // functions for the SRCP Listener interface.
    @Override
    public void message(SRCPMessage m) {
    }

    @Override
    public void reply(SRCPReply m) {
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification="Notify passing reply event, not state")
    public void reply(jmri.jmrix.srcp.parser.SimpleNode n) {
        log.debug("SimpleNode Reply called with " + n.toString());
        reply(new SRCPReply(n));
        if (n.jjtGetChild(1) instanceof ASTinfo) {
            jmri.jmrix.srcp.parser.SimpleNode infonode
                    = (jmri.jmrix.srcp.parser.SimpleNode) n.jjtGetChild(1);
            if (!((String) ((SimpleNode) (infonode.jjtGetChild(0))).jjtGetValue()).equals("" + _bus)) {
                return; // not for this bus.
            }          // Look for description information for this bus, and configure the
            // managers for this bus.
            if (infonode.jjtGetChild(1) instanceof jmri.jmrix.srcp.parser.ASTdescription) {
                SimpleNode descnode = (SimpleNode) infonode.jjtGetChild(1);
                for (int i = 0; i < descnode.jjtGetNumChildren(); i++) {
                    jmri.jmrix.srcp.parser.SimpleNode child
                            = (jmri.jmrix.srcp.parser.SimpleNode) descnode.jjtGetChild(i);
                    log.debug("child node type " + child.toString()
                            + " value " + (String) child.jjtGetValue());
                    if (child instanceof jmri.jmrix.srcp.parser.ASTdevicegroup) {
                        String DeviceType = (String) child.jjtGetValue();
                        if (DeviceType.equals("FB")) {
                            setSensorManager(new jmri.jmrix.srcp.SRCPSensorManager(this, _bus));
                            jmri.InstanceManager.setSensorManager(getSensorManager());

                        } else if (DeviceType.equals("GA")) {
                            setTurnoutManager(new jmri.jmrix.srcp.SRCPTurnoutManager(this, _bus));
                            jmri.InstanceManager.setTurnoutManager(getTurnoutManager());
                        } else if (DeviceType.equals("SM")) {
                            setProgrammerManager(new SRCPProgrammerManager(new SRCPProgrammer(this), this));
                            jmri.InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
                        } else if (DeviceType.equals("POWER")) {
                            setPowerManager(new jmri.jmrix.srcp.SRCPPowerManager(this, _bus));
                            jmri.InstanceManager.store(getPowerManager(), jmri.PowerManager.class);
                        } else if (DeviceType.equals("GL")) {
                            setThrottleManager(new jmri.jmrix.srcp.SRCPThrottleManager(this));
                            jmri.InstanceManager.setThrottleManager(getThrottleManager());
                        } else if (DeviceType.equals("TIME")) {
                            setClockControl(new jmri.jmrix.srcp.SRCPClockControl(this));
                        }
                    }
                }
                configured = true;
                synchronized(this) {
                    this.notifyAll(); // wake up any thread that called configureManagers().
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SRCPBusConnectionMemo.class);

}



