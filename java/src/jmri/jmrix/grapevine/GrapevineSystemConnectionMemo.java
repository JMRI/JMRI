package jmri.jmrix.grapevine;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.TurnoutManager;
import jmri.SensorManager;

/**
 * Minimum required SystemConnectionMemo.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class GrapevineSystemConnectionMemo extends SystemConnectionMemo {

    private jmri.jmrix.swing.ComponentFactory cf = null;

    public GrapevineSystemConnectionMemo() {
        super("G", "Grapevine");

        register(); // registers general type
        InstanceManager.store(this, GrapevineSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.grapevine.swing.GrapevineComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created GrapevineSystemConnectionMemo");
    }

    private SerialTrafficController tc = null;

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param s jmri.jmrix.oaktree.SerialTrafficController object to use.
     */
    public void setTrafficController(SerialTrafficController s){
        tc = s;
    }

    /**
     * Get the traffic controller instance associated with this connection memo.
     */
    public SerialTrafficController getTrafficController(){
        if (tc == null) {
            setTrafficController(new SerialTrafficController());
            log.debug("Auto create of SerialTrafficController for initial configuration");
        }
        return tc;
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.grapevine.GrapevineActionListBundle");
    }

    public void configureManagers(){
        setTurnoutManager(new SerialTurnoutManager(this));
        setLightManager(new SerialLightManager(this));
        setSensorManager(new SerialSensorManager(this));
    }

    /**
     * Provide access to the Sensor Manager for this particular connection.
     * <p>
     * NOTE: Sensor manager defaults to NULL
     */
    public SensorManager getSensorManager() {
        return sensorManager;
    }

    public void setSensorManager(SerialSensorManager s) {
        sensorManager = s;
        getTrafficController().setSensorManager(s);
    }

    private SensorManager sensorManager = null;


    /**
     * Provide access to the Turnout Manager for this particular connection.
     * <p>
     * NOTE: Turnout manager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return turnoutManager;

    }

    public void setTurnoutManager(SerialTurnoutManager t) {
        turnoutManager = t;
    }

    private TurnoutManager turnoutManager = null;

    /**
     * Provide access to the Light Manager for this particular connection.
     * <p>
     * NOTE: Light manager defaults to NULL
     */
    public LightManager getLightManager() {
        return lightManager;

    }

    public void setLightManager(SerialLightManager l) {
        lightManager = l;
    }

    private LightManager lightManager = null;

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        } else if (type.equals(jmri.SensorManager.class)) {
            return true;
        } else if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        } else if (type.equals(jmri.LightManager.class)) {
            return true;
        }
        return super.provides(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
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
        return super.get(T);
    }


    private final static Logger log = LoggerFactory.getLogger(GrapevineSystemConnectionMemo.class);

}
