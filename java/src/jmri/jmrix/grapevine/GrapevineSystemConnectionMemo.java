package jmri.jmrix.grapevine;

import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.TurnoutManager;
import jmri.SensorManager;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimum required SystemConnectionMemo for Grapevine.
 * Expanded for multichar/multiconnection support.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class GrapevineSystemConnectionMemo extends SystemConnectionMemo {

    public GrapevineSystemConnectionMemo() {
        this("G", Bundle.getMessage("MenuSystem"));
    }

    public GrapevineSystemConnectionMemo(@Nonnull String prefix, @Nonnull String name) {
        super(prefix, name);

        register(); // registers general type
        InstanceManager.store(this, GrapevineSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory for the GUI (menu)
        InstanceManager.store(cf = new jmri.jmrix.grapevine.swing.GrapevineComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created GrapevineSystemConnectionMemo, prefix = {}", prefix);
    }

    private SerialTrafficController tc = null;
    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param tc jmri.jmrix.grapevine.SerialTrafficController object to use.
     */
    public void setTrafficController(SerialTrafficController tc){
        this.tc = tc;
        log.debug("Memo {} set GrapevineTrafficController {}", getUserName(), tc);
    }

    /**
     * Get the traffic controller instance associated with this connection memo.
     */
    public SerialTrafficController getTrafficController(){
        if (tc == null) {
            setTrafficController(new SerialTrafficController(this));
            log.debug("Auto create of SerialTrafficController for initial configuration");
        }
        return tc;
    }

    /**
     * Provide Grapevine menu strings.
     *
     * @return bundle file containing action - menuitem pairs
     */
    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.grapevine.GrapevineActionListBundle");
    }

    public void configureManagers() {
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

    @Override
    public void dispose() {
        tc = null;
        InstanceManager.deregister(this, GrapevineSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(GrapevineSystemConnectionMemo.class);

}
