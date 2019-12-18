package jmri.jmrix.lenz;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.AddressedProgrammerManager;
import jmri.CommandStation;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.NamedBean;
import jmri.PowerManager;
import jmri.SensorManager;
import jmri.ThrottleManager;
import jmri.TurnoutManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.util.NamedBeanComparator;

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
 */
public class XNetSystemConnectionMemo extends SystemConnectionMemo {

    public XNetSystemConnectionMemo(XNetTrafficController xt) {
        super("X", Bundle.getMessage("MenuXpressNet"));
        this.xt = xt;
        xt.setSystemConnectionMemo(this);
        this.setLenzCommandStation(xt.getCommandStation());
        register(); // registers general type
        InstanceManager.store(this, XNetSystemConnectionMemo.class); // also register as specific type

        // create and register the XNetComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.lenz.swing.XNetComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created XNetSystemConnectionMemo");
    }

    public XNetSystemConnectionMemo() {
        super("X", Bundle.getMessage("MenuXpressNet"));
        register(); // registers general type
        InstanceManager.store(this, XNetSystemConnectionMemo.class); // also register as specific type

        // create and register the XNetComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.lenz.swing.XNetComponentFactory(this), jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created XNetSystemConnectionMemo");
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provide access to the TrafficController for this particular connection.
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
        // and make sure the Lenz command station is set.
        this.setLenzCommandStation(xt.getCommandStation());
    }

    /**
     * Provide access to the Programmer for this particular connection.
     * <p>
     * NOTE: Programmer defaults to null
     */
    public XNetProgrammerManager getProgrammerManager() {
        return programmerManager;
    }

    public void setProgrammerManager(XNetProgrammerManager p) {
        programmerManager = p;
    }

    private XNetProgrammerManager programmerManager = null;

    /*
     * Provide access to the Throttle Manager for this particular connection.
     */
    public ThrottleManager getThrottleManager() {
        if (throttleManager == null) {
           if (xt.getCommandStation().getCommandStationType() == 0x10 ||
               xt.getCommandStation().getCommandStationType() == 0x04 ) {
              throttleManager = new jmri.jmrix.roco.RocoXNetThrottleManager(this);
           } else {
              throttleManager = new XNetThrottleManager(this);
           }
        }
        return throttleManager;
    }

    public void setThrottleManager(ThrottleManager t) {
        throttleManager = t;
    }

    private ThrottleManager throttleManager;

    /*
     * Provide access to the PowerManager for this particular connection.
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

    /**
     * Provide access to the SensorManager for this particular connection.
     * <p>
     * NOTE: SensorManager defaults to NULL
     */
    public SensorManager getSensorManager() {
        return sensorManager;

    }

    public void setSensorManager(SensorManager s) {
        sensorManager = s;
    }

    private SensorManager sensorManager = null;

    /**
     * Provide access to the TurnoutManager for this particular connection.
     * <p>
     * NOTE: TurnoutManager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return turnoutManager;

    }

    public void setTurnoutManager(TurnoutManager t) {
        turnoutManager = t;
    }

    private TurnoutManager turnoutManager = null;

    /**
     * Provide access to the LightManager for this particular connection.
     * <p>
     * NOTE: LightManager defaults to NULL
     */
    public LightManager getLightManager() {
        return lightManager;

    }

    public void setLightManager(LightManager l) {
        lightManager = l;
    }

    private LightManager lightManager = null;

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
        if (c instanceof LenzCommandStation && lenzCommandStation == null) {
            setLenzCommandStation((LenzCommandStation) c);
        }
    }

    private CommandStation commandStation = null;

    /**
     * Provide access to the Lenz Command Station for this particular connection.
     * <p>
     * NOTE: Lenz Command Station defaults to NULL
     */
    public LenzCommandStation getLenzCommandStation() {
        return lenzCommandStation;
    }

    public void setLenzCommandStation(LenzCommandStation c) {
        lenzCommandStation = c;
        lenzCommandStation.setTrafficController(xt);
        lenzCommandStation.setSystemConnectionMemo(this);
    }

    private LenzCommandStation lenzCommandStation = null;

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        } else if (type.equals(GlobalProgrammerManager.class)) {
            GlobalProgrammerManager p = getProgrammerManager();
            if (p == null) {
                return false;
            }
            return p.isGlobalProgrammerAvailable();
        } else if (type.equals(AddressedProgrammerManager.class)) {
            AddressedProgrammerManager p = getProgrammerManager();
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
            try {
               // multimouse doesn't support consists.
               return (getLenzCommandStation().getCommandStationType()!=0x10 );
            } catch (java.lang.NullPointerException npe) {
                // initialization may not be complete.  Assume true.
                return true;
            }
        } else if (type.equals(jmri.CommandStation.class)) {
            try {
                // compact/commander do not support the instructions required 
                // for command station interface.
                return (getLenzCommandStation().getCommandStationType() != 0x02  && getCommandStation()!=null );
            } catch (java.lang.NullPointerException npe) {
                // initialization may not be complete, return false if no 
                // command station object.
                return (getCommandStation()!=null);
            }
        }
        return super.provides(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
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
        return super.get(T);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.lenz.XNetActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        xt = null;
        InstanceManager.deregister(this, XNetSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(XNetSystemConnectionMemo.class);

}
