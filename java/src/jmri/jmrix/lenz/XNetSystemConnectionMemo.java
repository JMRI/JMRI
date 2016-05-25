//XNetSystemConnectionMemo.java
package jmri.jmrix.lenz;

import java.util.ResourceBundle;
import jmri.CommandStation;
import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.PowerManager;
import jmri.ProgrammerManager;
import jmri.SensorManager;
import jmri.ThrottleManager;
import jmri.TurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active and provide general
 * information
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */
public class XNetSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public XNetSystemConnectionMemo(XNetTrafficController xt) {
        super("X", "XPressnet");
        this.xt = xt;
        xt.setSystemConnectionMemo(this);
        register(); // registers general type
        InstanceManager.store(this, XNetSystemConnectionMemo.class); // also register as specific type

        // create and register the XNetComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.lenz.swing.XNetComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        if (log.isDebugEnabled()) {
            log.debug("Created XNetSystemConnectionMemo");
        }
    }

    public XNetSystemConnectionMemo() {
        super("X", "XPressnet");
        register(); // registers general type
        InstanceManager.store(this, XNetSystemConnectionMemo.class); // also register as specific type

        // create and register the XNetComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.lenz.swing.XNetComponentFactory(this), jmri.jmrix.swing.ComponentFactory.class);

        if (log.isDebugEnabled()) {
            log.debug("Created XNetSystemConnectionMemo");
        }
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public XNetTrafficController getXNetTrafficController() {
        return xt;
    }
    private XNetTrafficController xt;

    public void setXNetTrafficController(XNetTrafficController xt) {
        this.xt = xt;
        // in addition to setting the traffic controller in this object,
        // set the systemConnectionMemo in the traffic controller
        xt.setSystemConnectionMemo(this);
    }

    /**
     * Provides access to the Programmer for this particular connection. NOTE:
     * Programmer defaults to null
     */
    public ProgrammerManager getProgrammerManager() {
        return programmerManager;
    }

    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }

    private ProgrammerManager programmerManager = null;

    /*
     * Provides access to the Throttle Manager for this particular connection.
     */
    public ThrottleManager getThrottleManager() {
        if (throttleManager == null) {
            throttleManager = new XNetThrottleManager(this);
        }
        return throttleManager;

    }

    public void setThrottleManager(ThrottleManager t) {
        throttleManager = t;
    }

    private ThrottleManager throttleManager;

    /*
     * Provides access to the Power Manager for this particular connection.
     */
    public PowerManager getPowerManager() {
        if (powerManager == null) {
            powerManager = new XNetPowerManager(this);
        }
        return powerManager;

    }

    public void setPowerManager(PowerManager p) {
        powerManager = p;
    }

    private PowerManager powerManager;

    /*
     * Provides access to the Sensor Manager for this particular connection.
     * NOTE: Sensor manager defaults to NULL
     */
    public SensorManager getSensorManager() {
        return sensorManager;

    }

    public void setSensorManager(SensorManager s) {
        sensorManager = s;
    }

    private SensorManager sensorManager = null;

    /*
     * Provides access to the Turnout Manager for this particular connection.
     * NOTE: Turnout manager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return turnoutManager;

    }

    public void setTurnoutManager(TurnoutManager t) {
        turnoutManager = t;
    }

    private TurnoutManager turnoutManager = null;

    /*
     * Provides access to the Light Manager for this particular connection.
     * NOTE: Light manager defaults to NULL
     */
    public LightManager getLightManager() {
        return lightManager;

    }

    public void setLightManager(LightManager l) {
        lightManager = l;
    }

    private LightManager lightManager = null;

    /*
     * Provides access to the Consist Manager for this particular connection.
     * NOTE: Consist manager defaults to NULL
     */
    public ConsistManager getConsistManager() {
        return consistManager;
    }

    public void setConsistManager(ConsistManager c) {
        consistManager = c;
    }

    private ConsistManager consistManager = null;

    /*
     * Provides access to the Command Station for this particular connection.
     * NOTE: Command Station defaults to NULL
     */
    public CommandStation getCommandStation() {
        return commandStation;
    }

    public void setCommandStation(CommandStation c) {
        commandStation = c;
        if( c instanceof LenzCommandStation) {
           ((LenzCommandStation) c).setTrafficController(xt);
           ((LenzCommandStation) c).setSystemConnectionMemo(this);
        }
    }

    private CommandStation commandStation = null;

    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        } else if (type.equals(jmri.ProgrammerManager.class)) {
            return true;
        } else if (type.equals(jmri.GlobalProgrammerManager.class)) {
            ProgrammerManager p = getProgrammerManager();
            if (p == null) {
                return false;
            }
            return p.isGlobalProgrammerAvailable();
        } else if (type.equals(jmri.AddressedProgrammerManager.class)) {
            ProgrammerManager p = getProgrammerManager();
            if (p == null) {
                return false;
            }
            return p.isAddressedModePossible();
        } else if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        } else if (type.equals(jmri.PowerManager.class)) {
            return true;
        } else if (type.equals(jmri.SensorManager.class)) {
            return true;
        } else if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        } else if (type.equals(jmri.LightManager.class)) {
            return true;
        } else if (type.equals(jmri.ConsistManager.class)) {
            return true;
        } else if (type.equals(jmri.CommandStation.class)) {
            return true;
        } else {
            return false; // nothing, by default
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.ProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }

        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        }
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        if (T.equals(jmri.ConsistManager.class)) {
            return (T) getConsistManager();
        }
        if (T.equals(jmri.CommandStation.class)) {
            return (T) getCommandStation();
        }
        return null; // nothing, by default
    }

    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.lenz.XNetActionListBundle");
    }

    public void dispose() {
        xt = null;
        InstanceManager.deregister(this, XNetSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(XNetSystemConnectionMemo.class.getName());

}
/* @(#)XNetSystemConnectionMemo.java */
