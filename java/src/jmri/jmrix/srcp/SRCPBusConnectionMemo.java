package jmri.jmrix.srcp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
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
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class SRCPBusConnectionMemo extends DefaultSystemConnectionMemo implements SRCPListener, ConfiguringSystemConnectionMemo {

    private int _bus = 0;
    private boolean configured = false;

    public SRCPBusConnectionMemo(SRCPTrafficController et, String Prefix, int bus) {
        super(Prefix + bus, "SRCP:" + bus);
        this.et = et;
        _bus = bus;
        log.debug("Created SRCPBusConnectionMemo for bus {}", bus);
        et.addSRCPListener(this);
        et.sendSRCPMessage(new SRCPMessage("GET " + bus + " DESCRIPTION\n"), null);
        configured = false;
    }

    private jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     * @return SRCP Traffic Controller.
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
        register();
        log.debug("Manager configuration complete for bus {}", _bus );
    }

    /**
     * package protected function to get the bus associated with this memo.
     *
     * @return integer bus number
     */
    int getBus() {
        return _bus;
    }

    /**
     * Provides access to the Programmer for this particular connection.
     * <p>
     * NOTE: Programmer defaults to null
     * @return programmer manager.
     */
    public SRCPProgrammerManager getProgrammerManager() {
        return get(GlobalProgrammerManager.class);
    }

    public void setProgrammerManager(SRCPProgrammerManager p) {
        store(p,GlobalProgrammerManager.class);
    }

    /*
     * Provides access to the Throttle Manager for this particular connection.
     * NOTE: Throttle Manager defaults to null
     */
    public ThrottleManager getThrottleManager() {
        return get(ThrottleManager.class);
    }

    public void setThrottleManager(ThrottleManager t) {
        store(t,ThrottleManager.class);
    }

    /*
     * Provides access to the Clock Control for this particular connection.
     * NOTE: May return null if the Clock Control has not been set.
     */
    public ClockControl getClockControl() {
        return get(ClockControl.class);
    }

    public void setClockControl(ClockControl t) {
        store(t,ClockControl.class);
        InstanceManager.store(t, ClockControl.class);
        InstanceManager.setDefault(ClockControl.class, t);
    }

    /*
     * Provides access to the PowerManager for this particular connection.
     */
    public PowerManager getPowerManager() {
        return get(PowerManager.class);
    }

    public void setPowerManager(PowerManager p) {
        store(p,PowerManager.class);
    }

    /*
     * Provides access to the SensorManager for this particular connection.
     */
    public SensorManager getSensorManager() {
        return get(SensorManager.class);
    }

    public void setSensorManager(SensorManager s) {
        store(s,SensorManager.class);
    }

    /*
     * Provides access to the TurnoutManager for this particular connection.
     * NOTE: TurnoutManager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return get(TurnoutManager.class);
    }

    public void setTurnoutManager(TurnoutManager t) {
        store(t,TurnoutManager.class);
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
        if (et != null) {
            et = null;
        }
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
        log.debug("SimpleNode Reply called with {}", n.toString());
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
                    log.debug("child node type {} value {}", child.toString(), child.jjtGetValue());
                    if (child instanceof jmri.jmrix.srcp.parser.ASTdevicegroup) {
                        String DeviceType = (String) child.jjtGetValue();
                        switch (DeviceType) {
                            case "FB":
                                setSensorManager(new SRCPSensorManager(this));
                                InstanceManager.setSensorManager(getSensorManager());
                                break;
                            case "GA":
                                setTurnoutManager(new SRCPTurnoutManager(this));
                                InstanceManager.setTurnoutManager(getTurnoutManager());
                                break;
                            case "SM":
                                setProgrammerManager(new SRCPProgrammerManager(new SRCPProgrammer(this), this));
                                InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
                                break;
                            case "POWER":
                                setPowerManager(new SRCPPowerManager(this, _bus));
                                InstanceManager.store(getPowerManager(), PowerManager.class);
                                break;
                            case "GL":
                                setThrottleManager(new SRCPThrottleManager(this));
                                InstanceManager.setThrottleManager(getThrottleManager());
                                break;
                            case "TIME":
                                setClockControl(new SRCPClockControl(this));
                                break;
                            default:
                                log.warn("unexpected DeviceType");
                                break;
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
